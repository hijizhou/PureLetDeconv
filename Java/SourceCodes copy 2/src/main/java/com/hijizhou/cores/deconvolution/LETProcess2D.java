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
import com.hijizhou.utilities.PSFUtil;

import java.util.concurrent.Callable;

public class LETProcess2D implements Callable<LETProcess2D> {

	private int width;
	private int height;
	private final int jIndex;
	private final int oIndex;
	private FFT_WFilter wf;
	private DComplexMatrix2D Yi;
	private double alpha;
	private DComplexMatrix2D Hli;
	private DComplexMatrix2D Hbit;
	private AbstractMatrix2D auxInput;
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

	public LETProcess2D(double alpha, int jIndex, int oIndex, FFT_WFilter wf,
                        AbstractMatrix2D auxInput, DComplexMatrix2D Yi,
                        DComplexMatrix2D Hli, DComplexMatrix2D Hbit) {
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
	public LETProcess2D call() throws Exception {


		DoubleMatrix2D tp = new DenseDoubleMatrix2D(width, height);

		wf.fft_wfilters2D(width, height, oIndex, jIndex + 1);

		DComplexMatrix2D decEle = PSFUtil.getDecMatrix2D(wf);


		// Di = D.*Hli;
		DComplexMatrix2D decEleH = decEle.copy();
		decEleH.assign(Hli, DComplexFunctions.mult);
		DComplexMatrix2D decEleConj = decEle.copy();
		decEleConj.assign(DComplexFunctions.conj);
		DComplexMatrix2D recEle = decEleConj.copy();
		recEle.assign(DComplexFunctions.div(Math.pow(4, jIndex + 1)));

		DComplexMatrix2D di = decEleH.copy();
		((DenseDComplexMatrix2D) di).ifft2(true);
		 DoubleMatrix2D diReal = (di).getRealPart();
		 DoubleMatrix2D di2 = diReal.copy();
		(di2).assign(DoubleFunctions.square);
		DComplexMatrix2D Di2 = ((DenseDoubleMatrix2D) di2).getFft2();

		DComplexMatrix2D aux_w = decEle.copy();
		aux_w.assign(Yi, DComplexFunctions.mult);
		((DenseDComplexMatrix2D) aux_w).ifft2(true);
		DoubleMatrix2D w = aux_w.getRealPart();

		DComplexMatrix2D aux_v = Di2.copy();
		aux_v.assign((DComplexMatrix2D) auxInput, DComplexFunctions.mult);
		((DenseDComplexMatrix2D) aux_v).ifft2(true);
		DoubleMatrix2D v = aux_v.getRealPart();
		 v.assign(DoubleFunctions.mult(this.alpha));

		double eps = 1e-9;
		double beta = 1e2;
		DoubleMatrix2D betav = v.copy();
		betav.assign(DoubleFunctions.mult(beta));

		for (int hhi = 0; hhi < height; hhi++) {
			for (int wwi = 0; wwi < width; wwi++) {
				double aux = betav.getQuick(hhi, wwi);
				aux = Math.tanh(aux);
				betav.setQuick(hhi, wwi, aux);
			}
		}

		DoubleMatrix2D s = betav.copy();

		betav.assign(v, DoubleFunctions.mult);
		betav.assign(DoubleFunctions.plus(eps));
		betav.assign(DoubleFunctions.sqrt);
		DoubleMatrix2D t = betav.copy();

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
		DComplexMatrix2D auxrt = recEle.copy();
		auxrt.assign(Hbit, DComplexFunctions.mult);
		auxrt.assign(DComplexFunctions.conj);
		((DenseDComplexMatrix2D) auxrt).ifft2(true);
		DoubleMatrix2D rt = auxrt.getRealPart();

		DoubleMatrix2D auxDiRt = rt.copy();
		auxDiRt.assign(diReal, DoubleFunctions.mult);
		DComplexMatrix2D DiRt = ((DenseDoubleMatrix2D) auxDiRt).getFft2();
		 auxDiRt = rt.copy();
		auxDiRt.assign(di2, DoubleFunctions.mult);
		DComplexMatrix2D Di2Rt = ((DenseDoubleMatrix2D) auxDiRt).getFft2();

		DComplexMatrix2D auxW = DiRt.copy();
		auxW.assign((DenseDComplexMatrix2D) auxInput, DComplexFunctions.mult);
		((DenseDComplexMatrix2D) auxW).ifft2(true);
		DoubleMatrix2D w1 = auxW.getRealPart();
		auxW = Di2Rt.copy();
		auxW.assign((DenseDComplexMatrix2D) auxInput, DComplexFunctions.mult);
		((DenseDComplexMatrix2D) auxW).ifft2(true);
		DoubleMatrix2D w2 = auxW.getRealPart();

		// whether to threshold
		DoubleMatrix2D ww = w.copy();
		ww.assign(DoubleFunctions.square);
		double mean1 = ww.zSum()
				/ (width * height);
		ww = t.copy();
		ww.assign(DoubleFunctions.square);
		double mean2 = 2 * ww.zSum()
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
		private DoubleMatrix2D F_theta;
		private DoubleMatrix2D F_theta1;
		private DoubleMatrix2D F_theta2;

		private DComplexMatrix1D matrixCFele;
		private double div;

		public Theta(int width, int height) {
			this.F_theta = new DenseDoubleMatrix2D(width, height);
			this.F_theta1 = new DenseDoubleMatrix2D(width, height);
			this.F_theta2 = new DenseDoubleMatrix2D(width, height);
			this.matrixCFele = new DenseDComplexMatrix1D(width * height);
		}

		public DoubleMatrix2D getF_theta() {
			return F_theta;
		}

		public void setF_theta(DoubleMatrix2D f_theta) {
			F_theta = f_theta;
		}

		public DoubleMatrix2D getF_theta1() {
			return F_theta1;
		}

		public void setF_theta1(DoubleMatrix2D f_theta1) {
			F_theta1 = f_theta1;
		}

		public DoubleMatrix2D getF_theta2() {
			return F_theta2;
		}

		public void setF_theta2(DoubleMatrix2D f_theta2) {
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

	public static ReValue itemThreshold(DoubleMatrix2D w1, DoubleMatrix2D w2,
                                        DoubleMatrix2D w, DoubleMatrix2D t, DoubleMatrix2D tp,
                                        DComplexMatrix2D recEle, int width, int height, double alpha) {

		ReValue rv = new ReValue();

		DoubleMatrix2D w1_tmp = w1.copy();
		DoubleMatrix2D w2_tmp = w2.copy();
		DoubleMatrix2D w_tmp = w.copy();
		DoubleMatrix2D t_tmp = t.copy();
		DoubleMatrix2D tp_tmp = tp.copy();

		Theta theta = new Theta(width, height);

		// factor=4;
		theta = aux_threshold(alpha, 4.0D, recEle, w_tmp, w1_tmp, w2_tmp,
				t_tmp, tp_tmp, width, height, theta);

		rv.setDiv1(theta.getDiv());
		rv.setF1(theta.getMatrixCFele());

		w1_tmp = w1.copy();
		w2_tmp = w2.copy();
		w_tmp = w.copy();
		t_tmp = t.copy();
		tp_tmp = tp.copy();
		
		// factor=9;
		theta = aux_threshold(alpha, 9.0D, recEle, w_tmp, w1_tmp, w2_tmp,
				t_tmp, tp_tmp, width, height, theta);

		rv.setDiv2(theta.getDiv());
		rv.setF2(theta.getMatrixCFele());

		return rv;
	}

	public static Theta aux_threshold(double alpha, double factor,
                                      DComplexMatrix2D recEle, DoubleMatrix2D w, DoubleMatrix2D w1_tmp,
                                      DoubleMatrix2D w2_tmp, DoubleMatrix2D t, DoubleMatrix2D tp,
                                      int width, int height, Theta theta) {

//		double startTime = System.nanoTime(); // start timing

//		double startFor = System.nanoTime();

		DoubleMatrix2D y3 = w.copy();
		y3.assign(DoubleFunctions.pow(3));
		DoubleMatrix2D y4 = y3.copy();
		y4.assign(w, DoubleFunctions.mult);

		DComplexMatrix1D matrixCF = new DenseDComplexMatrix1D(width * height);
		theta = aux_hard4(w, y3, y4, factor, t, tp, theta);

//		double runningTime = (System.nanoTime() - startTime) / 1.0E9D;
//		System.out.println("aux_threshold  = " + runningTime + " s");

		DComplexMatrix2D F_thetaFFT = new DenseDComplexMatrix2D(width, height);
		F_thetaFFT.assignReal(theta.getF_theta());
		((DenseDComplexMatrix2D) F_thetaFFT).fft2();
		F_thetaFFT.assign(recEle, DComplexFunctions.mult);

		DComplexMatrix1D aux_1Dmat = F_thetaFFT.vectorize();

		matrixCF.assign(aux_1Dmat);

		w1_tmp.assign(theta.getF_theta1(), DoubleFunctions.mult);
		w2_tmp.assign(theta.getF_theta2(), DoubleFunctions.mult);
		w1_tmp.assign(w2_tmp, DoubleFunctions.plus);
		w1_tmp.assign(DoubleFunctions.mult(alpha));

		double auxDiv = w1_tmp.zSum();

		theta.setDiv(auxDiv);;
		theta.setMatrixCFele(matrixCF);

		return theta;
	}

	public static Theta aux_hard4(DoubleMatrix2D F_y, DoubleMatrix2D y3, DoubleMatrix2D y4, double F_factor,
                                  DoubleMatrix2D F_T, DoubleMatrix2D F_Tp, Theta theta) {
//		double startFor = System.nanoTime();

		F_T.assign(DoubleFunctions.mult(F_factor));

		DoubleMatrix2D T4 = F_T.copy();
		T4.assign(DoubleFunctions.pow(4));
		DoubleMatrix2D T5 = T4.copy();
		T5.assign(F_T, DoubleFunctions.mult);

		DoubleMatrix2D g = y4.copy();
		g.assign(T4, DoubleFunctions.div);
		g.assign(DoubleFunctions.mult(-1));
		g.assign(DoubleFunctions.exp);


//		double endFor = System.nanoTime();
//		System.out.println("------ aux_hard4 time: "
//				+ (endFor - startFor) / 1.0E9D);

		F_Tp.assign(DoubleFunctions.mult(F_factor));
		DoubleMatrix2D g1 = g.copy();
		g1.assign(y3, DoubleFunctions.mult);
		g1.assign(DoubleFunctions.mult(-4));
		g1.assign(T4, DoubleFunctions.div);

		DoubleMatrix2D g2 = g.copy();
		g2.assign(y4, DoubleFunctions.mult);
		g2.assign(F_Tp, DoubleFunctions.mult);
		g2.assign(DoubleFunctions.mult(4));
		g2.assign(T5, DoubleFunctions.div);

//		DoubleMatrix2D g12 = g.copy();
//		g12.assign(DoubleFunctions.mult(4));
//		DoubleMatrix2D yg1 = g1.copy();
//		yg1.assign(F_y, DoubleFunctions.mult);

		DoubleMatrix2D F_theta = g.copy();
		F_theta.assign(DoubleFunctions.mult(-1));
		F_theta.assign(DoubleFunctions.plus(1));
		F_theta.assign(F_y, DoubleFunctions.mult);

		DoubleMatrix2D F_theta1 = g1.copy();
		F_theta1.assign(F_y, DoubleFunctions.mult);
		F_theta1.assign(g, DoubleFunctions.plus);
		F_theta1.assign(DoubleFunctions.mult(-1));
		F_theta1.assign(DoubleFunctions.plus(1));

		DoubleMatrix2D F_theta2 = g2.copy();
		F_theta2.assign(F_y, DoubleFunctions.mult);
		F_theta2.assign(DoubleFunctions.mult(-1));
		
		theta.setF_theta(F_theta);
		theta.setF_theta1(F_theta1);
		theta.setF_theta2(F_theta2);

		
		return theta;

	}

}
