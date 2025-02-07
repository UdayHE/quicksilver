package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.QuickSilverDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.udayhe.quicksilver.constant.Constants.OK;

public class Flush implements Command {

    private static final Logger log = LoggerFactory.getLogger(Flush.class);
    private final QuickSilverDB db;

    public Flush(QuickSilverDB db) {
        this.db = db;
    }

    @Override
    public String execute(String[] args) {
        db.clear();
        log.info("🔥 Database flushed");
        return OK;
    }
}
