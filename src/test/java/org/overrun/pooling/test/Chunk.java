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

import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author squid233
 * @since 0.1.0
 */
final class Chunk {
    final World world;
    final int x, y, z;
    final AtomicBoolean submitted = new AtomicBoolean();
    final AtomicBoolean compiled = new AtomicBoolean();
    final AtomicBoolean dirty = new AtomicBoolean(true);
    final AtomicInteger data = new AtomicInteger();

    Chunk(World world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    void render(IntBuffer buffer) {
        buffer.put(world.data[World.index(x, y, z)]);
    }
}
