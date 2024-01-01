package configs;

import groovy.lang.Singleton;
import log.LogType;

@Singleton
public class JavaTracerConfig {

    public static final int DEFAULT_STEP_LIMIT = 100000;
    public static final int DEFAULT_DEBUG_PORT = 8000;
    public static final int DEFAULT_VARIABLE_LAYER = 2;
    public static final int DEFAULT_DISTRIBUTION_LAYER = 3;
    public static final LogType DEFAULT_LOG_TYPE = LogType.INFO;
    public static final boolean DEFAULT_IS_DEBUG_MODE = false;
    public static final boolean DEFAULT_APPLY_LIBRARY_OPTIMIZATION = false;
    public boolean isDebugMode;
    public boolean applyLibraryOptimization;
    public LogType logType;
    protected int stepLimit;
    protected int debugPort;
    protected int variableLayer;
    protected int distributionLayer;
    protected String dumpFolder;
    protected String javaTracerJarPath;

    private JavaTracerConfig() {
        this.stepLimit = DEFAULT_STEP_LIMIT;
        this.isDebugMode = DEFAULT_IS_DEBUG_MODE;
        this.debugPort = DEFAULT_DEBUG_PORT;
        this.variableLayer = DEFAULT_VARIABLE_LAYER;
        this.applyLibraryOptimization = DEFAULT_APPLY_LIBRARY_OPTIMIZATION;
        this.distributionLayer = DEFAULT_DISTRIBUTION_LAYER;
        this.logType = DEFAULT_LOG_TYPE;
        this.javaTracerJarPath = JavaTracerConfig.detectJavaTracerJarPath();
    }

    public static String detectJavaTracerJarPath() {
        return JavaTracerConfig.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    public static JavaTracerConfig getInstance() {
        return InstanceHolder.instance;
    }

    public String getJavaTracerJarPath() {
        return this.javaTracerJarPath;
    }

    private static final class InstanceHolder {

        private static final JavaTracerConfig instance = new JavaTracerConfig();
    }

}
