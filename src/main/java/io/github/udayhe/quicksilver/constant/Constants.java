package io.github.udayhe.quicksilver.constant;

public class Constants {

    private Constants() {}

    public static final String OK = "OK";
    public static final String NULL = "NULL";
    public static final String SET = "SET";
    public static final String GET = "GET";
    public static final String DEL = "DEL";
    public static final String FLUSH = "FLUSH";
    public static final String SHUTDOWN = "SHUTDOWN";
    public static final String EXIT = "EXIT";
    public static final String BYE = "BYE";
    public static final String SPACE = " ";

    //Environment-Variables
    public static final String ENV_QUICKSILVER_PORT = "QUICKSILVER_PORT";


    //Config
    public static final String CONFIG_SERVER_PORT = "server.port";
    public static final String CONFIG_FILE_NAME = "config.properties";


    public static final int CORE_POOL_SIZE = 1;
    public static final int DEFAULT_PORT = 6379;
}
