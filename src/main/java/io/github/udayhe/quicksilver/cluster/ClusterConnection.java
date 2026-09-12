package io.github.udayhe.quicksilver.cluster;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A pooled cluster connection: one socket plus its buffered streams.
 * Streams are created once at construction and reused across requests on
 * the same connection, avoiding per-request allocation of BufferedInputStream
 * and BufferedOutputStream wrappers.
 */
final class ClusterConnection implements AutoCloseable {

    private final Socket       socket;
    private final InputStream  in;
    private final OutputStream out;

    ClusterConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.in     = new BufferedInputStream(socket.getInputStream());
        this.out    = new BufferedOutputStream(socket.getOutputStream());
    }

    InputStream  in()  { return in; }
    OutputStream out() { return out; }

    boolean isAlive() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    @Override
    public void close() {
        try { socket.close(); } catch (IOException _) {}
    }
}
