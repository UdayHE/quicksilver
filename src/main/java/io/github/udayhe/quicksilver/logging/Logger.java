package io.github.udayhe.quicksilver.logging;

import io.github.udayhe.quicksilver.enums.LogLevel;

public interface Logger {
    void log(LogLevel level, String message);
}