package io.github.udayhe.quicksilver.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.*;
import static io.github.udayhe.quicksilver.enums.DBType.IN_MEMORY;

/**
 * Singleton configuration class that loads application properties from a file
 * (`config.properties`) and provides default values when properties are unavailable.
 */
public class Config {

    private static final Config INSTANCE = new Config();
    private static final Logger log = Logger.getLogger(Config.class.getName());

    private final Properties properties = new Properties();


    private static final String CONFIG_FILE_NOT_FOUND = "⚠️ config.properties not found. Using default values.";
    private static final String CONFIG_FILE_ERROR = "❌ Error reading config.properties. Using default values.";

    private Config() {
        loadProperties();
    }


    public static Config getInstance() {
        return INSTANCE;
    }


    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            if (input == null) {
                logWarning();
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            logSevere(e);
        }
    }

    private int getIntProperty(String key, int defaultValue) {
        return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    public int getServerPort() {
        return getIntProperty(CONFIG_SERVER_PORT, DEFAULT_PORT);
    }

    public String getDatabaseType() {
        return properties.getProperty(CONFIG_DB_TYPE, IN_MEMORY.name());
    }

    public int getTotalShards() {
        return getIntProperty(CONFIG_DB_TOTAL_SHARD, DEFAULT_TOTAL_SHARD);
    }

    public int getShardSize() {
        return getIntProperty(CONFIG_DB_SHARD_SIZE, DEFAULT_SHARD_SIZE);
    }

    public int getThreadPoolSize() {
        return getIntProperty(CONFIG_THREAD_POOL_SIZE, DEFAULT_THREAD_POOL_SIZE);
    }

    public int getLRUMaxSize() {
        return getIntProperty(CONFIG_LRU_MAX_SIZE, DEFAULT_LRU_MAX_SIZE);
    }

    public int getThreadPoolTerminationTimeout() {
        return getIntProperty(THREAD_POOL_TERMINATION_TIMEOUT, DEFAULT_TERMINATION_TIMEOUT);
    }


    private void logWarning() {
        log.log(Level.WARNING, Config.CONFIG_FILE_NOT_FOUND);
    }

    private void logSevere(Exception e) {
        log.log(Level.SEVERE, Config.CONFIG_FILE_ERROR, e);
    }
}