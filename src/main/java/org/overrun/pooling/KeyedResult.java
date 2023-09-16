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
 * A keyed result that holds the state and an exception.
 *
 * @author squid233
 * @since 0.1.0
 */
public /* value */ record KeyedResult<K, T extends Poolable>(KeyedPoolObjectState<K, T> state, Throwable throwable) {
    /**
     * {@return {@code true} if successful}
     */
    public boolean successful() {
        return state != null && throwable == null;
    }

    /**
     * {@return {@code true} if failed}
     */
    public boolean failed() {
        return state == null || throwable != null;
    }
}
