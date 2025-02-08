package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import org.slf4j.Logger;

import java.net.Socket;

import static io.github.udayhe.quicksilver.constant.Constants.BYE;
import static org.slf4j.LoggerFactory.getLogger;

public class Exit<K, V> implements Command<K, V> {

    private static final Logger log = getLogger(Exit.class);
    private final Socket socket;

    public Exit(Socket socket) {
        this.socket = socket;
    }

    @Override
    public String execute(K unusedKey, V unusedValue) {
        try {
            log.info("🔌 Closing connection for client: {}", socket.getRemoteSocketAddress());
            socket.close();
            return BYE;
        } catch (Exception e) {
            log.error("❌ Error closing client connection", e);
            return "ERROR: Unable to close connection";
        }
    }
}
