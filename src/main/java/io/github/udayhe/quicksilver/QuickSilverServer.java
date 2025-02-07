package io.github.udayhe.quicksilver;

import io.github.udayhe.quicksilver.command.CommandRegistry;
import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.QuickSilverDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static io.github.udayhe.quicksilver.constant.Constants.*;
import static java.lang.System.*;

public class QuickSilverServer {

    private static final Logger log = LoggerFactory.getLogger(QuickSilverServer.class);
    private final QuickSilverDB db = new QuickSilverDB();
    private final CommandRegistry commandRegistry = new CommandRegistry(db);
    private final int port;

    public QuickSilverServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        int port = getPort(args);
        port = getPortFromEnvironmentVariable(port);
        port = allowOverrideFromArgs(args, port);
        new QuickSilverServer(port).start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("🚀 QuickSilverServer DB started on port {}", port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log.info("📡 New client connected: {}", clientSocket.getRemoteSocketAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            log.error("❌ Error starting QuickSilverServer on port {}", port, e);
        }
    }

    private void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                log.debug("📩 Received command: {}", line);

                String[] parts = line.split(SPACE);
                String command = parts[0].toUpperCase();
                String[] args = new String[parts.length - 1];
                arraycopy(parts, 1, args, 0, args.length);

                String response = commandRegistry.executeCommand(command, args);
                out.println(response);

                if (command.equalsIgnoreCase(EXIT)) {
                    log.info("🔌 Client disconnected: {}", socket.getRemoteSocketAddress());
                    socket.close();
                    return;
                }
            }
        } catch (IOException e) {
            log.error("❌ Client communication error", e);
        }
    }


    private static int getPort(String[] args) {
        Config config = new Config();
        int port = config.getPortFromConfig();
        port = getPortFromEnvironmentVariable(port);
        port = allowOverrideFromArgs(args, port);
        return port;
    }

    private static int allowOverrideFromArgs(String[] args, int port) {
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                log.error("Invalid command-line port: {}. Using default port {}", args[0], port);
            }
        }
        return port;
    }

    private static int getPortFromEnvironmentVariable(int port) {
        String envPort = getenv(ENV_QUICKSILVER_PORT);
        if (envPort != null) {
            try {
                port = Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                log.error("Invalid QUICKSILVER_PORT value: {}. Using default port {}", envPort, port);
            }
        }
        return port;
    }

}

