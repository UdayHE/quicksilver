package io.github.udayhe.quicksilver;

import io.github.udayhe.quicksilver.client.ClientHandler;
import io.github.udayhe.quicksilver.cluster.ClusterService;
import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.db.DatabaseFactory;
import io.github.udayhe.quicksilver.db.implementation.InMemoryDB;
import io.github.udayhe.quicksilver.db.implementation.ShardedDB;
import io.github.udayhe.quicksilver.enums.DBType;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;
import io.github.udayhe.quicksilver.threads.ThreadPoolManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.*;
import static io.github.udayhe.quicksilver.util.Util.getPort;

public class Server<K, V> {

    private static final Logger log = Logger.getLogger(Server.class.getName());
    private final DB<K, V> db;
    private final int port;
    private final ExecutorService clientThreadPool;
    private final ClusterService<K> clusterService;
    private final PubSubManager pubSubManager;

    public Server(int port, DB<K, V> db, PubSubManager pubSubManager) {
        this.port = port;
        this.db = db;
        this.pubSubManager = pubSubManager;
        this.clusterService = new ClusterService<>();
        this.clientThreadPool = ThreadPoolManager.getInstance().getScheduledExecutorService();
        addShutdownHook();
    }

    public static void main(String[] args) {
        int port = getPort(args);
        DBType dbType = DBType.valueOf(Config.getInstance().getDatabaseType().toUpperCase());
        DB<String, Object> db = DatabaseFactory.createDatabase(dbType);
        new Server<>(port, db, new PubSubManager()).start();
    }

    public void start() {
        log.info(LOGO);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.log(Level.INFO, "🚀 QuickSilverServer DB started on port {0}", port);
            clusterService.registerInCluster(this.port);
            clusterService.syncDataFromCluster(this.db);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log.log(Level.INFO, "📡 New client connected: {0}", clientSocket.getRemoteSocketAddress());
                clientThreadPool.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "❌ Error starting QuickSilverServer on port {0} exception:{1}", new Object[]{port, e});
        }
    }

    private void handleClient(Socket socket) {
        try {
            ClientHandler<K, V> clientHandler = new ClientHandler<>(socket, db, clusterService, pubSubManager);
            clientThreadPool.execute(clientHandler);
        } catch (IOException e) {
            log.severe("❌ Failed to start ClientHandler for client " + socket.getRemoteSocketAddress() + " exception:" + e);
            log.log(Level.SEVERE, "❌ Failed to start ClientHandler for client {0} exception:{1}", new Object[]{socket.getRemoteSocketAddress(), e});
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
