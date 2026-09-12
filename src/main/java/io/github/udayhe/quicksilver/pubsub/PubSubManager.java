package io.github.udayhe.quicksilver.pubsub;

import io.github.udayhe.quicksilver.resp.RespEncoder;
import io.github.udayhe.quicksilver.resp.value.BulkString;
import io.github.udayhe.quicksilver.resp.value.RespArray;
import io.github.udayhe.quicksilver.resp.value.RespValue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages topic subscriptions and fan-out message delivery.
 *
 * Subscribers are keyed by their raw {@link OutputStream} so that any
 * RESP-encoded push message can be written directly without re-encoding.
 * Dead subscribers (closed sockets) are detected on write failure and
 * removed automatically to prevent unbounded accumulation.
 */
public class PubSubManager {

    private static final Logger log = Logger.getLogger(PubSubManager.class.getName());

    private final Map<String, Set<OutputStream>> subscribersByTopic = new ConcurrentHashMap<>();

    public void subscribe(String topic, OutputStream client) {
        subscribersByTopic.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(client);
        log.log(Level.INFO, "Client subscribed to topic: {0}", topic);
    }

    public void unsubscribe(String topic, OutputStream client) {
        Set<OutputStream> subs = subscribersByTopic.get(topic);
        if (subs != null) subs.remove(client);
        log.log(Level.INFO, "Client unsubscribed from topic: {0}", topic);
    }

    /**
     * Delivers {@code message} to all subscribers of {@code topic}.
     *
     * @return number of subscribers that received the message successfully
     */
    public int publish(String topic, String message) {
        Set<OutputStream> subscribers = subscribersByTopic.get(topic);
        if (subscribers == null || subscribers.isEmpty()) return 0;

        RespValue push = buildPushMessage(topic, message);
        Set<OutputStream> dead = ConcurrentHashMap.newKeySet();
        int delivered = 0;

        for (OutputStream sub : subscribers) {
            try {
                RespEncoder.write(sub, push);
                delivered++;
            } catch (IOException e) {
                log.log(Level.WARNING, "Subscriber write failed, removing dead connection: {0}", e.getMessage());
                dead.add(sub);
            }
        }

        subscribers.removeAll(dead);
        log.log(Level.INFO, "Published to topic '{0}': {1}/{2} delivered",
                new Object[]{topic, delivered, delivered + dead.size()});
        return delivered;
    }

    /** RESP push format: *3 ["message", <topic>, <payload>] */
    private RespValue buildPushMessage(String topic, String message) {
        return new RespArray(List.of(
                BulkString.of("message"),
                BulkString.of(topic),
                BulkString.of(message)
        ));
    }
}
