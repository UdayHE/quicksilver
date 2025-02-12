package io.github.udayhe.quicksilver.client;

import io.github.udayhe.quicksilver.cluster.ClusterService;
import io.github.udayhe.quicksilver.command.CommandRegistry;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.BYE;
import static io.github.udayhe.quicksilver.constant.Constants.LOGO;

public class ClientHandler<K, V> implements Runnable {

    private static final Logger log = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final DB<K, V> db;
    private final BufferedReader inputReader;
    private final PrintWriter outputWriter;
    private final CommandRegistry<K, V> commandRegistry;

    public ClientHandler(Socket socket, DB<K, V> db, ClusterService<K> clusterService, PubSubManager pubSubManager) throws IOException {
        this.socket = socket;
        this.db = db;
        this.inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.outputWriter = new PrintWriter(socket.getOutputStream(), true);
        this.commandRegistry = new CommandRegistry<>(db, clusterService.getClusterManager(), pubSubManager, socket);
    }

    @Override
    public void run() {
        log.log(Level.INFO, "📡 New client connected: {0}", socket.getRemoteSocketAddress());
        outputWriter.println(LOGO);

        try {
            String commandLine;
            while (!socket.isClosed() && (commandLine = inputReader.readLine()) != null) {
                try {
                    String response = handleCommand(commandLine.trim());
                    if (response != null) {
                        outputWriter.println(response);
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error processing command: {0}", commandLine);
                    outputWriter.println("ERROR: Could not process command.");
                }
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                log.log(Level.SEVERE, "❌ Communication error with client: {0}", socket.getRemoteSocketAddress());
            } else {
                log.log(Level.INFO, "🔌 Client disconnected: {0}", socket.getRemoteSocketAddress());
            }
        } finally {
            closeClientConnection();
        }
    }

    /**
     * Processes a single command from the client.
     */
    private String handleCommand(String commandLine) {
        log.log(Level.INFO, "📩 Received command: {0}", commandLine);
        String[] parts = commandLine.split(" ");

        if (parts.length == 0 || parts[0].isEmpty()) {
            return "ERROR: Invalid command.";
        }

        String command = parts[0].toUpperCase();

        if ("EXIT".equals(command)) {
            return handleExit();
        }

        // Add command handling logic as needed
        return commandRegistry.executeCommand(command, null, null);
    }

    /**
     * Handles the EXIT command and closes the socket.
     */
    private String handleExit() {
        log.log(Level.INFO, "🔌 Client requested disconnect: {0}", socket.getRemoteSocketAddress());
        outputWriter.println(BYE);
        closeClientConnection();
        return null;
    }

    /**
     * Closes the client connection and releases resources.
     */
    private void closeClientConnection() {
        try {
            if (!socket.isClosed()) {
                inputReader.close();
                outputWriter.close();
                socket.close();
                log.log(Level.INFO, "✅ Client connection closed: {0}", socket.getRemoteSocketAddress());
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while closing client connection: {0}", e.getMessage());
        }
    }
}