package io.github.udayhe.quicksilver.logging.implementation;

import io.github.udayhe.quicksilver.enums.LogLevel;
import io.github.udayhe.quicksilver.logging.Logger;
import io.github.udayhe.quicksilver.util.LogUtil;

public class ConsoleLogger implements Logger {

    @Override
    public void log(LogLevel level, String message) {
        System.out.println(LogUtil.formatMessage(level, message));
    }
}