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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A growable keyed object pool that allows borrowing and returning object instances.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class KeyedObjectPool<K, T extends Poolable> implements KeyedPool<K, T> {
    private final Consumer<T> cleanupAction;
    private final Map<K, List<KeyedPoolObjectState<K, T>>> states;
    private final Function<K, T> constructor;
    private final int initialCapacity;

    /**
     * Creates a growable keyed object pool with the given initial capacity and cleanup action.
     *
     * @param constructor     the constructor of the objects.
     * @param cleanupAction   the cleanup action of this pool.
     * @param numMappings     the initial mapping count of the map.
     * @param initialCapacity the initial capacity of the lists.
     */
    public KeyedObjectPool(Function<K, T> constructor, Consumer<T> cleanupAction, int numMappings, int initialCapacity) {
        this.cleanupAction = cleanupAction;
        this.states = Collections.synchronizedMap(new HashMap<>((int) (numMappings / 0.75)));
        this.constructor = constructor;
        this.initialCapacity = initialCapacity;
    }

    /**
     * Creates a growable keyed object pool with the given initial capacity.
     *
     * @param constructor     the constructor of the objects.
     * @param numMappings     the initial mapping count of the map.
     * @param initialCapacity the initial capacity of the lists.
     */
    public KeyedObjectPool(Function<K, T> constructor, int numMappings, int initialCapacity) {
        this(constructor, null, numMappings, initialCapacity);
    }

    /**
     * Creates a growable keyed object pool.
     *
     * @param constructor the constructor of the objects.
     */
    public KeyedObjectPool(Function<K, T> constructor) {
        this(constructor, 12, 10);
    }

    @Override
    public boolean hasRemaining(K key) {
        final var list = states.get(key);
        if (list == null) return false;
        for (var state : list) {
            if (!state.lent.get()) return true;
        }
        return false;
    }

    @Override
    public KeyedResult<K, T> borrow(K key) {
        final var list = states.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>(initialCapacity)));
        for (var state : list) {
            if (state.lent.compareAndSet(false, true)) {
                state.get().reset();
                return new KeyedResult<>(state, null);
            }
        }
        final var state = new KeyedPoolObjectState<>(key, list.size(), Objects.requireNonNull(constructor.apply(key)));
        list.add(state);
        return new KeyedResult<>(state, null);
    }

    @Override
    public void returning(KeyedPoolObjectState<K, T> state) {
        state.lent.compareAndSet(true, false);
    }

    @Override
    public void cleanup() {
        if (cleanupAction != null) {
            states.values().forEach(list -> {
                for (var state : list) {
                    final T t = state.get();
                    if (t != null) {
                        cleanupAction.accept(t);
                    }
                }
            });
        }
    }
}
