package io.github.udayhe.quicksilver.cluster;

import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.udayhe.quicksilver.constant.Constants.LOCALHOST;
import static io.github.udayhe.quicksilver.enums.Command.DUMP;
import static io.github.udayhe.quicksilver.util.ClusterUtil.isLocalNode;

public class ClusterService {

    private static final Logger log = LoggerFactory.getLogger(ClusterService.class);
    private final ClusterManager clusterManager = new ClusterManager();
    private final ConsistentHashing consistentHashing = new ConsistentHashing();

    public void registerInCluster(int port) {
        ClusterNode self = new ClusterNode(LOCALHOST, port);
        clusterManager.addNode(self);
        consistentHashing.addNode(self);
    }

    public void syncDataFromCluster(DB db) {
        for (ClusterNode node : clusterManager.getNodes()) {
            if (!isLocalNode(node, Config.getInstance().getPort())) {
                log.info("🔄 Syncing data from {}", node);
                String response = ClusterClient.sendRequest(node, DUMP.name());
                db.restoreData(response);
            }
        }
    }

    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    public ConsistentHashing getConsistentHashing() {
        return consistentHashing;
    }
}
