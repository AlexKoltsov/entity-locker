package com.alm.interview.locker.impl;

import com.alm.interview.locker.EntityLocker;
import com.alm.interview.locker.GlobalLocker;
import com.alm.interview.locker.LockStorage;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Entity locker implementation.
 * It supports concurrent execution of protected code on entities with different IDs.
 * Global locking based on {@code ReadWriteLock}.
 *
 * @param <T> Entity ID's type
 */
public abstract class DefaultEntityLocker<T> implements EntityLocker<T>, GlobalLocker {

    // TODO: Implement lock escalation

    private final LockStorage<T> lockStorage;
    private final ReadWriteLock globalLock = new ReentrantReadWriteLock();

    public DefaultEntityLocker(LockStorage<T> lockStorage) {
        this.lockStorage = lockStorage;
    }

    @Override
    public void runWithLock(T id, Runnable runnable, long time, TimeUnit unit) throws InterruptedException {
        Lock lock = lockStorage.getLockById(id);
        if (lock.tryLock(time, unit)) {
            runAndUnlock(runnable, lock);
        } else {
            interrupt();
        }
    }

    @Override
    public void runWithGlobalLock(Runnable runnable, long time, TimeUnit unit) throws InterruptedException {
        if (globalLock.writeLock().tryLock(time, unit)) {
            runGloballyAndUnblock(runnable);
        } else {
            interrupt();
        }
    }

    private void runAndUnlock(Runnable runnable, Lock lock) {
        try {
            globalLock.readLock().lock();
            runnable.run();
        } finally {
            lock.unlock();
            globalLock.readLock().unlock();
        }
    }

    private void runGloballyAndUnblock(Runnable runnable) {
        try {
            runnable.run();
        } finally {
            globalLock.writeLock().unlock();
        }
    }

    private void interrupt() throws InterruptedException {
        Thread.currentThread().interrupt();
        throw new InterruptedException();
    }
}
