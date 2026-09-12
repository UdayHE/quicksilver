package io.github.udayhe.quicksilver.resp;

import io.github.udayhe.quicksilver.resp.value.*;
import io.github.udayhe.quicksilver.security.ConnectionLimits;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Stateful RESP (Redis Serialization Protocol) parser.
 *
 * Supports both the binary RESP framing used by redis-cli and the
 * plain-text inline format used by telnet / nc for backward compatibility.
 *
 * Security guards enforced:
 *  - Inline lines capped at {@link ConnectionLimits#MAX_INLINE_BYTES}
 *  - Bulk strings capped at {@link ConnectionLimits#MAX_BULK_BYTES}
 *  - Array element counts capped at {@link ConnectionLimits#MAX_ARRAY_ELEMENTS}
 */
public class RespParser {

    private final InputStream in;

    public RespParser(InputStream in) {
        if (in == null) throw new IllegalArgumentException("InputStream must not be null");
        this.in = in;
    }

    /**
     * Reads and returns the next RESP value from the stream.
     * Returns {@code null} when the peer has closed the connection.
     */
    public RespValue parse() throws IOException {
        int b = in.read();
        if (b == -1) return null;

        return switch ((char) b) {
            case '+' -> new SimpleString(readLine());
            case '-' -> new RespError(readLine());
            case ':' -> new RespInteger(parseLong(readLine(), "integer"));
            case '$' -> parseBulkString();
            case '*' -> parseArray();
            default  -> parseInline((char) b); // plain-text / telnet compatibility
        };
    }

    // -------------------------------------------------------------------------
    // RESP type parsers
    // -------------------------------------------------------------------------

    private BulkString parseBulkString() throws IOException {
        long length = parseLong(readLine(), "bulk string length");
        if (length == -1) return BulkString.NIL;
        if (length < 0 || length > ConnectionLimits.MAX_BULK_BYTES) {
            throw new RespException("Bulk string length out of allowed range: " + length);
        }
        byte[] data = in.readNBytes((int) length);
        if (data.length != (int) length) {
            throw new RespException("Connection closed while reading bulk string data");
        }
        consumeCRLF("after bulk string data");
        return new BulkString(data);
    }

    private RespArray parseArray() throws IOException {
        int count = (int) parseLong(readLine(), "array length");
        if (count == -1) return RespArray.NIL;
        if (count < 0 || count > ConnectionLimits.MAX_ARRAY_ELEMENTS) {
            throw new RespException("Array element count out of allowed range: " + count);
        }
        List<RespValue> elements = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            RespValue element = parse();
            if (element == null) throw new RespException("Connection closed while reading array element " + i);
            elements.add(element);
        }
        return new RespArray(elements);
    }

    /**
     * Handles plain-text commands (e.g. from telnet): reads the rest of the
     * current line, splits on whitespace, and wraps tokens as BulkStrings.
     */
    private RespArray parseInline(char firstChar) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(firstChar);
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') {
                int next = in.read();
                if (next != '\n' && next != -1) {
                    throw new RespException("Protocol error: expected LF after CR in inline command");
                }
                break;
            }
            if (b == '\n') break; // bare LF accepted
            sb.append((char) b);
            if (sb.length() > ConnectionLimits.MAX_INLINE_BYTES) {
                throw new RespException("Inline command exceeds maximum allowed length");
            }
        }
        return tokenize(sb.toString());
    }

    // -------------------------------------------------------------------------
    // Low-level helpers
    // -------------------------------------------------------------------------

    /** Reads bytes until CRLF (or bare LF). Returns the line without the terminator. */
    private String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') {
                int next = in.read();
                if (next != '\n') {
                    throw new RespException("Protocol error: expected LF after CR, got " + next);
                }
                return sb.toString();
            }
            if (b == '\n') return sb.toString(); // bare LF
            sb.append((char) b);
            if (sb.length() > ConnectionLimits.MAX_INLINE_BYTES) {
                throw new RespException("Line exceeds maximum allowed length");
            }
        }
        throw new RespException("Unexpected end of stream while reading line");
    }

    private void consumeCRLF(String context) throws IOException {
        int r = in.read();
        int n = in.read();
        if (r != '\r' || n != '\n') {
            throw new RespException("Protocol error: expected CRLF " + context + ", got " + r + "," + n);
        }
    }

    private long parseLong(String s, String context) throws RespException {
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            throw new RespException("Protocol error: invalid " + context + " '" + s + "'");
        }
    }

    private RespArray tokenize(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) return new RespArray(List.of());
        String[] tokens = trimmed.split("\\s+");
        List<RespValue> elements = new ArrayList<>(tokens.length);
        for (String token : tokens) {
            elements.add(new BulkString(token.getBytes(StandardCharsets.UTF_8)));
        }
        return new RespArray(elements);
    }
}
