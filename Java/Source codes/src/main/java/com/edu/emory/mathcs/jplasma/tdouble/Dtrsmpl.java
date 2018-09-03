/* ***** BEGIN LICENSE BLOCK *****
 * -- Innovative Computing Laboratory
 * -- Electrical Engineering and Computer Science Department
 * -- University of Tennessee
 * -- (C) Copyright 2008
 *
 * Redistribution  and  use  in  source and binary forms, with or without
 * modification,  are  permitted  provided  that the following conditions
 * are met:
 *
 * * Redistributions  of  source  code  must  retain  the above copyright
 *   notice,  this  list  of  conditions  and  the  following  disclaimer.
 * * Redistributions  in  binary  form must reproduce the above copyright
 *   notice,  this list of conditions and the following disclaimer in the
 *   documentation  and/or other materials provided with the distribution.
 * * Neither  the  name of the University of Tennessee, Knoxville nor the
 *   names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS  SOFTWARE  IS  PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS''  AND  ANY  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A  PARTICULAR  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL,  EXEMPLARY,  OR  CONSEQUENTIAL  DAMAGES  (INCLUDING,  BUT NOT
 * LIMITED  TO,  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA,  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY  OF  LIABILITY,  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF  THIS  SOFTWARE,  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ***** END LICENSE BLOCK ***** */

package com.edu.emory.mathcs.jplasma.tdouble;

import java.util.concurrent.locks.Lock;

import com.edu.emory.mathcs.jplasma.Barrier;
import com.edu.emory.mathcs.jplasma.tdouble.Dallocate;
import com.edu.emory.mathcs.jplasma.tdouble.Dauxiliary;
import com.edu.emory.mathcs.jplasma.tdouble.DbdlConvert;
import com.edu.emory.mathcs.jplasma.tdouble.Dcommon;
import com.edu.emory.mathcs.jplasma.tdouble.Dglobal;
import com.edu.emory.mathcs.jplasma.tdouble.Dplasma;
import com.edu.emory.mathcs.jplasma.tdouble.Pdtrsmpl;

class Dtrsmpl {

    private Dtrsmpl() {

    }

    /*////////////////////////////////////////////////////////////////////////////////////////
     *  Forward substitution in tile LU
     *  Application of L to the right hand side
     */
    protected static int plasma_DTRSMPL(int M, int NRHS, int N, double[] A, int A_offset, int LDA, double[] L,
            int L_offset, int[] IPIV, int IPIV_offset, double[] B, int B_offset, int LDB) {
        int NB, MT, NT, NTRHS;
        int status;
        double[] Abdl;
        double[] Bbdl;
        double[] Lbdl;
        double[] bdl_mem;
        int size_elems;

        /* Check if initialized */
        if (!com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.initialized) {
            Dauxiliary.plasma_warning("plasma_DGETRS", "PLASMA not initialized");
            return com.edu.emory.mathcs.jplasma.tdouble.Dplasma.PLASMA_ERR_NOT_INITIALIZED;
        }

        /* Check input arguments */
        if (M < 0) {
            Dauxiliary.plasma_error("plasma_DGETRS", "illegal value of M");
            return com.edu.emory.mathcs.jplasma.tdouble.Dplasma.PLASMA_ERR_ILLEGAL_VALUE;
        }
        if (N < 0) {
            Dauxiliary.plasma_error("plasma_DGETRS", "illegal value of N");
            return com.edu.emory.mathcs.jplasma.tdouble.Dplasma.PLASMA_ERR_ILLEGAL_VALUE;
        }
        if (NRHS < 0) {
            Dauxiliary.plasma_error("plasma_DGETRS", "illegal value of NRHS");
            return com.edu.emory.mathcs.jplasma.tdouble.Dplasma.PLASMA_ERR_ILLEGAL_VALUE;
        }
        if (LDA < Math.max(1, M)) {
            Dauxiliary.plasma_error("plasma_DGETRS", "illegal value of LDA");
            return com.edu.emory.mathcs.jplasma.tdouble.Dplasma.PLASMA_ERR_ILLEGAL_VALUE;
        }
        if (LDB < Math.max(M, N)) {
            Dauxiliary.plasma_error("plasma_DGETRS", "illegal value of LDB");
            return com.edu.emory.mathcs.jplasma.tdouble.Dplasma.PLASMA_ERR_ILLEGAL_VALUE;
        }
        /* Quick return */
        if (Math.min(M, Math.min(N, NRHS)) == 0)
            return com.edu.emory.mathcs.jplasma.tdouble.Dplasma.PLASMA_SUCCESS;

        /* Tune NB & IB depending on M, N & NRHS; Set NBNBSIZE */
        status = Dauxiliary.plasma_tune(com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_TUNE_DGESV, N, N, NRHS);
        if (status != com.edu.emory.mathcs.jplasma.tdouble.Dplasma.PLASMA_SUCCESS) {
            Dauxiliary.plasma_error("plasma_DGETRS", "plasma_tune() failed");
            return status;
        }

        /* Set Mt, NT & NTRHS */
        NB = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.NB;
        MT = (M % NB == 0) ? (M / NB) : (M / NB + 1);
        NT = (N % NB == 0) ? (N / NB) : (N / NB + 1);
        NTRHS = (NRHS % NB == 0) ? (NRHS / NB) : (NRHS / NB + 1);

        /* If progress table too small, reallocate */
        size_elems = Math.max(MT, NT) * Math.max(NT, NTRHS);
        if (com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.progress_size_elems < size_elems) {
            status = Dallocate.plasma_free_aux_progress();
            if (status != com.edu.emory.mathcs.jplasma.tdouble.Dplasma.PLASMA_SUCCESS) {
                Dauxiliary.plasma_error("plasma_DGETRS", "plasma_free_aux_progress() failed");
            }
            status = Dallocate.plasma_alloc_aux_progress(size_elems);
            if (status != com.edu.emory.mathcs.jplasma.tdouble.Dplasma.PLASMA_SUCCESS) {
                Dauxiliary.plasma_error("plasma_DGETRS", "plasma_alloc_aux_progress() failed");
                return status;
            }
        }

        /* Assign arrays to BDL storage */
        bdl_mem = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_aux.bdl_mem;
        Abdl = bdl_mem;
        int Abdl_offset = 0;
        Lbdl = bdl_mem;
        int Lbdl_offset = MT * NT * com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.NBNBSIZE;
        Bbdl = bdl_mem;
        int Bbdl_offset = Lbdl_offset + MT * NT * com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.IBNBSIZE;
        size_elems = Bbdl_offset + Math.max(MT, NT) * NTRHS * com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.NBNBSIZE;
        /* If BDL storage too small, reallocate & reassign */
        if (com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.bdl_size_elems < size_elems) {
            status = Dallocate.plasma_free_aux_bdl();
            if (status != com.edu.emory.mathcs.jplasma.tdouble.Dplasma.PLASMA_SUCCESS) {
                Dauxiliary.plasma_error("plasma_DGETRS", "plasma_free_aux_bdl() failed");
                return status;
            }
            status = Dallocate.plasma_alloc_aux_bdl(size_elems);
            if (status != com.edu.emory.mathcs.jplasma.tdouble.Dplasma.PLASMA_SUCCESS) {
                Dauxiliary.plasma_error("plasma_DGETRS", "plasma_alloc_aux_bdl() failed");
                return status;
            }
            bdl_mem = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_aux.bdl_mem;
            Abdl = bdl_mem;
            Abdl_offset = 0;
            Lbdl = bdl_mem;
            Lbdl_offset = MT * NT * com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.NBNBSIZE;
            Bbdl = bdl_mem;
            Bbdl_offset = Lbdl_offset + MT * NT * com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.IBNBSIZE;
        }

        /* Convert A from LAPACK to BDL */
        /* Set arguments */
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.F77 = A;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.F77_offset = A_offset;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A = Abdl;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A_offset = Abdl_offset;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.M = M;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.N = N;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.LDA = LDA;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NB = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.NB;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.MT = MT;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NT = NT;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NBNBSIZE = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.NBNBSIZE;
        /* Signal workers */
        Lock lock = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action_mutex;
        lock.lock();
        try {
            com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action = com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_F77_TO_BDL;
            com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action_condt.signalAll();
        } finally {
            lock.unlock();
        }
        /* Call for master */
        Barrier.plasma_barrier(0, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.cores_num);
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action = com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_STAND_BY;
        DbdlConvert.plasma_lapack_to_bdl(com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.F77, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.F77_offset,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A_offset, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.M, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.N,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.LDA, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NB, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.MT, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NT,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NBNBSIZE, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.cores_num, 0);
        Barrier.plasma_barrier(0, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.cores_num);

        /* Convert B from LAPACK to BDL */
        /* Set arguments */
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.F77 = B;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.F77_offset = B_offset;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A = Bbdl;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A_offset = Bbdl_offset;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.M = M;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.N = NRHS;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.LDA = LDB;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NB = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.NB;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.MT = MT;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NT = NTRHS;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NBNBSIZE = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.NBNBSIZE;
        /* Signal workers */
        lock = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action_mutex;
        lock.lock();
        try {
            com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action = com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_F77_TO_BDL;
            com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action_condt.signalAll();
        } finally {
            lock.unlock();
        }
        /* Call for master */
        Barrier.plasma_barrier(0, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.cores_num);
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action = com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_STAND_BY;
        DbdlConvert.plasma_lapack_to_bdl(com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.F77, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.F77_offset,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A_offset, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.M, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.N,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.LDA, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NB, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.MT, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NT,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NBNBSIZE, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.cores_num, 0);
        Barrier.plasma_barrier(0, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.cores_num);

        /* Accept L from the user */
        System.arraycopy(L, L_offset, Lbdl, Lbdl_offset, MT * NT * com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.IBNBSIZE);

        /* Call parallel DTRSMPL */
        /* Set arguments */
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.M = M;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.N = N;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NRHS = NRHS;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A = Abdl;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A_offset = Abdl_offset;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NB = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.NB;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NBNBSIZE = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.NBNBSIZE;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.IBNBSIZE = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.IBNBSIZE;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.IB = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.IB;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.MT = MT;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NTRHS = NTRHS;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NT = NT;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.L = Lbdl;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.L_offset = Lbdl_offset;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.IPIV = IPIV;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.IPIV_offset = IPIV_offset;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.B = Bbdl;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.B_offset = Bbdl_offset;
        /* Clear progress table */
        Dauxiliary.plasma_clear_aux_progress(MT * NTRHS, -1);
        /* Signal workers */
        lock = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action_mutex;
        lock.lock();
        try {
            com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action = com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_DTRSMPL;
            com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action_condt.signalAll();
        } finally {
            lock.unlock();
        }
        /* Call for master */
        Barrier.plasma_barrier(0, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.cores_num);
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action = com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_STAND_BY;
        Pdtrsmpl.plasma_pDTRSMPL(com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.M, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NRHS, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.N,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A_offset, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NB,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NBNBSIZE, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.IBNBSIZE, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.IB,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.MT, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NTRHS, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NT, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.L,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.L_offset, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.IPIV, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.IPIV_offset,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.B, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.B_offset, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.INFO,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.cores_num, 0);
        Barrier.plasma_barrier(0, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.cores_num);

        /* Convert B from BDL to LAPACK */
        /* Set arguments */
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A = Bbdl;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A_offset = Bbdl_offset;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.F77 = B;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.F77_offset = B_offset;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.M = M;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.N = NRHS;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.LDA = LDB;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NB = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.NB;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.MT = MT;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NT = NTRHS;
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NBNBSIZE = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.NBNBSIZE;
        /* Signal workers */
        lock = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action_mutex;
        lock.lock();
        try {
            com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action = com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_BDL_TO_F77;
            com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action_condt.signalAll();
        } finally {
            lock.unlock();
        }
        /* Call for master */
        Barrier.plasma_barrier(0, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.cores_num);
        com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action = com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_STAND_BY;
        DbdlConvert.plasma_bdl_to_lapack(com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.A_offset, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.F77,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.F77_offset, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.M, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.N, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.LDA,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NB, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.MT, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NT, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args.NBNBSIZE,
                com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.cores_num, 0);
        Barrier.plasma_barrier(0, com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.cores_num);

        return Dplasma.PLASMA_SUCCESS;
    }

}
