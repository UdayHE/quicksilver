package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.resp.value.RespError;
import io.github.udayhe.quicksilver.resp.value.RespValue;
import io.github.udayhe.quicksilver.resp.value.SimpleString;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.DEFAULT_TTL;

public class Set<K, V> implements Command<K, V> {

    private static final Logger log = Logger.getLogger(Set.class.getName());

    private final DB<K, V> db;

    public Set(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public RespValue execute(K key, V value) {
        if (key == null) return RespError.wrongArgs("set");
        db.set(key, value, DEFAULT_TTL);
        log.log(Level.INFO, "SET {0} -> {1}", new Object[]{key, value});
        return new SimpleString("OK");
    }
}
