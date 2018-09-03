package com.hijizhou.utilities;

/**
 * EVALUATION: Evaluation the restoration performance
 *
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

import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.DoubleMatrix3D;
import com.cern.colt.matrix.tfloat.FloatMatrix2D;
import ij.ImagePlus;

public class Evaluation {

    public static double psnr(DoubleMatrix2D testPlus, DoubleMatrix2D refPlus) {

        if ((testPlus.rows() == refPlus.rows())
                && (testPlus.columns() == refPlus.columns())) {
            int w = refPlus.rows(), h = refPlus.columns();
            double sum = 0;
            double[][] m1 = refPlus.toArray();
            double[][] m2 = testPlus.toArray();
            double maxValue = m1[0][0];
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    double aux = (m1[j][i] - m2[j][i]);
                    double aux2 = Math.pow(aux, 2);
                    sum = sum + aux2;
                    // image maximum value
                    if (m1[j][i] > maxValue) {
                        maxValue = m1[j][i];
                    }
                }
            }
            double preRes = (maxValue * maxValue * w * h) / sum;
            double res = 10 * Math.log10(preRes);
            return res;
        } else {
            return 0;
        }
    }

    public static double psnr(FloatMatrix2D testPlus, FloatMatrix2D refPlus) {

        if ((testPlus.rows() == refPlus.rows())
                && (testPlus.columns() == refPlus.columns())) {
            int w = refPlus.rows(), h = refPlus.columns();
            double sum = 0;
            float[][] m1 = refPlus.toArray();
            float[][] m2 = testPlus.toArray();
            double maxValue = m1[0][0];
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    double aux = (m1[j][i] - m2[j][i]);
                    double aux2 = Math.pow(aux, 2);
                    sum = sum + aux2;
                    // image maximum value
                    if (m1[j][i] > maxValue) {
                        maxValue = m1[j][i];
                    }
                }
            }
            double preRes = (maxValue * maxValue * w * h) / sum;
            double res = 10 * Math.log10(preRes);
            return res;
        } else {
            return 0;
        }
    }

    public static double psnr(DoubleMatrix3D testPlus, DoubleMatrix3D refPlus) {

        if ((testPlus.slices() == refPlus.slices()) && (testPlus.rows() == refPlus.rows())
                && (testPlus.columns() == refPlus.columns())) {
            int w = refPlus.rows(), h = refPlus.columns();
            int s = refPlus.slices();

            double sum = 0;
            double[][][] m1 = refPlus.toArray();
            double[][][] m2 = testPlus.toArray();
            double maxValue = m1[0][0][0];
            for(int k = 0; k<s; k++){
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    double aux = (m1[k][j][i] - m2[k][j][i]);
                    double aux2 = Math.pow(aux, 2);
                    sum = sum + aux2;
                    // image maximum value
                    if (m1[k][j][i] > maxValue) {
                        maxValue = m1[k][j][i];
                    }
                }
            }
            }
            double preRes = (maxValue * maxValue * w * h*s) / sum;
            double res = 10 * Math.log10(preRes);
            return res;
        } else {
            return 0;
        }
    }

    public static double psnr(ImagePlus testPlus, ImagePlus refPlus) {

        if ((testPlus.getWidth() == refPlus.getWidth())
                && (testPlus.getHeight() == refPlus.getHeight())) {
            int w = refPlus.getWidth(), h = refPlus.getHeight();
            double sum = 0;
            float[][] m1 = refPlus.getProcessor().getFloatArray();
            float[][] m2 = testPlus.getProcessor().getFloatArray();
            double maxValue = m1[0][0];
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    double aux = (m1[j][i] - m2[j][i]);
                    double aux2 = Math.pow(aux, 2);
                    sum = sum + aux2;
                    // image maximum value
                    if (m1[j][i] > maxValue) {
                        maxValue = m1[j][i];
                    }
                }
            }
            double preRes = (maxValue * maxValue * w * h) / sum;
            double res = 10 * Math.log10(preRes);
            return res;
        } else {
            return 0;
        }
    }
}
