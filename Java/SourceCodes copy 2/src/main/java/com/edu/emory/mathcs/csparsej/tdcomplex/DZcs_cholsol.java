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
import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcsn;
import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcss;

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.CS_CSC ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_schol.cs_schol ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_chol.cs_chol ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_ipvec.cs_ipvec ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_lsolve.cs_lsolve ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_ltsolve.cs_ltsolve ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_pvec.cs_pvec ;

/**
 * Solves Ax=b where A is symmetric positive definite.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_cholsol {

	/**
	 * Solves Ax=b where A is symmetric positive definite; b is overwritten with
	 * solution.
	 *
	 * @param order
	 *            ordering method to use (0 or 1)
	 * @param A
	 *            column-compressed matrix, symmetric positive definite, only
	 *            upper triangular part is used
	 * @param b
	 *            right hand side, b is overwritten with solution
	 * @return true if successful, false on error
	 */
	public static boolean cs_cholsol(int order, DZcs A, DZcsa b) {
		DZcsa x;
		DZcss S;
		DZcsn N;
		int n;
		boolean ok;
		if (!CS_CSC (A) || b == null) return (false);	/* check inputs */
		n = A.n ;
		S = cs_schol (order, A) ;			/* ordering and symbolic analysis */
		N = cs_chol (A, S) ;				/* numeric Cholesky factorization */
		x = new DZcsa (n) ;				/* get workspace */
		ok = (S != null && N != null && x != null) ;
		if (ok)
		{
			cs_ipvec (S.pinv, b, x, n);		/* x = P*b */
			cs_lsolve (N.L, x) ;			/* x = L\x */
			cs_ltsolve (N.L, x) ;			/* x = L'\x */
			cs_pvec (S.pinv, x, b, n) ;		/* b = P'*x */
		}
		return (ok);
	}

}
