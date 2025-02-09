package io.github.udayhe.quicksilver.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class ClusterManager {

    private static final Logger log = LoggerFactory.getLogger(ClusterManager.class);
    private final List<ClusterNode> nodes = new CopyOnWriteArrayList<>();

    public void addNode(ClusterNode node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            log.info("🖥️ Node added: {}", node);
        }
    }

    public void removeNode(ClusterNode node) {
        nodes.remove(node);
        log.info("❌ Node removed: {}", node);
    }

    public List<ClusterNode> getNodes() {
        return nodes;
    }

}
