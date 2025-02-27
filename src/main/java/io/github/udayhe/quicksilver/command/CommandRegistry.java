package io.github.udayhe.quicksilver.command;

import io.github.udayhe.quicksilver.command.implementation.*;
import io.github.udayhe.quicksilver.cluster.ClusterManager;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static io.github.udayhe.quicksilver.enums.Command.*;

public class CommandRegistry<K, V> {
    private final Map<String, Command<K, V>> commands = new HashMap<>();

    public CommandRegistry(DB<K, V> db, ClusterManager clusterManager, PubSubManager pubSubManager, Socket socket) {
        commands.put(SET.name(), new Set<>(db));
        commands.put(GET.name(), new Get<>(db));
        commands.put(DEL.name(), new Del<>(db));
        commands.put(FLUSH.name(), new Flush<>(db, clusterManager));
        commands.put(DUMP.name(), new Dump<>(db));
        commands.put(EXIT.name(), new Exit<>(socket));

        commands.put(SUBSCRIBE.name(), (Command<K, V>) new Subscribe(pubSubManager));
        commands.put(UNSUBSCRIBE.name(), (Command<K, V>) new Unsubscribe(pubSubManager));
        commands.put(PUBLISH.name(), (Command<K, V>) new Publish(pubSubManager));
    }

    public String executeCommand(String command, K key, V value) {
        Command<K, V> cmd = commands.get(command.toUpperCase());
        return cmd != null ? cmd.execute(key, value) : "ERROR: Unknown command";
    }
}
