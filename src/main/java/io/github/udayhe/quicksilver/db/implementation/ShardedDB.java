package io.github.udayhe.quicksilver.db.implementation;

import io.github.udayhe.quicksilver.db.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.ToIntFunction;

public class ShardedDB<K, V> implements DB<K, V>, Serializable {

    private static final Logger log = LoggerFactory.getLogger(ShardedDB.class);
    private final List<InMemoryDB<K, V>> shards;
    private transient ToIntFunction<K> hashFunction;

    public ShardedDB(int numShards, int maxSizePerShard) {
        this(numShards, maxSizePerShard, Object::hashCode);
    }

    public ShardedDB(int numShards, int maxSizePerShard, ToIntFunction<K> hashFunction) {
        this.hashFunction = hashFunction;
        this.shards = new CopyOnWriteArrayList<>();
        for (int i = 0; i < numShards; i++) {
            shards.add(new InMemoryDB<>(maxSizePerShard));
        }
        log.info("🔄 ShardedDB initialized with {} shards", numShards);
    }

    private int getShardIndex(K key) {
        return Math.abs(hashFunction.applyAsInt(key)) % shards.size();
    }

    @Override
    public void set(K key, V value, long ttlMillis) {
        int shardIndex = getShardIndex(key);
        shards.get(shardIndex).set(key, value, ttlMillis);
        log.debug("✅ Key {} stored in shard {}", key, shardIndex);
    }

    @Override
    public V get(K key) {
        int shardIndex = getShardIndex(key);
        Map<K, V> shard = shards.get(shardIndex).getAll();

        if (shard.containsKey(key)) {
            log.info("📤 GET command: Found key {} in shard {}", key, shardIndex);
            return shard.get(key);
        } else {
            log.warn("⚠️ GET command: Key {} not found in shard {}", key, shardIndex);
            return null;
        }
    }


    @Override
    public void delete(K key) {
        int shardIndex = getShardIndex(key);
        shards.get(shardIndex).delete(key);
        log.debug("🗑️ Key {} removed from shard {}", key, shardIndex);
    }

    @Override
    public void clear() {
        shards.forEach(InMemoryDB::clear);
        log.info("🔥 All shards cleared");
    }

    @Override
    public void saveToDisk(String baseFilename) {
        for (int i = 0; i < shards.size(); i++) {
            shards.get(i).saveToDisk(baseFilename + "_shard" + i + ".db");
        }
        log.info("💾 All shards saved to disk");
    }

    @Override
    public void loadFromDisk(String baseFilename) {
        for (int i = 0; i < shards.size(); i++) {
            shards.get(i).loadFromDisk(baseFilename + "_shard" + i + ".db");
        }
        log.info("🔄 All shards loaded from disk");
    }

    @Override
    public Map<K, V> getAll() {
        Map<K, V> all = new HashMap<>();
        for(InMemoryDB<K, V> inMemoryDB: this.shards)
            all.putAll(inMemoryDB.getAll());
        return all;
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject(); // Serialize non-transient fields
        log.info("💾 Serializing ShardedDB...");
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // Deserialize non-transient fields
        log.info("🔄 Deserializing ShardedDB...");

        // Reinitialize transient fields
        hashFunction = Object::hashCode; // Default hash function
    }

}
