package io.github.udayhe.quicksilver.client;

import io.github.udayhe.quicksilver.command.CommandRegistry;
import io.github.udayhe.quicksilver.db.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

import static io.github.udayhe.quicksilver.command.enums.Command.EXIT;
import static io.github.udayhe.quicksilver.command.enums.Command.FLUSH;
import static io.github.udayhe.quicksilver.constant.Constants.*;

public class ClientHandler<K, V> implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final DB<K, V> db;

    public ClientHandler(Socket socket, DB<K, V> db) {
        this.socket = socket;
        this.db = db;
    }

    @Override
    public void run() {
        log.info("📡 New client connected: {}", socket.getRemoteSocketAddress());
        CommandRegistry<K, V> commandRegistry = new CommandRegistry<>(db, socket);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                log.debug("📩 Received command: {}", line);
                String[] parts = line.split(SPACE);
                String command = parts[0].toUpperCase();

                if (exit(socket, command, out)) return;
                if (flush(command, commandRegistry, out)) continue;
                if (invalidCommand(parts, out)) continue;

                K key = (K) parts[1];
                V value = (parts.length > 2) ? (V) parts[2] : null;
                String response = commandRegistry.executeCommand(command, key, value);
                out.println(response);
            }
        } catch (IOException e) {
            log.error("❌ Client communication error", e);
        } catch (ClassCastException e) {
            log.error("❌ Type conversion error: Ensure correct key-value types", e);
        }
    }

    private static boolean invalidCommand(String[] parts, PrintWriter out) {
        if (parts.length < 2) {
            out.println("ERROR: Invalid command format");
            return true;
        }
        return false;
    }

    private static <K, V> boolean flush(String command, CommandRegistry<K, V> commandRegistry, PrintWriter out) {
        if (command.equalsIgnoreCase(FLUSH.name())) {
            String response = commandRegistry.executeCommand(command, null, null);
            out.println(response);
            return true;
        }
        return false;
    }

    private static boolean exit(Socket socket, String command, PrintWriter out) throws IOException {
        if (command.equalsIgnoreCase(EXIT.name())) {
            log.info("🔌 Client disconnected: {}", socket.getRemoteSocketAddress());
            out.println(BYE);
            socket.close();
            return true;
        }
        return false;
    }
}
