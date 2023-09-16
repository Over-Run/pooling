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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A fixed-size object pool that allows borrowing and returning object instances.
 *
 * @param <T> the type of the instances in this pool.
 * @author squid233
 * @since 0.1.0
 */
public final class FixedObjectPool<T extends Poolable> implements Pool<T> {
    private final Consumer<T> cleanupAction;
    private final AtomicReferenceArray<PoolObjectState<T>> states;
    private final Supplier<T> constructor;

    /**
     * Creates a fixed-size object pool with the given size and cleanup action.
     *
     * @param size          the size of this pool.
     * @param constructor   the constructor of the objects.
     * @param cleanupAction the cleanup action of this pool.
     */
    public FixedObjectPool(int size, Supplier<T> constructor, Consumer<T> cleanupAction) {
        this.cleanupAction = cleanupAction;
        this.states = new AtomicReferenceArray<>(size);
        this.constructor = constructor;
    }

    /**
     * Creates a fixed-size object pool with the given size.
     *
     * @param size        the size of this pool.
     * @param constructor the constructor of the objects.
     */
    public FixedObjectPool(int size, Supplier<T> constructor) {
        this(size, constructor, null);
    }

    @Override
    public boolean hasRemaining() {
        for (int i = 0, c = states.length(); i < c; i++) {
            if (!states.get(i).lent.get()) return true;
        }
        return false;
    }

    @Override
    public Result<T> borrow() {
        for (int i = 0, c = states.length(); i < c; i++) {
            if (states.get(i) == null) {
                var state = new PoolObjectState<>(i, Objects.requireNonNull(constructor.get()));
                states.set(i, state);
                return new Result<>(state, null);
            }
            var state = states.get(i);
            if (state.lent.compareAndSet(false, true)) {
                state.get().reset();
                return new Result<>(state, null);
            }
        }
        return new Result<>(null,
            new ArrayIndexOutOfBoundsException("FixedObjectPool has exceeded the limit: " + states.length()));
    }

    @Override
    public void returning(PoolObjectState<T> state) {
        state.lent.compareAndSet(true, false);
    }

    @Override
    public void cleanup() {
        if (cleanupAction != null) {
            for (int i = 0, c = states.length(); i < c; i++) {
                var state = states.get(i);
                final T t = state.get();
                if (t != null) {
                    cleanupAction.accept(t);
                }
            }
        }
    }
}
