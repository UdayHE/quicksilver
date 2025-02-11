package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Unsubscribe implements Command<String, PrintWriter> {
    private static final Logger log = Logger.getLogger(Unsubscribe.class.getName());
    private final PubSubManager pubSubManager;

    public Unsubscribe(PubSubManager pubSubManager) {
        this.pubSubManager = pubSubManager;
    }

    @Override
    public String execute(String topic, PrintWriter client) {
        if (topic == null) {
            return "ERROR: Usage - UNSUBSCRIBE <topic>";
        }
        pubSubManager.unsubscribe(topic, client);
        log.log(Level.INFO, "🔌 Client unsubscribed from topic: {0}", topic);
        return "✅ Unsubscribed from topic: " + topic;
    }
}
