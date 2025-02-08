package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.udayhe.quicksilver.constant.Constants.OK;


public class Del<K, V> implements Command<K, V> {

    private static final Logger log = LoggerFactory.getLogger(Del.class);
    private final DB<K, V> db;

    public Del(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public String execute(K key, V unused) {
        db.delete(key);
        log.info("🗑️ DEL command executed: {}", key);
        return OK;
    }
}