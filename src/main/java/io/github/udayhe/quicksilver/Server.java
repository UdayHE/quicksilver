package io.github.udayhe.quicksilver;

import io.github.udayhe.quicksilver.command.CommandRegistry;
import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.db.enums.DBType;
import io.github.udayhe.quicksilver.db.implementation.InMemoryDB;
import io.github.udayhe.quicksilver.threadpool.ThreadPoolManager;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static io.github.udayhe.quicksilver.command.enums.Command.EXIT;
import static io.github.udayhe.quicksilver.command.enums.Command.FLUSH;
import static io.github.udayhe.quicksilver.constant.Constants.*;
import static io.github.udayhe.quicksilver.util.LogoUtil.printLogo;
import static java.lang.System.getenv;
import static org.slf4j.LoggerFactory.getLogger;

public class Server<K, V> {

    private static final Logger log = getLogger(Server.class);
    private final DB<K, V> db;
    private final int port;

    public Server(int port, DB<K, V> db) {
        this.port = port;
        this.db = db;
        addShutdownHook();
    }

    public static void main(String[] args) {
        int port = getPort(args);
        port = getPortFromEnvironmentVariable(port);
        port = allowOverrideFromArgs(args, port);
        DBType dbType = DBType.valueOf(new Config().getDBType().toUpperCase());
        DB db = getDatabase(dbType);
        new Server(port, db).start();
    }

    public void start() {
        printLogo();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("🚀 QuickSilverServer DB started on port {}", port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log.info("📡 New client connected: {}", clientSocket.getRemoteSocketAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            log.error("❌ Error starting QuickSilverServer on port {}", port, e);
        }
    }

    private void handleClient(Socket socket) {
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


    private static int getPort(String[] args) {
        Config config = new Config();
        int port = config.getPort();
        port = getPortFromEnvironmentVariable(port);
        port = allowOverrideFromArgs(args, port);
        return port;
    }


    /**
     * Override port from commandline arguments
     *
     * @param args
     * @param port
     * @return port
     */
    private static int allowOverrideFromArgs(String[] args, int port) {
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                log.error("Invalid command-line port: {}. Using default port {}", args[0], port);
            }
        }
        return port;
    }


    /**
     * Fetch port from QUICKSILVER_PORT environment variable
     *
     * @param port
     * @return port
     */
    private static int getPortFromEnvironmentVariable(int port) {
        String envPort = getenv(ENV_QUICKSILVER_PORT);
        if (envPort != null) {
            try {
                port = Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                log.error("Invalid QUICKSILVER_PORT value: {}. Using default port {}", envPort, port);
            }
        }
        return port;
    }

    /**
     * Adds a shutdown hook to ensure graceful termination of the thread pool.
     */
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("🛑 Server shutting down...");
            ThreadPoolManager.getInstance().shutdown();
            log.info("✅ Thread pool shut down successfully.");
        }));
    }

    private static DB<?, ?> getDatabase(DBType dbType) {
        return switch (dbType) {
            case IN_MEMORY -> new InMemoryDB<>();
        };
    }

}
