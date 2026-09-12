package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;
import io.github.udayhe.quicksilver.resp.value.*;

import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Subscribe implements Command<String, String> {

    private static final Logger log = Logger.getLogger(Subscribe.class.getName());

    private final PubSubManager pubSubManager;
    private final OutputStream clientOut;

    public Subscribe(PubSubManager pubSubManager, OutputStream clientOut) {
        this.pubSubManager = pubSubManager;
        this.clientOut = clientOut;
    }

    @Override
    public RespValue execute(String topic, String ignored) {
        if (topic == null || topic.isBlank()) return RespError.wrongArgs("subscribe");
        pubSubManager.subscribe(topic, clientOut);
        log.log(Level.INFO, "Client subscribed to topic: {0}", topic);
        return new RespArray(List.of(
                BulkString.of("subscribe"),
                BulkString.of(topic),
                new RespInteger(1)
        ));
    }
}
