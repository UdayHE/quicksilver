package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.resp.value.RespValue;
import io.github.udayhe.quicksilver.resp.value.SimpleString;

/**
 * EXIT command — signals that the client wishes to close the connection.
 * The actual socket teardown is performed by {@code ClientHandler} after
 * receiving this response, keeping I/O concerns out of the command layer.
 */
public class Exit<K, V> implements Command<K, V> {

    @Override
    public RespValue execute(K key, V value) {
        return new SimpleString("BYE");
    }
}
