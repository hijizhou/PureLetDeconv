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
import com.cern.colt.matrix.tfloat.FloatMatrix1D;
import com.cern.colt.matrix.tfloat.FloatMatrix3D;
import com.cern.colt.matrix.tfloat.algo.FloatSorting;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix1D;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix3D;
import com.cern.jet.math.tfloat.FloatFunctions;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.AbstractFloatSpectralDeconvolver3D;
import com.edu.emory.mathcs.restoretools.spectral.FloatCommon3D;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;

/**
 * 3D Truncated SVD with reflexive boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class FloatReflexiveTruncatedSVD3D extends AbstractFloatSpectralDeconvolver3D {

    private FloatMatrix3D E1;

    private FloatMatrix3D S;

    /**
     * Creates new instance of FloatTsvdDCT3D
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
    public FloatReflexiveTruncatedSVD3D(ImagePlus imB, ImagePlus imPSF, ResizingType resizing, OutputType output, boolean showPadded, float regParam, float threshold) {
        super("TSVD", imB, imPSF, resizing, output, PaddingType.REFLEXIVE, showPadded, regParam, threshold);
    }

    public ImagePlus deconvolve() {
        IJ.showStatus(name + ": deconvolving");
        E1 = new DenseFloatMatrix3D(bSlicesPad, bRowsPad, bColumnsPad);
        E1.setQuick(0, 0, 0, 1);
        ((DenseFloatMatrix3D) E1).dct3(true);
        ((DenseFloatMatrix3D) B).dct3(true);
        S = FloatCommon3D.dctShift((FloatMatrix3D) PSF, psfCenter);
        ((DenseFloatMatrix3D) S).dct3(true);
        S.assign(E1, FloatFunctions.div);
        if (regParam == -1) {
            IJ.showStatus(name + ": computing regularization parameter");
            regParam = gcvTsvdDCT3D(S, (FloatMatrix3D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        E1 = FloatCommon3D.createFilter(S, regParam);
        PSF = ((FloatMatrix3D) B).copy();
        ((FloatMatrix3D) PSF).assign(E1, FloatFunctions.mult);
        ((DenseFloatMatrix3D) PSF).idct3(true);
        IJ.showStatus(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, (FloatMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, (FloatMatrix3D) PSF, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, (FloatMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, (FloatMatrix3D) PSF, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", stackOut);
        FloatCommon3D.convertImage(imX, output);
        imX.setProperty("regParam", regParam);
        return imX;
    }

    public void update(float regParam, float threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        E1 = FloatCommon3D.createFilter(S, regParam);
        PSF = ((FloatMatrix3D) B).copy();
        ((FloatMatrix3D) PSF).assign(E1, FloatFunctions.mult);
        ((DenseFloatMatrix3D) PSF).idct3(true);
        IJ.showStatus(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, (FloatMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, (FloatMatrix3D) PSF, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, (FloatMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, (FloatMatrix3D) PSF, cmY, threshold);
            }
        }
        imX.setStack(imX.getTitle(), stackOut);
        FloatCommon3D.convertImage(imX, output);
    }

    private static float gcvTsvdDCT3D(FloatMatrix3D S, FloatMatrix3D Bhat) {
        int length = S.slices() * S.rows() * S.columns();
        FloatMatrix1D s = new DenseFloatMatrix1D(length);
        FloatMatrix1D bhat = new DenseFloatMatrix1D(length);
        System.arraycopy(((DenseFloatMatrix3D) S).elements(), 0, ((DenseFloatMatrix1D) s).elements(), 0, length);
        System.arraycopy(((DenseFloatMatrix3D) Bhat).elements(), 0, ((DenseFloatMatrix1D) bhat).elements(), 0, length);
        s.assign(FloatFunctions.abs);
        bhat.assign(FloatFunctions.abs);
        final float[] svalues = (float[]) ((DenseFloatMatrix1D) s).elements();

        IntComparator compDec = new IntComparator() {
            public int compare(int a, int b) {
                if (svalues[a] != svalues[a] || svalues[b] != svalues[b])
                    return compareNaN(svalues[a], svalues[b]); // swap NaNs to
                // the end
                return svalues[a] < svalues[b] ? 1 : (svalues[a] == svalues[b] ? 0 : -1);
            }
        };
        int[] indices = FloatSorting.quickSort.sortIndex(s, compDec);
        s = s.viewSelection(indices);
        bhat = bhat.viewSelection(indices);
        int n = (int)s.size();
        float[] rho = new float[n - 1];
        rho[n - 2] = bhat.getQuick(n - 1) * bhat.getQuick(n - 1);
        FloatMatrix1D G = new DenseFloatMatrix1D(n - 1);
        float[] elemsG = (float[]) G.elements();
        elemsG[n - 2] = rho[n - 2];
        float bhatel, temp1;
        for (int k = n - 2; k > 0; k--) {
            bhatel = bhat.getQuick(k);
            rho[k - 1] = rho[k] + bhatel * bhatel;
            temp1 = n - k;
            temp1 = temp1 * temp1;
            elemsG[k - 1] = rho[k - 1] / temp1;
        }
        for (int k = 0; k < n - 3; k++) {
            if (s.getQuick(k) == s.getQuick(k + 1)) {
                elemsG[k] = Float.POSITIVE_INFINITY;
            }
        }
        return s.getQuick((int) G.getMinLocation()[1]);
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
