package io.github.udayhe.quicksilver.threads;

import io.github.udayhe.quicksilver.config.Config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ThreadPoolManager {

    private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
    private final ScheduledExecutorService scheduledExecutorService;

    private ThreadPoolManager() {
        this.scheduledExecutorService = initializeScheduler();
    }

    private ScheduledExecutorService initializeScheduler() {
        int threadPoolSize = Config.getInstance().getThreadPoolSize();
        return Executors.newScheduledThreadPool(threadPoolSize);
    }

    public static ThreadPoolManager getInstance() {
        return INSTANCE;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public void shutdown() {
        try {
            scheduledExecutorService.shutdown();
            if (!scheduledExecutorService
                    .awaitTermination(Config.getInstance().getThreadPoolTerminationTimeout(), SECONDS)) {
                forceShutdown();
            }
        } catch (InterruptedException e) {
            forceShutdown();
            Thread.currentThread().interrupt();
        }
    }

    private void forceShutdown() {
        scheduledExecutorService.shutdownNow();
    }
}