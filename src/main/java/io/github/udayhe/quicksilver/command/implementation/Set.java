package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.OK;

public class Set<K, V> implements Command<K, V> {

    private static final Logger log = Logger.getLogger(Set.class.getName());
    private static final String LOG_TEMPLATE = "✅ SET command executed: {0} -> {1}";
    private static final long DEFAULT_TTL = 0L; // Default TTL value

    private final DB<K, V> db;

    public Set(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public String execute(K key, V value) {
        setKeyValue(key, value);
        log.log(Level.INFO, LOG_TEMPLATE, new Object[]{key, value});
        return OK;
    }

    private void setKeyValue(K key, V value) {
        db.set(key, value, DEFAULT_TTL);
    }
}