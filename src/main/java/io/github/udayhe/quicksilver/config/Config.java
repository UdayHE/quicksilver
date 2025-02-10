package io.github.udayhe.quicksilver.config;

import io.github.udayhe.quicksilver.logging.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.github.udayhe.quicksilver.constant.Constants.*;
import static io.github.udayhe.quicksilver.enums.DBType.IN_MEMORY;

public class Config {

    private static final LogManager log = LogManager.getInstance();
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    private Config() { // ✅ Private constructor to prevent external instantiation
        loadProperties();
    }

    public static Config getInstance() { // ✅ Static method to access singleton
        return INSTANCE;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            if (input == null) {
                log.warning("⚠️ config.properties not found. Using default values.");
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            log.error("❌ Error reading config.properties. Using default values."+ e);
        }
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty(CONFIG_SERVER_PORT, String.valueOf(DEFAULT_PORT)));
    }

    public String getDBType() {
        return properties.getProperty(CONFIG_DB_TYPE, IN_MEMORY.name());
    }

    public int getTotalShards() {
        return Integer.parseInt(properties.getProperty(CONFIG_DB_TOTAL_SHARD, String.valueOf(DEFAULT_TOTAL_SHARD)));
    }

    public int getShardSize() {
        return Integer.parseInt(properties.getProperty(CONFIG_DB_SHARD_SIZE, String.valueOf(DEFAULT_SHARD_SIZE)));
    }
}


