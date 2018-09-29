package com.hijizhou.cores.deconvolution;
/**
 * @reference [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution,
 * IEEE Trans. Image Process., vol. 27, no. 1, pp. 92-105, 2018.
 * [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP 2016), Phoenix, Arizona, USA, 2016, pp.2708-2712.
 * [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI 2017), Melbourne, Australia, 2017, pp. 723-727.
 * @author Jizhou Li
 * The Chinese University of Hong Kong
 */

import com.cern.colt.matrix.AbstractMatrix2D;
import com.cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import com.cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix1D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix2D;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.cern.jet.math.tdcomplex.DComplexFunctions;
import com.cern.jet.math.tdouble.DoubleFunctions;
import com.hijizhou.utilities.FFT_WFilter;
import com.hijizhou.utilities.ImageUtil;
import com.hijizhou.utilities.PSFUtil;
import com.hijizhou.utilities.SpectralUtils;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

import java.util.concurrent.Callable;

//completely using the jblas library

public class LETProcess2Djblas implements Callable<LETProcess2Djblas> {

    private int width;
    private int height;
    private final int jIndex;
    private final int oIndex;
    private FFT_WFilter wf;
    //	private DComplexMatrix2D Yi;
    private ComplexDoubleMatrix Yi;
    private double alpha;
    //	private DComplexMatrix2D Hli;
//	private DComplexMatrix2D Hbit;
//	private AbstractMatrix2D auxInput;
    private ComplexDoubleMatrix auxInput;
    private ComplexDoubleMatrix Hli;
    private ComplexDoubleMatrix Hbit;

    private ReValue returnvalue = new ReValue();

    public static class ReValue {
        private ComplexDoubleMatrix[] matrixCFele = new ComplexDoubleMatrix[2];
        private double[] div = new double[2];

        public ReValue() {

        }

//		public void setValues(DComplexMatrix1D matrixCFele1,
//                              DComplexMatrix1D matrixCFele2, double div1, double div2) {
//			this.matrixCFele[0] = matrixCFele1;
//			this.matrixCFele[1] = matrixCFele2;
//			this.div[0] = div1;
//			this.div[1] = div2;
//		}

        public void setF1(ComplexDoubleMatrix matrixCFele) {
            this.matrixCFele[0] = matrixCFele;
        }

        public void setF2(ComplexDoubleMatrix matrixCFele) {
            this.matrixCFele[1] = matrixCFele;
        }

        public void setDiv1(double div) {
            this.div[0] = div;
        }

        public void setDiv2(double div) {
            this.div[1] = div;
        }

        public ComplexDoubleMatrix[] getF() {
            return this.matrixCFele;
        }

        public double[] getDiv() {
            return this.div;
        }
    }

    public LETProcess2Djblas(double alpha, int jIndex, int oIndex, FFT_WFilter wf,
                             ComplexDoubleMatrix auxInput, ComplexDoubleMatrix Yi,
                             ComplexDoubleMatrix Hli, ComplexDoubleMatrix Hbit) {
        this.alpha = alpha;
        this.jIndex = jIndex;
        this.oIndex = oIndex;
        this.wf = wf;
        this.Yi = Yi;
        this.Hli = Hli;
        this.Hbit = Hbit;
        this.auxInput = auxInput;
        this.width = wf.getDx().length / 2;
        this.height = wf.getDy().length / 2;
    }

    public ReValue getRV() {
        return this.returnvalue;
    }

    @Override
    public LETProcess2Djblas call() throws Exception {

        ComplexDoubleMatrix decEle = new ComplexDoubleMatrix(width, height);
        ComplexDoubleMatrix decEleH = new ComplexDoubleMatrix(width, height);
        ComplexDoubleMatrix decEleConj = new ComplexDoubleMatrix(width, height);
        ComplexDoubleMatrix recEle = new ComplexDoubleMatrix(width, height);

        ComplexDoubleMatrix di = new ComplexDoubleMatrix(width, height);
        DoubleMatrix diReal = new DoubleMatrix(width, height);
        DoubleMatrix di2 = new DoubleMatrix(width, height);
        ComplexDoubleMatrix Di2 = new ComplexDoubleMatrix(width, height);

        ComplexDoubleMatrix aux_w = new ComplexDoubleMatrix(width, height);
        DoubleMatrix w = new DoubleMatrix(width, height);
        ComplexDoubleMatrix aux_v = new ComplexDoubleMatrix(width, height);
        DoubleMatrix v = new DoubleMatrix(width, height);

        DoubleMatrix tp = new DoubleMatrix(width, height);
        DoubleMatrix betav = new DoubleMatrix(width, height);
        DoubleMatrix s = new DoubleMatrix(width, height);
        DoubleMatrix t = new DoubleMatrix(width, height);

        ComplexDoubleMatrix auxrt = new ComplexDoubleMatrix(width, height);
        DoubleMatrix rt = new DoubleMatrix(width, height);

        ComplexDoubleMatrix DiRt = new ComplexDoubleMatrix(width, height);
        DoubleMatrix auxDiRt = new DoubleMatrix(width, height);

        ComplexDoubleMatrix auxW = new ComplexDoubleMatrix(width, height);
        DoubleMatrix w1 = new DoubleMatrix(width, height);
        DoubleMatrix w2 = new DoubleMatrix(width, height);
        DoubleMatrix ww = new DoubleMatrix(width, height);

        wf.fft_wfilters2D(width, height, oIndex, jIndex + 1);

        decEle = ImageUtil.colt2blasComplexMatrix(PSFUtil.getDecMatrix2D(wf));


        // Di = D.*Hli;
        decEleH = decEle.dup();
        decEleH.muli(Hli);
        decEleConj = decEle.dup();
        decEleConj.conj();
        recEle = decEleConj.dup();
        recEle.divi(Math.pow(4, jIndex + 1));

        di = decEleH.dup();
        SpectralUtils.invfft2D_inplace(di);
        diReal = di.getReal();

        di2 = diReal.dup();

        MatrixFunctions.powi(di2, 2);

        Di2 = SpectralUtils.fft2D(new ComplexDoubleMatrix((di2)));

        aux_w = decEle.dup();
        aux_w.muli(Yi);
        SpectralUtils.invfft2D_inplace(aux_w);
        w = aux_w.getReal();

        aux_v = Di2.dup();
        aux_v.muli(auxInput);
        SpectralUtils.invfft2D_inplace(aux_v);
        v = aux_v.getReal();
        v.muli(this.alpha);

        double eps = 1e-9;
        double beta = 1e2;
        betav = v.dup();
        betav.muli(beta);

        for (int hhi = 0; hhi < height; hhi++) {
            for (int wwi = 0; wwi < width; wwi++) {
                double aux = betav.get(hhi, wwi);
                aux = Math.tanh(aux);
                betav.put(hhi, wwi, aux);
            }
        }

        s = betav.dup();

        betav.muli(v);
        betav.addi(eps);
        MatrixFunctions.powi(betav, 1 / 2);

        t = betav.dup();

        betav = s.dup();
        MatrixFunctions.powi(betav, 2);
        betav.muli(-1);
        betav.addi(1);
        betav.muli(beta);
        betav.muli(v);
        betav.addi(s);

        tp.fill(0.5);
        tp.divi(t);
        tp.muli(betav);

        // divergence term
        auxrt = recEle.dup();
        auxrt.muli(Hbit);
        auxrt.conji();

        SpectralUtils.invfft2D_inplace(auxrt);
        rt = auxrt.getReal();

        auxDiRt = rt.dup();
        auxDiRt.muli(diReal);
        DiRt = SpectralUtils.fft2D(new ComplexDoubleMatrix((auxDiRt)));

        auxDiRt = rt.dup();
        auxDiRt.muli(di2);
        ComplexDoubleMatrix Di2Rt = SpectralUtils.fft2D(new ComplexDoubleMatrix((auxDiRt)));


        auxW = DiRt.dup();
        auxW.muli(auxInput);
        SpectralUtils.invfft2D_inplace(auxW);
        w1 = auxW.getReal();

        auxW = Di2Rt.dup();
        auxW.muli(auxInput);
        SpectralUtils.invfft2D_inplace(auxW);
        w2 = auxW.getReal();

        // whether to threshold
        ww = w.dup();
        MatrixFunctions.powi(ww, 2);
        double mean1 = ww.sum() / (width * height);

        ww = t.dup();
        MatrixFunctions.powi(ww, 2);
        double mean2 = 2 * ww.sum()
                / (width * height);

        if (mean1 <= mean2) {
            return this;
        }

//		double startTime = System.nanoTime();

        // begin thresholding
        ReValue rv = itemThreshold(w1, w2, w, t, tp, recEle, width, height,
                alpha);
        this.returnvalue = rv;

//		double endTime = System.nanoTime();
//		System.out.println("inner time: " + (endTime - startTime) / 1.0E9D);

        return this;
    }

    public static class Theta {
        private DoubleMatrix F_theta;
        private DoubleMatrix F_theta1;
        private DoubleMatrix F_theta2;

        private ComplexDoubleMatrix matrixCFele;
        private double div;

        public Theta(int width, int height) {
            this.F_theta = new DoubleMatrix(width, height);
            this.F_theta1 = new DoubleMatrix(width, height);
            this.F_theta2 = new DoubleMatrix(width, height);
            this.matrixCFele = new ComplexDoubleMatrix(width * height);
        }

        public DoubleMatrix getF_theta() {
            return F_theta;
        }

        public void setF_theta(DoubleMatrix f_theta) {
            F_theta = f_theta;
        }

        public DoubleMatrix getF_theta1() {
            return F_theta1;
        }

        public void setF_theta1(DoubleMatrix f_theta1) {
            F_theta1 = f_theta1;
        }

        public DoubleMatrix getF_theta2() {
            return F_theta2;
        }

        public void setF_theta2(DoubleMatrix f_theta2) {
            F_theta2 = f_theta2;
        }

        public ComplexDoubleMatrix getMatrixCFele() {
            return matrixCFele;
        }

        public void setMatrixCFele(ComplexDoubleMatrix matrixCFele) {
            this.matrixCFele = matrixCFele;
        }

        public double getDiv() {
            return div;
        }

        public void setDiv(double div) {
            this.div = div;
        }


    }

    public static ReValue itemThreshold(DoubleMatrix w1, DoubleMatrix w2,
                                        DoubleMatrix w, DoubleMatrix t, DoubleMatrix tp,
                                        ComplexDoubleMatrix recEle, int width, int height, double alpha) {

        ReValue rv = new ReValue();

        DoubleMatrix w1_tmp = w1.dup();
        DoubleMatrix w2_tmp = w2.dup();
        DoubleMatrix w_tmp = w.dup();
        DoubleMatrix t_tmp = t.dup();
        DoubleMatrix tp_tmp = tp.dup();

        Theta theta = new Theta(width, height);

        // factor=4;
        theta = aux_threshold(alpha, 4.0D, recEle, w_tmp, w1_tmp, w2_tmp,
                t_tmp, tp_tmp, width, height, theta);

        rv.setDiv1(theta.getDiv());
        rv.setF1(theta.getMatrixCFele());

        w1_tmp = w1.dup();
        w2_tmp = w2.dup();
        w_tmp = w.dup();
        t_tmp = t.dup();
        tp_tmp = tp.dup();

        // factor=9;
        theta = aux_threshold(alpha, 9.0D, recEle, w_tmp, w1_tmp, w2_tmp,
                t_tmp, tp_tmp, width, height, theta);

        rv.setDiv2(theta.getDiv());
        rv.setF2(theta.getMatrixCFele());

        return rv;
    }

    public static Theta aux_threshold(double alpha, double factor,
                                      ComplexDoubleMatrix recEle, DoubleMatrix w, DoubleMatrix w1_tmp,
                                      DoubleMatrix w2_tmp, DoubleMatrix t, DoubleMatrix tp,
                                      int width, int height, Theta theta) {

//		double startTime = System.nanoTime(); // start timing

//		double startFor = System.nanoTime();

        DoubleMatrix y3 = w.dup();
        MatrixFunctions.powi(y3, 3);

        DoubleMatrix y4 = y3.dup();
        y4.muli(w);

        ComplexDoubleMatrix matrixCF = new ComplexDoubleMatrix(width * height);
        theta = aux_hard4(w, y3, y4, factor, t, tp, theta);

//		double runningTime = (System.nanoTime() - startTime) / 1.0E9D;
//		System.out.println("aux_threshold  = " + runningTime + " s");

        ComplexDoubleMatrix F_thetaFFT = SpectralUtils.fft2D(new ComplexDoubleMatrix((theta.getF_theta())));

        F_thetaFFT.muli(recEle);

        matrixCF = F_thetaFFT.reshape(F_thetaFFT.rows*F_thetaFFT.columns,1);

        w1_tmp.muli(theta.getF_theta1());
        w2_tmp.muli(theta.getF_theta2());
        w1_tmp.addi(w2_tmp);
        w1_tmp.muli(alpha);

        double auxDiv = w1_tmp.sum();

        theta.setDiv(auxDiv);
        ;
        theta.setMatrixCFele(matrixCF);

        return theta;
    }

    public static Theta aux_hard4(DoubleMatrix F_y, DoubleMatrix y3, DoubleMatrix y4, double F_factor,
                                  DoubleMatrix F_T, DoubleMatrix F_Tp, Theta theta) {
//		double startFor = System.nanoTime();

        F_T.muli(F_factor);

        DoubleMatrix T4 = F_T.dup();
        MatrixFunctions.powi(T4, 4);

        DoubleMatrix T5 = T4.dup();
        T5.muli(F_T);

        DoubleMatrix g = y4.dup();
        g.divi(T4);
        g.muli(-1);
        MatrixFunctions.expi(g);

        F_Tp.muli(F_factor);

        DoubleMatrix g1 = g.dup();
        g1.muli(y3);
        g1.muli(-4);
        g1.divi(T4);

        DoubleMatrix g2 = g.dup();
        g2.muli(y4);
        g2.muli(F_Tp);
        g2.muli(4);
        g2.divi(T5);


        DoubleMatrix F_theta = g.dup();
        F_theta.muli(-1);
        F_theta.addi(1);
        F_theta.muli(F_y);


        DoubleMatrix F_theta1 = g1.dup();
        F_theta1.muli(F_y);
        F_theta1.addi(g);
        F_theta1.muli(-1);
        F_theta1.addi(1);


        DoubleMatrix F_theta2 = g2.dup();
        F_theta2.muli(F_y);
        F_theta2.muli(-1);

        theta.setF_theta(F_theta);
        theta.setF_theta1(F_theta1);
        theta.setF_theta2(F_theta2);


        return theta;

    }

}
