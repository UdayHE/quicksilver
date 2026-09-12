package io.github.udayhe.quicksilver.security;

/**
 * Hard limits applied at the protocol parsing layer to prevent
 * denial-of-service via oversized payloads or unbounded allocations.
 * Values mirror Redis defaults where applicable.
 */
public final class ConnectionLimits {

    private ConnectionLimits() {}

    /** Maximum length of an inline (plain-text) command line in bytes. */
    public static final int MAX_INLINE_BYTES = 65_536;

    /** Maximum bulk-string payload in bytes (512 MB, same as Redis). */
    public static final long MAX_BULK_BYTES = 512L * 1024 * 1024;

    /** Maximum number of elements in a single RESP array. */
    public static final int MAX_ARRAY_ELEMENTS = 1_048_576;

    /** Client socket read timeout in milliseconds (5 minutes). */
    public static final int SOCKET_TIMEOUT_MS = 300_000;

    /** Maximum key size in bytes (512 KB). */
    public static final int MAX_KEY_BYTES = 512 * 1024;
}
