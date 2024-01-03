package org.cophi.javatracer.instrumentation;

import org.cophi.javatracer.instrumentation.AgentParams.LogType;
import org.cophi.javatracer.instrumentation.runtime.ExecutionTracer;
import java.util.List;

public class AgentLogger {

    private static boolean enableDebug;
    private static boolean enableError;
    private static boolean enableInfo;
    private static boolean printProgress;

    public static void setup(boolean info, boolean debug, boolean error, boolean progress) {
        enableDebug = debug;
        enableError = error;
        printProgress = progress;
        enableInfo = info;
    }

    public static void setup(List<LogType> logTypes) {
        setup(logTypes.contains(LogType.info),
            logTypes.contains(LogType.debug),
            logTypes.contains(LogType.error),
            logTypes.contains(LogType.printProgress));
    }

    public static void debug(String msg) {
        if (enableDebug) {
            log(msg);
        }
    }

    public static void error(Throwable t) {
        if (enableError) {
            t.printStackTrace();
        }
    }

    private static void log(String msg) {
        System.out.print(AgentConstants.LOG_HEADER);
        System.out.println(msg);
    }

    public static void info(String msg) {
        if (enableInfo) {
            log(msg);
        }
    }

    public static void printProgress(int curStep) {
        if (printProgress) {
            int totalSteps =
                ExecutionTracer.expectedSteps == Integer.MAX_VALUE ? ExecutionTracer.stepLimit
                    : ExecutionTracer.expectedSteps;
            System.out.println(AgentConstants.PROGRESS_HEADER
                + curStep + " " + totalSteps + " ");
        }
    }

}
