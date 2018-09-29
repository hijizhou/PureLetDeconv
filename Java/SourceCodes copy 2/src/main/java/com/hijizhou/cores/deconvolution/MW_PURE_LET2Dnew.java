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
import com.cern.colt.matrix.tdouble.algo.solver.DoubleCG;
import com.cern.colt.matrix.tdouble.algo.solver.IterativeSolverDoubleNotConvergedException;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.cern.jet.math.tdcomplex.DComplexFunctions;
import com.cern.jet.math.tdouble.DoubleFunctions;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon2D;
import com.edu.emory.mathcs.utils.ConcurrencyUtils;
import com.hijizhou.cores.deconvolution.LETProcess2Dnew.ReValue;
import com.hijizhou.utilities.*;
import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import org.jblas.ComplexDouble;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

import java.awt.image.ColorModel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class MW_PURE_LET2Dnew {
    private int seType = 0; // 1-MSELET,0-PURELET
    private int numWiener;
    private double alpha; // Poisson noise
    private double sigma; // Gaussian noise
    private double beta;
    private int level = 4; // level number of wavelet decomposition
    private static volatile int numSubbands = 0;
    private double[] lambda;
    private DoubleMatrix2D imOutput;
    private ComplexDoubleMatrix fftInput;
    private ComplexDoubleMatrix fftPSF;
    private DComplexMatrix2D fftOriginal = null;
    private int width;
    private int height;
    private ColorModel cmY;
    private WalkBar walk;
    private Boolean Log;

    public MW_PURE_LET2Dnew(DoubleMatrix2D imInput, DoubleMatrix2D imPSF, double[] noiseparameters, WalkBar walk, Boolean Log) {
        this.walk = walk;
        this.seType = 0;
        this.alpha = noiseparameters[0];
        this.sigma = noiseparameters[1];
        this.Log = Log;

        this.height = imInput.rows();
        this.width = imInput.columns();

        this.fftInput = ImageUtil.colt2blasComplexMatrix(((DenseDoubleMatrix2D) imInput)
                .getFft2());

        int[] psfCenter = PSFUtil.getCenter((DenseDoubleMatrix2D) imPSF);
        DoubleMatrix2D auxPSF = DoubleCommon2D.circShift(imPSF, psfCenter);

        this.fftPSF = ImageUtil.colt2blasComplexMatrix(((DenseDoubleMatrix2D) auxPSF)
                .getFft2());
        //Parameters
//        double[] lambda = new double[]{1e-4, 1e-3, 1e-2};
        double[] lambda = new double[]{1e-4};

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

    private DoubleMatrix getS2() {
        DoubleMatrix2D S = PSFUtil.getRegularizer(width, height);
//        DoubleMatrix2D S = new DenseDoubleMatrix2D(width, height);
//        S.assign(DoubleFunctions.plus(1));
        DoubleMatrix2D S2 = S.copy();
        S2.assign(DoubleFunctions.square);

        DoubleMatrix SS = ImageUtil.colt2blasMatrix(S2);
        return SS;
    }

    private ComplexDoubleMatrix getH2() {

        ComplexDoubleMatrix H2 = fftPSF.dup();
        H2.conji();
        H2.muli(fftPSF);

        return H2;
    }

    private ComplexDoubleMatrix getHbi(DoubleMatrix S2, ComplexDoubleMatrix H2) {
        ComplexDoubleMatrix auxHbi = new ComplexDoubleMatrix(S2);
        auxHbi.muli(beta);
        auxHbi.addi(H2);
        ComplexDoubleMatrix Hbi = fftPSF.dup();
        Hbi.conji();
        Hbi.divi(auxHbi);

        return Hbi;
    }

    private ComplexDoubleMatrix getHli(DoubleMatrix S2, ComplexDoubleMatrix H2, double regParameter) {
        ComplexDoubleMatrix auxHli = new ComplexDoubleMatrix(S2);

        auxHli.muli(regParameter);
        auxHli.addi(H2);

        ComplexDoubleMatrix Hli = fftPSF.dup();
        Hli.conji();
        Hli.divi(auxHli);
        return Hli;
    }

    public boolean doDeconvolution() {


        if (Log) {
            IJ.log("---- begin deconvolution ----");
        }
        double startTime = System.nanoTime(); // start timing

        DoubleMatrix S2 = this.getS2();
        ComplexDoubleMatrix H2 = this.getH2();

        ComplexDoubleMatrix Ht = fftPSF.dup();
        Ht.conji();

        ComplexDoubleMatrix Hbi = this.getHbi(S2, H2);


        // Construct fftwfilter
        final FFT_WFilter wf = new FFT_WFilter();
        wf.fft_wfilters2D(width, height, 3, this.level);
        ComplexDoubleMatrix decLow = PSFUtil.getDecMatrix2DNew(wf);
        ComplexDoubleMatrix recLow = PSFUtil.getRecMatrix2DNew(decLow, this.level);

        int subnum = this.level * 3 * this.numWiener;
        int[][] indices = new int[subnum][4];

        int wk = 0;
        for (int wwi = 0; wwi < this.numWiener; wwi++) {
            for (int ji = 0; ji < this.level; ji++) {
                for (int oi = 0; oi < 3; oi++) {
                    indices[wk][0] = wk;
                    indices[wk][1] = wwi;
                    indices[wk][2] = ji;
                    indices[wk][3] = oi;
                    wk += 1;
                }
            }
        }

        DComplexMatrix1D[] matrixCFele = new DComplexMatrix1D[2];
        double[] Div = new double[2];
        DoubleMatrix1D div = new DenseDoubleMatrix1D(this.numSubbands);

        ReValue[] rvList = new ReValue[subnum];
        DComplexMatrix2D[] LowerBand = new DenseDComplexMatrix2D[this.numWiener];

        ////////////////////////////
        double runningTime = (System.nanoTime() - startTime) / 1.0E9D;
        if (Log)
        {
            IJ.log("[Step 0 - preparing] running time: " + runningTime + " s");
        }

        startTime = System.nanoTime(); // start timing

        int np = ConcurrencyUtils.getNumberOfThreads();

        ComplexDoubleMatrix Hbit = Hbi.dup();
        Hbit.conji();

        ComplexDoubleMatrix lowBand = new ComplexDoubleMatrix(width, height);
        ComplexDoubleMatrix[] YI = new ComplexDoubleMatrix[this.numWiener];
        ComplexDoubleMatrix[] HLI = new ComplexDoubleMatrix[this.numWiener];

        for (int wi = 0; wi < this.numWiener; wi++) {

            ComplexDoubleMatrix Hli = this.getHli(S2, H2, lambda[wi]);

            ComplexDoubleMatrix Yi = Hli.dup();
            Yi.muli(fftInput);

            ComplexDoubleMatrix aux_Band = Yi.dup();

            aux_Band.muli(decLow);
            aux_Band.muli(recLow);
            aux_Band.muli(1.0 / this.numWiener);
            aux_Band.addi(lowBand);

            lowBand = aux_Band.dup();

            YI[wi] = Yi.dup();
            HLI[wi] = Hli.dup();
        }

        if ((np > 1) && (width * height >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            Future<?>[] futures = new Future[np];
            int k = subnum / np;
            for (int j = 0; j < np; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == np - 1) ? subnum : firstRow + k;

                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int i = firstRow; i < lastRow; i++) {

                            int wwi = indices[i][1];
                            int ji = indices[i][2];
                            int oi = indices[i][3];

                            LETProcess2Dnew lp = new LETProcess2Dnew(alpha, ji,
                                    oi, wf, fftInput, YI[wwi], HLI[wwi], Hbit);

                            long time1 = System.nanoTime();
                            try {
                                lp.run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            ReValue rv = lp.getRV();
                            rvList[i] = rv;

                            double time2 = (System.nanoTime() - time1) / 1.0E9D;
                            System.out.println("proc: " + i + ", w: " + wwi + ", j: " + ji + ", o: " + oi + ", time:" + time2);

                        }

                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        }

        runningTime = (System.nanoTime() - startTime) / 1.0E9D;
        if (Log)

        {
            IJ.log("[Step 1 - Subprocessings] running time: " + runningTime + " s");
        }

        startTime = System.nanoTime(); // start timing


        for (
                int rvi = 0;
                rvi < subnum; rvi++)

        {
            ReValue rv = rvList[rvi];
            if (rv.getDiv()[0] == 0) {
                this.numSubbands -= 2;
                continue;
            }
        }

        DComplexMatrix2D matrixCF = new DenseDComplexMatrix2D(width * height,
                this.numSubbands);
        int kfv = 0;
        for (
                int rvi = 0;
                rvi < subnum; rvi++)

        {
            ReValue rv = rvList[rvi];
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
        if (Log)

        {
            IJ.log("[Step 1-1 - Post-Subprocessings] running time: " + runningTime + " s");
        }

        startTime = System.nanoTime(); // start timing


        //the most time-costing part
//        DComplexMatrix2D matrixFTrans = matrixCF.getConjugateTranspose();
//
//        DComplexMatrix2D matrixM = new DenseDComplexMatrix2D(MW_PURE_LET2D.numSubbands,
//                MW_PURE_LET2D.numSubbands);
//
//        DComplexMatrix2D pemFtrans = matrixFTrans.copy();
//
//        double startTime1 = System.nanoTime();
//        matrixFTrans.zMult(matrixCF, matrixM);
//        double runningTime1 = (System.nanoTime() - startTime1) / 1.0E9D;
//        System.out.println("-- multiplication time: " + runningTime1 + " s");
//        DoubleMatrix2D matrixA = matrixM.getRealPart();

        //Library: JBLAS
        DComplexMatrix2D matrixFTrans = matrixCF.getConjugateTranspose();
        DComplexMatrix2D pemFtrans = matrixFTrans.copy();

        ComplexDoubleMatrix matrix = ImageUtil.colt2blasComplexMatrix(matrixFTrans);

        ComplexDoubleMatrix FF = new ComplexDoubleMatrix(kfv, kfv);
        matrix.mmuli(matrix.transpose().

                conji(), FF);
        ComplexDouble[][] cd = FF.toArray2();
        DoubleMatrix2D matrixA = new DenseDoubleMatrix2D(kfv, kfv);

        for (
                int i = 0;
                i < cd.length; i++)

        {
            for (int j = 0; j < cd[0].length; j++) {
                double tmp = cd[i][j].real() / Math.pow(width * height, 2);
                matrixA.setQuick(i, j, tmp);
            }
        }

        runningTime = (System.nanoTime() - startTime) / 1.0E9D;
        if (Log)

        {
            IJ.log("[Step 2 - Matrix formation] running time: " + runningTime + " s");
        }

        DoubleMatrix1D matrixC = new DenseDoubleMatrix1D(width * height);

        startTime = System.nanoTime(); // start timing

        // PURE-LET
        DComplexMatrix2D Ybi = ImageUtil.blas2coltComplexMatrix(fftInput).copy();
        Ybi.assign(ImageUtil.blas2coltComplexMatrix(Hbi), DComplexFunctions.mult);

        DComplexMatrix2D lowBandColt = ImageUtil.blas2coltComplexMatrix(lowBand);
        Ybi.assign(lowBandColt, DComplexFunctions.minus);

        DComplexMatrix1D matrixMin = Ybi.vectorize();

        DComplexMatrix2D matrixEqC = new DenseDComplexMatrix2D(width
                * height, 1);
        matrixEqC.assign(matrixMin.toArray());

        DComplexMatrix2D auxC = new DenseDComplexMatrix2D(this.numSubbands,
                1);
        pemFtrans.zMult(matrixEqC,
                auxC, new double[]{1, 0},
                new double[]{0, 0}, false, false);
        matrixC = auxC.getRealPart().vectorize();
        matrixC.assign(DoubleFunctions.div(Math.pow(width * height, 2)));
        DoubleMatrix1D divC = new DenseDoubleMatrix1D(MW_PURE_LET2Dnew.numSubbands);
        divC.viewPart(0, MW_PURE_LET2Dnew.numSubbands).assign(
                div.viewPart(0, MW_PURE_LET2Dnew.numSubbands));
        matrixC.assign(divC.assign(DoubleFunctions.div(width * height)),
                DoubleFunctions.minus);

        for (int wi = 0; wi < MW_PURE_LET2Dnew.numSubbands; wi++) {
            double mV = matrixA.getQuick(wi, wi);
            matrixA.setQuick(wi, wi, mV + 0.05);
        }


        matrixC.assign(DoubleFunctions.max(0));

        // solving a linear system of equations
        DenseDoubleMatrix1D template = new DenseDoubleMatrix1D(MW_PURE_LET2Dnew.numSubbands);
        double[] arTemplate = new double[MW_PURE_LET2Dnew.numSubbands];
        Arrays.fill(arTemplate, 0D);
        template.assign(arTemplate);

        DoubleCG dc = new DoubleCG(template);
        DenseDoubleMatrix1D coeff = new DenseDoubleMatrix1D(MW_PURE_LET2Dnew.numSubbands);
        try

        {
            dc.solve(matrixA, matrixC, coeff);
        } catch (
                IterativeSolverDoubleNotConvergedException e)

        {
            e.printStackTrace();
        }

        runningTime = (System.nanoTime() - startTime) / 1.0E9D;
        if (Log)

        {
            IJ.log("[Step 3 - Solving linear system] running time: " + runningTime + " s");
        }


        startTime = System.nanoTime(); // start timing
        DComplexMatrix2D coeff2D = new DenseDComplexMatrix2D(MW_PURE_LET2Dnew.numSubbands,
                1);
        (coeff2D).

                viewColumn(0).

                assignReal(coeff);

        DComplexMatrix2D Fa = new DenseDComplexMatrix2D(width * height, 1);

        Fa = matrixCF.zMult(coeff2D,
                Fa, new double[]

                        {
                                1, 1
                        }, new double[]

                        {
                                0,
                                0
                        }, false, false);

        DComplexMatrix2D matrixLow2D = new DenseDComplexMatrix2D(
                width * height, 1);
        DComplexMatrix1D lowBand1D = lowBandColt.vectorize();

        matrixLow2D.viewColumn(0).

                assign(lowBand1D);

        Fa.assign(matrixLow2D, DComplexFunctions.plus);

        Fa = (Fa.vectorize()).

                reshape(width, height);
        ((DenseDComplexMatrix2D) Fa).

                ifft2(true);

        DoubleMatrix2D recImg = Fa.getRealPart();
        recImg.assign(DoubleFunctions.abs);

        runningTime = (System.nanoTime() - startTime) / 1.0E9D;
        if (Log)

        {
            IJ.log("[Step 4 - Final multiplication] running time: " + runningTime + " s");

            IJ.log("Deconvolution finished");
            IJ.log("-------------------------");
        }

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
