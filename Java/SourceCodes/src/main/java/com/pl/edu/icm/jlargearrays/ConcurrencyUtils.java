/* ***** BEGIN LICENSE BLOCK *****
 * JLargeArrays
 * Copyright (C) 2013 onward University of Warsaw, ICM
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ***** END LICENSE BLOCK ***** */
package com.pl.edu.icm.jlargearrays;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.util.FastMath;

/**
 * Concurrency utilities.
 * <p>
 * @author Piotr Wendykier (p.wendykier@icm.edu.pl)
 */
public class ConcurrencyUtils
{

    /**
     * Thread pool.
     */
    private static final ExecutorService DEFAULT_THREAD_POOL = Executors.newCachedThreadPool(new CustomThreadFactory(new CustomExceptionHandler()));

    private static ExecutorService threadPool = DEFAULT_THREAD_POOL;

    private static int nthreads = getNumberOfProcessors();

    private static long concurrentThreshold = 100000;

    private ConcurrencyUtils()
    {

    }

    private static class CustomExceptionHandler implements Thread.UncaughtExceptionHandler
    {

        @Override
        public void uncaughtException(Thread t, Throwable e)
        {
            e.printStackTrace();
        }

    }

    private static class CustomThreadFactory implements ThreadFactory
    {

        private static final ThreadFactory DEFAULT_FACTORY = Executors.defaultThreadFactory();

        private final Thread.UncaughtExceptionHandler handler;

        CustomThreadFactory(Thread.UncaughtExceptionHandler handler)
        {
            this.handler = handler;
        }

        @Override
        public Thread newThread(Runnable r)
        {
            Thread t = DEFAULT_FACTORY.newThread(r);
            t.setUncaughtExceptionHandler(handler);
            return t;
        }
    };

    /**
     * Returns the minimum length of array for which multiple threads are used.
     * <p>
     * @return the minimum length of array for which multiple threads are used
     */
    public static long getConcurrentThreshold()
    {
        return ConcurrencyUtils.concurrentThreshold;
    }

    /**
     * Sets the minimum length of an array for which multiple threads are used.
     * <p>
     * @param concurrentThreshold minimum length of an array for which multiple threads are used
     */
    public static void setConcurrentThreshold(long concurrentThreshold)
    {
        ConcurrencyUtils.concurrentThreshold = FastMath.max(1, concurrentThreshold);
    }

    /**
     * Returns the number of available processors.
     * <p>
     * @return number of available processors
     */
    public static int getNumberOfProcessors()
    {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Returns the current number of threads.
     * <p>
     * @return the current number of threads.
     */
    public static int getNumberOfThreads()
    {
        return ConcurrencyUtils.nthreads;
    }

    /**
     * Sets the number of threads.
     * <p>
     * @param n new value of threads
     */
    public static void setNumberOfThreads(int n)
    {
        ConcurrencyUtils.nthreads = n;
    }

    /**
     * Submits a value-returning task for execution and returns a Future
     * representing the pending results of the task.
     * <p>
     * @param <T>  type
     * @param task task for execution
     * <p>
     * @return handle to the task submitted for execution
     */
    public static <T> Future<T> submit(Callable<T> task)
    {
        if (ConcurrencyUtils.threadPool.isShutdown() || ConcurrencyUtils.threadPool.isTerminated()) {
            ConcurrencyUtils.threadPool = DEFAULT_THREAD_POOL;
        }
        return ConcurrencyUtils.threadPool.submit(task);
    }

    /**
     * Submits a Runnable task for execution and returns a Future representing that task.
     * <p>
     * @param task task for execution
     * <p>
     * @return handle to the task submitted for execution
     */
    public static Future<?> submit(Runnable task)
    {
        if (ConcurrencyUtils.threadPool.isShutdown() || ConcurrencyUtils.threadPool.isTerminated()) {
            ConcurrencyUtils.threadPool = DEFAULT_THREAD_POOL;
        }
        return ConcurrencyUtils.threadPool.submit(task);
    }

    /**
     * Waits for all threads to complete computation.
     * <p>
     * @param futures list of handles to the tasks
     * <p>
     * @throws ExecutionException   if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    public static void waitForCompletion(Future<?>[] futures) throws InterruptedException, ExecutionException
    {
        int size = futures.length;
        for (int j = 0; j < size; j++) {
            futures[j].get();
        }
    }

    /**
     * Sets the pool of threads.
     * <p>
     * @param threadPool pool of threads
     */
    public static void setThreadPool(ExecutorService threadPool)
    {
        ConcurrencyUtils.threadPool = threadPool;
    }

    /**
     * Returns the pool of threads.
     * <p>
     * @return pool of threads
     */
    public static ExecutorService getThreadPool()
    {
        return ConcurrencyUtils.threadPool;
    }

    /**
     * Shutdowns all submitted tasks.
     */
    public static void shutdownThreadPoolAndAwaitTermination()
    {
        ConcurrencyUtils.threadPool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!ConcurrencyUtils.threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                ConcurrencyUtils.threadPool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!ConcurrencyUtils.threadPool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            ConcurrencyUtils.threadPool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
