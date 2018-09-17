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
import com.edu.emory.mathcs.jplasma.tdouble.DbdlConvert;
import com.edu.emory.mathcs.jplasma.tdouble.Dcommon;
import com.edu.emory.mathcs.jplasma.tdouble.Dglobal;
import com.edu.emory.mathcs.jplasma.tdouble.Dglobal.Plasma_args;
import com.edu.emory.mathcs.jplasma.tdouble.Dplasma;
import com.edu.emory.mathcs.jplasma.tdouble.Pdgelqf;
import com.edu.emory.mathcs.jplasma.tdouble.Pdgeqrf;
import com.edu.emory.mathcs.jplasma.tdouble.Pdgetrf;
import com.edu.emory.mathcs.jplasma.tdouble.Pdormqr;
import com.edu.emory.mathcs.jplasma.tdouble.Pdpotrf;
import com.edu.emory.mathcs.jplasma.tdouble.Pdtrsm;
import com.edu.emory.mathcs.jplasma.tdouble.Pdtrsmpl;

class DcoreControl implements Runnable {
    private int my_core_id;

    protected DcoreControl(int core_id) {
        this.my_core_id = core_id;
    }

    public void run() {

        int action = 0;
        int cores_num = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.cores_num;
        Plasma_args args = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_args;
        Barrier.plasma_barrier(my_core_id, cores_num);

        while (true) {
            Lock lock = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action_mutex;
            lock.lock();
            try {
                while ((action = com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action) == com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_STAND_BY) {
                    com.edu.emory.mathcs.jplasma.tdouble.Dcommon.plasma_cntrl.action_condt.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
            Barrier.plasma_barrier(my_core_id, cores_num);

            switch (action) {
            case com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_DGEQRF:
                com.edu.emory.mathcs.jplasma.tdouble.Pdgeqrf.plasma_pDGEQRF(args.M, args.N, args.A, args.A_offset, args.NB, args.NBNBSIZE, args.IBNBSIZE,
                        args.IB, args.MT, args.NT, args.T, args.T_offset, args.INFO, cores_num, my_core_id);
                break;
            case com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_DGELQF:
                com.edu.emory.mathcs.jplasma.tdouble.Pdgelqf.plasma_pDGELQF(args.M, args.N, args.A, args.A_offset, args.NB, args.NBNBSIZE, args.IBNBSIZE,
                        args.IB, args.MT, args.NT, args.T, args.T_offset, args.INFO, cores_num, my_core_id);
                break;
            case com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_DORMQR:
                com.edu.emory.mathcs.jplasma.tdouble.Pdormqr.plasma_pDORMQR(args.M, args.NRHS, args.N, args.A, args.A_offset, args.NB, args.NBNBSIZE,
                        args.IBNBSIZE, args.IB, args.MT, args.NTRHS, args.NT, args.T, args.T_offset, args.B,
                        args.B_offset, args.INFO, cores_num, my_core_id);
                break;
            case com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_DTRSM:
                com.edu.emory.mathcs.jplasma.tdouble.Pdtrsm.plasma_pDTRSM(Dplasma.PlasmaLeft, args.uplo, args.trans, args.diag, args.N, args.NRHS, 1.0,
                        args.A, args.A_offset, args.NB, args.NBNBSIZE, args.NT, args.MT, args.B, args.B_offset,
                        args.MTB, args.NTRHS, cores_num, my_core_id);
                break;
            case com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_DPOTRF:
                com.edu.emory.mathcs.jplasma.tdouble.Pdpotrf.plasma_pDPOTRF(args.uplo, args.N, args.A, args.A_offset, args.NB, args.NBNBSIZE, args.NT,
                        args.INFO, cores_num, my_core_id);
                break;
            case com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_DGETRF:
                com.edu.emory.mathcs.jplasma.tdouble.Pdgetrf.plasma_pDGETRF(args.M, args.N, args.A, args.A_offset, args.NB, args.NBNBSIZE, args.IBNBSIZE,
                        args.IB, args.MT, args.NT, args.L, args.L_offset, args.IPIV, args.IPIV_offset, args.INFO,
                        cores_num, my_core_id);
                break;
            case com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_DTRSMPL:
                com.edu.emory.mathcs.jplasma.tdouble.Pdtrsmpl.plasma_pDTRSMPL(args.M, args.NRHS, args.N, args.A, args.A_offset, args.NB, args.NBNBSIZE,
                        args.IBNBSIZE, args.IB, args.MT, args.NTRHS, args.NT, args.L, args.L_offset, args.IPIV,
                        args.IPIV_offset, args.B, args.B_offset, args.INFO, cores_num, my_core_id);
                break;
            case com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_F77_TO_BDL:
                com.edu.emory.mathcs.jplasma.tdouble.DbdlConvert.plasma_lapack_to_bdl(args.F77, args.F77_offset, args.A, args.A_offset, args.M, args.N,
                        args.LDA, args.NB, args.MT, args.NT, args.NBNBSIZE, cores_num, my_core_id);
                break;
            case com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_BDL_TO_F77:
                com.edu.emory.mathcs.jplasma.tdouble.DbdlConvert.plasma_bdl_to_lapack(args.A, args.A_offset, args.F77, args.F77_offset, args.M, args.N,
                        args.LDA, args.NB, args.MT, args.NT, args.NBNBSIZE, cores_num, my_core_id);
                break;
            case com.edu.emory.mathcs.jplasma.tdouble.Dglobal.PLASMA_ACT_FINALIZE:
                return;
            default:
                return;
            }
            Barrier.plasma_barrier(my_core_id, cores_num);
        }
    }
}
