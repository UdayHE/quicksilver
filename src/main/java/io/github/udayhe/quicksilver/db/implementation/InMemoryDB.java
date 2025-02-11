package io.github.udayhe.quicksilver.db.implementation;

import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.threads.ThreadPoolManager;

import java.io.*;
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

    private final int maxSize;
    private final Map<K, V> store;
    private final ConcurrentHashMap<K, Long> expirationMap = new ConcurrentHashMap<>();

    private transient ScheduledExecutorService expirationService = ThreadPoolManager.getInstance().getScheduler();
    private transient BiConsumer<K, V> evictionListener = (key, _) -> {};

    public InMemoryDB(int maxSize) {
        this.maxSize = maxSize;
        this.store = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                boolean shouldRemove = size() > InMemoryDB.this.maxSize; // Enforce max size
                if (shouldRemove)
                    evictionListener.accept(eldest.getKey(), eldest.getValue());
                return shouldRemove;
            }
        };
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

    // ✅ Get a value by key
    @Override
    public V get(K key) {
        if (isExpired(key)) {
            removeKey(key);
            return null;
        }
        return store.get(key);
    }

    // ✅ Delete a key
    @Override
    public void delete(K key) {
        removeKey(key);
    }

    // ✅ Clear all entries
    @Override
    public void clear() {
        store.clear();
        expirationMap.clear();
    }

    // ✅ Save data to disk (Persistence)
    @Override
    public void saveToDisk(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(store);
            log.log(Level.INFO, "💾 Database saved to {0}", filename);
        } catch (IOException e) {
            log.log(Level.SEVERE, "❌ Error saving database.", e);
        }
    }

    // ✅ Load data from disk
    @Override
    public void loadFromDisk(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            Map<K, V> loadedStore = (Map<K, V>) in.readObject();
            store.putAll(loadedStore);
            log.log(Level.INFO, "🔄 Database loaded from {0}", filename);
        } catch (IOException | ClassNotFoundException e) {
            log.severe("❌ Error loading database."+ e);
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

    private boolean isExpired(K key) {
        Long expiry = expirationMap.get(key);
        return expiry != null && System.currentTimeMillis() > expiry;
    }

    private void removeKey(K key) {
        V value = store.remove(key);
        expirationMap.remove(key);
        evictionListener.accept(key, value);
    }

    private void startExpirationTask() {
        expirationService.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            expirationMap.forEach((key, expiry) -> {
                if (expiry < now) {
                    removeKey(key);
                }
            });
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject(); // Serialize default fields (store, expirationMap, maxSize)
        log.log(Level.INFO, "💾 Serializing InMemoryDB...");
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // Deserialize default fields
        log.log(Level.INFO, "🔄 Deserializing InMemoryDB...");

        // Reinitialize non-serializable fields
        expirationService = ThreadPoolManager.getInstance().getScheduler();
        evictionListener = (key, _) -> {
        }; // Reset eviction listener
        startExpirationTask(); // Restart background expiration task
    }

}
