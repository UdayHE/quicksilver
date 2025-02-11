package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Publish implements Command<String, String> {
    private static final Logger log = Logger.getLogger(Publish.class.getName());
    private final PubSubManager pubSubManager;

    public Publish(PubSubManager pubSubManager) {
        this.pubSubManager = pubSubManager;
    }

    @Override
    public String execute(String topic, String message) {
        if (topic == null || message == null) {
            return "ERROR: Usage - PUBLISH <topic> <message>";
        }
        pubSubManager.publish(topic, message);
        log.log(Level.INFO, "📢 Published message to topic {0}: {1}", new Object[]{topic, message});
        return "📢 Message published to topic: " + topic;
    }
}
