package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.BYE;
import static io.github.udayhe.quicksilver.constant.Constants.ERROR;

public class Exit<K, V> implements Command<K, V> {

    private static final Logger logger = Logger.getLogger(Exit.class.getName());
    private static final String LOG_CLOSING_CONNECTION = "🔌 Closing connection for client: {0}";
    private static final String LOG_ERROR_CLOSING = "❌ Error closing client connection:";

    private final Socket socket;

    public Exit(Socket socket) {
        this.socket = socket;
    }

    @Override
    public String execute(K key, V value) {
        logger.log(Level.INFO, LOG_CLOSING_CONNECTION, socket.getRemoteSocketAddress());
        return closeSocket();
    }

    private String closeSocket() {
        try {
            socket.close();
            return BYE;
        } catch (Exception e) {
            logger.log(Level.SEVERE, LOG_ERROR_CLOSING, e);
            return ERROR + ": Unable to close connection";
        }
    }
}