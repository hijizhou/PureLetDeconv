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
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_czero ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_happly.cs_happly ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_scatter.cs_scatter ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_house.cs_house ;

/**
 * Sparse QR factorization.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_qr {

	/**
	 * Sparse QR factorization of an m-by-n matrix A, A= Q*R
	 *
	 * @param A
	 *            column-compressed matrix
	 * @param S
	 *            symbolic QR analysis
	 * @return numeric QR factorization, null on error
	 */
	public static DZcsn cs_qr(DZcs A, DZcss S)
	{
		DZcsa Rx = new DZcsa(), Vx = new DZcsa(), Ax = new DZcsa(), x ;
		double Beta[] ;
		int i, k, p, n, vnz, p1, top, m2, len, col, rnz, s[], leftmost[], Ap[], Ai[],
			parent[], Rp[], Ri[], Vp[], Vi[], w[], pinv[], q[] ;
		DZcs R, V ;
		DZcsn N ;
		if (!CS_CSC (A) || S == null) return (null) ;
		n = A.n ; Ap = A.p ; Ai = A.i ; Ax.x = A.x ;
		q = S.q ; parent = S.parent ; pinv = S.pinv ; m2 = S.m2 ;
		vnz = S.lnz ; rnz = S.unz ; leftmost = S.leftmost ;
		w = new int [m2] ; 			/* get int workspace */
		x = new DZcsa (m2) ;			/* get double workspace */
		N = new DZcsn () ;			/* allocate result */
		s = new int [n] ; 			/* get int workspace, s is size n */
		//for (k = 0 ; k < m2 ; k++) x.set(k, cs_czero()) ; 	/* clear workspace x */
		N.L = V = cs_spalloc(m2, n, vnz, true, false) ;  	/* allocate result V */
		N.U = R = cs_spalloc(m2, n, rnz, true, false) ;  	/* allocate result R */
		N.B = Beta = new double [n] ;				/* allocate result Beta */
		if (R == null || V == null || Beta == null) return (cs_ndone (N, null, w, x, false)) ;
		Rp = R.p ; Ri = R.i ; Rx.x = R.x ;
		Vp = V.p ; Vi = V.i ; Vx.x = V.x ;
		for (i = 0 ; i < m2 ; i++) w [i] = -1 ;	/* clear w, to mark nodes */
		rnz = 0 ; vnz = 0 ;
		for (k = 0 ; k < n ; k++)		/* compute V and R */
		{
			Rp [k] = rnz ;			/* R(:,k) starts here */
			Vp [k] = p1 = vnz ;		/* V(:,k) starts here */
			w [k] = k ;			/* add V(k,k) to pattern of V */
			Vi [vnz++] = k ;
			top = n ;
			col = q != null ? q [k] : k ;
			for (p = Ap [col] ; p < Ap [col+1] ; p++)  /* find R(:,k) pattern */
			{
				i = leftmost [Ai [p]] ;	/* i = min(find(A(i,q))) */
				for (len = 0 ; w [i] != k ; i = parent [i])  /* traverse up to k */
				{
					s [len++] = i ;
					w [i] = k ;
				}
				while (len > 0)
					s [--top] = s [--len] ;  /* push path on stack */
				i = pinv [Ai [p]] ;	/* i = permuted row of A(:,col) */
				x.set(i, Ax.real(p), Ax.imag(p)) ;	/* x (i) = A(:,col) */
				if (i > k && w [i] < k)	/* pattern of V(:,k) = x (k+1:m) */
				{
					Vi [vnz++] = i ;  /* add i to pattern of V(:,k) */
					w [i] = k ;
				}
			}
			for (p = top ; p < n ; p++)	/* for each i in pattern of R(:,k) */
			{
				i = s [p] ;	/* R(i,k) is nonzero */
				cs_happly (V, i, Beta [i], x) ;  /* apply (V(i),Beta(i)) to x */
				Ri [rnz] = i ;		/* R(i,k) = x(i) */
				Rx.set(rnz++, x.real(i), x.imag(i)) ;
				x.set(i, 0.0, 0.0) ;
				if (parent [i] == k)
					vnz = cs_scatter (V, i, cs_czero(), w, null, k, V, vnz) ;
			}
			for (p = p1 ; p < vnz ; p++)	/* gather V(:,k) = x */
			{
				Vx.set(p, x.real(Vi [p]), x.imag(Vi [p])) ;
				x.set(Vi [p], 0.0, 0.0) ;
			}
			Ri [rnz] = k ;			/* R(k,k) = norm (x) */
			double[] beta = new double[] {Beta [k]} ;
			Rx.set(rnz++, cs_house(Vx, p1, beta, vnz - p1)) ;  /* [v,beta]=house(x) */
			Beta [k] = beta [0] ;
		}
		Rp [n] = rnz ; 				/* finalize R */
		Vp [n] = vnz ;				/* finalize V */
		return (N) ;
	}

}
