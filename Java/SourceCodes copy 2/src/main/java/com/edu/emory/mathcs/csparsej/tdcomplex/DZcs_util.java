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
import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcsa ;
import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcsd ;
import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcsn ;

/**
 * Various utilities.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_util {

	/**
	 * Allocate a sparse matrix (triplet form or compressed-column form).
	 *
	 * @param m
	 *            number of rows
	 * @param n
	 *            number of columns
	 * @param nzmax
	 *            maximum number of entries
	 * @param values
	 *            allocate pattern only if false, values and pattern otherwise
	 * @param triplet
	 *            compressed-column if false, triplet form otherwise
	 * @return sparse matrix
	 */
	public static DZcs cs_spalloc(int m, int n, int nzmax, boolean values, boolean triplet)
	{
		DZcs A = new DZcs() ;			/* allocate the DZcs struct */
		A.m = m ;				/* define dimensions and nzmax */
		A.n = n ;
		A.nzmax = nzmax = Math.max (nzmax, 1) ;
		A.nz = triplet ? 0 : -1 ;		/* allocate triplet or comp.col */
		A.p = triplet ? new int [nzmax] : new int [n+1] ;
		A.i = new int [nzmax] ;
		A.x = values ? new double [2*nzmax] : null ;
		return A ;
	}

	/**
	 * Change the max # of entries a sparse matrix can hold.
	 *
	 * @param A
	 *            column-compressed matrix
	 * @param nzmax
	 *            new maximum number of entries
	 * @return true if successful, false on error
	 */
	public static boolean cs_sprealloc(DZcs A, int nzmax)
	{
		if (A == null) return (false) ;
		if (nzmax <= 0) nzmax = (CS_CSC (A)) ? (A.p [A.n]) : A.nz ;
		int[] Ainew = new int [nzmax] ;
		int length = Math.min (nzmax, A.i.length) ;
		System.arraycopy (A.i, 0, Ainew, 0, length) ;
		A.i = Ainew ;
		if (CS_TRIPLET (A))
		{
			int [] Apnew = new int [nzmax] ;
			length = Math.min (nzmax, A.p.length) ;
			System.arraycopy (A.p, 0, Apnew, 0, length) ;
			A.p = Apnew ;
		}
		if (A.x != null)
		{
			double [] Axnew = new double [2*nzmax] ;
			length = Math.min (2*nzmax, A.x.length) ;
			System.arraycopy (A.x, 0, Axnew, 0, length) ;
			A.x = Axnew ;
		}
		A.nzmax = nzmax ;
		return (true) ;
	}

	/**
	 * Allocate a Zcsd object (a Dulmage-Mendelsohn decomposition).
	 *
	 * @param m
	 *            number of rows of the matrix A to be analyzed
	 * @param n
	 *            number of columns of the matrix A to be analyzed
	 * @return Dulmage-Mendelsohn decomposition
	 */
	public static DZcsd cs_dalloc(int m, int n)
	{
		DZcsd D ;
		D = new DZcsd() ;
		D.p = new int [m] ;
		D.r = new int [m+6] ;
		D.q = new int [n] ;
		D.s = new int [n+6] ;
		D.cc = new int [5] ;
		D.rr = new int [5] ;
		return D ;
	}

	protected static int CS_FLIP(int i)
	{
		return (-(i) - 2) ;
	}

	protected static int CS_UNFLIP(int i)
	{
		return (((i) < 0) ? CS_FLIP (i) : (i)) ;
	}

	protected static boolean CS_MARKED(int [] w, int j)
	{
		return (w [j] < 0) ;
	}

	protected static void CS_MARK(int [] w, int j)
	{
		w [j] = CS_FLIP (w [j]) ;
	}

	/**
	 * Returns true if A is in column-compressed form, false otherwise.
	 *
	 * @param A
	 *            sparse matrix
	 * @return true if A is in column-compressed form, false otherwise
	 */
	public static boolean CS_CSC(DZcs A)
	{
		return (A != null && (A.nz == -1)) ;
	}

	/**
	 * Returns true if A is in triplet form, false otherwise.
	 *
	 * @param A
	 *            sparse matrix
	 * @return true if A is in triplet form, false otherwise
	 */
	public static boolean CS_TRIPLET(DZcs A)
	{
		return (A != null && (A.nz >= 0)) ;
	}

	/* free workspace and return a sparse matrix result */
	public static DZcs cs_done (DZcs C, int[] w, DZcsa x, boolean ok)
	{
	//        cs_free (w) ;                       /* free workspace */
	//        cs_free (x) ;
		return (ok ? C : null) ;   /* return result if OK, else free it */
	}

	/* free workspace and return CS_INT array result */
	public static int[] cs_idone (int[] p, DZcs C, int[] w, boolean ok)
	{
	//        cs_spfree (C) ;                     /* free temporary matrix */
	//        cs_free (w) ;                       /* free workspace */
		return (ok ? p : null) ; /* return result, or free it */
	}

	/* free workspace and return a numeric factorization (Cholesky, LU, or QR) */
	public static DZcsn cs_ndone (DZcsn N, DZcs C, int[] w, DZcsa x, boolean ok)
	{
	//	    cs_spfree (C) ;                     /* free temporary matrix */
	//	    cs_free (w) ;                       /* free workspace */
	//	    cs_free (x) ;
		return (ok ? N : null) ;    /* return result if OK, else free it */
	}

	/* free workspace and return a csd result */
	public static DZcsd cs_ddone (DZcsd D, DZcs C, int [] w, boolean ok)
	{
	//	    cs_spfree (C) ;                     /* free temporary matrix */
	//	    cs_free (w) ;                       /* free workspace */
		return (ok ? D : null) ;    /* return result if OK, else free it */
	}

}
