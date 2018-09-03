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
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_spalloc ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_ndone ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_symperm.cs_symperm ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_ereach.cs_ereach ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_czero ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cdiv ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cminus ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cmult ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_conj ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_csqrt ;

/**
 * Sparse Cholesky.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_chol {
	/**
	 * Numeric Cholesky factorization LL=PAP'.
	 *
	 * @param A
	 *            column-compressed matrix, only upper triangular part is used
	 * @param S
	 *            symbolic Cholesky analysis, pinv is optional
	 * @return numeric Cholesky factorization, null on error
	 */
	public static DZcsn cs_chol(DZcs A, DZcss S) {
		double[] d, lki ;
		DZcsa Lx = new DZcsa (), x, Cx = new DZcsa () ;
		int top, i, p, k, n, Li[], Lp[], cp[], pinv[], s[], c[], parent[], Cp[], Ci[] ;
		DZcs L, C, E ;
		DZcsn N ;
		if (!CS_CSC(A) || S == null || S.cp == null || S.parent == null) return (null) ;
		n = A.n ;
		N = new DZcsn () ;		/* allocate result */
		c = new int [2*n] ;		/* get int workspace */
		x = new DZcsa (n) ;		/* get complex workspace */
		cp = S.cp ; pinv = S.pinv ; parent = S.parent ;
		C = pinv != null ? cs_symperm (A, pinv, true) : A ;
		E = pinv != null ? C : null ;	/* E is alias for A, or a copy E=A(p,p) */
		if (N == null || c == null || x == null || C == null) return (cs_ndone (N, E, c, x, false)) ;
		s = c ;
		int s_offset = n ;
		Cp = C.p ; Ci = C.i ; Cx.x = C.x ;
		N.L = L = cs_spalloc (n, n, cp[n], true, false); /* allocate result */
		if (L == null) return (cs_ndone (N, E, c, x, false)) ;
		Lp = L.p ; Li = L.i ; Lx.x = L.x ;
		for (k = 0 ; k < n ; k++) Lp [k] = c [k] = cp [k] ;
		for (k = 0 ; k < n ; k++)	/* compute L(k,:) for L*L' = C */
		{
			/* --- Nonzero pattern of L(k,:) ------------------------------------ */
			top = cs_ereach (C, k, parent, s, s_offset, c) ;		/* find pattern of L(k,:) */
			x.set(k, cs_czero ()) ;					/* x (0:k) is now zero */
			for (p = Cp [k] ; p < Cp [k + 1] ; p++)			/* x = full(triu(C(:,k))) */
			{
				if (Ci [p] <= k) x.set(Ci [p], Cx.get(p)) ;
			}
			d = x.get(k) ;				/* d = C(k,k) */
			x.set(k, cs_czero ()) ;			/* clear x for k+1st iteration */
			/* --- Triangular solve --------------------------------------------- */
			for ( ; top < n ; top++)		/* solve L(0:k-1,0:k-1) * x = C(:,k) */
			{
				i = s [s_offset + top] ;	/* s [top..n-1] is pattern of L(k,:) */
				lki = cs_cdiv (x.get(i), Lx.get(Lp [i])) ; /* L(k,i) = x (i) / L(i,i) */
				x.set(i, cs_czero ()) ;		/* clear x for k+1st iteration */
				for (p = Lp [i] + 1 ; p < c [i] ; p++)
				{
					x.set(Li [p], cs_cminus (x.get(Li [p]), cs_cmult (Lx.get(p), lki))) ;
				}
				d = cs_cminus (d, cs_cmult (lki, cs_conj (lki))) ;	/* d = d - L(k,i)*L(k,i) */
				p = c [i]++ ;
				Li [p] = k ;			/* store L(k,i) in column i */
				Lx.set(p, cs_conj (lki)) ;
			}
			/* --- Compute L(k,k) ----------------------------------------------- */
			if (d[0] <= 0 || d[1] != 0)
				return (cs_ndone (N, E, c, x, false)) ;	/* not pos def */
			p = c [k]++ ;
			Li [p] = k ;				/* store L(k,k) = sqrt (d) in column k */
			Lx.set(p, cs_csqrt (d)) ;
		}
		Lp [n] = cp [n] ;				/* finalize L */
		return (cs_ndone (N, E, c, x, true)) ;		/* success: free E,s,x; return N */
	}
}
