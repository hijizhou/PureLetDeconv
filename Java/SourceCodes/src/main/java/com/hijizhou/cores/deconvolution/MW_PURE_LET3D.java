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
import com.cern.colt.matrix.AbstractMatrix3D;
import com.cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import com.cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import com.cern.colt.matrix.tdcomplex.DComplexMatrix3D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix1D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix2D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix3D;
import com.cern.colt.matrix.tdouble.DoubleMatrix1D;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.DoubleMatrix3D;
import com.cern.colt.matrix.tdouble.algo.solver.DoubleCG;
import com.cern.colt.matrix.tdouble.algo.solver.IterativeSolverDoubleNotConvergedException;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import com.cern.jet.math.tdcomplex.DComplexFunctions;
import com.cern.jet.math.tdouble.DoubleFunctions;
import com.hijizhou.cores.deconvolution.LETProcess3D.ReValue3D;
import com.hijizhou.utilities.FFT_WFilter;
import com.hijizhou.utilities.PSFUtil;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon3D;
import ij.ImagePlus;
import ij.ImageStack;

import java.awt.image.ColorModel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class MW_PURE_LET3D {
    private int seType = 0; // 1-MSELET,0-PURELET
    private int numWiener;
    private double alpha;
    private double sigma;
    private double beta;
    private int level = 3; // level number of wavelet decomposition
    private static volatile int numSubbands = 0;
    private double[] lambda;
    private DoubleMatrix3D imOutput;
    private AbstractMatrix3D fftInput;
    private AbstractMatrix3D fftPSF;
    private AbstractMatrix3D fftOriginal = null;
    private int width;
    private int height;
    private int slice;
    private ColorModel cmY;

    public MW_PURE_LET3D(DoubleMatrix3D imInput, DoubleMatrix3D imPSF, double[] noiseparameters) {
        this.seType = 0;
        this.alpha = noiseparameters[0];
        this.sigma = noiseparameters[1];

        this.height = imInput.rows();
        this.width = imInput.columns();

        this.slice = imInput.slices();

        this.fftInput = ((DenseDoubleMatrix3D) imInput)
                .getFft3();

        int[] psfCenter = PSFUtil.getCenter((DenseDoubleMatrix3D) imPSF);
        AbstractMatrix3D auxPSF = DoubleCommon3D.circShift(imPSF, psfCenter);
        this.fftPSF = ((DenseDoubleMatrix3D) auxPSF).getFft3();

        //Parameters
        double[] lambda = new double[]{1e-3D};

        this.numWiener = lambda.length;
        MW_PURE_LET3D.numSubbands = this.numWiener * 2 * this.level * 7;
        this.lambda = new double[this.numWiener];

        double Ey = imInput.zSum() / (width * height * slice);
        for (int i = 0; i < lambda.length; i++) {
            this.lambda[i] = lambda[i] * this.alpha * Ey;
        }
        this.beta = 1e-5 * this.alpha * Ey;

    }

    public MW_PURE_LET3D(DoubleMatrix3D imOriginal, DoubleMatrix3D imInput, DoubleMatrix3D imPSF, double[] noiseparameters) {
        this.seType = 1; //MSE-LET
        this.alpha = noiseparameters[0];
        this.sigma = noiseparameters[1];

        this.height = imInput.rows();
        this.width = imInput.columns();
        this.slice = imInput.slices();

        double[] minLocation = imInput.getMinLocation();
        if(minLocation[0]>10) {
            imInput.assign(DoubleFunctions.minus(minLocation[0]));
        }

        this.fftInput = ((DenseDoubleMatrix3D) imInput)
                .getFft3();

        int[] psfCenter = PSFUtil.getCenter((DenseDoubleMatrix3D) imPSF);
        AbstractMatrix3D auxPSF = DoubleCommon3D.circShift(imPSF, psfCenter);
        this.fftPSF = ((DenseDoubleMatrix3D) auxPSF).getFft3();
        this.fftOriginal = ((DenseDoubleMatrix3D) imOriginal).getFft3();


        //Parameters
        double[] lambda = new double[]{1e-3D, 3.2e-3D, 1e-2D};
//        double[] lambda = new double[]{1e-2D};

        this.numWiener = lambda.length;
        MW_PURE_LET3D.numSubbands = this.numWiener * 2 * this.level * 7;
        this.lambda = new double[this.numWiener];

        double Ey = imInput.zSum() / (width * height * slice);
        for (int i = 0; i < lambda.length; i++) {
            this.lambda[i] = lambda[i] * this.alpha * Ey;
        }
        this.beta = 1e-5 * this.alpha * Ey;

    }

    public DoubleMatrix3D getS2() {
        DoubleMatrix3D S = PSFUtil.getRegularizer(width, height,slice);
        DoubleMatrix3D S2 = S.copy();
        S2.assign(DoubleFunctions.square);
        return S2;
    }

    public DComplexMatrix3D getH2() {
        DComplexMatrix3D H2 = ((DenseDComplexMatrix3D) fftPSF).copy();
        (H2).assign(DComplexFunctions.conj);
        (H2).assign((DComplexMatrix3D) fftPSF,
                DComplexFunctions.mult);
        return H2;
    }

    public boolean doDeconvolution() {

        DoubleMatrix3D S2 = this.getS2();
        DComplexMatrix3D H2 = this.getH2();

        DComplexMatrix3D Ht = ((DComplexMatrix3D) fftPSF).copy();
        Ht.assign(DComplexFunctions.conj);
        DComplexMatrix3D auxHbi = new DenseDComplexMatrix3D(slice, width, height);
        auxHbi.assignReal(S2);

        auxHbi.assign(DComplexFunctions.mult(beta));
        auxHbi.assign(H2, DComplexFunctions.plus);
        DComplexMatrix3D Hbi = ((DComplexMatrix3D) fftPSF).copy();
        Hbi.assign(DComplexFunctions.conj);
        Hbi.assign(auxHbi, DComplexFunctions.div);
        DComplexMatrix3D Hbit = Hbi.copy();
        Hbit.assign(DComplexFunctions.conj);

        // Construct fftwfilter
        FFT_WFilter wf = new FFT_WFilter();
        wf.fft_wfilters3D(width, height, slice, 7, this.level);
        DComplexMatrix3D decLow = PSFUtil.getDecMatrix3D(wf);

        System.out.println(decLow.getQuick(30,107,118)[0]);
        DComplexMatrix3D recLow = decLow.copy();
        recLow.assign(DComplexFunctions.conj);
        recLow.assign(DComplexFunctions.mult(1.0 / Math.pow(8, this.level)));

//		startTime = System.nanoTime();
        DComplexMatrix3D lowBand = new DenseDComplexMatrix3D(slice, width, height);

        DComplexMatrix3D aux_Band = new DenseDComplexMatrix3D(slice, width, height);

//		endTime = System.nanoTime();
//		System.out.println("Variable initilization: " + (endTime - startTime)
//				/ 1.0E9D);

        ExecutorService pool = Executors.newFixedThreadPool(4);
        Set<Future<LETProcess3D>> set = new HashSet<Future<LETProcess3D>>();

        for (int wi = 0; wi < this.numWiener; wi++) {
            double regParameter = lambda[wi];

            DComplexMatrix3D auxHli = new DenseDComplexMatrix3D(slice, width, height);
            auxHli.assignReal(S2);
            auxHli.assign(DComplexFunctions.mult(regParameter));
            auxHli.assign(H2, DComplexFunctions.plus);
            DComplexMatrix3D Hli = ((DComplexMatrix3D) fftPSF).copy();
            Hli.assign(DComplexFunctions.conj);
            Hli.assign(auxHli, DComplexFunctions.div);

            DComplexMatrix3D Yi = Hli.copy();

            Yi.assign((DComplexMatrix3D) fftInput, DComplexFunctions.mult);

//            ImageUtil.preview((DComplexMatrix3D)
//                    ((DComplexMatrix3D) Yi).copy());

            aux_Band = Yi.copy();

            aux_Band.assign(decLow, DComplexFunctions.mult);

            aux_Band.assign(recLow, DComplexFunctions.mult);
            aux_Band.assign(DComplexFunctions.mult(1.0 / this.numWiener));
            aux_Band.assign(lowBand, DComplexFunctions.plus);

            lowBand = aux_Band.copy();

//            ImageUtil.preview((DComplexMatrix3D) auxPSF);

            for (int ji = 0; ji < this.level; ji++) {
                for (int oi = 0; oi < 7; oi++) {
                    // D = Dx(:,:,j,o)*Dy(:,:,j,o);
                    Callable<LETProcess3D> callable = new LETProcess3D(alpha, ji,
                            oi, wf, fftInput, Yi, Hli, Hbit);
                    Future<LETProcess3D> future = pool.submit(callable);
                    set.add(future);

                }
            }
        }

        MW_PURE_LET3D.numSubbands = set.size() * 2;

        DComplexMatrix1D[] matrixCFele = new DenseDComplexMatrix1D[2];
        double[] Div = new double[2];
        DComplexMatrix2D matrixCF = new DenseDComplexMatrix2D(width * height*slice,
                MW_PURE_LET3D.numSubbands);
        DoubleMatrix1D div = new DenseDoubleMatrix1D(MW_PURE_LET3D.numSubbands);

        int kf = 0;
        ReValue3D[] rvList = new ReValue3D[MW_PURE_LET3D.numSubbands];
        ReValue3D rv = new ReValue3D();

        try {
            LETProcess3D lp;
            for (Future<LETProcess3D> future : set) {
                double startTime = System.nanoTime();
                lp = future.get();
                rv = lp.getRV();
                rvList[kf++] = rv;
                double endTime = System.nanoTime();
                System.out.println(set.size()+"Thread time: " + (endTime - startTime) / 1.0E9D);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        pool.shutdown();

        int kfv = 0;
        for (int rvi = 0; rvi < kf; rvi++) {
            rv = rvList[rvi];
            if (rv.getDiv()[0] == 0) {
                MW_PURE_LET3D.numSubbands -= 2;
                continue;
            }
            matrixCFele = rv.getF();
            Div = rv.getDiv();
            matrixCF.viewColumn(kfv).assign(matrixCFele[0]);
            div.setQuick(kfv, Div[0]);
            kfv += 1;

            matrixCF.viewColumn(kfv).assign(matrixCFele[1]);
            div.setQuick(kfv, Div[1]);
            kfv += 1;
        }

//        endFor = System.nanoTime();
//        System.out.println("Extract time: " + (endFor - startFor) / 1.0E9D);

        for(int i=0; i< div.size(); i++){
            System.out.println(div.getQuick(i));
        }

        DComplexMatrix2D matrixF = new DenseDComplexMatrix2D(width * height * slice,
                MW_PURE_LET3D.numSubbands);

        matrixF.viewPart(0, 0, width * height * slice,
                MW_PURE_LET3D.numSubbands).assign(
                matrixCF.viewPart(0, 0, width
                        * height * slice, MW_PURE_LET3D.numSubbands));

        AbstractMatrix2D matrixFTrans = matrixF.getConjugateTranspose();

        DComplexMatrix2D matrixM = new DenseDComplexMatrix2D(MW_PURE_LET3D.numSubbands,
                MW_PURE_LET3D.numSubbands);

        AbstractMatrix2D pemFtrans = ((DenseDComplexMatrix2D) matrixFTrans)
                .copy();

//        startFor = System.nanoTime();

        ((DenseDComplexMatrix2D) matrixFTrans).zMult(matrixF, matrixM);

//        endFor = System.nanoTime();
//        System.out.println("Multiplication time: " + (endFor - startFor) / 1.0E9D);

        DoubleMatrix2D matrixA = matrixM.getRealPart();
        matrixA.assign(DoubleFunctions.div(Math.pow(width * height * slice, 2)));
        DoubleMatrix1D matrixC = new DenseDoubleMatrix1D(width * height * slice);

        double startTime = System.nanoTime();

        if (this.seType == 1) {
            // MSE-LET

            DComplexMatrix3D matricCC = ((DenseDComplexMatrix3D) fftOriginal)
                    .copy();
            matricCC.assign(lowBand, DComplexFunctions.minus);

            DComplexMatrix1D matrixMin = matricCC.vectorize();

            DComplexMatrix2D matrixEqC = new DenseDComplexMatrix2D(width
                    * height * slice, 1);
            matrixEqC.assign(matrixMin.toArray());

            DComplexMatrix2D auxC = new DenseDComplexMatrix2D(MW_PURE_LET3D.numSubbands,
                    1);
            ((DComplexMatrix2D) pemFtrans).zMult(matrixEqC,
                    auxC, new double[]{1, 0},
                    new double[]{0, 0}, false, false);
            matrixC = auxC.getRealPart().vectorize();
            matrixC.assign(DoubleFunctions.div(Math.pow(width * height * slice, 2)));
        } else {
            // PURE-LET
            // Ybi = Hbi.*Y;
            // c = real(F'*(Ybi(:)-Yl(:)))/N^2-div/N;
            // c = max(c,0);
            DComplexMatrix3D Ybi = ((DenseDComplexMatrix3D) fftInput).copy();
            Ybi.assign(Hbi, DComplexFunctions.mult);
            Ybi.assign(lowBand, DComplexFunctions.minus);

            DComplexMatrix1D matrixMin = Ybi.vectorize();

            DComplexMatrix2D matrixEqC = new DenseDComplexMatrix2D(width
                    * height * slice, 1);
            matrixEqC.assign(matrixMin.toArray());

            DComplexMatrix2D auxC = new DenseDComplexMatrix2D(MW_PURE_LET3D.numSubbands,
                    1);
            ((DComplexMatrix2D) pemFtrans).zMult(matrixEqC,
                    auxC, new double[]{1, 0},
                    new double[]{0, 0}, false, false);
            matrixC = auxC.getRealPart().vectorize();
            matrixC.assign(DoubleFunctions.div(Math.pow(width * height * slice, 2)));
            DoubleMatrix1D divC = new DenseDoubleMatrix1D(MW_PURE_LET3D.numSubbands);
            divC.viewPart(0, MW_PURE_LET3D.numSubbands).assign(
                    div.viewPart(0, MW_PURE_LET3D.numSubbands));
            matrixC.assign(divC.assign(DoubleFunctions.div(width * height * slice)),
                    DoubleFunctions.minus);

            matrixA.assign(DoubleFunctions.plus(5e-3D));

        }
        matrixC.assign(DoubleFunctions.max(0));

        double endTime = System.nanoTime();
        System.out.println("Equation time: " + (endTime - startTime) / 1.0E9D);

//		double startTime = System.nanoTime();

        // solving a linear system of equations
//        DoubleMatrix1D coeff = (new DenseDoubleAlgebra()).solve(matrixA, matrixC);


        DenseDoubleMatrix1D template = new DenseDoubleMatrix1D(MW_PURE_LET3D.numSubbands);
        double[] arTemplate = new double[MW_PURE_LET3D.numSubbands];
        Arrays.fill(arTemplate, 0D);
        template.assign(arTemplate);

        DoubleCG dc = new DoubleCG(template);
        DenseDoubleMatrix1D coeff = new DenseDoubleMatrix1D(MW_PURE_LET3D.numSubbands);
        try {
            dc.solve(matrixA, matrixC, coeff);
        } catch (IterativeSolverDoubleNotConvergedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//		double endTime = System.nanoTime();
//		System.out.println("Solving time: " + (endTime - startTime) / 1.0E9D);
//
//		startTime = System.nanoTime();

        DComplexMatrix2D coeff2D = new DenseDComplexMatrix2D(MW_PURE_LET3D.numSubbands,
                1);
        coeff2D.viewColumn(0).assignReal(coeff);

        DComplexMatrix2D Fa = new DenseDComplexMatrix2D(width * height * slice, 1);

        Fa = matrixF.zMult(coeff2D,
                Fa, new double[]{1, 1}, new double[]{0,
                        0}, false, false);

        DComplexMatrix2D matrixLow2D = new DenseDComplexMatrix2D(
                width * height * slice, 1);
        DComplexMatrix1D lowBand1D = lowBand.vectorize();

        ((DenseDComplexMatrix2D) matrixLow2D).viewColumn(0).assign(lowBand1D);

        // for (int ttt = 0; ttt < width * height; ttt++) {
        // matrixLow2D.setQuick(ttt, 0, lowBand1D.getQuick(ttt));
        // }

        ((DenseDComplexMatrix2D) Fa)
                .assign(matrixLow2D, DComplexFunctions.plus);

        DComplexMatrix3D Faa = (((DenseDComplexMatrix2D) Fa).vectorize()).reshape(slice, width, height);
        ((DenseDComplexMatrix3D) Faa).ifft3(true);

        DoubleMatrix3D recImg = ((DenseDComplexMatrix3D) Faa).getRealPart();
        recImg.assign(DoubleFunctions.abs);

        this.imOutput = recImg;

        return true;
    }

    public ImagePlus getOutputPlus() {
        // TODO Auto-generated method stub

        DoubleMatrix3D recImg = this.imOutput;
        ImageStack stackOut = new ImageStack(width, height);

        DoubleCommon3D.assignPixelsToStack(stackOut, recImg, cmY);

        ImagePlus imX = new ImagePlus("Deconvolved Image", stackOut);
        return imX;
    }

    public DoubleMatrix3D getOutputMatrix() {
        return this.imOutput;
    }
}
