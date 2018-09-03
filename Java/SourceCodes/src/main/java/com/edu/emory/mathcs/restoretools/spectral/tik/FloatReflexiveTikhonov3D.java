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
package com.edu.emory.mathcs.restoretools.spectral.tik;

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
 * 3D Tikhonov with reflexive boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class FloatReflexiveTikhonov3D extends AbstractFloatSpectralDeconvolver3D {

    private FloatMatrix3D E1;

    private FloatMatrix3D S;

    /**
     * Creates new instance of FloatTikDCT3D
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
    public FloatReflexiveTikhonov3D(ImagePlus imB, ImagePlus imPSF, ResizingType resizing, OutputType output, boolean showPadded, float regParam, float threshold) {
        super("Tikhonov", imB, imPSF, resizing, output, PaddingType.REFLEXIVE, showPadded, regParam, threshold);
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
            regParam = gcvTikDCT3D(S, (FloatMatrix3D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        PSF = S.copy();
        ((FloatMatrix3D) PSF).assign(FloatFunctions.square);
        E1 = ((FloatMatrix3D) PSF).copy();
        ((FloatMatrix3D) PSF).assign(FloatFunctions.plus(regParam * regParam));
        ((FloatMatrix3D) B).assign(S, FloatFunctions.mult);
        S = ((FloatMatrix3D) B).copy();
        S.assign((FloatMatrix3D) PSF, FloatFunctions.div);
        ((DenseFloatMatrix3D) S).idct3(true);
        IJ.showStatus(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, S, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, S, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, S, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, S, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", stackOut);
        FloatCommon3D.convertImage(imX, output);
        imX.setProperty("regParam", regParam);
        return imX;
    }

    public void update(float regParam, float threshold, ImagePlus imX) {
        IJ.showStatus("Tikhonov: updating");
        PSF = E1.copy();
        ((FloatMatrix3D) PSF).assign(FloatFunctions.plus(regParam * regParam));
        S = ((FloatMatrix3D) B).copy();
        S.assign((FloatMatrix3D) PSF, FloatFunctions.div);
        ((DenseFloatMatrix3D) S).idct3(true);
        IJ.showStatus("Tikhonov: finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, S, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, S, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, S, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, S, cmY, threshold);
            }
        }
        imX.setStack(imX.getTitle(), stackOut);
        FloatCommon3D.convertImage(imX, output);
    }

    private static float gcvTikDCT3D(FloatMatrix3D S, FloatMatrix3D Bhat) {
        FloatMatrix3D s = S.copy();
        FloatMatrix3D bhat = Bhat.copy();
        s = s.assign(FloatFunctions.abs);
        bhat = bhat.assign(FloatFunctions.abs);
        float[] tmp = s.getMinLocation();
        float smin = tmp[0];
        tmp = s.getMaxLocation();
        float smax = tmp[0];
        s = s.assign(FloatFunctions.square);
        TikFmin3D fmin = new TikFmin3D(s, bhat);
        return (float) FloatFmin.fmin(smin, smax, fmin, FloatCommon2D.FMIN_TOL);
    }

    private static class TikFmin3D implements FloatFmin_methods {
        FloatMatrix3D ssquare;

        FloatMatrix3D bhat;

        public TikFmin3D(FloatMatrix3D ssquare, FloatMatrix3D bhat) {
            this.ssquare = ssquare;
            this.bhat = bhat;
        }

        public float f_to_minimize(float regParam) {
            FloatMatrix3D sloc = ssquare.copy();
            FloatMatrix3D bhatloc = bhat.copy();
            sloc.assign(FloatFunctions.plus(regParam * regParam));
            sloc.assign(FloatFunctions.inv);
            bhatloc.assign(sloc, FloatFunctions.mult);
            bhatloc.assign(FloatFunctions.square);
            float ss = sloc.zSum();
            return bhatloc.zSum() / (ss * ss);
        }

    }
}
