package io.github.udayhe.quicksilver.db;

import io.github.udayhe.quicksilver.threadpool.ThreadPoolManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;

public class DB<K, V> {

    private final ConcurrentHashMap<K, V> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<K, Long> expirationMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService expirationService = ThreadPoolManager.getInstance().getScheduler();

    public DB() {
        startExpirationTask();
    }

    public void set(K key, V value, long ttlMillis) {
        store.put(key, value);
        if (ttlMillis > 0) {
            expirationMap.put(key, currentTimeMillis() + ttlMillis);
        }
    }

    public V get(K key) {
        if (isExpired(key)) {
            store.remove(key);
            expirationMap.remove(key);
            return null;
        }
        return store.get(key);
    }

    public void delete(K key) {
        store.remove(key);
        expirationMap.remove(key);
    }


    private boolean isExpired(K key) {
        Long expiry = expirationMap.get(key);
        return expiry != null && currentTimeMillis() > expiry;
    }

    private void startExpirationTask() {
        expirationService.scheduleAtFixedRate(() -> {
            long now = currentTimeMillis();
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