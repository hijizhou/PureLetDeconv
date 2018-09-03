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
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_czero ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cdiv ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_creal ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cmult ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cplus ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cminus ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cabs ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_conj ;

/**
 * Sparse rank-1 Cholesky update/downate.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_updown {

	/**
	 * Sparse Cholesky rank-1 update/downdate, L*L' + sigma*w*w' (sigma = +1 or -1).
	 * Note that this CXSparseJ version is different than CXSparseJ.
	 *
	 * @param L
	 *            factorization to update/downdate
	 * @param sigma
	 *            +1 for update, -1 for downdate
	 * @param C
	 *            the vector c
	 * @param parent
	 *            the elimination tree of L
	 * @return true if successful, false on error
	 */
	@SuppressWarnings("unused")
	public static boolean cs_updown(DZcs L, int sigma, DZcs C, int[] parent)
	{
		int n, p, f, j, Lp[], Li[], Cp[], Ci[] ;
		DZcsa Lx = new DZcsa(), Cx = new DZcsa(), w ;
		double[] alpha, gamma, w1, w2 ;
		double phase, beta = 1, delta, beta2 = 1 ;
		if (!CS_CSC(L) || !CS_CSC(C) || parent == null)
			return (false) ;				/* check inputs */
		Lp = L.p ; Li = L.i ; Lx.x = L.x ; n = L.n ;
		Cp = C.p ; Ci = C.i ; Cx.x = C.x ;
		if ((p = Cp[0]) >= Cp[1]) return (true) ;		/* return if C empty */
		w = new DZcsa(n) ;					/* get workspace */
		if (w == null) return (false) ;				/* out of memory */
		f = Ci [p] ;
		for ( ; p < Cp [1]; p++) f = Math.min(f, Ci [p]) ;	/* f = min (find (C)) */
		for (j = f ; j != -1 ; j = parent [j])
			w.set(j, cs_czero()) ;				/* clear workspace w */
		for (p = Cp [0]; p < Cp [1]; p++)
			w.set(Ci [p], Cx.get(p)) ;			/* w = C */
		for (j = f ; j != -1 ; j = parent [j])			/* walk path f up to root */
		{
			p = Lp [j] ;
			alpha = cs_cdiv (w.get(j), Lx.get(p)) ;		/* alpha = w(j) / L(j,j) */
			/* CXSparseJ */
			beta2 = beta*beta + sigma * cs_creal(cs_cmult(alpha, cs_conj(alpha))) ;
			if (beta2 <= 0) break ;				/* not positive definite */
			beta2 = Math.sqrt(beta2) ;
			delta = (sigma > 0) ? (beta / beta2) : (beta2 / beta) ;
			gamma = cs_cmult(cs_cdiv(cs_conj(alpha), beta2*beta, 0.0), sigma) ;
			Lx.set(p, cs_cplus(cs_cmult(Lx.get(p), delta), (sigma>0) ? cs_cmult(gamma, w.get(j)) : cs_czero()));
			beta = beta2 ;
			/* CXSparseJ */
			phase = cs_cabs(cs_cdiv(Lx.get(p), Lx.get(p))) ;  /* phase = abs(L(j,j))/L(j,j) */
			Lx.set(p, cs_cmult(Lx.get(p), phase)) ;		/* L(j,j) = L(j,j) * phase */
			for (p++; p < Lp [j + 1]; p++)
			{
				w1 = w.get(Li [p]) ;
				w.set(Li [p], cs_cminus(w1, cs_cmult(alpha, Lx.get(p)))) ;
				w2 = w.get(Li [p]) ;
				Lx.set(p, cs_cplus(cs_cmult(Lx.get(p), delta), cs_cmult(gamma, (sigma>0) ? w1 : w2))) ;
				/* CXSparseJ */
				Lx.set(p, cs_cmult(Lx.get(p), phase)) ;	/* L(i,j) = L(i,j) * phase */
			}
		}
		w = null ;
		return (beta2 > 0) ;
	}

}
