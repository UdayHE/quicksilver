package io.github.udayhe.quicksilver.util;

import io.github.udayhe.quicksilver.enums.LogLevel;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {

  private LogUtil() {}

  public static String formatMessage(LogLevel level, String message) {
    return String.format("[%s] [%s] [%s]", getCurrentTime(), level, message);
  }

  private static String getCurrentTime() {
    return new SimpleDateFormat("yyyy-MM-dd HH::mm:ss").format(new Date());
  }
}