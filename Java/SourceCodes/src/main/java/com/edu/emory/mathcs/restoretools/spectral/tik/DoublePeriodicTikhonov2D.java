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
package com.edu.emory.mathcs.restoretools.spectral.tik;

import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import optimization.DoubleFmin;
import optimization.DoubleFmin_methods;
import com.cern.colt.matrix.AbstractMatrix2D;
import com.cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix2D;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.cern.jet.math.tdcomplex.DComplexFunctions;
import com.cern.jet.math.tdouble.DoubleFunctions;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.AbstractDoubleSpectralDeconvolver2D;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;

/**
 * 2D Tikhonov with periodic boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoublePeriodicTikhonov2D extends AbstractDoubleSpectralDeconvolver2D {
    private AbstractMatrix2D S;

    private DComplexMatrix2D ConjS;

    /**
     * Creates new instance of DoubleTikFFT2D
     * 
     * @param imB
     *            blurred image
     * @param imPSF
     *            Point Spread Function image
     * @param resizing
     *            type of resizing
     * @param output
     *            type of output
     * @param showPadded
     *            if true, then a padded image is displayed
     * @param regParam
     *            regularization parameter. If regParam == -1 then the
     *            regularization parameter is computed by Generalized Cross
     *            Validation.
     * @param threshold
     *            the smallest positive value assigned to the restored image,
     *            all the values less than the threshold are set to zero. To
     *            disable thresholding use threshold = -1.
     */
    public DoublePeriodicTikhonov2D(ImagePlus imB, ImagePlus imPSF, ResizingType resizing, OutputType output, boolean showPadded, double regParam, double threshold) {
        super("Tikhonov", imB, imPSF, resizing, output, PaddingType.PERIODIC, showPadded, regParam, threshold);
    }

    public ImagePlus deconvolve() {
        IJ.showStatus(name + ": deconvolving");
        S = DoubleCommon2D.circShift((DoubleMatrix2D) PSF, psfCenter);
        S = ((DenseDoubleMatrix2D) S).getFft2();
        B = ((DenseDoubleMatrix2D) B).getFft2();
        if (regParam == -1) {
            IJ.showStatus(name + ": computing regularization parameter");
            regParam = gcvTikFFT2D((DComplexMatrix2D) S, (DComplexMatrix2D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        ConjS = ((DComplexMatrix2D) S).copy();
        ConjS.assign(DComplexFunctions.conj);
        PSF = ConjS.copy();
        ((DComplexMatrix2D) PSF).assign((DComplexMatrix2D) S, DComplexFunctions.mult);
        S = ((DComplexMatrix2D) PSF).copy();
        ((DComplexMatrix2D) PSF).assign(DComplexFunctions.plus(new double[] { regParam * regParam, 0 }));
        ((DComplexMatrix2D) B).assign(ConjS, DComplexFunctions.mult);
        ConjS = ((DComplexMatrix2D) B).copy();
        ((DComplexMatrix2D) ConjS).assign((DComplexMatrix2D) PSF, DComplexFunctions.div);
        ((DenseDComplexMatrix2D) ConjS).ifft2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, (DComplexMatrix2D) ConjS, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, (DComplexMatrix2D) ConjS, cmY);
            }
        } else {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, (DComplexMatrix2D) ConjS, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, (DComplexMatrix2D) ConjS, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", ip);
        DoubleCommon2D.convertImage(imX, output);
        imX.setProperty("regParam", regParam);
        return imX;
    }

    public void update(double regParam, double threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        PSF = ((DComplexMatrix2D) S).copy();
        ((DComplexMatrix2D) PSF).assign(DComplexFunctions.plus(new double[] { regParam * regParam, 0 }));
        ConjS = ((DComplexMatrix2D) B).copy();
        ((DComplexMatrix2D) ConjS).assign((DComplexMatrix2D) PSF, DComplexFunctions.div);
        ((DenseDComplexMatrix2D) ConjS).ifft2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, (DComplexMatrix2D) ConjS, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, (DComplexMatrix2D) ConjS, cmY);
            }
        } else {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, (DComplexMatrix2D) ConjS, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, (DComplexMatrix2D) ConjS, cmY, threshold);
            }
        }
        imX.setProcessor(imX.getTitle(), ip);
        DoubleCommon2D.convertImage(imX, output);
    }

    private static double gcvTikFFT2D(DComplexMatrix2D S, DComplexMatrix2D Bhat) {
        AbstractMatrix2D s = S.copy();
        AbstractMatrix2D bhat = Bhat.copy();
        s = ((DComplexMatrix2D) s).assign(DComplexFunctions.abs).getRealPart();
        bhat = ((DComplexMatrix2D) bhat).assign(DComplexFunctions.abs).getRealPart();
        double[] tmp = ((DoubleMatrix2D) s).getMinLocation();
        double smin = tmp[0];
        tmp = ((DoubleMatrix2D) s).getMaxLocation();
        double smax = tmp[0];
        ((DoubleMatrix2D) s).assign(DoubleFunctions.square);
        TikFmin2D fmin = new TikFmin2D((DoubleMatrix2D) s, (DoubleMatrix2D) bhat);
        return DoubleFmin.fmin(smin, smax, fmin, DoubleCommon2D.FMIN_TOL);
    }

    private static class TikFmin2D implements DoubleFmin_methods {
        DoubleMatrix2D ssquare;

        DoubleMatrix2D bhat;

        public TikFmin2D(DoubleMatrix2D ssquare, DoubleMatrix2D bhat) {
            this.ssquare = ssquare;
            this.bhat = bhat;
        }

        public double f_to_minimize(double regParam) {
            DoubleMatrix2D sloc = ssquare.copy();
            DoubleMatrix2D bhatloc = bhat.copy();

            sloc.assign(DoubleFunctions.plus(regParam * regParam));
            sloc.assign(DoubleFunctions.inv);
            bhatloc.assign(sloc, DoubleFunctions.mult);
            bhatloc.assign(DoubleFunctions.square);
            double ss = sloc.zSum();
            return bhatloc.zSum() / (ss * ss);
        }

    }
}
