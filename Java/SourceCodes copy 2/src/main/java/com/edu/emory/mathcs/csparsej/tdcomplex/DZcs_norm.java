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
import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcs;

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.CS_CSC ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cabs ;

/**
 * Sparse matrix 1-norm.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_norm {

	/**
	 * Computes the 1-norm of a sparse matrix = max (sum (abs (A))), largest
	 * column sum.
	 *
	 * @param A
	 *            column-compressed matrix
	 * @return the 1-norm if successful, -1 on error
	 */
	public static double cs_norm(DZcs A)
	{
		int p, j, n, Ap[] ;
		DZcsa Ax = new DZcsa() ;
		double norm = 0, s ;
		if (!CS_CSC (A) || A.x == null) return (-1) ;	/* check inputs */
		n = A.n ; Ap = A.p ; Ax.x = A.x ;
		for (j = 0 ; j < n ; j++)
		{
			for (s = 0, p = Ap [j] ; p < Ap [j+1] ; p++) s += cs_cabs(Ax.get(p)) ;
			norm = Math.max(norm, s) ;
		}
		return (norm) ;
	}

}
