/*
 * CXSparse: a Concise Sparse matrix package.
 * Copyright (C) 2006-2011, Timothy A. Davis.
 * Copyright (C) 2011-2012, Richard W. Lincoln.
 * http://www.cise.ufl.edu/research/sparse/CXSparse
 *
 * -------------------------------------------------------------------------
 *
 * CXSparseJ is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * CXSparseJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this Module; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 *
 */

package com.edu.emory.mathcs.csparsej.tdcomplex;

import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcs;

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.CS_TRIPLET ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_sprealloc ;

/**
 * Add an entry to a triplet matrix.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_entry {

	/**
	 * Adds an entry to a triplet matrix. Memory-space and dimension of T are
	 * increased if necessary.
	 *
	 * @param T
	 *            triplet matrix; new entry added on output
	 * @param i
	 *            row index of new entry
	 * @param j
	 *            column index of new entry
	 * @param x
	 *            numerical value of new entry
	 * @return true if successful, false otherwise
	 */
	public static boolean cs_entry(DZcs T, int i, int j, double [] x)
	{
		return cs_entry(T, i, j, x [0], x [1]);
	}

	public static boolean cs_entry(DZcs T, int i, int j, double re, double im)
	{
		if (!CS_TRIPLET (T) || i < 0 || j < 0) return (false) ;	/* check inputs */
		if ((T.nz >= T.nzmax) && !cs_sprealloc (T, 2*(T.nzmax))) return (false) ;
		if (T.x != null) T.set(T.nz, re, im) ;
		T.i [T.nz] = i ;
		T.p [T.nz++] = j ;
		T.m = Math.max(T.m, i + 1) ;
		T.n = Math.max(T.n, j + 1) ;
		return (true) ;
	}

}
