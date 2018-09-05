/* ***** BEGIN LICENSE BLOCK *****
 * JLargeArrays
 * Copyright (C) 2013 onward University of Warsaw, ICM
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ***** END LICENSE BLOCK ***** */
package com.pl.edu.icm.jlargearrays;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.math3.util.FastMath;
import com.pl.edu.icm.jlargearrays.ConcurrencyUtils;
import com.pl.edu.icm.jlargearrays.LargeArray;
import com.pl.edu.icm.jlargearrays.LargeArrayType;

/**
 *
 * Statistical operations on LargeArrays.
 *
 * @author Piotr Wendykier (p.wendykier@icm.edu.pl)
 */
public class LargeArrayStatistics
{

    private LargeArrayStatistics()
    {
    }

    /**
     * Minimum value of a LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return minimum value
     */
    public static double min(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        if (a == null || !a.isNumeric() || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
            throw new IllegalArgumentException("a == null || !a.isNumeric() || a.getType() == LargeArrayType.COMPLEX_FLOAT || a.getType() == LargeArrayType.COMPLEX_DOUBLE");
        }
        if (a.isConstant()) {
            return a.getDouble(0);
        } else {
            double min = a.getDouble(0);
            long length = a.length();
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                for (long i = 1; i < length; i++) {
                    double elem = a.getDouble(i);
                    if (elem < min) {
                        min = elem;
                    }
                }
            } else {
                long k = length / nthreads;
                Future[] threads = new Future[nthreads];
                for (int j = 0; j < nthreads; j++) {
                    final long firstIdx = j * k;
                    final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                    threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Callable<Double>()
                    {
                        @Override
                        public Double call()
                        {
                            double min = a.getDouble(firstIdx);
                            for (long k = firstIdx + 1; k < lastIdx; k++) {
                                double elem = a.getDouble(k);
                                if (elem < min) {
                                    min = elem;
                                }
                            }
                            return min;
                        }
                    });
                }
                try {
                    com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    for (int j = 0; j < nthreads; j++) {
                        double res = (double) threads[j].get();
                        if (res < min) {
                            min = res;
                        }
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    for (long i = 1; i < length; i++) {
                        double elem = a.getDouble(i);
                        if (elem < min) {
                            min = elem;
                        }
                    }
                }
            }
            return min;
        }
    }

    /**
     * Maximum value of a LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return maximum value
     */
    public static double max(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        if (a == null || !a.isNumeric() || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
            throw new IllegalArgumentException("a == null || !a.isNumeric() || a.getType() == LargeArrayType.COMPLEX_FLOAT || a.getType() == LargeArrayType.COMPLEX_DOUBLE");
        }
        if (a.isConstant()) {
            return a.getDouble(0);
        } else {
            double max = a.getDouble(0);
            long length = a.length();
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                for (long i = 1; i < length; i++) {
                    double elem = a.getDouble(i);
                    if (elem > max) {
                        max = elem;
                    }
                }
            } else {
                long k = length / nthreads;
                Future[] threads = new Future[nthreads];
                for (int j = 0; j < nthreads; j++) {
                    final long firstIdx = j * k;
                    final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                    threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Callable<Double>()
                    {
                        @Override
                        public Double call()
                        {
                            double max = a.getDouble(firstIdx);
                            for (long k = firstIdx + 1; k < lastIdx; k++) {
                                double elem = a.getDouble(k);
                                if (elem > max) {
                                    max = elem;
                                }
                            }
                            return max;
                        }
                    });
                }
                try {
                    com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    for (int j = 0; j < nthreads; j++) {
                        double res = (double) threads[j].get();
                        if (res > max) {
                            max = res;
                        }
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    for (long i = 1; i < length; i++) {
                        double elem = a.getDouble(i);
                        if (elem > max) {
                            max = elem;
                        }
                    }
                }
            }
            return max;
        }
    }

    /**
     * Sum of all elements in a LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return sum
     */
    public static double sum(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        if (a == null || !a.isNumeric() || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
            throw new IllegalArgumentException("a == null || !a.isNumeric() || a.getType() == LargeArrayType.COMPLEX_FLOAT || a.getType() == LargeArrayType.COMPLEX_DOUBLE");
        }
        if (a.isConstant()) {
            return a.length() * a.getDouble(0);
        } else {
            double sum = 0;
            long length = a.length();
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                for (long i = 0; i < length; i++) {
                    sum += a.getDouble(i);
                }
            } else {
                long k = length / nthreads;
                Future[] threads = new Future[nthreads];
                for (int j = 0; j < nthreads; j++) {
                    final long firstIdx = j * k;
                    final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                    threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Callable<Double>()
                    {
                        @Override
                        public Double call()
                        {
                            double sum = 0;
                            for (long k = firstIdx; k < lastIdx; k++) {
                                sum += a.getDouble(k);
                            }
                            return sum;
                        }
                    });
                }
                try {
                    com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    for (int j = 0; j < nthreads; j++) {
                        double res = (double) threads[j].get();
                        sum += res;
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    for (long i = 0; i < length; i++) {
                        sum += a.getDouble(i);
                    }
                }
            }
            return sum;
        }
    }

    /**
     * Sum of all elements in a LargeArray computed using Kahan algorithm.
     * <p>
     * @param a input array
     * <p>
     * @return sum
     */
    public static double sumKahan(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        if (a == null || !a.isNumeric() || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
            throw new IllegalArgumentException("a == null || !a.isNumeric() || a.getType() == LargeArrayType.COMPLEX_FLOAT || a.getType() == LargeArrayType.COMPLEX_DOUBLE");
        }
        if (a.isConstant()) {
            return a.length() * a.getDouble(0);
        } else {
            double sum = 0;
            double c = 0;
            long length = a.length();
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                for (long i = 0; i < length; i++) {
                    double y = a.getDouble(i) - c;
                    double t = sum + y;
                    c = (t - sum) - y;
                    sum = t;
                }
            } else {
                long k = length / nthreads;
                Future[] threads = new Future[nthreads];
                for (int j = 0; j < nthreads; j++) {
                    final long firstIdx = j * k;
                    final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                    threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Callable<Double>()
                    {
                        @Override
                        public Double call()
                        {
                            double sum = 0;
                            double c = 0;
                            for (long k = firstIdx; k < lastIdx; k++) {
                                double y = a.getDouble(k) - c;
                                double t = sum + y;
                                c = (t - sum) - y;
                                sum = t;
                            }
                            return sum;
                        }
                    });
                }
                try {
                    com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    for (int j = 0; j < nthreads; j++) {
                        double res = (double) threads[j].get();
                        sum += res;
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    for (long i = 0; i < length; i++) {
                        double y = a.getDouble(i) - c;
                        double t = sum + y;
                        c = (t - sum) - y;
                        sum = t;
                    }
                }
            }
            return sum;
        }
    }

    /**
     * Mean value of a LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return mean value
     */
    public static double avg(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        if (a == null || !a.isNumeric() || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
            throw new IllegalArgumentException("a == null || !a.isNumeric() || a.getType() == LargeArrayType.COMPLEX_FLOAT || a.getType() == LargeArrayType.COMPLEX_DOUBLE");
        }
        if (a.isConstant()) {
            return a.getDouble(0);
        } else {
            double sum = 0;
            long length = a.length();
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                for (long i = 0; i < length; i++) {
                    sum += a.getDouble(i);
                }
            } else {
                long k = length / nthreads;
                Future[] threads = new Future[nthreads];
                for (int j = 0; j < nthreads; j++) {
                    final long firstIdx = j * k;
                    final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                    threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Callable<Double>()
                    {
                        @Override
                        public Double call()
                        {
                            double sum = 0;
                            for (long k = firstIdx; k < lastIdx; k++) {
                                sum += a.getDouble(k);
                            }
                            return sum;
                        }
                    });
                }
                try {
                    com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    for (int j = 0; j < nthreads; j++) {
                        double res = (double) threads[j].get();
                        sum += res;
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    for (long i = 0; i < length; i++) {
                        sum += a.getDouble(i);
                    }
                }
            }
            return sum / length;
        }
    }

    /**
     * Mean value of a LargeArray computed using Kahan algorithm.
     * <p>
     * @param a input array
     * <p>
     * @return mean value
     */
    public static double avgKahan(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        if (a == null || !a.isNumeric() || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
            throw new IllegalArgumentException("a == null || !a.isNumeric() || a.getType() == LargeArrayType.COMPLEX_FLOAT || a.getType() == LargeArrayType.COMPLEX_DOUBLE");
        }
        if (a.isConstant()) {
            return a.getDouble(0);
        } else {
            double sum = 0;
            double c = 0;
            long length = a.length();
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                for (long i = 0; i < length; i++) {
                    double y = a.getDouble(i) - c;
                    double t = sum + y;
                    c = (t - sum) - y;
                    sum = t;
                }
            } else {
                long k = length / nthreads;
                Future[] threads = new Future[nthreads];
                for (int j = 0; j < nthreads; j++) {
                    final long firstIdx = j * k;
                    final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                    threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Callable<Double>()
                    {
                        @Override
                        public Double call()
                        {
                            double sum = 0;
                            double c = 0;
                            for (long k = firstIdx; k < lastIdx; k++) {
                                double y = a.getDouble(k) - c;
                                double t = sum + y;
                                c = (t - sum) - y;
                                sum = t;
                            }
                            return sum;
                        }
                    });
                }
                try {
                    com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    for (int j = 0; j < nthreads; j++) {
                        double res = (double) threads[j].get();
                        sum += res;
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    for (long i = 0; i < length; i++) {
                        double y = a.getDouble(i) - c;
                        double t = sum + y;
                        c = (t - sum) - y;
                        sum = t;
                    }
                }
            }
            return sum / length;
        }
    }

    /**
     * Standard deviation value of a LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return standard deviation value
     */
    public static double std(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        if (a == null || !a.isNumeric() || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
            throw new IllegalArgumentException("a == null || !a.isNumeric() || a.getType() == LargeArrayType.COMPLEX_FLOAT || a.getType() == LargeArrayType.COMPLEX_DOUBLE");
        }
        if (a.isConstant()) {
            return 0;
        } else {
            double sum = 0;
            double sum2 = 0;
            long length = a.length();
            if (length < 2) return Double.NaN;
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                for (long i = 0; i < length; i++) {
                    double elem = a.getDouble(i);
                    sum += elem;
                    sum2 += (elem * elem);
                }
            } else {
                long k = length / nthreads;
                Future[] threads = new Future[nthreads];
                for (int j = 0; j < nthreads; j++) {
                    final long firstIdx = j * k;
                    final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                    threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Callable<double[]>()
                    {
                        @Override
                        public double[] call()
                        {
                            double[] sum = new double[2];
                            for (long k = firstIdx; k < lastIdx; k++) {
                                double elem = a.getDouble(k);
                                sum[0] += elem;
                                sum[1] += (elem * elem);
                            }
                            return sum;
                        }
                    });
                }
                try {
                    com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    for (int j = 0; j < nthreads; j++) {
                        double[] res = (double[]) threads[j].get();
                        sum += res[0];
                        sum2 += res[1];
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    for (long i = 0; i < length; i++) {
                        double elem = a.getDouble(i);
                        sum += elem;
                        sum2 += (elem * elem);
                    }
                }
            }
            sum /= length;
            sum2 /= length;
            return FastMath.sqrt(FastMath.max(0, sum2 - sum * sum));
        }
    }

    /**
     * Standard deviation value of a LargeArray computed using Kahan algorithm.
     * <p>
     * @param a input array
     * <p>
     * @return standard deviation value
     */
    public static double stdKahan(final LargeArray a)
    {
        if (a == null || !a.isNumeric() || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT || a.getType() == LargeArrayType.COMPLEX_DOUBLE) {
            throw new IllegalArgumentException("a == null || !a.isNumeric() || a.getType() == LargeArrayType.COMPLEX_FLOAT || a.getType() == LargeArrayType.COMPLEX_DOUBLE");
        }
        if (a.isConstant()) {
            return 0;
        } else {
            double sum = 0;
            double sum2 = 0;
            double c1 = 0;
            double c2 = 0;
            long length = a.length();
            if (length < 2) return Double.NaN;
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                for (long i = 0; i < length; i++) {
                    double elem = a.getDouble(i);
                    double y = elem - c1;
                    double t = sum + y;
                    c1 = (t - sum) - y;
                    sum = t;
                    double y2 = elem * elem - c2;
                    double t2 = sum2 + y2;
                    c2 = (t2 - sum2) - y2;
                    sum2 = t2;
                }
            } else {
                long k = length / nthreads;
                Future[] threads = new Future[nthreads];
                for (int j = 0; j < nthreads; j++) {
                    final long firstIdx = j * k;
                    final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                    threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Callable<double[]>()
                    {
                        @Override
                        public double[] call()
                        {
                            double[] sum = new double[2];
                            double c1 = 0;
                            double c2 = 0;
                            for (long k = firstIdx; k < lastIdx; k++) {
                                double elem = a.getDouble(k);
                                double y = elem - c1;
                                double t = sum[0] + y;
                                c1 = (t - sum[0]) - y;
                                sum[0] = t;
                                double y2 = elem * elem - c2;
                                double t2 = sum[1] + y2;
                                c2 = (t2 - sum[1]) - y2;
                                sum[1] = t2;
                            }
                            return sum;
                        }
                    });
                }
                try {
                    ConcurrencyUtils.waitForCompletion(threads);
                    for (int j = 0; j < nthreads; j++) {
                        double[] res = (double[]) threads[j].get();
                        sum += res[0];
                        sum2 += res[1];
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    for (long i = 0; i < length; i++) {
                        double elem = a.getDouble(i);
                        double y = elem - c1;
                        double t = sum + y;
                        c1 = (t - sum) - y;
                        sum = t;
                        double y2 = elem * elem - c2;
                        double t2 = sum2 + y2;
                        c2 = (t2 - sum2) - y2;
                        sum2 = t2;
                    }
                }
            }
            sum /= length;
            sum2 /= length;
            return FastMath.sqrt(FastMath.max(0, sum2 - sum * sum));
        }
    }
}
