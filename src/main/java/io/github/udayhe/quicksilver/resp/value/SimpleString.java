package io.github.udayhe.quicksilver.resp.value;

/**
 * RESP Simple String: +<text>\r\n
 * Used for short, non-binary status replies (e.g. "+OK").
 * Must not contain CR or LF characters.
 */
public record SimpleString(String value) implements RespValue {

    public SimpleString {
        if (value == null) throw new IllegalArgumentException("SimpleString value must not be null");
        if (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0)
            throw new IllegalArgumentException("SimpleString must not contain CR or LF");
    }
}
