package io.github.udayhe.quicksilver.util;

import io.github.udayhe.quicksilver.cluster.ClusterNode;

import static io.github.udayhe.quicksilver.constant.Constants.LOCALHOST;

public class ClusterUtil {

    private ClusterUtil() {}

    /**
     * Determines if this node represents the local node based on its host and port.
     *
     * @param node ClusterNode
     * @param port The expected local port.
     * @return true if the node is local, false otherwise.
     */
    public static boolean isLocalNode(ClusterNode node, int port) {
        return node.host().equals(LOCALHOST) && node.port() == port;
    }
}
