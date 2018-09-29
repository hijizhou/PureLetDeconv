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
package com.edu.emory.mathcs.restoretools.spectral.gtik;

import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import optimization.DoubleFmin;
import optimization.DoubleFmin_methods;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.cern.jet.math.tdouble.DoubleFunctions;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.AbstractDoubleSpectralDeconvolver2D;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;

/**
 * 2D Generalized Tikhonov with reflexive boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoubleReflexiveGeneralizedTikhonov2D extends AbstractDoubleSpectralDeconvolver2D {

    private DoubleMatrix2D Pd;

    private DoubleMatrix2D E1;

    private DoubleMatrix2D Sa;

    private DoubleMatrix2D Sd;

    /**
     * Creates new instance of DoubleGTikDCT2D
     * 
     * @param imB
     *            blurred image
     * @param imPSF
     *            Point Spread Function image
     * @param stencil
     *            3-by-3 stencil for regularization operator
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
    public DoubleReflexiveGeneralizedTikhonov2D(ImagePlus imB, ImagePlus imPSF, DoubleMatrix2D stencil, ResizingType resizing, OutputType output, boolean showPadded, double regParam, double threshold) {
        super("Generalized Tikhonov", imB, imPSF, resizing, output, PaddingType.REFLEXIVE, showPadded, regParam, threshold);
        if ((stencil.rows() != 3) || (stencil.columns() != 3)) {
            throw new IllegalArgumentException("Illegal stencil for regularization operator.");
        }
        Pd = new DenseDoubleMatrix2D(bRowsPad, bColumnsPad);
        Pd.viewPart(0, 0, 3, 3).assign(stencil);
    }

    public ImagePlus deconvolve() {
        IJ.showStatus(name + ": deconvolving");
        E1 = new DenseDoubleMatrix2D(bRowsPad, bColumnsPad);
        E1.setQuick(0, 0, 1);
        ((DenseDoubleMatrix2D) E1).dct2(true);
        Sa = DoubleCommon2D.dctShift((DoubleMatrix2D) PSF, psfCenter);
        ((DenseDoubleMatrix2D) Sa).dct2(true);
        Sa.assign(E1, DoubleFunctions.div);
        Sd = DoubleCommon2D.dctShift(Pd, new int[] { 1, 1 });
        ((DenseDoubleMatrix2D) Sd).dct2(true);
        Sd.assign(E1, DoubleFunctions.div);
        ((DenseDoubleMatrix2D) B).dct2(true);
        if (regParam == -1) {
            IJ.showStatus(name + ": computing regularization parameter");
            regParam = gcvGTikDCT2D(Sa, Sd, (DoubleMatrix2D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        Sd.assign(DoubleFunctions.square);
        PSF = Sa.copy();
        Pd = Sd.copy();
        Pd.assign(DoubleFunctions.mult(regParam * regParam));
        ((DoubleMatrix2D) PSF).assign(DoubleFunctions.square);
        E1 = ((DoubleMatrix2D) PSF).copy();
        ((DoubleMatrix2D) PSF).assign(Pd, DoubleFunctions.plus);
        ((DoubleMatrix2D) B).assign(Sa, DoubleFunctions.mult);
        Sa = ((DoubleMatrix2D) B).copy();
        Sa.assign((DoubleMatrix2D) PSF, DoubleFunctions.div);
        ((DenseDoubleMatrix2D) Sa).idct2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, Sa, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, Sa, cmY);
            }
        } else {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, Sa, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, Sa, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", ip);
        DoubleCommon2D.convertImage(imX, output);
        imX.setProperty("regParam", regParam);
        IJ.showProgress(1);
        return imX;
    }

    public void update(double regParam, double threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        Pd = Sd.copy();
        Pd.assign(DoubleFunctions.mult(regParam * regParam));
        PSF = E1.copy();
        ((DoubleMatrix2D) PSF).assign(Pd, DoubleFunctions.plus);
        Sa = ((DoubleMatrix2D) B).copy();
        Sa.assign((DoubleMatrix2D) PSF, DoubleFunctions.div);
        ((DenseDoubleMatrix2D) Sa).idct2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, Sa, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, Sa, cmY);
            }
        } else {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, Sa, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, Sa, cmY, threshold);
            }
        }
        imX.setProcessor(imX.getTitle(), ip);
        DoubleCommon2D.convertImage(imX, output);
    }

    private double gcvGTikDCT2D(DoubleMatrix2D Sa, DoubleMatrix2D Sd, DoubleMatrix2D Bhat) {
        DoubleMatrix2D sa = Sa.copy();
        DoubleMatrix2D sd = Sd.copy();
        DoubleMatrix2D bhat = Bhat.copy();
        sa.assign(DoubleFunctions.abs);
        bhat.assign(DoubleFunctions.abs);
        double[] tmp = sa.getMinLocation();
        double smin = tmp[0];
        tmp = sa.getMaxLocation();
        double smax = tmp[0];
        sa.assign(DoubleFunctions.square);
        sd.assign(DoubleFunctions.square);
        GTikFmin2D fmin = new GTikFmin2D(sa, sd, bhat);
        return (double) DoubleFmin.fmin(smin, smax, fmin, DoubleCommon2D.FMIN_TOL);
    }

    private class GTikFmin2D implements DoubleFmin_methods {
        DoubleMatrix2D sasquare;

        DoubleMatrix2D sdsquare;

        DoubleMatrix2D bhat;

        public GTikFmin2D(DoubleMatrix2D sasquare, DoubleMatrix2D sdsquare, DoubleMatrix2D bhat) {
            this.sasquare = sasquare;
            this.sdsquare = sdsquare;
            this.bhat = bhat;
        }

        public double f_to_minimize(double regParam) {
            DoubleMatrix2D sdloc = sdsquare.copy();
            DoubleMatrix2D denom = sdloc.copy();

            denom.assign(DoubleFunctions.mult(regParam * regParam));
            denom.assign(sasquare, DoubleFunctions.plus);
            sdloc.assign(denom, DoubleFunctions.div);
            denom = bhat.copy();
            denom.assign(sdloc, DoubleFunctions.mult);
            denom.assign(DoubleFunctions.square);
            double sphi_d = sdloc.zSum();
            return denom.zSum() / (sphi_d * sphi_d);
        }

    }

}
