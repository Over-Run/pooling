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

/**
 * @author squid233
 * @since 0.1.0
 */
final class World {
    static final int width = 3;
    static final int height = 3;
    static final int depth = 3;
    final int[] data = new int[width * height * depth];
    final Chunk[] chunks = new Chunk[width * height * depth];

    World() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    final int i = index(x, y, z);
                    data[i] = i;
                    chunks[i] = new Chunk(this, x, y, z);
                }
            }
        }
    }

    static int index(int x, int y, int z) {
        return (y * depth + z) * width + x;
    }

    void render() throws InterruptedException {
        System.out.println("----- render -----");
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    final Chunk chunk = chunks[index(x, y, z)];
                    if (chunk.compiled.get()) {
                        System.out.println("x: " + x + ", y: " + y + ", z: " + z + ", data: " + chunk.data.get());
                    }
                }
            }
        }
        Thread.sleep(1000);
    }
}
