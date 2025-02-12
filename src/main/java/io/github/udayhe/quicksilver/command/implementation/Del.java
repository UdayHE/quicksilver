package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.OK;

public class Del<K, V> implements Command<K, V> {

    private static final Logger log = Logger.getLogger(Del.class.getName());
    private static final String LOG_TEMPLATE = "🗑️ DEL command executed: Key = {0}";

    private final DB<K, V> db;

    public Del(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public String execute(K key, V ignoredValue) {
        deleteKey(key);
        log.log(Level.INFO, LOG_TEMPLATE, key);
        return OK;
    }

    private void deleteKey(K key) {
        db.delete(key);
    }
}