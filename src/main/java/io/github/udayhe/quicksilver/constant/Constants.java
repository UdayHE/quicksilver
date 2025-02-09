package io.github.udayhe.quicksilver.constant;

public class Constants {

    private Constants() {}

    public static final String OK = "OK";
    public static final String BYE = "BYE";
    public static final String SPACE = " ";
    public static final String BACKUP_DB = "backup.db";
    public static final String SHARDED_BACKUP = "sharded_backup";
    public static final String LOCALHOST = "localhost";

    //Environment-Variables
    public static final String ENV_QUICKSILVER_PORT = "QUICKSILVER_PORT";


    //Config
    public static final String CONFIG_SERVER_PORT = "server.port";
    public static final String CONFIG_FILE_NAME = "config.properties";
    public static final String CONFIG_DB_TYPE = "db.type";
    public static final String CONFIG_DB_TOTAL_SHARD = "db.shard.total";
    public static final String CONFIG_DB_SHARD_SIZE = "db.shard.size";


    public static final int CORE_POOL_SIZE = 1;
    public static final int DEFAULT_PORT = 6379;
    public static final int DEFAULT_SHARD_SIZE = 50;
    public static final int DEFAULT_TOTAL_SHARD = 4;
    public static final int LRU_MAX_SIZE = 100;
}
