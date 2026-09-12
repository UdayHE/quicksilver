package io.github.udayhe.quicksilver.resp.value;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * RESP Bulk String: $<len>\r\n<data>\r\n  (or $-1\r\n for nil).
 * Binary-safe; data may be null to represent the nil bulk string.
 */
public record BulkString(byte[] data) implements RespValue {

    public static final BulkString NIL = new BulkString(null);

    public static BulkString of(String value) {
        return value == null ? NIL : new BulkString(value.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isNil() {
        return data == null;
    }

    public String asString() {
        return data == null ? null : new String(data, StandardCharsets.UTF_8);
    }

    // byte[] record components don't get structural equals/hashCode automatically.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BulkString(byte[] data1))) return false;
        return Arrays.equals(data, data1);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        return isNil() ? "BulkString(nil)" : "BulkString(" + asString() + ")";
    }
}
