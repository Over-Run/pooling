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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The state that holds the instance of the object.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class KeyedPoolObjectState<K, T extends Poolable> {
    final K key;
    final int id;
    private final AtomicReference<T> object;
    final AtomicBoolean lent = new AtomicBoolean(true);

    KeyedPoolObjectState(K key, int id, T object) {
        this.key = key;
        this.id = id;
        this.object = new AtomicReference<>(object);
    }

    /**
     * {@return the instance of the object}
     */
    public T get() {
        return object.get();
    }
}
