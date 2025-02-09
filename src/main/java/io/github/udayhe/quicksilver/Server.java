package io.github.udayhe.quicksilver;

import io.github.udayhe.quicksilver.cluster.ClusterClient;
import io.github.udayhe.quicksilver.cluster.ClusterManager;
import io.github.udayhe.quicksilver.cluster.ClusterNode;
import io.github.udayhe.quicksilver.cluster.ConsistentHashing;
import io.github.udayhe.quicksilver.command.CommandRegistry;
import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.db.DatabaseFactory;
import io.github.udayhe.quicksilver.enums.DBType;
import io.github.udayhe.quicksilver.db.implementation.InMemoryDB;
import io.github.udayhe.quicksilver.db.implementation.ShardedDB;
import io.github.udayhe.quicksilver.threadpool.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static io.github.udayhe.quicksilver.enums.Command.*;
import static io.github.udayhe.quicksilver.constant.Constants.*;
import static io.github.udayhe.quicksilver.util.ClusterUtil.isLocalNode;
import static java.lang.System.getenv;

public class Server<K, V> {

    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private final DB<K, V> db;
    private final int port;
    private final ExecutorService clientThreadPool;
    private final ClusterManager clusterManager = new ClusterManager();
    private final ConsistentHashing consistentHashing = new ConsistentHashing();

    public Server(int port, DB<K, V> db) {
        this.port = port;
        this.db = db;
        this.clientThreadPool = ThreadPoolManager.getInstance().getScheduler();
        addShutdownHook();
    }

    public static void main(String[] args) {
        int port = getPort(args);
        port = getPortFromEnvironmentVariable(port);
        port = allowOverrideFromArgs(args, port);

        DBType dbType = DBType.valueOf(Config.getInstance().getDBType().toUpperCase());
        DB<String, String> db = DatabaseFactory.createDatabase(dbType);

        new Server<>(port, db).start();
    }

    public void start() {
        System.out.println(LOGO);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("🚀 QuickSilverServer DB started on port {}", port);
            registerInCluster();
            syncDataFromCluster();
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientThreadPool.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            log.error("❌ Error starting QuickSilverServer on port {}", port, e);
        }
    }

    private void registerInCluster() {
        ClusterNode self = new ClusterNode(LOCALHOST, port);
        clusterManager.addNode(self);
        consistentHashing.addNode(self);
    }

    private void handleClient(Socket socket) {
        try {
            CommandRegistry<K, V> commandRegistry = new CommandRegistry<>(db, clusterManager, socket);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String line;
                out.println(LOGO);
                while ((line = in.readLine()) != null) {
                    log.debug("📩 Received command: {}", line);
                    String[] parts = line.trim().split(SPACE);
                    if (parts.length == 0 || parts[0].isEmpty()) continue;

                    String cmd = parts[0].toUpperCase();
                    K key = (parts.length > 1) ? (K) parts[1] : null;
                    V value = (parts.length > 2) ? (V) parts[2] : null;

                    if (exit(cmd, out, socket)) return;
                    if (flush(cmd, commandRegistry, out)) continue;
                    if (invalidCommand(cmd, key, out)) continue;

                    ClusterNode targetNode = consistentHashing.getNodeForKey(parts[1]);
                    if (redirectToOtherNode(targetNode, line, out)) continue;

                    // Process command locally
                    String response = commandRegistry.executeCommand(cmd, key, value);
                    out.println(response);
                }
            }
        } catch (IOException e) {
            log.error("❌ Client communication error", e);
        }
    }

    private boolean redirectToOtherNode(ClusterNode targetNode, String line, PrintWriter out) {
        if (!isLocalNode(targetNode, Config.getInstance().getPort())) {
            log.info("🔄 Redirecting request [{}] to node {}", line, targetNode);
            String response = ClusterClient.sendRequest(targetNode, line);
            if (!response.equals("ERROR")) {
                out.println(response);
            } else {
                log.error("❌ Failed to process command [{}] on node {}", line, targetNode);
                out.println("ERROR: Failed to process request");
            }
            return true;
        }
        return false;
    }


    private boolean exit(String command, PrintWriter out, Socket socket) throws IOException {
        if (command.equalsIgnoreCase(EXIT.name())) {
            log.info("🔌 Client disconnected: {}", socket.getRemoteSocketAddress());
            out.println(BYE);
            socket.close();
            return true;
        }
        return false;
    }


    private boolean flush(String command, CommandRegistry<K, V> commandRegistry, PrintWriter out) {
        if (command.equalsIgnoreCase(FLUSH.name())) {
            String response = commandRegistry.executeCommand(command, null, null);
            out.println(response);
            return true;
        }
        return false;
    }


    private boolean invalidCommand(String command, K key, PrintWriter out) {
        if ((command.equalsIgnoreCase(SET.name()) || command.equalsIgnoreCase(DEL.name())) && key == null) {
            out.println("ERROR: Missing key");
            return true;
        }
        if (command.equalsIgnoreCase(GET.name()) && key == null) {
            out.println("ERROR: GET requires a key");
            return true;
        }
        return false;
    }

    private void syncDataFromCluster() {
        for (ClusterNode node : clusterManager.getNodes()) {
            if (!isLocalNode(node, Config.getInstance().getPort())) {
                log.info("🔄 Syncing data from {}", node);
                String response = ClusterClient.sendRequest(node, DUMP.name());
                restoreData(response);
            }
        }
    }

    private void restoreData(String dataDump) {
        String[] entries = dataDump.split("\n");
        for (String entry : entries) {
            String[] kv = entry.split(" ");
            if (kv.length == 2) {
                db.set((K) kv[0], (V) kv[1], 0);
            }
        }
    }

    private static int getPort(String[] args) {
        int port = Config.getInstance().getPort();
        port = getPortFromEnvironmentVariable(port);
        port = allowOverrideFromArgs(args, port);
        return port;
    }

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

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("🛑 Server shutting down...");

            if (db instanceof InMemoryDB<K, V> memoryDB) {
                log.info("💾 Saving DB before shutdown...");
                memoryDB.saveToDisk(BACKUP_DB);
            } else if (db instanceof ShardedDB<K, V> shardedDB) {
                log.info("💾 Saving Sharded DB before shutdown...");
                shardedDB.saveToDisk("sharded_backup");
            }

            ThreadPoolManager.getInstance().shutdown();
            log.info("✅ Thread pool shut down successfully.");
        }));
    }
}
