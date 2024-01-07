package org.cophi.javatracer.log;

import org.cophi.javatracer.configs.JavaTracerAgentParameters;
import org.cophi.javatracer.core.AgentParameters;

public class LogConfig implements AgentParameters {

    public static final LogType DEFAULT_LOG_TYPE = LogType.INFO;
    public static final String LOG_TYPE_KEY = "log_type";

    protected LogType logType;

    private LogConfig() {
        this.logType = DEFAULT_LOG_TYPE;
    }

    public static LogConfig getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    public JavaTracerAgentParameters genParameters() {
        JavaTracerAgentParameters parameters = new JavaTracerAgentParameters();
        parameters.setParameter(LogConfig.LOG_TYPE_KEY, logType.name());
        return parameters;
    }

    @Override
    public void update(JavaTracerAgentParameters parameters) {
        this.setLogType(LogType.valueOf(parameters.getParameter(LogConfig.LOG_TYPE_KEY)));
    }

    public void setLogType(final LogType logType) {
        this.logType = logType;
        Log.currentLogType = logType;
    }

    private static final class InstanceHolder {

        private static final LogConfig instance = new LogConfig();
    }


}
