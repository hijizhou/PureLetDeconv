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

import com.sun.xml.internal.ws.encoding.soap.SerializationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.pl.edu.icm.jlargearrays.LargeArray;
import com.pl.edu.icm.jlargearrays.LargeArrayType;
import com.pl.edu.icm.jlargearrays.LargeArrayUtils;
import com.pl.edu.icm.jlargearrays.MemoryCounter;
import com.pl.edu.icm.jlargearrays.ShortLargeArray;
import sun.misc.Cleaner;
import org.apache.commons.math3.util.FastMath;

/**
 *
 * An array of objects that can store up to 2<SUP>63</SUP> elements.
 *
 * @author Piotr Wendykier (p.wendykier@icm.edu.pl)
 */
public class ObjectLargeArray extends LargeArray
{

    private static final long serialVersionUID = -4096759496772248522L;
    private Object[] data;
    private com.pl.edu.icm.jlargearrays.ShortLargeArray objectLengths;
    private int maxObjectLength;
    private long size;
    private byte[] byteArray;

    /**
     * Creates new instance of this class. The maximal string length is set to 100.
     *
     * @param length number of elements
     */
    public ObjectLargeArray(long length)
    {
        this(length, 1024);
    }

    /**
     * Creates new instance of this class.
     *
     * @param length          number of elements
     * @param maxObjectLength maximal length of the object serialized to an array of bytes, it is ignored when number of elements is smaller than
     *                        LargeArray.getMaxSizeOf43bitArray()
     */
    public ObjectLargeArray(long length, int maxObjectLength)
    {
        this(length, maxObjectLength, true);
    }

    /**
     * Creates new instance of this class.
     *
     * @param length           number of elements
     * @param maxObjectLength  maximal length of the object serialized to an array of bytes, it is ignored when number of elements is smaller than
     *                         LargeArray.getMaxSizeOf43bitArray()
     * @param zeroNativeMemory if true, then the native memory is zeroed.
     */
    public ObjectLargeArray(long length, int maxObjectLength, boolean zeroNativeMemory)
    {
        this.type = com.pl.edu.icm.jlargearrays.LargeArrayType.OBJECT;
        this.sizeof = 1;
        if (length <= 0) {
            throw new IllegalArgumentException(length + " is not a positive long value.");
        }
        if (maxObjectLength <= 0) {
            throw new IllegalArgumentException(maxObjectLength + " is not a positive int value.");
        }
        this.length = length;
        this.size = length * (long) maxObjectLength;
        this.maxObjectLength = maxObjectLength;
        if (length > getMaxSizeOf32bitArray()) {
            this.ptr = com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.allocateMemory(this.size * this.sizeof);
            if (zeroNativeMemory) {
                zeroNativeMemory(this.size);
            }
            Cleaner.create(this, new Deallocator(this.ptr, this.size, this.sizeof));
            MemoryCounter.increaseCounter(this.size * this.sizeof);
            objectLengths = new ShortLargeArray(length);
            byteArray = new byte[maxObjectLength];
        } else {
            data = new Object[(int) length];
        }
    }

    /**
     * Creates a constant array.
     * <p>
     * @param length        number of elements
     * @param constantValue value
     */
    public ObjectLargeArray(long length, Object constantValue)
    {
        this.type = com.pl.edu.icm.jlargearrays.LargeArrayType.OBJECT;
        this.sizeof = 1;
        if (length <= 0) {
            throw new IllegalArgumentException(length + " is not a positive long value");
        }
        this.length = length;
        this.isConstant = true;
        this.data = new Object[]{constantValue};
    }

    /**
     * Creates new instance of this class.
     *
     * @param data data array, this reference is used internally.
     */
    public ObjectLargeArray(Object[] data)
    {
        this.type = LargeArrayType.OBJECT;
        this.sizeof = 1;
        this.length = data.length;
        this.data = data;
    }

    /**
     * Returns a deep copy of this instance. (The elements themselves are copied.)
     *
     * @return a clone of this instance
     */
    @Override
    public ObjectLargeArray clone()
    {
        if (isConstant) {
            return new ObjectLargeArray(length, get(0));
        } else {
            ObjectLargeArray v = new ObjectLargeArray(length, FastMath.max(1, maxObjectLength), false);
            com.pl.edu.icm.jlargearrays.LargeArrayUtils.arraycopy(this, 0, v, 0, length);
            return v;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (super.equals(o)) {
            ObjectLargeArray la = (ObjectLargeArray) o;
            boolean res = this.maxObjectLength == la.maxObjectLength && this.data == la.data;
            if (this.objectLengths != null && la.objectLengths != null) {
                return res && this.objectLengths.equals(la.objectLengths);
            } else if (this.objectLengths == la.objectLengths) {
                return res;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 29 * super.hashCode() + (this.data != null ? this.data.hashCode() : 0);
        hash = 29 * hash + (int) (this.maxObjectLength ^ (this.maxObjectLength >>> 16));
        return 29 * hash + (this.objectLengths != null ? this.objectLengths.hashCode() : 0);
    }

    @Override
    public final Object get(long i)
    {
        if (ptr != 0) {
            short objLen = objectLengths.getShort(i);
            if (objLen < 0) return null;
            long offset = sizeof * i * maxObjectLength;
            for (int j = 0; j < objLen; j++) {
                byteArray[j] = com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getByte(ptr + offset + sizeof * j);
            }
            return fromByteArray(byteArray);
        } else if (isConstant) {
            return data[0];
        } else {
            return data[(int) i];
        }
    }

    @Override
    public final Object getFromNative(long i)
    {
        short objLen = objectLengths.getShort(i);
        if (objLen < 0) return null;
        long offset = sizeof * i * maxObjectLength;
        for (int j = 0; j < objLen; j++) {
            byteArray[j] = com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.getByte(ptr + offset + sizeof * j);
        }
        return fromByteArray(byteArray);
    }

    @Override
    public final boolean getBoolean(long i)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final byte getByte(long i)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final short getUnsignedByte(long i)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final short getShort(long i)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final int getInt(long i)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final long getLong(long i)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final float getFloat(long i)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final double getDouble(long i)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final Object[] getData()
    {
        return data;
    }

    @Override
    public final boolean[] getBooleanData()
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final boolean[] getBooleanData(boolean[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final byte[] getByteData()
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final byte[] getByteData(byte[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final short[] getShortData()
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final short[] getShortData(short[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final int[] getIntData()
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final int[] getIntData(int[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final long[] getLongData()
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final long[] getLongData(long[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final float[] getFloatData()
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final float[] getFloatData(float[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final double[] getDoubleData()
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final double[] getDoubleData(double[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final void setToNative(long i, Object value)
    {
        if (value == null) {
            objectLengths.setShort(i, (short) -1);
        } else {
            byte[] ba = toByteArray(value);
            if (ba.length > maxObjectLength) {
                throw new IllegalArgumentException("Object  " + value + " is too long.");
            }
            int objLen = ba.length;
            if (objLen > Short.MAX_VALUE) {
                throw new IllegalArgumentException("Object  " + value + " is too long.");
            }
            objectLengths.setShort(i, (short) objLen);
            long offset = sizeof * i * maxObjectLength;
            for (int j = 0; j < objLen; j++) {
                com.pl.edu.icm.jlargearrays.LargeArrayUtils.UNSAFE.putByte(ptr + offset + sizeof * j, ba[j]);
            }
        }
    }

    @Override
    public final void set(long i, Object o)
    {

        if (o == null) {
            if (ptr != 0) {
                objectLengths.setShort(i, (short) -1);
            } else {
                if (isConstant) {
                    throw new IllegalAccessError("Constant arrays cannot be modified.");
                }
                data[(int) i] = null;
            }
        } else if (ptr != 0) {
            byte[] ba = toByteArray(o);
            if (ba.length > maxObjectLength) {
                throw new IllegalArgumentException("Object  " + o + " is too long.");
            }
            int objLen = ba.length;
            if (objLen > Short.MAX_VALUE) {
                throw new IllegalArgumentException("Object  " + o + " is too long.");
            }
            objectLengths.setShort(i, (short) objLen);
            long offset = sizeof * i * maxObjectLength;
            for (int j = 0; j < objLen; j++) {
                LargeArrayUtils.UNSAFE.putByte(ptr + offset + sizeof * j, ba[j]);
            }
        } else {
            if (isConstant) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            data[(int) i] = o;
        }
    }

    @Override
    public final void set_safe(long i, Object value)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        set(i, value);
    }

    @Override
    public final void setBoolean(long i, boolean value)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final void setByte(long i, byte value)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final void setUnsignedByte(long i, short value)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final void setShort(long i, short value)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final void setInt(long i, int value)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final void setLong(long i, long value)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final void setFloat(long i, float value)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public final void setDouble(long i, double value)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    private static byte[] toByteArray(final Object obj)
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);

        } catch (Exception ex) {
            throw new SerializationException(ex);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return baos.toByteArray();
    }

    private static Object fromByteArray(final byte[] objectData)
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(objectData);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bais);
            final Object obj = ois.readObject();
            return obj;

        } catch (Exception ex) {
            throw new SerializationException(ex);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    /**
     * Returns maximal length of each object serialized to an array of bytes.
     * <p>
     * @return maximal length of each object serialized to an array of bytes
     *
     */
    public int getMaxObjectLength()
    {
        return maxObjectLength;
    }

}
