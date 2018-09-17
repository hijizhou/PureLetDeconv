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
import com.cern.colt.matrix.tfloat.FloatMatrix1D;
import com.cern.colt.matrix.tfloat.FloatMatrix2D;
import com.cern.colt.matrix.tfloat.algo.FloatSorting;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix1D;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import com.cern.jet.math.tfloat.FloatFunctions;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.AbstractFloatSpectralDeconvolver2D;
import com.edu.emory.mathcs.restoretools.spectral.FloatCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;

/**
 * 2D Truncated SVD with reflexive boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class FloatReflexiveTruncatedSVD2D extends AbstractFloatSpectralDeconvolver2D {

    private FloatMatrix2D E1;

    private FloatMatrix2D S;

    /**
     * Creates new instance of FloatTsvdDCT2D
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
    public FloatReflexiveTruncatedSVD2D(ImagePlus imB, ImagePlus imPSF, ResizingType resizing, OutputType output, boolean showPadded, float regParam, float threshold) {
        super("TSVD", imB, imPSF, resizing, output, PaddingType.REFLEXIVE, showPadded, regParam, threshold);
    }

    public ImagePlus deconvolve() {
        IJ.showStatus(name + ": deconvolving");
        E1 = new DenseFloatMatrix2D(bRowsPad, bColumnsPad);
        E1.setQuick(0, 0, 1);
        ((DenseFloatMatrix2D) B).dct2(true);
        S = FloatCommon2D.dctShift((FloatMatrix2D) PSF, psfCenter);
        ((DenseFloatMatrix2D) S).dct2(true);
        ((DenseFloatMatrix2D) E1).dct2(true);
        S.assign(E1, FloatFunctions.div);
        if (ragParam == -1) {
            IJ.showStatus(name + ": computing regularization parameter");
            ragParam = gcvTsvdDCT2D(S, (FloatMatrix2D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        E1 = FloatCommon2D.createFilter(S, ragParam);
        PSF = ((FloatMatrix2D) B).copy();
        ((FloatMatrix2D) PSF).assign(E1, FloatFunctions.mult);
        ((DenseFloatMatrix2D) PSF).idct2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FloatMatrix2D) PSF, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FloatMatrix2D) PSF, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FloatMatrix2D) PSF, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FloatMatrix2D) PSF, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", ip);
        FloatCommon2D.convertImage(imX, output);
        imX.setProperty("regParam", ragParam);
        return imX;
    }

    public void update(float regParam, float threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        E1 = FloatCommon2D.createFilter(S, regParam);
        PSF = ((FloatMatrix2D) B).copy();
        ((FloatMatrix2D) PSF).assign(E1, FloatFunctions.mult);
        ((DenseFloatMatrix2D) PSF).idct2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FloatMatrix2D) PSF, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FloatMatrix2D) PSF, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FloatMatrix2D) PSF, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FloatMatrix2D) PSF, cmY, threshold);
            }
        }
        imX.setProcessor(imX.getTitle(), ip);
        FloatCommon2D.convertImage(imX, output);
    }

    private float gcvTsvdDCT2D(FloatMatrix2D S, FloatMatrix2D Bhat) {
        int length = S.rows() * S.columns();
        FloatMatrix1D s = new DenseFloatMatrix1D(length);
        FloatMatrix1D bhat = new DenseFloatMatrix1D(length);
        System.arraycopy(((DenseFloatMatrix2D) S).elements(), 0, ((DenseFloatMatrix1D) s).elements(), 0, length);
        System.arraycopy(((DenseFloatMatrix2D) Bhat).elements(), 0, ((DenseFloatMatrix1D) bhat).elements(), 0, length);
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
