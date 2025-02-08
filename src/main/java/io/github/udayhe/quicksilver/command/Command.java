package io.github.udayhe.quicksilver.command;

public interface Command<K, V> {

    String execute(K key, V value);
}
