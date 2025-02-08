package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.DB;
import org.slf4j.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.OK;
import static org.slf4j.LoggerFactory.getLogger;

public class Flush<K, V> implements Command<K, V> {

    private static final Logger log = getLogger(Flush.class);
    private final DB<K, V> db;

    public Flush(DB<K, V> db) {
        this.db = db;
    }

    @Override
    public String execute(K unusedKey, V unusedValue) {
        db.clear();
        log.info("🔥 Database flushed");
        return OK;
    }
}
