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

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cplus ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.CS_CSC ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_sprealloc ;

/**
 * Remove (and sum) duplicates.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_dupl {

	/**
	 * Removes and sums duplicate entries in a sparse matrix.
	 *
	 * @param A
	 *            column-compressed matrix
	 * @return true if successful, false on error
	 */
	public static boolean cs_dupl(DZcs A)
	{
		int i, j, p, q, nz = 0, n, m, Ap[], Ai[], w[] ;
		DZcsa Ax = new DZcsa() ;
		if (!CS_CSC (A)) return (false) ;		/* check inputs */
		m = A.m ; n = A.n ; Ap = A.p ; Ai = A.i ; Ax.x = A.x ;
		w = new int [m] ;				/* get workspace */
//		if (w == null) return (false) ;			/* out of memory */
		for (i = 0 ; i < m ; i++) w [i] = -1 ;		/* row i not yet seen */
		for (j = 0 ; j < n ; j++)
		{
			q = nz ;				/* column j will start at q */
			for (p = Ap [j] ; p < Ap [j+1] ; p++)
			{
				i = Ai [p] ;			/* A(i,j) is nonzero */
				if (w [i] >= q)
				{
					Ax.set(w [i], cs_cplus(Ax.get(w [i]), Ax.get(p))); /* A(i,j) is a duplicate */
				}
				else
				{
					w [i] = nz ;		/* record where row i occurs */
					Ai [nz] = i ;		/* keep A(i,j) */
					Ax.set(nz++, Ax.get(p)) ;
				}
			}
			Ap [j] = q ;				/* record start of column j */
		}
		Ap [n] = nz ;					/* finalize A */
		w = null ;
		return (cs_sprealloc (A, 0)) ;			/* remove extra space from A */
	}

}
