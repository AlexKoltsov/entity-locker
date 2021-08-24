package com.alm.interview.locker;

import java.util.concurrent.TimeUnit;

/**
 * Reusable entity locker that provides
 * synchronization mechanism similar to row-level DB locking.
 *
 * @param <T> Entity ID's type
 */
public interface EntityLocker<T> {

    /**
     * Run protected code for entity with id {@code id}
     *
     * @param id       entity's ID
     * @param runnable callback to be run in critical section
     * @param time     the maximum time to wait for the lock
     * @param unit     the time unit of the {@code time} argument
     * @throws InterruptedException if the current thread is interrupted
     *                              while acquiring the lock
     */
    void runWithLock(T id, Runnable runnable, long time, TimeUnit unit) throws InterruptedException;

    default void runWithLock(T id, Runnable runnable) throws InterruptedException {
        runWithLock(id, runnable, 10, TimeUnit.SECONDS);
    }
}
