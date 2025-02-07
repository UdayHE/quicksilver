package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.QuickSilverDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.udayhe.quicksilver.constant.Constants.NULL;

public class Get implements Command {

    private static final Logger log = LoggerFactory.getLogger(Get.class);
    private final QuickSilverDB db;

    public Get(QuickSilverDB db) {
        this.db = db;
    }

    @Override
    public String execute(String[] args) {
        if (args.length == 1) {
            Object value = db.get(args[0]);
            log.info("📤 GET command: {} -> {}", args[0], value);
            return value != null ? value.toString() : NULL;
        }
        return "ERROR: Invalid GET command";
    }
}

