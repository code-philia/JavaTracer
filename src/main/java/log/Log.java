package log;

import configs.JavaTracerConfig;

/**
 * Log utility class.
 */
public class Log {

    private Log() {
        throw new IllegalStateException(Log.genMessage("Utility class", this.getClass()));
    }

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
        return logType.toLevel() >= JavaTracerConfig.getInstance().logType.toLevel();
    }

    /**
     * Log the given message with info level. <br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message   the message to be logged
     * @param className the name of the class that the log is generated from
     * @see JavaTracerConfig#logType
     */
    public static void info(final String message, final String className) {
        if (Log.shouldLog(LogType.INFO)) {
            System.out.println(genMessage(message, className, LogType.INFO));
        }
    }

    /**
     * Log the given message with info level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @param clazz   the class that the log is generated from
     * @see JavaTracerConfig#logType
     */
    public static void info(final String message, final Class<?> clazz) {
        Log.info(message, clazz.getName());
    }

    /**
     * Log the given message with info level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @see JavaTracerConfig#logType
     */
    public static void info(final String message) {
        if (Log.shouldLog(LogType.INFO)) {
            System.out.println(Log.genMessage(message, LogType.INFO));
        }
    }

    /**
     * Log the given message with debug level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message   the message to be logged
     * @param className the name of the class that the log is generated from
     * @see JavaTracerConfig#logType
     */
    public static void debug(final String message, final String className) {
        if (Log.shouldLog(LogType.DEBUG)) {
            System.out.println(Log.genMessage(message, className, LogType.DEBUG));
        }
    }

    /**
     * Log the given message with debug level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @param clazz   the class that the log is generated from.
     * @see JavaTracerConfig#logType
     */
    public static void debug(final String message, final Class<?> clazz) {
        Log.debug(message, clazz.getName());
    }

    /**
     * Log the given message with debug level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @see JavaTracerConfig#logType
     */
    public static void debug(final String message) {
        if (Log.shouldLog(LogType.DEBUG)) {
            System.out.println(Log.genMessage(message, LogType.DEBUG));
        }
    }

    /**
     * Log the given message with warn level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message   the message to be logged
     * @param className the name of the class that the log is generated from
     * @see JavaTracerConfig#logType
     */
    public static void warn(final String message, final String className) {
        if (Log.shouldLog(LogType.WARN)) {
            System.out.println(Log.genMessage(message, className, LogType.WARN));
        }
    }

    /**
     * Log the given message with warn level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @param clazz   the class that the log is generated from
     * @see JavaTracerConfig#logType
     */
    public static void warn(final String message, final Class<?> clazz) {
        Log.warn(message, clazz.getName());
    }

    /**
     * Log the given message with warn level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @see JavaTracerConfig#logType
     */
    public static void warn(final String message) {
        if (Log.shouldLog(LogType.WARN)) {
            System.out.println(Log.genMessage(message, LogType.WARN));
        }
    }

    /**
     * Log the given message with error level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message   the message to be logged
     * @param className the name of the class that the log is generated from
     * @see JavaTracerConfig#logType
     */
    public static void error(final String message, final String className) {
        if (Log.shouldLog(LogType.ERROR)) {
            System.out.println(Log.genMessage(message, className, LogType.ERROR));
        }
    }

    /**
     * Log the given message with error level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @param clazz   the class that the log is generated from
     * @see JavaTracerConfig#logType
     */
    public static void error(final String message, final Class<?> clazz) {
        Log.error(message, clazz.getName());
    }

    /**
     * Log the given message with error level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @see JavaTracerConfig#logType
     */
    public static void error(final String message) {
        if (Log.shouldLog(LogType.ERROR)) {
            System.out.println(Log.genMessage(message, LogType.ERROR));
        }
    }

    /**
     * Log the given message with fetal level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message   the message to be logged
     * @param className the name of the class that the log is generated from
     * @see JavaTracerConfig#logType
     */
    public static void fetal(final String message, final String className) {
        if (Log.shouldLog(LogType.FETAL)) {
            System.out.println(Log.genMessage(message, className, LogType.FETAL));
        }
    }

    /**
     * Log the given message with fetal level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @param clazz   the class that the log is generated from
     * @see JavaTracerConfig#logType
     */
    public static void fetal(final String message, final Class<?> clazz) {
        Log.fetal(message, clazz.getName());
    }

    /**
     * Log the given message with fetal level.<br/> Note that it will check the JavaTracerConfig for
     * current logging lever.
     *
     * @param message the message to be logged
     * @see JavaTracerConfig#logType
     */
    public static void fetal(final String message) {
        if (Log.shouldLog(LogType.FETAL)) {
            System.out.println(Log.genMessage(message, LogType.FETAL));
        }
    }
}
