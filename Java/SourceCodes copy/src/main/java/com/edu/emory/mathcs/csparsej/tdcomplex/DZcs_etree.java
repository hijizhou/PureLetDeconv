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

import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcs ;

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.CS_CSC ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_idone ;

/**
 * Find elimination tree.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_etree {

	/**
	 * Compute the elimination tree of A or A'A (without forming A'A).
	 *
	 * @param A
	 *            column-compressed matrix
	 * @param ata
	 *            analyze A if false, A'A oterwise
	 * @return elimination tree, null on error
	 */
	public static int[] cs_etree(DZcs A, boolean ata)
	{
		int i, k, p, m, n, inext, Ap[], Ai[], w[], parent[], ancestor[], prev[] ;
		if (!CS_CSC (A)) return (null) ;		/* check inputs */
		m = A.m ; n = A.n ; Ap = A.p ; Ai = A.i ;
		parent = new int [n] ;				/* allocate result */
		w = new int [n + (ata ? m : 0)] ;		/* get workspace */
		if (w == null || parent == null) return (cs_idone (parent, null, w, false)) ;
		ancestor = w ; prev = w ;
		int prev_offset = n ;
		if (ata) for (i = 0 ; i < m ; i++) prev [prev_offset + i] = -1 ;
		for (k = 0 ; k < n ; k++)
		{
			parent [k] = -1 ;			/* node k has no parent yet */
			ancestor [k] = -1 ;			/* nor does k have an ancestor */
			for (p = Ap [k] ; p < Ap [k + 1] ; p++)
			{
				i = ata ? (prev [prev_offset + Ai [p]]) : (Ai [p]) ;
				for ( ; i != -1 && i < k ; i = inext)  /* traverse from i to k */
				{
					inext = ancestor [i] ;	/* inext = ancestor of i */
					ancestor [i] = k ;	/* path compression */
					if (inext == -1)
						parent [i] = k ;  /* no anc., parent is k */
				}
				if (ata) prev [prev_offset + Ai [p]] = k ;
			}
		}
		return (cs_idone (parent, null, w, true)) ;
	}

}
