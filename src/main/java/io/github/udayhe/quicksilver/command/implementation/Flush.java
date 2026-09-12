package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.cluster.ClusterClient;
import io.github.udayhe.quicksilver.cluster.ClusterManager;
import io.github.udayhe.quicksilver.cluster.ClusterNode;
import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.resp.value.RespValue;
import io.github.udayhe.quicksilver.resp.value.SimpleString;

import java.util.logging.Level;
import java.util.logging.Logger;

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
    public RespValue execute(K key, V value) {
        log.info("Starting database flush on current node");
        db.clear();
        broadcastFlush(Config.getInstance().getServerPort());
        log.info("Database flush completed");
        return new SimpleString("OK");
    }

    private void broadcastFlush(int localPort) {
        for (ClusterNode node : clusterManager.getNodes()) {
            if (!isLocalNode(node, localPort)) {
                log.log(Level.INFO, "Sending FLUSH to cluster node: {0}", node);
                ClusterClient.sendCommand(node, FLUSH.name());
            }
        }
    }
}
