package io.github.udayhe.quicksilver;

import io.github.udayhe.quicksilver.client.ClientHandler;
import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.db.DatabaseFactory;
import io.github.udayhe.quicksilver.db.enums.DBType;
import io.github.udayhe.quicksilver.db.implementation.InMemoryDB;
import io.github.udayhe.quicksilver.db.implementation.ShardedDB;
import io.github.udayhe.quicksilver.threadpool.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static io.github.udayhe.quicksilver.constant.Constants.BACKUP_DB;
import static io.github.udayhe.quicksilver.constant.Constants.ENV_QUICKSILVER_PORT;
import static io.github.udayhe.quicksilver.util.LogoUtil.printLogo;
import static java.lang.System.getenv;

public class Server<K, V> {

    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private final DB<K, V> db;
    private final int port;
    private final ExecutorService clientThreadPool;

    public Server(int port, DB<K, V> db) {
        this.port = port;
        this.db = db;
        this.clientThreadPool = ThreadPoolManager.getInstance().getScheduler(); // Use shared thread pool
        addShutdownHook();
    }

    public static void main(String[] args) {
        int port = getPort(args);
        port = getPortFromEnvironmentVariable(port);
        port = allowOverrideFromArgs(args, port);

        DBType dbType = DBType.valueOf(new Config().getDBType().toUpperCase());
        DB<String, String> db = DatabaseFactory.createDatabase(dbType);

        new Server<>(port, db).start();
    }

    public void start() {
        printLogo();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("🚀 QuickSilverServer DB started on port {}", port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientThreadPool.execute(new ClientHandler<>(clientSocket, db));
            }
        } catch (IOException e) {
            log.error("❌ Error starting QuickSilverServer on port {}", port, e);
        }
    }

    private static int getPort(String[] args) {
        Config config = new Config();
        int port = config.getPort();
        port = getPortFromEnvironmentVariable(port);
        port = allowOverrideFromArgs(args, port);
        return port;
    }

    /**
     * Override port from command-line arguments
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
