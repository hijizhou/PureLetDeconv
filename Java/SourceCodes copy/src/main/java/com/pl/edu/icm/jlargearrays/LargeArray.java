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
import com.pl.edu.icm.jlargearrays.ConcurrencyUtils;
import com.pl.edu.icm.jlargearrays.LargeArrayType;
import com.pl.edu.icm.jlargearrays.LargeArrayUtils;
import com.pl.edu.icm.jlargearrays.MemoryCounter;

/**
 * The base class for all large arrays. All implementations of this abstract
 * class can store up to 2<SUP>63</SUP> elements of primitive data types.
 *
 * @author Piotr Wendykier (p.wendykier@icm.edu.pl)
 */
public abstract class LargeArray implements
    java.io.Serializable,
    Cloneable
{

    private static final long serialVersionUID = 7921589398878016801L;
    protected LargeArrayType type;
    protected long length;
    protected long sizeof;
    protected boolean isConstant = false;
    protected Object parent = null;
    protected long ptr = 0;

    /**
     * The largest array size for which a regular 1D Java array is used to store the
     * data.
     */
    private static int maxSizeOf32bitArray = 1073741824; // 2^30;

    /**
     * The largest size of subarray returned by getXXXData() methods.
     */
    public static final int LARGEST_SUBARRAY = 1073741824; // 2^30;

    /**
     * Creates new instance of this class.
     */
    protected LargeArray()
    {
        super();
    }

    /**
     * Creates new instance of this class by wrapping a native pointer.
     * Providing an invalid pointer, parent or length will result in
     * unpredictable behavior and likely JVM crash. The assumption is that the
     * pointer is valid as long as the parent is not garbage collected.
     *
     * @param parent         class instance responsible for handling the pointer's life
     *                       cycle, the created instance of LargeArray will prevent the GC from
     *                       reclaiming the parent.
     * @param nativePointer  native pointer to wrap.
     * @param largeArrayType type of array
     * @param length         array length
     */
    public LargeArray(final Object parent,
                      final long nativePointer,
                      final LargeArrayType largeArrayType,
                      final long length)
    {
        super();
        this.parent = parent;
        this.ptr = nativePointer;
        this.type = largeArrayType;
        this.sizeof = largeArrayType.sizeOf();
        if (length <= 0) {
            throw new IllegalArgumentException(length + " is not a positive long value");
        }
        this.length = length;
    }

    /**
     * Returns the internal pointer address (if isLarge)
     *
     * @return native pointer address or 0 if not large array.
     */
    public long nativePointer()
    {
        return ptr;
    }

    /**
     * Returns the length of an array.
     *
     * @return the length of an array
     */
    public long length()
    {
        return length;
    }

    /**
     * Returns the type of an array.
     *
     * @return the type of an array
     */
    public LargeArrayType getType()
    {
        return type;
    }

    /**
     * Returns a value at index i. Array bounds are not checked. Calling this
     * method with invalid index argument will cause JVM crash.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public abstract Object get(long i);

    /**
     * Returns a value at index i. Array bounds are checked.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public Object get_safe(final long i)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        return get(i);
    }

    /**
     * Returns a value at index i. Array bounds are not checked. If isLarge()
     * returns false for a given array or the index argument is invalid, then
     * calling this method will cause JVM crash.
     *
     * @param i index
     *
     * @return a value at index i. The type of returned value is the same as the
     *         type of this array.
     */
    public abstract Object getFromNative(long i);

    /**
     * Returns a boolean value at index i. Array bounds are not checked. Calling
     * this method with invalid index argument will cause JVM crash.
     *
     * @param i an index
     *
     * @return a boolean value at index i.
     */
    public abstract boolean getBoolean(long i);

    /**
     * Returns a boolean value at index i. Array bounds are checked.
     *
     * @param i an index
     *
     * @return a boolean value at index i.
     */
    public boolean getBoolean_safe(final long i)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        return getBoolean(i);
    }

    /**
     * Returns a signed byte value at index i. Array bounds are not checked. Calling
     * this method with invalid index argument will cause JVM crash.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public abstract byte getByte(long i);

    /**
     * Returns a signed byte value at index i. Array bounds are checked.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public byte getByte_safe(final long i)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        return getByte(i);
    }

    /**
     * Returns an unsigned byte value at index i. Array bounds are not checked. Calling
     * this method with invalid index argument will cause JVM crash.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public abstract short getUnsignedByte(long i);

    /**
     * Returns an unsigned byte value at index i. Array bounds are checked.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public short getUnsignedByte_safe(final long i)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        return getUnsignedByte(i);
    }

    /**
     * Returns a short value at index i. Array bounds are not checked. Calling
     * this method with invalid index argument will cause JVM crash.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public abstract short getShort(long i);

    /**
     * Returns a short value at index i. Array bounds are checked.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public short getShort_safe(final long i)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        return getShort(i);
    }

    /**
     * Returns an int value at index i. Array bounds are not checked. Calling
     * this method with invalid index argument will cause JVM crash.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public abstract int getInt(long i);

    /**
     * Returns an int value at index i. Array bounds are checked.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public int getInt_safe(final long i)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        return getInt(i);
    }

    /**
     * Returns a long value at index i. Array bounds are not checked. Calling
     * this method with invalid index argument will cause JVM crash.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public abstract long getLong(long i);

    /**
     * Returns a long value at index i. Array bounds are checked.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public long getLong_safe(final long i)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        return getLong(i);
    }

    /**
     * Returns a float value at index i. Array bounds are not checked. Calling
     * this method with invalid index argument will cause JVM crash.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public abstract float getFloat(long i);

    /**
     * Returns a float value at index i. Array bounds are checked.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public float getFloat_safe(final long i)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        return getFloat(i);
    }

    /**
     * Returns a double value at index i. Array bounds are not checked. Calling
     * this method with invalid index argument will cause JVM crash.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public abstract double getDouble(long i);

    /**
     * Returns a double value at index i. Array bounds are checked.
     *
     * @param i an index
     *
     * @return a value at index i.
     */
    public double getDouble_safe(final long i)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        return getDouble(i);
    }

    /**
     * Returns a reference to the internal data array. For constant arrays the length of the returned array is always equal to 1.
     * If isLarge() returns true, then this method returns null.
     *
     * @return reference to the internal data array or null
     */
    public abstract Object getData();

    /**
     * If the size of the array is smaller than LargeArray.LARGEST_SUBARRAY, then this
     * method returns boolean data. Otherwise, it returns null.
     *
     * @return an array containing the elements of this object or null
     */
    public abstract boolean[] getBooleanData();

    /**
     * If (endPos - startPos) / step is smaller than LargeArray.LARGEST_SUBARRAY, then
     * this method returns selected elements of this object. Otherwise, it returns
     * null. If (endPos - startPos) / step is smaller or equal to a.length, it
     * is returned therein. Otherwise, a new array is allocated and returned.
     * Array bounds are checked.
     *
     * @param a        the array into which the elements are to be stored, if it is big
     *                 enough; otherwise, a new array of is allocated for this purpose.
     * @param startPos starting position (included)
     * @param endPos   ending position (excluded)
     * @param step     step size
     *
     * @return an array containing the elements of this object or null
     */
    public abstract boolean[] getBooleanData(boolean[] a,
                                             long startPos,
                                             long endPos,
                                             long step);

    /**
     * If the size of the array is smaller than LargeArray.LARGEST_SUBARRAY, then this
     * method returns byte data. Otherwise, it returns null.
     *
     * @return an array containing the elements of this object or null
     */
    public abstract byte[] getByteData();

    /**
     * If (endPos - startPos) / step is smaller than LargeArray.LARGEST_SUBARRAY, then
     * this method returns selected elements of this object. Otherwise, it returns
     * null. If (endPos - startPos) / step is smaller or equal to a.length, it
     * is returned therein. Otherwise, a new array is allocated and returned.
     * Array bounds are checked.
     *
     * @param a        the array into which the elements are to be stored, if it is big
     *                 enough; otherwise, a new array of is allocated for this purpose.
     * @param startPos starting position (included)
     * @param endPos   ending position (excluded)
     * @param step     step size
     *
     * @return an array containing the elements of this object or null
     */
    public abstract byte[] getByteData(byte[] a,
                                       long startPos,
                                       long endPos,
                                       long step);

    /**
     * If the size of the array is smaller than LargeArray.LARGEST_SUBARRAY, then this
     * method returns short data. Otherwise, it returns null.
     *
     * @return an array containing the elements of this object or null
     */
    public abstract short[] getShortData();

    /**
     * If (endPos - startPos) / step is smaller than LargeArray.LARGEST_SUBARRAY, then
     * this method returns selected elements of this object. Otherwise, it returns
     * null. If (endPos - startPos) / step is smaller or equal to a.length, it
     * is returned therein. Otherwise, a new array is allocated and returned.
     * Array bounds are checked.
     *
     * @param a        the array into which the elements are to be stored, if it is big
     *                 enough; otherwise, a new array of is allocated for this purpose.
     * @param startPos starting position (included)
     * @param endPos   ending position (excluded)
     * @param step     step size
     *
     * @return an array containing the elements of this object or null
     */
    public abstract short[] getShortData(short[] a,
                                         long startPos,
                                         long endPos,
                                         long step);

    /**
     * If the size of the array is smaller than LargeArray.LARGEST_SUBARRAY, then this
     * method returns int data. Otherwise, it returns null.
     *
     * @return an array containing the elements of this object or null
     */
    public abstract int[] getIntData();

    /**
     * If (endPos - startPos) / step is smaller than LargeArray.LARGEST_SUBARRAY, then
     * this method returns selected elements of this object. Otherwise, it returns
     * null. If (endPos - startPos) / step is smaller or equal to a.length, it
     * is returned therein. Otherwise, a new array is allocated and returned.
     * Array bounds are checked.
     *
     * @param a        the array into which the elements are to be stored, if it is big
     *                 enough; otherwise, a new array of is allocated for this purpose.
     * @param startPos starting position (included)
     * @param endPos   ending position (excluded)
     * @param step     step size
     *
     * @return an array containing the elements of this object or null
     */
    public abstract int[] getIntData(int[] a,
                                     long startPos,
                                     long endPos,
                                     long step);

    /**
     * If the size of the array is smaller than LargeArray.LARGEST_SUBARRAY, then this
     * method returns long data. Otherwise, it returns null.
     *
     * @return an array containing the elements of this object or null
     */
    public abstract long[] getLongData();

    /**
     * If (endPos - startPos) / step is smaller than LargeArray.LARGEST_SUBARRAY, then
     * this method returns selected elements of this object. Otherwise, it returns
     * null. If (endPos - startPos) / step is smaller or equal to a.length, it
     * is returned therein. Otherwise, a new array is allocated and returned.
     * Array bounds are checked.
     *
     * @param a        the array into which the elements are to be stored, if it is big
     *                 enough; otherwise, a new array of is allocated for this purpose.
     * @param startPos starting position (included)
     * @param endPos   ending position (excluded)
     * @param step     step size
     *
     * @return an array containing the elements of this object or null
     */
    public abstract long[] getLongData(long[] a,
                                       long startPos,
                                       long endPos,
                                       long step);

    /**
     * If the size of the array is smaller than LargeArray.LARGEST_SUBARRAY, then this
     * method returns float data. Otherwise, it returns null.
     *
     * @return an array containing the elements of this object or null
     */
    public abstract float[] getFloatData();

    /**
     * If (endPos - startPos) / step is smaller than LargeArray.LARGEST_SUBARRAY, then
     * this method returns selected elements of this object. Otherwise, it returns
     * null. If (endPos - startPos) / step is smaller or equal to a.length, it
     * is returned therein. Otherwise, a new array is allocated and returned.
     * Array bounds are checked.
     *
     * @param a        the array into which the elements are to be stored, if it is big
     *                 enough; otherwise, a new array of is allocated for this purpose.
     * @param startPos starting position (included)
     * @param endPos   ending position (excluded)
     * @param step     step size
     *
     * @return an array containing the elements of this object or null
     */
    public abstract float[] getFloatData(float[] a,
                                         long startPos,
                                         long endPos,
                                         long step);

    /**
     * If the size of the array is smaller than LargeArray.LARGEST_SUBARRAY, then this
     * method returns double data. Otherwise, it returns null.
     *
     * @return an array containing the elements of this object or null
     */
    public abstract double[] getDoubleData();

    /**
     * If (endPos - startPos) / step is smaller than LargeArray.LARGEST_SUBARRAY, then
     * this method returns selected elements of this object. Otherwise, it returns
     * null. If (endPos - startPos) / step is smaller or equal to a.length, it
     * is returned therein. Otherwise, a new array is allocated and returned.
     * Array bounds are checked.
     *
     * @param a        the array into which the elements are to be stored, if it is big
     *                 enough; otherwise, a new array of is allocated for this purpose.
     * @param startPos starting position (included)
     * @param endPos   ending position (excluded)
     * @param step     step size
     *
     * @return an array containing the elements of this object or null
     */
    public abstract double[] getDoubleData(double[] a,
                                           long startPos,
                                           long endPos,
                                           long step);

    /**
     * Sets a value at index i. Array bounds are not checked. Calling this
     * method with invalid index argument will cause JVM crash.
     *
     * @param i     index
     * @param value value to set
     */
    public void set(final long i, final Object value)
    {
        if (value instanceof Boolean) {
            setBoolean(i, (Boolean) value);
        } else if (value instanceof Byte) {
            setByte(i, (Byte) value);
        } else if (value instanceof Short) {
            setShort(i, (Short) value);
        } else if (value instanceof Integer) {
            setInt(i, (Integer) value);
        } else if (value instanceof Long) {
            setLong(i, (Long) value);
        } else if (value instanceof Float) {
            setFloat(i, (Float) value);
        } else if (value instanceof Double) {
            setDouble(i, (Double) value);
        } else {
            throw new IllegalArgumentException("Unsupported type.");
        }
    }

    /**
     * Sets a value at index i. Array bounds are not checked. If isLarge()
     * returns false for a given array or the index argument is invalid, then
     * calling this method will cause JVM crash.
     *
     * @param i     index
     * @param value value to set
     *
     * @throws ClassCastException if the type of value argument is different
     *                            than the type of the array
     */
    public abstract void setToNative(long i, Object value);

    /**
     * Sets a value at index i. Array bounds are checked.
     *
     * @param i     index
     * @param value value to set
     */
    public void set_safe(final long i, final Object value)
    {
        if (value instanceof Boolean) {
            setBoolean_safe(i, (Boolean) value);
        } else if (value instanceof Byte) {
            setByte_safe(i, (Byte) value);
        } else if (value instanceof Short) {
            setShort_safe(i, (Short) value);
        } else if (value instanceof Integer) {
            setInt_safe(i, (Integer) value);
        } else if (value instanceof Long) {
            setLong_safe(i, (Long) value);
        } else if (value instanceof Float) {
            setFloat_safe(i, (Float) value);
        } else if (value instanceof Double) {
            setDouble_safe(i, (Double) value);
        } else {
            throw new IllegalArgumentException("Unsupported type.");
        }
    }

    /**
     * Sets a boolean value at index i. Array bounds are not checked. Calling
     * this method with invalid index argument will cause JVM crash.
     *
     * @param i     index
     * @param value value to set
     */
    public abstract void setBoolean(long i, boolean value);

    /**
     * Sets a boolean value at index i. Array bounds are checked.
     *
     * @param i     index
     * @param value value to set
     */
    public void setBoolean_safe(final long i, final boolean value)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        setBoolean(i, value);
    }

    /**
     * Sets a byte value at index i. Array bounds are not checked. Calling this
     * method with invalid index argument will cause JVM crash.
     *
     * @param i     index
     * @param value value to set
     */
    public abstract void setByte(long i, byte value);

    /**
     * Sets a byte value at index i. Array bounds are checked.
     *
     * @param i     index
     * @param value value to set
     */
    public void setByte_safe(final long i, final byte value)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        setByte(i, value);
    }

    /**
     * Sets an unsigned byte value at index i. Array bounds are not checked. Calling this
     * method with invalid index argument will cause JVM crash.
     *
     * @param i     index
     * @param value value to set
     */
    public abstract void setUnsignedByte(long i, short value);

    /**
     * Sets an unsigned value at index i. Array bounds are checked.
     *
     * @param i     index
     * @param value value to set
     */
    public void setUnsignedByte_safe(final long i, final byte value)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        setUnsignedByte(i, value);
    }

    /**
     * Sets a short value at index i. Array bounds are not checked. Calling this
     * method with invalid index argument will cause JVM crash.
     *
     * @param i     index
     * @param value value to set
     */
    public abstract void setShort(long i, short value);

    /**
     * Sets a short value at index i. Array bounds are checked.
     *
     * @param i     index
     * @param value value to set
     */
    public void setShort_safe(final long i, final short value)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        setShort(i, value);
    }

    /**
     * Sets an int value at index i. Array bounds are not checked. Calling this
     * method with invalid index argument will cause JVM crash.
     *
     * @param i     index
     * @param value value to set
     */
    public abstract void setInt(long i, int value);

    /**
     * Sets an int value at index i. Array bounds are checked.
     *
     * @param i     index
     * @param value value to set
     */
    public void setInt_safe(final long i, final int value)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        setInt(i, value);
    }

    /**
     * Sets a long value at index i. Array bounds are not checked. Calling this
     * method with invalid index argument will cause JVM crash.
     *
     * @param i     index
     * @param value value to set
     */
    public abstract void setLong(long i, long value);

    /**
     * Sets a long value at index i. Array bounds are checked.
     *
     * @param i     index
     * @param value value to set
     */
    public void setLong_safe(final long i, final long value)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        setLong(i, value);
    }

    /**
     * Sets a float value at index i. Array bounds are not checked. Calling this
     * method with invalid index argument will cause JVM crash.
     *
     * @param i     index
     * @param value value to set
     */
    public abstract void setFloat(long i, float value);

    /**
     * Sets a float value at index i. Array bounds are checked.
     *
     * @param i     index
     * @param value value to set
     */
    public void setFloat_safe(final long i, final float value)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        setFloat(i, value);
    }

    /**
     * Sets a double value at index i. Array bounds are not checked. Calling
     * this method with invalid index argument will cause JVM crash.
     *
     * @param i     index
     * @param value value to set
     */
    public abstract void setDouble(long i, double value);

    /**
     * Sets a double value at index i. Array bounds are checked.
     *
     * @param i     index
     * @param value value to set
     */
    public void setDouble_safe(final long i, final double value)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        setDouble(i, value);
    }

    /**
     * Returns true if the size of an array is larger than LARGEST_SUBARRAY.
     *
     * @return true if the size of an array is larger than LARGEST_SUBARRAY,
     *         false otherwise.
     */
    public boolean isLarge()
    {
        return ptr != 0;
    }

    /**
     * Returns true if the type of the array is numeric, false otherwise.
     *
     * @return true if the type of the array is numeric, false otherwise.
     */
    public boolean isNumeric()
    {
        return type.isNumericType();
    }

    /**
     * Return true if the array is constant. Constant arrays cannot be modified,
     * i.e. all setters throw an IllegalAccessError exception.
     *
     * @return true if the arrays is constant, false otherwise
     */
    public boolean isConstant()
    {
        return isConstant;
    }

    /**
     * Sets the maximal size of a 32-bit array. For arrays of the size larger
     * than index, the data is stored in the memory allocated by
     * sun.misc.Unsafe.allocateMemory().
     *
     * @param index the maximal size of a 32-bit array.
     */
    public static void setMaxSizeOf32bitArray(final int index)
    {
        if (index < 0) {
            throw new IllegalArgumentException("index cannot be negative");
        }
        maxSizeOf32bitArray = index;
    }

    /**
     * Returns the maximal size of a 32-bit array.
     *
     * @return the maximal size of a 32-bit array.
     */
    public static int getMaxSizeOf32bitArray()
    {
        return maxSizeOf32bitArray;
    }

    @Override
    public Object clone()
    {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException exc) {
            throw new InternalError(); // should never happen
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof LargeArray))
            return false;
        LargeArray la = (LargeArray) o;
        boolean equal = this.type == la.type && this.length == la.length && this.sizeof == la.sizeof && this.isConstant == la.isConstant && this.ptr == la.ptr;
        if (this.parent != null && la.parent != null) {
            equal = equal && this.parent.equals(la.parent);
        } else if (this.parent == null && la.parent == null) {
            equal = equal && true;
        } else {
            equal = false;
        }
        return equal;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 29 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 29 * hash + (int) (this.length ^ (this.length >>> 32));
        hash = 29 * hash + (int) (this.sizeof ^ (this.sizeof >>> 32));
        hash = 29 * hash + (this.isConstant ? 1 : 0);
        hash = 29 * hash + (this.parent != null ? this.parent.hashCode() : 0);
        hash = 29 * hash + (int) (this.ptr ^ (this.ptr >>> 32));
        return hash;
    }

    /**
     * Memory deallocator.
     */
    protected static class Deallocator implements Runnable
    {

        private long ptr;
        private final long length;
        private final long sizeof;

        public Deallocator(final long ptr,
                           final long length,
                           final long sizeof)
        {
            this.ptr = ptr;
            this.length = length;
            this.sizeof = sizeof;
        }

        @Override
        public void run()
        {
            if (ptr != 0) {
                LargeArrayUtils.UNSAFE.freeMemory(ptr);
                ptr = 0;
                MemoryCounter.decreaseCounter(length * sizeof);
            }
        }
    }

    /**
     * Initializes allocated native memory to zero.
     *
     * @param size the length of native memory block
     */
    protected void zeroNativeMemory(final long size)
    {
        if (ptr != 0) {
            final int nthreads = (int) FastMath.min(size, ConcurrencyUtils.getNumberOfThreads());
            if (nthreads <= 2 || size < ConcurrencyUtils.getConcurrentThreshold()) {
                LargeArrayUtils.UNSAFE.setMemory(ptr, size * sizeof, (byte) 0);
            } else {
                final long k = size / nthreads;
                final Future[] threads = new Future[nthreads];
                final long ptrf = ptr;
                for (int j = 0; j < nthreads; j++) {
                    final long firstIdx = j * k;
                    final long lastIdx = (j == nthreads - 1) ? size : firstIdx + k;
                    threads[j] = ConcurrencyUtils.submit(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            switch(type) {
                                case LOGIC:
                                case BYTE:
                                case UNSIGNED_BYTE:
                                case STRING:
                                case OBJECT:
                                    for (long k = firstIdx; k < lastIdx; k++) {
                                        LargeArrayUtils.UNSAFE.putByte(ptrf + sizeof * k, (byte) 0);
                                    }
                                    break;
                                case SHORT:
                                    for (long k = firstIdx; k < lastIdx; k++) {
                                        LargeArrayUtils.UNSAFE.putShort(ptrf + sizeof * k, (short) 0);
                                    }
                                    break;
                                case INT:
                                    for (long k = firstIdx; k < lastIdx; k++) {
                                        LargeArrayUtils.UNSAFE.putInt(ptrf + sizeof * k, 0);
                                    }
                                    break;
                                case LONG:
                                    for (long k = firstIdx; k < lastIdx; k++) {
                                        LargeArrayUtils.UNSAFE.putLong(ptrf + sizeof * k, 0);
                                    }
                                    break;
                                case FLOAT:
                                    for (long k = firstIdx; k < lastIdx; k++) {
                                        LargeArrayUtils.UNSAFE.putFloat(ptrf + sizeof * k, 0f);
                                    }
                                    break;
                                case DOUBLE:
                                    for (long k = firstIdx; k < lastIdx; k++) {
                                        LargeArrayUtils.UNSAFE.putDouble(ptrf + sizeof * k, 0.0);
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
                } catch (final InterruptedException ex) {
                    LargeArrayUtils.UNSAFE.setMemory(ptr, size * sizeof, (byte) 0);
                } catch (ExecutionException ex) {
                    LargeArrayUtils.UNSAFE.setMemory(ptr, size * sizeof, (byte) 0);
                }
            }
        }
    }
}
