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

/**
 * Invert a permutation vector.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_pinv {

	/**
	 * Inverts a permutation vector. Returns pinv[i] = k if p[k] = i on input.
	 *
	 * @param p
	 *            a permutation vector if length n
	 * @param n
	 *            length of p
	 * @return pinv, null on error
	 */
	@SuppressWarnings("unused")
	public static int[] cs_pinv(int[] p, int n)
	{
		int k, pinv[] ;
		if (p == null) return (null) ;		/* p = NULL denotes identity */
		pinv = new int [n] ;			/* allocate result */
		if (pinv == null) return (null) ;	/* out of memory */
		for (k = 0 ; k < n ; k++) pinv [p [k]] = k ;  /* invert the permutation */
		return (pinv) ; 			/* return result */
	}

}
