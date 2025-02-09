package io.github.udayhe.quicksilver.util;

import io.github.udayhe.quicksilver.config.Config;

import static io.github.udayhe.quicksilver.constant.Constants.DEFAULT_PORT;
import static io.github.udayhe.quicksilver.constant.Constants.ENV_QUICKSILVER_PORT;
import static java.lang.System.getenv;

public class Util {

    private Util() {}

    public static int getPort(String[] args) {
        int port = Config.getInstance().getPort();
        port = getPortFromEnvironmentVariable(port);
        port = allowOverrideFromArgs(args, port);
        return port;
    }

    public static int allowOverrideFromArgs(String[] args, int port) {
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                port = DEFAULT_PORT;
            }
        }
        return port;
    }

    public static int getPortFromEnvironmentVariable(int port) {
        String envPort = getenv(ENV_QUICKSILVER_PORT);
        if (envPort != null) {
            try {
                port = Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                port = DEFAULT_PORT;
            }
        }
        return port;
    }
}
