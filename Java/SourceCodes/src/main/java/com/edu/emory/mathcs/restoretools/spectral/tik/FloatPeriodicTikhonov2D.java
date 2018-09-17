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
 * 2D Tikhonov with periodic boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class FloatPeriodicTikhonov2D extends AbstractFloatSpectralDeconvolver2D {
    private AbstractMatrix2D S;

    private FComplexMatrix2D ConjS;

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
    public FloatPeriodicTikhonov2D(ImagePlus imB, ImagePlus imPSF, ResizingType resizing, OutputType output, boolean showPadded, float regParam, float threshold) {
        super("Tikhonov", imB, imPSF, resizing, output, PaddingType.PERIODIC, showPadded, regParam, threshold);
    }

    public ImagePlus deconvolve() {
        IJ.showStatus(name + ": deconvolving");
        S = FloatCommon2D.circShift((FloatMatrix2D) PSF, psfCenter);
        S = ((DenseFloatMatrix2D) S).getFft2();
        B = ((DenseFloatMatrix2D) B).getFft2();
        if (ragParam == -1) {
            IJ.showStatus(name + ": computing regularization parameter");
            ragParam = gcvTikFFT2D((FComplexMatrix2D) S, (FComplexMatrix2D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        ConjS = ((FComplexMatrix2D) S).copy();
        ConjS.assign(FComplexFunctions.conj);
        PSF = ConjS.copy();
        ((FComplexMatrix2D) PSF).assign((FComplexMatrix2D) S, FComplexFunctions.mult);
        S = ((FComplexMatrix2D) PSF).copy();
        ((FComplexMatrix2D) PSF).assign(FComplexFunctions.plus(new float[] { ragParam * ragParam, 0 }));
        ((FComplexMatrix2D) B).assign(ConjS, FComplexFunctions.mult);
        ConjS = ((FComplexMatrix2D) B).copy();
        ((FComplexMatrix2D) ConjS).assign((FComplexMatrix2D) PSF, FComplexFunctions.div);
        ((DenseFComplexMatrix2D) ConjS).ifft2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FComplexMatrix2D) ConjS, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FComplexMatrix2D) ConjS, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FComplexMatrix2D) ConjS, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FComplexMatrix2D) ConjS, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", ip);
        FloatCommon2D.convertImage(imX, output);
        imX.setProperty("regParam", ragParam);
        return imX;
    }

    public void update(float regParam, float threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        PSF = ((FComplexMatrix2D) S).copy();
        ((FComplexMatrix2D) PSF).assign(FComplexFunctions.plus(new float[] { regParam * regParam, 0 }));
        ConjS = ((FComplexMatrix2D) B).copy();
        ((FComplexMatrix2D) ConjS).assign((FComplexMatrix2D) PSF, FComplexFunctions.div);
        ((DenseFComplexMatrix2D) ConjS).ifft2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FComplexMatrix2D) ConjS, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FComplexMatrix2D) ConjS, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, (FComplexMatrix2D) ConjS, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, (FComplexMatrix2D) ConjS, cmY, threshold);
            }
        }
        imX.setProcessor(imX.getTitle(), ip);
        FloatCommon2D.convertImage(imX, output);
    }

    private static float gcvTikFFT2D(FComplexMatrix2D S, FComplexMatrix2D Bhat) {
        AbstractMatrix2D s = S.copy();
        AbstractMatrix2D bhat = Bhat.copy();
        s = ((FComplexMatrix2D) s).assign(FComplexFunctions.abs).getRealPart();
        bhat = ((FComplexMatrix2D) bhat).assign(FComplexFunctions.abs).getRealPart();
        float[] tmp = ((FloatMatrix2D) s).getMinLocation();
        float smin = tmp[0];
        tmp = ((FloatMatrix2D) s).getMaxLocation();
        float smax = tmp[0];
        ((FloatMatrix2D) s).assign(FloatFunctions.square);
        TikFmin2D fmin = new TikFmin2D((FloatMatrix2D) s, (FloatMatrix2D) bhat);
        return FloatFmin.fmin(smin, smax, fmin, FloatCommon2D.FMIN_TOL);
    }

    private static class TikFmin2D implements FloatFmin_methods {
        FloatMatrix2D ssquare;

        FloatMatrix2D bhat;

        public TikFmin2D(FloatMatrix2D ssquare, FloatMatrix2D bhat) {
            this.ssquare = ssquare;
            this.bhat = bhat;
        }

        public float f_to_minimize(float regParam) {
            FloatMatrix2D sloc = ssquare.copy();
            FloatMatrix2D bhatloc = bhat.copy();

            sloc.assign(FloatFunctions.plus(regParam * regParam));
            sloc.assign(FloatFunctions.inv);
            bhatloc.assign(sloc, FloatFunctions.mult);
            bhatloc.assign(FloatFunctions.square);
            float ss = sloc.zSum();
            return bhatloc.zSum() / (ss * ss);
        }

    }
}
