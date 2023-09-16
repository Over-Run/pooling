/*
 * MIT License
 *
 * Copyright (c) 2023 Overrun Organization
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package org.overrun.pooling;

/**
 * A keyed object pool.
 *
 * @param <K> the type of the key.
 * @param <T> the type of the instances in this pool.
 * @author squid233
 * @since 0.1.0
 */
public interface KeyedPool<K, T extends Poolable> {
    /**
     * {@return {@code true} if this pool has remaining.}
     *
     * @param key the key.
     */
    boolean hasRemaining(K key);

    /**
     * Borrows an object with the given key from this pool.
     *
     * @param key the key.
     * @return the result that contains the instance; or {@link Throwable} if this pool has exceeded the limit.
     */
    KeyedResult<K, T> borrow(K key);

    /**
     * Returns the result to this pool.
     *
     * @param state the state that holds the object instance.
     */
    void returning(KeyedPoolObjectState<K, T> state);

    /**
     * Executes the cleanup action of this pool.
     */
    void cleanup();
}
