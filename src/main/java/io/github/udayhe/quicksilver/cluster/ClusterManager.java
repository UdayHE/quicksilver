package io.github.udayhe.quicksilver.cluster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClusterManager {

    private static final Logger log = Logger.getLogger(ClusterManager.class.getName());
    private final List<ClusterNode> nodes = new CopyOnWriteArrayList<>();

    public void addNode(ClusterNode node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            log.log(Level.INFO, "🖥️ Node added: {0}", node);
        }
    }

    public void removeNode(ClusterNode node) {
        nodes.remove(node);
        log.log(Level.INFO, "❌ Node removed: {0}", node);
    }

    public List<ClusterNode> getNodes() {
        return nodes;
    }

}
