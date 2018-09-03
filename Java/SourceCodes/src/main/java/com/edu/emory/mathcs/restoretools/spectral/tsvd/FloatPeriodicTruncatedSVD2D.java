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
package com.edu.emory.mathcs.restoretools.spectral.tsvd;

import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import com.cern.colt.function.tint.IntComparator;
import com.cern.colt.matrix.AbstractMatrix1D;
import com.cern.colt.matrix.AbstractMatrix2D;
import com.cern.colt.matrix.tfcomplex.FComplexMatrix1D;
import com.cern.colt.matrix.tfcomplex.FComplexMatrix2D;
import com.cern.colt.matrix.tfcomplex.impl.DenseFComplexMatrix1D;
import com.cern.colt.matrix.tfcomplex.impl.DenseFComplexMatrix2D;
import com.cern.colt.matrix.tfloat.FloatMatrix1D;
import com.cern.colt.matrix.tfloat.FloatMatrix2D;
import com.cern.colt.matrix.tfloat.algo.FloatSorting;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix1D;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import com.cern.jet.math.tfcomplex.FComplexFunctions;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.AbstractFloatSpectralDeconvolver2D;
import com.edu.emory.mathcs.restoretools.spectral.FloatCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;

/**
 * 3D Truncated SVD with periodic boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class FloatPeriodicTruncatedSVD2D extends AbstractFloatSpectralDeconvolver2D {

    private AbstractMatrix2D S;

    /**
     * Creates new instance of FloatTsvdFFT2D
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
    public FloatPeriodicTruncatedSVD2D(ImagePlus imB, ImagePlus imPSF, ResizingType resizing, OutputType output, boolean showPadded, float regParam, float threshold) {
        super("TSVD", imB, imPSF, resizing, output, PaddingType.PERIODIC, showPadded, regParam, threshold);
    }

    public ImagePlus deconvolve() {
        IJ.showStatus(name + ": deconvolving");
        S = FloatCommon2D.circShift((FloatMatrix2D) PSF, psfCenter);
        S = ((DenseFloatMatrix2D) S).getFft2();
        B = ((DenseFloatMatrix2D) B).getFft2();
        if (ragParam == -1) {
            IJ.showStatus(name + ": computing regularization parameter");
            ragParam = gcvTsvdFFT2D((FComplexMatrix2D) S, (FComplexMatrix2D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        FComplexMatrix2D Sfilt = FloatCommon2D.createFilter((FComplexMatrix2D) S, ragParam);
        PSF = ((FComplexMatrix2D) B).copy();
        ((FComplexMatrix2D) PSF).assign(Sfilt, FComplexFunctions.mult);
        ((DenseFComplexMatrix2D) PSF).ifft2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FComplexMatrix2D) PSF, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FComplexMatrix2D) PSF, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FComplexMatrix2D) PSF, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FComplexMatrix2D) PSF, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", ip);
        FloatCommon2D.convertImage(imX, output);
        imX.setProperty("regParam", ragParam);
        return imX;
    }

    public void update(float regParam, float threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        FComplexMatrix2D Sfilt = FloatCommon2D.createFilter((FComplexMatrix2D) S, regParam);
        PSF = ((FComplexMatrix2D) B).copy();
        ((FComplexMatrix2D) PSF).assign(Sfilt, FComplexFunctions.mult);
        ((DenseFComplexMatrix2D) PSF).ifft2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FComplexMatrix2D) PSF, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FComplexMatrix2D) PSF, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FComplexMatrix2D) PSF, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FComplexMatrix2D) PSF, cmY, threshold);
            }
        }
        imX.setProcessor(imX.getTitle(), ip);
        FloatCommon2D.convertImage(imX, output);
    }

    private float gcvTsvdFFT2D(FComplexMatrix2D S, FComplexMatrix2D Bhat) {
        int length = S.rows() * S.columns();
        AbstractMatrix1D s = new DenseFComplexMatrix1D(length);
        AbstractMatrix1D bhat = new DenseFComplexMatrix1D(length);
        System.arraycopy(((DenseFComplexMatrix2D) S).elements(), 0, ((DenseFComplexMatrix1D) s).elements(), 0, 2 * length);
        System.arraycopy(((DenseFComplexMatrix2D) Bhat).elements(), 0, ((DenseFComplexMatrix1D) bhat).elements(), 0, 2 * length);
        s = ((FComplexMatrix1D) s).assign(FComplexFunctions.abs).getRealPart();
        bhat = ((FComplexMatrix1D) bhat).assign(FComplexFunctions.abs).getRealPart();
        final float[] svalues = (float[]) ((DenseFloatMatrix1D) s).elements();
        IntComparator compDec = new IntComparator() {
            public int compare(int a, int b) {
                if (svalues[a] != svalues[a] || svalues[b] != svalues[b])
                    return compareNaN(svalues[a], svalues[b]); // swap NaNs to
                // the end
                return svalues[a] < svalues[b] ? 1 : (svalues[a] == svalues[b] ? 0 : -1);
            }
        };
        int[] indices = FloatSorting.quickSort.sortIndex((FloatMatrix1D) s, compDec);
        s = ((FloatMatrix1D) s).viewSelection(indices);
        bhat = ((FloatMatrix1D) bhat).viewSelection(indices);
        int n = (int)s.size();
        float[] rho = new float[n - 1];
        rho[n - 2] = ((FloatMatrix1D) bhat).getQuick(n - 1) * ((FloatMatrix1D) bhat).getQuick(n - 1);
        FloatMatrix1D G = new DenseFloatMatrix1D(n - 1);
        float[] elemsG = (float[]) G.elements();
        elemsG[n - 2] = rho[n - 2];
        float bhatel, temp1;
        for (int k = n - 2; k > 0; k--) {
            bhatel = ((FloatMatrix1D) bhat).getQuick(k);
            rho[k - 1] = rho[k] + bhatel * bhatel;
            temp1 = n - k;
            temp1 = temp1 * temp1;
            elemsG[k - 1] = rho[k - 1] / temp1;
        }
        for (int k = 0; k < n - 3; k++) {
            if (((FloatMatrix1D) s).getQuick(k) == ((FloatMatrix1D) s).getQuick(k + 1)) {
                elemsG[k] = Float.POSITIVE_INFINITY;
            }
        }
        return ((FloatMatrix1D) s).getQuick((int) G.getMinLocation()[1]);
    }

    private final int compareNaN(float a, float b) {
        if (a != a) {
            if (b != b)
                return 0; // NaN equals NaN
            else
                return 1; // e.g. NaN > 5
        }
        return -1; // e.g. 5 < NaN
    }

}
