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

import com.pl.edu.icm.jlargearrays.LargeArray;
import com.pl.edu.icm.jlargearrays.LargeArrayType;
import com.pl.edu.icm.jlargearrays.LargeArrayUtils;
import com.pl.edu.icm.jlargearrays.MemoryCounter;
import sun.misc.Cleaner;
import org.apache.commons.math3.util.FastMath;

/**
 *
 * An array of doubles that can store up to 2<SUP>63</SUP> elements.
 *
 * @author Piotr Wendykier (p.wendykier@icm.edu.pl)
 */
public class DoubleLargeArray extends com.pl.edu.icm.jlargearrays.LargeArray
{

    private static final long serialVersionUID = 7436383149749497101L;
    private double[] data;

    /**
     * Creates new instance of this class.
     *
     * @param length number of elements
     */
    public DoubleLargeArray(long length)
    {
        this(length, true);
    }

    /**
     * Creates new instance of this class.
     *
     * @param length           number of elements
     * @param zeroNativeMemory if true, then the native memory is zeroed.
     */
    public DoubleLargeArray(long length, boolean zeroNativeMemory)
    {
        this.type = com.pl.edu.icm.jlargearrays.LargeArrayType.DOUBLE;
        this.sizeof = 8;
        if (length <= 0) {
            throw new IllegalArgumentException(length + " is not a positive long value");
        }
        this.length = length;
        if (length > getMaxSizeOf32bitArray()) {
            this.ptr = com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.allocateMemory(this.length * this.sizeof);
            if (zeroNativeMemory) {
                zeroNativeMemory(length);
            }
            Cleaner.create(this, new Deallocator(this.ptr, this.length, this.sizeof));
            MemoryCounter.increaseCounter(this.length * this.sizeof);
        } else {
            data = new double[(int) length];
        }
    }

    /**
     * Creates a constant array.
     * <p>
     * @param length        number of elements
     * @param constantValue value
     */
    public DoubleLargeArray(long length, double constantValue)
    {
        this.type = com.pl.edu.icm.jlargearrays.LargeArrayType.DOUBLE;
        this.sizeof = 8;
        if (length <= 0) {
            throw new IllegalArgumentException(length + " is not a positive long value");
        }
        this.length = length;
        this.isConstant = true;
        this.data = new double[]{constantValue};
    }

    /**
     * Creates new instance of this class.
     *
     * @param data data array, this reference is used internally.
     */
    public DoubleLargeArray(double[] data)
    {
        this.type = LargeArrayType.DOUBLE;
        this.sizeof = 8;
        this.length = data.length;
        this.data = data;
    }

    /**
     * Returns a deep copy of this instance. (The elements themselves are copied.)
     *
     * @return a clone of this instance
     */
    @Override
    public DoubleLargeArray clone()
    {
        if (isConstant) {
            return new DoubleLargeArray(length, getDouble(0));
        } else {
            DoubleLargeArray v = new DoubleLargeArray(length, false);
            com.pl.edu.icm.jlargearrays.LargeArrayUtils.arraycopy(this, 0, v, 0, length);
            return v;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (super.equals(o)) {
            DoubleLargeArray la = (DoubleLargeArray) o;
            return this.data == la.data;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return 29 * super.hashCode() + (this.data != null ? this.data.hashCode() : 0);
    }

    @Override
    public final Double get(long i)
    {
        return getDouble(i);
    }

    @Override
    public final Double getFromNative(long i)
    {
        return com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
    }

    @Override
    public final boolean getBoolean(long i)
    {
        if (ptr != 0) {
            return (com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i)) != 0;
        } else if (isConstant) {
            return data[0] != 0;
        } else {
            return data[(int) i] != 0;
        }
    }

    @Override
    public final byte getByte(long i)
    {
        if (ptr != 0) {
            return (byte) (com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i));
        } else if (isConstant) {
            return (byte) data[0];
        } else {
            return (byte) data[(int) i];
        }
    }

    @Override
    public final short getUnsignedByte(long i)
    {
        if (ptr != 0) {
            return (short) (0xFF & (int) com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i));
        } else if (isConstant) {
            return (short) (0xFF & (int) data[0]);
        } else {
            return (short) (0xFF & (int) data[(int) i]);
        }
    }

    @Override
    public final short getShort(long i)
    {
        if (ptr != 0) {
            return (short) (com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i));
        } else if (isConstant) {
            return (short) data[0];
        } else {
            return (short) data[(int) i];
        }
    }

    @Override
    public final int getInt(long i)
    {
        if (ptr != 0) {
            return (int) (com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i));
        } else if (isConstant) {
            return (int) data[0];
        } else {
            return (int) data[(int) i];
        }
    }

    @Override
    public final long getLong(long i)
    {
        if (ptr != 0) {
            return (long) (com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i));
        } else if (isConstant) {
            return (long) data[0];
        } else {
            return (long) data[(int) i];
        }
    }

    @Override
    public final float getFloat(long i)
    {
        if (ptr != 0) {
            return (float) (com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i));
        } else if (isConstant) {
            return (float) data[0];
        } else {
            return (float) data[(int) i];
        }
    }

    @Override
    public final double getDouble(long i)
    {
        if (ptr != 0) {
            return com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
        } else if (isConstant) {
            return data[0];
        } else {
            return data[(int) i];
        }
    }

    @Override
    public final double[] getData()
    {
        return data;
    }

    @Override
    public final boolean[] getBooleanData()
    {
        if (length > com.pl.edu.icm.jlargearrays.LargeArray.LARGEST_SUBARRAY) return null;
        boolean[] out = new boolean[(int) length];
        if (ptr != 0) {
            for (int i = 0; i < length; i++) {
                double v = com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
                out[i] = v != 0;
            }
        } else if (isConstant) {
            boolean elem = data[0] != 0;
            for (int i = 0; i < length; i++) {
                out[i] = elem;
            }
        } else {
            for (int i = 0; i < length; i++) {
                out[i] = data[i] != 0;

            }
        }
        return out;
    }

    @Override
    public final boolean[] getBooleanData(boolean[] a, long startPos, long endPos, long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) FastMath.ceil((endPos - startPos) / (double) step);
        if (len > com.pl.edu.icm.jlargearrays.LargeArray.LARGEST_SUBARRAY) {
            return null;
        } else {
            boolean[] out;
            if (a != null && a.length >= len) {
                out = a;
            } else {
                out = new boolean[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    double v = com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
                    out[idx++] = v != 0;
                }
            } else if (isConstant) {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = data[0] != 0;
                }
            } else {
                for (long i = startPos; i < endPos; i += step) {
                    double v = data[(int) i];
                    out[idx++] = v != 0;
                }
            }
            return out;
        }
    }

    @Override
    public final byte[] getByteData()
    {
        if (length > com.pl.edu.icm.jlargearrays.LargeArray.LARGEST_SUBARRAY) return null;
        byte[] out = new byte[(int) length];
        if (ptr != 0) {
            for (int i = 0; i < length; i++) {
                out[i] = (byte) com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
            }
        } else if (isConstant) {
            double elem = data[0];
            for (int i = 0; i < length; i++) {
                out[i] = (byte)elem;
            }
        } else {
            for (int i = 0; i < length; i++) {
                out[i] = (byte)data[i];

            }
        }
        return out;
    }

    @Override
    public final byte[] getByteData(byte[] a, long startPos, long endPos, long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) FastMath.ceil((endPos - startPos) / (double) step);
        if (len > com.pl.edu.icm.jlargearrays.LargeArray.LARGEST_SUBARRAY) {
            return null;
        } else {
            byte[] out;
            if (a != null && a.length >= len) {
                out = a;
            } else {
                out = new byte[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (byte) com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
                }
            } else if (isConstant) {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (byte) data[0];
                }
            } else {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (byte) data[(int) i];
                }
            }
            return out;
        }
    }

    @Override
    public final short[] getShortData()
    {
        if (length > com.pl.edu.icm.jlargearrays.LargeArray.LARGEST_SUBARRAY) return null;
        short[] out = new short[(int) length];
        if (ptr != 0) {
            for (int i = 0; i < length; i++) {
                out[i] = (short) com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
            }
        } else if (isConstant) {
            double elem = data[0];
            for (int i = 0; i < length; i++) {
                out[i] = (short)elem;
            }
        } else {
            for (int i = 0; i < length; i++) {
                out[i] = (short)data[i];

            }
        }
        return out;
    }

    @Override
    public final short[] getShortData(short[] a, long startPos, long endPos, long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) FastMath.ceil((endPos - startPos) / (double) step);
        if (len > com.pl.edu.icm.jlargearrays.LargeArray.LARGEST_SUBARRAY) {
            return null;
        } else {
            short[] out;
            if (a != null && a.length >= len) {
                out = a;
            } else {
                out = new short[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (short) com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
                }
            } else if (isConstant) {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (short) data[0];
                }
            } else {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (short) data[(int) i];
                }
            }
            return out;
        }
    }

    @Override
    public final int[] getIntData()
    {
        if (length > com.pl.edu.icm.jlargearrays.LargeArray.LARGEST_SUBARRAY) return null;
        int[] out = new int[(int) length];
        if (ptr != 0) {
            for (int i = 0; i < length; i++) {
                out[i] = (int) com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
            }
        } else if (isConstant) {
            double elem = data[0];
            for (int i = 0; i < length; i++) {
                out[i] = (int)elem;
            }
        } else {
            for (int i = 0; i < length; i++) {
                out[i] = (int)data[i];

            }
        }
        return out;
    }

    @Override
    public final int[] getIntData(int[] a, long startPos, long endPos, long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) FastMath.ceil((endPos - startPos) / (double) step);
        if (len > com.pl.edu.icm.jlargearrays.LargeArray.LARGEST_SUBARRAY) {
            return null;
        } else {
            int[] out;
            if (a != null && a.length >= len) {
                out = a;
            } else {
                out = new int[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (int) com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
                }
            } else if (isConstant) {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (int) data[0];
                }
            } else {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (int) data[(int) i];
                }
            }
            return out;
        }
    }

    @Override
    public final long[] getLongData()
    {
        if (length > com.pl.edu.icm.jlargearrays.LargeArray.LARGEST_SUBARRAY) return null;
        long[] out = new long[(int) length];
        if (ptr != 0) {
            for (int i = 0; i < length; i++) {
                out[i] = (long) com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
            }
        } else if (isConstant) {
            double elem = data[0];
            for (int i = 0; i < length; i++) {
                out[i] = (long)elem;
            }
        } else {
            for (int i = 0; i < length; i++) {
                out[i] = (long)data[i];

            }
        }
        return out;
    }

    @Override
    public final long[] getLongData(long[] a, long startPos, long endPos, long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) FastMath.ceil((endPos - startPos) / (double) step);
        if (len > com.pl.edu.icm.jlargearrays.LargeArray.LARGEST_SUBARRAY) {
            return null;
        } else {
            long[] out;
            if (a != null && a.length >= len) {
                out = a;
            } else {
                out = new long[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (long) com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
                }
            } else if (isConstant) {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (long) data[0];
                }
            } else {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (long) data[(int) i];
                }
            }
            return out;
        }
    }

    @Override
    public final float[] getFloatData()
    {
        if (length > com.pl.edu.icm.jlargearrays.LargeArray.LARGEST_SUBARRAY) return null;
        float[] out = new float[(int) length];
        if (ptr != 0) {
            for (int i = 0; i < length; i++) {
                out[i] = (float) com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
            }
        } else if (isConstant) {
            double elem = data[0];
            for (int i = 0; i < length; i++) {
                out[i] = (float)elem;
            }
        } else {
            for (int i = 0; i < length; i++) {
                out[i] = (float)data[i];

            }
        }
        return out;
    }

    @Override
    public final float[] getFloatData(float[] a, long startPos, long endPos, long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) FastMath.ceil((endPos - startPos) / (double) step);
        if (len > com.pl.edu.icm.jlargearrays.LargeArray.LARGEST_SUBARRAY) {
            return null;
        } else {
            float[] out;
            if (a != null && a.length >= len) {
                out = a;
            } else {
                out = new float[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (float) com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
                }
            } else if (isConstant) {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (float) data[0];
                }
            } else {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = (float) data[(int) i];
                }
            }
            return out;
        }
    }

    @Override
    public final double[] getDoubleData()
    {
        if (length > com.pl.edu.icm.jlargearrays.LargeArray.LARGEST_SUBARRAY) return null;
        double[] out = new double[(int) length];
        if (ptr != 0) {
            for (int i = 0; i < length; i++) {
                out[i] = com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
            }
        } else if (isConstant) {
            double elem = data[0];
            for (int i = 0; i < length; i++) {
                out[i] = elem;
            }
        } else {
            System.arraycopy(data, 0, out, 0, (int)length);
        }
        return out;
    }

    @Override
    public final double[] getDoubleData(double[] a, long startPos, long endPos, long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) FastMath.ceil((endPos - startPos) / (double) step);
        if (len > LargeArray.LARGEST_SUBARRAY) {
            return null;
        } else {
            double[] out;
            if (a != null && a.length >= len) {
                out = a;
            } else {
                out = new double[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getDouble(ptr + sizeof * i);
                }
            } else if (isConstant) {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = data[0];
                }
            } else {
                for (long i = startPos; i < endPos; i += step) {
                    out[idx++] = data[(int) i];
                }
            }
            return out;
        }
    }

    @Override
    public final void setToNative(long i, Object value)
    {
        com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.putDouble(ptr + sizeof * i, (Double) value);
    }

    @Override
    public final void setBoolean(long i, boolean value)
    {
        if (ptr != 0) {
            com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.putDouble(ptr + sizeof * i, value == true ? 1.0 : 0.0);
        } else {
            if (isConstant) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            data[(int) i] = value == true ? 1.0 : 0.0;
        }
    }

    @Override
    public final void setByte(long i, byte value)
    {
        if (ptr != 0) {
            com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.putDouble(ptr + sizeof * i, (double) value);
        } else {
            if (isConstant) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            data[(int) i] = (double) value;
        }
    }

    @Override
    public final void setUnsignedByte(long i, short value)
    {
        setShort(i, value);
    }

    @Override
    public void setShort(long i, short value)
    {
        if (ptr != 0) {
            com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.putDouble(ptr + sizeof * i, (double) value);
        } else {
            if (isConstant) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            data[(int) i] = (double) value;
        }
    }

    @Override
    public final void setInt(long i, int value)
    {
        if (ptr != 0) {
            com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.putDouble(ptr + sizeof * i, (double) value);
        } else {
            if (isConstant) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            data[(int) i] = (double) value;
        }
    }

    @Override
    public final void setLong(long i, long value)
    {
        if (ptr != 0) {
            com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.putDouble(ptr + sizeof * i, (double) value);
        } else {
            if (isConstant) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            data[(int) i] = (double) value;
        }
    }

    @Override
    public final void setFloat(long i, float value)
    {
        if (ptr != 0) {
            com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.putDouble(ptr + sizeof * i, (double) value);
        } else {
            if (isConstant) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            data[(int) i] = (double) value;
        }
    }

    @Override
    public final void setDouble(long i, double value)
    {
        if (ptr != 0) {
            LargeArrayUtils.UNSAFE.putDouble(ptr + sizeof * i, value);
        } else {
            if (isConstant) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            data[(int) i] = value;
        }
    }
}
