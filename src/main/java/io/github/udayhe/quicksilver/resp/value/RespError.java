package io.github.udayhe.quicksilver.resp.value;

/**
 * RESP Error: -<message>\r\n
 * Conventionally prefixed with an error type, e.g. "ERR", "WRONGTYPE".
 */
public record RespError(String message) implements RespValue {

    public RespError {
        if (message == null) throw new IllegalArgumentException("RespError message must not be null");
    }

    public static RespError err(String detail) {
        return new RespError("ERR " + detail);
    }

    public static RespError wrongArgs(String command) {
        return new RespError("ERR wrong number of arguments for '" + command + "' command");
    }
}
