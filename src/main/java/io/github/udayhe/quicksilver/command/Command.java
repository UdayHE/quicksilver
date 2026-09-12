package io.github.udayhe.quicksilver.command;

import io.github.udayhe.quicksilver.resp.value.RespValue;

/**
 * Strategy interface for all server commands.
 * Each implementation encapsulates one RESP command's logic and returns
 * a typed {@link RespValue} that the connection layer encodes on the wire.
 */
public interface Command<K, V> {

    RespValue execute(K key, V value);
}
