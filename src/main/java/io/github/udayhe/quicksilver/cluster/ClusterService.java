package io.github.udayhe.quicksilver.cluster;

import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.DB;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.util.ClusterUtil.isLocalNode;

public class ClusterService<K> {

    private static final Logger log = Logger.getLogger(ClusterService.class.getName());

    private final ClusterManager    clusterManager    = new ClusterManager();
    private final ConsistentHashing<K> consistentHashing = new ConsistentHashing<>();

    public void registerInCluster(int port) {
        ClusterNode self = new ClusterNode("localhost", port);
        clusterManager.addNode(self);
        consistentHashing.addNode(self);
    }

    @SuppressWarnings("rawtypes")
    public void syncDataFromCluster(DB db) {
        int localPort = Config.getInstance().getServerPort();
        for (ClusterNode node : clusterManager.getNodes()) {
            if (!isLocalNode(node, localPort)) {
                log.log(Level.INFO, "Syncing data from peer node {0}", node);
                String dump = ClusterClient.sendDumpCommand(node);
                if (!dump.isBlank()) {
                    db.restoreData(dump);
                }
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
