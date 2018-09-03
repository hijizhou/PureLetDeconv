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
 * 3D Tikhonov with periodic boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class FloatPeriodicTikhonov3D extends AbstractFloatSpectralDeconvolver3D {
    private AbstractMatrix3D S;

    private FComplexMatrix3D ConjS;

    /**
     * Creates new instance of FloatTikFFT2D
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
    public FloatPeriodicTikhonov3D(ImagePlus imB, ImagePlus imPSF, ResizingType resizing, OutputType output, boolean showPadded, float regParam, float threshold) {
        super("Tikhonov", imB, imPSF, resizing, output, PaddingType.PERIODIC, showPadded, regParam, threshold);
    }

    public ImagePlus deconvolve() {
        IJ.showStatus(name + ": deconvolving");
        S = FloatCommon3D.circShift((FloatMatrix3D) PSF, psfCenter);
        S = ((DenseFloatMatrix3D) S).getFft3();
        B = ((DenseFloatMatrix3D) B).getFft3();
        if (regParam == -1) {
            IJ.showStatus(name + ": computing regularization parameter");
            regParam = gcvTikFFT3D((FComplexMatrix3D) S, (FComplexMatrix3D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        ConjS = ((FComplexMatrix3D) S).copy();
        ConjS.assign(FComplexFunctions.conj);
        PSF = ConjS.copy();
        ((FComplexMatrix3D) PSF).assign((FComplexMatrix3D) S, FComplexFunctions.mult);
        S = ((FComplexMatrix3D) PSF).copy();
        ((FComplexMatrix3D) PSF).assign(FComplexFunctions.plus(new float[] { regParam * regParam, 0 }));
        ((FComplexMatrix3D) B).assign(ConjS, FComplexFunctions.mult);
        ConjS = ((FComplexMatrix3D) B).copy();
        ConjS.assign((FComplexMatrix3D) PSF, FComplexFunctions.div);
        ((DenseFComplexMatrix3D) ConjS).ifft3(true);
        IJ.showStatus(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, ConjS, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, ConjS, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, ConjS, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, ConjS, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", stackOut);
        FloatCommon3D.convertImage(imX, output);
        imX.setProperty("regParam", regParam);
        return imX;
    }

    public ImagePlus deblur(float regParam, float threshold) {
        IJ.showStatus(name + ": deblurring");
        S = FloatCommon3D.circShift((FloatMatrix3D) PSF, psfCenter);
        S = ((DenseFloatMatrix3D) S).getFft3();
        B = ((DenseFloatMatrix3D) B).getFft3();
        ConjS = ((FComplexMatrix3D) S).copy();
        ConjS.assign(FComplexFunctions.conj);
        PSF = ConjS.copy();
        ((FComplexMatrix3D) PSF).assign((FComplexMatrix3D) S, FComplexFunctions.mult);
        S = ((FComplexMatrix3D) PSF).copy();
        ((FComplexMatrix3D) PSF).assign(FComplexFunctions.plus(new float[] { regParam * regParam, 0 }));
        ((FComplexMatrix3D) B).assign(ConjS, FComplexFunctions.mult);
        ConjS = ((FComplexMatrix3D) B).copy();
        ConjS.assign((FComplexMatrix3D) PSF, FComplexFunctions.div);
        ((DenseFComplexMatrix3D) ConjS).ifft3(true);
        IJ.showStatus(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, ConjS, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, ConjS, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, ConjS, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, ConjS, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", stackOut);
        FloatCommon3D.convertImage(imX, output);
        return imX;
    }

    public void update(float regParam, float threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        PSF = ((FComplexMatrix3D) S).copy();
        ((FComplexMatrix3D) PSF).assign(FComplexFunctions.plus(new float[] { regParam * regParam, 0 }));
        ConjS = ((FComplexMatrix3D) B).copy();
        ((FComplexMatrix3D) ConjS).assign((FComplexMatrix3D) PSF, FComplexFunctions.div);
        ((DenseFComplexMatrix3D) ConjS).ifft3(true);
        IJ.showStatus(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, ConjS, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, ConjS, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon3D.assignPixelsToStackPadded(stackOut, ConjS, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon3D.assignPixelsToStack(stackOut, ConjS, cmY, threshold);
            }
        }
        imX.setStack(imX.getTitle(), stackOut);
        FloatCommon3D.convertImage(imX, output);
    }

    private static float gcvTikFFT3D(FComplexMatrix3D S, FComplexMatrix3D Bhat) {
        AbstractMatrix3D s = S.copy();
        AbstractMatrix3D bhat = Bhat.copy();
        s = ((FComplexMatrix3D) s).assign(FComplexFunctions.abs).getRealPart();
        bhat = ((FComplexMatrix3D) bhat).assign(FComplexFunctions.abs).getRealPart();
        float[] tmp = ((FloatMatrix3D) s).getMinLocation();
        float smin = tmp[0];
        tmp = ((FloatMatrix3D) s).getMaxLocation();
        float smax = tmp[0];
        ((FloatMatrix3D) s).assign(FloatFunctions.square);
        TikFmin3D fmin = new TikFmin3D((FloatMatrix3D) s, (FloatMatrix3D) bhat);
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
