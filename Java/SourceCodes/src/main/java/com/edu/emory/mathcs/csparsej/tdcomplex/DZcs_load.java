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

import java.io.BufferedReader ;
import java.io.IOException ;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_common.DZcs ;

import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_util.cs_spalloc ;
import static com.edu.emory.mathcs.csparsej.tdcomplex.DZcs_entry.cs_entry ;

/**
 * Load a sparse matrix from a file.
 *
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * @author Richard Lincoln (r.w.lincoln@gmail.com)
 *
 */
public class DZcs_load {

	/**
	 * Loads a triplet matrix T from a file. Each line of the file contains
	 * four values: a row index i, a column index j, a real value aij, and an
	 * imaginary value aij.
	 * The file is zero-based.
	 *
	 * @param fileName
	 *            file name
	 * @return T if successful, null on error
	 */
	public static DZcs cs_load(InputStream in)
	{
		int i, j;
		double x[], x_re, x_im ;
		DZcs T;
		String line, tokens[] ;
		Reader r = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(r);

		T = cs_spalloc(0, 0, 1, true, true) ;	/* allocate result */

		try
		{
			while ((line = br.readLine()) != null)
			{
				tokens = line.trim().split("\\s+") ;
				if (tokens.length != 4) return null ;
				i = Integer.parseInt(tokens [0]) ;
				j = Integer.parseInt(tokens [1]) ;
				x_re = Double.parseDouble(tokens [2]) ;
				x_im = Double.parseDouble(tokens [3]) ;
				x = new double[] {x_re, x_im} ;
				if (!cs_entry(T, i, j, x)) return (null) ;
			}
			r.close();
			br.close();
		}
		catch (IOException e)
		{
			return (null) ;
		}
		return (T) ;
	}

}
