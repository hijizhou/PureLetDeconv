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
import ij.ImageStack;
import optimization.FloatFmin;
import optimization.FloatFmin_methods;
import com.cern.colt.matrix.tfloat.FloatMatrix3D;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix3D;
import com.cern.jet.math.tfloat.FloatFunctions;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.AbstractFloatSpectralDeconvolver3D;
import com.edu.emory.mathcs.restoretools.spectral.FloatCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.FloatCommon3D;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;

/**
 * 3D Generalized Tikhonov with reflexive boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class FloatReflexiveGeneralizedTikhonov3D extends AbstractFloatSpectralDeconvolver3D {

    private FloatMatrix3D Pd;

    private FloatMatrix3D E1;

    private FloatMatrix3D Sa;

    private FloatMatrix3D Sd;

    /**
     * Creates new instance of FloatGTikDCT3D
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
    public FloatReflexiveGeneralizedTikhonov3D(ImagePlus imB, ImagePlus imPSF, FloatMatrix3D stencil, ResizingType resizing, OutputType output, boolean showPadded, float regParam, float threshold) {
        super("Generalized Tikhonov", imB, imPSF, resizing, output, PaddingType.REFLEXIVE, showPadded, regParam, threshold);
        if ((stencil.slices() != 3) || (stencil.rows() != 3) || (stencil.columns() != 3)) {
            throw new IllegalArgumentException("Illegal stencil for regularization operator");
        }
        Pd = new DenseFloatMatrix3D(bSlicesPad, bRowsPad, bColumnsPad);
        Pd.viewPart(0, 0, 0, 3, 3, 3).assign(stencil);
    }

    public ImagePlus deconvolve() {
        IJ.showStatus(name + ": decolvolving");
        E1 = new DenseFloatMatrix3D(bSlicesPad, bRowsPad, bColumnsPad);
        E1.setQuick(0, 0, 0, 1);
        ((DenseFloatMatrix3D) E1).dct3(true);
        Sa = FloatCommon3D.dctShift((FloatMatrix3D) PSF, psfCenter);
        ((DenseFloatMatrix3D) Sa).dct3(true);
        Sa.assign(E1, FloatFunctions.div);
        ((DenseFloatMatrix3D) B).dct3(true);
        Sd = FloatCommon3D.dctShift(Pd, new int[] { 1, 1, 1 });
        ((DenseFloatMatrix3D) Sd).dct3(true);
        Sd.assign(E1, FloatFunctions.div);
        if (regParam == -1) {
            IJ.showStatus(name + ": computing regularization parameter");
            regParam = gcvGTikDCT3D(Sa, Sd, (FloatMatrix3D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        Sd.assign(FloatFunctions.square);
        PSF = Sa.copy();
        Pd = Sd.copy();
        Pd.assign(FloatFunctions.mult(regParam * regParam));
        ((FloatMatrix3D) PSF).assign(FloatFunctions.square);
        E1 = ((FloatMatrix3D) PSF).copy();
        ((FloatMatrix3D) PSF).assign(Pd, FloatFunctions.plus);
        ((FloatMatrix3D) B).assign(Sa, FloatFunctions.mult);
        Sa = ((FloatMatrix3D) B).copy();
        Sa.assign((FloatMatrix3D) PSF, FloatFunctions.div);
        ((DenseFloatMatrix3D) Sa).idct3(true);
        IJ.showStatus(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, Sa, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, Sa, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", stackOut);
        FloatCommon3D.convertImage(imX, output);
        imX.setProperty("regParam", regParam);
        return imX;
    }

    public void update(float regParam, float threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        Pd = Sd.copy();
        Pd.assign(FloatFunctions.mult(regParam * regParam));
        PSF = E1.copy();
        ((FloatMatrix3D) PSF).assign(Pd, FloatFunctions.plus);
        Sa = ((FloatMatrix3D) B).copy();
        Sa.assign((FloatMatrix3D) PSF, FloatFunctions.div);
        ((DenseFloatMatrix3D) Sa).idct3(true);
        IJ.showStatus(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, Sa, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, Sa, cmY, threshold);
            }
        }
        imX.setStack(imX.getTitle(), stackOut);
        FloatCommon3D.convertImage(imX, output);
    }

    private static float gcvGTikDCT3D(FloatMatrix3D Sa, FloatMatrix3D Sd, FloatMatrix3D Bhat) {
        FloatMatrix3D sa = Sa.copy();
        FloatMatrix3D sd = Sd.copy();
        FloatMatrix3D bhat = Bhat.copy();
        sa.assign(FloatFunctions.abs);
        bhat.assign(FloatFunctions.abs);
        float[] tmp = sa.getMinLocation();
        float smin = tmp[0];
        tmp = sa.getMaxLocation();
        float smax = tmp[0];
        sa = sa.assign(FloatFunctions.square);
        sd = sd.assign(FloatFunctions.square);
        GTikFmin3D fmin = new GTikFmin3D(sa, sd, bhat);
        return (float) FloatFmin.fmin(smin, smax, fmin, FloatCommon2D.FMIN_TOL);
    }

    private static class GTikFmin3D implements FloatFmin_methods {
        FloatMatrix3D sasquare;

        FloatMatrix3D sdsquare;

        FloatMatrix3D bhat;

        public GTikFmin3D(FloatMatrix3D sasquare, FloatMatrix3D sdsquare, FloatMatrix3D bhat) {
            this.sasquare = sasquare;
            this.sdsquare = sdsquare;
            this.bhat = bhat;
        }

        public float f_to_minimize(float regParam) {
            FloatMatrix3D sdloc = sdsquare.copy();
            FloatMatrix3D denom = sdloc.copy();

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
