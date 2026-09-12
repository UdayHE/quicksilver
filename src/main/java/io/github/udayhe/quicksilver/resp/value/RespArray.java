package io.github.udayhe.quicksilver.resp.value;

import java.util.List;

/**
 * RESP Array: *<count>\r\n<elements>  (or *-1\r\n for nil).
 * Elements may be any mix of RespValue subtypes.
 */
public record RespArray(List<RespValue> elements) implements RespValue {

    public static final RespArray NIL = new RespArray(null);

    public boolean isNil() {
        return elements == null;
    }

    public boolean isEmpty() {
        return elements != null && elements.isEmpty();
    }

    public int size() {
        return elements == null ? -1 : elements.size();
    }
}
