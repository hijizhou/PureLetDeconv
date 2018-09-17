package com.hijizhou.cores.deconvolution;
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
import com.cern.colt.matrix.AbstractMatrix3D;
import com.cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import com.cern.colt.matrix.tdcomplex.DComplexMatrix3D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix1D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix3D;
import com.cern.colt.matrix.tdouble.DoubleMatrix3D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import com.cern.jet.math.tdcomplex.DComplexFunctions;
import com.cern.jet.math.tdouble.DoubleFunctions;
import com.hijizhou.utilities.FFT_WFilter;
import com.hijizhou.utilities.PSFUtil;

import java.util.concurrent.Callable;

public class LETProcess3D implements Callable<LETProcess3D> {

    private int width;
    private int height;
    private int slice;
    private final int jIndex;
    private final int oIndex;
    private FFT_WFilter wf;
    private DComplexMatrix3D Yi;
    private double alpha;
    private DComplexMatrix3D Hli;
    private DComplexMatrix3D Hbit;
    private AbstractMatrix3D auxInput;
    private ReValue3D returnvalue = new ReValue3D();

    public static class ReValue3D {
        private DComplexMatrix1D[] matrixCFele = new DComplexMatrix1D[2];
        private double[] div = new double[2];

        public ReValue3D() {

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

    public LETProcess3D(double alpha, int jIndex, int oIndex, FFT_WFilter wf,
                        AbstractMatrix3D auxInput, DComplexMatrix3D Yi,
                        DComplexMatrix3D Hli, DComplexMatrix3D Hbit) {
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
        this.slice = wf.getDz().length / 2;
    }

    public ReValue3D getRV() {
        return this.returnvalue;
    }

    @Override
    public LETProcess3D call() throws Exception{


        DoubleMatrix3D tp = new DenseDoubleMatrix3D(slice, width, height);

        wf.fft_wfilters3D(width, height, slice, oIndex, jIndex + 1);

        DComplexMatrix3D decEle = PSFUtil.getDecMatrix3D(wf);

        // Di = D.*Hli;
        DComplexMatrix3D decEleH = decEle.copy();
        decEleH.assign(Hli, DComplexFunctions.mult);

        DComplexMatrix3D decEleConj = decEle.copy();
        decEleConj.assign(DComplexFunctions.conj);
        DComplexMatrix3D recEle = decEleConj.copy();
        recEle.assign(DComplexFunctions.div(Math.pow(8, jIndex + 1)));

        AbstractMatrix3D di = decEleH.copy();

        ((DenseDComplexMatrix3D) di).ifft3(true);

        DoubleMatrix3D diReal = ((DenseDComplexMatrix3D) di).getRealPart();
        AbstractMatrix3D di2 = diReal.copy();
        ((DenseDoubleMatrix3D) di2).assign(DoubleFunctions.square);
        DComplexMatrix3D Di2 = ((DenseDoubleMatrix3D) di2).getFft3();

        DComplexMatrix3D aux_w = decEle.copy();
        aux_w.assign(Yi, DComplexFunctions.mult);
        ((DenseDComplexMatrix3D) aux_w).ifft3(true);
        DoubleMatrix3D w = aux_w.getRealPart();

//        ImageUtil.previewSave(w);

        DComplexMatrix3D aux_v = Di2.copy();
        aux_v.assign((DComplexMatrix3D) auxInput, DComplexFunctions.mult);
        ((DenseDComplexMatrix3D) aux_v).ifft3(true);
        DoubleMatrix3D v = aux_v.getRealPart();
        v.assign(DoubleFunctions.mult(alpha));

        double eps = 1e-9;
        double beta = 1e2;
        DoubleMatrix3D betav = v.copy();
        betav.assign(DoubleFunctions.mult(beta));

        for (int ssi = 0; ssi < slice; ssi++) {
            for (int hhi = 0; hhi < height; hhi++) {
                for (int wwi = 0; wwi < width; wwi++) {
                    double aux = betav.getQuick(ssi, hhi, wwi);
                    aux = Math.tanh(aux);
                    betav.setQuick(ssi, hhi, wwi, aux);
                }
            }
        }

        DoubleMatrix3D s = betav.copy();

        betav.assign(v, DoubleFunctions.mult);
        betav.assign(DoubleFunctions.plus(eps));
        betav.assign(DoubleFunctions.sqrt);
        DoubleMatrix3D t = betav.copy();

        betav = s.copy();
        betav.assign(DoubleFunctions.square);
        betav.assign(DoubleFunctions.mult(-1));
        betav.assign(DoubleFunctions.plus(1));
        betav.assign(DoubleFunctions.mult(beta));
        betav.assign(v, DoubleFunctions.mult);
        betav.assign(s, DoubleFunctions.plus);

        tp.assign(0.5);
        tp.assign(t, DoubleFunctions.div);
        tp.assign(betav, DoubleFunctions.mult);

        // divergence term
        DComplexMatrix3D auxrt = recEle.copy();
        auxrt.assign(Hbit, DComplexFunctions.mult);
        auxrt.assign(DComplexFunctions.conj);
        ((DenseDComplexMatrix3D) auxrt).ifft3(true);
        DoubleMatrix3D rt = auxrt.getRealPart();

        DoubleMatrix3D auxDiRt = rt.copy();
        auxDiRt.assign(diReal, DoubleFunctions.mult);
        DComplexMatrix3D DiRt = ((DenseDoubleMatrix3D) auxDiRt).getFft3();
        auxDiRt = rt.copy();
        auxDiRt.assign((DenseDoubleMatrix3D) di2, DoubleFunctions.mult);
        DComplexMatrix3D Di2Rt = ((DenseDoubleMatrix3D) auxDiRt).getFft3();

        DComplexMatrix3D auxW = DiRt.copy();
        auxW.assign((DenseDComplexMatrix3D) auxInput, DComplexFunctions.mult);
        ((DenseDComplexMatrix3D) auxW).ifft3(true);
        DoubleMatrix3D w1 = auxW.getRealPart();
        auxW = Di2Rt.copy();
        auxW.assign((DenseDComplexMatrix3D) auxInput, DComplexFunctions.mult);
        ((DenseDComplexMatrix3D) auxW).ifft3(true);
        DoubleMatrix3D w2 = auxW.getRealPart();

        // whether to threshold
        DoubleMatrix3D ww = w.copy();
        ww.assign(DoubleFunctions.square);
        double mean1 = ww.zSum()
                / (width * height*slice);
        ww = t.copy();
        ww.assign(DoubleFunctions.square);
        double mean2 = 2 * ww.zSum()
                / (width * height*slice);

        if (mean1 <= mean2) {
            return this;
        }

        double startTime = System.nanoTime();

//        ImageUtil.preview(w2);
//        ImageUtil.preview(w);
        // begin thresholding
        ReValue3D rv = itemThreshold(w1, w2, w, t, tp, recEle, width, height, slice,
                alpha);
        this.returnvalue = rv;


        double endTime = System.nanoTime();
        System.out.println("inner time: " + (endTime - startTime) / 1.0E9D);

        return this;
    }

    public static class Theta3D {
        private DoubleMatrix3D F_theta;
        private DoubleMatrix3D F_theta1;
        private DoubleMatrix3D F_theta2;

        private DComplexMatrix1D matrixCFele;
        private double div;

        public Theta3D(int width, int height, int slice) {
            this.F_theta = new DenseDoubleMatrix3D(slice, width, height);
            this.F_theta1 = new DenseDoubleMatrix3D(slice, width, height);
            this.F_theta2 = new DenseDoubleMatrix3D(slice, width, height);
            this.matrixCFele = new DenseDComplexMatrix1D(width * height * slice);
        }

        public DoubleMatrix3D getF_theta() {
            return F_theta;
        }

        public void setF_theta(DoubleMatrix3D f_theta) {
            F_theta = f_theta;
        }

        public DoubleMatrix3D getF_theta1() {
            return F_theta1;
        }

        public void setF_theta1(DoubleMatrix3D f_theta1) {
            F_theta1 = f_theta1;
        }

        public DoubleMatrix3D getF_theta2() {
            return F_theta2;
        }

        public void setF_theta2(DoubleMatrix3D f_theta2) {
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

    public static ReValue3D itemThreshold(DoubleMatrix3D w1, DoubleMatrix3D w2,
                                          DoubleMatrix3D w, DoubleMatrix3D t, DoubleMatrix3D tp,
                                          DComplexMatrix3D recEle, int width, int height, int slice, double alpha) {

        ReValue3D rv = new ReValue3D();

        DoubleMatrix3D w1_tmp = w1.copy();
        DoubleMatrix3D w2_tmp = w2.copy();
        DoubleMatrix3D w_tmp = w.copy();
        DoubleMatrix3D t_tmp = t.copy();
        DoubleMatrix3D tp_tmp = tp.copy();

        Theta3D theta = new Theta3D(width, height, slice);

        // factor=4;
        theta = aux_threshold(alpha, 4.0D, recEle, w_tmp, w1_tmp, w2_tmp,
                t_tmp, tp_tmp, width, height, slice, theta);

        rv.setDiv1(theta.getDiv());
        rv.setF1(theta.getMatrixCFele());

        w1_tmp = w1.copy();
        w2_tmp = w2.copy();
        w_tmp = w.copy();
        t_tmp = t.copy();
        tp_tmp = tp.copy();

        // factor=9;
        theta = aux_threshold(alpha, 9.0D, recEle, w_tmp, w1_tmp, w2_tmp,
                t_tmp, tp_tmp, width, height, slice,  theta);

        rv.setDiv2(theta.getDiv());
        rv.setF2(theta.getMatrixCFele());

        return rv;
    }

    public static Theta3D aux_threshold(double alpha, double factor,
                                        DComplexMatrix3D recEle, DoubleMatrix3D w, DoubleMatrix3D w1_tmp,
                                        DoubleMatrix3D w2_tmp, DoubleMatrix3D t, DoubleMatrix3D tp,
                                        int width, int height, int slice, Theta3D theta) {

        double startFor = System.nanoTime();

        DComplexMatrix1D matrixCF = new DenseDComplexMatrix1D(width * height*slice);
        theta = aux_hard4(w, factor, t, tp, theta);

        DComplexMatrix3D F_thetaFFT = new DenseDComplexMatrix3D(slice, width, height);
        F_thetaFFT.assignReal(theta.getF_theta());
        ((DenseDComplexMatrix3D) F_thetaFFT).fft3();
        F_thetaFFT.assign(recEle, DComplexFunctions.mult);

        //=============================================================
//        ImageUtil.preview(F_thetaFFT);
        //=============================================================

        DComplexMatrix1D aux_1Dmat = F_thetaFFT.vectorize();

        matrixCF.assign(aux_1Dmat);

        w1_tmp.assign(theta.getF_theta1(), DoubleFunctions.mult);
        w2_tmp.assign(theta.getF_theta2(), DoubleFunctions.mult);
        w1_tmp.assign(w2_tmp, DoubleFunctions.plus);
        w1_tmp.assign(DoubleFunctions.mult(alpha));

        double auxDiv = w1_tmp.zSum();

        theta.setDiv(auxDiv);

        theta.setMatrixCFele(matrixCF);

        double endFor = System.nanoTime();
        System.out.println("------ aux_threshold time: "
                + (endFor - startFor) / 1.0E9D);

        return theta;
    }

    public static Theta3D aux_hard4(DoubleMatrix3D F_y, double F_factor,
                                    DoubleMatrix3D F_T, DoubleMatrix3D F_Tp, Theta3D theta) {
//		double startFor = System.nanoTime();

        F_T.assign(DoubleFunctions.mult(F_factor));
        DoubleMatrix3D y4 = F_y.copy();
        y4.assign(DoubleFunctions.pow(4));
        DoubleMatrix3D T4 = F_T.copy();
        T4.assign(DoubleFunctions.pow(4));
        DoubleMatrix3D g = y4.copy();
        g.assign(T4, DoubleFunctions.div);
        g.assign(DoubleFunctions.mult(-1));
        g.assign(DoubleFunctions.exp);

//		double endFor = System.nanoTime();
//		System.out.println("------ aux_hard4 time: "
//				+ (endFor - startFor) / 1.0E9D);

        F_Tp.assign(DoubleFunctions.mult(F_factor));
        DoubleMatrix3D g1 = g.copy();
        DoubleMatrix3D y3 = F_y.copy();
        y3.assign(DoubleFunctions.pow(3));
        DoubleMatrix3D T5 = T4.copy();
        T5.assign(F_T, DoubleFunctions.mult);
        g1.assign(y3, DoubleFunctions.mult);
        g1.assign(DoubleFunctions.mult(-4));
        g1.assign(T4, DoubleFunctions.div);

        DoubleMatrix3D g2 = g.copy();
        g2.assign(y4, DoubleFunctions.mult);
        g2.assign(F_Tp, DoubleFunctions.mult);
        g2.assign(DoubleFunctions.mult(4));
        g2.assign(T5, DoubleFunctions.div);

        DoubleMatrix3D g12 = g.copy();
        g12.assign(DoubleFunctions.mult(4));
        DoubleMatrix3D yg1 = g1.copy();
        yg1.assign(F_y, DoubleFunctions.mult);

        DoubleMatrix3D F_theta = g.copy();
        F_theta.assign(DoubleFunctions.mult(-1));
        F_theta.assign(DoubleFunctions.plus(1));
        F_theta.assign(F_y, DoubleFunctions.mult);

        DoubleMatrix3D F_theta1 = g1.copy();
        F_theta1.assign(F_y, DoubleFunctions.mult);
        F_theta1.assign(g, DoubleFunctions.plus);
        F_theta1.assign(DoubleFunctions.mult(-1));
        F_theta1.assign(DoubleFunctions.plus(1));

        DoubleMatrix3D F_theta2 = g2.copy();
        F_theta2.assign(F_y, DoubleFunctions.mult);
        F_theta2.assign(DoubleFunctions.mult(-1));

        theta.setF_theta(F_theta);
        theta.setF_theta1(F_theta1);
        theta.setF_theta2(F_theta2);


        return theta;

    }

}
