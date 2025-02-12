package io.github.udayhe.quicksilver.pubsub;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PubSubManager {

    private static final Logger log = java.util.logging.Logger.getLogger(PubSubManager.class.getName());
    private static final Set<PrintWriter> DEFAULT_SUBSCRIBERS = ConcurrentHashMap.newKeySet();

    private final Map<String, Set<PrintWriter>> subscribersByTopic = new ConcurrentHashMap<>();

    public void subscribe(String topic, PrintWriter client) {
        subscribersByTopic.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(client);
        logInfo("📡 Client subscribed to topic: {0}", topic);
    }

    public void unsubscribe(String topic, PrintWriter client) {
        subscribersByTopic.getOrDefault(topic, DEFAULT_SUBSCRIBERS).remove(client);
        logInfo("🔌 Client unsubscribed from topic: {0}", topic);
    }

    public void publish(String topic, String message) {
        Set<PrintWriter> subscribers = subscribersByTopic.get(topic);
        if (subscribers != null && !subscribers.isEmpty()) {
            logInfo("📢 Publishing message to {0} subscribers on topic: {1}", subscribers.size(), topic);
            subscribers.forEach(subscriber -> subscriber.println("📢 [" + topic + "]: " + message));
        }
    }

    private void logInfo(String message, Object... params) {
        log.log(Level.INFO, message, params);
    }
}