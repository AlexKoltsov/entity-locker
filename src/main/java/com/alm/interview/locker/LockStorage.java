package com.alm.interview.locker;

import java.util.concurrent.locks.Lock;

public interface LockStorage<T> {

    /**
     * Get {@code Lock} by entity's ID
     *
     * @param id entity's ID
     * @return {@code Lock} of entity
     */
    Lock getLockById(T id);
}
