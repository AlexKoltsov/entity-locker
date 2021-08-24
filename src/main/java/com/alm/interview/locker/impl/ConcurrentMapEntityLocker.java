package com.alm.interview.locker.impl;

import com.alm.interview.locker.EntityLocker;
import com.alm.interview.locker.GlobalLocker;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Entity locker implementation based on {@code ConcurrentMap}.
 * It supports concurrent execution of protected code on entities with different IDs.
 * <p/>
 * Global locking based on {@code ReadWriteLock}.
 *
 * @param <T> Entity ID's type
 */
public class ConcurrentMapEntityLocker<T> implements EntityLocker<T>, GlobalLocker {

    // TODO: Implement lock escalation

    private final ConcurrentMap<T, Lock> locks;
    private final ReadWriteLock globalLock = new ReentrantReadWriteLock();
    private final boolean fair;

    public ConcurrentMapEntityLocker() {
        this(false);
    }

    public ConcurrentMapEntityLocker(boolean fair) {
        this(new ConcurrentHashMap<>(), fair);
    }

    public ConcurrentMapEntityLocker(ConcurrentMap<T, Lock> locks, boolean fair) {
        this.locks = locks;
        this.fair = fair;
    }

    @Override
    public void runWithLock(T id, Runnable runnable, long time, TimeUnit unit) throws InterruptedException {
        Lock lock = getLock(id);
        if (lock.tryLock(time, unit)) {
            runAndUnlock(runnable, lock);
        } else {
            interrupt();
        }
    }

    @Override
    public void runWithGlobalLock(Runnable runnable, long time, TimeUnit unit) throws InterruptedException {
        if (globalLock.writeLock().tryLock(time, unit)) {
            runGlobally(runnable);
        } else {
            interrupt();
        }
    }

    private Lock getLock(T id) {
        return Optional.ofNullable(id)
                .map(key -> locks.computeIfAbsent(id, _id -> new ReentrantLock(fair)))
                .orElseThrow(() -> new IllegalArgumentException("Entity ID could not be null"));
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

    private void runGlobally(Runnable runnable) {
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
