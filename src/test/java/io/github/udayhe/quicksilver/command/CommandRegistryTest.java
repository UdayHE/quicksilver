package io.github.udayhe.quicksilver.command;

import io.github.udayhe.quicksilver.cluster.ClusterManager;
import io.github.udayhe.quicksilver.db.DB;
import io.github.udayhe.quicksilver.db.implementation.InMemoryDB;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;
import io.github.udayhe.quicksilver.resp.value.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandRegistryTest {

    @Mock
    private DB<String, Object> mockDB;

    @Mock
    private ClusterManager mockClusterManager;

    private PubSubManager pubSubManager;
    private ByteArrayOutputStream clientOut;
    private CommandRegistry<String, Object> commandRegistry;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pubSubManager = new PubSubManager();
        clientOut     = new ByteArrayOutputStream();
        commandRegistry = new CommandRegistry<>(mockDB, mockClusterManager, pubSubManager, clientOut);
    }

    // -------------------------------------------------------------------------
    // Core CRUD
    // -------------------------------------------------------------------------

    @Test
    void testSetCommand() {
        RespValue result = commandRegistry.executeCommand("SET", "key1", "value1");
        assertInstanceOf(SimpleString.class, result);
        assertEquals("OK", ((SimpleString) result).value());
    }

    @Test
    void testGetCommand() {
        when(mockDB.get("key1")).thenReturn("value1");
        RespValue result = commandRegistry.executeCommand("GET", "key1", null);
        assertInstanceOf(BulkString.class, result);
        assertEquals("value1", ((BulkString) result).asString());
    }

    @Test
    void testGetNonExistentKey() {
        when(mockDB.get("nonexistent")).thenReturn(null);
        RespValue result = commandRegistry.executeCommand("GET", "nonexistent", null);
        assertInstanceOf(BulkString.class, result);
        assertTrue(((BulkString) result).isNil());
    }

    @Test
    void testDeleteCommand() {
        RespValue result = commandRegistry.executeCommand("DEL", "key1", null);
        assertInstanceOf(RespInteger.class, result);
        verify(mockDB).delete("key1");
    }

    @Test
    void testFlushCommand() {
        RespValue result = commandRegistry.executeCommand("FLUSH", null, null);
        assertInstanceOf(SimpleString.class, result);
        assertEquals("OK", ((SimpleString) result).value());
    }

    @Test
    void testDumpCommand() {
        when(mockDB.getAll()).thenReturn(Map.of("key1", "value1", "key2", "value2"));
        RespValue result = commandRegistry.executeCommand("DUMP", null, null);
        assertInstanceOf(BulkString.class, result);
        String dump = ((BulkString) result).asString();
        assertNotNull(dump);
        assertTrue(dump.contains("key1") && dump.contains("value1"));
        assertTrue(dump.contains("key2") && dump.contains("value2"));
    }

    @Test
    void testExitCommand() {
        RespValue result = commandRegistry.executeCommand("EXIT", null, null);
        assertInstanceOf(SimpleString.class, result);
        assertEquals("BYE", ((SimpleString) result).value());
    }

    @Test
    void testSubscribeCommand() {
        RespValue result = commandRegistry.executeCommand("SUBSCRIBE", "news", null);
        assertInstanceOf(RespArray.class, result);
        RespArray array = (RespArray) result;
        assertFalse(array.isNil());
        assertEquals(3, array.size());
        assertEquals("subscribe", ((BulkString) array.elements().get(0)).asString());
        assertEquals("news",      ((BulkString) array.elements().get(1)).asString());
        assertEquals(1L,          ((RespInteger) array.elements().get(2)).value());
    }

    @Test
    void testUnsubscribeCommand() {
        RespValue result = commandRegistry.executeCommand("UNSUBSCRIBE", "news", null);
        assertInstanceOf(RespArray.class, result);
        RespArray array = (RespArray) result;
        assertEquals("unsubscribe", ((BulkString) array.elements().get(0)).asString());
        assertEquals("news",        ((BulkString) array.elements().get(1)).asString());
    }

    @Test
    void testPublishCommand() {
        // No subscribers registered for this topic, so delivery count is 0.
        RespValue result = commandRegistry.executeCommand("PUBLISH", "news", "Breaking: Test message");
        assertInstanceOf(RespInteger.class, result);
        assertEquals(0L, ((RespInteger) result).value());
    }

    @Test
    void testUnknownCommand() {
        RespValue result = commandRegistry.executeCommand("UNKNOWN", null, null);
        assertInstanceOf(RespError.class, result);
        assertTrue(((RespError) result).message().contains("unknown command"));
    }

    // -------------------------------------------------------------------------
    // Argument validation (security)
    // -------------------------------------------------------------------------

    @Test
    void testGetWithNullKeyReturnsError() {
        RespValue result = commandRegistry.executeCommand("GET", null, null);
        assertInstanceOf(RespError.class, result);
    }

    @Test
    void testSetWithNullKeyReturnsError() {
        RespValue result = commandRegistry.executeCommand("SET", null, "value");
        assertInstanceOf(RespError.class, result);
    }

    @Test
    void testDelWithNullKeyReturnsError() {
        RespValue result = commandRegistry.executeCommand("DEL", null, null);
        assertInstanceOf(RespError.class, result);
    }

    // -------------------------------------------------------------------------
    // Case insensitivity
    // -------------------------------------------------------------------------

    @Test
    void testCaseInsensitiveCommands() {
        RespValue r1 = commandRegistry.executeCommand("set", "key1", "value1");
        RespValue r2 = commandRegistry.executeCommand("SET", "key1", "value1");
        RespValue r3 = commandRegistry.executeCommand("SeT", "key1", "value1");
        assertEquals(r1, r2);
        assertEquals(r2, r3);
    }

    // -------------------------------------------------------------------------
    // Pub/sub round-trip
    // -------------------------------------------------------------------------

    @Test
    void testPubSubWorkflow() {
        RespValue sub = commandRegistry.executeCommand("SUBSCRIBE", "events", null);
        assertInstanceOf(RespArray.class, sub);

        // Publishing delivers to the in-process subscriber (clientOut stream).
        RespValue pub = commandRegistry.executeCommand("PUBLISH", "events", "hello");
        assertInstanceOf(RespInteger.class, pub);
        assertEquals(1L, ((RespInteger) pub).value());

        RespValue unsub = commandRegistry.executeCommand("UNSUBSCRIBE", "events", null);
        assertInstanceOf(RespArray.class, unsub);
    }

    // -------------------------------------------------------------------------
    // Real DB integration
    // -------------------------------------------------------------------------

    @Test
    void testCommandRegistryWithRealDB() {
        InMemoryDB<String, Object> realDB = new InMemoryDB<>(10);
        CommandRegistry<String, Object> realRegistry = new CommandRegistry<>(
                realDB, mockClusterManager, pubSubManager, new ByteArrayOutputStream());

        RespValue setResult = realRegistry.executeCommand("SET", "testKey", "testValue");
        assertInstanceOf(SimpleString.class, setResult);
        assertEquals("OK", ((SimpleString) setResult).value());

        RespValue getResult = realRegistry.executeCommand("GET", "testKey", null);
        assertInstanceOf(BulkString.class, getResult);
        assertEquals("testValue", ((BulkString) getResult).asString());

        RespValue delResult = realRegistry.executeCommand("DEL", "testKey", null);
        assertInstanceOf(RespInteger.class, delResult);
        assertEquals(1L, ((RespInteger) delResult).value());

        RespValue afterDel = realRegistry.executeCommand("GET", "testKey", null);
        assertInstanceOf(BulkString.class, afterDel);
        assertTrue(((BulkString) afterDel).isNil());
    }

    @Test
    void testMultipleCommands() {
        InMemoryDB<String, Object> realDB = new InMemoryDB<>(10);
        CommandRegistry<String, Object> realRegistry = new CommandRegistry<>(
                realDB, mockClusterManager, pubSubManager, new ByteArrayOutputStream());

        realRegistry.executeCommand("SET", "key1", "value1");
        realRegistry.executeCommand("SET", "key2", "value2");

        RespValue r1 = realRegistry.executeCommand("GET", "key1", null);
        RespValue r2 = realRegistry.executeCommand("GET", "key2", null);

        assertEquals("value1", ((BulkString) r1).asString());
        assertEquals("value2", ((BulkString) r2).asString());
    }
}
