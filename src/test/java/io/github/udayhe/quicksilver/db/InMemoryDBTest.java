package io.github.udayhe.quicksilver.db;

import io.github.udayhe.quicksilver.db.implementation.InMemoryDB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for InMemoryDB implementation
 */
@ExtendWith(MockitoExtension.class)
public class InMemoryDBTest {

    private InMemoryDB<String, String> db;
    private static final int MAX_SIZE = 3;

    @BeforeEach
    void setUp() {
        db = new InMemoryDB<>(MAX_SIZE);
    }

    @Test
    void testSetAndGet() {
        db.set("key1", "value1", 0);
        assertEquals("value1", db.get("key1"));
    }

    @Test
    void testGetNonExistentKey() {
        assertNull(db.get("nonexistent"));
    }

    @Test
    void testDelete() {
        db.set("key1", "value1", 0);
        db.delete("key1");
        assertNull(db.get("key1"));
    }

    @Test
    void testLRUEviction() {
        // Fill the cache to capacity
        db.set("key1", "value1", 0);
        db.set("key2", "value2", 0);
        db.set("key3", "value3", 0);
        
        // Access key1 to make it most recently used
        db.get("key1");
        
        // Add a new key, should evict key2 (least recently used)
        db.set("key4", "value4", 0);
        
        assertNotNull(db.get("key1")); // Should still exist
        assertNull(db.get("key2"));   // Should be evicted
        assertNotNull(db.get("key3")); // Should still exist
        assertNotNull(db.get("key4")); // Should exist
    }

    @Test
    void testTTLExpiration() throws InterruptedException {
        db.set("key1", "value1", 100); // 100ms TTL
        
        // Should exist immediately
        assertEquals("value1", db.get("key1"));
        
        // Wait for expiration
        Thread.sleep(150);
        
        // Should be expired
        assertNull(db.get("key1"));
    }

    @Test
    void testTTLWithZeroValue() {
        db.set("key1", "value1", 0); // No expiration
        assertEquals("value1", db.get("key1"));
    }

    @Test
    void testClear() {
        db.set("key1", "value1", 0);
        db.set("key2", "value2", 0);
        
        db.clear();
        
        assertNull(db.get("key1"));
        assertNull(db.get("key2"));
    }

    @Test
    void testEvictionListener() {
        AtomicInteger evictedCount = new AtomicInteger(0);
        db.setEvictionListener((key, value) -> evictedCount.incrementAndGet());
        
        // Fill cache
        db.set("key1", "value1", 0);
        db.set("key2", "value2", 0);
        db.set("key3", "value3", 0);
        
        // Trigger eviction
        db.set("key4", "value4", 0);
        
        assertEquals(1, evictedCount.get());
    }

    @Test
    void testPersistence() throws IOException {
        String filename = "test_backup.db";
        
        // Add data
        db.set("key1", "value1", 0);
        db.set("key2", "value2", 0);
        
        // Save to disk
        db.saveToDisk(filename);
        
        // Create new DB and load
        InMemoryDB<String, String> newDb = new InMemoryDB<>(MAX_SIZE);
        newDb.loadFromDisk(filename);
        
        assertEquals("value1", newDb.get("key1"));
        assertEquals("value2", newDb.get("key2"));
        
        // Cleanup
        new File(filename).delete();
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
                        db.set(key, value, 0);
                        db.get(key);
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
        db.restoreData(dataDump);
        
        assertEquals("value1", db.get("key1"));
        assertEquals("value2", db.get("key2"));
        assertEquals("value3", db.get("key3"));
    }

    @Test
    void testRestoreDataWithInvalidFormat() {
        String dataDump = "invalid\nkey1 value1\nkey2\nkey3 value3";
        db.restoreData(dataDump);
        
        assertEquals("value1", db.get("key1"));
        assertNull(db.get("key2"));
        assertEquals("value3", db.get("key3"));
    }

    @Test
    void testGetAll() {
        db.set("key1", "value1", 0);
        db.set("key2", "value2", 0);
        
        Map<String, String> all = db.getAll();
        assertEquals(2, all.size());
        assertTrue(all.containsKey("key1"));
        assertTrue(all.containsKey("key2"));
    }
}
