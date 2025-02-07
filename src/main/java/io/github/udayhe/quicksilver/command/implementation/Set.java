package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.db.QuickSilverDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.udayhe.quicksilver.constant.Constants.OK;


public class Set implements Command {

    private static final Logger log = LoggerFactory.getLogger(Set.class);
    private final QuickSilverDB db;

    public Set(QuickSilverDB db) {
        this.db = db;
    }

    @Override
    public String execute(String[] args) {
        if (args.length == 2) {
            db.set(args[0], args[1], 0);
            log.info("✅ SET command executed: {} -> {}", args[0], args[1]);
            return OK;
        }
        return "ERROR: Invalid SET command";
    }
}
