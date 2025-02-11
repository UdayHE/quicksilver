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
    public String execute(K unusedKey, V unusedValue) {
        log.log(Level.INFO, "🔥 Flushing database on this node");
        db.clear();
        for (ClusterNode node : clusterManager.getNodes()) {
            if (!isLocalNode(node, Config.getInstance().getPort()))
                ClusterClient.sendRequest(node, FLUSH.name());
        }
        log.log(Level.INFO, "🔥 Flushing Completed.");
        return OK;
    }
}




