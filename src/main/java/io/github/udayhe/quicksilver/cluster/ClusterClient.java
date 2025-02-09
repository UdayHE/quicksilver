package io.github.udayhe.quicksilver.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static io.github.udayhe.quicksilver.constant.Constants.ERROR;

public class ClusterClient {

    private static final Logger log = LoggerFactory.getLogger(ClusterClient.class);

    private ClusterClient() {}

    public static String sendRequest(ClusterNode node, String command) {
        try (Socket socket = new Socket(node.getHost(), node.getPort());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            log.info("📡 Sending request [{}] to {}", command, node);
            out.println(command);

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }

            return response.toString().trim();
        } catch (IOException e) {
            log.error("❌ Failed to communicate with node: {}", node, e);
        }
        return ERROR;
    }

}
