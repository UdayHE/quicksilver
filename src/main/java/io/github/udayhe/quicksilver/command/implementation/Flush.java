package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.cluster.ClusterClient;
import io.github.udayhe.quicksilver.cluster.ClusterManager;
import io.github.udayhe.quicksilver.cluster.ClusterNode;
import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.DB;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.OK;
import static io.github.udayhe.quicksilver.enums.Command.FLUSH;
import static io.github.udayhe.quicksilver.util.ClusterUtil.isLocalNode;

public class Flush<K, V> implements Command<K, V> {
    private static final Logger log = Logger.getLogger(Flush.class.getName());

    private final DB<K, V> db;
    private final ClusterManager clusterManager;

    public Flush(DB<K, V> db, ClusterManager clusterManager) {
        this.db = db;
        this.clusterManager = clusterManager;
    }

    @Override
    public String execute(K key, V value) {
        log.log(Level.INFO, "🔥 Starting database flush on the current node...");
        db.clear();
        int localPort = Config.getInstance().getServerPort(); // Introduced variable for clarity
        sendFlushCommandToOtherNodes(localPort);
        log.log(Level.INFO, "✅ Database flush completed successfully.");
        return OK;
    }

    private void sendFlushCommandToOtherNodes(int localPort) {
        for (ClusterNode node : clusterManager.getNodes()) {
            if (!isLocalNode(node, localPort)) {
                log.log(Level.INFO, "📡 Sending FLUSH command to node: {0}", node);
                ClusterClient.sendRequest(node, FLUSH.name());
            }
        }
    }
}