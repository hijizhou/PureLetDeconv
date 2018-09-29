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
import optimization.FloatFmin;
import optimization.FloatFmin_methods;
import com.cern.colt.matrix.tfloat.FloatMatrix2D;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import com.cern.jet.math.tfloat.FloatFunctions;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.AbstractFloatSpectralDeconvolver2D;
import com.edu.emory.mathcs.restoretools.spectral.FloatCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;

/**
 * 2D Generalized Tikhonov with reflexive boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class FloatReflexiveGeneralizedTikhonov2D extends AbstractFloatSpectralDeconvolver2D {

    private FloatMatrix2D Pd;

    private FloatMatrix2D E1;

    private FloatMatrix2D Sa;

    private FloatMatrix2D Sd;

    /**
     * Creates new instance of FloatGTikDCT2D
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
    public FloatReflexiveGeneralizedTikhonov2D(ImagePlus imB, ImagePlus imPSF, FloatMatrix2D stencil, ResizingType resizing, OutputType output, boolean showPadded, float regParam, float threshold) {
        super("Generalized Tikhonov", imB, imPSF, resizing, output, PaddingType.REFLEXIVE, showPadded, regParam, threshold);
        if ((stencil.rows() != 3) || (stencil.columns() != 3)) {
            throw new IllegalArgumentException("Illegal stencil for regularization operator.");
        }
        Pd = new DenseFloatMatrix2D(bRowsPad, bColumnsPad);
        Pd.viewPart(0, 0, 3, 3).assign(stencil);
    }

    public ImagePlus deconvolve() {
        IJ.showStatus(name + ": deconvolving");
        E1 = new DenseFloatMatrix2D(bRowsPad, bColumnsPad);
        E1.setQuick(0, 0, 1);
        ((DenseFloatMatrix2D) E1).dct2(true);
        Sa = FloatCommon2D.dctShift((FloatMatrix2D) PSF, psfCenter);
        ((DenseFloatMatrix2D) Sa).dct2(true);
        Sa.assign(E1, FloatFunctions.div);
        Sd = FloatCommon2D.dctShift(Pd, new int[] { 1, 1 });
        ((DenseFloatMatrix2D) Sd).dct2(true);
        Sd.assign(E1, FloatFunctions.div);
        ((DenseFloatMatrix2D) B).dct2(true);
        if (ragParam == -1) {
            IJ.showStatus(name + ": computing regularization parameter");
            ragParam = gcvGTikDCT2D(Sa, Sd, (FloatMatrix2D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        Sd.assign(FloatFunctions.square);
        PSF = Sa.copy();
        Pd = Sd.copy();
        Pd.assign(FloatFunctions.mult(ragParam * ragParam));
        ((FloatMatrix2D) PSF).assign(FloatFunctions.square);
        E1 = ((FloatMatrix2D) PSF).copy();
        ((FloatMatrix2D) PSF).assign(Pd, FloatFunctions.plus);
        ((FloatMatrix2D) B).assign(Sa, FloatFunctions.mult);
        Sa = ((FloatMatrix2D) B).copy();
        Sa.assign((FloatMatrix2D) PSF, FloatFunctions.div);
        ((DenseFloatMatrix2D) Sa).idct2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, Sa, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, Sa, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, Sa, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, Sa, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", ip);
        FloatCommon2D.convertImage(imX, output);
        imX.setProperty("regParam", ragParam);
        IJ.showProgress(1);
        return imX;
    }

    public void update(float regParam, float threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        Pd = Sd.copy();
        Pd.assign(FloatFunctions.mult(regParam * regParam));
        PSF = E1.copy();
        ((FloatMatrix2D) PSF).assign(Pd, FloatFunctions.plus);
        Sa = ((FloatMatrix2D) B).copy();
        Sa.assign((FloatMatrix2D) PSF, FloatFunctions.div);
        ((DenseFloatMatrix2D) Sa).idct2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, Sa, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, Sa, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, Sa, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, Sa, cmY, threshold);
            }
        }
        imX.setProcessor(imX.getTitle(), ip);
        FloatCommon2D.convertImage(imX, output);
    }

    private float gcvGTikDCT2D(FloatMatrix2D Sa, FloatMatrix2D Sd, FloatMatrix2D Bhat) {
        FloatMatrix2D sa = Sa.copy();
        FloatMatrix2D sd = Sd.copy();
        FloatMatrix2D bhat = Bhat.copy();
        sa.assign(FloatFunctions.abs);
        bhat.assign(FloatFunctions.abs);
        float[] tmp = sa.getMinLocation();
        float smin = tmp[0];
        tmp = sa.getMaxLocation();
        float smax = tmp[0];
        sa.assign(FloatFunctions.square);
        sd.assign(FloatFunctions.square);
        GTikFmin2D fmin = new GTikFmin2D(sa, sd, bhat);
        return (float) FloatFmin.fmin(smin, smax, fmin, FloatCommon2D.FMIN_TOL);
    }

    private class GTikFmin2D implements FloatFmin_methods {
        FloatMatrix2D sasquare;

        FloatMatrix2D sdsquare;

        FloatMatrix2D bhat;

        public GTikFmin2D(FloatMatrix2D sasquare, FloatMatrix2D sdsquare, FloatMatrix2D bhat) {
            this.sasquare = sasquare;
            this.sdsquare = sdsquare;
            this.bhat = bhat;
        }

        public float f_to_minimize(float regParam) {
            FloatMatrix2D sdloc = sdsquare.copy();
            FloatMatrix2D denom = sdloc.copy();

            denom.assign(FloatFunctions.mult(regParam * regParam));
            denom.assign(sasquare, FloatFunctions.plus);
            sdloc.assign(denom, FloatFunctions.div);
            denom = bhat.copy();
            denom.assign(sdloc, FloatFunctions.mult);
            denom.assign(FloatFunctions.square);
            float sphi_d = sdloc.zSum();
            return denom.zSum() / (sphi_d * sphi_d);
        }

    }

}
