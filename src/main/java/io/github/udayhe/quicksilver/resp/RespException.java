package io.github.udayhe.quicksilver.resp;

import java.io.IOException;

/**
 * Thrown when the RESP parser encounters a protocol violation or
 * a security limit (e.g. oversized payload, malformed framing).
 */
public class RespException extends IOException {

    public RespException(String message) {
        super(message);
    }

    public RespException(String message, Throwable cause) {
        super(message, cause);
    }
}
