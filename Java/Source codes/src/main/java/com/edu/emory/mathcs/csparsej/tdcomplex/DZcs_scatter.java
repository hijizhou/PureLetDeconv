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
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cmult ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cplus ;

/**
 * Scatter a sparse vector.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_scatter {

	/**
	 * Scatters and sums a sparse vector A(:,j) into a dense vector,
	 * x = x + beta * A(:,j).
	 *
	 * @param A
	 *            the sparse vector is A(:,j)
	 * @param j
	 *            the column of A to use
	 * @param beta
	 *            scalar multiplied by A(:,j)
	 * @param w
	 *            size m, node i is marked if w[i] = mark
	 * @param x
	 *            size m, ignored if null
	 * @param mark
	 *            mark value of w
	 * @param C
	 *            pattern of x accumulated in C.i
	 * @param nz
	 *            pattern of x placed in C starting at C.i[nz]
	 * @return new value of nz, -1 on error
	 */
	public static int cs_scatter(DZcs A, int j, double[] beta, int[] w, DZcsa x, int mark, DZcs C, int nz)
	{
		int i, p, Ap[], Ai[], Ci[] ;
		DZcsa Ax = new DZcsa() ;
		if (!CS_CSC(A) || (w == null) || !CS_CSC(C)) return (-1) ;	/* check inputs */
		Ap = A.p ; Ai = A.i ; Ax.x = A.x ; Ci = C.i ;
		for (p = Ap [j]; p < Ap [j+1] ; p++)
		{
			i = Ai [p] ;		/* A(i,j) is nonzero */
			if (w [i] < mark)
			{
				w [i] = mark ;	/* i is new entry in column j */
				Ci [nz++] = i ;	/* add i to pattern of C(:,j) */
				if (x != null)
					x.set(i, cs_cmult(beta, Ax.get(p))) ;  /* x(i) = beta*A(i,j) */
			}
			else if (x != null)
			{
				x.set(i, cs_cplus(x.get(i), cs_cmult(beta, Ax.get(p))));  /* i exists in C(:,j) already */
			}
		}
		return (nz) ;
	}
}
