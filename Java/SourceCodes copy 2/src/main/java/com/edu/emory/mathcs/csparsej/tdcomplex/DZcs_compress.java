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

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.CS_TRIPLET ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_spalloc ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_done ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_cumsum.cs_cumsum ;

/**
 * Convert a triplet form to compressed-column form.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_compress {

	/**
	 * C = compressed-column form of a triplet matrix T. The columns of C are
	 * not sorted, and duplicate entries may be present in C.
	 *
	 * @param T
	 *		triplet matrix
	 * @return C
	 * 		if successful, null on error
	 */
	public static DZcs cs_compress(DZcs T) {
		int m, n, nz, p, k, Cp[], Ci[], w[], Ti[], Tj[] ;
		DZcsa Cx = new DZcsa (), Tx = new DZcsa () ;
		DZcs C ;
		if (!CS_TRIPLET(T)) return (null) ;		/* check inputs */
		m = T.m ; n = T.n ; Ti = T.i ; Tj = T.p ; Tx.x = T.x ; nz = T.nz ;
		C = cs_spalloc(m, n, nz, Tx.x != null, false) ;	/* allocate result */
		w = new int [n] ;				/* get workspace */
		if (C == null || w == null)
			return (cs_done (C, w, null, false)) ;	/* out of memory */
		Cp = C.p ; Ci = C.i ; Cx.x = C.x ;
		for (k = 0 ; k < nz ; k++) w [Tj [k]]++ ;	/* column counts */
		cs_cumsum (Cp, w, n) ;				/* column pointers */
		for (k = 0 ; k < nz ; k++)
		{
			Ci [p = w [Tj [k]]++] = Ti [k] ;	/* A(i,j) is the pth entry in C */
			if (Cx.x != null) Cx.set(p, Tx.get(k)) ;
		}
		return (cs_done (C, w, null, true)) ;		/* success; free w and return C */
	}

}
