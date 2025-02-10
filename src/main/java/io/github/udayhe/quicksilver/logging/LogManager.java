package io.github.udayhe.quicksilver.logging;

import io.github.udayhe.quicksilver.enums.LogLevel;
import io.github.udayhe.quicksilver.logging.implementation.ConsoleLogger;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LogManager {

    private static final LogManager INSTANCE = new LogManager();
    private Logger logger;
    private LogLevel level = LogLevel.INFO;
    private final Lock lock = new ReentrantLock();


    private LogManager() {
        this.logger = new ConsoleLogger();
    }

    public static LogManager getInstance() {
        return INSTANCE;
    }

    public void setLogger(Logger logger) {
        this.lock.lock();
        try {
            this.logger = logger;
        } finally {
            this.lock.unlock();
        }
    }

    public void setLevel(LogLevel level) {
        this.lock.lock();
        try {
            this.level = level;
        } finally {
            this.lock.unlock();
        }
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public void warning(String message) {
        log(LogLevel.WARNING, message);
    }

    public void fatal(String message) {
        log(LogLevel.FATAL, message);
    }

    public void log(LogLevel level, String message) {
        if (level.ordinal() >= this.level.ordinal())
            this.logger.log(level, message);
    }

}