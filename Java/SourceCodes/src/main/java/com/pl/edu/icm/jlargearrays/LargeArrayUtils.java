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

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.math3.util.FastMath;
import com.pl.edu.icm.jlargearrays.*;
import com.pl.edu.icm.jlargearrays.ByteLargeArray;
import com.pl.edu.icm.jlargearrays.FloatLargeArray;
import com.pl.edu.icm.jlargearrays.IntLargeArray;
import com.pl.edu.icm.jlargearrays.LargeArray;
import com.pl.edu.icm.jlargearrays.LargeArrayType;
import com.pl.edu.icm.jlargearrays.LongLargeArray;

/**
 *
 * LargeArray utilities.
 *
 * @author Piotr Wendykier (p.wendykier@icm.edu.pl)
 */
public class LargeArrayUtils
{

    /**
     * An object for performing low-level, unsafe operations.
     */
    public static final sun.misc.Unsafe UNSAFE;

    static {
        Object theUnsafe = null;
        Exception exception = null;
        try {
            Class<?> uc = Class.forName("sun.misc.Unsafe");
            Field f = uc.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            theUnsafe = f.get(uc);
        } catch (ClassNotFoundException e) {
            exception = e;
        } catch (IllegalAccessException e) {
            exception = e;
        } catch (IllegalArgumentException e) {
            exception = e;
        } catch (NoSuchFieldException e) {
            exception = e;
        } catch (SecurityException e) {
            exception = e;
        }
        UNSAFE = (sun.misc.Unsafe) theUnsafe;
        if (UNSAFE == null) {
            throw new Error("Could not obtain access to sun.misc.Unsafe", exception);
        }
    }

    private LargeArrayUtils()
    {
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Both arrays need to be of the same type. Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final com.pl.edu.icm.jlargearrays.LargeArray src, final long srcPos, final com.pl.edu.icm.jlargearrays.LargeArray dest, final long destPos, final long length)
    {
        if (src.getType() != dest.getType()) {
            throw new IllegalArgumentException("The type of source array is different than the type of destimation array.");
        }
        switch (src.getType()) {
            case LOGIC:
                arraycopy((LogicLargeArray) src, srcPos, (LogicLargeArray) dest, destPos, length);
                break;
            case BYTE:
                arraycopy((UnsignedByteLargeArray) src, srcPos, (UnsignedByteLargeArray) dest, destPos, length);
                break;
            case SHORT:
                arraycopy((ShortLargeArray) src, srcPos, (ShortLargeArray) dest, destPos, length);
                break;
            case INT:
                arraycopy((com.pl.edu.icm.jlargearrays.IntLargeArray) src, srcPos, (com.pl.edu.icm.jlargearrays.IntLargeArray) dest, destPos, length);
                break;
            case LONG:
                arraycopy((com.pl.edu.icm.jlargearrays.LongLargeArray) src, srcPos, (com.pl.edu.icm.jlargearrays.LongLargeArray) dest, destPos, length);
                break;
            case FLOAT:
                arraycopy((com.pl.edu.icm.jlargearrays.FloatLargeArray) src, srcPos, (com.pl.edu.icm.jlargearrays.FloatLargeArray) dest, destPos, length);
                break;
            case DOUBLE:
                arraycopy((DoubleLargeArray) src, srcPos, (DoubleLargeArray) dest, destPos, length);
                break;
            case COMPLEX_FLOAT:
                arraycopy((ComplexFloatLargeArray) src, srcPos, (ComplexFloatLargeArray) dest, destPos, length);
                break;
            case COMPLEX_DOUBLE:
                arraycopy((ComplexDoubleLargeArray) src, srcPos, (ComplexDoubleLargeArray) dest, destPos, length);
                break;
            case STRING:
                arraycopy((StringLargeArray) src, srcPos, (StringLargeArray) dest, destPos, length);
                break;
            case OBJECT:
                arraycopy((ObjectLargeArray) src, srcPos, (ObjectLargeArray) dest, destPos, length);
                break;
            default:
                throw new IllegalArgumentException("Invalid array type.");
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Both arrays need to be of the same type. Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final Object src, final long srcPos, final com.pl.edu.icm.jlargearrays.LargeArray dest, final long destPos, final long length)
    {
        switch (dest.getType()) {
            case LOGIC:
                arraycopy((boolean[]) src, (int) srcPos, (LogicLargeArray) dest, destPos, length);
                break;
            case BYTE:
                arraycopy((byte[]) src, (int) srcPos, (com.pl.edu.icm.jlargearrays.ByteLargeArray) dest, destPos, length);
                break;
            case UNSIGNED_BYTE: {
                Class dataClass = src.getClass();
                Class componentClass = dataClass.getComponentType();
                if (componentClass == Byte.TYPE) {
                    arraycopy((byte[]) src, (int) srcPos, (UnsignedByteLargeArray) dest, destPos, length);
                } else {
                    arraycopy((short[]) src, (int) srcPos, (UnsignedByteLargeArray) dest, destPos, length);
                }
                break;
            }
            case SHORT:
                arraycopy((short[]) src, (int) srcPos, (ShortLargeArray) dest, destPos, length);
                break;
            case INT:
                arraycopy((int[]) src, (int) srcPos, (com.pl.edu.icm.jlargearrays.IntLargeArray) dest, destPos, length);
                break;
            case LONG:
                arraycopy((long[]) src, (int) srcPos, (com.pl.edu.icm.jlargearrays.LongLargeArray) dest, destPos, length);
                break;
            case FLOAT:
                arraycopy((float[]) src, (int) srcPos, (com.pl.edu.icm.jlargearrays.FloatLargeArray) dest, destPos, length);
                break;
            case DOUBLE:
                arraycopy((double[]) src, (int) srcPos, (DoubleLargeArray) dest, destPos, length);
                break;
            case COMPLEX_FLOAT:
                arraycopy((float[]) src, (int) srcPos, (ComplexFloatLargeArray) dest, destPos, length);
                break;
            case COMPLEX_DOUBLE:
                arraycopy((double[]) src, (int) srcPos, (ComplexDoubleLargeArray) dest, destPos, length);
                break;
            case STRING:
                arraycopy((String[]) src, (int) srcPos, (StringLargeArray) dest, destPos, length);
                break;
            case OBJECT:
                arraycopy((Object[]) src, (int) srcPos, (ObjectLargeArray) dest, destPos, length);
                break;
            default:
                throw new IllegalArgumentException("Invalid array type.");
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final LogicLargeArray src, final long srcPos, final LogicLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setByte(j, src.getByte(i));
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setByte(destPos + k, src.getByte(srcPos + k));
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setByte(j, src.getByte(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final boolean[] src, final int srcPos, final LogicLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }

        int i = srcPos;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setBoolean(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setBoolean(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setBoolean(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final com.pl.edu.icm.jlargearrays.ByteLargeArray src, final long srcPos, final com.pl.edu.icm.jlargearrays.ByteLargeArray dest, final long destPos, final long length)
    {

        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setByte(j, src.getByte(i));
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setByte(destPos + k, src.getByte(srcPos + k));
                        }
                    }
                });

            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setByte(j, src.getByte(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final byte[] src, final int srcPos, final com.pl.edu.icm.jlargearrays.ByteLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setByte(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setByte(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });

            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setByte(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final UnsignedByteLargeArray src, final long srcPos, final UnsignedByteLargeArray dest, final long destPos, final long length)
    {

        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setByte(j, src.getByte(i));
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setByte(destPos + k, src.getByte(srcPos + k));
                        }
                    }
                });

            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setByte(j, src.getByte(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final byte[] src, final int srcPos, final UnsignedByteLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setByte(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setByte(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });

            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setByte(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final short[] src, final int srcPos, final UnsignedByteLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setUnsignedByte(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setUnsignedByte(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });

            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setUnsignedByte(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final ShortLargeArray src, final long srcPos, final ShortLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }

        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setShort(j, src.getShort(i));
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setShort(destPos + k, src.getShort(srcPos + k));
                        }
                    }
                });

            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setShort(j, src.getShort(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final short[] src, final int srcPos, final ShortLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setShort(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setShort(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setShort(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final com.pl.edu.icm.jlargearrays.IntLargeArray src, final long srcPos, final com.pl.edu.icm.jlargearrays.IntLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setInt(j, src.getInt(i));
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setInt(destPos + k, src.getInt(srcPos + k));
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setInt(j, src.getInt(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final int[] src, final int srcPos, final com.pl.edu.icm.jlargearrays.IntLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setInt(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setInt(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });

            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setInt(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final com.pl.edu.icm.jlargearrays.LongLargeArray src, final long srcPos, final com.pl.edu.icm.jlargearrays.LongLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setLong(j, src.getLong(i));
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setLong(destPos + k, src.getLong(srcPos + k));
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setLong(j, src.getLong(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final long[] src, final int srcPos, final com.pl.edu.icm.jlargearrays.LongLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setLong(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setLong(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setLong(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final com.pl.edu.icm.jlargearrays.FloatLargeArray src, final long srcPos, final com.pl.edu.icm.jlargearrays.FloatLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setFloat(j, src.getFloat(i));
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setFloat(destPos + k, src.getFloat(srcPos + k));
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setFloat(j, src.getFloat(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final float[] src, final int srcPos, final com.pl.edu.icm.jlargearrays.FloatLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setFloat(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setFloat(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setFloat(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final DoubleLargeArray src, final long srcPos, final DoubleLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setDouble(j, src.getDouble(i));
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setDouble(destPos + k, src.getDouble(srcPos + k));
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setDouble(j, src.getDouble(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final double[] src, final int srcPos, final DoubleLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setDouble(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setDouble(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setDouble(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final ComplexFloatLargeArray src, final long srcPos, final ComplexFloatLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setComplexFloat(j, src.getComplexFloat(i));
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setComplexFloat(destPos + k, src.getComplexFloat(srcPos + k));
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setComplexFloat(j, src.getComplexFloat(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final float[] src, final int srcPos, final ComplexFloatLargeArray dest, final long destPos, final long length)
    {
        if (src.length % 2 != 0) {
            throw new IllegalArgumentException("The length of the source array must be even.");
        }

        if (srcPos < 0 || srcPos >= src.length / 2) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length / 2");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            float[] elem = new float[2];
            for (long j = destPos; j < destPos + length; j++) {
                elem[0] = src[2 * i];
                elem[1] = src[2 * i + 1];
                dest.setComplexFloat(j, elem);
                i++;
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        float[] elem = new float[2];
                        for (long k = firstIdx; k < lastIdx; k++) {
                            elem[0] = src[2 * (srcPos + (int) k)];
                            elem[1] = src[2 * (srcPos + (int) k) + 1];
                            dest.setComplexFloat(destPos + k, elem);
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                float[] elem = new float[2];
                for (long j = destPos; j < destPos + length; j++) {
                    elem[0] = src[2 * i];
                    elem[1] = src[2 * i + 1];
                    dest.setComplexFloat(j, elem);
                    i++;
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final ComplexDoubleLargeArray src, final long srcPos, final ComplexDoubleLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setComplexDouble(j, src.getComplexDouble(i));
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setComplexDouble(destPos + k, src.getComplexDouble(srcPos + k));
                        }
                    }
                });

            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setComplexDouble(j, src.getComplexDouble(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final double[] src, final int srcPos, final ComplexDoubleLargeArray dest, final long destPos, final long length)
    {
        if (src.length % 2 != 0) {
            throw new IllegalArgumentException("The length of the source array must be even.");
        }

        if (srcPos < 0 || srcPos >= src.length / 2) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length / 2");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            double[] elem = new double[2];
            for (long j = destPos; j < destPos + length; j++) {
                elem[0] = src[2 * i];
                elem[1] = src[2 * i + 1];
                dest.setComplexDouble(j, elem);
                i++;
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        double[] elem = new double[2];
                        for (long k = firstIdx; k < lastIdx; k++) {
                            elem[0] = src[2 * (srcPos + (int) k)];
                            elem[1] = src[2 * (srcPos + (int) k) + 1];
                            dest.setComplexDouble(destPos + k, elem);
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                double[] elem = new double[2];
                for (long j = destPos; j < destPos + length; j++) {
                    elem[0] = src[2 * i];
                    elem[1] = src[2 * i + 1];
                    dest.setComplexDouble(j, elem);
                    i++;
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final StringLargeArray src, final long srcPos, final StringLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.set(j, src.get(i));
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.set(destPos + k, src.get(srcPos + k));
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.set(j, src.get(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final String[] src, final int srcPos, final StringLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.set(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.set(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.set(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final ObjectLargeArray src, final long srcPos, final ObjectLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.set(j, src.get(i));
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.set(destPos + k, src.get(srcPos + k));
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.set(j, src.get(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final Object[] src, final int srcPos, final ObjectLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.set(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.set(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.set(j, src[i++]);
                }
            }
        }
    }

    /**
     * Creates a new constant LargeArray.
     *
     * @param type   the type of constant LargeArray
     * @param length the length of constant LargeArray
     * @param value  the value of constant LargeArray
     *
     * @return constant LargeArray of a specified type
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray createConstant(final com.pl.edu.icm.jlargearrays.LargeArrayType type, final long length, Object value)
    {
        switch (type) {
            case LOGIC: {
                byte v;
                if (value instanceof Boolean) {
                    v = ((Boolean) value) == true ? (byte) 1 : (byte) 0;
                } else if (value instanceof Byte) {
                    v = ((Byte) value).byteValue();
                } else if (value instanceof Short) {
                    v = ((Short) value).byteValue();
                } else if (value instanceof Integer) {
                    v = ((Integer) value).byteValue();
                } else if (value instanceof Long) {
                    v = ((Long) value).byteValue();
                } else if (value instanceof Float) {
                    v = ((Float) value).byteValue();
                } else if (value instanceof Double) {
                    v = ((Double) value).byteValue();
                } else {
                    throw new IllegalArgumentException("Invalid value type.");
                }
                return new LogicLargeArray(length, v);
            }
            case BYTE: {
                byte v;
                if (value instanceof Boolean) {
                    v = ((Boolean) value) == true ? (byte) 1 : (byte) 0;
                } else if (value instanceof Byte) {
                    v = ((Byte) value).byteValue();
                } else if (value instanceof Short) {
                    v = ((Short) value).byteValue();
                } else if (value instanceof Integer) {
                    v = ((Integer) value).byteValue();
                } else if (value instanceof Long) {
                    v = ((Long) value).byteValue();
                } else if (value instanceof Float) {
                    v = ((Float) value).byteValue();
                } else if (value instanceof Double) {
                    v = ((Double) value).byteValue();
                } else {
                    throw new IllegalArgumentException("Invalid value type.");
                }
                return new com.pl.edu.icm.jlargearrays.ByteLargeArray(length, v);
            }
            case UNSIGNED_BYTE: {
                short v;
                if (value instanceof Boolean) {
                    v = ((Boolean) value) == true ? (short) 1 : (short) 0;
                } else if (value instanceof Byte) {
                    v = ((Byte) value).shortValue();
                } else if (value instanceof Short) {
                    v = ((Short) value).shortValue();
                } else if (value instanceof Integer) {
                    v = ((Integer) value).shortValue();
                } else if (value instanceof Long) {
                    v = ((Long) value).shortValue();
                } else if (value instanceof Float) {
                    v = ((Float) value).shortValue();
                } else if (value instanceof Double) {
                    v = ((Double) value).shortValue();
                } else {
                    throw new IllegalArgumentException("Invalid value type.");
                }
                return new UnsignedByteLargeArray(length, v);
            }
            case SHORT: {
                short v;
                if (value instanceof Boolean) {
                    v = ((Boolean) value) == true ? (short) 1 : (short) 0;
                } else if (value instanceof Byte) {
                    v = ((Byte) value).shortValue();
                } else if (value instanceof Short) {
                    v = ((Short) value).shortValue();
                } else if (value instanceof Integer) {
                    v = ((Integer) value).shortValue();
                } else if (value instanceof Long) {
                    v = ((Long) value).shortValue();
                } else if (value instanceof Float) {
                    v = ((Float) value).shortValue();
                } else if (value instanceof Double) {
                    v = ((Double) value).shortValue();
                } else {
                    throw new IllegalArgumentException("Invalid value type.");
                }
                return new ShortLargeArray(length, v);
            }
            case INT: {
                int v;
                if (value instanceof Boolean) {
                    v = ((Boolean) value) == true ? 1 : 0;
                } else if (value instanceof Byte) {
                    v = ((Byte) value).intValue();
                } else if (value instanceof Short) {
                    v = ((Short) value).intValue();
                } else if (value instanceof Integer) {
                    v = ((Integer) value).intValue();
                } else if (value instanceof Long) {
                    v = ((Long) value).intValue();
                } else if (value instanceof Float) {
                    v = ((Float) value).intValue();
                } else if (value instanceof Double) {
                    v = ((Double) value).intValue();
                } else {
                    throw new IllegalArgumentException("Invalid value type.");
                }
                return new com.pl.edu.icm.jlargearrays.IntLargeArray(length, v);
            }
            case LONG: {
                long v;
                if (value instanceof Boolean) {
                    v = ((Boolean) value) == true ? 1 : 0;
                } else if (value instanceof Byte) {
                    v = ((Byte) value).longValue();
                } else if (value instanceof Short) {
                    v = ((Short) value).longValue();
                } else if (value instanceof Integer) {
                    v = ((Integer) value).longValue();
                } else if (value instanceof Long) {
                    v = ((Long) value).longValue();
                } else if (value instanceof Float) {
                    v = ((Float) value).longValue();
                } else if (value instanceof Double) {
                    v = ((Double) value).longValue();
                } else {
                    throw new IllegalArgumentException("Invalid value type.");
                }
                return new com.pl.edu.icm.jlargearrays.LongLargeArray(length, v);
            }
            case FLOAT: {
                float v;
                if (value instanceof Boolean) {
                    v = ((Boolean) value) == true ? 1 : 0;
                } else if (value instanceof Byte) {
                    v = ((Byte) value).floatValue();
                } else if (value instanceof Short) {
                    v = ((Short) value).floatValue();
                } else if (value instanceof Integer) {
                    v = ((Integer) value).floatValue();
                } else if (value instanceof Long) {
                    v = ((Long) value).floatValue();
                } else if (value instanceof Float) {
                    v = ((Float) value).floatValue();
                } else if (value instanceof Double) {
                    v = ((Double) value).floatValue();
                } else {
                    throw new IllegalArgumentException("Invalid value type.");
                }
                return new com.pl.edu.icm.jlargearrays.FloatLargeArray(length, v);
            }
            case DOUBLE: {
                double v;
                if (value instanceof Boolean) {
                    v = ((Boolean) value).booleanValue() == true ? 1 : 0;
                } else if (value instanceof Byte) {
                    v = ((Byte) value).doubleValue();
                } else if (value instanceof Short) {
                    v = ((Short) value).doubleValue();
                } else if (value instanceof Integer) {
                    v = ((Integer) value).doubleValue();
                } else if (value instanceof Long) {
                    v = ((Long) value).doubleValue();
                } else if (value instanceof Float) {
                    v = ((Float) value).doubleValue();
                } else if (value instanceof Double) {
                    v = ((Double) value).doubleValue();
                } else {
                    throw new IllegalArgumentException("Invalid value type.");
                }
                return new DoubleLargeArray(length, v);
            }
            case COMPLEX_FLOAT: {
                Class dataClass = value.getClass();
                Class componentClass = dataClass.getComponentType();
                float[] v;
                if (componentClass == Float.TYPE) {
                    v = (float[]) value;
                } else {
                    throw new IllegalArgumentException("Invalid value type.");
                }
                return new ComplexFloatLargeArray(length, v);
            }
            case COMPLEX_DOUBLE: {
                Class dataClass = value.getClass();
                Class componentClass = dataClass.getComponentType();
                double[] v;
                if (componentClass == Double.TYPE) {
                    v = (double[]) value;
                } else {
                    throw new IllegalArgumentException("Invalid value type.");
                }
                return new ComplexDoubleLargeArray(length, v);
            }
            case STRING:
                String v;
                if (value instanceof String) {
                    v = (String) value;
                } else {
                    throw new IllegalArgumentException("Invalid value type.");
                }
                return new StringLargeArray(length, v);
            case OBJECT:
                return new ObjectLargeArray(length, value);
            default:
                throw new IllegalArgumentException("Invalid array type.");
        }
    }

    /**
     * Creates a new instance of LargeArray. The native memory is zeroed.
     *
     * @param type   the type of LargeArray
     * @param length number of elements
     *
     * @return new instance of LargeArray
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray create(com.pl.edu.icm.jlargearrays.LargeArrayType type, long length)
    {
        return create(type, length, true);
    }

    /**
     * Creates a new instance of LargeArray
     *
     * @param type             the type of LargeArray
     * @param length           number of elements
     * @param zeroNativeMemory if true, then the native memory is zeroed
     *
     * @return new instance of LargeArray
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray create(com.pl.edu.icm.jlargearrays.LargeArrayType type, long length, boolean zeroNativeMemory)
    {
        switch (type) {
            case LOGIC:
                return new LogicLargeArray(length, zeroNativeMemory);
            case BYTE:
                return new com.pl.edu.icm.jlargearrays.ByteLargeArray(length, zeroNativeMemory);
            case UNSIGNED_BYTE:
                return new UnsignedByteLargeArray(length, zeroNativeMemory);
            case SHORT:
                return new ShortLargeArray(length, zeroNativeMemory);
            case INT:
                return new com.pl.edu.icm.jlargearrays.IntLargeArray(length, zeroNativeMemory);
            case LONG:
                return new com.pl.edu.icm.jlargearrays.LongLargeArray(length, zeroNativeMemory);
            case FLOAT:
                return new com.pl.edu.icm.jlargearrays.FloatLargeArray(length, zeroNativeMemory);
            case DOUBLE:
                return new DoubleLargeArray(length, zeroNativeMemory);
            case COMPLEX_FLOAT:
                return new ComplexFloatLargeArray(length, zeroNativeMemory);
            case COMPLEX_DOUBLE:
                return new ComplexDoubleLargeArray(length, zeroNativeMemory);
            case STRING:
                return new StringLargeArray(length, 100, zeroNativeMemory);
            case OBJECT:
                return new ObjectLargeArray(length, 100, zeroNativeMemory);
            default:
                throw new IllegalArgumentException("Invalid array type.");
        }
    }

    /**
     * Generates a random LargeArray
     *
     * @param type   the type of LargeArray
     * @param length number of elements
     *
     * @return random LargeArray
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray generateRandom(com.pl.edu.icm.jlargearrays.LargeArrayType type, long length)
    {
        com.pl.edu.icm.jlargearrays.LargeArray res = create(type, length, false);
        Random rand = new Random();
        switch (type) {
            case LOGIC: {
                for (long i = 0; i < length; i++) {
                    res.setBoolean(i, rand.nextBoolean());
                }
                break;
            }
            case BYTE:
            case UNSIGNED_BYTE: {
                long i;
                int r;
                for (i = 0; i < length / 4; i += 4) {
                    r = rand.nextInt();
                    res.setByte(i, (byte) (r >>= Byte.SIZE));
                    res.setByte(i + 1, (byte) (r >>= Byte.SIZE));
                    res.setByte(i + 2, (byte) (r >>= Byte.SIZE));
                    res.setByte(i + 3, (byte) (r >>= Byte.SIZE));
                }
                r = rand.nextInt();
                for (; i < length; i++) {
                    res.setByte(i, (byte) (r >>= Byte.SIZE));
                }
                break;
            }
            case SHORT: {
                long i;
                int r;
                for (i = 0; i < length / 2; i += 2) {
                    r = rand.nextInt();
                    res.setShort(i, (short) (r >>= Short.SIZE));
                    res.setShort(i + 1, (short) (r >>= Short.SIZE));
                }
                r = rand.nextInt();
                for (; i < length; i++) {
                    res.setShort(i, (short) (r >>= Short.SIZE));
                }
                break;
            }
            case INT: {
                for (long i = 0; i < length; i++) {
                    res.setInt(i, rand.nextInt());
                }
                break;
            }
            case LONG: {
                for (long i = 0; i < length; i++) {
                    res.setLong(i, rand.nextLong());
                }
                break;
            }
            case FLOAT: {
                for (long i = 0; i < length; i++) {
                    res.setFloat(i, rand.nextFloat());
                }
                break;
            }
            case DOUBLE: {
                for (long i = 0; i < length; i++) {
                    res.setDouble(i, rand.nextDouble());
                }
                break;
            }
            case COMPLEX_FLOAT: {
                ComplexFloatLargeArray res_c = (ComplexFloatLargeArray) res;
                float[] elem_res = new float[2];
                for (long i = 0; i < length; i++) {
                    elem_res[0] = rand.nextFloat();
                    elem_res[1] = rand.nextFloat();
                    res_c.setComplexFloat(i, elem_res);
                }
                break;
            }
            case COMPLEX_DOUBLE: {
                ComplexDoubleLargeArray res_c = (ComplexDoubleLargeArray) res;
                double[] elem_res = new double[2];
                for (long i = 0; i < length; i++) {
                    elem_res[0] = rand.nextDouble();
                    elem_res[1] = rand.nextDouble();
                    res_c.setComplexDouble(i, elem_res);
                }
                break;
            }
            case STRING: {
                for (long i = 0; i < length; i++) {
                    res.setFloat(i, rand.nextFloat());
                }
                break;
            }
            case OBJECT: {
                for (long i = 0; i < length; i++) {
                    res.set(i, rand.nextFloat());
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Invalid array type.");
        }
        return res;
    }

    /**
     * Converts LargeArray to a given type.
     *
     * @param src  the source array
     * @param type the type of LargeArray
     *
     * @return LargeArray of a specified type
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray convert(final com.pl.edu.icm.jlargearrays.LargeArray src, final com.pl.edu.icm.jlargearrays.LargeArrayType type)
    {
        if (src.getType() == type) {
            return src;
        }
        if (src.isConstant()) {
            switch (type) {
                case LOGIC:
                    return new LogicLargeArray(src.length(), src.getByte(0));
                case BYTE:
                    return new ByteLargeArray(src.length(), src.getByte(0));
                case UNSIGNED_BYTE:
                    return new UnsignedByteLargeArray(src.length(), src.getUnsignedByte(0));
                case SHORT:
                    return new ShortLargeArray(src.length(), src.getShort(0));
                case INT:
                    return new IntLargeArray(src.length(), src.getInt(0));
                case LONG:
                    return new LongLargeArray(src.length(), src.getLong(0));
                case FLOAT:
                    return new FloatLargeArray(src.length(), src.getFloat(0));
                case DOUBLE:
                    return new DoubleLargeArray(src.length(), src.getDouble(0));
                case COMPLEX_FLOAT: {
                    if (src.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                        return new ComplexFloatLargeArray(src.length(), ((ComplexDoubleLargeArray) src).getComplexFloat(0));
                    } else {
                        return new ComplexFloatLargeArray(src.length(), new float[]{src.getFloat(0), 0f});
                    }
                }
                case COMPLEX_DOUBLE:
                    if (src.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                        return new ComplexDoubleLargeArray(src.length(), ((ComplexFloatLargeArray) src).getComplexDouble(0));
                    } else {
                        return new ComplexDoubleLargeArray(src.length(), new double[]{src.getDouble(0), 0});
                    }
                case STRING:
                    return new StringLargeArray(src.length(), src.get(0).toString());
                case OBJECT:
                    return new ObjectLargeArray(src.length(), src.get(0));
                default:
                    throw new IllegalArgumentException("Invalid array type.");
            }
        }
        long length = src.length;
        final com.pl.edu.icm.jlargearrays.LargeArray out = create(type, length, false);
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        if (nthreads < 2 || length < ConcurrencyUtils.getConcurrentThreshold()) {
            switch (type) {
                case LOGIC:
                case BYTE:
                    for (long i = 0; i < length; i++) {
                        out.setByte(i, src.getByte(i));
                    }
                    break;
                case UNSIGNED_BYTE:
                    for (long i = 0; i < length; i++) {
                        out.setUnsignedByte(i, src.getUnsignedByte(i));
                    }
                    break;
                case SHORT:
                    for (long i = 0; i < length; i++) {
                        out.setShort(i, src.getShort(i));
                    }
                    break;
                case INT:
                    for (long i = 0; i < length; i++) {
                        out.setInt(i, src.getInt(i));
                    }
                    break;
                case LONG:
                    for (long i = 0; i < length; i++) {
                        out.setLong(i, src.getLong(i));
                    }
                    break;
                case FLOAT:
                    for (long i = 0; i < length; i++) {
                        out.setFloat(i, src.getFloat(i));
                    }
                    break;
                case DOUBLE:
                    for (long i = 0; i < length; i++) {
                        out.setDouble(i, src.getDouble(i));
                    }
                    break;
                case COMPLEX_FLOAT:
                    if (src.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                        for (long i = 0; i < length; i++) {
                            ((ComplexFloatLargeArray) out).setComplexFloat(i, ((ComplexDoubleLargeArray) src).getComplexFloat(i));
                        }
                    } else {
                        for (long i = 0; i < length; i++) {
                            out.setFloat(i, src.getFloat(i));
                        }
                    }
                    break;
                case COMPLEX_DOUBLE:
                    if (src.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                        for (long i = 0; i < length; i++) {
                            ((ComplexDoubleLargeArray) out).setComplexDouble(i, ((ComplexFloatLargeArray) src).getComplexDouble(i));
                        }
                    } else {
                        for (long i = 0; i < length; i++) {
                            out.setDouble(i, src.getDouble(i));
                        }
                    }
                    break;
                case STRING:
                    for (long i = 0; i < length; i++) {
                        out.set(i, src.get(i).toString());
                    }
                    break;
                case OBJECT:
                    for (long i = 0; i < length; i++) {
                        out.set(i, src.get(i));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            long k = length / nthreads;
            Future[] threads = new Future[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = ConcurrencyUtils.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        switch (type) {
                            case BYTE:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.setByte(i, src.getByte(i));
                                }
                                break;
                            case UNSIGNED_BYTE:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.setUnsignedByte(i, src.getUnsignedByte(i));
                                }
                                break;
                            case SHORT:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.setShort(i, src.getShort(i));
                                }
                                break;
                            case INT:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.setInt(i, src.getInt(i));
                                }
                                break;
                            case LONG:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.setLong(i, src.getLong(i));
                                }
                                break;
                            case FLOAT:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.setFloat(i, src.getFloat(i));
                                }
                                break;
                            case DOUBLE:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.setDouble(i, src.getDouble(i));
                                }
                                break;
                            case COMPLEX_FLOAT:
                                if (src.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                                    for (long i = firstIdx; i < lastIdx; i++) {
                                        ((ComplexFloatLargeArray) out).setComplexFloat(i, ((ComplexDoubleLargeArray) src).getComplexFloat(i));
                                    }
                                } else {
                                    for (long i = firstIdx; i < lastIdx; i++) {
                                        out.setFloat(i, src.getFloat(i));
                                    }
                                }
                                break;
                            case COMPLEX_DOUBLE:
                                if (src.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_FLOAT) {
                                    for (long i = firstIdx; i < lastIdx; i++) {
                                        ((ComplexDoubleLargeArray) out).setComplexDouble(i, ((ComplexFloatLargeArray) src).getComplexDouble(i));
                                    }
                                } else {
                                    for (long i = firstIdx; i < lastIdx; i++) {
                                        out.setDouble(i, src.getDouble(i));
                                    }
                                }
                                break;
                            case STRING:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.set(i, src.get(i).toString());
                                }
                                break;
                            case OBJECT:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.set(i, src.get(i));
                                }
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid array type.");
                        }
                    }
                });

            }
            try {
                ConcurrencyUtils.waitForCompletion(threads);
            } catch (InterruptedException | ExecutionException ex) {
                switch (type) {
                    case LOGIC:
                    case BYTE:
                        for (long i = 0; i < length; i++) {
                            out.setByte(i, src.getByte(i));
                        }
                        break;
                    case UNSIGNED_BYTE:
                        for (long i = 0; i < length; i++) {
                            out.setUnsignedByte(i, src.getUnsignedByte(i));
                        }
                        break;
                    case SHORT:
                        for (long i = 0; i < length; i++) {
                            out.setShort(i, src.getShort(i));
                        }
                        break;
                    case INT:
                        for (long i = 0; i < length; i++) {
                            out.setInt(i, src.getInt(i));
                        }
                        break;
                    case LONG:
                        for (long i = 0; i < length; i++) {
                            out.setLong(i, src.getLong(i));
                        }
                        break;
                    case FLOAT:
                        for (long i = 0; i < length; i++) {
                            out.setFloat(i, src.getFloat(i));
                        }
                        break;
                    case DOUBLE:
                        for (long i = 0; i < length; i++) {
                            out.setDouble(i, src.getDouble(i));
                        }
                        break;
                    case COMPLEX_FLOAT:
                        if (src.getType() == com.pl.edu.icm.jlargearrays.LargeArrayType.COMPLEX_DOUBLE) {
                            for (long i = 0; i < length; i++) {
                                ((ComplexFloatLargeArray) out).setComplexFloat(i, ((ComplexDoubleLargeArray) src).getComplexFloat(i));
                            }
                        } else {
                            for (long i = 0; i < length; i++) {
                                out.setFloat(i, src.getFloat(i));
                            }
                        }
                        break;
                    case COMPLEX_DOUBLE:
                        if (src.getType() == LargeArrayType.COMPLEX_FLOAT) {
                            for (long i = 0; i < length; i++) {
                                ((ComplexDoubleLargeArray) out).setComplexDouble(i, ((ComplexFloatLargeArray) src).getComplexDouble(i));
                            }
                        } else {
                            for (long i = 0; i < length; i++) {
                                out.setDouble(i, src.getDouble(i));
                            }
                        }
                        break;
                    case STRING:
                        for (long i = 0; i < length; i++) {
                            out.set(i, src.get(i).toString());
                        }
                        break;
                    case OBJECT:
                        for (long i = 0; i < length; i++) {
                            out.set(i, src.get(i));
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid array type.");
                }
            }
        }
        return out;
    }

    /**
     * Returns all elements of the specified source array for which the corresponding mask element is equal to 1.
     *
     * @param src  the source array
     * @param mask the mask array
     * <p>
     * @return selection of elements from the source array
     */
    public static com.pl.edu.icm.jlargearrays.LargeArray select(final com.pl.edu.icm.jlargearrays.LargeArray src, final LogicLargeArray mask)
    {
        if (src.length != mask.length) {
            throw new IllegalArgumentException("src.length != mask.length");
        }
        long length = src.length;
        long count = 0;
        int nthreads = (int) FastMath.min(length, ConcurrencyUtils.getNumberOfThreads());
        long k = length / nthreads;
        ExecutorService pool = Executors.newCachedThreadPool();
        Future[] futures = new Future[nthreads];
        for (int j = 0; j < nthreads; j++) {
            final long firstIdx = j * k;
            final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
            futures[j] = pool.submit(new Callable()
            {
                @Override
                public Long call()
                {
                    long count = 0;
                    for (long k = firstIdx; k < lastIdx; k++) {
                        if (mask.getByte(k) == 1) count++;
                    }
                    return count;
                }
            });
        }
        try {
            for (int j = 0; j < nthreads; j++) {
                count += (Long) (futures[j].get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            for (long j = 0; j < length; j++) {
                if (mask.getByte(j) == 1) count++;
            }
        }

        if (count <= 0) return null;

        LargeArray res = create(src.getType(), count, false);
        k = 0;
        for (long j = 0; j < length; j++) {
            if (mask.getByte(j) == 1) {
                res.set(k++, src.get(j));
            }
        }

        return res;
    }
}
