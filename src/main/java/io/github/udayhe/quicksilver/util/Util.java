package io.github.udayhe.quicksilver.util;

import io.github.udayhe.quicksilver.config.Config;

import static io.github.udayhe.quicksilver.constant.Constants.ENV_QUICKSILVER_PORT;
import static java.lang.System.getenv;

public class Util {
    private Util() {}

    /**
     * Retrieves the port number by checking the configuration, environment variables, and command-line arguments.
     *
     * @param args Command-line arguments
     * @return Resolved port number
     */
    public static int getPort(String[] args) {
        int port = Config.getInstance().getServerPort();
        port = getPortFromEnv(port);
        return getPortFromArgs(args, port);
    }

    /**
     * Retrieves the port from an environment variable, if set.
     *
     * @param defaultPort Default port value if the environment variable is not set or invalid
     * @return Port from the environment variable, or the default port
     */
    public static int getPortFromEnv(int defaultPort) {
        String envPort = getenv(ENV_QUICKSILVER_PORT);
        return envPort != null ? parsePort(envPort, defaultPort) : defaultPort;
    }

    /**
     * Retrieves the port from command-line arguments, if provided.
     *
     * @param args        Command-line arguments
     * @param defaultPort Default port value if the argument is not provided or invalid
     * @return Port from the arguments, or the default port
     */
    public static int getPortFromArgs(String[] args, int defaultPort) {
        return args.length > 0 ? parsePort(args[0], defaultPort) : defaultPort;
    }

    /**
     * Parses the port number from a string, falling back to a default value on failure.
     *
     * @param value       String value to parse
     * @param defaultPort Fallback port value in case of a parsing error
     * @return Parsed port number or the default port
     */
    private static int parsePort(String value, int defaultPort) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultPort;
        }
    }
}