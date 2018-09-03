/*
 *  Copyright (C) 2008-2009 Piotr Wendykier
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.edu.emory.mathcs.restoretools.spectral;

import ij.ImagePlus;
import ij.io.Opener;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.cern.colt.matrix.tfloat.FloatMatrix2D;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;
import com.edu.emory.mathcs.restoretools.spectral.gtik.DoublePeriodicGeneralizedTikhonov2D;
import com.edu.emory.mathcs.restoretools.spectral.gtik.DoubleReflexiveGeneralizedTikhonov2D;
import com.edu.emory.mathcs.restoretools.spectral.gtik.FloatPeriodicGeneralizedTikhonov2D;
import com.edu.emory.mathcs.restoretools.spectral.gtik.FloatReflexiveGeneralizedTikhonov2D;
import com.edu.emory.mathcs.restoretools.spectral.tik.DoublePeriodicTikhonov2D;
import com.edu.emory.mathcs.restoretools.spectral.tik.DoubleReflexiveTikhonov2D;
import com.edu.emory.mathcs.restoretools.spectral.tik.FloatPeriodicTikhonov2D;
import com.edu.emory.mathcs.restoretools.spectral.tik.FloatReflexiveTikhonov2D;
import com.edu.emory.mathcs.restoretools.spectral.tsvd.DoublePeriodicTruncatedSVD2D;
import com.edu.emory.mathcs.restoretools.spectral.tsvd.DoubleReflexiveTruncatedSVD2D;
import com.edu.emory.mathcs.restoretools.spectral.tsvd.FloatPeriodicTruncatedSVD2D;
import com.edu.emory.mathcs.restoretools.spectral.tsvd.FloatReflexiveTruncatedSVD2D;
import com.edu.emory.mathcs.utils.ConcurrencyUtils;

/**
 * Benchmark for Parallel Spectral Deconvolution 2D
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class Benchmark2D {

    private static final String path = "/home/pwendyk/images/spectral/";

    private static final String blur_image = "astronaut-blur.png";

    private static final String psf_image = "astronaut-psf.png";

    private static final DoubleMatrix2D doubleStencil = new DenseDoubleMatrix2D(3, 3).assign(new double[] { 0, 1, 0, 1, -4, 1, 0, 1, 0 });

    private static final FloatMatrix2D floatStencil = new DenseFloatMatrix2D(3, 3).assign(new float[] { 0, 1, 0, 1, -4, 1, 0, 1, 0 });

    private static final ResizingType resizing = ResizingType.NONE;

    private static final OutputType output = OutputType.FLOAT;

    private static final int NITER = 20;

    private static final int threshold = 0;

    private static final double double_regParam_deblur = 0.01;

    private static final float float_regParam_deblur = 0.01f;

    private static final double double_regParam_update = 0.02;

    private static final float float_regParam_update = 0.02f;

    private static final String format = "%.1f";

    public static void benchmarkDoubleTSVD_Periodic_2D(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        Opener o = new Opener();
        ImagePlus blurImage = o.openImage(path + blur_image);
        ImagePlus psfImage = o.openImage(path + psf_image);
        double av_time_deblur = 0;
        double av_time_deblur_regParam = 0;
        double av_time_update = 0;
        long elapsedTime_deblur = 0;
        long elapsedTime_deblur_regParam = 0;
        long elapsedTime_update = 0;
        System.out.println("Benchmarking DoubleTSVD_Periodic_2D using " + threads + " threads");
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            DoublePeriodicTruncatedSVD2D tsvd = new DoublePeriodicTruncatedSVD2D(blurImage, psfImage, resizing, output, false, -1, threshold);
            ImagePlus imX = tsvd.deconvolve();
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;

            elapsedTime_deblur_regParam = System.nanoTime();
            tsvd = new DoublePeriodicTruncatedSVD2D(blurImage, psfImage, resizing, output, false, double_regParam_deblur, threshold);
            imX = tsvd.deconvolve();
            elapsedTime_deblur_regParam = System.nanoTime() - elapsedTime_deblur_regParam;
            av_time_deblur_regParam = av_time_deblur_regParam + elapsedTime_deblur_regParam;

            elapsedTime_update = System.nanoTime();
            tsvd.update(double_regParam_update, threshold, imX);
            elapsedTime_update = System.nanoTime() - elapsedTime_update;
            av_time_update = av_time_update + elapsedTime_update;

            tsvd = null;
            imX = null;
            
        }
        blurImage = null;
        psfImage = null;
        System.out.println("Average execution time (deblur()): " + String.format(format, av_time_deblur / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (deblur(regParam)): " + String.format(format, av_time_deblur_regParam / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (update()): " + String.format(format, av_time_update / 1000000000.0 / (double) NITER) + " sec");
        writeResultsToFile("DoubleTSVD_Periodic_2D_" + threads + "_threads.txt", (double) av_time_deblur / 1000000000.0 / (double) NITER, (double) av_time_deblur_regParam / 1000000000.0 / (double) NITER, (double) av_time_update / 1000000000.0 / (double) NITER);
    }

    public static void benchmarkDoubleTSVD_Reflexive_2D(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        Opener o = new Opener();
        ImagePlus blurImage = o.openImage(path + blur_image);
        ImagePlus psfImage = o.openImage(path + psf_image);
        double av_time_deblur = 0;
        double av_time_deblur_regParam = 0;
        double av_time_update = 0;
        long elapsedTime_deblur = 0;
        long elapsedTime_deblur_regParam = 0;
        long elapsedTime_update = 0;
        System.out.println("Benchmarking DoubleTSVD_Reflexive_2D using " + threads + " threads");
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            DoubleReflexiveTruncatedSVD2D tsvd = new DoubleReflexiveTruncatedSVD2D(blurImage, psfImage, resizing, output, false, -1, threshold);
            ImagePlus imX = tsvd.deconvolve();
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;

            elapsedTime_deblur_regParam = System.nanoTime();
            tsvd = new DoubleReflexiveTruncatedSVD2D(blurImage, psfImage, resizing, output, false, double_regParam_deblur, threshold);
            imX = tsvd.deconvolve();
            elapsedTime_deblur_regParam = System.nanoTime() - elapsedTime_deblur_regParam;
            av_time_deblur_regParam = av_time_deblur_regParam + elapsedTime_deblur_regParam;

            elapsedTime_update = System.nanoTime();
            tsvd.update(double_regParam_update, threshold, imX);
            elapsedTime_update = System.nanoTime() - elapsedTime_update;
            av_time_update = av_time_update + elapsedTime_update;

            imX = null;
            tsvd = null;
            
        }
        blurImage = null;
        psfImage = null;
        System.out.println("Average execution time (deblur()): " + String.format(format, av_time_deblur / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (deblur(regParam)): " + String.format(format, av_time_deblur_regParam / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (update()): " + String.format(format, av_time_update / 1000000000.0 / (double) NITER) + " sec");
        writeResultsToFile("DoubleTSVD_Reflexive_2D_" + threads + "_threads.txt", (double) av_time_deblur / 1000000000.0 / (double) NITER, (double) av_time_deblur_regParam / 1000000000.0 / (double) NITER, (double) av_time_update / 1000000000.0 / (double) NITER);
    }

    public static void benchmarkDoubleTikhonov_Periodic_2D(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        Opener o = new Opener();
        ImagePlus blurImage = o.openImage(path + blur_image);
        ImagePlus psfImage = o.openImage(path + psf_image);
        double av_time_deblur = 0;
        double av_time_deblur_regParam = 0;
        double av_time_update = 0;
        long elapsedTime_deblur = 0;
        long elapsedTime_deblur_regParam = 0;
        long elapsedTime_update = 0;
        System.out.println("Benchmarking DoubleTikhonov_Periodic_2D using " + threads + " threads");
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            DoublePeriodicTikhonov2D tsvd = new DoublePeriodicTikhonov2D(blurImage, psfImage, resizing, output, false, -1, threshold);
            ImagePlus imX = tsvd.deconvolve();
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;

            elapsedTime_deblur_regParam = System.nanoTime();
            tsvd = new DoublePeriodicTikhonov2D(blurImage, psfImage, resizing, output, false, double_regParam_deblur, threshold);
            imX = tsvd.deconvolve();
            elapsedTime_deblur_regParam = System.nanoTime() - elapsedTime_deblur_regParam;
            av_time_deblur_regParam = av_time_deblur_regParam + elapsedTime_deblur_regParam;

            elapsedTime_update = System.nanoTime();
            tsvd.update(double_regParam_update, threshold, imX);
            elapsedTime_update = System.nanoTime() - elapsedTime_update;
            av_time_update = av_time_update + elapsedTime_update;

            imX = null;
            tsvd = null;
            
        }
        blurImage = null;
        psfImage = null;
        System.out.println("Average execution time (deblur()): " + String.format(format, av_time_deblur / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (deblur(regParam)): " + String.format(format, av_time_deblur_regParam / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (update()): " + String.format(format, av_time_update / 1000000000.0 / (double) NITER) + " sec");
        writeResultsToFile("DoubleTikhonov_Periodic_2D_" + threads + "_threads.txt", (double) av_time_deblur / 1000000000.0 / (double) NITER, (double) av_time_deblur_regParam / 1000000000.0 / (double) NITER, (double) av_time_update / 1000000000.0 / (double) NITER);
    }

    public static void benchmarkDoubleTikhonov_Reflexive_2D(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        Opener o = new Opener();
        ImagePlus blurImage = o.openImage(path + blur_image);
        ImagePlus psfImage = o.openImage(path + psf_image);
        double av_time_deblur = 0;
        double av_time_deblur_regParam = 0;
        double av_time_update = 0;
        long elapsedTime_deblur = 0;
        long elapsedTime_deblur_regParam = 0;
        long elapsedTime_update = 0;
        System.out.println("Benchmarking DoubleTikhonov_Reflexive_2D using " + threads + " threads");
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            DoubleReflexiveTikhonov2D tsvd = new DoubleReflexiveTikhonov2D(blurImage, psfImage, resizing, output, false, -1, threshold);
            ImagePlus imX = tsvd.deconvolve();
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;

            elapsedTime_deblur_regParam = System.nanoTime();
            tsvd = new DoubleReflexiveTikhonov2D(blurImage, psfImage, resizing, output, false, double_regParam_deblur, threshold);
            imX = tsvd.deconvolve();
            elapsedTime_deblur_regParam = System.nanoTime() - elapsedTime_deblur_regParam;
            av_time_deblur_regParam = av_time_deblur_regParam + elapsedTime_deblur_regParam;

            elapsedTime_update = System.nanoTime();
            tsvd.update(double_regParam_update, threshold, imX);
            elapsedTime_update = System.nanoTime() - elapsedTime_update;
            av_time_update = av_time_update + elapsedTime_update;

            imX = null;
            tsvd = null;
            
        }
        blurImage = null;
        psfImage = null;
        System.out.println("Average execution time (deblur()): " + String.format(format, av_time_deblur / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (deblur(regParam)): " + String.format(format, av_time_deblur_regParam / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (update()): " + String.format(format, av_time_update / 1000000000.0 / (double) NITER) + " sec");
        writeResultsToFile("DoubleTikhonov_Reflexive_2D_" + threads + "_threads.txt", (double) av_time_deblur / 1000000000.0 / (double) NITER, (double) av_time_deblur_regParam / 1000000000.0 / (double) NITER, (double) av_time_update / 1000000000.0 / (double) NITER);
    }

    public static void benchmarkDoubleGTikhonov_Periodic_2D(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        Opener o = new Opener();
        ImagePlus blurImage = o.openImage(path + blur_image);
        ImagePlus psfImage = o.openImage(path + psf_image);
        double av_time_deblur = 0;
        double av_time_deblur_regParam = 0;
        double av_time_update = 0;
        long elapsedTime_deblur = 0;
        long elapsedTime_deblur_regParam = 0;
        long elapsedTime_update = 0;
        System.out.println("Benchmarking DoubleGTikhonov_Periodic_2D using " + threads + " threads");
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            DoublePeriodicGeneralizedTikhonov2D tsvd = new DoublePeriodicGeneralizedTikhonov2D(blurImage, psfImage, doubleStencil, resizing, output, false, -1, threshold);
            ImagePlus imX = tsvd.deconvolve();
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;

            elapsedTime_deblur_regParam = System.nanoTime();
            tsvd = new DoublePeriodicGeneralizedTikhonov2D(blurImage, psfImage, doubleStencil, resizing, output, false, double_regParam_deblur, threshold);
            imX = tsvd.deconvolve();
            elapsedTime_deblur_regParam = System.nanoTime() - elapsedTime_deblur_regParam;
            av_time_deblur_regParam = av_time_deblur_regParam + elapsedTime_deblur_regParam;

            elapsedTime_update = System.nanoTime();
            tsvd.update(double_regParam_update, threshold, imX);
            elapsedTime_update = System.nanoTime() - elapsedTime_update;
            av_time_update = av_time_update + elapsedTime_update;

            imX = null;
            tsvd = null;
            
        }
        blurImage = null;
        psfImage = null;
        System.out.println("Average execution time (deblur()): " + String.format(format, av_time_deblur / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (deblur(regParam)): " + String.format(format, av_time_deblur_regParam / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (update()): " + String.format(format, av_time_update / 1000000000.0 / (double) NITER) + " sec");
        writeResultsToFile("DoubleGTikhonov_Periodic_2D_" + threads + "_threads.txt", (double) av_time_deblur / 1000000000.0 / (double) NITER, (double) av_time_deblur_regParam / 1000000000.0 / (double) NITER, (double) av_time_update / 1000000000.0 / (double) NITER);
    }

    public static void benchmarkDoubleGTikhonov_Reflexive_2D(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        Opener o = new Opener();
        ImagePlus blurImage = o.openImage(path + blur_image);
        ImagePlus psfImage = o.openImage(path + psf_image);
        double av_time_deblur = 0;
        double av_time_deblur_regParam = 0;
        double av_time_update = 0;
        long elapsedTime_deblur = 0;
        long elapsedTime_deblur_regParam = 0;
        long elapsedTime_update = 0;
        System.out.println("Benchmarking DoubleGTikhonov_Reflexive_2D using " + threads + " threads");
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            DoubleReflexiveGeneralizedTikhonov2D tsvd = new DoubleReflexiveGeneralizedTikhonov2D(blurImage, psfImage, doubleStencil, resizing, output, false, -1, threshold);
            ImagePlus imX = tsvd.deconvolve();
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;

            elapsedTime_deblur_regParam = System.nanoTime();
            tsvd = new DoubleReflexiveGeneralizedTikhonov2D(blurImage, psfImage, doubleStencil, resizing, output, false, double_regParam_deblur, threshold);
            imX = tsvd.deconvolve();
            elapsedTime_deblur_regParam = System.nanoTime() - elapsedTime_deblur_regParam;
            av_time_deblur_regParam = av_time_deblur_regParam + elapsedTime_deblur_regParam;

            elapsedTime_update = System.nanoTime();
            tsvd.update(double_regParam_update, threshold, imX);
            elapsedTime_update = System.nanoTime() - elapsedTime_update;
            av_time_update = av_time_update + elapsedTime_update;

            imX = null;
            tsvd = null;
            
        }
        blurImage = null;
        psfImage = null;
        System.out.println("Average execution time (deblur()): " + String.format(format, av_time_deblur / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (deblur(regParam)): " + String.format(format, av_time_deblur_regParam / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (update()): " + String.format(format, av_time_update / 1000000000.0 / (double) NITER) + " sec");
        writeResultsToFile("DoubleGTikhonov_Reflexive_2D_" + threads + "_threads.txt", (double) av_time_deblur / 1000000000.0 / (double) NITER, (double) av_time_deblur_regParam / 1000000000.0 / (double) NITER, (double) av_time_update / 1000000000.0 / (double) NITER);
    }

    public static void benchmarkFloatTSVD_Periodic_2D(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        Opener o = new Opener();
        ImagePlus blurImage = o.openImage(path + blur_image);
        ImagePlus psfImage = o.openImage(path + psf_image);
        double av_time_deblur = 0;
        double av_time_deblur_regParam = 0;
        double av_time_update = 0;
        long elapsedTime_deblur = 0;
        long elapsedTime_deblur_regParam = 0;
        long elapsedTime_update = 0;
        System.out.println("Benchmarking FloatTSVD_Periodic_2D using " + threads + " threads");
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            FloatPeriodicTruncatedSVD2D tsvd = new FloatPeriodicTruncatedSVD2D(blurImage, psfImage, resizing, output, false, -1, threshold);
            ImagePlus imX = tsvd.deconvolve();
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;

            elapsedTime_deblur_regParam = System.nanoTime();
            tsvd = new FloatPeriodicTruncatedSVD2D(blurImage, psfImage, resizing, output, false, float_regParam_deblur, threshold);
            imX = tsvd.deconvolve();
            elapsedTime_deblur_regParam = System.nanoTime() - elapsedTime_deblur_regParam;
            av_time_deblur_regParam = av_time_deblur_regParam + elapsedTime_deblur_regParam;

            elapsedTime_update = System.nanoTime();
            tsvd.update(float_regParam_update, threshold, imX);
            elapsedTime_update = System.nanoTime() - elapsedTime_update;
            av_time_update = av_time_update + elapsedTime_update;

            tsvd = null;
            imX = null;
            
        }
        blurImage = null;
        psfImage = null;
        System.out.println("Average execution time (deblur()): " + String.format(format, av_time_deblur / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (deblur(regParam)): " + String.format(format, av_time_deblur_regParam / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (update()): " + String.format(format, av_time_update / 1000000000.0 / (double) NITER) + " sec");
        writeResultsToFile("FloatTSVD_Periodic_2D_" + threads + "_threads.txt", (double) av_time_deblur / 1000000000.0 / (double) NITER, (double) av_time_deblur_regParam / 1000000000.0 / (double) NITER, (double) av_time_update / 1000000000.0 / (double) NITER);
    }

    public static void benchmarkFloatTSVD_Reflexive_2D(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        Opener o = new Opener();
        ImagePlus blurImage = o.openImage(path + blur_image);
        ImagePlus psfImage = o.openImage(path + psf_image);
        double av_time_deblur = 0;
        double av_time_deblur_regParam = 0;
        double av_time_update = 0;
        long elapsedTime_deblur = 0;
        long elapsedTime_deblur_regParam = 0;
        long elapsedTime_update = 0;
        System.out.println("Benchmarking FloatTSVD_Reflexive_2D using " + threads + " threads");
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            FloatReflexiveTruncatedSVD2D tsvd = new FloatReflexiveTruncatedSVD2D(blurImage, psfImage, resizing, output, false, -1, threshold);
            ImagePlus imX = tsvd.deconvolve();
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;

            elapsedTime_deblur_regParam = System.nanoTime();
            tsvd = new FloatReflexiveTruncatedSVD2D(blurImage, psfImage, resizing, output, false, float_regParam_deblur, threshold);
            imX = tsvd.deconvolve();
            elapsedTime_deblur_regParam = System.nanoTime() - elapsedTime_deblur_regParam;
            av_time_deblur_regParam = av_time_deblur_regParam + elapsedTime_deblur_regParam;

            elapsedTime_update = System.nanoTime();
            tsvd.update(float_regParam_update, threshold, imX);
            elapsedTime_update = System.nanoTime() - elapsedTime_update;
            av_time_update = av_time_update + elapsedTime_update;

            imX = null;
            tsvd = null;
            
        }
        blurImage = null;
        psfImage = null;
        System.out.println("Average execution time (deblur()): " + String.format(format, av_time_deblur / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (deblur(regParam)): " + String.format(format, av_time_deblur_regParam / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (update()): " + String.format(format, av_time_update / 1000000000.0 / (double) NITER) + " sec");
        writeResultsToFile("FloatTSVD_Reflexive_2D_" + threads + "_threads.txt", (double) av_time_deblur / 1000000000.0 / (double) NITER, (double) av_time_deblur_regParam / 1000000000.0 / (double) NITER, (double) av_time_update / 1000000000.0 / (double) NITER);
    }

    public static void benchmarkFloatTikhonov_Periodic_2D(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        Opener o = new Opener();
        ImagePlus blurImage = o.openImage(path + blur_image);
        ImagePlus psfImage = o.openImage(path + psf_image);
        double av_time_deblur = 0;
        double av_time_deblur_regParam = 0;
        double av_time_update = 0;
        long elapsedTime_deblur = 0;
        long elapsedTime_deblur_regParam = 0;
        long elapsedTime_update = 0;
        System.out.println("Benchmarking FloatTikhonov_Periodic_2D using " + threads + " threads");
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            FloatPeriodicTikhonov2D tsvd = new FloatPeriodicTikhonov2D(blurImage, psfImage, resizing, output, false, -1, threshold);
            ImagePlus imX = tsvd.deconvolve();
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;

            elapsedTime_deblur_regParam = System.nanoTime();
            tsvd = new FloatPeriodicTikhonov2D(blurImage, psfImage, resizing, output, false, float_regParam_deblur, threshold);
            imX = tsvd.deconvolve();
            elapsedTime_deblur_regParam = System.nanoTime() - elapsedTime_deblur_regParam;
            av_time_deblur_regParam = av_time_deblur_regParam + elapsedTime_deblur_regParam;

            elapsedTime_update = System.nanoTime();
            tsvd.update(float_regParam_update, threshold, imX);
            elapsedTime_update = System.nanoTime() - elapsedTime_update;
            av_time_update = av_time_update + elapsedTime_update;

            imX = null;
            tsvd = null;
            
        }
        blurImage = null;
        psfImage = null;
        System.out.println("Average execution time (deblur()): " + String.format(format, av_time_deblur / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (deblur(regParam)): " + String.format(format, av_time_deblur_regParam / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (update()): " + String.format(format, av_time_update / 1000000000.0 / (double) NITER) + " sec");
        writeResultsToFile("FloatTikhonov_Periodic_2D_" + threads + "_threads.txt", (double) av_time_deblur / 1000000000.0 / (double) NITER, (double) av_time_deblur_regParam / 1000000000.0 / (double) NITER, (double) av_time_update / 1000000000.0 / (double) NITER);
    }

    public static void benchmarkFloatTikhonov_Reflexive_2D(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        Opener o = new Opener();
        ImagePlus blurImage = o.openImage(path + blur_image);
        ImagePlus psfImage = o.openImage(path + psf_image);
        double av_time_deblur = 0;
        double av_time_deblur_regParam = 0;
        double av_time_update = 0;
        long elapsedTime_deblur = 0;
        long elapsedTime_deblur_regParam = 0;
        long elapsedTime_update = 0;
        System.out.println("Benchmarking FloatTikhonov_Reflexive_2D using " + threads + " threads");
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            FloatReflexiveTikhonov2D tsvd = new FloatReflexiveTikhonov2D(blurImage, psfImage, resizing, output, false, -1, threshold);
            ImagePlus imX = tsvd.deconvolve();
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;

            elapsedTime_deblur_regParam = System.nanoTime();
            tsvd = new FloatReflexiveTikhonov2D(blurImage, psfImage, resizing, output, false, float_regParam_deblur, threshold);
            imX = tsvd.deconvolve();
            elapsedTime_deblur_regParam = System.nanoTime() - elapsedTime_deblur_regParam;
            av_time_deblur_regParam = av_time_deblur_regParam + elapsedTime_deblur_regParam;

            elapsedTime_update = System.nanoTime();
            tsvd.update(float_regParam_update, threshold, imX);
            elapsedTime_update = System.nanoTime() - elapsedTime_update;
            av_time_update = av_time_update + elapsedTime_update;

            imX = null;
            tsvd = null;
            
        }
        blurImage = null;
        psfImage = null;
        System.out.println("Average execution time (deblur()): " + String.format(format, av_time_deblur / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (deblur(regParam)): " + String.format(format, av_time_deblur_regParam / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (update()): " + String.format(format, av_time_update / 1000000000.0 / (double) NITER) + " sec");
        writeResultsToFile("FloatTikhonov_Reflexive_2D_" + threads + "_threads.txt", (double) av_time_deblur / 1000000000.0 / (double) NITER, (double) av_time_deblur_regParam / 1000000000.0 / (double) NITER, (double) av_time_update / 1000000000.0 / (double) NITER);
    }

    public static void benchmarkFloatGTikhonov_Periodic_2D(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        Opener o = new Opener();
        ImagePlus blurImage = o.openImage(path + blur_image);
        ImagePlus psfImage = o.openImage(path + psf_image);
        double av_time_deblur = 0;
        double av_time_deblur_regParam = 0;
        double av_time_update = 0;
        long elapsedTime_deblur = 0;
        long elapsedTime_deblur_regParam = 0;
        long elapsedTime_update = 0;
        System.out.println("Benchmarking FloatGTikhonov_Periodic_2D using " + threads + " threads");
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            FloatPeriodicGeneralizedTikhonov2D tsvd = new FloatPeriodicGeneralizedTikhonov2D(blurImage, psfImage, floatStencil, resizing, output, false, -1, threshold);
            ImagePlus imX = tsvd.deconvolve();
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;

            elapsedTime_deblur_regParam = System.nanoTime();
            tsvd = new FloatPeriodicGeneralizedTikhonov2D(blurImage, psfImage, floatStencil, resizing, output, false, float_regParam_deblur, threshold);
            imX = tsvd.deconvolve();
            elapsedTime_deblur_regParam = System.nanoTime() - elapsedTime_deblur_regParam;
            av_time_deblur_regParam = av_time_deblur_regParam + elapsedTime_deblur_regParam;

            elapsedTime_update = System.nanoTime();
            tsvd.update(float_regParam_update, threshold, imX);
            elapsedTime_update = System.nanoTime() - elapsedTime_update;
            av_time_update = av_time_update + elapsedTime_update;

            imX = null;
            tsvd = null;
            
        }
        blurImage = null;
        psfImage = null;
        System.out.println("Average execution time (deblur()): " + String.format(format, av_time_deblur / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (deblur(regParam)): " + String.format(format, av_time_deblur_regParam / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (update()): " + String.format(format, av_time_update / 1000000000.0 / (double) NITER) + " sec");
        writeResultsToFile("FloatGTikhonov_Periodic_2D_" + threads + "_threads.txt", (double) av_time_deblur / 1000000000.0 / (double) NITER, (double) av_time_deblur_regParam / 1000000000.0 / (double) NITER, (double) av_time_update / 1000000000.0 / (double) NITER);
    }

    public static void benchmarkFloatGTikhonov_Reflexive_2D(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        Opener o = new Opener();
        ImagePlus blurImage = o.openImage(path + blur_image);
        ImagePlus psfImage = o.openImage(path + psf_image);
        double av_time_deblur = 0;
        double av_time_deblur_regParam = 0;
        double av_time_update = 0;
        long elapsedTime_deblur = 0;
        long elapsedTime_deblur_regParam = 0;
        long elapsedTime_update = 0;
        System.out.println("Benchmarking FloatGTikhonov_Reflexive_2D using " + threads + " threads");
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            FloatReflexiveGeneralizedTikhonov2D tsvd = new FloatReflexiveGeneralizedTikhonov2D(blurImage, psfImage, floatStencil, resizing, output, false, -1, threshold);
            ImagePlus imX = tsvd.deconvolve();
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;

            elapsedTime_deblur_regParam = System.nanoTime();
            tsvd = new FloatReflexiveGeneralizedTikhonov2D(blurImage, psfImage, floatStencil, resizing, output, false, float_regParam_deblur, threshold);
            imX = tsvd.deconvolve();
            elapsedTime_deblur_regParam = System.nanoTime() - elapsedTime_deblur_regParam;
            av_time_deblur_regParam = av_time_deblur_regParam + elapsedTime_deblur_regParam;

            elapsedTime_update = System.nanoTime();
            tsvd.update(float_regParam_update, threshold, imX);
            elapsedTime_update = System.nanoTime() - elapsedTime_update;
            av_time_update = av_time_update + elapsedTime_update;

            imX = null;
            tsvd = null;
            
        }
        blurImage = null;
        psfImage = null;
        System.out.println("Average execution time (deblur()): " + String.format(format, av_time_deblur / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (deblur(regParam)): " + String.format(format, av_time_deblur_regParam / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (update()): " + String.format(format, av_time_update / 1000000000.0 / (double) NITER) + " sec");
        writeResultsToFile("FloatGTikhonov_Reflexive_2D_" + threads + "_threads.txt", (double) av_time_deblur / 1000000000.0 / (double) NITER, (double) av_time_deblur_regParam / 1000000000.0 / (double) NITER, (double) av_time_update / 1000000000.0 / (double) NITER);
    }

    public static void writeResultsToFile(String filename, double time_deblur, double time_deblur_regParam, double time_update) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            out.write(new Date().toString());
            out.newLine();
            out.write("Number of processors: " + ConcurrencyUtils.getNumberOfThreads());
            out.newLine();
            out.write("deblur() time=");
            out.write(String.format(format, time_deblur));
            out.write(" seconds");
            out.newLine();
            out.write("deblur(regParam) time=");
            out.write(String.format(format, time_deblur_regParam));
            out.write(" seconds");
            out.newLine();
            out.write("update() time=");
            out.write(String.format(format, time_update));
            out.write(" seconds");
            out.newLine();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        //		benchmarkDoubleTSVD_Periodic_2D(1);
        //		
        //		benchmarkDoubleTSVD_Periodic_2D(2);
        //		
        //		benchmarkDoubleTSVD_Periodic_2D(4);
        //		
        //		benchmarkDoubleTSVD_Periodic_2D(8);
        //		
        //		benchmarkDoubleTSVD_Reflexive_2D(1);
        //		
        //		benchmarkDoubleTSVD_Reflexive_2D(2);
        //		
        //		benchmarkDoubleTSVD_Reflexive_2D(4);
        //		
        //		benchmarkDoubleTSVD_Reflexive_2D(8);
        //		
        //		benchmarkDoubleTikhonov_Periodic_2D(1);
        //		
        //		benchmarkDoubleTikhonov_Periodic_2D(2);
        //		
        //		benchmarkDoubleTikhonov_Periodic_2D(4);
        //		
        //		benchmarkDoubleTikhonov_Periodic_2D(8);
        //		
        //		benchmarkDoubleTikhonov_Reflexive_2D(1);
        //		
        //		benchmarkDoubleTikhonov_Reflexive_2D(2);
        //		
        //		benchmarkDoubleTikhonov_Reflexive_2D(4);
        //		
        //		benchmarkDoubleTikhonov_Reflexive_2D(8);
        //		
        //		benchmarkDoubleGTikhonov_Periodic_2D(1);
        //		
        //		benchmarkDoubleGTikhonov_Periodic_2D(2);
        //		
        //		benchmarkDoubleGTikhonov_Periodic_2D(4);
        //		
        //		benchmarkDoubleGTikhonov_Periodic_2D(8);
        //		
        //		benchmarkDoubleGTikhonov_Reflexive_2D(1);
        //		
        //		benchmarkDoubleGTikhonov_Reflexive_2D(2);
        //		
        //		benchmarkDoubleGTikhonov_Reflexive_2D(4);
        //		
        //		benchmarkDoubleGTikhonov_Reflexive_2D(8);
        //		

//        benchmarkFloatTSVD_Periodic_2D(1);
//        
//        benchmarkFloatTSVD_Periodic_2D(2);
//        
//        benchmarkFloatTSVD_Periodic_2D(4);
//        
//        benchmarkFloatTSVD_Periodic_2D(8);
//        
        benchmarkFloatTSVD_Reflexive_2D(1);
        
        benchmarkFloatTSVD_Reflexive_2D(2);
        
        benchmarkFloatTSVD_Reflexive_2D(4);
        
        benchmarkFloatTSVD_Reflexive_2D(8);
        
//        benchmarkFloatTikhonov_Periodic_2D(1);
//        
//        benchmarkFloatTikhonov_Periodic_2D(2);
//        
//        benchmarkFloatTikhonov_Periodic_2D(4);
//        
//        benchmarkFloatTikhonov_Periodic_2D(8);
//        
        benchmarkFloatTikhonov_Reflexive_2D(1);
        
        benchmarkFloatTikhonov_Reflexive_2D(2);
        
        benchmarkFloatTikhonov_Reflexive_2D(4);
        
        benchmarkFloatTikhonov_Reflexive_2D(8);
        
//        benchmarkFloatGTikhonov_Periodic_2D(1);
//        
//        benchmarkFloatGTikhonov_Periodic_2D(2);
//        
//        benchmarkFloatGTikhonov_Periodic_2D(4);
//        
//        benchmarkFloatGTikhonov_Periodic_2D(8);
//        
        benchmarkFloatGTikhonov_Reflexive_2D(1);
        
        benchmarkFloatGTikhonov_Reflexive_2D(2);
        
        benchmarkFloatGTikhonov_Reflexive_2D(4);
        
        benchmarkFloatGTikhonov_Reflexive_2D(8);
        
        System.exit(0);

    }
}
