package com.hijizhou.imagej;

import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

public class TestJBLAS {
    public static void main(String[] args){

        int m = 65536; int n = 64;
        DoubleMatrix real = DoubleMatrix.rand(m, n);
        DoubleMatrix img = DoubleMatrix.rand(m, n);

        ComplexDoubleMatrix matrix = new ComplexDoubleMatrix(real, img);

        long startTime = System.nanoTime();
        ComplexDoubleMatrix result = new ComplexDoubleMatrix(n,n);
        matrix.muli(matrix.transpose(), result);

        long endTime = System.nanoTime();

        double estTime = (endTime - startTime) / 1000000000.0;
        System.out.println("Noise estimation time: " + estTime);



    }
}
