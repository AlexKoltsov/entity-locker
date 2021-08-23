package com.alm.interview.locker;

import java.util.concurrent.TimeUnit;

public interface GlobalLocker {

    void runWithGlobalLock(Runnable runnable, long time, TimeUnit unit) throws InterruptedException;

    default void runWithGlobalLock(Runnable runnable) throws InterruptedException {
        runWithGlobalLock(runnable, 10, TimeUnit.SECONDS);
    }
}
