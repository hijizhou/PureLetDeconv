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

import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.DoubleSpectralDeconvolver;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import com.cern.colt.matrix.AbstractMatrix2D;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;
import com.edu.emory.mathcs.utils.ConcurrencyUtils;

/**
 * 2D abstract spectral deconvolver.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public abstract class AbstractDoubleSpectralDeconvolver2D implements DoubleSpectralDeconvolver {

    /**
     * Blurred image
     */
    protected AbstractMatrix2D B;

    /**
     * Point Spread Function
     */
    protected AbstractMatrix2D PSF;

    /**
     * Color model
     */
    protected java.awt.image.ColorModel cmY;

    /**
     * Number of columns in the padded blurred image.
     */
    protected int bColumnsPad;

    /**
     * Number of rows in the padded blurred image.
     */
    protected int bRowsPad;

    /**
     * Number of columns in the blurred image.
     */
    protected int bColumns;

    /**
     * Number of rows in the blurred image.
     */
    protected int bRows;

    /**
     * Offset for columns in the padded blurred image.
     */
    protected int bColumnsOff;

    /**
     * Offset for rows in the padded blurred image.
     */
    protected int bRowsOff;

    /**
     * The center of the PSF image.
     */
    protected int[] psfCenter;

    /**
     * True only if the blurred image is padded.
     */
    protected boolean isPadded = false;

    /**
     * Type of restored image.
     */
    protected OutputType output;

    /**
     * the smallest positive value assigned to the restored image, all the
     * values less than the threshold are set to zero.
     */
    protected double threshold;

    /**
     * regularization parameter.
     */
    protected double regParam;

    /**
     * The name of a deconvolution algorithm.
     */
    protected String name;

    /**
     * Creates new instance of AbstractDoubleSpectralDeconvolver2D
     * 
     * @param name
     *            name of a deconvolution algorithm
     * @param imB
     *            blurred image
     * @param imPSF
     *            Point Spread Function image
     * @param resizing
     *            type of resizing
     * @param output
     *            type of output
     * @param padding
     *            type of padding
     * @param showPadded
     *            if true, then a padded image is displayed
     * @param regParam
     *            regularization parameter
     * @param threshold
     *            the smallest positive value assigned to the restored image
     */
    public AbstractDoubleSpectralDeconvolver2D(String name, ImagePlus imB, ImagePlus imPSF, ResizingType resizing, OutputType output, PaddingType padding, boolean showPadded, double regParam, double threshold) {
        IJ.showStatus(name + ": initializing");
        this.name = name;
        ImageProcessor ipB = imB.getProcessor();
        ImageProcessor ipPSF = imPSF.getProcessor();
        this.cmY = ipB.getColorModel();
        int kCols = ipPSF.getWidth();
        int kRows = ipPSF.getHeight();
        bColumns = ipB.getWidth();
        bRows = ipB.getHeight();
        if ((kRows > bRows) || (kCols > bColumns)) {
            throw new IllegalArgumentException("The PSF image cannot be larger than the blurred image.");
        }
        switch (resizing) {
        case NEXT_POWER_OF_TWO:
            if (ConcurrencyUtils.isPowerOf2(bRows)) {
                bRowsPad = bRows;
            } else {
                isPadded = true;
                bRowsPad = ConcurrencyUtils.nextPow2(bRows);
            }
            if (ConcurrencyUtils.isPowerOf2(bColumns)) {
                bColumnsPad = bColumns;
            } else {
                isPadded = true;
                bColumnsPad = ConcurrencyUtils.nextPow2(bColumns);
            }
            break;
        case NONE:
            bColumnsPad = bColumns;
            bRowsPad = bRows;
            break;
        default:
            throw new IllegalArgumentException("Unsupported resizing type.");
        }
        if (output == OutputType.SAME_AS_SOURCE) {
            if (ipB instanceof ByteProcessor) {
                this.output = OutputType.BYTE;
            } else if (ipB instanceof ShortProcessor) {
                this.output = OutputType.SHORT;
            } else if (ipB instanceof FloatProcessor) {
                this.output = OutputType.FLOAT;
            } else {
                throw new IllegalArgumentException("Unsupported image type.");
            }
        } else {
            this.output = output;
        }
        B = new DenseDoubleMatrix2D(bRows, bColumns);
        DoubleCommon2D.assignPixelsToMatrix((DoubleMatrix2D) B, ipB);
        if (isPadded) {
            switch (padding) {
            case PERIODIC:
                B = DoubleCommon2D.padPeriodic((DoubleMatrix2D) B, bRowsPad, bColumnsPad);
                break;
            case REFLEXIVE:
                B = DoubleCommon2D.padReflexive((DoubleMatrix2D) B, bRowsPad, bColumnsPad);
                break;
            default:
                throw new IllegalArgumentException("Unsupported padding type.");
            }
            bColumnsOff = (bColumnsPad - bColumns + 1) / 2;
            bRowsOff = (bRowsPad - bRows + 1) / 2;
        }
        PSF = new DenseDoubleMatrix2D(kRows, kCols);
        DoubleCommon2D.assignPixelsToMatrix((DoubleMatrix2D) PSF, ipPSF);
        double[] maxAndLoc = ((DoubleMatrix2D) PSF).getMaxLocation();
        psfCenter = new int[] { (int) maxAndLoc[1], (int) maxAndLoc[2] };
        ((DoubleMatrix2D) PSF).normalize();
        if ((kCols != bColumnsPad) || (kRows != bRowsPad)) {
            PSF = DoubleCommon2D.padZero(((DoubleMatrix2D) PSF), bRowsPad, bColumnsPad);
        }
        psfCenter[0] += (bRowsPad - kRows + 1) / 2;
        psfCenter[1] += (bColumnsPad - kCols + 1) / 2;
        if (showPadded && isPadded) {
            FloatProcessor ipTemp = new FloatProcessor(bColumnsPad, bRowsPad);
            DoubleCommon2D.assignPixelsToProcessor(ipTemp, (DoubleMatrix2D) B, cmY);
            ImagePlus imTemp = new ImagePlus("", ipTemp);
            imTemp.setTitle(WindowManager.makeUniqueName(imB.getShortTitle() + " (padded)"));
            imTemp.show();
            imTemp.setRoi(bColumnsOff, bRowsOff, bColumns, bRows);
        }
        this.regParam = regParam;
        this.threshold = threshold;
    }

}
