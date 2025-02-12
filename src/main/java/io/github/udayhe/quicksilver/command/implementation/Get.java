package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.NULL;

public class Get<K, V> implements Command<K, V> {

    private static final Logger logger = Logger.getLogger(Get.class.getName());
    private static final String LOG_TEMPLATE = "📤 GET command executed: {0} -> {1}";

    private final DB<K, V> db;

    public Get(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public String execute(K key, V unused) {
        V value = db.get(key);
        logger.log(Level.INFO, LOG_TEMPLATE, new Object[]{key, value});
        return getResult(value);
    }

    private String getResult(V value) {
        return value != null ? value.toString() : NULL;
    }
}