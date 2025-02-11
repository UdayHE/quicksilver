package io.github.udayhe.quicksilver.client;

import io.github.udayhe.quicksilver.cluster.ClusterClient;
import io.github.udayhe.quicksilver.cluster.ClusterNode;
import io.github.udayhe.quicksilver.cluster.ClusterService;
import io.github.udayhe.quicksilver.command.CommandRegistry;
import io.github.udayhe.quicksilver.command.implementation.Publish;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.*;
import static io.github.udayhe.quicksilver.enums.Command.*;
import static io.github.udayhe.quicksilver.util.ClusterUtil.isLocalNode;

public class ClientHandler<K, V> implements Runnable {

    private static final Logger log = Logger.getLogger(ClientHandler.class.getName());
    private final Socket socket;
    private final DB<K, V> db;
    private final BufferedReader in;
    private final PrintWriter out;
    private final CommandRegistry<K, V> commandRegistry;

    public ClientHandler(Socket socket, DB<K, V> db, ClusterService clusterService, PubSubManager pubSubManager) throws IOException {
        this.socket = socket;
        this.db = db;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.commandRegistry = new CommandRegistry<>(db, clusterService.getClusterManager(), pubSubManager, socket);
    }

    @Override
    public void run() {
        log.log(Level.INFO,"📡 New client connected: {0}", socket.getRemoteSocketAddress());
        try {
            String line;
            out.println(LOGO);
            while ((line = in.readLine()) != null) {
                log.log(Level.WARNING,"📩 Received command: {0}", line);
                String[] parts = line.trim().split(SPACE);
                if (parts.length == 0) continue;

                String cmd = parts[0].toUpperCase();
                K key = (parts.length > 1) ? (K) parts[1] : null;
                V value = (parts.length > 2) ? (V) parts[2] : null;

                // ✅ Executes all commands, including Pub/Sub
                String response = commandRegistry.executeCommand(cmd, key, value);
                sendResponse(response);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "❌ Client communication error", e);
        }
    }


    /**
     * Reads a command from the client
     */
    public String readCommand() throws IOException {
        return in.readLine();
    }

    /**
     * Sends a response back to the client
     */
    public void sendResponse(String response) {
        out.println(response);
    }

    /**
     * 🛠️ Handles commands that do not require key-value pairs (like FLUSH & DUMP)
     */
    private boolean handleSpecialCommands(String command) {
        if (command.equalsIgnoreCase(FLUSH.name()) || command.equalsIgnoreCase(DUMP.name())) {
            String response = commandRegistry.executeCommand(command, null, null);
            sendResponse(response);
            return true;
        }
        return false;
    }

    /**
     * 🔌 Handles the EXIT command
     */
    private boolean exit(String command) throws IOException {
        if (command.equalsIgnoreCase(EXIT.name())) {
            log.log(Level.INFO, "🔌 Client disconnected: {0}", socket.getRemoteSocketAddress());
            sendResponse(BYE);
            socket.close();
            return true;
        }
        return false;
    }

    /**
     * 🔄 Redirects request to the correct cluster node (if necessary)
     */
    private boolean redirectToOtherNode(ClusterNode targetNode, String line) {
        if (!isLocalNode(targetNode, socket.getLocalPort())) {
            log.log(Level.INFO, "🔄 Redirecting request [{0}] to node {1}", new Object[]{line, targetNode});
            String response = ClusterClient.sendRequest(targetNode, line);

            if (!response.equals(ERROR)) {
                sendResponse(response);
            } else {
                log.log(Level.SEVERE, "❌ Failed to process command [{0}] on node {1}", new Object[]{line, targetNode});
                sendResponse("ERROR: Failed to process request");
            }
            return true;
        }
        return false;
    }
}