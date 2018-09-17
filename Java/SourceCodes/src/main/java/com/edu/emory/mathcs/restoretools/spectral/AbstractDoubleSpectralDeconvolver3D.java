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

import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon3D;
import com.edu.emory.mathcs.restoretools.spectral.DoubleSpectralDeconvolver;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import com.cern.colt.matrix.AbstractMatrix3D;
import com.cern.colt.matrix.tdouble.DoubleMatrix3D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;
import com.edu.emory.mathcs.utils.ConcurrencyUtils;

/**
 * 3D abstract spectral deconvolver.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public abstract class AbstractDoubleSpectralDeconvolver3D implements DoubleSpectralDeconvolver {

    /**
     * Blurred image
     */
    protected AbstractMatrix3D B;

    /**
     * Point Spread Function
     */
    protected AbstractMatrix3D PSF;

    /**
     * Color model
     */
    protected java.awt.image.ColorModel cmY;

    /**
     * Number of slices in the padded blurred image.
     */
    protected int bSlicesPad;

    /**
     * Number of columns in the padded blurred image.
     */
    protected int bColumnsPad;

    /**
     * Number of rows in the padded blurred image.
     */
    protected int bRowsPad;

    /**
     * Number of slices in the blurred image.
     */
    protected int bSlices;

    /**
     * Number of columns in the blurred image.
     */
    protected int bColumns;

    /**
     * Number of rows in the blurred image.
     */
    protected int bRows;

    /**
     * Offset for slices in the padded blurred image.
     */
    protected int bSlicesOff;

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
    protected double ragParam;

    /**
     * The name of a deconvolution algorithm.
     */
    protected String name;

    /**
     * Creates new instance of AbstractDoubleSpectralDeconvolver3D
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
    public AbstractDoubleSpectralDeconvolver3D(String name, ImagePlus imB, ImagePlus imPSF, ResizingType resizing, OutputType output, PaddingType padding, boolean showPadded, double regParam, double threshold) {
        IJ.showStatus(name + ": initializing");
        this.name = name;
        ImageStack isB = imB.getStack();
        ImageStack isPSF = imPSF.getStack();
        this.cmY = isB.getColorModel();
        int kSlices = isPSF.getSize();
        int kCols = isPSF.getWidth();
        int kRows = isPSF.getHeight();
        bSlices = isB.getSize();
        bColumns = isB.getWidth();
        bRows = isB.getHeight();
        if ((kSlices > bSlices) || (kRows > bRows) || (kCols > bColumns)) {
            throw new IllegalArgumentException("The PSF image cannot be larger than the blurred image.");
        }
        IJ.showStatus(name + ": initializing");
        switch (resizing) {
        case NEXT_POWER_OF_TWO:
            if (ConcurrencyUtils.isPowerOf2(bSlices)) {
                bSlicesPad = bSlices;
            } else {
                isPadded = true;
                bSlicesPad = ConcurrencyUtils.nextPow2(bSlices);
            }
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
            bSlicesPad = bSlices;
            bRowsPad = bRows;
            bColumnsPad = bColumns;
            break;
        default:
            throw new IllegalArgumentException("Unsupported resizing type.");
        }
        ImageProcessor ipB = imB.getProcessor();
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
        B = new DenseDoubleMatrix3D(bSlices, bRows, bColumns);
        DoubleCommon3D.assignPixelsToMatrix(isB, (DoubleMatrix3D) B);
        if (isPadded) {
            switch (padding) {
            case PERIODIC:
                B = DoubleCommon3D.padPeriodic((DoubleMatrix3D) B, bSlicesPad, bRowsPad, bColumnsPad);
                break;
            case REFLEXIVE:
                B = DoubleCommon3D.padReflexive((DoubleMatrix3D) B, bSlicesPad, bRowsPad, bColumnsPad);
                break;
            default:
                throw new IllegalArgumentException("Unsupported padding type.");
            }
            bSlicesOff = (bSlicesPad - bSlices + 1) / 2;
            bRowsOff = (bRowsPad - bRows + 1) / 2;
            bColumnsOff = (bColumnsPad - bColumns + 1) / 2;
        }
        PSF = new DenseDoubleMatrix3D(kSlices, kRows, kCols);
        DoubleCommon3D.assignPixelsToMatrix(isPSF, (DoubleMatrix3D) PSF);
        double[] maxAndLoc = ((DoubleMatrix3D) PSF).getMaxLocation();
        psfCenter = new int[] { (int) maxAndLoc[1], (int) maxAndLoc[2], (int) maxAndLoc[3] };
        ((DoubleMatrix3D) PSF).normalize();
        if (kSlices != bSlicesPad || kRows != bRowsPad || kCols != bColumnsPad) {
            PSF = DoubleCommon3D.padZero((DoubleMatrix3D) PSF, bSlicesPad, bRowsPad, bColumnsPad);
        }
        psfCenter[0] += (bSlicesPad - kSlices + 1) / 2;
        psfCenter[1] += (bRowsPad - kRows + 1) / 2;
        psfCenter[2] += (bColumnsPad - kCols + 1) / 2;
        if (showPadded && isPadded) {
            ImageStack stackTemp = new ImageStack(bColumnsPad, bRowsPad);
            DoubleCommon3D.assignPixelsToStack(stackTemp, (DoubleMatrix3D) B, cmY);
            ImagePlus impTemp = new ImagePlus("", stackTemp);
            impTemp.setTitle(WindowManager.makeUniqueName(imB.getShortTitle() + " (padded)"));
            impTemp.show();
            impTemp.setRoi(bColumnsOff, bRowsOff, bColumns, bRows);
        }
        this.ragParam = regParam;
        this.threshold = threshold;

    }

}
