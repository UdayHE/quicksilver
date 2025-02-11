package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Subscribe implements Command<String, PrintWriter> {
    private static final Logger log = Logger.getLogger(Subscribe.class.getName());
    private final PubSubManager pubSubManager;

    public Subscribe(PubSubManager pubSubManager) {
        this.pubSubManager = pubSubManager;
    }

    @Override
    public String execute(String topic, PrintWriter client) {
        if (topic == null) {
            return "ERROR: Usage - SUBSCRIBE <topic>";
        }
        pubSubManager.subscribe(topic, client);
        log.log(Level.INFO, "📡 Client subscribed to topic: {0}", topic);
        return "✅ Subscribed to topic: " + topic;
    }
}