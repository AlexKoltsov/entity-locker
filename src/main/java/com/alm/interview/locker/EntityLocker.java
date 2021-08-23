package com.alm.interview.locker;

import java.util.concurrent.TimeUnit;

public interface EntityLocker<T> {

    void runWithLock(T id, Runnable runnable, long time, TimeUnit unit) throws InterruptedException;

    default void runWithLock(T id, Runnable runnable) throws InterruptedException {
        runWithLock(id, runnable, 10, TimeUnit.SECONDS);
    }
}
