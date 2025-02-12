package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Subscribe implements Command<String, PrintWriter> {

    private static final Logger LOG = Logger.getLogger(Subscribe.class.getName());
    private static final String ERROR_USAGE_MESSAGE = "ERROR: Usage - SUBSCRIBE <topic>";
    private static final String SUCCESS_MESSAGE_TEMPLATE = "✅ Subscribed to topic: {0}";
    private static final String LOG_MESSAGE_TEMPLATE = "📡 Client subscribed to topic: {0}";
    private final PubSubManager pubSubManager;

    public Subscribe(PubSubManager pubSubManager) {
        this.pubSubManager = pubSubManager;
    }

    @Override
    public String execute(String topic, PrintWriter subscriberWriter) {
        if (isInvalidTopic(topic)) {
            return ERROR_USAGE_MESSAGE;
        }
        pubSubManager.subscribe(topic, subscriberWriter);
        LOG.log(Level.INFO, LOG_MESSAGE_TEMPLATE, topic);
        return SUCCESS_MESSAGE_TEMPLATE.replace("{0}", topic);
    }

    private boolean isInvalidTopic(String topic) {
        return topic == null || topic.trim().isEmpty();
    }
}