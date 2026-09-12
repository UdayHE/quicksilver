package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.resp.value.BulkString;
import io.github.udayhe.quicksilver.resp.value.RespError;
import io.github.udayhe.quicksilver.resp.value.RespValue;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Get<K, V> implements Command<K, V> {

    private static final Logger log = Logger.getLogger(Get.class.getName());

    private final DB<K, V> db;

    public Get(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public RespValue execute(K key, V unused) {
        if (key == null) return RespError.wrongArgs("get");
        V value = db.get(key);
        log.log(Level.INFO, "GET {0} -> {1}", new Object[]{key, value});
        return value != null ? BulkString.of(value.toString()) : BulkString.NIL;
    }
}
