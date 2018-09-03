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
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cmult ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_czero ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cplus ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cminus ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_conj ;

/**
 * Apply Householder reflection.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_happly {

	/**
	 * Applies a Householder reflection to a dense vector, x = (I -
	 * beta*v*v')*x.
	 *
	 * @param V
	 *            column-compressed matrix of Householder vectors
	 * @param i
	 *            v = V(:,i), the ith column of V
	 * @param beta
	 *            scalar beta
	 * @param x
	 *            vector x of size m
	 * @return true if successful, false on error
	 */
	public static boolean cs_happly(DZcs V, int i, double beta, DZcsa x)
	{
		int p, Vp[], Vi[] ;
		DZcsa Vx = new DZcsa() ;
		double[] tau = cs_czero() ;
		if (!CS_CSC (V) || x == null) return (false) ;	/* check inputs */
		Vp = V.p ; Vi = V.i ; Vx.x = V.x ;
		for (p = Vp [i] ; p < Vp [i+1] ; p++)		/* tau = v'*x */
		{
		    tau = cs_cplus(tau, cs_cmult(cs_conj(Vx.get(p)), x.get(Vi [p]))) ;
		}
		tau = cs_cmult(tau, beta) ;			/* tau = beta*(v'*x) */
		for (p = Vp [i] ; p < Vp [i+1] ; p++) 		/* x = x - v*tau */
		{
			x.set(Vi [p], cs_cminus(x.get(Vi [p]), cs_cmult(Vx.get(p), tau))) ;
		}
		return (true) ;
	}

}
