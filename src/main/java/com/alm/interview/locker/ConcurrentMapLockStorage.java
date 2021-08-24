package com.alm.interview.locker;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code LockStorage} implementation based on {@code ConcurrentMap}
 *
 * @param <T> Entity ID's type
 */
public class ConcurrentMapLockStorage<T> implements LockStorage<T> {

    private final ConcurrentMap<T, Lock> locks = new ConcurrentHashMap<>();

    /**
     * Get already existent lock by ID or create a new one.
     *
     * @param id entity's ID
     * @return {@code Lock} of entity
     * @throws IllegalArgumentException if id is null
     */
    @Override
    public Lock getLockById(T id) {
        return Optional.ofNullable(id)
                .map(key -> locks.computeIfAbsent(id, _id -> new ReentrantLock()))
                .orElseThrow(() -> new IllegalArgumentException("Entity ID could not be null"));
    }
}
