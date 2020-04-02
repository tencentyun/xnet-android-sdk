package com.tencent.qcloud;

public abstract class LoggerCallback {

    private static LoggerCallback logger = null;

    public static void setLoggerCallback(LoggerCallback aLoggerCallback) {
        logger = aLoggerCallback;
    }

    public static void verbose(String tag, String msg) {
        if (logger != null) {
            logger.v(tag, msg);
        }
    }

    public static void info(String tag, String msg) {
        if (logger != null) {
            logger.i(tag, msg);
        }
    }

    public static void debug(String tag, String msg) {
        if (logger != null) {
            logger.d(tag, msg);
        }
    }

    public static void warn(String tag, String msg) {
        if (logger != null) {
            logger.w(tag, msg);
        }

    }

    public static void error(String tag, String msg) {
        if (logger != null) {
            logger.e(tag, msg);
        }
    }

    abstract public void v(String tag, String msg);

    abstract public void d(String tag, String msg);

    abstract public void i(String tag, String msg);

    abstract public void w(String tag, String msg);

    abstract public void e(String tag, String msg);
}
