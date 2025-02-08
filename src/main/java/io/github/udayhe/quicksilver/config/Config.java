package io.github.udayhe.quicksilver.config;

import io.github.udayhe.quicksilver.Server;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.github.udayhe.quicksilver.constant.Constants.*;
import static io.github.udayhe.quicksilver.db.enums.DBType.IN_MEMORY;
import static org.slf4j.LoggerFactory.getLogger;

public class Config {

    private static final Logger log = getLogger(Config.class);
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = Server.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            if (input == null) {
                log.warn("⚠️ config.properties not found. Using default values.");
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            log.error("❌ Error reading config.properties. Using default values.", e);
        }
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty(CONFIG_SERVER_PORT, String.valueOf(DEFAULT_PORT)));
    }

    public String getDBType() {
        return properties.getProperty(CONFIG_DB_TYPE, IN_MEMORY.name());
    }
}

