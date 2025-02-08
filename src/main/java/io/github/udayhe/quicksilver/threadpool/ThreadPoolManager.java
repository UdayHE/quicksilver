package io.github.udayhe.quicksilver.threadpool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.udayhe.quicksilver.constant.Constants.CORE_POOL_SIZE;

public class ThreadPoolManager {

    private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
    private final ScheduledExecutorService scheduler;

    private ThreadPoolManager() {
        this.scheduler = Executors.newScheduledThreadPool(CORE_POOL_SIZE);
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
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS))
                scheduler.shutdownNow();
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
