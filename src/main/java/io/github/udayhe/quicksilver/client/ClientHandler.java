package io.github.udayhe.quicksilver.client;

import io.github.udayhe.quicksilver.cluster.ClusterClient;
import io.github.udayhe.quicksilver.cluster.ClusterNode;
import io.github.udayhe.quicksilver.cluster.ClusterService;
import io.github.udayhe.quicksilver.command.CommandRegistry;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;
import io.github.udayhe.quicksilver.resp.RespEncoder;
import io.github.udayhe.quicksilver.resp.RespParser;
import io.github.udayhe.quicksilver.resp.value.BulkString;
import io.github.udayhe.quicksilver.resp.value.RespArray;
import io.github.udayhe.quicksilver.resp.value.RespError;
import io.github.udayhe.quicksilver.resp.value.RespValue;
import io.github.udayhe.quicksilver.resp.value.SimpleString;
import io.github.udayhe.quicksilver.security.ConnectionLimits;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.enums.Command.DUMP;
import static io.github.udayhe.quicksilver.enums.Command.EXIT;
import static io.github.udayhe.quicksilver.enums.Command.FLUSH;
import static io.github.udayhe.quicksilver.util.ClusterUtil.isLocalNode;

/**
 * Handles a single client connection for its full lifetime.
 *
 * Protocol flow:
 *  1. Parse one {@link RespValue} per iteration (RESP array or plain-text inline).
 *  2. If the key belongs to another cluster node, forward the raw args via
 *     {@link ClusterClient} and relay the response.
 *  3. Otherwise dispatch to {@link CommandRegistry} and encode the reply.
 *
 * Security measures:
 *  - {@link ConnectionLimits#SOCKET_TIMEOUT_MS} idle read timeout.
 *  - Key length capped at {@link ConnectionLimits#MAX_KEY_BYTES}.
 *  - Bulk-string and array size limits enforced inside {@link RespParser}.
 *  - All I/O is buffered to minimise system-call overhead.
 */
public class ClientHandler<K, V> implements Runnable {

    private static final Logger log = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final OutputStream out;
    private final RespParser parser;
    private final CommandRegistry<K, V> commandRegistry;
    private final ClusterService<K> clusterService;

    public ClientHandler(Socket socket,
                         DB<K, V> db,
                         ClusterService<K> clusterService,
                         PubSubManager pubSubManager) throws IOException {
        this.socket = socket;
        socket.setSoTimeout(ConnectionLimits.SOCKET_TIMEOUT_MS);
        this.out    = new BufferedOutputStream(socket.getOutputStream());
        this.parser = new RespParser(new BufferedInputStream(socket.getInputStream()));
        this.clusterService  = clusterService;
        this.commandRegistry = new CommandRegistry<>(
                db, clusterService.getClusterManager(), pubSubManager, out);
    }

    @Override
    public void run() {
        log.log(Level.INFO, "Client connected: {0}", socket.getRemoteSocketAddress());
        try {
            RespValue request;
            while ((request = parser.parse()) != null) {
                if (!processRequest(request)) break;
            }
        } catch (SocketTimeoutException e) {
            log.log(Level.INFO, "Client idle timeout: {0}", socket.getRemoteSocketAddress());
            writeErrorQuietly("ERR connection timed out");
        } catch (IOException e) {
            if (!socket.isClosed()) {
                log.log(Level.WARNING, "Client I/O error [{0}]: {1}",
                        new Object[]{socket.getRemoteSocketAddress(), e.getMessage()});
            }
        } finally {
            closeQuietly();
        }
        log.log(Level.INFO, "Client disconnected: {0}", socket.getRemoteSocketAddress());
    }

    /**
     * Processes one parsed request.
     *
     * @return {@code false} when the connection should be closed after this response
     */
    private boolean processRequest(RespValue request) throws IOException {
        if (!(request instanceof RespArray array) || array.isNil()) {
            RespEncoder.writeError(out, "ERR invalid request format");
            return true;
        }

        List<RespValue> elements = array.elements();
        if (elements == null || elements.isEmpty()) return true;

        String cmd = extractString(elements, 0);
        if (cmd == null) {
            RespEncoder.writeError(out, "ERR command name is missing or not a string");
            return true;
        }
        cmd = cmd.toUpperCase();

        // EXIT signals connection teardown — reply then let the loop end.
        if (EXIT.name().equals(cmd)) {
            RespEncoder.writeSimpleString(out, "BYE");
            return false;
        }

        // FLUSH / DUMP are store-wide; no key needed and no cluster routing.
        if (FLUSH.name().equals(cmd) || DUMP.name().equals(cmd)) {
            RespEncoder.write(out, commandRegistry.executeCommand(cmd, null, null));
            return true;
        }

        K key   = elements.size() > 1 ? castKey(extractString(elements, 1)) : null;
        V value = elements.size() > 2 ? castValue(extractString(elements, 2)) : null;

        // Security: reject oversized keys before reaching storage.
        if (key instanceof String keyStr && keyStr.length() > ConnectionLimits.MAX_KEY_BYTES) {
            RespEncoder.writeError(out, "ERR key exceeds maximum allowed size");
            return true;
        }

        // Cluster routing: forward to the responsible node when it is not local.
        ClusterNode target = clusterService.getResponsibleNode(key);
        if (target != null && !isLocalNode(target, socket.getLocalPort())) {
            forwardToNode(target, elements);
            return true;
        }

        RespEncoder.write(out, commandRegistry.executeCommand(cmd, key, value));
        return true;
    }

    /** Forwards the raw command args to another cluster node and relays its reply. */
    private void forwardToNode(ClusterNode target, List<RespValue> elements) throws IOException {
        String[] args = elements.stream()
                .map(e -> e instanceof BulkString bs ? bs.asString() : null)
                .toArray(String[]::new);
        log.log(Level.INFO, "Forwarding {0} to cluster node {1}", new Object[]{args[0], target});
        Optional<RespValue> reply = ClusterClient.sendCommandWithResponse(target, args);
        RespEncoder.write(out, reply.orElse(RespError.err("cluster forwarding failed")));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String extractString(List<RespValue> elements, int index) {
        if (index >= elements.size()) return null;
        return switch (elements.get(index)) {
            case BulkString bs   -> bs.asString();
            case SimpleString ss -> ss.value();
            default              -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private K castKey(String s)   { return (K) s; }

    @SuppressWarnings("unchecked")
    private V castValue(String s) { return (V) s; }

    private void writeErrorQuietly(String message) {
        try { RespEncoder.writeError(out, message); } catch (IOException ignored) {}
    }

    private void closeQuietly() {
        try {
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            log.log(Level.WARNING, "Error closing client socket", e);
        }
    }
}
