package io.github.udayhe.quicksilver.performance;

import io.github.udayhe.quicksilver.db.implementation.InMemoryDB;
import io.github.udayhe.quicksilver.db.implementation.ShardedDB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Performance test suite for Quicksilver database operations
 */
@ExtendWith(MockitoExtension.class)
public class PerformanceTestSuite {

    private static final int WARMUP_ITERATIONS = 1000;
    private static final int TEST_ITERATIONS = 10000;
    private static final int NUM_THREADS = 10;
    private static final int KEYS_SPACE = 1000;
    
    private InMemoryDB<String, String> inMemoryDB;
    private ShardedDB<String, String> shardedDB;
    private Random random;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        inMemoryDB = new InMemoryDB<>(10000);
        shardedDB = new ShardedDB<>(4, 2500);
        random = new Random();
        executorService = Executors.newFixedThreadPool(NUM_THREADS);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    @Disabled("Performance test - run manually")
    void testInMemoryDBSetPerformance() {
        System.out.println("Testing InMemoryDB SET performance...");
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            inMemoryDB.set("warmup_key_" + i, "warmup_value_" + i, 0);
        }
        
        // Test
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            inMemoryDB.set("perf_key_" + i, "perf_value_" + i, 0);
        }
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        double opsPerSecond = TEST_ITERATIONS / (durationMs / 1000.0);
        
        System.out.printf("InMemoryDB SET: %.2f ms, %.2f ops/sec%n", durationMs, opsPerSecond);
        assertTrue(opsPerSecond > 10000, "SET operations should be faster than 10,000 ops/sec");
    }

    @Test
    @Disabled("Performance test - run manually")
    void testInMemoryDBGetPerformance() {
        System.out.println("Testing InMemoryDB GET performance...");
        
        // Pre-populate database
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            inMemoryDB.set("perf_key_" + i, "perf_value_" + i, 0);
        }
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            inMemoryDB.get("perf_key_" + i);
        }
        
        // Test
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            inMemoryDB.get("perf_key_" + i);
        }
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        double opsPerSecond = TEST_ITERATIONS / (durationMs / 1000.0);
        
        System.out.printf("InMemoryDB GET: %.2f ms, %.2f ops/sec%n", durationMs, opsPerSecond);
        assertTrue(opsPerSecond > 50000, "GET operations should be faster than 50,000 ops/sec");
    }

    @Test
    @Disabled("Performance test - run manually")
    void testShardedDBSetPerformance() {
        System.out.println("Testing ShardedDB SET performance...");
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            shardedDB.set("warmup_key_" + i, "warmup_value_" + i, 0);
        }
        
        // Test
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            shardedDB.set("perf_key_" + i, "perf_value_" + i, 0);
        }
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        double opsPerSecond = TEST_ITERATIONS / (durationMs / 1000.0);
        
        System.out.printf("ShardedDB SET: %.2f ms, %.2f ops/sec%n", durationMs, opsPerSecond);
        assertTrue(opsPerSecond > 8000, "Sharded SET operations should be faster than 8,000 ops/sec");
    }

    @Test
    @Disabled("Performance test - run manually")
    void testShardedDBGetPerformance() {
        System.out.println("Testing ShardedDB GET performance...");
        
        // Pre-populate database
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            shardedDB.set("perf_key_" + i, "perf_value_" + i, 0);
        }
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            shardedDB.get("perf_key_" + i);
        }
        
        // Test
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            shardedDB.get("perf_key_" + i);
        }
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        double opsPerSecond = TEST_ITERATIONS / (durationMs / 1000.0);
        
        System.out.printf("ShardedDB GET: %.2f ms, %.2f ops/sec%n", durationMs, opsPerSecond);
        assertTrue(opsPerSecond > 40000, "Sharded GET operations should be faster than 40,000 ops/sec");
    }

    @Test
    @Disabled("Performance test - run manually")
    void testConcurrentSetPerformance() throws InterruptedException {
        System.out.println("Testing concurrent SET performance...");
        
        AtomicLong totalOperations = new AtomicLong(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_THREADS);
        
        Runnable worker = () -> {
            try {
                startLatch.await();
                long operations = 0;
                for (int i = 0; i < TEST_ITERATIONS / NUM_THREADS; i++) {
                    inMemoryDB.set("concurrent_key_" + Thread.currentThread().getId() + "_" + i, 
                                 "concurrent_value_" + i, 0);
                    operations++;
                }
                totalOperations.addAndGet(operations);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        };
        
        // Start all threads
        for (int i = 0; i < NUM_THREADS; i++) {
            executorService.submit(worker);
        }
        
        long startTime = System.nanoTime();
        startLatch.countDown();
        doneLatch.await();
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        double opsPerSecond = totalOperations.get() / (durationMs / 1000.0);
        
        System.out.printf("Concurrent SET (%d threads): %.2f ms, %.2f ops/sec%n", 
                         NUM_THREADS, durationMs, opsPerSecond);
        assertTrue(opsPerSecond > 5000, "Concurrent SET should be faster than 5,000 ops/sec");
    }

    @Test
    @Disabled("Performance test - run manually")
    void testConcurrentGetPerformance() throws InterruptedException {
        System.out.println("Testing concurrent GET performance...");
        
        // Pre-populate database
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            inMemoryDB.set("concurrent_test_key_" + i, "concurrent_test_value_" + i, 0);
        }
        
        AtomicLong totalOperations = new AtomicLong(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_THREADS);
        
        Runnable worker = () -> {
            try {
                startLatch.await();
                long operations = 0;
                for (int i = 0; i < TEST_ITERATIONS / NUM_THREADS; i++) {
                    inMemoryDB.get("concurrent_test_key_" + (i % TEST_ITERATIONS));
                    operations++;
                }
                totalOperations.addAndGet(operations);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        };
        
        // Start all threads
        for (int i = 0; i < NUM_THREADS; i++) {
            executorService.submit(worker);
        }
        
        long startTime = System.nanoTime();
        startLatch.countDown();
        doneLatch.await();
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        double opsPerSecond = totalOperations.get() / (durationMs / 1000.0);
        
        System.out.printf("Concurrent GET (%d threads): %.2f ms, %.2f ops/sec%n", 
                         NUM_THREADS, durationMs, opsPerSecond);
        assertTrue(opsPerSecond > 20000, "Concurrent GET should be faster than 20,000 ops/sec");
    }

    @Test
    @Disabled("Performance test - run manually")
    void testMixedWorkloadPerformance() throws InterruptedException {
        System.out.println("Testing mixed workload performance...");
        
        AtomicLong totalOperations = new AtomicLong(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_THREADS);
        
        Runnable worker = () -> {
            try {
                startLatch.await();
                long operations = 0;
                for (int i = 0; i < TEST_ITERATIONS / NUM_THREADS; i++) {
                    String key = "mixed_key_" + random.nextInt(KEYS_SPACE);
                    String value = "mixed_value_" + i;
                    
                    // 70% GET, 20% SET, 10% DELETE
                    double operation = random.nextDouble();
                    if (operation < 0.7) {
                        inMemoryDB.get(key);
                    } else if (operation < 0.9) {
                        inMemoryDB.set(key, value, 0);
                    } else {
                        inMemoryDB.delete(key);
                    }
                    operations++;
                }
                totalOperations.addAndGet(operations);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        };
        
        // Start all threads
        for (int i = 0; i < NUM_THREADS; i++) {
            executorService.submit(worker);
        }
        
        long startTime = System.nanoTime();
        startLatch.countDown();
        doneLatch.await();
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        double opsPerSecond = totalOperations.get() / (durationMs / 1000.0);
        
        System.out.printf("Mixed workload (%d threads): %.2f ms, %.2f ops/sec%n", 
                         NUM_THREADS, durationMs, opsPerSecond);
        assertTrue(opsPerSecond > 3000, "Mixed workload should be faster than 3,000 ops/sec");
    }

    @Test
    @Disabled("Performance test - run manually")
    void testMemoryUsage() {
        System.out.println("Testing memory usage...");
        
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Request garbage collection
        
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Add a large number of entries
        int numEntries = 50000;
        for (int i = 0; i < numEntries; i++) {
            inMemoryDB.set("memory_test_key_" + i, "memory_test_value_" + i, 0);
        }
        
        runtime.gc();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        double memoryPerEntry = (double) memoryUsed / numEntries;
        
        System.out.printf("Memory usage: %d bytes total, %.2f bytes per entry%n", 
                         memoryUsed, memoryPerEntry);
        
        // Each entry should use less than 200 bytes (rough estimate)
        assertTrue(memoryPerEntry < 200, "Memory usage per entry should be less than 200 bytes");
    }

    @Test
    @Disabled("Performance test - run manually")
    void testShardingDistribution() {
        System.out.println("Testing sharding distribution...");
        
        int numEntries = 10000;
        int[] shardCounts = new int[4];
        
        // Add entries and count distribution
        for (int i = 0; i < numEntries; i++) {
            String key = "distribution_test_key_" + i;
            shardedDB.set(key, "distribution_test_value_" + i, 0);
            
            // Determine which shard this key went to
            int shardIndex = Math.abs(key.hashCode()) % 4;
            shardCounts[shardIndex]++;
        }
        
        System.out.println("Shard distribution:");
        for (int i = 0; i < 4; i++) {
            double percentage = (double) shardCounts[i] / numEntries * 100;
            System.out.printf("Shard %d: %d entries (%.2f%%)%n", i, shardCounts[i], percentage);
        }
        
        // Check that distribution is reasonably even (within 20%)
        double expectedPerShard = numEntries / 4.0;
        for (int count : shardCounts) {
            double deviation = Math.abs(count - expectedPerShard) / expectedPerShard;
            assertTrue(deviation < 0.2, "Shard distribution should be within 20% of expected");
        }
    }
}
