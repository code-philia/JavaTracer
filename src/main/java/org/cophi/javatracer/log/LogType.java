package org.cophi.javatracer.log;

/**
 * Log type indicating the level of the log.
 */
public enum LogType {
    /**
     * Fetal log type indicate severer error events that lead the application to abort.
     */
    FETAL,
    /**
     * Error log type indicate error events that might still allow the application to continue
     * running.
     */
    ERROR,
    /**
     * Warn log type indicate potentially harmful situations.
     */
    WARN,
    /**
     * Debug log type indicate debug information.
     */
    DEBUG,
    /**
     * Info log type indicate informational messages that highlight the progress of the application
     * at coarse-grained level.
     */
    INFO,
    /**
     * Print all type of logging.
     */
    ALL,
    /**
     * Turn off all logging.
     */
    OFF;

    /**
     * Convert the log type to the corresponding level.
     *
     * @return the level of the log type.
     */
    public int toLevel() {
        return switch (this) {
            case ALL -> 0;
            case DEBUG -> 1;
            case INFO -> 2;
            case WARN -> 3;
            case ERROR -> 4;
            case FETAL -> 5;
            case OFF -> 6;
        };
    }
}
