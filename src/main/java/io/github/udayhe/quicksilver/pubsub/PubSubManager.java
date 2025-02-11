package io.github.udayhe.quicksilver.pubsub;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PubSubManager {

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(PubSubManager.class.getName());

    private final Map<String, Set<PrintWriter>> topicSubscribers = new ConcurrentHashMap<>();

    public void subscribe(String topic, PrintWriter client) {
        topicSubscribers.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(client);
        log.log(Level.INFO, "📡 Client subscribed to topic: {0}", topic);
    }

    public void unsubscribe(String topic, PrintWriter client) {
        topicSubscribers.getOrDefault(topic, ConcurrentHashMap.newKeySet()).remove(client);
        log.log(Level.INFO, "🔌 Client unsubscribed from topic: {0}", topic);
    }

    public void publish(String topic, String message) {
        Set<PrintWriter> subscribers = topicSubscribers.get(topic);
        if (subscribers != null && !subscribers.isEmpty()) {
            log.log(Level.INFO, "📢 Publishing message to {0} subscribers on topic: {1}", new Object[]{subscribers.size(), topic});
            for (PrintWriter subscriber : subscribers) {
                subscriber.println("📢 [" + topic + "]: " + message);
            }
        }
    }
}
