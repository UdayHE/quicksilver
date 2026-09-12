package io.github.udayhe.quicksilver.resp.value;

/**
 * RESP Integer: :<number>\r\n
 * Used for counts (DEL, PUBLISH subscriber count, etc.).
 */
public record RespInteger(long value) implements RespValue {}
