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
import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_ifkeep;

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cone ;

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.CS_CSC ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_sprealloc ;

/**
 * Drop entries from a sparse matrix.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_fkeep {

	/**
	 * Drops entries from a sparse matrix;
	 *
	 * @param A
	 *            column-compressed matrix
	 * @param fkeep
	 *            drop aij if fkeep.fkeep(i,j,aij,other) is false
	 * @param other
	 *            optional parameter to fkeep
	 * @return nz, new number of entries in A, -1 on error
	 */
	public static int cs_fkeep(DZcs A, DZcs_ifkeep fkeep, Object other)
	{
		int j, p, nz = 0, n, Ap[], Ai[] ;
		DZcsa Ax = new DZcsa() ;
		if (!CS_CSC (A)) return (-1) ;		/* check inputs */
		n = A.n ; Ap = A.p ; Ai = A.i ; Ax.x = A.x ;
		for (j = 0 ; j < n ; j++)
		{
			p = Ap [j] ;			/* get current location of col j */
			Ap [j] = nz ;			/* record new location of col j */
			for ( ; p < Ap [j+1] ; p++)
			{
				if (fkeep.fkeep(Ai [p], j, Ax.x != null ? Ax.get(p) : cs_cone(), other))
				{
					if (Ax.x != null) Ax.set(nz, Ax.get(p));  /* keep A(i,j) */
					Ai [nz++] = Ai [p] ;
				}
			}
		}
		Ap [n] = nz ;				/* finalize A */
		cs_sprealloc (A, 0) ;			/* remove extra space from A */
		return (nz) ;
	}

}
