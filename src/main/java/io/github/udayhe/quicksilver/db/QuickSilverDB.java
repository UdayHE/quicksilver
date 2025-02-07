package io.github.udayhe.quicksilver.db;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.udayhe.quicksilver.constant.Constants.CORE_POOL_SIZE;

public class QuickSilverDB {

    private final ConcurrentHashMap<String, Object> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> expirationMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService expirationService = Executors.newScheduledThreadPool(CORE_POOL_SIZE);

    public QuickSilverDB() {
        startExpirationTask();
    }

    public void set(String key, Object value, long ttlMillis) {
        store.put(key, value);
        if (ttlMillis > 0) {
            expirationMap.put(key, System.currentTimeMillis() + ttlMillis);
        }
    }

    public Object get(String key) {
        if (isExpired(key)) {
            store.remove(key);
            expirationMap.remove(key);
            return null;
        }
        return store.get(key);
    }

    public void delete(String key) {
        store.remove(key);
        expirationMap.remove(key);
    }

    public void shutdown() {
        expirationService.shutdown();
    }

    private boolean isExpired(String key) {
        Long expiry = expirationMap.get(key);
        return expiry != null && System.currentTimeMillis() > expiry;
    }

    private void startExpirationTask() {
        expirationService.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            expirationMap.forEach((key, expiry) -> {
                if (expiry < now) {
                    store.remove(key);
                    expirationMap.remove(key);
                }
            });
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void clear() {
        store.clear();
        expirationMap.clear();
    }
}
