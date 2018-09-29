package com.hijizhou.utilities;

import com.cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix2D;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.edu.emory.mathcs.utils.pc.ConcurrencyUtils;
import com.org.jtransforms.fft.DoubleFFT_1D;
import com.org.jtransforms.fft.DoubleFFT_2D;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

import java.util.concurrent.Future;

public class SpectralUtils {

    private static void fftTransform1D_inplace(ComplexDoubleMatrix vector, int fftLength, int direction) {
        switch (direction) {
            case 1:
                fft1D_inplace(vector, fftLength);
                break;
            case -1:
                invfft1D_inplace(vector, fftLength);
                break;
            default:
                throw new IllegalArgumentException("fourier1D: direction 1, or -1");
        }
    }

    public static void fft1D_inplace(ComplexDoubleMatrix vector, final int fftLength) {
        DoubleFFT_1D fft = new DoubleFFT_1D(fftLength);
        fft.complexForward(vector.data);
    }

    public static void invfft1D_inplace(ComplexDoubleMatrix vector, final int fftLength) {
        DoubleFFT_1D fft = new DoubleFFT_1D(fftLength);
        fft.complexInverse(vector.data, true);
    }

    public static ComplexDoubleMatrix fft1D(ComplexDoubleMatrix vector, final int fftLength) {
        DoubleFFT_1D fft = new DoubleFFT_1D(fftLength);
        fft.complexForward(vector.data);
        return vector;
    }

    public static ComplexDoubleMatrix invfft1D(ComplexDoubleMatrix vector, final int fftLength) {
        DoubleFFT_1D fft = new DoubleFFT_1D(fftLength);
        fft.complexInverse(vector.data, true);
        return vector;
    }

    private static ComplexDoubleMatrix fftTransform(ComplexDoubleMatrix A, final int dimension, final int flag) {
        ComplexDoubleMatrix result = A.dup(); // have to copy matrix!
        fftTransformInPlace(result, dimension, flag);
        return result;
    }

    private static void fftTransformInPlace(ComplexDoubleMatrix cplxData, int dimension, int flag) {
        int i;
        final int columns = cplxData.columns;
        final int rows = cplxData.rows;

        switch (dimension) {
            case 1: {

                for (i = 0; i < columns; ++i) {
                    ComplexDoubleMatrix VECTOR = cplxData.getColumn(i);
                    fftTransform1D_inplace(VECTOR, rows, flag);
                    cplxData.putColumn(i, VECTOR);
                }
                break;
            }
            case 2: {

                for (i = 0; i < rows; ++i) {
                    ComplexDoubleMatrix VECTOR = cplxData.getRow(i);
                    fftTransform1D_inplace(VECTOR, columns, flag);
                    cplxData.putRow(i, VECTOR);
                }
                break;
            }
            default:

                throw new IllegalArgumentException("ifft: dimension != {1,2}");
        }
    }

    public static ComplexDoubleMatrix fft(ComplexDoubleMatrix inMatrix, final int dimension) {
        return fftTransform(inMatrix, dimension, 1);
    }

    public static ComplexDoubleMatrix invfft(ComplexDoubleMatrix inMatrix, final int dimension) {
        return fftTransform(inMatrix, dimension, -1);
    }

    public static void fft_inplace(ComplexDoubleMatrix inMatrix, int dimension) {
        fftTransformInPlace(inMatrix, dimension, 1);
    }

    public static void invfft_inplace(ComplexDoubleMatrix inMatrix, int dimension) {
        fftTransformInPlace(inMatrix, dimension, -1);
    }

    public static void fft2D_inplace(ComplexDoubleMatrix A) {
        DoubleFFT_2D fft2d = new DoubleFFT_2D(A.rows, A.columns);
//        fft2d.complexForward(A.data);
        ComplexDoubleMatrix aTemp = A.transpose();
        fft2d.complexForward(aTemp.data);
        A.data = aTemp.transpose().data;
    }

    public static ComplexDoubleMatrix fft2D(ComplexDoubleMatrix inMatrix) {
        ComplexDoubleMatrix outMatrix = inMatrix.dup();
        fft2D_inplace(outMatrix);
        return outMatrix;
    }
    public static ComplexDoubleMatrix fft2DNew(DoubleMatrix inMatrix){

        DoubleMatrix2D A = ImageUtil.blas2coltMatrix(inMatrix);
        return ImageUtil.colt2blasComplexMatrix(((DenseDoubleMatrix2D) A)
                .getFft2());
    }

    public static ComplexDoubleMatrix fft2DNew(DoubleMatrix2D inMatrix){

        return ImageUtil.colt2blasComplexMatrix(((DenseDoubleMatrix2D) inMatrix)
                .getFft2());
    }

    public static void fft2D_inplace2(ComplexDoubleMatrix A) {
        int rows = A.rows;
        int columns = A.columns;

        ComplexDoubleMatrix aTemp = A.transpose();

        DoubleFFT_2D fft2 = new DoubleFFT_2D(rows, columns);

        int oldNthreads = ConcurrencyUtils.getNumberOfThreads();
        ConcurrencyUtils.setNumberOfThreads(ConcurrencyUtils.nextPow2(oldNthreads));
        if (fft2 == null) {
            fft2 = new DoubleFFT_2D(rows, columns);
        }

         final  double[] elementsA = aTemp.data;

        DenseDComplexMatrix2D C = new DenseDComplexMatrix2D(rows, columns);
        final double[] elementsC = (C).elements();
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows*columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        for (int r = firstRow; r < lastRow; r++) {
                            System.arraycopy(elementsA, r * columns, elementsC, r * columns, columns);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                System.arraycopy(elementsA, r * columns, elementsC, r * columns, columns);
            }
        }
        fft2.realForwardFull(elementsC);
        ConcurrencyUtils.setNumberOfThreads(oldNthreads);

        A.data = elementsC;
    }

    public static void fft2D_inplace(DoubleMatrix A) {
        DoubleFFT_2D fft2d = new DoubleFFT_2D(A.rows, A.columns);
        fft2d.realForwardFull(A.data);
    }



    public static void invfft2D_inplace(ComplexDoubleMatrix A) {
        DoubleFFT_2D fft2d = new DoubleFFT_2D(A.rows, A.columns);
//        fft2d.complexInverse(A.data, true);
        ComplexDoubleMatrix aTemp = A.transpose();
        fft2d.complexInverse(aTemp.data, true);
        A.data = aTemp.transpose().data;
    }

    public static void invfft2D_inplace2(ComplexDoubleMatrix A) {
        int oldNthreads = ConcurrencyUtils.getNumberOfThreads();
        DoubleFFT_2D fft2 = new DoubleFFT_2D(A.rows, A.columns);
        ConcurrencyUtils.setNumberOfThreads(ConcurrencyUtils.nextPow2(oldNthreads));
        fft2.complexInverse(A.data, true);
    }

    public static ComplexDoubleMatrix invfft2d(ComplexDoubleMatrix inMatrix) {
        ComplexDoubleMatrix outMatrix = inMatrix.dup();
        invfft2D_inplace(outMatrix);
        return outMatrix;
    }

    public static ComplexDoubleMatrix fftshift(ComplexDoubleMatrix inMatrix) {
        if (!inMatrix.isVector()) {

            throw new IllegalArgumentException("ifftshift: works only for vectors!");
        }

        final int cplxMatrixLength = 2*inMatrix.length;

        ComplexDoubleMatrix outMatrix = new ComplexDoubleMatrix(inMatrix.rows, inMatrix.columns);
        final int start = (int) (Math.floor((double) cplxMatrixLength / 2) + 1);

        System.arraycopy(inMatrix.data, start, outMatrix.data, 0, cplxMatrixLength - start);
        System.arraycopy(inMatrix.data, 0, outMatrix.data, cplxMatrixLength - start, start);

        return outMatrix;
    }

    public static DoubleMatrix fftshift(DoubleMatrix inMatrix) {
        if (!inMatrix.isVector()) {

            throw new IllegalArgumentException("ifftshift: works only for vectors!");
        }

        DoubleMatrix outMatrix = new DoubleMatrix(inMatrix.rows, inMatrix.columns);
        final int start = (int) (Math.ceil((double) inMatrix.length / 2));

        System.arraycopy(inMatrix.data, start, outMatrix.data, 0, inMatrix.length - start);
        System.arraycopy(inMatrix.data, 0, outMatrix.data, inMatrix.length - start, start);

        return outMatrix;
    }

    public static void fftshift_inplace(ComplexDoubleMatrix inMatrix) {
        // NOT very efficient! Allocating and copying! //
        inMatrix.copy(fftshift(inMatrix));
    }

    public static void fftshift_inplace(DoubleMatrix inMatrix) {
        // NOT very efficient! Allocating and copying! //
        inMatrix.copy(fftshift(inMatrix));
    }

    /**
     * ifftshift(inMatrix)                                                 *
     * ifftshift of vector inMatrix is returned in inMatrix by reference       *
     * undo effect of fftshift. ?p=floor(m/2); inMatrix=inMatrix[p:m-1 0:p-1]; *
     */
    public static ComplexDoubleMatrix ifftshift(ComplexDoubleMatrix inMatrix) throws IllegalArgumentException {

        if (!inMatrix.isVector()) {

            throw new IllegalArgumentException("ifftshift: works only for vectors!");
        }

        final int cplxMatrixLength = 2*inMatrix.length;

        ComplexDoubleMatrix outMatrix = new ComplexDoubleMatrix(inMatrix.rows, inMatrix.columns);
        final int start = (int) (Math.floor((double) cplxMatrixLength / 2) - 1);

        System.arraycopy(inMatrix.data, start, outMatrix.data, 0, cplxMatrixLength - start);
        System.arraycopy(inMatrix.data, 0, outMatrix.data, cplxMatrixLength - start, start);

        return outMatrix;
    }

    public static DoubleMatrix ifftshift(DoubleMatrix inMatrix) throws IllegalArgumentException {

        if (!inMatrix.isVector()) {

            throw new IllegalArgumentException("ifftshift: works only for vectors!");
        }

        DoubleMatrix outMatrix = new DoubleMatrix(inMatrix.rows, inMatrix.columns);
        final int start = (int) (Math.ceil((double) inMatrix.length) / 2);

        System.arraycopy(inMatrix.data, start, outMatrix.data, 0, inMatrix.length - start);
        System.arraycopy(inMatrix.data, 0, outMatrix.data, inMatrix.length - start, start);

        return outMatrix;
    }

    public static void ifftshift_inplace(ComplexDoubleMatrix inMatrix) throws IllegalArgumentException {
        // NOT very efficient! Allocating and copying! //
        inMatrix.copy(ifftshift(inMatrix));
    }

    public static void ifftshift_inplace(DoubleMatrix inMatrix) throws IllegalArgumentException {
        // NOT very efficient! Allocating and copying! //
        inMatrix.copy(ifftshift(inMatrix));
    }




}