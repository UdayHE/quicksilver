package io.github.udayhe.quicksilver.db;

import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.implementation.InMemoryDB;
import io.github.udayhe.quicksilver.db.implementation.ShardedDB;
import io.github.udayhe.quicksilver.enums.DBType;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.udayhe.quicksilver.constant.Constants.*;

public class DatabaseFactory {
    private static final Logger log = Logger.getLogger(DatabaseFactory.class.getName());
    private static final String UNKNOWN_DB_TYPE_ERROR = "Unknown DBType: ";

    private DatabaseFactory() {
    }

    /**
     * Creates the appropriate DB instance based on DBType
     *
     * @param dbType The database type
     * @param <K>    Type of the key
     * @param <V>    Type of the value
     * @return The database instance
     */
    public static <K, V> DB<K, V> createDatabase(DBType dbType) {
        Config config = Config.getInstance();

        switch (dbType) {
            case IN_MEMORY -> {
                InMemoryDB<K, V> inMemoryDB = new InMemoryDB<>(config.getLRUMaxSize());
                inMemoryDB.setEvictionListener((key, value) ->
                        log.log(Level.INFO, "🔥 Key Evicted: {0} -> {1}", new Object[]{key, value}));
                loadDatabaseFromDisk(inMemoryDB, BACKUP_DB);
                return inMemoryDB;
            }
            case SHARDED -> {
                ShardedDB<K, V> shardedDB = new ShardedDB<>(config.getTotalShards(), config.getShardSize());
                loadDatabaseFromDisk(shardedDB, SHARDED_BACKUP);
                return shardedDB;
            }
            default -> throw new IllegalArgumentException(UNKNOWN_DB_TYPE_ERROR + dbType);
        }
    }

    /**
     * Loads the database state from a backup file.
     *
     * @param db       The database instance
     * @param fileName Backup file name
     * @param <K>      Type of the key
     * @param <V>      Type of the value
     */
    private static <K, V> void loadDatabaseFromDisk(DB<K, V> db, String fileName) {
        db.loadFromDisk(fileName);
    }
}