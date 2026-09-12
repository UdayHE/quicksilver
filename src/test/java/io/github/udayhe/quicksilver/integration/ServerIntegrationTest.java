package io.github.udayhe.quicksilver.integration;

import io.github.udayhe.quicksilver.Server;
import io.github.udayhe.quicksilver.command.CommandRegistry;
import io.github.udayhe.quicksilver.db.implementation.InMemoryDB;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;
import io.github.udayhe.quicksilver.resp.value.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ServerIntegrationTest {

    private Server<String, Object> server;
    private ServerSocket testServerSocket;
    private int testPort;
    private InMemoryDB<String, Object> testDB;
    private PubSubManager pubSubManager;

    @BeforeEach
    void setUp() throws IOException {
        testServerSocket = new ServerSocket(0);
        testPort         = testServerSocket.getLocalPort();
        testDB           = new InMemoryDB<>(100);
        pubSubManager    = new PubSubManager();
        server           = new Server<>(testPort, testDB, pubSubManager);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (testServerSocket != null && !testServerSocket.isClosed()) {
            testServerSocket.close();
        }
    }

    // -------------------------------------------------------------------------
    // CommandRegistry integration (via real DB, no live socket)
    // -------------------------------------------------------------------------

    @Test
    void testBasicSetAndGet() {
        CommandRegistry<String, Object> registry = registryFor(testDB);

        RespValue setResult = registry.executeCommand("SET", "testKey", "testValue");
        assertInstanceOf(SimpleString.class, setResult);
        assertEquals("OK", ((SimpleString) setResult).value());

        RespValue getResult = registry.executeCommand("GET", "testKey", null);
        assertInstanceOf(BulkString.class, getResult);
        assertEquals("testValue", ((BulkString) getResult).asString());

        assertEquals("testValue", testDB.get("testKey"));
    }

    @Test
    void testSetGetDelete() {
        CommandRegistry<String, Object> registry = registryFor(testDB);

        registry.executeCommand("SET", "testKey", "testValue");

        RespValue delResult = registry.executeCommand("DEL", "testKey", null);
        assertInstanceOf(RespInteger.class, delResult);
        assertEquals(1L, ((RespInteger) delResult).value());

        RespValue afterDel = registry.executeCommand("GET", "testKey", null);
        assertInstanceOf(BulkString.class, afterDel);
        assertTrue(((BulkString) afterDel).isNil());
    }

    @Test
    void testPubSubOperations() {
        ByteArrayOutputStream subscriberOut = new ByteArrayOutputStream();
        CommandRegistry<String, Object> registry = new CommandRegistry<>(
                testDB, server.getClusterService().getClusterManager(),
                pubSubManager, subscriberOut);

        RespValue sub = registry.executeCommand("SUBSCRIBE", "news", null);
        assertInstanceOf(RespArray.class, sub);
        assertEquals("subscribe", ((BulkString) ((RespArray) sub).elements().get(0)).asString());

        RespValue pub = registry.executeCommand("PUBLISH", "news", "Breaking: Test");
        assertInstanceOf(RespInteger.class, pub);
        assertEquals(1L, ((RespInteger) pub).value());

        // Confirm the push message was written to the subscriber stream.
        assertTrue(subscriberOut.size() > 0, "Subscriber stream should have received a push message");

        RespValue unsub = registry.executeCommand("UNSUBSCRIBE", "news", null);
        assertInstanceOf(RespArray.class, unsub);
    }

    @Test
    void testUnknownCommandReturnsError() {
        CommandRegistry<String, Object> registry = registryFor(testDB);
        RespValue result = registry.executeCommand("UNKNOWN", null, null);
        assertInstanceOf(RespError.class, result);
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    @Test
    void testDatabasePersistence() throws IOException {
        testDB.set("key1", "value1", 0);
        testDB.set("key2", "value2", 0);
        testDB.saveToDisk("test_persistence.db");

        InMemoryDB<String, Object> newDB = new InMemoryDB<>(100);
        newDB.loadFromDisk("test_persistence.db");

        assertEquals("value1", newDB.get("key1"));
        assertEquals("value2", newDB.get("key2"));

        new File("test_persistence.db").delete();
    }

    // -------------------------------------------------------------------------
    // Concurrency
    // -------------------------------------------------------------------------

    @Test
    void testConcurrentClientHandling() throws InterruptedException {
        int numClients = 5;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(numClients);

        for (int i = 0; i < numClients; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    testDB.set("concurrentKey", "concurrentValue", 0);
                    testDB.get("concurrentKey");
                    testDB.delete("concurrentKey");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS), "All client threads should complete");
    }

    // -------------------------------------------------------------------------
    // TTL / LRU
    // -------------------------------------------------------------------------

    @Test
    void testTTLExpiration() throws InterruptedException {
        testDB.set("expiringKey", "expiringValue", 100);
        assertEquals("expiringValue", testDB.get("expiringKey"));
        Thread.sleep(150);
        assertNull(testDB.get("expiringKey"));
    }

    @Test
    void testLRUEviction() {
        for (int i = 0; i < 100; i++) testDB.set("key" + i, "value" + i, 0);
        testDB.get("key0");
        testDB.get("key1");
        for (int i = 100; i < 110; i++) testDB.set("key" + i, "value" + i, 0);
        assertNotNull(testDB.get("key0"));
        assertNotNull(testDB.get("key1"));
    }

    @Test
    @Disabled("Integration test — requires a live server socket")
    void testServerStartAndStop() {
        assertTrue(testPort > 0);
        assertFalse(testServerSocket.isClosed());
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private CommandRegistry<String, Object> registryFor(InMemoryDB<String, Object> db) {
        return new CommandRegistry<>(
                db, server.getClusterService().getClusterManager(),
                pubSubManager, new ByteArrayOutputStream());
    }
}
