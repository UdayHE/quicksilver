package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Publish implements Command<String, String> {

    private static final Logger log = Logger.getLogger(Publish.class.getName());
    private static final String ERROR_USAGE = "ERROR: Usage - PUBLISH <topic> <message>";
    private static final String LOG_MESSAGE_TEMPLATE = "📢 Published message to topic {0}: {1}";

    private final PubSubManager pubSubManager;

    public Publish(PubSubManager pubSubManager) {
        this.pubSubManager = pubSubManager;
    }

    @Override
    public String execute(String topicName, String messageContent) {
        if (isInvalidInput(topicName, messageContent)) {
            return ERROR_USAGE;
        }

        pubSubManager.publish(topicName, messageContent);
        log.log(Level.INFO, LOG_MESSAGE_TEMPLATE, new Object[]{topicName, messageContent});
        return String.format("📢 Message published to topic: %s", topicName);
    }

    private boolean isInvalidInput(String topicName, String messageContent) {
        return topicName == null || messageContent == null;
    }
}