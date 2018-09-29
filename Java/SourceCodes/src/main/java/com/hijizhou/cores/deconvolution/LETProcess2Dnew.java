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
import com.cern.colt.matrix.tdouble.DoubleMatrix1D;
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

public class LETProcess2Dnew {

    private int width;
    private int height;
    private final int jIndex;
    private final int oIndex;
    private FFT_WFilter wf;
    private ComplexDoubleMatrix Yi;
    private double alpha;
    private ComplexDoubleMatrix Hli;
    private ComplexDoubleMatrix Hbit;
    private ComplexDoubleMatrix auxInput;
    private ReValue returnvalue = new ReValue();

    public static class ReValue {
        private DComplexMatrix1D[] matrixCFele = new DComplexMatrix1D[2];
        private double[] div = new double[2];

        public ReValue() {

        }

        public void setValues(DComplexMatrix1D matrixCFele1,
                              DComplexMatrix1D matrixCFele2, double div1, double div2) {
            this.matrixCFele[0] = matrixCFele1;
            this.matrixCFele[1] = matrixCFele2;
            this.div[0] = div1;
            this.div[1] = div2;
        }

        public void setF1(DComplexMatrix1D matrixCFele) {
            this.matrixCFele[0] = matrixCFele;
        }

        public void setF2(DComplexMatrix1D matrixCFele) {
            this.matrixCFele[1] = matrixCFele;
        }

        public void setDiv1(double div) {
            this.div[0] = div;
        }

        public void setDiv2(double div) {
            this.div[1] = div;
        }

        public DComplexMatrix1D[] getF() {
            return this.matrixCFele;
        }

        public double[] getDiv() {
            return this.div;
        }
    }

    public LETProcess2Dnew(double alpha, int jIndex, int oIndex, FFT_WFilter wf,
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

    public LETProcess2Dnew run() {
        /////
        wf.fft_wfilters2D(width, height, oIndex, jIndex + 1);

        ComplexDoubleMatrix decEle = PSFUtil.getDecMatrix2DNew(wf);
        ComplexDoubleMatrix decEleH = decEle.dup();
        decEleH.muli(Hli);

        ComplexDoubleMatrix decEleConj = decEle.dup();
        decEleConj.conji();
        ComplexDoubleMatrix recEle = decEleConj.dup();
        recEle.divi(Math.pow(4, jIndex + 1));

        ComplexDoubleMatrix di = decEleH.dup();
//        SpectralUtils.invfft2D_inplace(di);

        DComplexMatrix2D diColt = ImageUtil.blas2coltComplexMatrix(di);
        ((DenseDComplexMatrix2D) diColt).ifft2(true);
        di = ImageUtil.colt2blasComplexMatrix(diColt);

        DoubleMatrix diReal = di.getReal();


        DoubleMatrix di2 = diReal.dup();

        MatrixFunctions.powi(di2, 2);

        ComplexDoubleMatrix Di2 = SpectralUtils.fft2DNew(di2);

        ComplexDoubleMatrix aux_w = decEle.dup();
        aux_w.muli(Yi);
        SpectralUtils.invfft2D_inplace(aux_w);
        DoubleMatrix w = aux_w.getReal();


        ComplexDoubleMatrix aux_v = Di2.dup();
        aux_v.muli(auxInput);
        SpectralUtils.invfft2D_inplace(aux_v);
        DoubleMatrix v = aux_v.getReal();
        v.muli(this.alpha);

        double eps = 1e-9;
        double beta = 1e2;
        DoubleMatrix betav = v.dup();
        betav.muli(beta);

        for (int hhi = 0; hhi < height; hhi++) {
            for (int wwi = 0; wwi < width; wwi++) {
                double aux = betav.get(hhi, wwi);
                aux = Math.tanh(aux);
                betav.put(hhi, wwi, aux);
            }
        }

        DoubleMatrix s = betav.dup();

        betav.muli(v);
        betav.addi(eps);
        MatrixFunctions.sqrti(betav);

        DoubleMatrix t = betav.dup();

        betav = s.dup();
        MatrixFunctions.powi(betav, 2);
        betav.muli(-1);
        betav.addi(1);
        betav.muli(beta);
        betav.muli(v);
        betav.addi(s);

        DoubleMatrix tp = new DoubleMatrix(width, height);
        tp.fill(0.5);
        tp.divi(t);
        tp.muli(betav);

        // divergence term
        ComplexDoubleMatrix auxrt = recEle.dup();
        auxrt.muli(Hbit);
        auxrt.conji();

        SpectralUtils.invfft2D_inplace(auxrt);
        DoubleMatrix rt = auxrt.getReal();

        DoubleMatrix auxDiRt = rt.dup();
        auxDiRt.muli(diReal);
        ComplexDoubleMatrix DiRt = SpectralUtils.fft2DNew(auxDiRt);

        auxDiRt = rt.dup();
        auxDiRt.muli(di2);
        ComplexDoubleMatrix Di2Rt = SpectralUtils.fft2DNew(auxDiRt);


        ComplexDoubleMatrix auxW = DiRt.dup();
        auxW.muli(auxInput);
        SpectralUtils.invfft2D_inplace(auxW);
        DoubleMatrix w1 = auxW.getReal();

        auxW = Di2Rt.dup();
        auxW.muli(auxInput);
        SpectralUtils.invfft2D_inplace(auxW);
        DoubleMatrix w2 = auxW.getReal();

        // whether to threshold
        DoubleMatrix ww = w.dup();
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

    public static class ThetaNew {
        private DoubleMatrix F_theta;
        private DoubleMatrix F_theta1;
        private DoubleMatrix F_theta2;

        private DComplexMatrix1D matrixCFele;
        private double div;

        public ThetaNew(int width, int height) {
            this.F_theta = new DoubleMatrix(width, height);
            this.F_theta1 = new DoubleMatrix(width, height);
            this.F_theta2 = new DoubleMatrix(width, height);
            this.matrixCFele = new DenseDComplexMatrix1D(width * height);
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

        public DComplexMatrix1D getMatrixCFele() {
            return matrixCFele;
        }

        public void setMatrixCFele(DComplexMatrix1D matrixCFele) {
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

        ThetaNew theta = new ThetaNew(width, height);

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

//	public static Theta aux_threshold(double alpha, double factor,
//                                      DComplexMatrix2D recEle, DoubleMatrix2D w, DoubleMatrix2D w1_tmp,
//                                      DoubleMatrix2D w2_tmp, DoubleMatrix2D t, DoubleMatrix2D tp,
//                                      int width, int height, Theta theta) {
//public static ThetaNew aux_threshold(double alpha, double factor,
//								  DComplexMatrix2D recEle_colt, DoubleMatrix2D w_colt, DoubleMatrix2D w1_tmp_colt,
//								  DoubleMatrix2D w2_tmp_colt, DoubleMatrix2D t_colt, DoubleMatrix2D tp_colt,
//								  int width, int height, ThetaNew theta) {

    public static ThetaNew aux_threshold(double alpha, double factor,
                                         ComplexDoubleMatrix recEle, DoubleMatrix w, DoubleMatrix w1_tmp,
                                         DoubleMatrix w2_tmp, DoubleMatrix t, DoubleMatrix tp,
                                         int width, int height, ThetaNew theta) {
        DoubleMatrix y3 = w.dup();
        MatrixFunctions.powi(y3, 3);

        DoubleMatrix y4 = y3.dup();
        y4.muli(w);

        ComplexDoubleMatrix matrixCF = new ComplexDoubleMatrix(width * height);
        theta = aux_hard4(w, y3, y4, factor, t, tp, theta);

//		double runningTime = (System.nanoTime() - startTime) / 1.0E9D;
//		System.out.println("aux_threshold  = " + runningTime + " s");

        ComplexDoubleMatrix F_thetaFFT = SpectralUtils.fft2DNew(theta.getF_theta());

        F_thetaFFT.muli(recEle);

        matrixCF = F_thetaFFT.reshape(F_thetaFFT.rows * F_thetaFFT.columns, 1);

        w1_tmp.muli(theta.getF_theta1());
        w2_tmp.muli(theta.getF_theta2());
        w1_tmp.addi(w2_tmp);
        w1_tmp.muli(alpha);

        double auxDiv = w1_tmp.sum();

        theta.setDiv(auxDiv);

        DComplexMatrix1D matrixCF_colt = ImageUtil.blas2coltComplexMatrix1D(matrixCF);

        theta.setMatrixCFele(matrixCF_colt);

        return theta;
    }

    public static ThetaNew aux_hard4(DoubleMatrix F_y, DoubleMatrix y3, DoubleMatrix y4, double F_factor,
                                     DoubleMatrix F_T, DoubleMatrix F_Tp, ThetaNew theta) {
//		public static Theta aux_hard4(DoubleMatrix2D F_y, DoubleMatrix2D y3, DoubleMatrix2D y4, double F_factor,
//		DoubleMatrix2D F_T, DoubleMatrix2D F_Tp, Theta theta) {
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
