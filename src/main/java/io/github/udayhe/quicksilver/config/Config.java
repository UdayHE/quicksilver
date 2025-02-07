package io.github.udayhe.quicksilver.config;

import io.github.udayhe.quicksilver.QuickSilverServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.github.udayhe.quicksilver.constant.Constants.*;

public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    public int getPortFromConfig() {
        Properties properties = new Properties();

        try (InputStream input = QuickSilverServer.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            if (input == null) {
                log.error("config.properties not found. Using default port 6379");
                return DEFAULT_PORT;
            }
            properties.load(input);
            return Integer.parseInt(properties.getProperty(CONFIG_SERVER_PORT, String.valueOf(DEFAULT_PORT)));
        } catch (IOException | NumberFormatException e) {
            log.error("Error reading config.properties. Using default port 6379", e);
            return DEFAULT_PORT;
        }
    }

}
