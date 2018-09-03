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
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_sqr.cs_sqr ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_qr.cs_qr ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_ipvec.cs_ipvec ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_happly.cs_happly ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_usolve.cs_usolve ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_transpose.cs_transpose ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_pvec.cs_pvec ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_utsolve.cs_utsolve ;

/**
 * Solve a least-squares or underdetermined problem.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_qrsol {

	/**
	 * Solve a least-squares problem (min ||Ax-b||_2, where A is m-by-n with m
	 * >= n) or underdetermined system (Ax=b, where m < n)
	 *
	 * @param order
	 *            ordering method to use (0 to 3)
	 * @param A
	 *            column-compressed matrix
	 * @param b
	 *            size max(m,n), b (size m) on input, x(size n) on output
	 * @return true if successful, false on error
	 */
	public static boolean cs_qrsol(int order, DZcs A, DZcsa b)
	{
		DZcsa x ;
		DZcss S ;
		DZcsn N ;
		DZcs AT = null ;
		int k, m, n ;
		boolean ok ;
		if (!CS_CSC (A) || b == null) return (false) ;	/* check inputs */
		n = A.n ;
		m = A.m ;
		if (m >= n)
		{
			S = cs_sqr (order, A, true) ; 		/* ordering and symbolic analysis */
			N = cs_qr (A, S) ;			/* numeric QR factorization */
			x = new DZcsa (S != null ? S.m2 : 1) ;	/* get workspace */
			ok = (S != null && N != null) ;
			if (ok)
			{
				cs_ipvec (S.pinv, b, x, m) ;	/* x(0:m-1) = b(p(0:m-1) */
				for (k = 0 ; k < n ; k++)	/* apply Householder refl. to x */
				{
					cs_happly (N.L, k, N.B [k], x) ;
				}
				cs_usolve (N.U, x) ;		/* x = R\x */
				cs_ipvec (S.q, x, b, n) ;	/* b(q(0:n-1)) = x(0:n-1) */
			}
		}
		else
		{
			AT = cs_transpose (A, true) ;		/* Ax=b is underdetermined */
			S = cs_sqr (order, AT, true) ;		/* ordering and symbolic analysis */
			N = cs_qr (AT, S) ;				/* numeric QR factorization of A' */
			x = new DZcsa(S != null ? S.m2 : 1) ;	/* get workspace */
			ok = (AT != null && S != null && N != null) ;
			if (ok)
			{
				cs_pvec (S.q, b, x, m) ;	/* x(q(0:m-1)) = b(0:m-1) */
				cs_utsolve (N.U, x) ;		/* x = R'\x */
				for (k = m-1 ; k >= 0 ; k--)	/* apply Householder refl. to x */
				{
					cs_happly (N.L, k, N.B [k], x) ;
				}
				cs_pvec (S.pinv, x, b, n) ;	/* b(0:n-1) = x(p(0:n-1)) */
			}
		}
		x = null ;
		S = null ;
		N = null ;
		AT = null ;
		return (ok);
	}

}
