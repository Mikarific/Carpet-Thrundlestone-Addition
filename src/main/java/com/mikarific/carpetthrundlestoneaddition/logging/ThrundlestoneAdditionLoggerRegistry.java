package com.mikarific.carpetthrundlestoneaddition.logging;

import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;

public class ThrundlestoneAdditionLoggerRegistry {
    public static boolean __asyncUpdates;
    public static boolean __dungeons;

    public static void registerLoggers() {
        registerLogger("asyncUpdates", standardLogger("asyncUpdates", "all", new String[]{"thread", "skips", "all"}, true));
        registerLogger("dungeons", standardLogger("dungeons", "all", new String[]{"spawner", "chest", "all"}, true));
    }

    public static void registerLogger(String name, Logger logger) {
        LoggerRegistry.registerLogger(name, logger);
    }

    public static Logger standardLogger(String logName, String def, String [] options, boolean strictOptions) {
        try {
            return new Logger(ThrundlestoneAdditionLoggerRegistry.class.getField("__" + logName), logName, def, options, strictOptions);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to create logger " + logName);
        }
    }
}