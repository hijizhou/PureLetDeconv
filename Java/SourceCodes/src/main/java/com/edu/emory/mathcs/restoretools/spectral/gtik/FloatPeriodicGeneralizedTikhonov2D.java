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
import com.cern.colt.matrix.AbstractMatrix2D;
import com.cern.colt.matrix.tfcomplex.FComplexMatrix2D;
import com.cern.colt.matrix.tfcomplex.impl.DenseFComplexMatrix2D;
import com.cern.colt.matrix.tfloat.FloatMatrix2D;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import com.cern.jet.math.tfcomplex.FComplexFunctions;
import com.cern.jet.math.tfloat.FloatFunctions;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.AbstractFloatSpectralDeconvolver2D;
import com.edu.emory.mathcs.restoretools.spectral.FloatCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;

/**
 * 2D Generalized Tikhonov with periodic boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class FloatPeriodicGeneralizedTikhonov2D extends AbstractFloatSpectralDeconvolver2D {

    private AbstractMatrix2D Pd;

    private AbstractMatrix2D Sa;

    private AbstractMatrix2D Sd;

    private FComplexMatrix2D ConjSa;

    private FComplexMatrix2D ConjSd;

    /**
     * Creates new instance of FloatGTikFFT2D
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
    public FloatPeriodicGeneralizedTikhonov2D(ImagePlus imB, ImagePlus imPSF, FloatMatrix2D stencil, ResizingType resizing, OutputType output, boolean showPadded, float regParam, float threshold) {
        super("Generalized Tikhonov", imB, imPSF, resizing, output, PaddingType.PERIODIC, showPadded, regParam, threshold);
        if ((stencil.rows() != 3) || (stencil.columns() != 3)) {
            throw new IllegalArgumentException("Illegal stencil for regularization operator");
        }
        Pd = new DenseFloatMatrix2D(bRowsPad, bColumnsPad);
        ((FloatMatrix2D) Pd).viewPart(0, 0, 3, 3).assign(stencil);
    }

    public ImagePlus deconvolve() {
        IJ.showStatus(name + ": deconvolving");
        Sa = FloatCommon2D.circShift((FloatMatrix2D) PSF, psfCenter);
        Sa = ((DenseFloatMatrix2D) Sa).getFft2();
        B = ((DenseFloatMatrix2D) B).getFft2();
        Sd = FloatCommon2D.circShift((FloatMatrix2D) Pd, new int[] { 1, 1 });
        Sd = ((DenseFloatMatrix2D) Sd).getFft2();
        if (ragParam == -1) {
            IJ.showStatus(name + ": computing regularization parameter");
            ragParam = gcvGTikFFT2D((FComplexMatrix2D) Sa, (FComplexMatrix2D) Sd, (FComplexMatrix2D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        ConjSa = ((FComplexMatrix2D) Sa).copy();
        ConjSa.assign(FComplexFunctions.conj);
        ConjSd = ((FComplexMatrix2D) Sd).copy();
        ConjSd.assign(FComplexFunctions.conj);
        ConjSd.assign((FComplexMatrix2D) Sd, FComplexFunctions.mult);
        Pd = ConjSd.copy();
        ((FComplexMatrix2D) Pd).assign(FComplexFunctions.mult(ragParam * ragParam));
        PSF = ConjSa.copy();
        ((FComplexMatrix2D) PSF).assign((FComplexMatrix2D) Sa, FComplexFunctions.mult);
        Sd = ((FComplexMatrix2D) PSF).copy();
        ((FComplexMatrix2D) PSF).assign((FComplexMatrix2D) Pd, FComplexFunctions.plus);
        ((FComplexMatrix2D) B).assign(ConjSa, FComplexFunctions.mult);
        Sa = ((FComplexMatrix2D) B).copy();
        ((FComplexMatrix2D) Sa).assign((FComplexMatrix2D) PSF, FComplexFunctions.div);
        ((DenseFComplexMatrix2D) Sa).ifft2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FComplexMatrix2D) Sa, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FComplexMatrix2D) Sa, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FComplexMatrix2D) Sa, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FComplexMatrix2D) Sa, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", ip);
        FloatCommon2D.convertImage(imX, output);
        imX.setProperty("regParam", ragParam);
        return imX;
    }

    public void update(float regParam, float threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        PSF = ConjSd.copy();
        ((FComplexMatrix2D) PSF).assign(FComplexFunctions.mult(regParam * regParam));
        Pd = ((FComplexMatrix2D) Sd).copy();
        ((FComplexMatrix2D) Pd).assign((FComplexMatrix2D) PSF, FComplexFunctions.plus);
        Sa = ((FComplexMatrix2D) B).copy();
        ((FComplexMatrix2D) Sa).assign((FComplexMatrix2D) Pd, FComplexFunctions.div);
        ((DenseFComplexMatrix2D) Sa).ifft2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FComplexMatrix2D) Sa, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FComplexMatrix2D) Sa, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FComplexMatrix2D) Sa, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FComplexMatrix2D) Sa, cmY, threshold);
            }
        }
        imX.setProcessor(imX.getTitle(), ip);
        FloatCommon2D.convertImage(imX, output);
    }

    private float gcvGTikFFT2D(FComplexMatrix2D Sa, FComplexMatrix2D Sd, FComplexMatrix2D Bhat) {
        AbstractMatrix2D sa = Sa.copy();
        sa = ((FComplexMatrix2D) sa).assign(FComplexFunctions.abs).getRealPart();
        AbstractMatrix2D sd = Sd.copy();
        sd = ((FComplexMatrix2D) sd).assign(FComplexFunctions.abs).getRealPart();
        AbstractMatrix2D bhat = Bhat.copy();
        bhat = ((FComplexMatrix2D) bhat).assign(FComplexFunctions.abs).getRealPart();
        float[] tmp = ((FloatMatrix2D) sa).getMinLocation();
        float smin = tmp[0];
        tmp = ((FloatMatrix2D) sa).getMaxLocation();
        float smax = tmp[0];
        ((FloatMatrix2D) sa).assign(FloatFunctions.square);
        ((FloatMatrix2D) sd).assign(FloatFunctions.square);
        GTikFmin2D fmin = new GTikFmin2D((FloatMatrix2D) sa, (FloatMatrix2D) sd, (FloatMatrix2D) bhat);
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
