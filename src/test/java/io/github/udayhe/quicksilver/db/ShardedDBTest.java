package io.github.udayhe.quicksilver.db;

import io.github.udayhe.quicksilver.db.implementation.ShardedDB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ShardedDB implementation
 */
@ExtendWith(MockitoExtension.class)
public class ShardedDBTest {

    private ShardedDB<String, String> shardedDB;
    private static final int NUM_SHARDS = 4;
    private static final int SHARD_SIZE = 3;

    @BeforeEach
    void setUp() {
        shardedDB = new ShardedDB<>(NUM_SHARDS, SHARD_SIZE);
    }

    @Test
    void testSetAndGet() {
        shardedDB.set("key1", "value1", 0);
        assertEquals("value1", shardedDB.get("key1"));
    }

    @Test
    void testGetNonExistentKey() {
        assertNull(shardedDB.get("nonexistent"));
    }

    @Test
    void testDelete() {
        shardedDB.set("key1", "value1", 0);
        shardedDB.delete("key1");
        assertNull(shardedDB.get("key1"));
    }

    @Test
    void testShardingDistribution() {
        // Add keys that should distribute across different shards
        shardedDB.set("key1", "value1", 0);
        shardedDB.set("key2", "value2", 0);
        shardedDB.set("key3", "value3", 0);
        shardedDB.set("key4", "value4", 0);
        
        // Verify all keys are accessible
        assertEquals("value1", shardedDB.get("key1"));
        assertEquals("value2", shardedDB.get("key2"));
        assertEquals("value3", shardedDB.get("key3"));
        assertEquals("value4", shardedDB.get("key4"));
    }

    @Test
    void testLRUEvictionPerShard() {
        // Fill each shard to capacity
        for (int i = 0; i < NUM_SHARDS * SHARD_SIZE; i++) {
            shardedDB.set("key" + i, "value" + i, 0);
        }
        
        // Access some keys to make them recently used
        shardedDB.get("key0");
        shardedDB.get("key1");
        
        // Add more keys to trigger eviction
        for (int i = NUM_SHARDS * SHARD_SIZE; i < NUM_SHARDS * SHARD_SIZE + 5; i++) {
            shardedDB.set("key" + i, "value" + i, 0);
        }
        
        // Keys 0 and 1 should still exist (recently used)
        assertNotNull(shardedDB.get("key0"));
        assertNotNull(shardedDB.get("key1"));
    }

    @Test
    void testTTLExpiration() throws InterruptedException {
        shardedDB.set("key1", "value1", 100); // 100ms TTL
        
        // Should exist immediately
        assertEquals("value1", shardedDB.get("key1"));
        
        // Wait for expiration
        Thread.sleep(150);
        
        // Should be expired
        assertNull(shardedDB.get("key1"));
    }

    @Test
    void testClear() {
        shardedDB.set("key1", "value1", 0);
        shardedDB.set("key2", "value2", 0);
        
        shardedDB.clear();
        
        assertNull(shardedDB.get("key1"));
        assertNull(shardedDB.get("key2"));
    }

    @Test
    void testPersistence() throws IOException {
        String baseFilename = "test_sharded_backup";
        
        // Add data
        shardedDB.set("key1", "value1", 0);
        shardedDB.set("key2", "value2", 0);
        
        // Save to disk
        shardedDB.saveToDisk(baseFilename);
        
        // Create new DB and load
        ShardedDB<String, String> newDb = new ShardedDB<>(NUM_SHARDS, SHARD_SIZE);
        newDb.loadFromDisk(baseFilename);
        
        assertEquals("value1", newDb.get("key1"));
        assertEquals("value2", newDb.get("key2"));
        
        // Cleanup
        for (int i = 0; i < NUM_SHARDS; i++) {
            new File(baseFilename + "_shard" + i + ".db").delete();
        }
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        int numThreads = 10;
        int operationsPerThread = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        
        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "key" + j;
                        String value = "value" + j;
                        shardedDB.set(key, value, 0);
                        shardedDB.get(key);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }
        
        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
    }

    @Test
    void testRestoreData() {
        String dataDump = "key1 value1\nkey2 value2\nkey3 value3";
        shardedDB.restoreData(dataDump);
        
        assertEquals("value1", shardedDB.get("key1"));
        assertEquals("value2", shardedDB.get("key2"));
        assertEquals("value3", shardedDB.get("key3"));
    }

    @Test
    void testRestoreDataWithInvalidFormat() {
        String dataDump = "invalid\nkey1 value1\nkey2\nkey3 value3";
        shardedDB.restoreData(dataDump);
        
        assertEquals("value1", shardedDB.get("key1"));
        assertNull(shardedDB.get("key2"));
        assertEquals("value3", shardedDB.get("key3"));
    }

    @Test
    void testGetAll() {
        shardedDB.set("key1", "value1", 0);
        shardedDB.set("key2", "value2", 0);
        
        Map<String, String> all = shardedDB.getAll();
        assertEquals(2, all.size());
        assertTrue(all.containsKey("key1"));
        assertTrue(all.containsKey("key2"));
    }

    @Test
    void testCustomHashFunction() {
        // Test with a custom hash function that maps all keys to the same shard
        ShardedDB<String, String> customHashDB = new ShardedDB<>(
            NUM_SHARDS, 
            SHARD_SIZE, 
            key -> 0  // Always return 0, mapping all keys to shard 0
        );
        
        customHashDB.set("key1", "value1", 0);
        customHashDB.set("key2", "value2", 0);
        customHashDB.set("key3", "value3", 0);
        
        // Should trigger LRU eviction in shard 0
        customHashDB.set("key4", "value4", 0);
        
        // key1 should be evicted (least recently used in shard 0)
        assertNull(customHashDB.get("key1"));
        assertNotNull(customHashDB.get("key2"));
        assertNotNull(customHashDB.get("key3"));
        assertNotNull(customHashDB.get("key4"));
    }

    @Test
    void testMultipleShardsWithSameKey() {
        // Test that the same key always maps to the same shard
        String key = "testKey";
        String value = "testValue";
        
        shardedDB.set(key, value, 0);
        String retrievedValue = shardedDB.get(key);
        
        assertEquals(value, retrievedValue);
        
        // Delete and re-add
        shardedDB.delete(key);
        shardedDB.set(key, value + "_new", 0);
        
        assertEquals(value + "_new", shardedDB.get(key));
    }
}
