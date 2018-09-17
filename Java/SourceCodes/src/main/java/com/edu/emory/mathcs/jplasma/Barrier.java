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

package com.edu.emory.mathcs.jplasma;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A barrier used for threads synchronization. This class is not a part of
 * user's API.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class Barrier {

    private static CountDownLatch workersLatch;
    private static Lock masterLock;
    private static Condition masterCondition;

    /*////////////////////////////////////////////////////////////////////////////////////////
     *  Barrier initialization
     */
    public static void plasma_barrier_init(int num_workers) {
        workersLatch = new CountDownLatch(num_workers);
        masterLock = new ReentrantLock();
        masterCondition = masterLock.newCondition();

    }

    public static void plasma_barrier(int my_core_id, int cores_num) {
        if (my_core_id == 0) {
            try {
                workersLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            masterLock.lock();
            try {
                workersLatch = new CountDownLatch(cores_num - 1);
                masterCondition.signalAll();
            } finally {
                masterLock.unlock();
            }
        } else {
           masterLock.lock();
            try {
                workersLatch.countDown();
                masterCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                masterLock.unlock();
            }
        }
    }
}
