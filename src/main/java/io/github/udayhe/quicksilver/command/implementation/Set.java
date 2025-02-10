package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.logging.LogManager;

import static io.github.udayhe.quicksilver.constant.Constants.OK;


public class Set<K, V> implements Command<K, V> {

    private static final LogManager log = LogManager.getInstance();
    private final DB<K, V> db;

    public Set(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public String execute(K key, V value) {
        db.set(key, value, 0);
        log.info("✅ SET command executed: " + key + " -> " + value);
        return OK;
    }
}

