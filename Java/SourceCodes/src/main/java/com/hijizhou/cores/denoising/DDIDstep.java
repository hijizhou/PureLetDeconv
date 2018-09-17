package com.hijizhou.cores.denoising;

import com.cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.cern.jet.math.tdcomplex.DComplexFunctions;
import com.cern.jet.math.tdouble.DoubleFunctions;
import com.hijizhou.utilities.Evaluation;
import com.hijizhou.utilities.ImageUtil;
import com.hijizhou.utilities.PSFUtil;
import com.hijizhou.utilities.Simulation;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon2D;
import com.edu.emory.mathcs.utils.ConcurrencyUtils;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

import java.util.concurrent.Future;

public class DDIDstep {
    private double sigma2 = 5.0f;
    private int r = 15;
    private double sigma_s = 10f;
    private double gamma_r = 10f;
    private double gamma_f = 200f;

    public void setSigma2(double sigma2) {
        this.sigma2 = sigma2;
    }

    public void setR(int r) {
        this.r = r;
    }

    public void setSigma_s(double sigma_s) {
        this.sigma_s = sigma_s;
    }

    public void setGamma_r(double gamma_r) {
        this.gamma_r = gamma_r;
    }

    public void setGamma_f(double gamma_f) {
        this.gamma_f = gamma_f;
    }

    public double getSigma2() {
        return sigma2;
    }

    public int getR() {
        return r;
    }

    public double getSigma_s() {
        return sigma_s;
    }

    public double getGamma_r() {
        return gamma_r;
    }

    public double getGamma_f() {
        return gamma_f;
    }

    public DoubleMatrix2D run(DoubleMatrix2D x, DoubleMatrix2D y) {

        int width = x.columns();
        int height = x.rows();

        DoubleMatrix2D output = new DenseDoubleMatrix2D(height, width);

        int sW = 2 * r + 1;
        DoubleMatrix2D H = getH(sW);

        //Boundary extension - symmetric
        DoubleMatrix2D xp = symExtension(x);
        DoubleMatrix2D yp = symExtension(y);

        double[][] o_m = new double[width][height];

        int np = ConcurrencyUtils.getNumberOfThreads();

//        long startTime = System.nanoTime();

        if ((np > 1) && (width * height >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            Future<?>[] futures = new Future[np];
            int k = height / np;
            for (int j = 0; j < np; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == np - 1) ? height : firstRow + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        //basis functions for each pixel
                        for (int i = firstRow; i < lastRow; i++) {
                            for (int j = 0; j < height; j++) {
//                                long startTime_pixel = System.nanoTime();

                                double value = basisPixel2(xp, yp, i, j, sW, H);
                                o_m[i][j] = value;

//                                long endTime_pixel = System.nanoTime();
//                                System.out.println(" ----- time: " + (endTime_pixel - startTime_pixel) / 1000000000.0);
                            }
                        }

                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        }

        output.assign(o_m);

//        long endTime = System.nanoTime();
//        System.out.println(" step time: " + (endTime - startTime) / 1000000000.0);

        return output;
    }

    public double basisPixel2(DoubleMatrix2D xp, DoubleMatrix2D yp, int i, int j, int sW, DoubleMatrix2D H) {

        DoubleMatrix2D g = xp.viewPart(i, j, sW, sW).copy();
        DoubleMatrix2D y = yp.viewPart(i, j, sW, sW).copy();

        // Spatial Domain: Bilateral Filter
        double value = 0.0d;

        double center = g.getQuick(this.r, this.r);
        DoubleMatrix2D k = g.copy();
//
        k.assign(DoubleFunctions.minus(center)); //d
        k.assign(DoubleFunctions.pow(2));
        k.assign(DoubleFunctions.div((-1) * this.gamma_r * this.sigma2));
        k.assign(DoubleFunctions.exp);
        k.assign(H, DoubleFunctions.mult);

        DoubleMatrix2D gk = g.copy();
        gk.assign(k, DoubleFunctions.mult);
        double gt = gk.zSum();
        gt = gt / k.zSum();

        DoubleMatrix2D yk = y.copy();
        yk.assign(k, DoubleFunctions.mult);
        double yt = yk.zSum();
        yt = yt / k.zSum();

        // Fourier Domain: Wavelet Shrinkage
        DoubleMatrix2D V = k.copy();
        V.assign(DoubleFunctions.pow(2));
        double v = V.zSum();
        v = v * this.sigma2 * this.gamma_f * (-1);

        g.assign(DoubleFunctions.minus(gt));
        g.assign(k, DoubleFunctions.mult);

        y.assign(DoubleFunctions.minus(yt));
        y.assign(k, DoubleFunctions.mult);

        int[] psfCenter = {this.r, this.r};
        g = DoubleCommon2D.circShift(
                g, psfCenter);
        DComplexMatrix2D G = ((DenseDoubleMatrix2D) g).getFft2();
        y = DoubleCommon2D.circShift(
                y, psfCenter);
        DComplexMatrix2D S = ((DenseDoubleMatrix2D) y).getFft2();

        DComplexMatrix2D K = G.copy();
        K.assign(DComplexFunctions.conj);
        K.assign(G, DComplexFunctions.mult);

        //to avoid the NaN
        double[] epsilon = {0.00000001, 0.0000001};
        K.assign(DComplexFunctions.plus(epsilon));

        K.assign(DComplexFunctions.inv);
        K.assign(DComplexFunctions.mult(v));
        K.assign(DComplexFunctions.exp);

        S.assign(K, DComplexFunctions.mult);
        double St = S.zSum()[0] / (sW * sW); //real value

        value = St + yt;

        return value;
    }

    public DoubleMatrix2D getH(int sW) {
        DoubleMatrix2D Hmatrix = new DenseDoubleMatrix2D(sW, sW);
        double tmp = 0.0d;
        for (int i = 0; i < sW; i++) {
            for (int j = 0; j < sW; j++) {
                int dx = i - this.r;
                int dy = j - this.r;
                tmp = (-1) * (dx * dx + dy * dy) / (2 * this.sigma_s * this.sigma_s);
                Hmatrix.setQuick(i, j, Math.exp(tmp));
            }
        }
        return Hmatrix;
    }

    public DoubleMatrix2D symExtension(DoubleMatrix2D input) {
        //imagesize
        int wI = input.columns();
        int hI = input.rows();
        int wO = wI + 2 * this.r;
        int hO = hI + 2 * this.r;

        DoubleMatrix2D output = new DenseDoubleMatrix2D(hO, wO);
        double[][] o_m = new double[hO][wO];
        double[][] i_m = input.toArray();

        int idX = 0;
        int idY = 0;
        double value = 0.0f;
        //! Inner image

        for (int i = 0; i < wI; i++) {
            for (int j = 0; j < hI; j++) {
                o_m[r + j][r + i] = i_m[j][i];
            }
        }

        // Top and bottom
        for (int i = 0; i < wI; i++) {
            for (int j = 0; j < this.r; j++) {
                idX = this.r - j - 1;
                value = i_m[j][i];
                o_m[idX][i + this.r] = value;

                idX = hI + this.r + j;
                value = i_m[hI - j - 1][i];
                o_m[idX][i + this.r] = value;
            }
        }

        // Left and right
        for (int i = 0; i < hO; i++) {
            for (int j = 0; j < this.r; j++) {
                idY = this.r - j - 1;
                value = o_m[i][this.r + j];
                o_m[i][idY] = value;

                idY = wI + this.r + j;
                value = o_m[i][wI + this.r - j - 1];
                o_m[i][idY] = value;
            }
        }
        output.assign(o_m);
        return output;
    }


    public static void main(String[] args) {
        new ImageJ();
//
//        // open the Clown sample
        ImagePlus ipInput = IJ.openImage("/Users/hijizhou/Dropbox/A Documents/Research/A Projects/E2 PURE-LET Java/PureLetDeconv_Icy/ImageJ/src/main/resources/cameraman.tif");

        DDIDstep ddid = new DDIDstep();

//        double[] imNoisy = {0.9942, 0.0500, 0.2331, 0.7855, 0.1429,
//                0.3217, 0.4811, 0.4680, 0.1955, 0.9974,
//                0.6910, 0.8710, 0.4604, 0.9858, 0.4149};
//        double[] imNoisy = {1, 2,3,4};

//        ddid.setR(1);

//        DoubleMatrix2D input = new DenseDoubleMatrix2D(2,2);
//        input.assign(imNoisy);

        DoubleMatrix2D original = new DenseDoubleMatrix2D(256, 256);
        DoubleCommon2D.assignPixelsToMatrix(original, ipInput.getProcessor());

//        DoubleMatrix2D input = original.copy();
        double sigma = 5;
//        Simulation.gaussian(input, sigma);

        DoubleMatrix2D PSF = PSFUtil.getGaussPSF(256, 256, Math.sqrt(3));
        double[] noisePar = {2, 0};
        DoubleMatrix2D input = Simulation.getInput(original, PSF, noisePar);

//        Simulation.poisson(input, sigma);

        ddid.setR(5);
        ddid.setSigma2(sigma * sigma * 50);
        DoubleMatrix2D output = ddid.run(input, input);

        ddid.setGamma_r(8.7f);
        ddid.setGamma_f(0.4f);
        output = ddid.run(output, input);
////
        ddid.setGamma_r(0.7f);
        ddid.setGamma_f(0.8f);
        output = ddid.run(output.copy(), input);


        ImagePlus imgIn = ImageUtil.matrix2Plus(input, ipInput.getProcessor().getColorModel(), "Input");
        imgIn.show();

        ImagePlus imgOut = ImageUtil.matrix2Plus(output, ipInput.getProcessor().getColorModel(), "Output");
        imgOut.show();

        double inputPSNR = Evaluation.psnr(input, original);
        System.out.println(inputPSNR);

        double outputPSNR = Evaluation.psnr(output, original);
        System.out.println(outputPSNR);
    }

}
