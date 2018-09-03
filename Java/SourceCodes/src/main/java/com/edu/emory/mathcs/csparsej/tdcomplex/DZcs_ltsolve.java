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
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cminus ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cdiv ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cmult ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_conj ;

/**
 * Solve an upper triangular system L'x=b.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_ltsolve {

	/**
	 * Solves an upper triangular system L'x=b where x and b are dense. x=b on
	 * input, solution on output.
	 *
	 * @param L
	 *            column-compressed, lower triangular matrix
	 * @param x
	 *            size n, right hand side on input, solution on output
	 * @return true if successful, false on error
	 */
	public static boolean cs_ltsolve(DZcs L, DZcsa x)
	{
		int p, j, n, Lp[], Li[] ;
		DZcsa Lx = new DZcsa() ;
		if (!CS_CSC (L) || x == null) return (false) ;	/* check inputs */
		n = L.n ; Lp = L.p ; Li = L.i ; Lx.x = L.x ;
		for (j = n-1 ; j >= 0 ; j--)
		{
			for (p = Lp [j] + 1 ; p < Lp [j+1] ; p++)
			{
				x.set(j, cs_cminus(x.get(j), cs_cmult(cs_conj(Lx.get(p)), x.get(Li [p])) )) ;
			}
			x.set(j, cs_cdiv(x.get(j), cs_conj( Lx.get(Lp [j]) ))) ;
		}
		return (true) ;
	}

}
