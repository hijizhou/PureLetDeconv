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
import com.cern.colt.matrix.tfloat.FloatMatrix2D;
import com.cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import com.cern.jet.math.tfloat.FloatFunctions;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.spectral.AbstractFloatSpectralDeconvolver2D;
import com.edu.emory.mathcs.restoretools.spectral.FloatCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.PaddingType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;

/**
 * 2D Tikhonov with reflexive boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class FloatReflexiveTikhonov2D extends AbstractFloatSpectralDeconvolver2D {

    private FloatMatrix2D E1;

    private FloatMatrix2D S;

    /**
     * Creates new instance of FloatTikDCT2D
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
    public FloatReflexiveTikhonov2D(ImagePlus imB, ImagePlus imPSF, ResizingType resizing, OutputType output, boolean showPadded, float regParam, float threshold) {
        super("Tikhonov", imB, imPSF, resizing, output, PaddingType.REFLEXIVE, showPadded, regParam, threshold);
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
            ragParam = gcvTikDCT2D(S, (FloatMatrix2D) B);
        }
        IJ.showStatus(name + ": deconvolving");
        PSF = S.copy();
        ((FloatMatrix2D) PSF).assign(FloatFunctions.square);
        E1 = ((FloatMatrix2D) PSF).copy();
        ((FloatMatrix2D) PSF).assign(FloatFunctions.plus(ragParam * ragParam));
        ((FloatMatrix2D) B).assign(S, FloatFunctions.mult);
        S = ((FloatMatrix2D) B).copy();
        S.assign((FloatMatrix2D) PSF, FloatFunctions.div);
        ((DenseFloatMatrix2D) S).idct2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, S, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, S, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, S, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, S, cmY, threshold);
            }
        }
        ImagePlus imX = new ImagePlus("Deblurred", ip);
        FloatCommon2D.convertImage(imX, output);
        imX.setProperty("regParam", ragParam);
        return imX;

    }

    public void update(float regParam, float threshold, ImagePlus imX) {
        IJ.showStatus(name + ": updating");
        PSF = E1.copy();
        ((FloatMatrix2D) PSF).assign(FloatFunctions.plus(regParam * regParam));
        S = ((FloatMatrix2D) B).copy();
        S.assign((FloatMatrix2D) PSF, FloatFunctions.div);
        ((DenseFloatMatrix2D) S).idct2(true);
        IJ.showStatus(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, S, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, S, cmY);
            }
        } else {
            if (isPadded) {
                FloatCommon2D.assignPixelsToProcessorPadded(ip, S, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                FloatCommon2D.assignPixelsToProcessor(ip, S, cmY, threshold);
            }
        }
        imX.setProcessor(imX.getTitle(), ip);
        FloatCommon2D.convertImage(imX, output);

    }

    private static float gcvTikDCT2D(FloatMatrix2D S, FloatMatrix2D Bhat) {
        FloatMatrix2D s = S.copy();
        FloatMatrix2D bhat = Bhat.copy();
        s.assign(FloatFunctions.abs);
        bhat.assign(FloatFunctions.abs);
        float[] tmp = s.getMinLocation();
        float smin = tmp[0];
        tmp = s.getMaxLocation();
        float smax = tmp[0];
        s.assign(FloatFunctions.square);
        TikFmin2D fmin = new TikFmin2D(s, bhat);
        return (float) FloatFmin.fmin(smin, smax, fmin, FloatCommon2D.FMIN_TOL);
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
