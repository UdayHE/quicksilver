package io.github.udayhe.quicksilver.db;

import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.enums.DBType;
import io.github.udayhe.quicksilver.db.implementation.InMemoryDB;
import io.github.udayhe.quicksilver.db.implementation.ShardedDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.udayhe.quicksilver.constant.Constants.*;

public class DatabaseFactory {
    private static final Logger log = LoggerFactory.getLogger(DatabaseFactory.class);

    /**
     * Creates the appropriate DB instance based on DBType
     *
     * @param dbType The database type
     * @return The database instance
     */
    public static DB<String, String> createDatabase(DBType dbType) {
        switch (dbType) {
            case IN_MEMORY -> {
                InMemoryDB<String, String> db = new InMemoryDB<>(LRU_MAX_SIZE);
                db.loadFromDisk(BACKUP_DB);
                db.setEvictionListener((key, value) -> log.info("🔥 Key Evicted: {} -> {}", key, value));
                return db;
            }
            case SHARDED -> {
                Config config = new Config();
                int numShards = config.getTotalShards();
                int shardSize = config.getShardSize();

                ShardedDB<String, String> shardedDB = new ShardedDB<>(numShards, shardSize);
                shardedDB.loadFromDisk(SHARDED_BACKUP);
                return shardedDB;
            }
            default -> throw new IllegalArgumentException("Unknown DBType: " + dbType);
        }
    }
}
