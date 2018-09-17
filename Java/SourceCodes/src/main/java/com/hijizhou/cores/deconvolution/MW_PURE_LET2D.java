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
import com.cern.colt.matrix.tdcomplex.algo.DenseDComplexAlgebra;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix1D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix2D;
import com.cern.colt.matrix.tdouble.DoubleMatrix1D;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import com.cern.colt.matrix.tdouble.algo.solver.DoubleCG;
import com.cern.colt.matrix.tdouble.algo.solver.IterativeSolverDoubleNotConvergedException;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.cern.jet.math.tdcomplex.DComplexFunctions;
import com.cern.jet.math.tdouble.DoubleFunctions;
import com.edu.emory.mathcs.utils.ConcurrencyUtils;
import com.hijizhou.cores.deconvolution.LETProcess2D.ReValue;
import com.hijizhou.utilities.FFT_WFilter;
import com.hijizhou.utilities.PSFUtil;
import com.hijizhou.utilities.WalkBar;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon2D;
import ij.ImagePlus;
import ij.process.FloatProcessor;

import java.awt.image.ColorModel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class MW_PURE_LET2D {
    private int seType = 0; // 1-MSELET,0-PURELET
    private int numWiener;
    private double alpha; // Poisson noise
    private double sigma; // Gaussian noise
    private double beta;
    private int level = 4; // level number of wavelet decomposition
    private static volatile int numSubbands = 0;
    private double[] lambda;
    private DoubleMatrix2D imOutput;
    private DComplexMatrix2D fftInput;
    private DComplexMatrix2D fftPSF;
    private DComplexMatrix2D fftOriginal = null;
    private int width;
    private int height;
    private ColorModel cmY;
    private WalkBar walk;

    public MW_PURE_LET2D(DoubleMatrix2D imInput, DoubleMatrix2D imPSF, double[] noiseparameters, WalkBar walk) {
        this.walk = walk;
        this.seType = 0;
        this.alpha = noiseparameters[0];
        this.sigma = noiseparameters[1];

        this.height = imInput.rows();
        this.width = imInput.columns();

        this.fftInput = ((DenseDoubleMatrix2D) imInput)
                .getFft2();
        int[] psfCenter = PSFUtil.getCenter((DenseDoubleMatrix2D) imPSF);
        AbstractMatrix2D auxPSF = DoubleCommon2D.circShift(imPSF, psfCenter);
        this.fftPSF = ((DenseDoubleMatrix2D) auxPSF).getFft2();

        //Parameters
        double[] lambda = new double[]{1e-4, 1e-3, 1e-2};

        this.numWiener = lambda.length;
        this.numSubbands = this.numWiener * 2 * this.level * 3;
        this.lambda = new double[this.numWiener];
        double Ey = imInput.zSum() / (width * height);
        for (int i = 0; i < lambda.length; i++) {
            this.lambda[i] = lambda[i] * this.alpha * Ey;
        }
        this.beta = 1e-5 * this.alpha * Ey;

        walk.setMessage("Preparing...");

        ConcurrencyUtils.setNumberOfThreads(ConcurrencyUtils.getNumberOfThreads());

    }

    public MW_PURE_LET2D(DoubleMatrix2D imOriginal, DoubleMatrix2D imInput, DoubleMatrix2D imPSF, double[] noiseparameters) {
        this.seType = 1; //MSE-LET
        this.alpha = noiseparameters[0];
        this.sigma = noiseparameters[1];

        this.height = imInput.rows();
        this.width = imInput.columns();

        this.fftInput = ((DenseDoubleMatrix2D) imInput)
                .getFft2();

        int[] psfCenter = PSFUtil.getCenter((DenseDoubleMatrix2D) imPSF);
        AbstractMatrix2D auxPSF = DoubleCommon2D.circShift(imPSF, psfCenter);
        this.fftPSF = ((DenseDoubleMatrix2D) auxPSF).getFft2();
        this.fftOriginal = ((DenseDoubleMatrix2D) imOriginal).getFft2();

        //Parameters
        double[] lambda = new double[]{1e-4, 1e-3, 1e-2};

        this.numWiener = lambda.length;
        MW_PURE_LET2D.numSubbands = this.numWiener * 2 * this.level * 3;
        this.lambda = new double[this.numWiener];

        double Ey = imInput.zSum() / (width * height);
        for (int i = 0; i < lambda.length; i++) {
            this.lambda[i] = lambda[i] * this.alpha * Ey;
        }
        this.beta = 1e-5 * this.alpha * Ey;

    }

    private DoubleMatrix2D getS2() {
        DoubleMatrix2D S = PSFUtil.getRegularizer(width, height);
//        DoubleMatrix2D S = new DenseDoubleMatrix2D(width, height);
//        S.assign(DoubleFunctions.plus(1));
        DoubleMatrix2D S2 = S.copy();
        S2.assign(DoubleFunctions.square);
        return S2;
    }

    private DComplexMatrix2D getH2() {
        DComplexMatrix2D H2 = ((DenseDComplexMatrix2D) fftPSF).copy();
        (H2).assign(DComplexFunctions.conj);
        (H2).assign((DComplexMatrix2D) fftPSF,
                DComplexFunctions.mult);
        return H2;
    }

    private DComplexMatrix2D getHbi(DoubleMatrix2D S2, DComplexMatrix2D H2){
        DComplexMatrix2D auxHbi = new DenseDComplexMatrix2D(width, height);
        auxHbi.assignReal(S2);
        auxHbi.assign(DComplexFunctions.mult(beta));
        auxHbi.assign(H2, DComplexFunctions.plus);
        DComplexMatrix2D Hbi = ((DComplexMatrix2D) fftPSF).copy();
        Hbi.assign(DComplexFunctions.conj);
        Hbi.assign(auxHbi, DComplexFunctions.div);

        return Hbi;
    }

    private DComplexMatrix2D getHli(DoubleMatrix2D S2, DComplexMatrix2D H2, double regParameter){
        DComplexMatrix2D auxHli = new DenseDComplexMatrix2D(width, height);
        auxHli.assignReal(S2);
        auxHli.assign(DComplexFunctions.mult(regParameter));
        auxHli.assign(H2, DComplexFunctions.plus);
        DComplexMatrix2D Hli = fftPSF.copy();
        Hli.assign(DComplexFunctions.conj);
        Hli.assign(auxHli, DComplexFunctions.div);
        return Hli;
    }

    public boolean doDeconvolution() {


        System.out.println("---- begin deconvolution ----");
        double startTime = System.nanoTime(); // start timing

        DoubleMatrix2D S2 = this.getS2();
        DComplexMatrix2D H2 = this.getH2();

        DComplexMatrix2D Ht = fftPSF.copy();
        Ht.assign(DComplexFunctions.conj);

        DComplexMatrix2D Hbi = this.getHbi(S2, H2);
        DComplexMatrix2D Hbit = Hbi.copy();
        Hbit.assign(DComplexFunctions.conj);

        // Construct fftwfilter
        FFT_WFilter wf = new FFT_WFilter();
        wf.fft_wfilters2D(width, height, 3, this.level);
        DComplexMatrix2D decLow = PSFUtil.getDecMatrix2D(wf);
        DComplexMatrix2D recLow = PSFUtil.getRecMatrix2D(decLow, this.level);

        DComplexMatrix2D lowBand = new DenseDComplexMatrix2D(width, height);
        DComplexMatrix2D aux_Band = new DenseDComplexMatrix2D(width, height);
        DComplexMatrix2D Yi = new DenseDComplexMatrix2D(width, height);

        ExecutorService pool = Executors.newFixedThreadPool(ConcurrencyUtils.getNumberOfThreads());

        Set<Future<LETProcess2D>> set = new HashSet<Future<LETProcess2D>>();

        for (int wi = 0; wi < this.numWiener; wi++) {

            DComplexMatrix2D Hli = this.getHli(S2, H2, lambda[wi]);

            Yi = Hli.copy();
            Yi.assign(fftInput, DComplexFunctions.mult);

            aux_Band = Yi.copy();

            aux_Band.assign(decLow, DComplexFunctions.mult);
            aux_Band.assign(recLow, DComplexFunctions.mult);
            aux_Band.assign(DComplexFunctions.mult(1.0 / this.numWiener));
            aux_Band.assign(lowBand, DComplexFunctions.plus);

            lowBand = aux_Band.copy();

            for (int ji = 0; ji < this.level; ji++) {
                for (int oi = 0; oi < 3; oi++) {
                    Callable<LETProcess2D> callable = new LETProcess2D(alpha, ji,
                            oi, wf, fftInput, Yi, Hli, Hbit);
                    Future<LETProcess2D> future = pool.submit(callable);
                    set.add(future);

                    walk.setMessage("Sub-processing: " + wi);
//                    System.out.println(ji + " " + oi);
                }
            }
        }



        ////// clear variables //////
        S2 = null;
        H2 = null;
        Ht = null;
        Hbit = null;
        Yi = null;
        wf = null;
        decLow = null;
        recLow = null;
        aux_Band = null;
        System.gc();
        ////////////////////////////
        double runningTime = (System.nanoTime() - startTime) / 1.0E9D;
        System.out.println("Part 0 running time: " + runningTime + " s");

        startTime = System.nanoTime(); // start timing
        DComplexMatrix1D[] matrixCFele = new DenseDComplexMatrix1D[2];
        double[] Div = new double[2];
        DoubleMatrix1D div = new DenseDoubleMatrix1D(MW_PURE_LET2D.numSubbands);

        int kf = 0;
        ReValue[] rvList = new ReValue[set.size()];
        ReValue rv = new ReValue();
        try {
            LETProcess2D lp;
            for (Future<LETProcess2D> future : set) {
                lp = future.get();
                rv = lp.getRV();
                rvList[kf++] = rv;
                double endTime = System.nanoTime();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        pool.shutdown();


        for (int rvi = 0; rvi < kf; rvi++) {
            rv = rvList[rvi];
            if (rv.getDiv()[0] == 0) {
                this.numSubbands -= 2;
                continue;
            }
        }

        DComplexMatrix2D matrixCF = new DenseDComplexMatrix2D(width * height,
                this.numSubbands);
        int kfv = 0;
        for (int rvi = 0; rvi < kf; rvi++) {
            rv = rvList[rvi];
            if (rv.getDiv()[0] == 0) {
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

        walk.setMessage("Sub-processing finished");


        runningTime = (System.nanoTime() - startTime) / 1.0E9D;
        System.out.println("Part 1 running time: " + runningTime + " s");

        startTime = System.nanoTime(); // start timing


        //the most time-costing part
        DComplexMatrix2D matrixFTrans = matrixCF.getConjugateTranspose();

        DComplexMatrix2D matrixM = new DenseDComplexMatrix2D(MW_PURE_LET2D.numSubbands,
                MW_PURE_LET2D.numSubbands);

        DComplexMatrix2D pemFtrans = matrixFTrans.copy();

        double startTime1 = System.nanoTime();
        matrixFTrans.zMult(matrixCF, matrixM);
        double runningTime1 = (System.nanoTime() - startTime1) / 1.0E9D;
        System.out.println("-- multiplication time: " + runningTime1 + " s");


        DoubleMatrix2D matrixA = matrixM.getRealPart();
        matrixA.assign(DoubleFunctions.div(Math.pow(width * height, 2)));

        runningTime = (System.nanoTime() - startTime) / 1.0E9D;
        System.out.println("Part 2 running time: " + runningTime + " s");

        DoubleMatrix1D matrixC = new DenseDoubleMatrix1D(width * height);

        startTime = System.nanoTime(); // start timing
        if (this.seType == 1) {
            // MSE-LET
            DComplexMatrix2D matricCC = fftOriginal.copy();
            matricCC.assign(lowBand, DComplexFunctions.minus);

            DComplexMatrix1D matrixMin = matricCC.vectorize();

            DComplexMatrix2D matrixEqC = new DenseDComplexMatrix2D(width
                    * height, 1);
            matrixEqC.assign(matrixMin.toArray());

            DComplexMatrix2D auxC = new DenseDComplexMatrix2D(MW_PURE_LET2D.numSubbands,
                    1);
            ((DComplexMatrix2D) pemFtrans).zMult(matrixEqC,
                    auxC, new double[]{1, 0},
                    new double[]{0, 0}, false, false);
            matrixC = auxC.getRealPart().vectorize();
            matrixC.assign(DoubleFunctions.div(Math.pow(width * height, 2)));
        } else {
            // PURE-LET
            DComplexMatrix2D Ybi = fftInput.copy();
            Ybi.assign(Hbi, DComplexFunctions.mult);
            Ybi.assign(lowBand, DComplexFunctions.minus);

            DComplexMatrix1D matrixMin = Ybi.vectorize();

            DComplexMatrix2D matrixEqC = new DenseDComplexMatrix2D(width
                    * height, 1);
            matrixEqC.assign(matrixMin.toArray());

            DComplexMatrix2D auxC = new DenseDComplexMatrix2D(this.numSubbands,
                    1);
            ((DComplexMatrix2D) pemFtrans).zMult(matrixEqC,
                    auxC, new double[]{1, 0},
                    new double[]{0, 0}, false, false);
            matrixC = auxC.getRealPart().vectorize();
            matrixC.assign(DoubleFunctions.div(Math.pow(width * height, 2)));
            DoubleMatrix1D divC = new DenseDoubleMatrix1D(MW_PURE_LET2D.numSubbands);
            divC.viewPart(0, MW_PURE_LET2D.numSubbands).assign(
                    div.viewPart(0, MW_PURE_LET2D.numSubbands));
            matrixC.assign(divC.assign(DoubleFunctions.div(width * height)),
                    DoubleFunctions.minus);

            for (int wi = 0; wi < MW_PURE_LET2D.numSubbands; wi++) {
                double mV = matrixA.getQuick(wi, wi);
                matrixA.setQuick(wi, wi, mV + 0.05);
            }

        }
        matrixC.assign(DoubleFunctions.max(0));

        // solving a linear system of equations
        DenseDoubleMatrix1D template = new DenseDoubleMatrix1D(MW_PURE_LET2D.numSubbands);
        double[] arTemplate = new double[MW_PURE_LET2D.numSubbands];
        Arrays.fill(arTemplate, 0D);
        template.assign(arTemplate);

        DoubleCG dc = new DoubleCG(template);
        DenseDoubleMatrix1D coeff = new DenseDoubleMatrix1D(MW_PURE_LET2D.numSubbands);
        try {
            dc.solve(matrixA, matrixC, coeff);
        } catch (IterativeSolverDoubleNotConvergedException e) {
            e.printStackTrace();
        }

        DComplexMatrix2D coeff2D = new DenseDComplexMatrix2D(MW_PURE_LET2D.numSubbands,
                1);
        (coeff2D).viewColumn(0).assignReal(coeff);

        DComplexMatrix2D Fa = new DenseDComplexMatrix2D(width * height, 1);

        Fa = matrixCF.zMult(coeff2D,
                Fa, new double[]{1, 1}, new double[]{0,
                        0}, false, false);

        DComplexMatrix2D matrixLow2D = new DenseDComplexMatrix2D(
                width * height, 1);
        DComplexMatrix1D lowBand1D = lowBand.vectorize();

        matrixLow2D.viewColumn(0).assign(lowBand1D);

        Fa.assign(matrixLow2D, DComplexFunctions.plus);

        Fa = (Fa.vectorize()).reshape(width, height);
        ((DenseDComplexMatrix2D) Fa).ifft2(true);

        DoubleMatrix2D recImg = Fa.getRealPart();
        recImg.assign(DoubleFunctions.abs);

        runningTime = (System.nanoTime() - startTime) / 1.0E9D;
        System.out.println("Part 3 running time: " + runningTime + " s");

        System.out.println("Deconvolution finished");

        this.imOutput = recImg;

        walk.setMessage("Deconvolution finished");

        return true;
    }

    public DoubleMatrix2D getOutputMatrix() {
        return this.imOutput;
    }

    public ImagePlus getOutputPlus() {
        FloatProcessor ip = new FloatProcessor(height, width);
        DoubleMatrix2D recImg = this.imOutput;
        DoubleCommon2D.assignPixelsToProcessor(ip,
                recImg, this.cmY);
        ImagePlus imOutput = new ImagePlus("Deconvolved Image", ip);
        return imOutput;
    }
}
