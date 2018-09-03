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

import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcsa;

/**
 * Permutes a vector, x=P*b.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_pvec {

	/**
	 * Permutes a vector, x=P*b, for dense vectors x and b.
	 *
	 * @param p
	 *            permutation vector, p=null denotes identity
	 * @param b
	 *            input vector
	 * @param x
	 *            output vector, x=P*b
	 * @param n
	 *            length of p, b and x
	 * @return true if successful, false otherwise
	 */
	public static boolean cs_pvec(int [] p, DZcsa b, DZcsa x, int n)
	{
		int k ;
		if (x == null || b == null) return (false) ;	/* check inputs */
		for (k = 0 ; k < n ; k++) x.set(k, b.get(p != null ? p [k] : k)) ;
		return (true) ;
	}

}
