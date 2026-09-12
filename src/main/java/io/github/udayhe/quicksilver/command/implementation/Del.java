package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.resp.value.RespError;
import io.github.udayhe.quicksilver.resp.value.RespInteger;
import io.github.udayhe.quicksilver.resp.value.RespValue;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Del<K, V> implements Command<K, V> {

    private static final Logger log = Logger.getLogger(Del.class.getName());

    private final DB<K, V> db;

    public Del(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public RespValue execute(K key, V ignoredValue) {
        if (key == null) return RespError.wrongArgs("del");
        long existed = db.get(key) != null ? 1L : 0L;
        db.delete(key);
        log.log(Level.INFO, "DEL {0} (existed={1})", new Object[]{key, existed});
        return new RespInteger(existed);
    }
}
