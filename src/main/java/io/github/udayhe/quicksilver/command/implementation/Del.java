package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.OK;


public class Del<K, V> implements Command<K, V> {

    private static final Logger log = Logger.getLogger(Del.class.getName());
    private final DB<K, V> db;

    public Del(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public String execute(K key, V unused) {
        db.delete(key);
        log.log(Level.INFO, "🗑️ DEL command executed: {0}", key);
        return OK;
    }
}