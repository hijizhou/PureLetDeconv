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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_print.cs_print;

/**
 * Common data structures.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_common {

	public static final int CS_VER = 2; /* CXSparseJ Version 2.2.6 */
	public static final int CS_SUBVER = 2;
	public static final int CS_SUBSUB = 6;
	public static final String CS_DATE = "Dec 15, 2011"; /* CXSparseJ release date */
	public static final String CS_COPYRIGHT = "Copyright (C) Timothy A. Davis, 2006-2011";

	/**
	 *
	 * Complex array.
	 *
	 */
	public static class DZcsa
	{

		/**
		 * numerical values
		 */
		public double[] x;

		public DZcsa()
		{

		}

		public DZcsa(double [] x)
		{
			this.x = x ;
		}

		/**
		 * Constructs an array of the given length.
		 */
		public DZcsa(int len)
		{
			this.x = new double [2*len] ;
		}

		/**
		 *
		 * @param idx
		 * @return
		 */
		public double[] get(final int idx)
		{
			int offset = 2 * idx ;
			return new double [] {x [offset], x [offset + 1]} ;
		}

		public double real(final int idx) {
			return x [2 * idx] ;
		}

		public double imag(final int idx) {
			return x [(2 * idx) + 1] ;
		}

		/**
		 *
		 * @param idx
		 * @param val
		 */
		public void set(final int idx, final double [] val)
		{
			int offset = 2 * idx ;

			x [offset] = val [0] ;
			x [offset + 1] = val [1] ;
		}

		public void set(final int idx, final double re, final double im) {
			int offset = 2 * idx ;

			x [offset] = re ;
			x [offset + 1] = im ;
		}

		@Override
		public String toString() {
			String s = "DZcsa [" ;
			for (int i = 0; i < x.length; i+=2) {
				if (i != 0) s += ", " ;
				s += String.format("%g+j%g", x[i], x[i + 1]) ;
			}
			return s + "]" ;
		}
	}

	/**
	 *
	 * Complex matrix in compressed-column or triplet form.
	 *
	 */
	public static class DZcs
	{

		/**
		 * show a few entries in string representation
		 */
		public static boolean BRIEF_PRINT = true;

		/**
		 * maximum number of entries
		 */
		public int nzmax ;

		/**
		 * number of rows
		 */
		public int m ;

		/**
		 * number of columns
		 */
		public int n ;

		/**
		 * column pointers (size n+1) or col indices (size nzmax)
		 */
		public int [] p ;

		/**
		 * row indices, size nzmax
		 */
		public int [] i ;

		/**
		 * numerical values, size 2 * nzmax
		 */
		public double [] x ;

		/**
		 * # of entries in triplet matrix, -1 for compressed-col
		 */
		public int nz ;

		public DZcs()
		{

		}

		public double [] get(final int idx)
		{
			int offset = 2 * idx ;
			return new double [] {x [offset], x [offset + 1]} ;
		}

		public void set(final int idx, final double [] val) {
			set(idx, val [0], val [1]);
		}

		public void set(final int idx, final double re, final double im)
		{
			int offset = 2 * idx ;

			x [offset] = re ;
			x [offset + 1] = im ;
		}

		public String toString() {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			cs_print(this, BRIEF_PRINT, out);
			try {
				return new String(out.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return out.toString();
			}
		}

	};

	/**
	 *
	 * Output of symbolic Cholesky, LU, or QR analysis.
	 *
	 */
	public static class DZcss
	{
		/**
		 * inverse row perm. for QR, fill red. perm for Chol
		 */
		public int [] pinv ;

		/**
		 * fill-reducing column permutation for LU and QR
		 */
		public int [] q ;

		/**
		 * elimination tree for Cholesky and QR
		 */
		public int [] parent ;

		/**
		 * column pointers for Cholesky, row counts for QR
		 */
		public int [] cp ;

		/**
		 * leftmost[i] = min(find(A(i,:))), for QR
		 */
		public int [] leftmost ;

		/**
		 * # of rows for QR, after adding fictitious rows
		 */
		public int m2 ;

		/**
		 * # entries in L for LU or Cholesky; in V for QR
		 */
		public int lnz ;

		/**
		 * # entries in U for LU; in R for QR
		 */
		public int unz ;

		public DZcss()
		{

		}
	};

	/**
	 *
	 * Output of numeric Cholesky, LU, or QR factorization
	 *
	 */
	public static class DZcsn
	{

		/**
		 * L for LU and Cholesky, V for QR
		 */
		public DZcs L ;

		/**
		 * U for LU, R for QR, not used for Cholesky
		 */
		public DZcs U ;

		/**
		 * partial pivoting for LU
		 */
		public int [] pinv ;

		/**
		 * beta [0..n-1] for QR
		 */
		public double [] B ;

		public DZcsn()
		{

		}

	};

	/**
	 *
	 * Output of Dulmage-Mendelsohn decomposition.
	 *
	 */
	public static class DZcsd
	{

		/**
		 * size m, row permutation
		 */
		public int [] p ;

		/**
		 * size n, column permutation
		 */
		public int [] q ;

		/**
		 * size nb+1, block k is rows r[k] to r[k+1]-1 in A(p,q)
		 */
		public int [] r ;

		/**
		 * size nb+1, block k is cols s[k] to s[k+1]-1 in A(p,q)
		 */
		public int [] s ;

		/**
		 * # of blocks in fine dmperm decomposition
		 */
		public int nb ;

		/**
		 * coarse row decomposition
		 */
		public int [] rr ;

		/**
		 * coarse column decomposition
		 */
		public int [] cc ;
	};

}
