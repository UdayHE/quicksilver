package io.github.udayhe.quicksilver.command;

import io.github.udayhe.quicksilver.cluster.ClusterManager;
import io.github.udayhe.quicksilver.command.implementation.*;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;
import io.github.udayhe.quicksilver.resp.value.RespError;
import io.github.udayhe.quicksilver.resp.value.RespValue;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static io.github.udayhe.quicksilver.enums.Command.*;

/**
 * Maps command names to their {@link Command} implementations.
 *
 * {@code clientOut} is the subscriber's output stream, injected into the
 * pub/sub commands so they can register/deregister with {@link PubSubManager}
 * without coupling the command layer to socket I/O.
 *
 * EXIT is registered here so it can be looked up normally, but the actual
 * connection teardown is performed by {@code ClientHandler} after it receives
 * the {@code SimpleString("BYE")} response — keeping I/O out of this layer.
 */
public class CommandRegistry<K, V> {

    private final Map<String, Command<K, V>> commands = new HashMap<>();

    public CommandRegistry(DB<K, V> db,
                           ClusterManager clusterManager,
                           PubSubManager pubSubManager,
                           OutputStream clientOut) {
        commands.put(SET.name(),         new Set<>(db));
        commands.put(GET.name(),         new Get<>(db));
        commands.put(DEL.name(),         new Del<>(db));
        commands.put(FLUSH.name(),       new Flush<>(db, clusterManager));
        commands.put(DUMP.name(),        new Dump<>(db));
        commands.put(EXIT.name(),        new Exit<>());

        @SuppressWarnings("unchecked")
        Command<K, V> subscribe   = (Command<K, V>) new Subscribe(pubSubManager, clientOut);
        @SuppressWarnings("unchecked")
        Command<K, V> unsubscribe = (Command<K, V>) new Unsubscribe(pubSubManager, clientOut);
        @SuppressWarnings("unchecked")
        Command<K, V> publish     = (Command<K, V>) new Publish(pubSubManager);

        commands.put(SUBSCRIBE.name(),   subscribe);
        commands.put(UNSUBSCRIBE.name(), unsubscribe);
        commands.put(PUBLISH.name(),     publish);
    }

    public RespValue executeCommand(String command, K key, V value) {
        if (command == null) return RespError.err("command must not be null");
        Command<K, V> cmd = commands.get(command.toUpperCase());
        return cmd != null ? cmd.execute(key, value)
                           : new RespError("ERR unknown command '" + command + "'");
    }
}
