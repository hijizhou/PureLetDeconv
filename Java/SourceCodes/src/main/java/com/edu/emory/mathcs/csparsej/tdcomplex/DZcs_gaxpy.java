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

import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcsa ;
import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcs ;

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.CS_CSC ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cplus ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cmult ;

/**
 * Sparse matrix times dense vector.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_gaxpy {

	/**
	 * Sparse matrix times dense column vector, y = A*x+y.
	 *
	 * @param A
	 *            column-compressed matrix
	 * @param x
	 *            size n, vector x
	 * @param y
	 *            size m, vector y
	 * @return true if successful, false on error
	 */
	public static boolean cs_gaxpy (DZcs A, DZcsa x, DZcsa y)
	{
		int p, j, n, Ap[], Ai[] ;
		DZcsa Ax = new DZcsa() ;
		if (!CS_CSC (A) || x == null || y == null) return (false) ;	/* check inputs */
		n = A.n ; Ap = A.p ; Ai = A.i ; Ax.x = A.x ;
		for (j = 0 ; j < n ; j++)
		{
			for (p = Ap [j] ; p < Ap [j+1] ; p++)
			{
				y.set(Ai [p], cs_cplus(y.get(Ai [p]), cs_cmult(Ax.get(p), x.get(j)))) ;
			}
		}
		return (true) ;
	}

}
