package io.github.udayhe.quicksilver.db.implementation;

import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.threads.ThreadPoolManager;

import java.io.*;
import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.NEW_LINE;
import static io.github.udayhe.quicksilver.constant.Constants.SPACE;

public class InMemoryDB<K, V> implements DB<K, V>, Serializable {

    private static final Logger log = Logger.getLogger(InMemoryDB.class.getName());

    // Constants for expiration task schedule
    private static final long EXPIRATION_INITIAL_DELAY = 1;
    private static final long EXPIRATION_INTERVAL = 1;

    private final int maxSize;
    private final Map<K, V> store;
    private final ConcurrentHashMap<K, Long> expirationMap = new ConcurrentHashMap<>();

    private transient ScheduledExecutorService expirationService =
            ThreadPoolManager.getInstance().getScheduledExecutorService();
    private transient BiConsumer<K, V> evictionListener = (key, _) -> {};

    public InMemoryDB(int maxSize) {
        this.maxSize = maxSize;
        this.store = createEvictionEnabledStore(); // Extracted method
        startExpirationTask();
    }

    public void setEvictionListener(BiConsumer<K, V> listener) {
        this.evictionListener = listener;
    }

    @Override
    public void set(K key, V value, long ttlMillis) {
        store.put(key, value);
        if (ttlMillis > 0) {
            expirationMap.put(key, System.currentTimeMillis() + ttlMillis);
        }
    }

    @Override
    public V get(K key) {
        if (isExpired(key)) {
            evictKey(key); // Renamed method
            return null;
        }
        return store.get(key);
    }

    @Override
    public void delete(K key) {
        evictKey(key); // Renamed method
    }

    @Override
    public void clear() {
        store.clear();
        expirationMap.clear();
    }

    @Override
    public void saveToDisk(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(store);
            log.log(Level.INFO, "Database saved to {0}", filename);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error saving database.", e);
        }
    }

    @Override
    public void loadFromDisk(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            store.putAll((Map<K, V>) in.readObject()); // Inlined unnecessary variable
            log.log(Level.INFO, "Database loaded from {0}", filename);
        } catch (IOException | ClassNotFoundException e) {
            log.log(Level.SEVERE, "Error loading database.", e);
        }
    }

    @Override
    public Map<K, V> getAll() {
        return this.store;
    }

    @Override
    public void restoreData(String dataDump) {
        String[] entries = dataDump.split(NEW_LINE);
        for (String entry : entries) {
            String[] kv = entry.split(SPACE);
            if (kv.length == 2) {
                set((K) kv[0], (V) kv[1], 0);
            }
        }
    }

    // Extracted method to create store with eviction enabled via LinkedHashMap
    private Map<K, V> createEvictionEnabledStore() {
        return new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                boolean shouldRemove = size() > InMemoryDB.this.maxSize;
                if (shouldRemove)
                    evictionListener.accept(eldest.getKey(), eldest.getValue());
                return shouldRemove;
            }
        };
    }

    // Logic renamed for better readability
    private void evictKey(K key) {
        V value = store.remove(key);
        expirationMap.remove(key);
        evictionListener.accept(key, value);
    }

    private boolean isExpired(K key) {
        Long expiry = expirationMap.get(key);
        return expiry != null && System.currentTimeMillis() > expiry;
    }

    private void startExpirationTask() {
        expirationService.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            expirationMap.forEach((key, expiry) -> {
                if (expiry < now) {
                    evictKey(key);
                }
            });
        }, EXPIRATION_INITIAL_DELAY, EXPIRATION_INTERVAL, TimeUnit.SECONDS); // Extracted constants
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        log.log(Level.INFO, "Serializing InMemoryDB...");
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        log.log(Level.INFO, "Deserializing InMemoryDB...");
        expirationService = ThreadPoolManager.getInstance().getScheduledExecutorService();
        evictionListener = (key, _) -> {};
        startExpirationTask();
    }
}