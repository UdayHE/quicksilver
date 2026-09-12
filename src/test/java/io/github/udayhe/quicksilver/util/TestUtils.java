package io.github.udayhe.quicksilver.util;

import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.db.implementation.InMemoryDB;
import io.github.udayhe.quicksilver.db.implementation.ShardedDB;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for testing Quicksilver components
 */
public class TestUtils {

    /**
     * Creates a test InMemoryDB with specified capacity
     */
    public static <K, V> InMemoryDB<K, V> createTestInMemoryDB(int capacity) {
        return new InMemoryDB<>(capacity);
    }

    /**
     * Creates a test ShardedDB with specified parameters
     */
    public static <K, V> ShardedDB<K, V> createTestShardedDB(int numShards, int shardSize) {
        return new ShardedDB<>(numShards, shardSize);
    }

    /**
     * Populates a database with test data
     */
    public static void populateTestData(DB<String, String> db, int numEntries) {
        for (int i = 0; i < numEntries; i++) {
            db.set("test_key_" + i, "test_value_" + i, 0);
        }
    }

    /**
     * Populates a database with test data including TTL
     */
    public static void populateTestDataWithTTL(DB<String, String> db, int numEntries, long ttlMillis) {
        for (int i = 0; i < numEntries; i++) {
            db.set("test_key_" + i, "test_value_" + i, ttlMillis);
        }
    }

    /**
     * Creates a map of test data
     */
    public static Map<String, String> createTestDataMap(int numEntries) {
        Map<String, String> testData = new ConcurrentHashMap<>();
        for (int i = 0; i < numEntries; i++) {
            testData.put("test_key_" + i, "test_value_" + i);
        }
        return testData;
    }

    /**
     * Verifies that all expected keys exist in the database with correct values
     */
    public static <K, V> boolean verifyData(DB<K, V> db, Map<K, V> expectedData) {
        for (Map.Entry<K, V> entry : expectedData.entrySet()) {
            V actualValue = db.get(entry.getKey());
            if (!entry.getValue().equals(actualValue)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Measures execution time of a runnable operation
     */
    public static long measureExecutionTime(Runnable operation) {
        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000; // Return milliseconds
    }

    /**
     * Measures execution time of a callable operation
     */
    public static <T> MeasurementResult<T> measureExecutionTime(Callable<T> operation) {
        long startTime = System.nanoTime();
        try {
            T result = operation.call();
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;
            return new MeasurementResult<>(result, durationMs);
        } catch (Exception e) {
            throw new RuntimeException("Error during measurement", e);
        }
    }

    /**
     * Generates random test data
     */
    public static Map<String, String> generateRandomTestData(int numEntries, int keyLength, int valueLength) {
        Map<String, String> testData = new ConcurrentHashMap<>();
        StringBuilder keyBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();
        
        for (int i = 0; i < numEntries; i++) {
            keyBuilder.setLength(0);
            valueBuilder.setLength(0);
            
            // Generate random key
            for (int j = 0; j < keyLength; j++) {
                keyBuilder.append((char) ('a' + (int) (Math.random() * 26)));
            }
            
            // Generate random value
            for (int j = 0; j < valueLength; j++) {
                valueBuilder.append((char) ('a' + (int) (Math.random() * 26)));
            }
            
            testData.put(keyBuilder.toString(), valueBuilder.toString());
        }
        
        return testData;
    }

    /**
     * Result class for measurement operations
     */
    public static class MeasurementResult<T> {
        private final T result;
        private final long durationMs;

        public MeasurementResult(T result, long durationMs) {
            this.result = result;
            this.durationMs = durationMs;
        }

        public T getResult() {
            return result;
        }

        public long getDurationMs() {
            return durationMs;
        }

        @Override
        public String toString() {
            return String.format("Result: %s, Duration: %d ms", result, durationMs);
        }
    }

    /**
     * Functional interface for callable operations
     */
    @FunctionalInterface
    public interface Callable<T> {
        T call() throws Exception;
    }
}
