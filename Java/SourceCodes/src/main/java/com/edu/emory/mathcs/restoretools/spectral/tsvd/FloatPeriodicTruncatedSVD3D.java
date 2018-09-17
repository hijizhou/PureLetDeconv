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
import ij.ImageStack;
import com.cern.colt.function.tint.IntComparator;
import com.cern.colt.matrix.AbstractMatrix1D;
import com.cern.colt.matrix.AbstractMatrix3D;
import com.cern.colt.matrix.tfcomplex.FComplexMatrix1D;
import com.cern.colt.matrix.tfcomplex.FComplexMatrix3D;
import com.cern.colt.matrix.tfcomplex.impl.DenseFComplexMatrix1D;
import com.cern.colt.matrix.tfcomplex.impl.DenseFComplexMatrix3D;
import com.cern.colt.matrix.tfloat.FloatMatrix1D;
import com.cern.colt.matrix.tfloat.FloatMatrix3D;
import com.cern.colt.matrix.tfloat.algo.FloatSorting;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix1D;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix3D;
import com.cern.jet.math.tfcomplex.FComplexFunctions;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.AbstractFloatSpectralDeconvolver3D;
import com.edu.emory.mathcs.restoretools.spectral.FloatCommon3D;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;

/**
 * 3D Truncated SVD with periodic boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class FloatPeriodicTruncatedSVD3D extends AbstractFloatSpectralDeconvolver3D {
    private AbstractMatrix3D S;

    /**
     * Creates new instance of FloatTsvdFFT3D
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
    public FloatPeriodicTruncatedSVD3D(ImagePlus imB, ImagePlus imPSF, ResizingType resizing, OutputType output, boolean showPadded, float regParam, float threshold) {
        super("TSVD", imB, imPSF, resizing, output, PaddingType.PERIODIC, showPadded, regParam, threshold);
    }

    public ImagePlus deconvolve() {
        IJ.showStatus(name + ": deconvolving");
        S = FloatCommon3D.circShift((FloatMatrix3D) PSF, psfCenter);
        S = ((DenseFloatMatrix3D) S).getFft3();
        B = ((DenseFloatMatrix3D) B).getFft3();
        if (regParam == -1) {
            IJ.showStatus(name + ": computing regularization parameter");
            regParam = gcvTsvdFFT3D((FComplexMatrix3D) S, (FComplexMatrix3D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        FComplexMatrix3D Sfilt = FloatCommon3D.createFilter((FComplexMatrix3D) S, regParam);
        PSF = ((FComplexMatrix3D) B).copy();
        ((FComplexMatrix3D) PSF).assign(Sfilt, FComplexFunctions.mult);
        ((DenseFComplexMatrix3D) PSF).ifft3(true);
        IJ.showStatus(name + ": deconvolving");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, (FComplexMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, (FComplexMatrix3D) PSF, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, (FComplexMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, (FComplexMatrix3D) PSF, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", stackOut);
        FloatCommon3D.convertImage(imX, output);
        imX.setProperty("regParam", regParam);
        return imX;
    }

    public void update(float regParam, float threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        FComplexMatrix3D Sfilt = FloatCommon3D.createFilter((FComplexMatrix3D) S, regParam);
        PSF = ((FComplexMatrix3D) B).copy();
        ((FComplexMatrix3D) PSF).assign(Sfilt, FComplexFunctions.mult);
        ((DenseFComplexMatrix3D) PSF).ifft3(true);
        IJ.showStatus(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, (FComplexMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, (FComplexMatrix3D) PSF, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, (FComplexMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, (FComplexMatrix3D) PSF, cmY, threshold);
            }
        }
        imX.setStack(imX.getTitle(), stackOut);
        FloatCommon3D.convertImage(imX, output);
    }

    private static float gcvTsvdFFT3D(FComplexMatrix3D S, FComplexMatrix3D Bhat) {
        int length = S.slices() * S.rows() * S.columns();
        AbstractMatrix1D s = new DenseFComplexMatrix1D(length);
        AbstractMatrix1D bhat = new DenseFComplexMatrix1D(length);
        System.arraycopy(((DenseFComplexMatrix3D) S).elements(), 0, ((DenseFComplexMatrix1D) s).elements(), 0, 2 * length);
        System.arraycopy(((DenseFComplexMatrix3D) Bhat).elements(), 0, ((DenseFComplexMatrix1D) bhat).elements(), 0, 2 * length);
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

    private static final int compareNaN(float a, float b) {
        if (a != a) {
            if (b != b)
                return 0; // NaN equals NaN
            else
                return 1; // e.g. NaN > 5
        }
        return -1; // e.g. 5 < NaN
    }
}
