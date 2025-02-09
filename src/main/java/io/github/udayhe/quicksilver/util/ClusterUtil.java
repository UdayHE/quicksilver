package io.github.udayhe.quicksilver.util;

import io.github.udayhe.quicksilver.cluster.ClusterNode;

import static io.github.udayhe.quicksilver.constant.Constants.LOCALHOST;

public class ClusterUtil {

    private ClusterUtil() {}

    public static boolean isLocalNode(ClusterNode node, int port) {
        return node.host().equals(LOCALHOST) && node.port() == port;
    }
}
