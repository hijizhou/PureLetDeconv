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

import java.io.OutputStream;
import java.io.PrintStream;

import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcsa;
import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcs;

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_norm.cs_norm ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_creal ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_complex.cs_cimag ;

/**
 * Print a sparse matrix.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_print {

	/**
	 * Prints a sparse matrix.
	 *
	 * @param A
	 *            sparse matrix (triplet ot column-compressed)
	 * @param brief
	 *            print all of A if false, a few entries otherwise
	 * @return true if successful, false on error
	 */
	public static boolean cs_print(DZcs A, boolean brief, OutputStream output)
	{
		int p, j, m, n, nzmax, nz, Ap[], Ai[] ;
		DZcsa Ax = new DZcsa() ;
		PrintStream out = new PrintStream(output);
		if (A == null)
		{
		    out.print("(null)\n") ;
		    return (false) ;
		}
		m = A.m ; n = A.n ; Ap = A.p ; Ai = A.i ; Ax.x = A.x ;
		nzmax = A.nzmax ; nz = A.nz ;
		/*out.printf("CXSparseJ Version %d.%d.%d, %s.  %s\n",
			DZcs_common.CS_VER, DZcs_common.CS_SUBVER, DZcs_common.CS_SUBSUB,
			DZcs_common.CS_DATE, DZcs_common.CS_COPYRIGHT) ;*/
		if (nz < 0)
		{
			out.printf("%d-by-%d, nzmax: %d nnz: %d, 1-norm: %g\n", m, n,
				nzmax, Ap[n], cs_norm (A)) ;
			for (j = 0 ; j < n ; j++)
			{
				out.printf("    col %d : locations %d to %d\n",
					j, Ap [j], Ap [j+1] - 1) ;
				for (p = Ap [j] ; p < Ap [j+1] ; p++)
				{
					out.printf("      %d : (%g, %g)\n", Ai [p],
						Ax.x != null ? cs_creal (Ax.get(p)) : 1,
						Ax.x != null ? cs_cimag (Ax.get(p)) : 0) ;
					if (brief && p > 20)
					{
						out.print("  ...\n") ;
						return (true) ;
					}
				}
			}
		}
		else
		{
			out.printf("triplet: %d-by-%d, nzmax: %d nnz: %d\n",
				m, n, nzmax, nz) ;
			for (p = 0 ; p < nz ; p++)
			{
				out.printf("    %d %d : (%g, %g)\n", Ai [p], Ap [p],
					Ax.x != null ? cs_creal (Ax.get(p)) : 1,
					Ax.x != null ? cs_cimag (Ax.get(p)) : 0) ;
				if (brief && p > 20)
				{
					out.print("  ...\n") ;
					return (true) ;
				}
			}
		}
		return (true) ;
	}

	public static boolean cs_print(DZcs A, boolean brief)
	{
		return cs_print(A, brief, System.out);
	}

}
