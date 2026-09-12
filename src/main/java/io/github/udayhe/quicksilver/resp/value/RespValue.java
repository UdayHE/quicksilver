package io.github.udayhe.quicksilver.resp.value;

/**
 * Sealed type hierarchy for all RESP (Redis Serialization Protocol) value types.
 *
 * Wire format summary:
 *   SimpleString  →  +<text>\r\n
 *   RespError     →  -<message>\r\n
 *   RespInteger   →  :<number>\r\n
 *   BulkString    →  $<len>\r\n<data>\r\n  (or $-1\r\n for nil)
 *   RespArray     →  *<count>\r\n<elements>  (or *-1\r\n for nil)
 */
public sealed interface RespValue
        permits SimpleString, RespError, RespInteger, BulkString, RespArray {}
