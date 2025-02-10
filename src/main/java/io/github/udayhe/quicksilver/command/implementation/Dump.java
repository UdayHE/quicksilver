package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.logging.LogManager;

import java.util.Map;
import java.util.stream.Collectors;

import static io.github.udayhe.quicksilver.constant.Constants.NEW_LINE;
import static io.github.udayhe.quicksilver.constant.Constants.SPACE;

public class Dump<K, V> implements Command<K, V> {

    private static final LogManager log = LogManager.getInstance();
    private final DB<K, V> db;

    public Dump(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public String execute(K unusedKey, V unusedValue) {
        log.info("📤 Dumping database data");

        // Serialize all data into a string format
        Map<K, V> data = db.getAll();
        return data.entrySet().stream()
                .map(entry -> entry.getKey() + SPACE + entry.getValue())
                .collect(Collectors.joining(NEW_LINE));
    }
}
