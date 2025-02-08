package io.github.udayhe.quicksilver.db;

public interface DB<K, V> {

    void set(K key, V value, long ttlMillis);

    V get(K key);

    void delete(K key);

    void clear();
}
