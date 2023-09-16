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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.overrun.pooling.FixedObjectPool;
import org.overrun.pooling.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author squid233
 * @since 0.1.0
 */
class FixedObjectPoolTest {
    private static final int SIZE = 4;
    static World world;
    static FixedObjectPool<ChunkCompiler> pool;
    static AtomicInteger timer;
    static ExecutorService executorService;

    @BeforeAll
    static void beforeAll() {
        world = new World();
        pool = new FixedObjectPool<>(SIZE, ChunkCompiler::new);
        timer = new AtomicInteger(10);
        final int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Using " + processors + " processors");
        executorService = new ThreadPoolExecutor(processors,
            processors + 1,
            3,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            (r, executor) -> {
                if (!executor.isShutdown() && r instanceof Future<?> future) {
                    future.cancel(true);
                }
            });
    }

    static boolean running() {
        return timer.get() > 0;
    }

    List<Chunk> dirtyChunks() {
        List<Chunk> chunks = null;
        Chunk[] chunks1 = world.chunks;
        for (int i = 0, j = 0; i < chunks1.length && j < SIZE; i++) {
            Chunk chunk = chunks1[i];
            if (chunk.dirty.get() && !chunk.submitted.get()) {
                if (chunks == null) {
                    chunks = new ArrayList<>();
                }
                chunks.add(chunk);
                j++;
            }
        }
        return chunks;
    }

    void compile() {
        final List<Chunk> dirtyChunks = dirtyChunks();
        if (dirtyChunks != null) {
            for (Chunk dirtyChunk : dirtyChunks) {
                CompletableFuture.supplyAsync(() -> {
                        Result<ChunkCompiler> result = null;
                        try {
                            result = pool.borrow();
                            if (result.successful()) {
                                dirtyChunk.submitted.set(true);
                                result.state().get().compile(dirtyChunk);
                            }
                        } finally {
                            if (result != null && result.successful()) {
                                pool.returning(result.state());
                            }
                        }
                        return dirtyChunk;
                    }, executorService)
                    .thenAccept(chunk -> {
                        chunk.dirty.set(false);
                        chunk.submitted.set(false);
                        chunk.compiled.set(true);
                    });
            }
        }
    }

    @SuppressWarnings("BusyWait")
    @Test
    void test() throws InterruptedException {
        final Thread renderThread = new Thread(() -> {
            while (running()) {
                try {
                    world.render();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "Render Thread");
        renderThread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        renderThread.start();
        while (running()) {
            Thread.sleep(1000);
            timer.getAndDecrement();
            compile();
        }
    }

    @AfterAll
    static void afterAll() {
        pool.cleanup();
        executorService.shutdown();
    }
}
