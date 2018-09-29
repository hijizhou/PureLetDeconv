package com.hijizhou.utilities;

/**
 * @reference
 *       [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution,
 *             IEEE Trans. Image Process., vol. 27, no. 1, pp. 92-105, 2018.
 *       [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP 2016), Phoenix, Arizona, USA, 2016, pp.2708-2712.
 *       [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI 2017), Melbourne, Australia, 2017, pp. 723-727.
 *
 * @author	Jizhou Li
 *			The Chinese University of Hong Kong
 *
 */
import com.cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import com.cern.colt.matrix.tdcomplex.DComplexMatrix3D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix2D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix3D;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.DoubleMatrix3D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import com.cern.colt.matrix.tfloat.FloatMatrix2D;
import com.cern.jet.math.tdcomplex.DComplexFunctions;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon3D;

public class Simulation {

    public static DoubleMatrix2D getInput(DoubleMatrix2D original, DoubleMatrix2D PSF, double[] noisePar) {

        DComplexMatrix2D Y = ((DenseDoubleMatrix2D) original).getFft2();

        int[] psfCenter = PSFUtil.getCenter((DenseDoubleMatrix2D) PSF);
        PSF = DoubleCommon2D.circShift(
                PSF, psfCenter);
        PSF.normalize();

        DComplexMatrix2D H = ((DenseDoubleMatrix2D) PSF).getFft2();
        Y.assign(H, DComplexFunctions.mult);
        ((DenseDComplexMatrix2D) Y).ifft2(true);

        DoubleMatrix2D input = Y.getRealPart();
        poisson(input, noisePar[0]);
        gaussian(input, noisePar[1]);

        return input;
    }

    public static DoubleMatrix3D getInput(DoubleMatrix3D original, DoubleMatrix3D PSF, double[] noisePar) {

        DComplexMatrix3D Y = ((DenseDoubleMatrix3D) original).getFft3();
        int[] psfCenter = PSFUtil.getCenter((DenseDoubleMatrix3D) PSF.copy());

//        ImageUtil.preview(PSF);
//
////        psfCenter[0] = 32;
////        psfCenter[1] = 63;
////        psfCenter[2] = 63;
        PSF = DoubleCommon3D.circShift(
                PSF, psfCenter);
//        int sz = PSF.slices()/2;
//        int sx = PSF.rows()/2;
//        int sy = PSF.columns()/2;
//
//        DoubleMatrix3D PSF2 = PSF.copy();
//        PSF2.viewPart(0, 0, 0, PSF.slices()/2, PSF.rows(), PSF.columns())
//                .assign(PSF.viewPart(sz, 0, 0, PSF.slices()/2, PSF.rows(), PSF.columns()));
//        PSF2.viewPart(sz, 0, 0, PSF.slices()/2, PSF.rows(), PSF.columns())
//                .assign(PSF.viewPart(0, 0, 0, PSF.slices()/2, PSF.rows(), PSF.columns()));
//
//
//        PSF.viewPart(0, 0, 0, PSF.slices(), PSF.rows()/2, PSF.columns()/2)
//                .assign(PSF2.viewPart(0, sx, sy, PSF.slices(), PSF.rows()/2, PSF.columns()/2));
//
//        PSF.viewPart(0, sx, 0, PSF.slices(), PSF.rows()/2, PSF.columns()/2)
//                .assign(PSF2.viewPart(0, 0, sy, PSF.slices(), PSF.rows()/2, PSF.columns()/2));
//
//
//        PSF.viewPart(0, 0, sy, PSF.slices(), PSF.rows()/2, PSF.columns()/2)
//                .assign(PSF2.viewPart(0, sx, 0, PSF.slices(), PSF.rows()/2, PSF.columns()/2));
//
//        PSF.viewPart(0, sx, sy, PSF.slices(), PSF.rows()/2, PSF.columns()/2)
//                .assign(PSF2.viewPart(0, 0, 0, PSF.slices(), PSF.rows()/2, PSF.columns()/2));


//        ImageUtil.preview(PSF);

//        System.out.println(PSF.getQuick(10,10,10));

        PSF.normalize();
        DComplexMatrix3D H = ((DenseDoubleMatrix3D) PSF).getFft3();
        Y.assign(H, DComplexFunctions.mult);
        ((DenseDComplexMatrix3D) Y).ifft3(true);

//        ImageUtil.preview(Y.getRealPart());
        DoubleMatrix3D input = Y.getRealPart();
//        poisson(input, noisePar[0]);
//        gaussian(input, noisePar[1]);
//        DComplexMatrix3D YY = ((DenseDoubleMatrix3D) input).getFft3();
//
        System.out.println(H.getQuick(30,21,10)[0]);

        poisson(input, noisePar[0]);
        gaussian(input, noisePar[1]);

        return input;
    }


    public static void gaussian(DoubleMatrix2D x, double sigma) {
        PsRandom rand = new PsRandom(1234);
        int nx = x.rows();
        int ny = x.columns();
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                double pixel = x.getQuick(i, j);
                pixel += rand.nextGaussian(0, sigma);
                x.setQuick(i, j, pixel);
            }
        }
    }

    public static void gaussian(FloatMatrix2D x, double sigma) {
        PsRandom rand = new PsRandom(1234);
        int nx = x.rows();
        int ny = x.columns();
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                float pixel = x.getQuick(i, j);
                pixel += rand.nextGaussian(0, sigma);
                x.setQuick(i, j, pixel);
            }
        }
    }

    public static void gaussian(DoubleMatrix3D x, double sigma) {
        PsRandom rand = new PsRandom(1234);
        int nx = x.rows();
        int ny = x.columns();
        int nz = x.slices();
        for (int k = 0; k < nz; k++) {
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny; j++) {
                    double pixel = x.getQuick(k, i, j);
                    pixel += rand.nextGaussian(0, sigma);
                    x.setQuick(k, i, j, pixel);
                }
            }
        }
    }

    public static void poisson(DoubleMatrix3D x, double factor) {
        PsRandom rand = new PsRandom(1234);
        int nx = x.rows();
        int ny = x.columns();
        int nz = x.slices();

        if (factor < 1e-6)
            return;
        double f = 1.0 / (factor);
        for (int k = 0; k < nz; k++) {
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny; j++) {
                    double pixel = x.getQuick(k, i, j);
                    if (pixel > 1e-6) {
                        pixel = rand.nextPoissonian(f * (pixel)) * factor;
                    }
                    x.setQuick(k, i, j, pixel);
                }
            }
        }

    }

    public static void poisson(DoubleMatrix2D x, double factor) {
        PsRandom rand = new PsRandom(1234);
        int nx = x.rows();
        int ny = x.columns();

        if (factor < 1e-6)
            return;
        double f = 1.0 / (factor);
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                double pixel = x.getQuick(i, j);
                if (pixel > 1e-6) {
                    pixel = rand.nextPoissonian(f * (pixel)) * factor;
                }
                x.setQuick(i, j, pixel);
            }
        }

    }
}
