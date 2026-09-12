package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;
import io.github.udayhe.quicksilver.resp.value.RespError;
import io.github.udayhe.quicksilver.resp.value.RespInteger;
import io.github.udayhe.quicksilver.resp.value.RespValue;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Publish implements Command<String, String> {

    private static final Logger log = Logger.getLogger(Publish.class.getName());

    private final PubSubManager pubSubManager;

    public Publish(PubSubManager pubSubManager) {
        this.pubSubManager = pubSubManager;
    }

    @Override
    public RespValue execute(String topic, String message) {
        if (topic == null || message == null) return RespError.wrongArgs("publish");
        int delivered = pubSubManager.publish(topic, message);
        log.log(Level.INFO, "PUBLISH {0}: delivered to {1} subscriber(s)", new Object[]{topic, delivered});
        return new RespInteger(delivered);
    }
}
