package org.cophi.javatracer.log;

/**
 * Log utility class.
 */
public class Log {

    /**
     * The default log type.
     */
    public static final LogType DEFAULT_LOG_TYPE = LogType.INFO;
    /**
     * The current log type. <br/> The accessor is set to package private so that only the LogConfig
     * can update it. <br/> The purpose is to ensure that the log type is consistent with LogConfig.
     * <br/> This design also prevent Log class depends on any other classes.
     *
     * @see LogConfig
     */
    static LogType currentLogType = Log.DEFAULT_LOG_TYPE;

    /**
     * Generate the log message in following format: <br/> [logType | className]: message
     *
     * @param message   the message to be logged
     * @param className the name of the class that the log is generated from
     * @param logType   the type of the log
     * @return the generated log message
     */
    public static String genMessage(final String message, final String className,
        final LogType logType) {
        return "[" + logType + " | " + className + "]: " + message;
    }

    /**
     * Generate the log message in following format: <br/> [logType | className]: message
     *
     * @param message the message to be logged
     * @param clazz   the class that the log is generated from
     * @param logType the type of the log
     * @return the generated log message
     */
    public static String genMessage(final String message, final Class<?> clazz,
        final LogType logType) {
        return genMessage(message, clazz.getName(), logType);
    }

    /**
     * Generate the log message in following format: <br/> [className]: message
     *
     * @param message   the message to be logged
     * @param className the name of the class that the log is generated from
     * @return the generated log message
     */
    public static String genMessage(final String message, final String className) {
        return "[" + className + "]: " + message;
    }

    /**
     * Generate the log message in following format: <br/> [className]: message
     *
     * @param message the message to be logged
     * @param clazz   the class that the log is generated from
     * @return the generated log message
     */
    public static String genMessage(final String message, final Class<?> clazz) {
        return genMessage(message, clazz.getName());
    }

    /**
     * Generate the log message in following format: <br/> [logType]: message
     *
     * @param message the message to be logged
     * @param logType the type of the log
     * @return the generated log message
     */
    public static String genMessage(final String message, final LogType logType) {
        return "[" + logType + "]: " + message;
    }

    /**
     * Generate the log message in following format: <br/> [INFO]: message
     *
     * @param message the message to be logged
     * @return the generated log message
     */
    public static String genMessage(final String message) {
        return genMessage(message, LogType.INFO);
    }

    /**
     * Check if the given log type should be logged.
     *
     * @param logType the log type to be checked
     * @return {@code True} if the given log type should be logged, {@code False} otherwise
     */
    protected static boolean shouldLog(final LogType logType) {
        return logType.toLevel() <= Log.currentLogType.toLevel();
    }

    protected static void log(final String message) {
        System.out.println(message);
    }

    /**
     * Log the given message with info level. <br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message   the message to be logged
     * @param className the name of the class that the log is generated from
     */
    public static void info(final String message, final String className) {
        if (Log.shouldLog(LogType.INFO)) {
            Log.log(genMessage(message, className, LogType.INFO));
        }
    }

    /**
     * Log the given message with info level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @param clazz   the class that the log is generated from
     */
    public static void info(final String message, final Class<?> clazz) {
        Log.info(message, clazz.getName());
    }

    /**
     * Log the given message with info level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     */
    public static void info(final String message) {
        if (Log.shouldLog(LogType.INFO)) {
            Log.log(Log.genMessage(message, LogType.INFO));
        }
    }

    /**
     * Log the given message with debug level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message   the message to be logged
     * @param className the name of the class that the log is generated from
     */
    public static void debug(final String message, final String className) {
        if (Log.shouldLog(LogType.DEBUG)) {
            Log.log(Log.genMessage(message, className, LogType.DEBUG));
        }
    }

    /**
     * Log the given message with debug level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @param clazz   the class that the log is generated from.
     */
    public static void debug(final String message, final Class<?> clazz) {
        Log.debug(message, clazz.getName());
    }

    /**
     * Log the given message with debug level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     */
    public static void debug(final String message) {
        if (Log.shouldLog(LogType.DEBUG)) {
            Log.log(Log.genMessage(message, LogType.DEBUG));
        }
    }

    /**
     * Log the given message with warn level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message   the message to be logged
     * @param className the name of the class that the log is generated from
     */
    public static void warn(final String message, final String className) {
        if (Log.shouldLog(LogType.WARN)) {
            Log.log(Log.genMessage(message, className, LogType.WARN));
        }
    }

    /**
     * Log the given message with warn level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @param clazz   the class that the log is generated from
     */
    public static void warn(final String message, final Class<?> clazz) {
        Log.warn(message, clazz.getName());
    }

    /**
     * Log the given message with warn level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     */
    public static void warn(final String message) {
        if (Log.shouldLog(LogType.WARN)) {
            Log.log(Log.genMessage(message, LogType.WARN));
        }
    }

    /**
     * Log the given message with error level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message   the message to be logged
     * @param className the name of the class that the log is generated from
     */
    public static void error(final String message, final String className) {
        if (Log.shouldLog(LogType.ERROR)) {
            Log.log(Log.genMessage(message, className, LogType.ERROR));
        }
    }

    /**
     * Log the given message with error level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @param clazz   the class that the log is generated from
     */
    public static void error(final String message, final Class<?> clazz) {
        Log.error(message, clazz.getName());
    }

    /**
     * Log the given message with error level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     */
    public static void error(final String message) {
        if (Log.shouldLog(LogType.ERROR)) {
            Log.log(Log.genMessage(message, LogType.ERROR));
        }
    }

    /**
     * Log the given message with fetal level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message   the message to be logged
     * @param className the name of the class that the log is generated from
     */
    public static void fetal(final String message, final String className) {
        if (Log.shouldLog(LogType.FETAL)) {
            Log.log(Log.genMessage(message, className, LogType.FETAL));
        }
    }

    /**
     * Log the given message with fetal level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @param clazz   the class that the log is generated from
     */
    public static void fetal(final String message, final Class<?> clazz) {
        Log.fetal(message, clazz.getName());
    }

    /**
     * Log the given message with fetal level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     */
    public static void fetal(final String message) {
        if (Log.shouldLog(LogType.FETAL)) {
            Log.log(Log.genMessage(message, LogType.FETAL));
        }
    }

    /**
     * Log the given message with flow level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     */
    public static void flow(final String message) {
        if (Log.shouldLog(LogType.FLOW)) {
            Log.log(Log.genMessage(message, LogType.FLOW));
        }
    }

    /**
     * Log the given message with flow level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message   the message to be logged
     * @param className the name of the class that the log is generated from
     */
    public static void flow(final String message, final String className) {
        if (Log.shouldLog(LogType.FLOW)) {
            Log.log(Log.genMessage(message, className, LogType.FLOW));
        }
    }

    /**
     * Log the given message with flow level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @param clazz   the class that the log is generated from
     */
    public static void flow(final String message, final Class<?> clazz) {
        Log.flow(message, clazz.getName());
    }
}
