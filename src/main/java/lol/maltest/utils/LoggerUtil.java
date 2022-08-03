package lol.maltest.utils;

import lol.maltest.MalHubServer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {
    public static void info(String message) {
        LoggerFactory.getLogger(LoggerUtil.class).info(message);
    }

    public static void warn(String message) {
        LoggerFactory.getLogger(LoggerUtil.class).warn(message);
    }

    public static void error(String message) {
        LoggerFactory.getLogger(LoggerUtil.class).error(message);
    }
}
