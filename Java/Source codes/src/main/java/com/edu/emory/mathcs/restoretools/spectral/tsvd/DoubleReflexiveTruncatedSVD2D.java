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
import com.cern.colt.matrix.tdouble.DoubleMatrix1D;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.algo.DoubleSorting;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.cern.jet.math.tdouble.DoubleFunctions;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.AbstractDoubleSpectralDeconvolver2D;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;

/**
 * 2D Truncated SVD with reflexive boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoubleReflexiveTruncatedSVD2D extends AbstractDoubleSpectralDeconvolver2D {

    private DoubleMatrix2D E1;

    private DoubleMatrix2D S;

    /**
     * Creates new instance of DoubleTsvdDCT2D
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
    public DoubleReflexiveTruncatedSVD2D(ImagePlus imB, ImagePlus imPSF, ResizingType resizing, OutputType output, boolean showPadded, double regParam, double threshold) {
        super("TSVD", imB, imPSF, resizing, output, PaddingType.REFLEXIVE, showPadded, regParam, threshold);
    }

    public ImagePlus deconvolve() {
        IJ.showStatus(name + ": deconvolving");
        E1 = new DenseDoubleMatrix2D(bRowsPad, bColumnsPad);
        E1.setQuick(0, 0, 1);
        ((DenseDoubleMatrix2D) B).dct2(true);
        S = DoubleCommon2D.dctShift((DoubleMatrix2D) PSF, psfCenter);
        ((DenseDoubleMatrix2D) S).dct2(true);
        ((DenseDoubleMatrix2D) E1).dct2(true);
        S.assign(E1, DoubleFunctions.div);
        if (regParam == -1) {
            IJ.showStatus(name + ": computing regularization parameter");
            regParam = gcvTsvdDCT2D(S, (DoubleMatrix2D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        E1 = DoubleCommon2D.createFilter(S, regParam);
        PSF = ((DoubleMatrix2D) B).copy();
        ((DoubleMatrix2D) PSF).assign(E1, DoubleFunctions.mult);
        ((DenseDoubleMatrix2D) PSF).idct2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, (DoubleMatrix2D) PSF, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, (DoubleMatrix2D) PSF, cmY);
            }
        } else {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, (DoubleMatrix2D) PSF, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, (DoubleMatrix2D) PSF, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", ip);
        DoubleCommon2D.convertImage(imX, output);
        imX.setProperty("regParam", regParam);
        return imX;
    }

    public void update(double regParam, double threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        E1 = DoubleCommon2D.createFilter(S, regParam);
        PSF = ((DoubleMatrix2D) B).copy();
        ((DoubleMatrix2D) PSF).assign(E1, DoubleFunctions.mult);
        ((DenseDoubleMatrix2D) PSF).idct2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, (DoubleMatrix2D) PSF, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, (DoubleMatrix2D) PSF, cmY);
            }
        } else {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, (DoubleMatrix2D) PSF, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, (DoubleMatrix2D) PSF, cmY, threshold);
            }
        }
        imX.setProcessor(imX.getTitle(), ip);
        DoubleCommon2D.convertImage(imX, output);
    }

    private double gcvTsvdDCT2D(DoubleMatrix2D S, DoubleMatrix2D Bhat) {
        int length = S.rows() * S.columns();
        DoubleMatrix1D s = new DenseDoubleMatrix1D(length);
        DoubleMatrix1D bhat = new DenseDoubleMatrix1D(length);
        System.arraycopy(((DenseDoubleMatrix2D) S).elements(), 0, ((DenseDoubleMatrix1D) s).elements(), 0, length);
        System.arraycopy(((DenseDoubleMatrix2D) Bhat).elements(), 0, ((DenseDoubleMatrix1D) bhat).elements(), 0, length);
        s.assign(DoubleFunctions.abs);
        bhat.assign(DoubleFunctions.abs);
        final double[] svalues = (double[]) ((DenseDoubleMatrix1D) s).elements();

        IntComparator compDec = new IntComparator() {
            public int compare(int a, int b) {
                if (svalues[a] != svalues[a] || svalues[b] != svalues[b])
                    return compareNaN(svalues[a], svalues[b]); // swap NaNs to
                // the end
                return svalues[a] < svalues[b] ? 1 : (svalues[a] == svalues[b] ? 0 : -1);
            }
        };
        int[] indices = DoubleSorting.quickSort.sortIndex(s, compDec);
        s = s.viewSelection(indices);
        bhat = bhat.viewSelection(indices);
        int n = (int)s.size();
        double[] rho = new double[n - 1];
        rho[n - 2] = bhat.getQuick(n - 1) * bhat.getQuick(n - 1);
        DoubleMatrix1D G = new DenseDoubleMatrix1D(n - 1);
        double[] elemsG = (double[]) G.elements();
        elemsG[n - 2] = rho[n - 2];
        double bhatel, temp1;
        for (int k = n - 2; k > 0; k--) {
            bhatel = bhat.getQuick(k);
            rho[k - 1] = rho[k] + bhatel * bhatel;
            temp1 = n - k;
            temp1 = temp1 * temp1;
            elemsG[k - 1] = rho[k - 1] / temp1;
        }
        for (int k = 0; k < n - 3; k++) {
            if (s.getQuick(k) == s.getQuick(k + 1)) {
                elemsG[k] = Double.POSITIVE_INFINITY;
            }
        }
        return s.getQuick((int) G.getMinLocation()[1]);
    }

    private final int compareNaN(double a, double b) {
        if (a != a) {
            if (b != b)
                return 0; // NaN equals NaN
            else
                return 1; // e.g. NaN > 5
        }
        return -1; // e.g. 5 < NaN
    }

}
