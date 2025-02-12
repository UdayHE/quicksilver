package io.github.udayhe.quicksilver.threads;

import io.github.udayhe.quicksilver.config.Config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ThreadPoolManager {

    private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
    private static final int TERMINATION_TIMEOUT = 5; // Timeout for scheduler termination in seconds

    private final ScheduledExecutorService scheduledExecutorService;

    private ThreadPoolManager() {
        this.scheduledExecutorService = initializeScheduler();
    }

    // Creates and initializes the scheduled executor service
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
            if (!scheduledExecutorService.awaitTermination(TERMINATION_TIMEOUT, SECONDS)) {
                forceShutdown();
            }
        } catch (InterruptedException e) {
            forceShutdown();
            Thread.currentThread().interrupt();
        }
    }

    // Helper method to forcibly shut down the scheduler
    private void forceShutdown() {
        scheduledExecutorService.shutdownNow();
    }
}