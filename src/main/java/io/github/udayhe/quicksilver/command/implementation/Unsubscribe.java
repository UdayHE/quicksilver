package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Unsubscribe implements Command<String, PrintWriter> {

    private static final Logger logger = Logger.getLogger(Unsubscribe.class.getName());
    private static final String ERROR_MESSAGE = "ERROR: Usage - UNSUBSCRIBE <topic>";
    private static final String SUCCESS_TEMPLATE = "✅ Unsubscribed from topic: {0}";

    private final PubSubManager pubSubManager;

    public Unsubscribe(PubSubManager pubSubManager) {
        this.pubSubManager = pubSubManager;
    }

    @Override
    public String execute(String topic, PrintWriter client) {
        if (topic == null) {
            return ERROR_MESSAGE;
        }
        pubSubManager.unsubscribe(topic, client);
        logClientUnsubscribed(topic);
        return SUCCESS_TEMPLATE.replace("{0}", topic);
    }

    private void logClientUnsubscribed(String topic) {
        logger.log(Level.INFO, "🔌 Client unsubscribed from topic: {0}", topic);
    }
}