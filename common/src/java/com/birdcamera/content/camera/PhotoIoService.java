package com.birdcamera.content.camera;

import com.birdcamera.BirdCameraMod;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;

/**
 * 照片 I/O 异步线程池（迁移自 guaniao-2.1.3）。
 */
public final class PhotoIoService {
    private static final int THREADS = 2;
    private static final int QUEUE_CAPACITY = 32;
    private static ThreadPoolExecutor executor = createExecutor();

    private PhotoIoService() {
    }

    public static synchronized <T> boolean submit(MinecraftServer server, Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        if (executor.isShutdown()) {
            executor = createExecutor();
        }
        try {
            executor.execute(() -> {
                try {
                    T result = work.call();
                    server.execute(() -> onSuccess.accept(result));
                } catch (Throwable e) {
                    server.execute(() -> onFailure.accept(e));
                }
            });
            return true;
        } catch (RejectedExecutionException e) {
            BirdCameraMod.LOGGER.warn("Photograph I/O queue is full; rejecting work");
            return false;
        }
    }

    public static synchronized void shutdown() {
        executor.shutdownNow();
    }

    public static synchronized int queuedTasks() {
        return executor.getQueue().size();
    }

    private static ThreadPoolExecutor createExecutor() {
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "BirdCamera-Photo-IO");
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((ignored, throwable) ->
                    BirdCameraMod.LOGGER.error("Uncaught photograph I/O error", throwable));
            return thread;
        };
        ThreadPoolExecutor created = new ThreadPoolExecutor(THREADS, THREADS, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY), factory, new AbortPolicy());
        created.prestartAllCoreThreads();
        return created;
    }
}