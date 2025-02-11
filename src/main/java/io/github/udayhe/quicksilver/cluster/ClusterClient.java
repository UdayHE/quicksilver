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

    private ClusterClient() {
    }

    public static String sendRequest(ClusterNode node, String command) {
        try (Socket socket = new Socket(node.host(), node.port());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            log.log(Level.INFO, "📡 Sending request [{0}] to {1}", new Object[]{command, node});
            out.println(command);

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null)
                response.append(line).append(NEW_LINE);

            return response.toString().trim();
        } catch (IOException e) {
            log.log(Level.SEVERE, "❌ Failed to communicate with node: {0} exception: {1}", new Object[]{node, e});
        }
        return ERROR;
    }

}
