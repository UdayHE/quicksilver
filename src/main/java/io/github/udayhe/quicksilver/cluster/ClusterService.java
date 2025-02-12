package io.github.udayhe.quicksilver.cluster;

import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.DB;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.LOCALHOST;
import static io.github.udayhe.quicksilver.enums.Command.DUMP;
import static io.github.udayhe.quicksilver.util.ClusterUtil.isLocalNode;

public class ClusterService<K> {

    private static final Logger log = Logger.getLogger(ClusterService.class.getName());
    private final ClusterManager clusterManager = new ClusterManager();
    private final ConsistentHashing<K> consistentHashing = new ConsistentHashing<>();

    public void registerInCluster(int port) {
        ClusterNode self = new ClusterNode(LOCALHOST, port);
        clusterManager.addNode(self);
        consistentHashing.addNode(self);
    }

    public void syncDataFromCluster(DB db) {
        for (ClusterNode node : clusterManager.getNodes()) {
            if (!isLocalNode(node, Config.getInstance().getServerPort())) {
                log.log(Level.INFO, "🔄 Syncing data from {0}", node);
                String response = ClusterClient.sendRequest(node, DUMP.name());
                db.restoreData(response);
            }
        }
    }

    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    public ConsistentHashing<K> getConsistentHashing() {
        return consistentHashing;
    }

    public ClusterNode getResponsibleNode(K key) {
        return consistentHashing.getNodeForKey(key);
    }

}
