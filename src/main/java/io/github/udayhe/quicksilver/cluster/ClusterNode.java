package io.github.udayhe.quicksilver.cluster;

import java.net.InetSocketAddress;

import static io.github.udayhe.quicksilver.constant.Constants.COLON;

public record ClusterNode(String host, int port) {

    public InetSocketAddress getAddress() {
        return new InetSocketAddress(host, port);
    }

    @Override
    public String toString() {
        return host + COLON + port;
    }
}
