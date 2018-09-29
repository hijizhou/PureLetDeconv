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
import com.cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import com.cern.colt.matrix.tdcomplex.DComplexMatrix3D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix2D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix3D;
import com.cern.colt.matrix.tdouble.DoubleFactory2D;
import com.cern.colt.matrix.tdouble.DoubleFactory3D;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.DoubleMatrix3D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import com.cern.jet.math.tdcomplex.DComplexFunctions;
import com.cern.jet.math.tdouble.DoubleFunctions;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon3D;
import ij.ImageStack;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

public class PSFUtil {

    public static DoubleMatrix2D getGaussPSF(int nx, int ny, double sigma) {
        DoubleMatrix2D PSF = DoubleFactory2D.dense.make(nx, ny);
        double KX = 0.5 / (sigma * sigma);
        double KY = 0.5 / (sigma * sigma);

        double xc = 0.5 * nx;
        double yc = 0.5 * ny;

        for (int x = 0; x < nx; x++) {
            for (int y = 0; y < ny; y++) {
                double r2 = KX * (x - xc) * (x - xc) + KY * (y - yc) * (y - yc);
                PSF.setQuick(x, y, Math.exp(-r2));
            }
        }
        return PSF;
    }

    public static DoubleMatrix3D getGaussPSF(int nx, int ny, int nz, double sigma) {
        DoubleMatrix3D PSF = DoubleFactory3D.dense.make(nz, nx, ny);
        double KX = 0.5 / (sigma * sigma);
        double KY = 0.5 / (sigma * sigma);
        double KZ = 0.5 / (sigma * sigma);

        double xc = 0.5 * nx;
        double yc = 0.5 * ny;
        double zc = 0.5 * nz;

        for (int z = 0; z < nz; z++) {
            for (int x = 0; x < nx; x++) {
                for (int y = 0; y < ny; y++) {
                    double r2 = KX * (x - xc) * (x - xc) + KY * (y - yc) * (y - yc) + KZ * (z - zc) * (z - zc);
                    PSF.setQuick(z, x, y, Math.exp(-r2));
                }
            }
        }
        return PSF;
    }

    public static DoubleMatrix3D getGibsonLanni(int nx, int ny, int nz) {

        GibsonLanni GL = new GibsonLanni();
        GL.setNx(nx);
        GL.setNy(ny);
        GL.setNz(nz);
        ImageStack PSFstack = GL.compute();
        DoubleMatrix3D PSF = DoubleFactory3D.dense.make(nz, nx, ny);
        DoubleCommon3D.assignPixelsToMatrix(PSFstack, PSF);
        return PSF;
    }


    public static int[] getCenter(DenseDoubleMatrix2D matrixPSF) {
        double[] maxAndLoc = matrixPSF.getMaxLocation();
        int[] psfCenter = new int[]{(int) maxAndLoc[1], (int) maxAndLoc[2]};
        matrixPSF.normalize();
        psfCenter[0] += 0.5;
        psfCenter[1] += 0.5;
        return psfCenter;
    }

    public static int[] getCenter(DenseDoubleMatrix3D matrixPSF) {
        double[] maxAndLoc = matrixPSF.getMaxLocation();
        int[] psfCenter = new int[]{(int) maxAndLoc[1], (int) maxAndLoc[2], (int) maxAndLoc[3]};
        matrixPSF.normalize();
        psfCenter[0] += 0.5;
        psfCenter[1] += 0.5;
        psfCenter[2] += 0.5;

        return psfCenter;
    }

    public static DoubleMatrix2D getRegularizer(int width, int height) {
        double[] templX = new double[width];
        double[] templY = new double[height];

        int xi = 0;
        for (double ix = -1; ix <= 1; ix += 2.0D / (width - 1)) {
            templX[xi++] = ix;
        }
        templX[width - 1] = 1;
        xi = 0;
        for (double iy = -1; iy <= 1; iy += 2.0D / (height - 1)) {
            templY[xi++] = iy;
        }
        templY[height - 1] = 1;
        // construct regularizer S
        DoubleMatrix2D S = new DenseDoubleMatrix2D(width, height);
        DoubleMatrix2D X = new DenseDoubleMatrix2D(width, height);
        DoubleMatrix2D Y = new DenseDoubleMatrix2D(width, height);
        for (int sx = 0; sx < height; sx++) {
            for (int sy = 0; sy < width; sy++) {
                X.setQuick(sx, sy, templX[sy]);
                Y.setQuick(sy, sx, templY[sy]);
            }
        }
        for (int sx = 0; sx < height; sx++) {
            for (int sy = 0; sy < width; sy++) {
                double tmpSx = X.getQuick(sx, sy);
                double tmpSy = Y.getQuick(sx, sy);

                S.setQuick(sx, sy, tmpSx * tmpSx + tmpSy * tmpSy);
            }
        }
        double maxS = S.getQuick(0, 0);
        for (int sx = 0; sx < height; sx++) {
            for (int sy = 0; sy < width; sy++) {
                double tmpS = S.getQuick(sx, sy);
                if (tmpS > maxS) {
                    maxS = tmpS;
                }
            }
        }

        S.assign(DoubleFunctions.mult(-1));
        S.assign(DoubleFunctions.plus(maxS));

        // padding and duplication
        if (((width % 2) == 0) && ((height % 2) == 0)) {
            DoubleMatrix2D auxS1 = S.viewPart(0, 0,
                    height / 2 + 1, width / 2 + 1);
            DoubleMatrix2D auxS2 = auxS1.copy();
            DoubleMatrix2D auxS3 = auxS1.copy();
            DoubleMatrix2D auxS4 = auxS1.copy();
            for (int hindex = 0; hindex < height / 2 + 1; hindex++) {
                for (int windex = 0; windex < width / 2 + 1; windex++) {
                    auxS2.setQuick(hindex, width / 2 - windex,
                            auxS1.getQuick(windex, hindex));
                    auxS3.setQuick(height / 2 - hindex, windex,
                            auxS1.getQuick(windex, hindex));
                    auxS4.setQuick(height / 2 - hindex, width / 2 - windex,
                            auxS1.getQuick(windex, hindex));
                }
            }

            S.viewPart(0, width / 2, height / 2 + 1,
                    width / 2).assign(
                    auxS2.viewPart(0, 0, height / 2 + 1, width / 2));
            S.viewPart(height / 2, 0, height / 2,
                    width / 2 + 1).assign(
                    auxS3.viewPart(0, 0, height / 2, width / 2 + 1));
            S.viewPart(height / 2, width / 2,
                    height / 2, width / 2).assign(
                    auxS4.viewPart(0, 0, height / 2, width / 2));

        }
        return S;
    }

    public static DoubleMatrix3D getRegularizer(int width, int height, int slice) {
        double[] templX = new double[width];
        double[] templY = new double[height];
        double[] templZ = new double[slice];

        int xi = 0;
        for (double ix = -1; ix <= 1; ix += 2.0D / (width - 1)) {
            templX[xi++] = ix;
        }
        templX[width - 1] = 1;

        xi = 0;
        for (double iy = -1; iy <= 1; iy += 2.0D / (height - 1)) {
            templY[xi++] = iy;
        }
        templY[height - 1] = 1;

        xi = 0;
        for (double iz = -1; iz <= 1; iz += 2.0D / (slice - 1)) {
            templZ[xi++] = iz;
        }
        templZ[slice - 1] = 1;
        // construct regularizer S
        DoubleMatrix3D S = new DenseDoubleMatrix3D(slice, width, height);
        DoubleMatrix3D X = new DenseDoubleMatrix3D(slice, width, height);
        DoubleMatrix3D Y = new DenseDoubleMatrix3D(slice, width, height);
        DoubleMatrix3D Z = new DenseDoubleMatrix3D(slice, width, height);
        for (int sz = 0; sz < slice; sz++) {
            for (int sx = 0; sx < height; sx++) {
                for (int sy = 0; sy < width; sy++) {
                    X.setQuick(sz, sx, sy, templX[sy]);
                    Y.setQuick(sz, sy, sx, templY[sy]);
                    Z.setQuick(sz, sy, sx, templZ[sz]);
                }
            }
        }

        for (int sz = 0; sz < slice; sz++) {
            for (int sx = 0; sx < height; sx++) {
                for (int sy = 0; sy < width; sy++) {
                    double tmpSx = X.getQuick(sz, sx, sy);
                    double tmpSy = Y.getQuick(sz, sx, sy);
                    double tmpSz = Z.getQuick(sz, sx, sy);

                    S.setQuick(sz, sx, sy, tmpSx * tmpSx + tmpSy * tmpSy + tmpSz * tmpSz);
                }
            }
        }
        double maxS = S.getQuick(0, 0, 0);
        for (int sz = 0; sz < slice; sz++) {
            for (int sx = 0; sx < height; sx++) {
                for (int sy = 0; sy < width; sy++) {
                    double tmpS = S.getQuick(sz, sx, sy);
                    if (tmpS > maxS) {
                        maxS = tmpS;
                    }
                }
            }
        }

        S.assign(DoubleFunctions.mult(-1));
        S.assign(DoubleFunctions.plus(maxS));

        // padding and duplication
        if (((width % 2) == 0) && ((height % 2) == 0) && ((slice % 2) == 0)) {
            DoubleMatrix3D auxS1 = S.viewPart(0, 0, 0,
                    slice / 2 + 1, height / 2 + 1, width / 2 + 1);
            DoubleMatrix3D auxS2 = auxS1.copy();
            DoubleMatrix3D auxS3 = auxS1.copy();
            DoubleMatrix3D auxS4 = auxS1.copy();
            DoubleMatrix3D auxS5 = auxS1.copy();
            DoubleMatrix3D auxS6 = auxS1.copy();
            DoubleMatrix3D auxS7 = auxS1.copy();
            DoubleMatrix3D auxS8 = auxS1.copy();
            for (int sindex = 0; sindex < slice / 2 + 1; sindex++) {
                for (int hindex = 0; hindex < height / 2 + 1; hindex++) {
                    for (int windex = 0; windex < width / 2 + 1; windex++) {
                        double aux = auxS1.getQuick(sindex, windex, hindex);
                        auxS2.setQuick(sindex, hindex, width / 2 - windex,
                                aux);
                        auxS3.setQuick(sindex, height / 2 - hindex, windex,
                                aux);
                        auxS4.setQuick(sindex, height / 2 - hindex, width / 2 - windex,
                                aux);

                        auxS5.setQuick(slice / 2 - sindex, hindex, windex,
                                aux);
                        auxS6.setQuick(slice / 2 - sindex, height / 2 - hindex, windex,
                                aux);
                        auxS7.setQuick(slice / 2 - sindex, hindex, width / 2 - windex,
                                aux);
                        auxS8.setQuick(slice / 2 - sindex, height / 2 - hindex, width / 2 - windex,
                                aux);

                    }
                }
            }


            S.viewPart(0, 0, width / 2, slice / 2 + 1, height / 2 + 1,
                    width / 2).assign(
                    auxS2.viewPart(0, 0, 0, slice / 2 + 1, height / 2 + 1, width / 2));

            S.viewPart(0, height / 2, 0, slice / 2 + 1, height / 2,
                    width / 2 + 1).assign(
                    auxS3.viewPart(0, 0, 0, slice / 2 + 1, height / 2, width / 2 + 1));

            S.viewPart(0, height / 2, width / 2, slice / 2 + 1, height / 2,
                    width / 2).assign(
                    auxS4.viewPart(0, 0, 0, slice / 2 + 1, height / 2, width / 2));

            S.viewPart(slice / 2, 0, 0, slice / 2, height / 2 + 1,
                    width / 2 + 1).assign(
                    auxS5.viewPart(0, 0, 0, slice / 2, height / 2 + 1, width / 2 + 1));

            S.viewPart(slice / 2, height / 2, 0, slice / 2, height / 2,
                    width / 2 + 1).assign(
                    auxS6.viewPart(0, 0, 0, slice / 2, height / 2, width / 2 + 1));

            S.viewPart(slice / 2, 0, width/2, slice / 2, height / 2+1,
                    width / 2).assign(
                    auxS7.viewPart(0, 0, 0, slice / 2, height / 2+1, width / 2));

            S.viewPart(slice / 2, height / 2, width / 2, slice / 2, height / 2,
                    width / 2).assign(
                    auxS8.viewPart(0, 0, 0, slice / 2, height / 2, width / 2));
        }

        return S;
    }

    public static DComplexMatrix2D getRecMatrix2D(DComplexMatrix2D decMatrix, int level){
        DComplexMatrix2D recMatrix = decMatrix.copy();
        recMatrix.assign(DComplexFunctions.conj);
        recMatrix.assign(DComplexFunctions.mult(1.0 / Math.pow(4, level)));
        return recMatrix;
    }

    public static DComplexMatrix3D getRecMatrix3D(DComplexMatrix3D decMatrix, int level){
        DComplexMatrix3D recMatrix = decMatrix.copy();
        recMatrix.assign(DComplexFunctions.conj);
        recMatrix.assign(DComplexFunctions.mult(1.0 / Math.pow(8, level)));
        return recMatrix;
    }

    public static DComplexMatrix2D getDecMatrix2D(FFT_WFilter wf) {
        double[] Dx = wf.getDx();
        double[] Dy = wf.getDy();
        int width = Dx.length / 2;
        int height = Dy.length / 2;

        DComplexMatrix2D decMatrix = new DenseDComplexMatrix2D(width, height);
        DComplexMatrix2D auxM_Dx = new DenseDComplexMatrix2D(width, 1);
        double[] aux_DxR = new double[width];
        double[] aux_DxI = new double[width];
        for (int i = 0; i < width * 2; i += 2) {
            aux_DxR[i / 2] = Dx[i];
            aux_DxI[i / 2] = Dx[i + 1];
        }
        auxM_Dx.assignReal((new DenseDoubleMatrix2D(width, 1)).assign(aux_DxR));
        auxM_Dx.assignImaginary((new DenseDoubleMatrix2D(width, 1))
                .assign(aux_DxI));

        DComplexMatrix2D auxM_Dy = new DenseDComplexMatrix2D(1, height);
        double[] aux_DyR = new double[height];
        double[] aux_DyI = new double[height];

        for (int i = 0; i < height * 2; i += 2) {
            aux_DyR[i / 2] = Dy[i];
            aux_DyI[i / 2] = Dy[i + 1];
        }
        auxM_Dy.assignReal((new DenseDoubleMatrix2D(1, height)).assign(aux_DyR));
        auxM_Dy.assignImaginary((new DenseDoubleMatrix2D(1, height))
                .assign(aux_DyI));

        decMatrix = auxM_Dx.zMult(auxM_Dy, decMatrix,
                new double[]{1, 0}, new double[]{0, 0}, false, false);

        return decMatrix;

    }

    public static ComplexDoubleMatrix getDecMatrix2DNew(FFT_WFilter wf) {

        DComplexMatrix2D decMatrix_colt = getDecMatrix2D(wf);
        ComplexDoubleMatrix decMatrix = ImageUtil.colt2blasComplexMatrix(decMatrix_colt);


        return decMatrix;

    }

    public static ComplexDoubleMatrix getRecMatrix2DNew(ComplexDoubleMatrix decMatrix, int level) {

        ComplexDoubleMatrix recMatrix = decMatrix.dup();
        recMatrix.conji();
        recMatrix.muli(1.0 / Math.pow(4, level));

        return recMatrix;

    }

//    public static DC

    public static DComplexMatrix3D getDecMatrix3D(FFT_WFilter wf) {
        double[] Dx = wf.getDx();
        double[] Dy = wf.getDy();
        double[] Dz = wf.getDz();
        int nx = Dx.length / 2;
        int ny = Dy.length / 2;
        int nz = Dz.length / 2;

        DComplexMatrix3D decMatrix = new DenseDComplexMatrix3D(nz, nx, ny);
        DComplexMatrix2D DzMatrix = new DenseDComplexMatrix2D(nz, 1);
        DzMatrix.assign(Dz);

        DComplexMatrix2D slice = getDecMatrix2D(wf);
//        slice.assign(DComplexFunctions.conj);

        for (int k = 0; k < nz; k++) {
            DComplexMatrix2D slice2 = slice.copy();
            double[] zp = DzMatrix.getQuick(k, 0);
            slice2.assign(DComplexFunctions.mult(zp));
//            decMatrix.viewSlice(k).assign(slice2);
            for(int i=0; i<nx; i++){
                for(int j=0; j<ny; j++){
                    decMatrix.setQuick(k, i, j, slice2.getQuick(i,j));
                }
            }
        }


        return decMatrix;
    }

    public static DComplexMatrix3D getDecMatrix3D_2(FFT_WFilter wf) {
        double[] Dx = wf.getDx();
        double[] Dy = wf.getDy();
        double[] Dz = wf.getDz();
        int nx = Dx.length / 2;
        int ny = Dy.length / 2;
        int nz = Dz.length / 2;

        DComplexMatrix3D decMatrix = new DenseDComplexMatrix3D(nz, nx, ny);


        // Dlx*Dly*Dlz to formulate a 3D matrix
        DoubleMatrix3D rsReal = new DenseDoubleMatrix3D(nz, nx, ny);
        DoubleMatrix3D rsImg = new DenseDoubleMatrix3D(nz, nx, ny);

        double[][] AuxReal = new double[nx][ny];
        double[][] AuxImg = new double[nx][ny];
        // real part
        for (int k = 0; k < nz; k++) {
            int kk = 2 * k;
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny; j++) {
                    int ii = 2 * i;
                    int jj = 2 * j;

                    double xyReal = Dx[ii] * Dy[jj] - Dx[ii + 1] * Dy[jj + 1];
                    double xyImag = Dx[ii + 1] * Dy[jj] + Dx[ii] * Dy[jj + 1];

                    AuxReal[i][j] = xyReal * Dz[kk] - xyImag * Dz[kk + 1]; //real
                    AuxImg[i][j] = xyImag * Dz[kk] + xyReal * Dz[kk + 1]; //imag
                }
            }
            rsReal.viewSlice(k).assign(AuxReal);
            rsImg.viewSlice(k).assign(AuxImg);
        }
        decMatrix.assignReal(rsReal);
        decMatrix.assignImaginary(rsImg);

        return decMatrix;
    }
}
