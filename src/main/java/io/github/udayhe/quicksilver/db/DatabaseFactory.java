package io.github.udayhe.quicksilver.db;

import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.implementation.InMemoryDB;
import io.github.udayhe.quicksilver.db.implementation.ShardedDB;
import io.github.udayhe.quicksilver.enums.DBType;

import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.*;

public class DatabaseFactory {

    private static final Logger log = Logger.getLogger(DatabaseFactory.class.getName());

    private DatabaseFactory() {
    }

    /**
     * Creates the appropriate DB instance based on DBType
     *
     * @param dbType The database type
     * @return The database instance
     */
    public static <K, V> DB<K, V> createDatabase(DBType dbType) {
        switch (dbType) {
            case IN_MEMORY -> {
                InMemoryDB<K, V> db = new InMemoryDB<>(LRU_MAX_SIZE);
                db.loadFromDisk(BACKUP_DB);
                db.setEvictionListener((key, value) -> log.info("🔥 Key Evicted: " + key + " -> " + value));
                return db;
            }
            case SHARDED -> {
                Config config = Config.getInstance();
                int numShards = config.getTotalShards();
                int shardSize = config.getShardSize();

                ShardedDB<K, V> shardedDB = new ShardedDB<>(numShards, shardSize);
                shardedDB.loadFromDisk(SHARDED_BACKUP);
                return shardedDB;
            }
            default -> throw new IllegalArgumentException("Unknown DBType: " + dbType);
        }
    }
}
