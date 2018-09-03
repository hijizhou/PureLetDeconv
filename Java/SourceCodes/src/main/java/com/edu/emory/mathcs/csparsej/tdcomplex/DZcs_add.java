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
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_sprealloc ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_scatter.cs_scatter ;

/**
 * Add sparse matrices.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_add {

	/**
	 * C = alpha*A + beta*B
	 *
	 * @param A
	 *            column-compressed matrix
	 * @param B
	 *            column-compressed matrix
	 * @param alpha
	 *            scalar alpha
	 * @param beta
	 *            scalar beta
	 * @return C=alpha*A + beta*B, null on error
	 */
	public static DZcs cs_add(DZcs A, DZcs B, double[] alpha, double[] beta)
	{
		int p, j, nz = 0, anz ;
		int Cp[], Ci[], Bp[], m, n, bnz, w[] ;
		DZcsa x, Bx = new DZcsa(), Cx = new DZcsa() ;
		boolean values ;
		DZcs C ;
		if (!CS_CSC(A) || !CS_CSC(B)) return null ;		/* check inputs */
		if (A.m != B.m || A.n != B.n) return null ;
		m = A.m ; anz = A.p[A.n] ;
		n = B.n ; Bp = B.p ; Bx.x = B.x ; bnz = Bp[n] ;
		w = new int [m] ;					/* get workspace */
		values = (A.x != null) && (Bx.x != null) ;
		x = values ? new DZcsa (m) : null ;			/* get workspace */
		C = cs_spalloc (m, n, anz + bnz, values, false) ;	/* allocate result*/
		Cp = C.p ; Ci = C.i ; Cx.x = C.x ;
		for (j = 0 ; j < n ; j++)
		{
		    Cp[j] = nz ;	/* column j of C starts here */
		    nz = cs_scatter (A, j, alpha, w, x, j + 1, C, nz) ;		/* alpha*A(:,j)*/
		    nz = cs_scatter (B, j, beta, w, x, j + 1, C, nz) ;		/* beta*B(:,j) */
		    if (values) for (p = Cp[j] ; p < nz ; p++) Cx.set (p, x.get (Ci [p])) ;
		}
		Cp[n] = nz ;			/* finalize the last column of C */
		cs_sprealloc (C, 0) ;		/* remove extra space from C */
		return C ;			/* success; free workspace, return C */
	}

}
