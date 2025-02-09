package io.github.udayhe.quicksilver.db;

import java.util.Map;

public interface DB<K, V> {

    V get(K key);

    void set(K key, V value, long ttlMillis);

    void delete(K key);

    void clear();

    void saveToDisk(String filename);

    void loadFromDisk(String filename);

    Map<K,V> getAll();
}
