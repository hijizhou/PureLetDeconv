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
import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcsd ;

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.CS_CSC ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_dalloc ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_ddone ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.CS_MARKED ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.CS_MARK ;

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_transpose.cs_transpose ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_dfs.cs_dfs ;

/**
 * Strongly-connected components.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_scc {

	/**
	 * Finds the strongly connected components of a square matrix.
	 *
	 * @param A
	 *            column-compressed matrix (A.p modified then restored)
	 * @return strongly connected components, null on error
	 */
	public static DZcsd cs_scc(DZcs A)
	{
		int n, i, k, b, nb = 0, top, xi[], pstack[], p[], r[], Ap[], ATp[], rcopy[], Blk[] ;
		DZcs AT ;
		DZcsd D ;
		if (!CS_CSC (A)) return (null) ; 		/* check inputs */
		n = A.n ; Ap = A.p ;
		D = cs_dalloc (n, 0) ;				/* allocate result */
		AT = cs_transpose (A, false) ;			/* AT = A' */
		xi = new int [2*n+1] ;				/* get workspace */
		if (D == null || AT == null || xi == null)
			return (cs_ddone (D, AT, xi, false)) ;
		Blk = xi ; rcopy = xi ;
		int rcopy_offset = n ;
		pstack = xi ;
		int pstack_offset = n ;
		p = D.p ; r = D.r ; ATp = AT.p ;
		top = n ;
		for (i = 0 ; i < n ; i++)			/* first dfs(A) to find finish times (xi) */
		{
			if (!CS_MARKED (Ap, i))
			top = cs_dfs (i, A, top, xi, 0, pstack, pstack_offset, null, 0) ;
		}
		for (i = 0 ; i < n ; i++) CS_MARK (Ap, i) ;	/* restore A; unmark all nodes*/
		top = n ; nb = n ;
		for (k = 0 ; k < n ; k++)			/* dfs(A') to find strongly connnected comp */
		{
			i = xi [k] ;				/* get i in reverse order of finish times */
			if (CS_MARKED (ATp, i)) continue ;	/* skip node i if already ordered */
			r [nb--] = top ;			/* node i is the start of a component in p */
			top = cs_dfs (i, AT, top, p, 0, pstack, pstack_offset, null, 0) ;
		}
		r [nb] = 0 ;					/* first block starts at zero; shift r up */
		for (k = nb ; k <= n ; k++) r [k-nb] = r [k] ;
		D.nb = nb = n - nb ;				/* nb = # of strongly connected components */
		for (b = 0 ; b < nb ; b++)			/* sort each block in natural order */
		{
			for (k = r [b] ; k < r [b+1] ; k++) Blk [p [k]] = b ;
		}
		for (b = 0 ; b <= nb ; b++) rcopy [rcopy_offset + b] = r [b] ;
		for (i = 0 ; i < n ; i++) p [rcopy [rcopy_offset + Blk [i]]++] = i ;
		return (D) ;
	}

}
