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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.math3.util.FastMath;
import com.pl.edu.icm.jlargearrays.ComplexDoubleLargeArray;
import com.pl.edu.icm.jlargearrays.ComplexFloatLargeArray;
import com.pl.edu.icm.jlargearrays.ConcurrencyUtils;
import com.pl.edu.icm.jlargearrays.LargeArray;
import com.pl.edu.icm.jlargearrays.LargeArrayType;
import com.pl.edu.icm.jlargearrays.LargeArrayUtils;

/**
 *
 * Arithmetical operations on LargeArrays.
 *
 * @author Piotr Wendykier (p.wendykier@icm.edu.pl)
 */
public class LargeArrayArithmetics
{

    private LargeArrayArithmetics()
    {
    }

    /**
     * Complex sine.
     * <p>
     * @param a complex number
     * <p>
     * @return sin(a)
     */
    public static float[] complexSin(float[] a)
    {
        float[] res = new float[2];
        res[0] = (float) (FastMath.sin(a[0]) * FastMath.cosh(a[1]));
        res[1] = (float) (FastMath.cos(a[0]) * FastMath.sinh(a[1]));
        return res;
    }

    /**
     * Complex sine.
     * <p>
     * @param a complex number
     * <p>
     * @return sin(a)
     */
    public static double[] complexSin(double[] a)
    {
        double[] res = new double[2];
        res[0] = FastMath.sin(a[0]) * FastMath.cosh(a[1]);
        res[1] = FastMath.cos(a[0]) * FastMath.sinh(a[1]);
        return res;
    }

    /**
     * Complex cosine.
     * <p>
     * @param a complex number
     * <p>
     * @return cos(a)
     */
    public static float[] complexCos(float[] a)
    {
        float[] res = new float[2];
        res[0] = (float) (FastMath.cos(a[0]) * FastMath.cosh(a[1]));
        res[1] = (float) (-FastMath.sin(a[0]) * FastMath.sinh(a[1]));
        return res;
    }

    /**
     * Complex cosine.
     * <p>
     * @param a complex number
     * <p>
     * @return cos(a)
     */
    public static double[] complexCos(double[] a)
    {
        double[] res = new double[2];
        res[0] = FastMath.cos(a[0]) * FastMath.cosh(a[1]);
        res[1] = -FastMath.sin(a[0]) * FastMath.sinh(a[1]);
        return res;
    }

    /**
     * Complex tangent.
     * <p>
     * @param a complex number
     * <p>
     * @return tan(a)
     */
    public static float[] complexTan(float[] a)
    {
        float[] s = complexSin(a);
        float[] c = complexCos(a);
        return complexDiv(s, c);
    }

    /**
     * Complex tangent.
     * <p>
     * @param a complex number
     * <p>
     * @return tan(a)
     */
    public static double[] complexTan(double[] a)
    {
        double[] s = complexSin(a);
        double[] c = complexCos(a);
        return complexDiv(s, c);
    }

    /**
     * Complex addition.
     * <p>
     * @param a complex number
     * @param b complex number
     * <p>
     * @return a+b
     */
    public static float[] complexAdd(float[] a, float[] b)
    {
        float[] res = new float[2];
        res[0] = a[0] + b[0];
        res[1] = a[1] + b[1];
        return res;
    }

    /**
     * Complex addition.
     * <p>
     * @param a complex number
     * @param b complex number
     * <p>
     * @return a+b
     */
    public static double[] complexAdd(double[] a, double[] b)
    {
        double[] res = new double[2];
        res[0] = a[0] + b[0];
        res[1] = a[1] + b[1];
        return res;
    }

    /**
     * Complex subtraction.
     * <p>
     * @param a complex number
     * @param b complex number
     * <p>
     * @return a-b
     */
    public static float[] complexDiff(float[] a, float[] b)
    {
        float[] res = new float[2];
        res[0] = a[0] - b[0];
        res[1] = a[1] - b[1];
        return res;
    }

    /**
     * Complex subtraction.
     * <p>
     * @param a complex number
     * @param b complex number
     * <p>
     * @return a-b
     */
    public static double[] complexDiff(double[] a, double[] b)
    {
        double[] res = new double[2];
        res[0] = a[0] - b[0];
        res[1] = a[1] - b[1];
        return res;
    }

    /**
     * Complex multiplication.
     * <p>
     * @param a complex number
     * @param b complex number
     * <p>
     * @return a*b
     */
    public static float[] complexMult(float[] a, float[] b)
    {
        float[] res = new float[2];
        res[0] = a[0] * b[0] - a[1] * b[1];
        res[1] = a[1] * b[0] + a[0] * b[1];
        return res;
    }

    /**
     * Complex multiplication.
     * <p>
     * @param a complex number
     * @param b complex number
     * <p>
     * @return a*b
     */
    public static double[] complexMult(double[] a, double[] b)
    {
        double[] res = new double[2];
        res[0] = a[0] * b[0] - a[1] * b[1];
        res[1] = a[1] * b[0] + a[0] * b[1];
        return res;
    }

    /**
     * Complex division.
     * <p>
     * @param a complex number
     * @param b complex number
     * <p>
     * @return a/b
     */
    public static float[] complexDiv(float[] a, float[] b)
    {
        float r = b[0] * b[0] + b[1] * b[1];
        float[] res = new float[2];
        res[0] = (a[0] * b[0] + a[1] * b[1]) / r;
        res[1] = (a[1] * b[0] - a[0] * b[1]) / r;
        return res;
    }

    /**
     * Complex division.
     * <p>
     * @param a complex number
     * @param b complex number
     * <p>
     * @return a/b
     */
    public static double[] complexDiv(double[] a, double[] b)
    {
        double r = b[0] * b[0] + b[1] * b[1];
        double[] res = new double[2];
        res[0] = (a[0] * b[0] + a[1] * b[1]) / r;
        res[1] = (a[1] * b[0] - a[0] * b[1]) / r;
        return res;
    }

    /**
     * Complex power.
     * <p>
     * @param a complex number
     * @param n exponent
     * <p>
     * @return a^n
     */
    public static float[] complexPow(float[] a, double n)
    {
        float[] res = new float[2];
        double mod = FastMath.pow(FastMath.sqrt(a[0] * a[0] + a[1] * a[1]), n);
        double arg = FastMath.atan2(a[1], a[0]);
        res[0] = (float) (mod * FastMath.cos(n * arg));
        res[1] = (float) (mod * FastMath.sin(n * arg));
        return res;
    }

    /**
     * Complex power.
     * <p>
     * @param a complex number
     * @param n exponent
     * <p>
     * @return a^n
     */
    public static double[] complexPow(double[] a, double n)
    {
        double[] res = new double[2];
        double mod = FastMath.pow(FastMath.sqrt(a[0] * a[0] + a[1] * a[1]), n);
        double arg = FastMath.atan2(a[1], a[0]);
        res[0] = mod * FastMath.cos(n * arg);
        res[1] = mod * FastMath.sin(n * arg);
        return res;
    }

    /**
     * Complex power.
     * <p>
     * @param a complex number
     * @param n exponent
     * <p>
     * @return a^n
     */
    public static float[] complexPow(float[] a, float[] n)
    {
        return complexExp(complexMult(n, complexLog(a)));
    }

    /**
     * Complex power.
     * <p>
     * @param a complex number
     * @param n exponent
     * <p>
     * @return a^n
     */
    public static double[] complexPow(double[] a, double[] n)
    {
        return complexExp(complexMult(n, complexLog(a)));
    }

    /**
     * Complex square root.
     * <p>
     * @param a complex number
     * <p>
     * @return sqrt(a)
     */
    public static float[] complexSqrt(float[] a)
    {
        float[] res = new float[2];
        double mod = FastMath.sqrt(a[0] * a[0] + a[1] * a[1]);
        res[0] = (float) FastMath.sqrt((a[0] + mod) / 2.0);
        res[1] = (float) (FastMath.signum(a[1]) * FastMath.sqrt((-a[0] + mod) / 2.0));
        return res;
    }

    /**
     * Complex square root.
     * <p>
     * @param a complex number
     * <p>
     * @return sqrt(a)
     */
    public static double[] complexSqrt(double[] a)
    {
        double[] res = new double[2];
        double mod = FastMath.sqrt(a[0] * a[0] + a[1] * a[1]);
        res[0] = FastMath.sqrt((a[0] + mod) / 2.0);
        res[1] = FastMath.signum(a[1]) * FastMath.sqrt((-a[0] + mod) / 2.0);
        return res;
    }

    /**
     * Complex absolute value.
     * <p>
     * @param a complex number
     * <p>
     * @return abs(a)
     */
    public static float complexAbs(float[] a)
    {
        return (float) FastMath.sqrt(a[0] * a[0] + a[1] * a[1]);
    }

    /**
     * Complex absolute value.
     * <p>
     * @param a complex number
     * <p>
     * @return abs(a)
     */
    public static double complexAbs(double[] a)
    {
        return FastMath.sqrt(a[0] * a[0] + a[1] * a[1]);
    }

    /**
     * Complex natural logarithm.
     * <p>
     * @param a complex number
     * <p>
     * @return log(a)
     */
    public static float[] complexLog(float[] a)
    {
        float[] res = new float[2];
        double mod = FastMath.sqrt(a[0] * a[0] + a[1] * a[1]);
        double arg = FastMath.atan2(a[1], a[0]);
        res[0] = (float) FastMath.log(mod);
        res[1] = (float) arg;
        return res;
    }

    /**
     * Complex natural logarithm.
     * <p>
     * @param a complex number
     * <p>
     * @return log(a)
     */
    public static double[] complexLog(double[] a)
    {
        double[] res = new double[2];
        double mod = FastMath.sqrt(a[0] * a[0] + a[1] * a[1]);
        double arg = FastMath.atan2(a[1], a[0]);
        res[0] = FastMath.log(mod);
        res[1] = arg;
        return res;
    }

    /**
     * Complex base-10 logarithm.
     * <p>
     * @param a complex number
     * <p>
     * @return log10(a)
     */
    public static float[] complexLog10(float[] a)
    {
        float[] res = new float[2];
        final double scale = FastMath.log(10.0);
        double mod = FastMath.sqrt(a[0] * a[0] + a[1] * a[1]);
        double arg = FastMath.atan2(a[1], a[0]) / scale;
        res[0] = (float) ((FastMath.log(mod) / scale));
        res[1] = (float) arg;
        return res;
    }

    /**
     * Complex base-10 logarithm.
     * <p>
     * @param a complex number
     * <p>
     * @return log10(a)
     */
    public static double[] complexLog10(double[] a)
    {
        double[] res = new double[2];
        final double scale = FastMath.log(10.0);
        double mod = FastMath.sqrt(a[0] * a[0] + a[1] * a[1]);
        double arg = FastMath.atan2(a[1], a[0]) / scale;
        res[0] = (FastMath.log(mod) / scale);
        res[1] = arg;
        return res;
    }

    /**
     * Complex exponent.
     * <p>
     * @param a complex number
     * <p>
     * @return exp(a)
     */
    public static float[] complexExp(float[] a)
    {
        float[] res = new float[2];
        res[0] = (float) (FastMath.exp(a[0]) * FastMath.cos(a[1]));
        res[1] = (float) (FastMath.exp(a[0]) * FastMath.sin(a[1]));
        return res;
    }

    /**
     * Complex exponent.
     * <p>
     * @param a complex number
     * <p>
     * @return exp(a)
     */
    public static double[] complexExp(double[] a)
    {
        double[] res = new double[2];
        res[0] = FastMath.exp(a[0]) * FastMath.cos(a[1]);
        res[1] = FastMath.exp(a[0]) * FastMath.sin(a[1]);
        return res;
    }

    /**
     * Complex inverse sine.
     * <p>
     * @param a complex number
     * <p>
     * @return asin(a)
     */
    public static float[] complexAsin(float[] a)
    {
        float[] res;
        float[] i = new float[]{0, 1};
        float[] mi = new float[]{0, -1};
        res = complexMult(a, a);
        res[0] = 1 - res[0];
        res[1] = 1 - res[1];
        res = complexLog(res);
        i = complexMult(i, a);
        res[0] += i[0];
        res[1] += i[1];
        return complexMult(mi, res);
    }

    /**
     * Complex inverse sine.
     * <p>
     * @param a complex number
     * <p>
     * @return asin(a)
     */
    public static double[] complexAsin(double[] a)
    {
        double[] res;
        double[] i = new double[]{0, 1};
        double[] mi = new double[]{0, -1};
        res = complexMult(a, a);
        res[0] = 1 - res[0];
        res[1] = 1 - res[1];
        res = complexLog(res);
        i = complexMult(i, a);
        res[0] += i[0];
        res[1] += i[1];
        return complexMult(mi, res);
    }

    /**
     * Complex inverse cosine.
     * <p>
     * @param a complex number
     * <p>
     * @return acos(a)
     */
    public static float[] complexAcos(float[] a)
    {
        float[] res;
        float[] i = new float[]{0, 1};
        float[] mi = new float[]{0, -1};
        res = complexMult(a, a);
        res[0] = 1 - res[0];
        res[1] = 1 - res[1];
        res = complexMult(i, res);
        res[0] += a[0];
        res[1] += a[1];
        res = complexLog(res);
        return complexMult(mi, res);
    }

    /**
     * Complex inverse cosine.
     * <p>
     * @param a complex number
     * <p>
     * @return acos(a)
     */
    public static double[] complexAcos(double[] a)
    {
        double[] res;
        double[] i = new double[]{0, 1};
        double[] mi = new double[]{0, -1};
        res = complexMult(a, a);
        res[0] = 1 - res[0];
        res[1] = 1 - res[1];
        res = complexMult(i, res);
        res[0] += a[0];
        res[1] += a[1];
        res = complexLog(res);
        return complexMult(mi, res);
    }

    /**
     * Complex inverse tangent.
     * <p>
     * @param a complex number
     * <p>
     * @return atan(a)
     */
    public static float[] complexAtan(float[] a)
    {
        float[] res = new float[2];
        float[] tmp = new float[2];
        float[] i = new float[]{0, 1};
        res[0] = i[0] + a[0];
        res[1] = i[1] + a[1];
        tmp[0] = i[0] - a[0];
        tmp[1] = i[1] - a[1];
        res = complexLog(complexDiv(res, tmp));
        i[1] /= 2.0;
        return complexMult(i, res);
    }

    /**
     * Complex inverse tangent.
     * <p>
     * @param a complex number
     * <p>
     * @return atan(a)
     */
    public static double[] complexAtan(double[] a)
    {
        double[] res = new double[2];
        double[] tmp = new double[2];
        double[] i = new double[]{0, 1};
        res[0] = i[0] + a[0];
        res[1] = i[1] + a[1];
        tmp[0] = i[0] - a[0];
        tmp[1] = i[1] - a[1];
        res = complexLog(complexDiv(res, tmp));
        i[1] /= 2.0;
        return complexMult(i, res);
    }

    /**
     * Addition of two LargeArrays.
     * <p>
     * @param a input array
     * @param b input array
     * <p>
     * @return a+b
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray add(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArray b)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().compareTo(b.getType()) >= 0 ? a.getType() : b.getType();
        return add(a, b, out_type);
    }

    /**
     * Addition of two LargeArrays.
     * <p>
     * @param a        input array
     * @param b        input array
     * @param out_type output type
     * <p>
     * @return a+b
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray add(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArray b, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || b == null || a.length() != b.length() || !a.isNumeric() || !b.isNumeric()) {
            throw new IllegalArgumentException("a == null || b == null || a.length() != b.length() || !a.isNumeric() || !b.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant() && b.isConstant()) {
            if (out_type.isIntegerNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, a.getLong(0) + b.getLong(0));
            } else if (out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, a.getDouble(0) + b.getDouble(0));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                float[] elem_b = ((ComplexFloatLargeArray) b).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, new float[]{elem_a[0] + elem_b[0], elem_a[1] + elem_b[1]});
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                double[] elem_b = ((ComplexDoubleLargeArray) b).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, new double[]{elem_a[0] + elem_b[0], elem_a[1] + elem_b[1]});
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType()) {
                res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setLong(i, a.getLong(i) + b.getLong(i));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setLong(k, a.getLong(k) + b.getLong(k));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setLong(i, a.getLong(i) + b.getLong(i));
                        }
                    }
                }
            } else if (out_type.isRealNumericType()) {
                res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, a.getDouble(i) + b.getDouble(i));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, a.getDouble(k) + b.getDouble(k));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, a.getDouble(i) + b.getDouble(i));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(a, out_type);
                final ComplexFloatLargeArray _bc = (ComplexFloatLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(b, out_type);
                if (_ac.getType() == a.getType() && _bc.getType() == b.getType()) {
                    res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                } else if (_ac.getType() != a.getType()) {
                    res = _ac;
                } else {
                    res = _bc;
                }
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    float[] elem_res = new float[2];
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        float[] elem_b = _bc.getComplexFloat(i);
                        elem_res[0] = elem_a[0] + elem_b[0];
                        elem_res[1] = elem_a[1] + elem_b[1];
                        resc.setComplexFloat(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                float[] elem_res = new float[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    float[] elem_b = _bc.getComplexFloat(k);
                                    elem_res[0] = elem_a[0] + elem_b[0];
                                    elem_res[1] = elem_a[1] + elem_b[1];
                                    resc.setComplexFloat(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        float[] elem_res = new float[2];
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            float[] elem_b = _bc.getComplexFloat(i);
                            elem_res[0] = elem_a[0] + elem_b[0];
                            elem_res[1] = elem_a[1] + elem_b[1];
                            resc.setComplexFloat(i, elem_res);
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(a, out_type);
                final ComplexDoubleLargeArray _bc = (ComplexDoubleLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(b, out_type);
                if (_ac.getType() == a.getType() && _bc.getType() == b.getType()) {
                    res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                } else if (_ac.getType() != a.getType()) {
                    res = _ac;
                } else {
                    res = _bc;
                }
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    double[] elem_res = new double[2];
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        double[] elem_b = _bc.getComplexDouble(i);
                        elem_res[0] = elem_a[0] + elem_b[0];
                        elem_res[1] = elem_a[1] + elem_b[1];
                        resc.setComplexDouble(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                double[] elem_res = new double[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    double[] elem_b = _bc.getComplexDouble(k);
                                    elem_res[0] = elem_a[0] + elem_b[0];
                                    elem_res[1] = elem_a[1] + elem_b[1];
                                    resc.setComplexDouble(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        double[] elem_res = new double[2];
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            double[] elem_b = _bc.getComplexDouble(i);
                            elem_res[0] = elem_a[0] + elem_b[0];
                            elem_res[1] = elem_a[1] + elem_b[1];
                            resc.setComplexDouble(i, elem_res);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Subtraction of two LargeArrays.
     * <p>
     * @param a input array
     * @param b input array
     * <p>
     * @return a-b
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray diff(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArray b)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().compareTo(b.getType()) >= 0 ? a.getType() : b.getType();
        return diff(a, b, out_type);
    }

    /**
     * Subtraction of two LargeArrays.
     * <p>
     * @param a        input array
     * @param b        input array
     * @param out_type output type
     * <p>
     * @return a-b
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray diff(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArray b, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || b == null || a.length() != b.length() || !a.isNumeric() || !b.isNumeric()) {
            throw new IllegalArgumentException("a == null || b == null || a.length() != b.length() || !a.isNumeric() || !b.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant() && b.isConstant()) {
            if (out_type.isIntegerNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, a.getLong(0) - b.getLong(0));
            } else if (out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, a.getDouble(0) - b.getDouble(0));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                float[] elem_b = ((ComplexFloatLargeArray) b).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, new float[]{elem_a[0] - elem_b[0], elem_a[1] - elem_b[1]});
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                double[] elem_b = ((ComplexDoubleLargeArray) b).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, new double[]{elem_a[0] - elem_b[0], elem_a[1] - elem_b[1]});
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType()) {
                res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setLong(i, a.getLong(i) - b.getLong(i));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setLong(k, a.getLong(k) - b.getLong(k));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setLong(i, a.getLong(i) - b.getLong(i));
                        }
                    }
                }
            } else if (out_type.isRealNumericType()) {
                res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, a.getDouble(i) - b.getDouble(i));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, a.getDouble(k) - b.getDouble(k));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, a.getDouble(i) - b.getDouble(i));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(a, out_type);
                final ComplexFloatLargeArray _bc = (ComplexFloatLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(b, out_type);
                if (_ac.getType() == a.getType() && _bc.getType() == b.getType()) {
                    res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                } else if (_ac.getType() != a.getType()) {
                    res = _ac;
                } else {
                    res = _bc;
                }
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    float[] elem_res = new float[2];
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        float[] elem_b = _bc.getComplexFloat(i);
                        elem_res[0] = elem_a[0] - elem_b[0];
                        elem_res[1] = elem_a[1] - elem_b[1];
                        resc.setComplexFloat(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                float[] elem_res = new float[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    float[] elem_b = _bc.getComplexFloat(k);
                                    elem_res[0] = elem_a[0] - elem_b[0];
                                    elem_res[1] = elem_a[1] - elem_b[1];
                                    resc.setComplexFloat(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        float[] elem_res = new float[2];
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            float[] elem_b = _bc.getComplexFloat(i);
                            elem_res[0] = elem_a[0] - elem_b[0];
                            elem_res[1] = elem_a[1] - elem_b[1];
                            resc.setComplexFloat(i, elem_res);
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(a, out_type);
                final ComplexDoubleLargeArray _bc = (ComplexDoubleLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(b, out_type);
                if (_ac.getType() == a.getType() && _bc.getType() == b.getType()) {
                    res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                } else if (_ac.getType() != a.getType()) {
                    res = _ac;
                } else {
                    res = _bc;
                }
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    double[] elem_res = new double[2];
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        double[] elem_b = _bc.getComplexDouble(i);
                        elem_res[0] = elem_a[0] - elem_b[0];
                        elem_res[1] = elem_a[1] - elem_b[1];
                        resc.setComplexDouble(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                double[] elem_res = new double[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    double[] elem_b = _bc.getComplexDouble(k);
                                    elem_res[0] = elem_a[0] - elem_b[0];
                                    elem_res[1] = elem_a[1] - elem_b[1];
                                    resc.setComplexDouble(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        double[] elem_res = new double[2];
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            double[] elem_b = _bc.getComplexDouble(i);
                            elem_res[0] = elem_a[0] - elem_b[0];
                            elem_res[1] = elem_a[1] - elem_b[1];
                            resc.setComplexDouble(i, elem_res);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Multiplication of two LargeArrays.
     * <p>
     * @param a input array
     * @param b input array
     * <p>
     * @return a*b
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray mult(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArray b)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().compareTo(b.getType()) >= 0 ? a.getType() : b.getType();
        return mult(a, b, out_type);
    }

    /**
     * Multiplication of two LargeArrays.
     * <p>
     * @param a        input array
     * @param b        input array
     * @param out_type output type
     * <p>
     * @return a*b
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray mult(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArray b, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || b == null || a.length() != b.length() || !a.isNumeric() || !b.isNumeric()) {
            throw new IllegalArgumentException("a == null || b == null || a.length() != b.length() || !a.isNumeric() || !b.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant() && b.isConstant()) {
            if (out_type.isIntegerNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, a.getLong(0) * b.getLong(0));
            } else if (out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, a.getDouble(0) * b.getDouble(0));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                float[] elem_b = ((ComplexFloatLargeArray) b).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexMult(elem_a, elem_b));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                double[] elem_b = ((ComplexDoubleLargeArray) b).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexMult(elem_a, elem_b));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType()) {
                res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setLong(i, a.getLong(i) * b.getLong(i));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setLong(k, a.getLong(k) * b.getLong(k));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setLong(i, a.getLong(i) * b.getLong(i));
                        }
                    }
                }
            } else if (out_type.isRealNumericType()) {
                res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, a.getDouble(i) * b.getDouble(i));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, a.getDouble(k) * b.getDouble(k));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, a.getDouble(i) * b.getDouble(i));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(a, out_type);
                final ComplexFloatLargeArray _bc = (ComplexFloatLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(b, out_type);
                if (_ac.getType() == a.getType() && _bc.getType() == b.getType()) {
                    res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                } else if (_ac.getType() != a.getType()) {
                    res = _ac;
                } else {
                    res = _bc;
                }
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    float[] elem_res = new float[2];
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        float[] elem_b = _bc.getComplexFloat(i);
                        elem_res[0] = elem_a[0] * elem_b[0] - elem_a[1] * elem_b[1];
                        elem_res[1] = elem_a[1] * elem_b[0] + elem_a[0] * elem_b[1];
                        resc.setComplexFloat(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                float[] elem_res = new float[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    float[] elem_b = _bc.getComplexFloat(k);
                                    elem_res[0] = elem_a[0] * elem_b[0] - elem_a[1] * elem_b[1];
                                    elem_res[1] = elem_a[1] * elem_b[0] + elem_a[0] * elem_b[1];
                                    resc.setComplexFloat(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        float[] elem_res = new float[2];
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            float[] elem_b = _bc.getComplexFloat(i);
                            elem_res[0] = elem_a[0] * elem_b[0] - elem_a[1] * elem_b[1];
                            elem_res[1] = elem_a[1] * elem_b[0] + elem_a[0] * elem_b[1];
                            resc.setComplexFloat(i, elem_res);
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(a, out_type);
                final ComplexDoubleLargeArray _bc = (ComplexDoubleLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(b, out_type);
                if (_ac.getType() == a.getType() && _bc.getType() == b.getType()) {
                    res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                } else if (_ac.getType() != a.getType()) {
                    res = _ac;
                } else {
                    res = _bc;
                }
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    double[] elem_res = new double[2];
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        double[] elem_b = _bc.getComplexDouble(i);
                        elem_res[0] = elem_a[0] * elem_b[0] - elem_a[1] * elem_b[1];
                        elem_res[1] = elem_a[1] * elem_b[0] + elem_a[0] * elem_b[1];
                        resc.setComplexDouble(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                double[] elem_res = new double[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    double[] elem_b = _bc.getComplexDouble(k);
                                    elem_res[0] = elem_a[0] * elem_b[0] - elem_a[1] * elem_b[1];
                                    elem_res[1] = elem_a[1] * elem_b[0] + elem_a[0] * elem_b[1];
                                    resc.setComplexDouble(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        double[] elem_res = new double[2];
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            double[] elem_b = _bc.getComplexDouble(i);
                            elem_res[0] = elem_a[0] * elem_b[0] - elem_a[1] * elem_b[1];
                            elem_res[1] = elem_a[1] * elem_b[0] + elem_a[0] * elem_b[1];
                            resc.setComplexDouble(i, elem_res);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Division of two LargeArrays.
     * <p>
     * @param a input array
     * @param b input array
     * <p>
     * @return a/b
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray div(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArray b)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().compareTo(b.getType()) >= 0 ? a.getType() : b.getType();
        return div(a, b, out_type);
    }

    /**
     * Division of two LargeArrays.
     * <p>
     * @param a        input array
     * @param b        input array
     * @param out_type output type
     * <p>
     * @return a/b
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray div(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArray b, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || b == null || a.length() != b.length() || !a.isNumeric() || !b.isNumeric()) {
            throw new IllegalArgumentException("a == null || b == null || a.length() != b.length() || !a.isNumeric() || !b.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant() && b.isConstant()) {
            if (out_type.isIntegerNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, a.getLong(0) / b.getLong(0));
            } else if (out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, a.getDouble(0) / b.getDouble(0));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                float[] elem_b = ((ComplexFloatLargeArray) b).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexDiv(elem_a, elem_b));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                double[] elem_b = ((ComplexDoubleLargeArray) b).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexDiv(elem_a, elem_b));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType()) {
                res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setLong(i, a.getLong(i) / b.getLong(i));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setLong(k, a.getLong(k) / b.getLong(k));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setLong(i, a.getLong(i) / b.getLong(i));
                        }
                    }
                }
            } else if (out_type.isRealNumericType()) {
                res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, a.getDouble(i) / b.getDouble(i));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, a.getDouble(k) / b.getDouble(k));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, a.getDouble(i) / b.getDouble(i));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(a, out_type);
                final ComplexFloatLargeArray _bc = (ComplexFloatLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(b, out_type);
                if (_ac.getType() == a.getType() && _bc.getType() == b.getType()) {
                    res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                } else if (_ac.getType() != a.getType()) {
                    res = _ac;
                } else {
                    res = _bc;
                }
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    float[] elem_res = new float[2];
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        float[] elem_b = _bc.getComplexFloat(i);
                        float r = elem_b[0] * elem_b[0] + elem_b[1] * elem_b[1];
                        elem_res[0] = (elem_a[0] * elem_b[0] + elem_a[1] * elem_b[1]) / r;
                        elem_res[1] = (elem_a[1] * elem_b[0] - elem_a[0] * elem_b[1]) / r;
                        resc.setComplexFloat(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                float[] elem_res = new float[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    float[] elem_b = _bc.getComplexFloat(k);
                                    float r = elem_b[0] * elem_b[0] + elem_b[1] * elem_b[1];
                                    elem_res[0] = (elem_a[0] * elem_b[0] + elem_a[1] * elem_b[1]) / r;
                                    elem_res[1] = (elem_a[1] * elem_b[0] - elem_a[0] * elem_b[1]) / r;
                                    resc.setComplexFloat(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        float[] elem_res = new float[2];
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            float[] elem_b = _bc.getComplexFloat(i);
                            float r = elem_b[0] * elem_b[0] + elem_b[1] * elem_b[1];
                            elem_res[0] = (elem_a[0] * elem_b[0] + elem_a[1] * elem_b[1]) / r;
                            elem_res[1] = (elem_a[1] * elem_b[0] - elem_a[0] * elem_b[1]) / r;
                            resc.setComplexFloat(i, elem_res);
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(a, out_type);
                final ComplexDoubleLargeArray _bc = (ComplexDoubleLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(b, out_type);
                if (_ac.getType() == a.getType() && _bc.getType() == b.getType()) {
                    res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                } else if (_ac.getType() != a.getType()) {
                    res = _ac;
                } else {
                    res = _bc;
                }
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    double[] elem_res = new double[2];
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        double[] elem_b = _bc.getComplexDouble(i);
                        double r = elem_b[0] * elem_b[0] + elem_b[1] * elem_b[1];
                        elem_res[0] = (elem_a[0] * elem_b[0] + elem_a[1] * elem_b[1]) / r;
                        elem_res[1] = (elem_a[1] * elem_b[0] - elem_a[0] * elem_b[1]) / r;
                        resc.setComplexDouble(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                double[] elem_res = new double[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    double[] elem_b = _bc.getComplexDouble(k);
                                    double r = elem_b[0] * elem_b[0] + elem_b[1] * elem_b[1];
                                    elem_res[0] = (elem_a[0] * elem_b[0] + elem_a[1] * elem_b[1]) / r;
                                    elem_res[1] = (elem_a[1] * elem_b[0] - elem_a[0] * elem_b[1]) / r;
                                    resc.setComplexDouble(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        double[] elem_res = new double[2];
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            double[] elem_b = _bc.getComplexDouble(i);
                            double r = elem_b[0] * elem_b[0] + elem_b[1] * elem_b[1];
                            elem_res[0] = (elem_a[0] * elem_b[0] + elem_a[1] * elem_b[1]) / r;
                            elem_res[1] = (elem_a[1] * elem_b[0] - elem_a[0] * elem_b[1]) / r;
                            resc.setComplexDouble(i, elem_res);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Power of LargeArray.
     * <p>
     * @param a input array
     * @param n exponent
     * <p>
     * @return a^n
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray pow(final com.pl.edu.icm.jlargearrays.LargeArray a, final double n)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().isIntegerNumericType() ? com.pl.edu.icm.jlargearrays.LargeArrayType.FLOAT : a.getType();
        return pow(a, n, out_type);
    }

    /**
     * Power of LargeArray.
     * <p>
     * @param a        input array
     * @param n        exponent
     * @param out_type output type
     * <p>
     * @return a^n
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray pow(final com.pl.edu.icm.jlargearrays.LargeArray a, final double n, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric()) {
            throw new IllegalArgumentException("a == null || !a.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant()) {
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.pow(a.getDouble(0), n));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexPow(elem_a, n));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexPow(elem_a, n));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.pow(a.getDouble(i), n));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, FastMath.pow(a.getDouble(k), n));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.pow(a.getDouble(i), n));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) a;
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    float[] elem_res = new float[2];
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        double mod = FastMath.pow(FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]), n);
                        double arg = FastMath.atan2(elem_a[1], elem_a[0]);
                        elem_res[0] = (float) (mod * FastMath.cos(n * arg));
                        elem_res[1] = (float) (mod * FastMath.sin(n * arg));
                        resc.setComplexFloat(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                float[] elem_res = new float[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    double mod = FastMath.pow(FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]), n);
                                    double arg = FastMath.atan2(elem_a[1], elem_a[0]);
                                    elem_res[0] = (float) (mod * FastMath.cos(n * arg));
                                    elem_res[1] = (float) (mod * FastMath.sin(n * arg));
                                    resc.setComplexFloat(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        float[] elem_res = new float[2];
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            double mod = FastMath.pow(FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]), n);
                            double arg = FastMath.atan2(elem_a[1], elem_a[0]);
                            elem_res[0] = (float) (mod * FastMath.cos(n * arg));
                            elem_res[1] = (float) (mod * FastMath.sin(n * arg));
                            resc.setComplexFloat(i, elem_res);
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) a;
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    double[] elem_res = new double[2];
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        double mod = FastMath.pow(FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]), n);
                        double arg = FastMath.atan2(elem_a[1], elem_a[0]);
                        elem_res[0] = mod * FastMath.cos(n * arg);
                        elem_res[1] = mod * FastMath.sin(n * arg);
                        resc.setComplexDouble(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                double[] elem_res = new double[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    double mod = FastMath.pow(FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]), n);
                                    double arg = FastMath.atan2(elem_a[1], elem_a[0]);
                                    elem_res[0] = mod * FastMath.cos(n * arg);
                                    elem_res[1] = mod * FastMath.sin(n * arg);
                                    resc.setComplexDouble(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        double[] elem_res = new double[2];
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            double mod = FastMath.pow(FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]), n);
                            double arg = FastMath.atan2(elem_a[1], elem_a[0]);
                            elem_res[0] = mod * FastMath.cos(n * arg);
                            elem_res[1] = mod * FastMath.sin(n * arg);
                            resc.setComplexDouble(i, elem_res);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Power of two LargeArrays.
     * <p>
     * @param a input array
     * @param b input array
     * <p>
     * @return a^b
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray pow(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArray b)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().compareTo(b.getType()) >= 0 ? a.getType() : b.getType();
        if (out_type.isIntegerNumericType()) {
            out_type = com.pl.edu.icm.jlargearrays.LargeArrayType.FLOAT;
        }
        return pow(a, b, out_type);
    }

    /**
     * Power of two LargeArrays.
     * <p>
     * @param a        input array
     * @param b        input array
     * @param out_type output type
     *
     * @return a^b
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray pow(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArray b, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || b == null || a.length() != b.length() || !a.isNumeric() || !b.isNumeric()) {
            throw new IllegalArgumentException("a == null || b == null || a.length() != b.length() || !a.isNumeric() || !b.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant() && b.isConstant()) {
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.pow(a.getDouble(0), b.getDouble(0)));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                float[] elem_b = ((ComplexFloatLargeArray) b).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexPow(elem_a, elem_b));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                double[] elem_b = ((ComplexDoubleLargeArray) b).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexPow(elem_a, elem_b));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.pow(a.getDouble(i), b.getDouble(i)));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, FastMath.pow(a.getDouble(k), b.getDouble(k)));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.pow(a.getDouble(i), b.getDouble(i)));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(a, out_type);
                final ComplexFloatLargeArray _bc = (ComplexFloatLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(b, out_type);
                if (_ac.getType() == a.getType() && _bc.getType() == b.getType()) {
                    res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                } else if (_ac.getType() != a.getType()) {
                    res = _ac;
                } else {
                    res = _bc;
                }
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        float[] elem_b = _bc.getComplexFloat(i);
                        resc.setComplexFloat(i, complexPow(elem_a, elem_b));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    float[] elem_b = _bc.getComplexFloat(k);
                                    resc.setComplexFloat(k, complexPow(elem_a, elem_b));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            float[] elem_b = _bc.getComplexFloat(i);
                            resc.setComplexFloat(i, complexPow(elem_a, elem_b));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(a, out_type);
                final ComplexDoubleLargeArray _bc = (ComplexDoubleLargeArray) com.pl.edu.icm.jlargearrays.LargeArrayUtils.convert(b, out_type);
                if (_ac.getType() == a.getType() && _bc.getType() == b.getType()) {
                    res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
                } else if (_ac.getType() != a.getType()) {
                    res = _ac;
                } else {
                    res = _bc;
                }
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        double[] elem_b = _bc.getComplexDouble(i);
                        resc.setComplexDouble(i, complexPow(elem_a, elem_b));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    double[] elem_b = _bc.getComplexDouble(k);
                                    resc.setComplexDouble(k, complexPow(elem_a, elem_b));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            double[] elem_b = _bc.getComplexDouble(i);
                            resc.setComplexDouble(i, complexPow(elem_a, elem_b));
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Negation of LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return -a
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray neg(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType();
        return neg(a, out_type);
    }

    /**
     * Negation of LargeArray.
     * <p>
     * @param a        input array
     * @param out_type output type
     * <p>
     * @return -a
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray neg(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric()) {
            throw new IllegalArgumentException("a == null || !a.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant()) {
            if (out_type.isIntegerNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, -a.getLong(0));
            } else if (out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, -a.getDouble(0));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, new float[]{-elem_a[0], -elem_a[1]});
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, new double[]{-elem_a[0], -elem_a[1]});
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setLong(i, -a.getLong(i));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setLong(k, -a.getLong(k));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setLong(i, -a.getLong(i));
                        }
                    }
                }
            } else if (out_type.isRealNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, -a.getDouble(i));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, -a.getDouble(k));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, -a.getDouble(i));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) a;
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    float[] elem_res = new float[2];
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        elem_res[0] = -elem_a[0];
                        elem_res[1] = -elem_a[1];
                        resc.setComplexFloat(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                float[] elem_res = new float[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    elem_res[0] = -elem_a[0];
                                    elem_res[1] = -elem_a[1];
                                    resc.setComplexFloat(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        float[] elem_res = new float[2];
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            elem_res[0] = -elem_a[0];
                            elem_res[1] = -elem_a[1];
                            resc.setComplexFloat(i, elem_res);
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) a;
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    double[] elem_res = new double[2];
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        elem_res[0] = -elem_a[0];
                        elem_res[1] = -elem_a[1];
                        resc.setComplexDouble(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                double[] elem_res = new double[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    elem_res[0] = -elem_a[0];
                                    elem_res[1] = -elem_a[1];
                                    resc.setComplexDouble(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        double[] elem_res = new double[2];
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            elem_res[0] = -elem_a[0];
                            elem_res[1] = -elem_a[1];
                            resc.setComplexDouble(i, elem_res);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Square root of LargeArray. For complex arrays the principal square root is returned.
     * <p>
     * @param a input array
     * <p>
     * @return sqrt(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray sqrt(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().isIntegerNumericType() ? com.pl.edu.icm.jlargearrays.LargeArrayType.FLOAT : a.getType();
        return sqrt(a, out_type);
    }

    /**
     * Square root of LargeArray. For complex arrays the principal square root is returned.
     * <p>
     * @param a        input array
     * @param out_type output type
     * <p>
     * @return sqrt(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray sqrt(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric()) {
            throw new IllegalArgumentException("a == null || !a.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant()) {
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.sqrt(a.getDouble(0)));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexSqrt(elem_a));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexSqrt(elem_a));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.sqrt(a.getDouble(i)));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, FastMath.sqrt(a.getDouble(k)));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.sqrt(a.getDouble(i)));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) a;
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    float[] elem_res = new float[2];
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                        elem_res[0] = (float) (FastMath.sqrt((elem_a[0] + mod) / 2.0));
                        elem_res[1] = (float) (FastMath.signum(elem_a[1]) * FastMath.sqrt((-elem_a[0] + mod) / 2.0));
                        resc.setComplexFloat(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                float[] elem_res = new float[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                                    elem_res[0] = (float) (FastMath.sqrt((elem_a[0] + mod) / 2.0));
                                    elem_res[1] = (float) (FastMath.signum(elem_a[1]) * FastMath.sqrt((-elem_a[0] + mod) / 2.0));
                                    resc.setComplexFloat(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        float[] elem_res = new float[2];
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                            elem_res[0] = (float) (FastMath.sqrt((elem_a[0] + mod) / 2.0));
                            elem_res[1] = (float) (FastMath.signum(elem_a[1]) * FastMath.sqrt((-elem_a[0] + mod) / 2.0));
                            resc.setComplexFloat(i, elem_res);
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) a;
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    double[] elem_res = new double[2];
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                        elem_res[0] = FastMath.sqrt((elem_a[0] + mod) / 2.0);
                        elem_res[1] = FastMath.signum(elem_a[1]) * FastMath.sqrt((-elem_a[0] + mod) / 2.0);
                        resc.setComplexDouble(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                double[] elem_res = new double[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                                    elem_res[0] = FastMath.sqrt((elem_a[0] + mod) / 2.0);
                                    elem_res[1] = FastMath.signum(elem_a[1]) * FastMath.sqrt((-elem_a[0] + mod) / 2.0);
                                    resc.setComplexDouble(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        double[] elem_res = new double[2];
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                            elem_res[0] = FastMath.sqrt((elem_a[0] + mod) / 2.0);
                            elem_res[1] = FastMath.signum(elem_a[1]) * FastMath.sqrt((-elem_a[0] + mod) / 2.0);
                            resc.setComplexDouble(i, elem_res);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Natural logarithm of LargeArray. For complex arrays the principal value logarithm is returned.
     * <p>
     * @param a input array
     * <p>
     * @return log(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray log(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().isIntegerNumericType() ? com.pl.edu.icm.jlargearrays.LargeArrayType.FLOAT : a.getType();
        return log(a, out_type);
    }

    /**
     * Natural logarithm of LargeArray. For complex arrays the principal value logarithm is returned.
     * <p>
     * @param a        input array
     * @param out_type output type
     * <p>
     * @return log(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray log(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric()) {
            throw new IllegalArgumentException("a == null || !a.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();

        if (a.isConstant()) {
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.log(a.getDouble(0)));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexLog(elem_a));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexLog(elem_a));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.log(a.getDouble(i)));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, FastMath.log(a.getDouble(k)));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.log(a.getDouble(i)));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) a;
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    float[] elem_res = new float[2];
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                        double arg = FastMath.atan2(elem_a[1], elem_a[0]);
                        elem_res[0] = (float) (FastMath.log(mod));
                        elem_res[1] = (float) arg;
                        resc.setComplexFloat(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                float[] elem_res = new float[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                                    double arg = FastMath.atan2(elem_a[1], elem_a[0]);
                                    elem_res[0] = (float) (FastMath.log(mod));
                                    elem_res[1] = (float) arg;
                                    resc.setComplexFloat(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        float[] elem_res = new float[2];
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                            double arg = FastMath.atan2(elem_a[1], elem_a[0]);
                            elem_res[0] = (float) (FastMath.log(mod));
                            elem_res[1] = (float) arg;
                            resc.setComplexFloat(i, elem_res);
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) a;
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    double[] elem_res = new double[2];
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                        double arg = FastMath.atan2(elem_a[1], elem_a[0]);
                        elem_res[0] = FastMath.log(mod);
                        elem_res[1] = arg;
                        resc.setComplexDouble(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                double[] elem_res = new double[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                                    double arg = FastMath.atan2(elem_a[1], elem_a[0]);
                                    elem_res[0] = FastMath.log(mod);
                                    elem_res[1] = arg;
                                    resc.setComplexDouble(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        double[] elem_res = new double[2];
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                            double arg = FastMath.atan2(elem_a[1], elem_a[0]);
                            elem_res[0] = FastMath.log(mod);
                            elem_res[1] = arg;
                            resc.setComplexDouble(i, elem_res);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Base-10 logarithm of LargeArray. For complex arrays the principal value logarithm is returned.
     * <p>
     * @param a input array
     * <p>
     * @return log10(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray log10(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().isIntegerNumericType() ? com.pl.edu.icm.jlargearrays.LargeArrayType.FLOAT : a.getType();
        return log10(a, out_type);
    }

    /**
     * Base-10 logarithm of LargeArray. For complex arrays the principal value logarithm is returned.
     * <p>
     * @param a        input array
     * <p>
     * @param out_type output type
     * <p>
     * @return log10(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray log10(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric()) {
            throw new IllegalArgumentException("a == null || !a.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant()) {
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.log10(a.getDouble(0)));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexLog10(elem_a));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexLog10(elem_a));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.log10(a.getDouble(i)));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, FastMath.log10(a.getDouble(k)));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.log10(a.getDouble(i)));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final double scale = FastMath.log(10.0);
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) a;
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    float[] elem_res = new float[2];
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                        double arg = FastMath.atan2(elem_a[1], elem_a[0]) / scale;
                        elem_res[0] = (float) (FastMath.log(mod) / scale);
                        elem_res[1] = (float) arg;
                        resc.setComplexFloat(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                float[] elem_res = new float[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                                    double arg = FastMath.atan2(elem_a[1], elem_a[0]) / scale;
                                    elem_res[0] = (float) (FastMath.log(mod) / scale);
                                    elem_res[1] = (float) arg;
                                    resc.setComplexFloat(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        float[] elem_res = new float[2];
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                            double arg = FastMath.atan2(elem_a[1], elem_a[0]) / scale;
                            elem_res[0] = (float) (FastMath.log(mod) / scale);
                            elem_res[1] = (float) arg;
                            resc.setComplexFloat(i, elem_res);
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final double scale = FastMath.log(10.0);
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) a;
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    double[] elem_res = new double[2];
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                        double arg = FastMath.atan2(elem_a[1], elem_a[0]) / scale;
                        elem_res[0] = (FastMath.log(mod) / scale);
                        elem_res[1] = arg;
                        resc.setComplexDouble(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                double[] elem_res = new double[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                                    double arg = FastMath.atan2(elem_a[1], elem_a[0]) / scale;
                                    elem_res[0] = (FastMath.log(mod) / scale);
                                    elem_res[1] = arg;
                                    resc.setComplexDouble(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        double[] elem_res = new double[2];
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            double mod = FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]);
                            double arg = FastMath.atan2(elem_a[1], elem_a[0]) / scale;
                            elem_res[0] = (FastMath.log(mod) / scale);
                            elem_res[1] = arg;
                            resc.setComplexDouble(i, elem_res);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Exponent of LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return exp(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray exp(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().isIntegerNumericType() ? com.pl.edu.icm.jlargearrays.LargeArrayType.FLOAT : a.getType();
        return exp(a, out_type);
    }

    /**
     * Exponent of LargeArray.
     * <p>
     * @param a        input array
     * @param out_type output type
     * <p>
     * @return exp(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray exp(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric()) {
            throw new IllegalArgumentException("a == null || !a.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant()) {
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.exp(a.getDouble(0)));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexExp(elem_a));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexExp(elem_a));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.exp(a.getDouble(i)));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, FastMath.exp(a.getDouble(k)));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.exp(a.getDouble(i)));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) a;
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    float[] elem_res = new float[2];
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        elem_res[0] = (float) (FastMath.exp(elem_a[0]) * FastMath.cos(elem_a[1]));
                        elem_res[1] = (float) (FastMath.exp(elem_a[0]) * FastMath.sin(elem_a[1]));
                        resc.setComplexFloat(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                float[] elem_res = new float[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    elem_res[0] = (float) (FastMath.exp(elem_a[0]) * FastMath.cos(elem_a[1]));
                                    elem_res[1] = (float) (FastMath.exp(elem_a[0]) * FastMath.sin(elem_a[1]));
                                    resc.setComplexFloat(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        float[] elem_res = new float[2];
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            elem_res[0] = (float) (FastMath.exp(elem_a[0]) * FastMath.cos(elem_a[1]));
                            elem_res[1] = (float) (FastMath.exp(elem_a[0]) * FastMath.sin(elem_a[1]));
                            resc.setComplexFloat(i, elem_res);
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) a;
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    double[] elem_res = new double[2];
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        elem_res[0] = FastMath.exp(elem_a[0]) * FastMath.cos(elem_a[1]);
                        elem_res[1] = FastMath.exp(elem_a[0]) * FastMath.sin(elem_a[1]);
                        resc.setComplexDouble(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                double[] elem_res = new double[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    elem_res[0] = FastMath.exp(elem_a[0]) * FastMath.cos(elem_a[1]);
                                    elem_res[1] = FastMath.exp(elem_a[0]) * FastMath.sin(elem_a[1]);
                                    resc.setComplexDouble(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        double[] elem_res = new double[2];
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            elem_res[0] = FastMath.exp(elem_a[0]) * FastMath.cos(elem_a[1]);
                            elem_res[1] = FastMath.exp(elem_a[0]) * FastMath.sin(elem_a[1]);
                            resc.setComplexDouble(i, elem_res);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Absolute value of LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return |a|
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray abs(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT ? com.pl.edu.icm.jlargearrays.LargeArrayType.FLOAT : a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE ? com.pl.edu.icm.jlargearrays.LargeArrayType.DOUBLE : a.getType();
        return abs(a, out_type);
    }

    /**
     * Absolute value of LargeArray.
     * <p>
     * @param a        input array
     * @param out_type output type
     * <p>
     * @return |a|
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray abs(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric()) {
            throw new IllegalArgumentException("a == null || !a.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();

        if (a.isConstant()) {
            if (out_type.isIntegerNumericType() ) {
                if (a.getType().isComplexNumericType()) {
                    double[] elem;
                    if (a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                        elem = ((ComplexFloatLargeArray) a).getComplexDouble(0);
                    } else {
                        elem = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                    }
                    return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.sqrt(elem[0] * elem[0] + elem[1] * elem[1]));
                } else {
                    return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.abs(a.getLong(0)));
                }
            } else if (out_type.isRealNumericType()) {
                if (a.getType().isComplexNumericType()) {
                    double[] elem;
                    if (a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                        elem = ((ComplexFloatLargeArray) a).getComplexDouble(0);
                    } else {
                        elem = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                    }
                    return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.sqrt(elem[0] * elem[0] + elem[1] * elem[1]));
                } else {
                    return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.abs(a.getDouble(0)));
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (a.getType().isIntegerNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.abs(a.getLong(i)));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setLong(k, FastMath.abs(a.getLong(k)));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.abs(a.getDouble(i)));
                        }
                    }
                }
            } else if (a.getType().isRealNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.abs(a.getDouble(i)));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, FastMath.abs(a.getDouble(k)));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.abs(a.getDouble(i)));
                        }
                    }
                }
            } else if (a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) a;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        res.setDouble(i, FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    res.setDouble(k, FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            res.setDouble(i, FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]));
                        }
                    }
                }
            } else if (a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) a;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        res.setDouble(i, FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    res.setDouble(k, FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            res.setDouble(i, FastMath.sqrt(elem_a[0] * elem_a[0] + elem_a[1] * elem_a[1]));
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Sine of LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return sin(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray sin(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().isIntegerNumericType() ? com.pl.edu.icm.jlargearrays.LargeArrayType.FLOAT : a.getType();
        return sin(a, out_type);
    }

    /**
     * Sine of LargeArray.
     * <p>
     * @param a        input array
     * @param out_type output type
     * <p>
     * @return sin(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray sin(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric()) {
            throw new IllegalArgumentException("a == null || !a.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant()) {
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.sin(a.getDouble(0)));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexSin(elem_a));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexSin(elem_a));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.sin(a.getDouble(i)));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, FastMath.sin(a.getDouble(k)));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.sin(a.getDouble(i)));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) a;
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    float[] elem_res = new float[2];
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        elem_res[0] = (float) (FastMath.sin(elem_a[0]) * FastMath.cosh(elem_a[1]));
                        elem_res[1] = (float) (FastMath.cos(elem_a[0]) * FastMath.sinh(elem_a[1]));
                        resc.setComplexFloat(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                float[] elem_res = new float[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    elem_res[0] = (float) (FastMath.sin(elem_a[0]) * FastMath.cosh(elem_a[1]));
                                    elem_res[1] = (float) (FastMath.cos(elem_a[0]) * FastMath.sinh(elem_a[1]));

                                    resc.setComplexFloat(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        float[] elem_res = new float[2];
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            elem_res[0] = (float) (FastMath.sin(elem_a[0]) * FastMath.cosh(elem_a[1]));
                            elem_res[1] = (float) (FastMath.cos(elem_a[0]) * FastMath.sinh(elem_a[1]));
                            resc.setComplexFloat(i, elem_res);
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) a;
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    double[] elem_res = new double[2];
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        elem_res[0] = FastMath.sin(elem_a[0]) * FastMath.cosh(elem_a[1]);
                        elem_res[1] = FastMath.cos(elem_a[0]) * FastMath.sinh(elem_a[1]);
                        resc.setComplexDouble(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                double[] elem_res = new double[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    elem_res[0] = FastMath.sin(elem_a[0]) * FastMath.cosh(elem_a[1]);
                                    elem_res[1] = FastMath.cos(elem_a[0]) * FastMath.sinh(elem_a[1]);
                                    resc.setComplexDouble(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        double[] elem_res = new double[2];
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            elem_res[0] = FastMath.sin(elem_a[0]) * FastMath.cosh(elem_a[1]);
                            elem_res[1] = FastMath.cos(elem_a[0]) * FastMath.sinh(elem_a[1]);
                            resc.setComplexDouble(i, elem_res);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Cosine of LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return cos(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray cos(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().isIntegerNumericType() ? com.pl.edu.icm.jlargearrays.LargeArrayType.FLOAT : a.getType();
        return cos(a, out_type);
    }

    /**
     * Cosine of LargeArray.
     * <p>
     * @param a        input array
     * @param out_type output type
     * <p>
     * @return cos(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray cos(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric()) {
            throw new IllegalArgumentException("a == null || !a.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant()) {
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.cos(a.getDouble(0)));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexCos(elem_a));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexCos(elem_a));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.cos(a.getDouble(i)));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, FastMath.cos(a.getDouble(k)));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.cos(a.getDouble(i)));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) a;
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    float[] elem_res = new float[2];
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        elem_res[0] = (float) (FastMath.cos(elem_a[0]) * FastMath.cosh(elem_a[1]));
                        elem_res[1] = (float) (-FastMath.sin(elem_a[0]) * FastMath.sinh(elem_a[1]));
                        resc.setComplexFloat(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                float[] elem_res = new float[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    elem_res[0] = (float) (FastMath.cos(elem_a[0]) * FastMath.cosh(elem_a[1]));
                                    elem_res[1] = (float) (-FastMath.sin(elem_a[0]) * FastMath.sinh(elem_a[1]));
                                    resc.setComplexFloat(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        float[] elem_res = new float[2];
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            elem_res[0] = (float) (FastMath.cos(elem_a[0]) * FastMath.cosh(elem_a[1]));
                            elem_res[1] = (float) (-FastMath.sin(elem_a[0]) * FastMath.sinh(elem_a[1]));
                            resc.setComplexFloat(i, elem_res);
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) a;
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    double[] elem_res = new double[2];
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        elem_res[0] = FastMath.cos(elem_a[0]) * FastMath.cosh(elem_a[1]);
                        elem_res[1] = -FastMath.sin(elem_a[0]) * FastMath.sinh(elem_a[1]);
                        resc.setComplexDouble(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                double[] elem_res = new double[2];
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    elem_res[0] = FastMath.cos(elem_a[0]) * FastMath.cosh(elem_a[1]);
                                    elem_res[1] = -FastMath.sin(elem_a[0]) * FastMath.sinh(elem_a[1]);
                                    resc.setComplexDouble(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        double[] elem_res = new double[2];
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            elem_res[0] = FastMath.cos(elem_a[0]) * FastMath.cosh(elem_a[1]);
                            elem_res[1] = -FastMath.sin(elem_a[0]) * FastMath.sinh(elem_a[1]);
                            resc.setComplexDouble(i, elem_res);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Tangent of LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return tan(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray tan(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().isIntegerNumericType() ? com.pl.edu.icm.jlargearrays.LargeArrayType.FLOAT : a.getType();
        return tan(a, out_type);
    }

    /**
     * Tangent of LargeArray.
     * <p>
     * @param a        input array
     * @param out_type output type
     * <p>
     * @return tan(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray tan(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric()) {
            throw new IllegalArgumentException("a == null || !a.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant()) {
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.tan(a.getDouble(0)));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexTan(elem_a));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexTan(elem_a));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.tan(a.getDouble(i)));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, FastMath.tan(a.getDouble(k)));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.tan(a.getDouble(i)));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) a;
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        float[] s = complexSin(elem_a);
                        float[] c = complexCos(elem_a);
                        float[] elem_res = complexDiv(s, c);
                        resc.setComplexFloat(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    float[] s = complexSin(elem_a);
                                    float[] c = complexCos(elem_a);
                                    float[] elem_res = complexDiv(s, c);
                                    resc.setComplexFloat(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            float[] s = complexSin(elem_a);
                            float[] c = complexCos(elem_a);
                            float[] elem_res = complexDiv(s, c);
                            resc.setComplexFloat(i, elem_res);
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) a;
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        double[] s = complexSin(elem_a);
                        double[] c = complexCos(elem_a);
                        double[] elem_res = complexDiv(s, c);
                        resc.setComplexDouble(i, elem_res);
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    double[] s = complexSin(elem_a);
                                    double[] c = complexCos(elem_a);
                                    double[] elem_res = complexDiv(s, c);
                                    resc.setComplexDouble(k, elem_res);
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            double[] s = complexSin(elem_a);
                            double[] c = complexCos(elem_a);
                            double[] elem_res = complexDiv(s, c);
                            resc.setComplexDouble(i, elem_res);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Inverse sine of LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return asin(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray asin(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().isIntegerNumericType() ? com.pl.edu.icm.jlargearrays.LargeArrayType.FLOAT : a.getType();
        return asin(a, out_type);
    }

    /**
     * Inverse sine of LargeArray.
     * <p>
     * @param a        input array
     * @param out_type output type
     * <p>
     * @return asin(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray asin(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric()) {
            throw new IllegalArgumentException("a == null || !a.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant()) {
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.asin(a.getDouble(0)));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexAsin(elem_a));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexAsin(elem_a));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.asin(a.getDouble(i)));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, FastMath.asin(a.getDouble(k)));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.asin(a.getDouble(i)));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) a;
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        resc.setComplexFloat(i, complexAsin(elem_a));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    resc.setComplexFloat(k, complexAsin(elem_a));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            resc.setComplexFloat(i, complexAsin(elem_a));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) a;
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        resc.setComplexDouble(i, complexAsin(elem_a));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    resc.setComplexDouble(k, complexAsin(elem_a));

                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            resc.setComplexDouble(i, complexAsin(elem_a));

                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Inverse cosine of LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return acos(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray acos(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().isIntegerNumericType() ? com.pl.edu.icm.jlargearrays.LargeArrayType.FLOAT : a.getType();
        return acos(a, out_type);
    }

    /**
     * Inverse cosine of LargeArray.
     * <p>
     * @param a        input array
     * <p>
     * @param out_type output type
     * <p>
     * @return acos(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray acos(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric()) {
            throw new IllegalArgumentException("a == null || !a.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant()) {
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.acos(a.getDouble(0)));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexAcos(elem_a));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexAcos(elem_a));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.acos(a.getDouble(i)));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, FastMath.acos(a.getDouble(k)));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.acos(a.getDouble(i)));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) a;
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        resc.setComplexFloat(i, complexAcos(elem_a));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    resc.setComplexFloat(k, complexAcos(elem_a));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            resc.setComplexFloat(i, complexAcos(elem_a));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) a;
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        resc.setComplexDouble(i, complexAcos(elem_a));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    resc.setComplexDouble(k, complexAcos(elem_a));

                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            resc.setComplexDouble(i, complexAcos(elem_a));

                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Inverse tangent of LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return atan(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray atan(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = a.getType().isIntegerNumericType() ? com.pl.edu.icm.jlargearrays.LargeArrayType.FLOAT : a.getType();
        return atan(a, out_type);
    }

    /**
     * Inverse tangent of LargeArray.
     * <p>
     * @param a        input array
     * @param out_type output type
     * <p>
     * @return atan(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray atan(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric()) {
            throw new IllegalArgumentException("a == null || !a.isNumeric()");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final com.pl.edu.icm.jlargearrays.LargeArray res;
        long length = a.length();
        if (a.isConstant()) {
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, FastMath.atan(a.getDouble(0)));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                float[] elem_a = ((ComplexFloatLargeArray) a).getComplexFloat(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexAtan(elem_a));
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                double[] elem_a = ((ComplexDoubleLargeArray) a).getComplexDouble(0);
                return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, complexAtan(elem_a));
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            res = com.pl.edu.icm.jlargearrays.LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (out_type.isIntegerNumericType() || out_type.isRealNumericType()) {
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        res.setDouble(i, FastMath.atan(a.getDouble(i)));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    res.setDouble(k, FastMath.atan(a.getDouble(k)));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            res.setDouble(i, FastMath.atan(a.getDouble(i)));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                final ComplexFloatLargeArray _ac = (ComplexFloatLargeArray) a;
                final ComplexFloatLargeArray resc = (ComplexFloatLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        float[] elem_a = _ac.getComplexFloat(i);
                        resc.setComplexFloat(i, complexAtan(elem_a));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    float[] elem_a = _ac.getComplexFloat(k);
                                    resc.setComplexFloat(k, complexAtan(elem_a));
                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            float[] elem_a = _ac.getComplexFloat(i);
                            resc.setComplexFloat(i, complexAtan(elem_a));
                        }
                    }
                }
            } else if (out_type == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                final ComplexDoubleLargeArray _ac = (ComplexDoubleLargeArray) a;
                final ComplexDoubleLargeArray resc = (ComplexDoubleLargeArray) res;
                if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                    for (long i = 0; i < length; i++) {
                        double[] elem_a = _ac.getComplexDouble(i);
                        resc.setComplexDouble(i, complexAtan(elem_a));
                    }
                } else {
                    long k = length / nthreads;
                    Future[] threads = new Future[nthreads];
                    for (int j = 0; j < nthreads; j++) {
                        final long firstIdx = j * k;
                        final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                        threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (long k = firstIdx; k < lastIdx; k++) {
                                    double[] elem_a = _ac.getComplexDouble(k);
                                    resc.setComplexDouble(k, complexAtan(elem_a));

                                }
                            }
                        });
                    }
                    try {
                        com.pl.edu.icm.jlargearrays.ConcurrencyUtils.waitForCompletion(threads);
                    } catch (InterruptedException | ExecutionException ex) {
                        for (long i = 0; i < length; i++) {
                            double[] elem_a = _ac.getComplexDouble(i);
                            resc.setComplexDouble(i, complexAtan(elem_a));

                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid array type.");
            }
            return res;
        }
    }

    /**
     * Signum of LargeArray.
     * <p>
     * @param a input array
     * <p>
     * @return signum(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray signum(final com.pl.edu.icm.jlargearrays.LargeArray a)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayType out_type = com.pl.edu.icm.jlargearrays.LargeArrayType.BYTE;
        return signum(a, out_type);
    }

    /**
     * Signum of LargeArray.
     * <p>
     * @param a        input array
     * <p>
     * @param out_type output type
     * <p>
     * @return signum(a)
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray signum(final com.pl.edu.icm.jlargearrays.LargeArray a, final com.pl.edu.icm.jlargearrays.LargeArrayType out_type)
    {
        if (a == null || !a.isNumeric() || a.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT || a.getType() == LargeArrayType.COMPLEX_DOUBLE) {
            throw new IllegalArgumentException("a == null || !a.isNumeric() || a.getType() == LargeArrayType.COMPLEX_FLOAT || a.getType() == LargeArrayType.COMPLEX_DOUBLE");
        }
        if (!out_type.isNumericType()) throw new IllegalArgumentException("Output type must be numeric.");
        final LargeArray res;
        long length = a.length();

        if (a.isConstant()) {
            return com.pl.edu.icm.jlargearrays.LargeArrayUtils.createConstant(out_type, length, (byte) FastMath.signum(a.getDouble(0)));
        } else {
            res = LargeArrayUtils.create(out_type, length, false);
            int nthreads = (int) FastMath.min(length, com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getNumberOfThreads());
            if (nthreads < 2 || length < com.pl.edu.icm.jlargearrays.ConcurrencyUtils.getConcurrentThreshold()) {
                for (long i = 0; i < length; i++) {
                    res.setByte(i, (byte) FastMath.signum(a.getDouble(i)));
                }
            } else {
                long k = length / nthreads;
                Future[] threads = new Future[nthreads];
                for (int j = 0; j < nthreads; j++) {
                    final long firstIdx = j * k;
                    final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                    threads[j] = com.pl.edu.icm.jlargearrays.ConcurrencyUtils.submit(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            for (long k = firstIdx; k < lastIdx; k++) {
                                res.setByte(k, (byte) FastMath.signum(a.getDouble(k)));
                            }
                        }
                    });
                }
                try {
                    ConcurrencyUtils.waitForCompletion(threads);
                } catch (InterruptedException | ExecutionException ex) {
                    for (long i = 0; i < length; i++) {
                        res.setByte(i, (byte) FastMath.signum(a.getDouble(i)));
                    }
                }
            }
            return res;
        }
    }

}
