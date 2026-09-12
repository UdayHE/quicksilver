package io.github.udayhe.quicksilver.command.implementation;

import io.github.udayhe.quicksilver.command.Command;
import io.github.udayhe.quicksilver.pubsub.PubSubManager;
import io.github.udayhe.quicksilver.resp.value.*;

import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Unsubscribe implements Command<String, String> {

    private static final Logger log = Logger.getLogger(Unsubscribe.class.getName());

    private final PubSubManager pubSubManager;
    private final OutputStream clientOut;

    public Unsubscribe(PubSubManager pubSubManager, OutputStream clientOut) {
        this.pubSubManager = pubSubManager;
        this.clientOut = clientOut;
    }

    @Override
    public RespValue execute(String topic, String ignored) {
        if (topic == null) return RespError.wrongArgs("unsubscribe");
        pubSubManager.unsubscribe(topic, clientOut);
        log.log(Level.INFO, "Client unsubscribed from topic: {0}", topic);
        return new RespArray(List.of(
                BulkString.of("unsubscribe"),
                BulkString.of(topic),
                new RespInteger(0)
        ));
    }
}
