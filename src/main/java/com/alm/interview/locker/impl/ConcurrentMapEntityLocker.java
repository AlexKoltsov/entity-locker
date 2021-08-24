package com.alm.interview.locker.impl;

import com.alm.interview.locker.ConcurrentMapLockStorage;

/**
 * Entity locker implementation based on {@code ConcurrentMapLockStorage}.
 *
 * @param <T> Entity ID's type
 */
public class ConcurrentMapEntityLocker<T> extends DefaultEntityLocker<T> {
    public ConcurrentMapEntityLocker() {
        super(new ConcurrentMapLockStorage<>());
    }
}
