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

package org.overrun.pooling.test;

import org.overrun.pooling.Poolable;

import java.nio.IntBuffer;

/**
 * @author squid233
 * @since 0.1.0
 */
final class ChunkCompiler implements Poolable {
    final IntBuffer buffer = IntBuffer.allocate(1);

    void compile(Chunk chunk) {
        buffer.clear();
        chunk.render(buffer);
        buffer.flip();
        chunk.data.set(buffer.get(0));
    }

    @Override
    public void reset() {
        buffer.clear();
    }
}
