package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.OK;


public class Set<K, V> implements Command<K, V> {

    private static final Logger log = Logger.getLogger(Set.class.getName());
    private final DB<K, V> db;

    public Set(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public String execute(K key, V value) {
        db.set(key, value, 0);
        log.log(Level.INFO, "✅ SET command executed: {0} -> {1}", new Object[]{key, value});
        return OK;
    }
}

