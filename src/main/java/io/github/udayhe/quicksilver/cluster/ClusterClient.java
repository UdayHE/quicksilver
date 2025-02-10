package io.github.udayhe.quicksilver.cluster;

import io.github.udayhe.quicksilver.logging.LogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static io.github.udayhe.quicksilver.constant.Constants.ERROR;
import static io.github.udayhe.quicksilver.constant.Constants.NEW_LINE;

public class ClusterClient {

    private static final LogManager log = LogManager.getInstance();

    private ClusterClient() {
    }

    public static String sendRequest(ClusterNode node, String command) {
        try (Socket socket = new Socket(node.host(), node.port());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            log.info("📡 Sending request [" + command + "] to " + node);
            out.println(command);

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null)
                response.append(line).append(NEW_LINE);

            return response.toString().trim();
        } catch (IOException e) {
            log.error("❌ Failed to communicate with node: " + node + " exception:" + e);
        }
        return ERROR;
    }

}
