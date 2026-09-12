package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.resp.value.BulkString;
import io.github.udayhe.quicksilver.resp.value.RespValue;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.github.udayhe.quicksilver.constant.Constants.NEW_LINE;
import static io.github.udayhe.quicksilver.constant.Constants.SPACE;

public class Dump<K, V> implements Command<K, V> {

    private static final Logger log = Logger.getLogger(Dump.class.getName());

    private final DB<K, V> db;

    public Dump(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public RespValue execute(K ignoredKey, V ignoredValue) {
        log.log(Level.INFO, "Dumping database");
        Map<K, V> data = db.getAll();
        return BulkString.of(serialize(data));
    }

    private String serialize(Map<K, V> data) {
        return data.entrySet().stream()
                .map(e -> e.getKey() + SPACE + e.getValue())
                .collect(Collectors.joining(NEW_LINE));
    }
}
