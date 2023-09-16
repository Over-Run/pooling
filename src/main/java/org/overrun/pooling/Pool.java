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
 * An object pool.
 *
 * @param <T> the type of the instances in this pool.
 * @author squid233
 * @since 0.1.0
 */
public interface Pool<T extends Poolable> {
    /**
     * {@return {@code true} if this pool has remaining.}
     */
    boolean hasRemaining();

    /**
     * Borrows an object from this pool.
     *
     * @return the result that contains the instance; or {@link Throwable} if this pool has exceeded the limit.
     */
    Result<T> borrow();

    /**
     * Returns the result to this pool.
     *
     * @param state the state that holds the object instance.
     */
    void returning(PoolObjectState<T> state);

    /**
     * Executes the cleanup action of this pool.
     */
    void cleanup();
}
