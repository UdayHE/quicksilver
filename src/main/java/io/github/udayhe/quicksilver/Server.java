package io.github.udayhe.quicksilver;

import io.github.udayhe.quicksilver.client.ClientHandler;
import io.github.udayhe.quicksilver.cluster.ClusterService;
import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.db.DatabaseFactory;
import io.github.udayhe.quicksilver.db.implementation.InMemoryDB;
import io.github.udayhe.quicksilver.db.implementation.ShardedDB;
import io.github.udayhe.quicksilver.enums.DBType;
import io.github.udayhe.quicksilver.logging.LogManager;
import io.github.udayhe.quicksilver.threads.ThreadPoolManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static io.github.udayhe.quicksilver.constant.Constants.*;
import static io.github.udayhe.quicksilver.util.Util.getPort;

public class Server<K, V> {

    private static final LogManager log = LogManager.getInstance();
    private final DB<K, V> db;
    private final int port;
    private final ExecutorService clientThreadPool;
    private final ClusterService clusterService;

    public Server(int port, DB<K, V> db) {
        this.port = port;
        this.db = db;
        this.clusterService = new ClusterService();
        this.clientThreadPool = ThreadPoolManager.getInstance().getScheduler();
        addShutdownHook();
    }

    public static void main(String[] args) {
        int port = getPort(args);
        DBType dbType = DBType.valueOf(Config.getInstance().getDBType().toUpperCase());
        DB<String, Object> db = DatabaseFactory.createDatabase(dbType);
        new Server<>(port, db).start();
    }

    public void start() {
       log.info(LOGO);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("🚀 QuickSilverServer DB started on port "+ port);
            clusterService.registerInCluster(this.port);
            clusterService.syncDataFromCluster(this.db);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log.info("📡 New client connected: "+ clientSocket.getRemoteSocketAddress());
                clientThreadPool.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            log.error("❌ Error starting QuickSilverServer on port "+ port +" exception:"+ e);
        }
    }

    private void handleClient(Socket socket) {
        try {
            ClientHandler<K, V> clientHandler = new ClientHandler<>(socket, db, clusterService);
            clientThreadPool.execute(clientHandler);
        } catch (IOException e) {
            log.error("❌ Failed to start ClientHandler for client " + socket.getRemoteSocketAddress() + " exception:" + e);
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("🛑 Server shutting down...");

            if (db instanceof InMemoryDB<K, V> memoryDB) {
                log.info("💾 Saving DB before shutdown...");
                memoryDB.saveToDisk(BACKUP_DB);
            } else if (db instanceof ShardedDB<K, V> shardedDB) {
                log.info("💾 Saving Sharded DB before shutdown...");
                shardedDB.saveToDisk(SHARDED_BACKUP);
            }
            ThreadPoolManager.getInstance().shutdown();
            log.info("✅ Thread pool shut down successfully.");
        }));
    }
}
