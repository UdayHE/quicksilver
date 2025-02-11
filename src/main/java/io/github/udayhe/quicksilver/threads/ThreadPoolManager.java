package io.github.udayhe.quicksilver.threads;

import io.github.udayhe.quicksilver.config.Config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ThreadPoolManager {

    private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
    private final ScheduledExecutorService scheduler;

    private ThreadPoolManager() {
        this.scheduler = Executors.newScheduledThreadPool(Config.getInstance().getThreadPoolSize());
    }

    public static ThreadPoolManager getInstance() {
        return INSTANCE;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, SECONDS))
                scheduler.shutdownNow();
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
