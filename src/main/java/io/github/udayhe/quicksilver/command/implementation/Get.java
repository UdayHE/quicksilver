package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;

import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.NULL;

public class Get<K, V> implements Command<K, V> {

    private static final Logger log = Logger.getLogger(Get.class.getName());
    private final DB<K, V> db;

    public Get(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public String execute(K key, V unused) {
        V value = db.get(key);
        log.info("📤 GET command: " + key + " -> " + value);
        return value != null ? value.toString() : NULL;
    }
}

