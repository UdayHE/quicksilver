package io.github.udayhe.quicksilver.cluster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.ERROR;
import static io.github.udayhe.quicksilver.constant.Constants.NEW_LINE;

public class ClusterClient {

    private static final Logger log = Logger.getLogger(ClusterClient.class.getName());
    private static final String LINE_SEPARATOR = NEW_LINE;

    private ClusterClient() {
    }

    public static String sendRequest(ClusterNode node, String command) {
        try {
            return executeRequest(node, command);
        } catch (IOException e) {
            log.log(Level.SEVERE, "❌ Communication failure with node {0}. Exception: {1}", new Object[]{node, e});
            return ERROR;
        }
    }

    private static String executeRequest(ClusterNode node, String command) throws IOException {
        try (Socket socket = new Socket(node.host(), node.port());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            log.log(Level.INFO, "📡 Sending command [{0}] to node {1}", new Object[]{command, node});
            out.println(command);

            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                responseBuilder.append(line).append(LINE_SEPARATOR);
            }

            log.log(Level.INFO, "✅ Received response from node {0}", node);
            return responseBuilder.toString().trim();
        }
    }
}