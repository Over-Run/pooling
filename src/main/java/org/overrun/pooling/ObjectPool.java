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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A growable object pool that allows borrowing and returning object instances.
 *
 * @param <T> the type of the instances in this pool.
 * @author squid233
 * @since 0.1.0
 */
public final class ObjectPool<T extends Poolable> implements Pool<T> {
    private final Consumer<T> cleanupAction;
    private final List<PoolObjectState<T>> states;
    private final Supplier<T> constructor;

    /**
     * Creates a growable object pool with the given initial capacity and cleanup action.
     *
     * @param constructor     the constructor of the objects.
     * @param cleanupAction   the cleanup action of this pool.
     * @param initialCapacity the initial capacity of this pool.
     */
    public ObjectPool(Supplier<T> constructor, Consumer<T> cleanupAction, int initialCapacity) {
        this.cleanupAction = cleanupAction;
        this.states = Collections.synchronizedList(new ArrayList<>(initialCapacity));
        this.constructor = constructor;
    }

    /**
     * Creates a growable object pool with the given initial capacity.
     *
     * @param constructor     the constructor of the objects.
     * @param initialCapacity the initial capacity of this pool.
     */
    public ObjectPool(Supplier<T> constructor, int initialCapacity) {
        this(constructor, null, initialCapacity);
    }

    /**
     * Creates a growable object pool.
     *
     * @param constructor the constructor of the objects.
     */
    public ObjectPool(Supplier<T> constructor) {
        this(constructor, 10);
    }

    @Override
    public boolean hasRemaining() {
        for (PoolObjectState<T> state : states) {
            if (!state.lent.get()) return true;
        }
        return false;
    }

    @Override
    public Result<T> borrow() {
        for (var state : states) {
            if (state.lent.compareAndSet(false, true)) {
                state.get().reset();
                return new Result<>(state, null);
            }
        }
        final var state = new PoolObjectState<>(states.size(), Objects.requireNonNull(constructor.get()));
        states.add(state);
        return new Result<>(state, null);
    }

    @Override
    public void returning(PoolObjectState<T> state) {
        state.lent.compareAndSet(true, false);
    }

    @Override
    public void cleanup() {
        if (cleanupAction != null) {
            for (PoolObjectState<T> state : states) {
                final T t = state.get();
                if (t != null) {
                    cleanupAction.accept(t);
                }
            }
        }
    }
}
