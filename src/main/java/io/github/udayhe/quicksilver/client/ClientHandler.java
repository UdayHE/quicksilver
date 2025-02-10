package io.github.udayhe.quicksilver.client;

import io.github.udayhe.quicksilver.cluster.*;
import io.github.udayhe.quicksilver.command.CommandRegistry;
import io.github.udayhe.quicksilver.db.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static io.github.udayhe.quicksilver.enums.Command.*;
import static io.github.udayhe.quicksilver.constant.Constants.*;
import static io.github.udayhe.quicksilver.util.ClusterUtil.isLocalNode;

public class ClientHandler<K, V> implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket socket;
    private final DB<K, V> db;
    private final BufferedReader in;
    private final PrintWriter out;
    private final ClusterService clusterService;

    public ClientHandler(Socket socket,
                         DB<K, V> db,
                         ClusterService clusterService) throws IOException {
        this.socket = socket;
        this.db = db;
        this.clusterService = clusterService;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        log.info("📡 New client connected: {}", socket.getRemoteSocketAddress());
        CommandRegistry<K, V> commandRegistry = new CommandRegistry<>(db, clusterService.getClusterManager(), socket);

        try {
            this.out.println(LOGO);
            String line;
            while ((line = readCommand()) != null) {
                log.debug("📩 Received command: {}", line);
                String[] parts = line.trim().split(SPACE);
                if (parts.length == 0 || parts[0].isEmpty()) continue;

                String cmd = parts[0].toUpperCase();
                K key = (parts.length > 1) ? (K) parts[1] : null;
                V value = (parts.length > 2) ? (V) parts[2] : null;

                if (exit(cmd)) return;
                if (commandWithoutKeyValue(cmd, FLUSH.name(),commandRegistry)) continue;
                if (commandWithoutKeyValue(cmd, DUMP.name(), commandRegistry)) continue;
                if (invalidCommand(cmd, key)) continue;

                ClusterNode targetNode = this.clusterService.getConsistentHashing().getNodeForKey(parts[1]);
                if (redirectToOtherNode(targetNode, line)) continue;

                // Process command locally
                String response = commandRegistry.executeCommand(cmd, key, value);
                sendResponse(response);
            }
        } catch (IOException e) {
            log.error("❌ Client communication error", e);
        }
    }


    /**
     * Reads a command from the client
     */
    public String readCommand() throws IOException {
        return this.in.readLine();
    }

    /**
     * Sends a response back to the client
     */
    public void sendResponse(String response) {
        this.out.println(response);
    }

    private boolean invalidCommand(String cmd, K key) {
        if ((cmd.equals(SET.name()) || cmd.equals(DEL.name())) && key == null) {
            sendResponse("ERROR: Missing key");
            return true;
        }
        if (cmd.equals(GET.name()) && key == null) {
            sendResponse("ERROR: GET requires a key");
            return true;
        }
        return false;
    }

    private boolean commandWithoutKeyValue(String command, String commandValue, CommandRegistry<K, V> commandRegistry) {
        if (command.equalsIgnoreCase(commandValue)) {
            String response = commandRegistry.executeCommand(command, null, null);
            sendResponse(response);
            return true;
        }
        return false;
    }

    private boolean exit(String command) throws IOException {
        if (command.equalsIgnoreCase(EXIT.name())) {
            log.info("🔌 Client disconnected: {}", socket.getRemoteSocketAddress());
            sendResponse(BYE);
            this.socket.close();
            return true;
        }
        return false;
    }

    private boolean redirectToOtherNode(ClusterNode targetNode, String line) {
        if (!isLocalNode(targetNode, this.socket.getLocalPort())) {
            log.info("🔄 Redirecting request [{}] to node {}", line, targetNode);
            String response = ClusterClient.sendRequest(targetNode, line);
            if (!response.equals(ERROR)) {
                sendResponse(response);
            } else {
                log.error("❌ Failed to process command [{}] on node {}", line, targetNode);
                sendResponse("ERROR: Failed to process request");
            }
            return true;
        }
        return false;
    }
}