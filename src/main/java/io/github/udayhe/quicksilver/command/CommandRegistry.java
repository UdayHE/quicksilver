package io.github.udayhe.quicksilver.command;

import io.github.udayhe.quicksilver.command.implementation.Del;
import io.github.udayhe.quicksilver.command.implementation.Flush;
import io.github.udayhe.quicksilver.command.implementation.Get;
import io.github.udayhe.quicksilver.command.implementation.Set;
import io.github.udayhe.quicksilver.command.implementation.Shutdown;
import io.github.udayhe.quicksilver.db.QuickSilverDB;

import java.util.HashMap;
import java.util.Map;

import static io.github.udayhe.quicksilver.constant.Constants.*;

public class CommandRegistry {

    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry(QuickSilverDB db) {
        commands.put(SET, new Set(db));
        commands.put(GET, new Get(db));
        commands.put(DEL, new Del(db));
        commands.put(FLUSH, new Flush(db));
        commands.put(SHUTDOWN, new Shutdown(db));
        commands.put(EXIT, args -> BYE);
    }

    public String executeCommand(String command, String[] args) {
        Command cmd = commands.get(command.toUpperCase());
        if (cmd != null) {
            return cmd.execute(args);
        }
        return "ERROR: Unknown command";
    }
}