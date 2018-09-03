/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package com.cern.colt.matrix.tobject.impl;

import com.cern.colt.map.tobject.AbstractLongObjectMap;
import com.cern.colt.matrix.tobject.ObjectMatrix1D;
import com.cern.colt.matrix.tobject.ObjectMatrix2D;
import com.cern.colt.matrix.tobject.ObjectMatrix3D;

/**
 * Selection view on sparse 1-d matrices holding <tt>Object</tt> elements. First
 * see the <a href="package-summary.html">package summary</a> and javadoc <a
 * href="package-tree.html">tree view</a> to get the broad picture.
 * <p>
 * <b>Implementation:</b>
 * <p>
 * Objects of this class are typically constructed via <tt>viewIndexes</tt>
 * methods on some source matrix. The interface introduced in abstract super
 * classes defines everything a user can do. From a user point of view there is
 * nothing special about this class; it presents the same functionality with the
 * same signatures and semantics as its abstract superclass(es) while
 * introducing no additional functionality. Thus, this class need not be visible
 * to users. By the way, the same principle applies to concrete DenseXXX,
 * SparseXXX classes: they presents the same functionality with the same
 * signatures and semantics as abstract superclass(es) while introducing no
 * additional functionality. Thus, they need not be visible to users, either.
 * Factory methods could hide all these concrete types.
 * <p>
 * This class uses no delegation. Its instances point directly to the data. Cell
 * addressing overhead is 1 additional array index access per get/set.
 * <p>
 * Note that this implementation is not synchronized.
 * <p>
 * <b>Memory requirements:</b>
 * <p>
 * <tt>memory [bytes] = 4*indexes.length</tt>. Thus, an index view with 1000
 * indexes additionally uses 4 KB.
 * <p>
 * <b>Time complexity:</b>
 * <p>
 * Depends on the parent view holding cells.
 * <p>
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
class SelectedSparseObjectMatrix1D extends ObjectMatrix1D {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /*
     * The elements of the matrix.
     */
    protected AbstractLongObjectMap elements;

    /**
     * The offsets of visible indexes of this matrix.
     */
    protected int[] offsets;

    /**
     * The offset.
     */
    protected int offset;

    /**
     * Constructs a matrix view with the given parameters.
     * 
     * @param size
     *            the number of cells the matrix shall have.
     * @param elements
     *            the cells.
     * @param zero
     *            the index of the first element.
     * @param stride
     *            the number of indexes between any two elements, i.e.
     *            <tt>index(i+1)-index(i)</tt>.
     * @param offsets
     *            the offsets of the cells that shall be visible.
     * @param offset
     */
    protected SelectedSparseObjectMatrix1D(int size, AbstractLongObjectMap elements, int zero, int stride,
            int[] offsets, int offset) {
        setUp(size, zero, stride);

        this.elements = elements;
        this.offsets = offsets;
        this.offset = offset;
        this.isNoView = false;
    }

    /**
     * Constructs a matrix view with the given parameters.
     * 
     * @param elements
     *            the cells.
     * @param indexes
     *            The indexes of the cells that shall be visible.
     */
    protected SelectedSparseObjectMatrix1D(AbstractLongObjectMap elements, int[] offsets) {
        this(offsets.length, elements, 0, 1, offsets, 0);
    }

    /**
     * Returns the position of the given absolute rank within the (virtual or
     * non-virtual) internal 1-dimensional array. Default implementation.
     * Override, if necessary.
     * 
     * @param rank
     *            the absolute rank of the element.
     * @return the position.
     */

    protected int _offset(int absRank) {
        return offsets[absRank];
    }

    public Object elements() {
        throw new IllegalArgumentException("This method is not supported.");
    }

    /**
     * Returns the matrix cell value at coordinate <tt>index</tt>.
     * 
     * <p>
     * Provided with invalid parameters this method may return invalid objects
     * without throwing any exception. <b>You should only use this method when
     * you are absolutely sure that the coordinate is within bounds.</b>
     * Precondition (unchecked): <tt>index&lt;0 || index&gt;=size()</tt>.
     * 
     * @param index
     *            the index of the cell.
     * @return the value of the specified cell.
     */

    public Object getQuick(int index) {
        // if (debug) if (index<0 || index>=size) checkIndex(index);
        // return elements.get(index(index));
        // manually inlined:
        return elements.get((long) offset + (long) offsets[zero + index * stride]);
    }

    /**
     * Returns <tt>true</tt> if both matrices share at least one identical cell.
     */

    protected boolean haveSharedCellsRaw(ObjectMatrix1D other) {
        if (other instanceof SelectedSparseObjectMatrix1D) {
            SelectedSparseObjectMatrix1D otherMatrix = (SelectedSparseObjectMatrix1D) other;
            return this.elements == otherMatrix.elements;
        } else if (other instanceof SparseObjectMatrix1D) {
            SparseObjectMatrix1D otherMatrix = (SparseObjectMatrix1D) other;
            return this.elements == otherMatrix.elements;
        }
        return false;
    }

    /**
     * Returns the position of the element with the given relative rank within
     * the (virtual or non-virtual) internal 1-dimensional array. You may want
     * to override this method for performance.
     * 
     * @param rank
     *            the rank of the element.
     */

    public long index(int rank) {
        // return this.offset + super.index(rank);
        // manually inlined:
        return (long) offset + (long) offsets[zero + rank * stride];
    }

    /**
     * Construct and returns a new empty matrix <i>of the same dynamic type</i>
     * as the receiver, having the specified size. For example, if the receiver
     * is an instance of type <tt>DenseObjectMatrix1D</tt> the new matrix must
     * also be of type <tt>DenseObjectMatrix1D</tt>, if the receiver is an
     * instance of type <tt>SparseObjectMatrix1D</tt> the new matrix must also
     * be of type <tt>SparseObjectMatrix1D</tt>, etc. In general, the new matrix
     * should have internal parametrization as similar as possible.
     * 
     * @param size
     *            the number of cell the matrix shall have.
     * @return a new empty matrix of the same dynamic type.
     */

    public ObjectMatrix1D like(int size) {
        return new SparseObjectMatrix1D(size);
    }

    /**
     * Construct and returns a new 2-d matrix <i>of the corresponding dynamic
     * type</i>, entirelly independent of the receiver. For example, if the
     * receiver is an instance of type <tt>DenseObjectMatrix1D</tt> the new
     * matrix must be of type <tt>DenseObjectMatrix2D</tt>, if the receiver is
     * an instance of type <tt>SparseObjectMatrix1D</tt> the new matrix must be
     * of type <tt>SparseObjectMatrix2D</tt>, etc.
     * 
     * @param rows
     *            the number of rows the matrix shall have.
     * @param columns
     *            the number of columns the matrix shall have.
     * @return a new matrix of the corresponding dynamic type.
     */

    public ObjectMatrix2D like2D(int rows, int columns) {
        return new SparseObjectMatrix2D(rows, columns);
    }
    
    public ObjectMatrix2D reshape(int rows, int columns) {
        throw new IllegalArgumentException("This method is not supported.");
    }

    public ObjectMatrix3D reshape(int slices, int rows, int columns) {
        throw new IllegalArgumentException("This method is not supported.");
    }


    /**
     * Sets the matrix cell at coordinate <tt>index</tt> to the specified value.
     * 
     * <p>
     * Provided with invalid parameters this method may access illegal indexes
     * without throwing any exception. <b>You should only use this method when
     * you are absolutely sure that the coordinate is within bounds.</b>
     * Precondition (unchecked): <tt>index&lt;0 || index&gt;=size()</tt>.
     * 
     * @param index
     *            the index of the cell.
     * @param value
     *            the value to be filled into the specified cell.
     */

    public void setQuick(int index, Object value) {
        // if (debug) if (index<0 || index>=size) checkIndex(index);
        // int i = index(index);
        // manually inlined:
        long i = (long) offset + (long) offsets[zero + index * stride];
        if (value == null)
            this.elements.removeKey(i);
        else
            this.elements.put(i, value);
    }

    /**
     * Sets up a matrix with a given number of cells.
     * 
     * @param size
     *            the number of cells the matrix shall have.
     */

    protected void setUp(int size) {
        super.setUp(size);
        this.stride = 1;
        this.offset = 0;
    }

    /**
     * Construct and returns a new selection view.
     * 
     * @param offsets
     *            the offsets of the visible elements.
     * @return a new view.
     */

    protected ObjectMatrix1D viewSelectionLike(int[] offsets) {
        return new SelectedSparseObjectMatrix1D(this.elements, offsets);
    }
}