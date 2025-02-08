package io.github.udayhe.quicksilver.command;

import io.github.udayhe.quicksilver.command.implementation.*;
import io.github.udayhe.quicksilver.db.DB;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static io.github.udayhe.quicksilver.command.enums.Command.*;

public class CommandRegistry<K, V> {
    private final Map<String, Command<K, V>> commands = new HashMap<>();

    public CommandRegistry(DB<K, V> db, Socket socket) {
        commands.put(SET.name(), new Set<>(db));
        commands.put(GET.name(), new Get<>(db));
        commands.put(DEL.name(), new Del<>(db));
        commands.put(FLUSH.name(), new Flush<>(db));
        commands.put(EXIT.name(), new Exit<>(socket));
    }

    public String executeCommand(String command, K key, V value) {
        Command<K, V> cmd = commands.get(command.toUpperCase());
        return cmd != null ? cmd.execute(key, value) : "ERROR: Unknown command";
    }
}
