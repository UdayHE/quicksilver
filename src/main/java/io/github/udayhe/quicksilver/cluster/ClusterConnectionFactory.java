package io.github.udayhe.quicksilver.cluster;

import io.github.udayhe.quicksilver.security.ConnectionLimits;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Commons Pool2 factory that creates, validates, and destroys
 * {@link ClusterConnection} instances for a single {@link ClusterNode}.
 */
final class ClusterConnectionFactory extends BasePooledObjectFactory<ClusterConnection> {

    private final ClusterNode node;

    ClusterConnectionFactory(ClusterNode node) {
        this.node = node;
    }

    @Override
    public ClusterConnection create() throws Exception {
        Socket socket = new Socket();
        try {
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(ConnectionLimits.SOCKET_TIMEOUT_MS);
            socket.connect(
                    new InetSocketAddress(node.host(), node.port()),
                    ConnectionLimits.CONNECT_TIMEOUT_MS);
            ClusterConnection conn = new ClusterConnection(socket);
            socket = null; // ownership transferred to conn
            return conn;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    @Override
    public PooledObject<ClusterConnection> wrap(ClusterConnection conn) {
        return new DefaultPooledObject<>(conn);
    }

    @Override
    public boolean validateObject(PooledObject<ClusterConnection> p) {
        return p.getObject().isAlive();
    }

    @Override
    public void destroyObject(PooledObject<ClusterConnection> p) {
        p.getObject().close();
    }
}
