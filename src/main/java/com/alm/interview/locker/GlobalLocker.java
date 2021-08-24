package com.alm.interview.locker;

import java.util.concurrent.TimeUnit;

/**
 * Reusable entity locker that provides global synchronization mechanism.
 */
public interface GlobalLocker {

    /**
     * Run protected code that executes under a global lock
     *
     * @param runnable callback to be run in critical section
     * @param time     the maximum time to wait for the lock
     * @param unit     the time unit of the {@code time} argument
     * @throws InterruptedException if the current thread is interrupted
     *                              while acquiring the lock
     */
    void runWithGlobalLock(Runnable runnable, long time, TimeUnit unit) throws InterruptedException;

    default void runWithGlobalLock(Runnable runnable) throws InterruptedException {
        runWithGlobalLock(runnable, 10, TimeUnit.SECONDS);
    }
}
