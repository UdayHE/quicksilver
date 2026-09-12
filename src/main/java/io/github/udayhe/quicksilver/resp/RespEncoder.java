package io.github.udayhe.quicksilver.resp;

import io.github.udayhe.quicksilver.resp.value.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Stateless utility for encoding {@link RespValue} objects to the RESP wire format.
 *
 * Public {@code write*} methods encode <em>and</em> flush the stream so the caller
 * does not need to manage flushing explicitly.
 *
 * Internal {@code encode} is package-private and does not flush; it is used
 * recursively when encoding arrays so only one flush occurs per top-level response.
 */
public final class RespEncoder {

    private static final byte[] CRLF      = {'\r', '\n'};
    private static final byte[] NIL_BULK  = "$-1\r\n".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] NIL_ARRAY = "*-1\r\n".getBytes(StandardCharsets.US_ASCII);

    private RespEncoder() {}

    // -------------------------------------------------------------------------
    // Public API — encode + flush
    // -------------------------------------------------------------------------

    /** Encodes {@code value} to {@code out} using RESP framing, then flushes. */
    public static void write(OutputStream out, RespValue value) throws IOException {
        encode(out, value);
        out.flush();
    }

    public static void writeSimpleString(OutputStream out, String value) throws IOException {
        encodeSimpleString(out, value);
        out.flush();
    }

    public static void writeError(OutputStream out, String message) throws IOException {
        encodeError(out, message);
        out.flush();
    }

    public static void writeInteger(OutputStream out, long value) throws IOException {
        encodeInteger(out, value);
        out.flush();
    }

    public static void writeBulkString(OutputStream out, byte[] data) throws IOException {
        encodeBulkString(out, data);
        out.flush();
    }

    /** Writes the RESP nil bulk string ($-1\r\n) — the standard "key not found" reply. */
    public static void writeNil(OutputStream out) throws IOException {
        out.write(NIL_BULK);
        out.flush();
    }

    // -------------------------------------------------------------------------
    // Package-private recursive encoder (no flush)
    // -------------------------------------------------------------------------

    static void encode(OutputStream out, RespValue value) throws IOException {
        switch (value) {
            case SimpleString ss -> encodeSimpleString(out, ss.value());
            case RespError    err -> encodeError(out, err.message());
            case RespInteger  ri  -> encodeInteger(out, ri.value());
            case BulkString   bs  -> encodeBulkString(out, bs.data());
            case RespArray    ra  -> encodeArray(out, ra.elements());
        }
    }

    // -------------------------------------------------------------------------
    // Private encoding primitives
    // -------------------------------------------------------------------------

    private static void encodeSimpleString(OutputStream out, String value) throws IOException {
        out.write('+');
        out.write(value.getBytes(StandardCharsets.UTF_8));
        out.write(CRLF);
    }

    private static void encodeError(OutputStream out, String message) throws IOException {
        out.write('-');
        out.write(message.getBytes(StandardCharsets.UTF_8));
        out.write(CRLF);
    }

    private static void encodeInteger(OutputStream out, long value) throws IOException {
        writeAscii(out, ':' + Long.toString(value));
        out.write(CRLF);
    }

    private static void encodeBulkString(OutputStream out, byte[] data) throws IOException {
        if (data == null) {
            out.write(NIL_BULK);
        } else {
            writeAscii(out, '$' + Integer.toString(data.length));
            out.write(CRLF);
            out.write(data);
            out.write(CRLF);
        }
    }

    private static void encodeArray(OutputStream out, List<RespValue> elements) throws IOException {
        if (elements == null) {
            out.write(NIL_ARRAY);
        } else {
            writeAscii(out, '*' + Integer.toString(elements.size()));
            out.write(CRLF);
            for (RespValue element : elements) {
                encode(out, element); // recursive, no flush
            }
        }
    }

    private static void writeAscii(OutputStream out, String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.US_ASCII));
    }
}
