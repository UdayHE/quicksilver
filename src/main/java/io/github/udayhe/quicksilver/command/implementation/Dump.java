package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.github.udayhe.quicksilver.constant.Constants.NEW_LINE;
import static io.github.udayhe.quicksilver.constant.Constants.SPACE;

public class Dump<K, V> implements Command<K, V> {

    private static final Logger log = Logger.getLogger(Dump.class.getName());
    private static final String LOG_TEMPLATE = "📤 Dumping database data";

    private final DB<K, V> db;

    public Dump(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public String execute(K _key, V _value) {
        log.log(Level.INFO, LOG_TEMPLATE);
        final Map<K, V> data = db.getAll();
        return serializeData(data);
    }

    private String serializeData(Map<K, V> data) {
        return data.entrySet().stream()
                .map(entry -> entry.getKey() + SPACE + entry.getValue())
                .collect(Collectors.joining(NEW_LINE));
    }
}