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
import com.cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import com.cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import com.cern.colt.matrix.tdcomplex.DComplexMatrix3D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix1D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix2D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix3D;
import com.cern.colt.matrix.tdouble.DoubleMatrix1D;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.DoubleMatrix3D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.cern.colt.matrix.tfloat.FloatMatrix2D;
import com.cern.jet.math.tdouble.DoubleFunctions;
import com.edu.emory.mathcs.utils.ConcurrencyUtils;
import com.hijizhou.cores.denoising.DDIDstep;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon3D;
import com.edu.emory.mathcs.restoretools.spectral.FloatCommon2D;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

import java.awt.image.ColorModel;
import java.util.concurrent.Future;

public class ImageUtil {


    public static DoubleMatrix colt2blasMatrix(DoubleMatrix2D input){

        return new DoubleMatrix(input.toArray());

    }

    public static DoubleMatrix2D blas2coltMatrix(DoubleMatrix input){
        return new DenseDoubleMatrix2D(input.toArray2());
    }

    public static DoubleMatrix1D blas2coltMatrix1D(DoubleMatrix input){
        return new DenseDoubleMatrix1D(input.toArray());
    }

    public static ComplexDoubleMatrix colt2blasComplexMatrix(DComplexMatrix2D input){

        DoubleMatrix matrixCFreal = new DoubleMatrix(input.getRealPart().toArray());
        DoubleMatrix matrixCFimg = new DoubleMatrix(input.getImaginaryPart().toArray());

        ComplexDoubleMatrix matrix = new ComplexDoubleMatrix(matrixCFreal, matrixCFimg);
        return matrix;

    }

    public static DComplexMatrix2D blas2coltComplexMatrix(ComplexDoubleMatrix input){
        int row = input.rows;
        int column = input.columns;
        DComplexMatrix2D matrix = new DenseDComplexMatrix2D(row, column);

        DoubleMatrix2D real = blas2coltMatrix(input.real());
        DoubleMatrix2D imag = blas2coltMatrix(input.imag());

        matrix.assignReal(real);
        matrix.assignImaginary(imag);

        return matrix;


    }
    public static DComplexMatrix1D blas2coltComplexMatrix1D(ComplexDoubleMatrix input){
        int row = input.rows;
        int column = input.columns;
        DComplexMatrix1D matrix = new DenseDComplexMatrix1D(row*column);

        DoubleMatrix1D real = blas2coltMatrix1D(input.real());
        DoubleMatrix1D imag = blas2coltMatrix1D(input.imag());

        matrix.assignReal(real);
        matrix.assignImaginary(imag);

        return matrix;


    }

    public static DoubleMatrix2D postfiltering(DoubleMatrix2D input, double sigma2) {
        DDIDstep ddid = new DDIDstep();

        ddid.setR(9);
        ddid.setSigma2(sigma2);

        input = ddid.run(input, input);
        return input;
    }

    public static DoubleMatrix2D rescale(DoubleMatrix2D input, double low, double high) {

        DoubleMatrix2D inputT = input.copy();
        double[] minV = inputT.getMinLocation();
        inputT.assign(DoubleFunctions.minus(minV[0]));
        // Obtain and appling the scale:
        double[] maxV = inputT.getMaxLocation();
        if (maxV[0] > 0) {
            inputT.assign(DoubleFunctions.div(maxV[0]));
        }

        if (Math.abs(high - low) != 1) {
            inputT.assign(DoubleFunctions.mult(high - low));
        }
        if (high != 0) {
            inputT.assign(DoubleFunctions.plus(high));
        }

        return inputT;

    }

    public static void preview(DoubleMatrix2D input) {
        FloatProcessor ip = new FloatProcessor(input.rows(), input.columns());
        DoubleCommon2D.assignPixelsToProcessor(ip,
                input, null);

        ImagePlus tmpIP = new ImagePlus("Preview", ip);
        tmpIP.show();
//        new FileSaver(tmpIP).save();
    }

    public static void preview(DoubleMatrix3D input) {
        DoubleMatrix3D aux = input.copy();
        ImageStack tmpOut = new ImageStack(aux.rows(), aux.columns());
        DoubleCommon3D.assignPixelsToStack(tmpOut,
                aux, null);

        ImagePlus tmpIP = new ImagePlus("Preview", tmpOut);
        tmpIP.show();
//        new FileSaver(tmpIP).save();
    }


    public static void previewSave(DoubleMatrix3D input) {
        DoubleMatrix3D aux = input.copy();
        ImageStack tmpOut = new ImageStack(aux.rows(), aux.columns());
        DoubleCommon3D.assignPixelsToStack(tmpOut,
                aux, null);

        ImagePlus tmpIP = new ImagePlus("Preview", tmpOut);
        tmpIP.show();
        new FileSaver(tmpIP).save();
    }

    public static void previewSave(DComplexMatrix3D input) {
        DComplexMatrix3D inputF = input.copy();
        ((DenseDComplexMatrix3D) inputF).ifft3(true);
        ImageStack tmpOut = new ImageStack(inputF.rows(), inputF.columns());
        DoubleMatrix3D MMa = inputF.getRealPart();
        DoubleCommon3D.assignPixelsToStack(tmpOut,
                MMa, null);

        ImagePlus tmpIP = new ImagePlus("Preview", tmpOut);
        tmpIP.show();
    }

    public static void preview(DComplexMatrix3D input) {
        DComplexMatrix3D inputF = input.copy();
        ((DenseDComplexMatrix3D) inputF).ifft3(true);
        ImageStack tmpOut = new ImageStack(inputF.rows(), inputF.columns());
        DoubleMatrix3D MMa = inputF.getRealPart();
        DoubleCommon3D.assignPixelsToStack(tmpOut,
                MMa, null);

        ImagePlus tmpIP = new ImagePlus("Preview", tmpOut);
        tmpIP.show();
    }

    public static ImagePlus matrix2Plus(DoubleMatrix input, ColorModel cmY, String title) {
        int width = input.rows;
        int height = input.columns;
        FloatProcessor ip = new FloatProcessor(height, width);
        matrix2ip(ip,input, cmY);
        ImagePlus imOutput = new ImagePlus(title, ip);
        return imOutput;

    }

    public static ImagePlus matrix2Plus(DoubleMatrix2D input, ColorModel cmY, String title) {
        int width = input.rows();
        int height = input.columns();
        FloatProcessor ip = new FloatProcessor(height, width);
        DoubleCommon2D.assignPixelsToProcessor(ip,
                input, cmY);
        ImagePlus imOutput = new ImagePlus(title, ip);
        return imOutput;

    }

    public static ImagePlus matrix2Plus(FloatMatrix2D input, ColorModel cmY, String title) {
        int width = input.rows();
        int height = input.columns();
        FloatProcessor ip = new FloatProcessor(height, width);
        FloatCommon2D.assignPixelsToProcessor(ip,
                input, cmY);
        ImagePlus imOutput = new ImagePlus(title, ip);
        return imOutput;

    }

    public static ImagePlus matrix2Plus(DoubleMatrix3D input, ColorModel cmY, String title) {
        int width = input.rows();
        int height = input.columns();

        ImageStack stackOut = new ImageStack(width, height);

        DoubleCommon3D.assignPixelsToStack(stackOut, input, cmY);

        ImagePlus imX = new ImagePlus(title, stackOut);

        return imX;
    }

    public static DoubleMatrix ip2matrix(ImageProcessor ipinput) {
        DoubleMatrix input = new DoubleMatrix((double[]) ipinput.convertToFloat().getPixels());
        return input;
    }

    /**
     * Copies pixel values from complex matrix <code>X</code> to image processor
     * <code>ip</code>
     *
     * @param ip  image processor
     * @param X   matrix
     * @param cmY color model
     */
    public static void matrix2ip(final FloatProcessor ip, final DoubleMatrix X, final java.awt.image.ColorModel cmY) {
        final int rows = X.rows;
        final int cols = X.columns;
        final float[] px = (float[]) ip.getPixels();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if ((np > 1) && (rows * cols >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            Future<?>[] futures = new Future[np];
            int k = rows / np;
            for (int j = 0; j < np; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == np - 1) ? rows : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        int idx = firstRow * 2 * cols;
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < cols; c++) {
                                px[c + cols * r] = (float) X.get(idx);
                                idx += 2;
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);

            ip.setMinAndMax(0, 0);
            ip.setColorModel(cmY);
        }
    }
}
