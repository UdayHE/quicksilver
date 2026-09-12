package io.github.udayhe.quicksilver.cluster;

import io.github.udayhe.quicksilver.resp.RespEncoder;
import io.github.udayhe.quicksilver.resp.RespParser;
import io.github.udayhe.quicksilver.resp.value.BulkString;
import io.github.udayhe.quicksilver.resp.value.RespArray;
import io.github.udayhe.quicksilver.resp.value.RespValue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thin client for inter-node cluster communication.
 *
 * All requests are encoded as RESP arrays; all responses are parsed via
 * {@link RespParser}.  A new TCP connection is opened per request (no pooling)
 * which is intentional for simplicity given the current cluster scale.
 *
 * Three call-site variants are provided:
 *  <ul>
 *   <li>{@link #sendCommand}            – fire-and-forget (FLUSH broadcast)</li>
 *   <li>{@link #sendDumpCommand}        – fetch bulk-string body (cluster sync)</li>
 *   <li>{@link #sendCommandWithResponse}– return full {@link RespValue} (routing)</li>
 *  </ul>
 */
public class ClusterClient {

    private static final Logger log = Logger.getLogger(ClusterClient.class.getName());

    private ClusterClient() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Sends a command to {@code node} and discards the response. */
    public static void sendCommand(ClusterNode node, String... args) {
        try {
            execute(node, args);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Fire-and-forget command failed for node {0}: {1}",
                    new Object[]{node, e.getMessage()});
        }
    }

    /**
     * Sends DUMP to {@code node} and returns the serialized key-value body.
     * Returns an empty string on failure so callers can skip gracefully.
     */
    public static String sendDumpCommand(ClusterNode node) {
        try {
            RespValue response = execute(node, "DUMP");
            if (response instanceof BulkString bs && !bs.isNil()) {
                return bs.asString();
            }
            log.log(Level.WARNING, "Unexpected DUMP response type from node {0}: {1}",
                    new Object[]{node, response});
            return "";
        } catch (IOException e) {
            log.log(Level.SEVERE, "DUMP failed for node {0}: {1}",
                    new Object[]{node, e.getMessage()});
            return "";
        }
    }

    /**
     * Sends a command and returns the {@link RespValue} reply wrapped in an
     * {@link Optional}, or {@link Optional#empty()} on I/O failure.
     * Used by {@code ClientHandler} when routing a request to another node.
     */
    public static Optional<RespValue> sendCommandWithResponse(ClusterNode node, String... args) {
        try {
            return Optional.of(execute(node, args));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Command failed for node {0}: {1}",
                    new Object[]{node, e.getMessage()});
            return Optional.empty();
        }
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private static RespValue execute(ClusterNode node, String... args) throws IOException {
        try (Socket socket = new Socket(node.host(), node.port())) {
            OutputStream out = new BufferedOutputStream(socket.getOutputStream());
            InputStream  in  = new BufferedInputStream(socket.getInputStream());

            RespEncoder.write(out, buildCommand(args));

            RespParser parser = new RespParser(in);
            RespValue  reply  = parser.parse();
            if (reply == null) {
                throw new IOException("Node " + node + " closed connection without a response");
            }
            log.log(Level.INFO, "Response from node {0} for command {1}: {2}",
                    new Object[]{node, args[0], reply});
            return reply;
        }
    }

    private static RespArray buildCommand(String... args) {
        List<RespValue> elements = new ArrayList<>(args.length);
        for (String arg : args) {
            elements.add(BulkString.of(arg));
        }
        return new RespArray(elements);
    }
}
