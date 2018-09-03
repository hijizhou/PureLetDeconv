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
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_spalloc ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_conj ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_cumsum.cs_cumsum ;

/**
 * Transpose a sparse matrix.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_transpose {

	/**
	 * Computes the transpose of a sparse matrix, C =A';
	 *
	 * @param A
	 *            column-compressed matrix
	 * @param values
	 *            pattern only if false, both pattern and values otherwise
	 * @return C=A', null on error
	 */
	public static DZcs cs_transpose(DZcs A, boolean values)
	{
		int p, q, j, Cp[], Ci[], n, m, Ap[], Ai[], w[] ;
		DZcsa Cx = new DZcsa(), Ax = new DZcsa() ;
		DZcs C ;
		if (!CS_CSC (A)) return (null) ;		/* check inputs */
		m = A.m ; n = A.n ; Ap = A.p ; Ai = A.i ; Ax.x = A.x ;
		C = cs_spalloc (n, m, Ap [n], values && (Ax.x != null), false) ;  /* allocate result */
		w = new int [m] ;				/* get workspace */
		Cp = C.p ; Ci = C.i ; Cx.x = C.x ;
		for (p = 0 ; p < Ap [n] ; p++) w [Ai [p]]++ ;	/* row counts */
		cs_cumsum (Cp, w, m) ;				/* row pointers */
		for (j = 0 ; j < n ; j++)
		{
			for (p = Ap [j] ; p < Ap [j + 1] ; p++)
			{
				Ci [q = w [Ai [p]]++] = j ;	/* place A(i,j) as entry C(j,i) */
				if (Cx.x != null)
					Cx.set(q, (values) ? cs_conj(Ax.get(p)) : Ax.get(p)) ;
			}
		}
		return (C) ;
	}

}
