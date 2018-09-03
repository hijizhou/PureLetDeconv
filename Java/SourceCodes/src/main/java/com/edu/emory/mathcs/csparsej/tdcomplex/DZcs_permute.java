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
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_spalloc ;

/**
 * Permute a sparse matrix.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_permute {

	/**
	 * Permutes a sparse matrix, C = PAQ.
	 *
	 * @param A
	 *            m-by-n, column-compressed matrix
	 * @param pinv
	 *            a permutation vector of length m
	 * @param q
	 *            a permutation vector of length n
	 * @param values
	 *            allocate pattern only if false, values and pattern otherwise
	 * @return C = PAQ, null on error
	 */
	public static DZcs cs_permute(DZcs A, int[] pinv, int[] q, boolean values)
	{
		int t, j, k, nz = 0, m, n, Ap[], Ai[], Cp[], Ci[] ;
		DZcsa Cx = new DZcsa(), Ax = new DZcsa() ;
		DZcs C ;
		if (!CS_CSC(A)) return (null);		/* check inputs */
		m = A.m ; n = A.n ; Ap = A.p ; Ai = A.i ; Ax.x = A.x ;
		C = cs_spalloc (m, n, Ap [n], values && Ax.x != null, false);  /* alloc result */
		Cp = C.p ; Ci = C.i ; Cx.x = C.x ;
		for (k = 0 ; k < n ; k++)
		{
			Cp [k] = nz ; 			/* column k of C is column q[k] of A */
			j = q != null ? (q [k]) : k ;
			for (t = Ap [j] ; t < Ap [j+1] ; t++)
			{
				if (Cx.x != null)
					Cx.set(nz, Ax.get(t)) ;  /* row i of A is row pinv[i] of C */
				Ci [nz++] = pinv != null ? (pinv [Ai [t]]) : Ai [t] ;
			}
		}
		Cp [n] = nz ;				/* finalize the last column of C */
		return C ;
	}

}
