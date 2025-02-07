package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.QuickSilverDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.udayhe.quicksilver.constant.Constants.OK;

public class Del implements Command {

    private static final Logger log = LoggerFactory.getLogger(Del.class);
    private final QuickSilverDB db;

    public Del(QuickSilverDB db) {
        this.db = db;
    }

    @Override
    public String execute(String[] args) {
        if (args.length == 1) {
            db.delete(args[0]);
            log.info("🗑️ DEL command executed: {}", args[0]);
            return OK;
        }
        return "ERROR: Invalid DEL command";
    }
}