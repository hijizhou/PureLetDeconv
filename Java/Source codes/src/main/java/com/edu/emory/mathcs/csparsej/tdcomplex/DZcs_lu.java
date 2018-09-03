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
import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcsn ;
import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcss ;

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.CS_CSC ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_spalloc ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_sprealloc ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_ndone ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_czero ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cone ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cabs ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cdiv ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_spsolve.cs_spsolve ;

/**
 * Sparse LU factorization.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_lu {

	/**
	 * Sparse LU factorization of a square matrix, PAQ = LU.
	 *
	 * @param A
	 *            column-compressed matrix
	 * @param S
	 *            symbolic LU analysis
	 * @param tol
	 *            partial pivoting threshold (1 for partial pivoting)
	 * @return numeric LU factorization, null on error
	 */
	public static DZcsn cs_lu(DZcs A, DZcss S, double tol)
	{
		DZcs L, U ;
		DZcsn N ;
		double[] pivot ;
		DZcsa Lx = new DZcsa(), Ux = new DZcsa(), x ;
		double a, t ;
		int Lp[], Li[], Up[], Ui[], pinv[], xi[], q[], n, ipiv, k, top, p, i, col, lnz, unz ;
		if (!CS_CSC(A) || S == null) return (null) ;	/* check inputs */
		n = A.n ;
		q = S.q ; lnz = S.lnz ; unz = S.unz ;
		x = new DZcsa(n) ;		/* get double workspace */
		xi = new int [2 * n] ;		/* get int workspace */
		N = new DZcsn() ;		/* allocate result */
		N.L = L = cs_spalloc (n, n, lnz, true, false) ;		/* allocate result L */
		N.U = U = cs_spalloc (n, n, unz, true, false) ;		/* allocate result U */
		N.pinv = pinv = new int [n] ;				/* allocate result pinv */
		Lp = L.p ; Up = U.p ;
		for (i = 0 ; i < n ; i++) x.set(i, cs_czero()) ;	/* clear workspace */
		for (i = 0 ; i < n ; i++) pinv [i] = -1 ;		/* no rows pivotal yet */
		for (k = 0 ; k <= n ; k++) Lp [k] = 0 ;			/* no cols of L yet */
		lnz = unz = 0 ;
		for (k = 0 ; k < n ; k++)				/* compute L(:,k) and U(:,k) */
		{
			/* --- Triangular solve --------------------------------------------- */
			Lp [k] = lnz ;					/* L(:,k) starts here */
			Up [k] = unz ;					/* U(:,k) starts here */
			if (lnz + n > L.nzmax)
			{
				cs_sprealloc(L, 2 * L.nzmax + n) ;
			}
			if (unz + n > U.nzmax)
			{
				cs_sprealloc(U, 2 * U.nzmax + n) ;
			}
			Li = L.i ; Lx.x = L.x ; Ui = U.i ; Ux.x = U.x ;
			col = q != null ? (q [k]) : k ;
			top = cs_spsolve(L, A, col, xi, x, pinv, true) ;  /* x = L\A(:,col) */
			/* --- Find pivot --------------------------------------------------- */
			ipiv = -1 ;
			a = -1 ;
			for (p = top ; p < n ; p++)
			{
				i = xi [p] ;			/* x(i) is nonzero */
				if (pinv [i] < 0) /* row i is not yet pivotal */
				{
					if ((t = cs_cabs(x.get(i))) > a)
					{
						a = t;		/* largest pivot candidate so far */
						ipiv = i ;
					}
				}
				else 				/* x(i) is the entry U(pinv[i],k) */
				{
					Ui [unz] = pinv [i] ;
					Ux.set(unz++, x.get(i)) ;
				}
			}
			if (ipiv == -1 || a <= 0) return (cs_ndone (N, null, xi, x, false)) ;
			if (pinv [col] < 0 && cs_cabs(x.get(col)) >= a * tol) ipiv = col ;
			/* --- Divide by pivot ---------------------------------------------- */
			pivot = x.get(ipiv) ;			/* the chosen pivot */
			Ui [unz] = k ;				/* last entry in U(:,k) is U(k,k) */
			Ux.set(unz++, pivot) ;
			pinv [ipiv] = k ;			/* ipiv is the kth pivot row */
			Li [lnz] = ipiv ;			/* first entry in L(:,k) is L(k,k) = 1 */
			Lx.set(lnz++, cs_cone()) ;
			for (p = top ; p < n ; p++)		/* L(k+1:n,k) = x / pivot */
			{
				i = xi [p] ;
				if (pinv [i] < 0)		/* x(i) is an entry in L(:,k) */
				{
					Li [lnz] = i ;		/* save unpermuted row in L */
					Lx.set(lnz++, cs_cdiv(x.get(i), pivot)) ;  /* scale pivot column */
				}
				x.set(i, cs_czero()) ;		/* x [0..n-1] = 0 for next k */
			}
		}
		/* --- Finalize L and U ------------------------------------------------- */
		Lp [n] = lnz ;
		Up [n] = unz ;
		Li = L.i ;					/* fix row indices of L for final pinv */
		for (p = 0 ; p < lnz ; p++) Li [p] = pinv [Li [p]] ;
		cs_sprealloc (L, 0) ;				/* remove extra space from L and U */
		cs_sprealloc (U, 0) ;
		return (cs_ndone (N, null, xi, x, true)) ;	/* success */
	}

}
