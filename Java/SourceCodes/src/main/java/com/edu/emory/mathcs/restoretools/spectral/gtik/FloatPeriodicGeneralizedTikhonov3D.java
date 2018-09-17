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
import com.cern.colt.matrix.AbstractMatrix3D;
import com.cern.colt.matrix.tfcomplex.FComplexMatrix3D;
import com.cern.colt.matrix.tfcomplex.impl.DenseFComplexMatrix3D;
import com.cern.colt.matrix.tfloat.FloatMatrix3D;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix3D;
import com.cern.jet.math.tfcomplex.FComplexFunctions;
import com.cern.jet.math.tfloat.FloatFunctions;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.AbstractFloatSpectralDeconvolver3D;
import com.edu.emory.mathcs.restoretools.spectral.FloatCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.FloatCommon3D;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;

/**
 * 3D Generalized Tikhonov with periodic boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class FloatPeriodicGeneralizedTikhonov3D extends AbstractFloatSpectralDeconvolver3D {
    private AbstractMatrix3D Pd;

    private AbstractMatrix3D Sa;

    private AbstractMatrix3D Sd;

    private FComplexMatrix3D ConjSa;

    private FComplexMatrix3D ConjSd;

    /**
     * Creates new instance of FloatGTikFFT3D
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
    public FloatPeriodicGeneralizedTikhonov3D(ImagePlus imB, ImagePlus imPSF, FloatMatrix3D stencil, ResizingType resizing, OutputType output, boolean showPadded, float regParam, float threshold) {
        super("Generalized Tikhonov", imB, imPSF, resizing, output, PaddingType.PERIODIC, showPadded, regParam, threshold);
        if ((stencil.slices() != 3) || (stencil.rows() != 3) || (stencil.columns() != 3)) {
            throw new IllegalArgumentException("Illegal stencil for regularization operator");
        }
        Pd = new DenseFloatMatrix3D(bSlicesPad, bRowsPad, bColumnsPad);
        ((FloatMatrix3D) Pd).viewPart(0, 0, 0, 3, 3, 3).assign(stencil);
    }

    public ImagePlus deconvolve() {
        IJ.showStatus(name + ": deconvolving");
        Sa = FloatCommon3D.circShift((FloatMatrix3D) PSF, psfCenter);
        Sd = FloatCommon3D.circShift((FloatMatrix3D) Pd, psfCenter);
        Sa = ((DenseFloatMatrix3D) Sa).getFft3();
        Sd = ((DenseFloatMatrix3D) Sd).getFft3();
        B = ((DenseFloatMatrix3D) B).getFft3();
        if (regParam == -1) {
            IJ.showStatus(name + ": computing regularization parameter");
            regParam = gcvGTikFFT3D((FComplexMatrix3D) Sa, (FComplexMatrix3D) Sd, (FComplexMatrix3D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        ConjSa = ((FComplexMatrix3D) Sa).copy();
        ConjSa.assign(FComplexFunctions.conj);
        ConjSd = ((FComplexMatrix3D) Sd).copy();
        ConjSd.assign(FComplexFunctions.conj);
        ConjSd.assign((FComplexMatrix3D) Sd, FComplexFunctions.mult);
        Pd = ConjSd.copy();
        ((FComplexMatrix3D) Pd).assign(FComplexFunctions.mult(regParam * regParam));
        PSF = ConjSa.copy();
        ((FComplexMatrix3D) PSF).assign((FComplexMatrix3D) Sa, FComplexFunctions.mult);
        Sd = ((FComplexMatrix3D) PSF).copy();
        ((FComplexMatrix3D) PSF).assign((FComplexMatrix3D) Pd, FComplexFunctions.plus);
        ((FComplexMatrix3D) B).assign(ConjSa, FComplexFunctions.mult);
        Sa = ((FComplexMatrix3D) B).copy();
        ((FComplexMatrix3D) Sa).assign((FComplexMatrix3D) PSF, FComplexFunctions.div);
        ((DenseFComplexMatrix3D) Sa).ifft3(true);
        IJ.showStatus(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, (FComplexMatrix3D) Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, (FComplexMatrix3D) Sa, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, (FComplexMatrix3D) Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, (FComplexMatrix3D) Sa, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", stackOut);
        FloatCommon3D.convertImage(imX, output);
        imX.setProperty("regParam", regParam);
        return imX;
    }

    public void update(float regParam, float threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        PSF = ConjSd.copy();
        ((FComplexMatrix3D) PSF).assign(FComplexFunctions.mult(regParam * regParam));
        Pd = ((FComplexMatrix3D) Sd).copy();
        ((FComplexMatrix3D) Pd).assign((FComplexMatrix3D) PSF, FComplexFunctions.plus);
        Sa = ((FComplexMatrix3D) B).copy();
        ((FComplexMatrix3D) Sa).assign((FComplexMatrix3D) Pd, FComplexFunctions.div);
        ((DenseFComplexMatrix3D) Sa).ifft3(true);
        IJ.showStatus(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, (FComplexMatrix3D) Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, (FComplexMatrix3D) Sa, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, (FComplexMatrix3D) Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, (FComplexMatrix3D) Sa, cmY, threshold);
            }
        }
        imX.setStack(imX.getTitle(), stackOut);
        FloatCommon3D.convertImage(imX, output);
    }

    private static float gcvGTikFFT3D(FComplexMatrix3D Sa, FComplexMatrix3D Sd, FComplexMatrix3D Bhat) {
        AbstractMatrix3D sa = Sa.copy();
        AbstractMatrix3D sd = Sd.copy();
        AbstractMatrix3D bhat = Bhat.copy();
        sa = ((FComplexMatrix3D) sa).assign(FComplexFunctions.abs).getRealPart();
        sd = ((FComplexMatrix3D) sd).assign(FComplexFunctions.abs).getRealPart();
        bhat = ((FComplexMatrix3D) bhat).assign(FComplexFunctions.abs).getRealPart();

        float[] tmp = ((FloatMatrix3D) sa).getMinLocation();
        float smin = tmp[0];
        tmp = ((FloatMatrix3D) sa).getMaxLocation();
        float smax = tmp[0];
        ((FloatMatrix3D) sa).assign(FloatFunctions.square);
        ((FloatMatrix3D) sd).assign(FloatFunctions.square);
        GTikFmin3D fmin = new GTikFmin3D((FloatMatrix3D) sa, (FloatMatrix3D) sd, (FloatMatrix3D) bhat);
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
