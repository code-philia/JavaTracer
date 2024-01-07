package org.cophi.javatracer.configs;

import groovy.lang.Singleton;
import org.cophi.javatracer.core.AgentParameters;

@Singleton
public class JavaTracerConfig implements AgentParameters {

    public static final int DEFAULT_STEP_LIMIT = 100000;
    public static final int DEFAULT_DEBUG_PORT = 8000;
    public static final int DEFAULT_VARIABLE_LAYER = 2;
    public static final int DEFAULT_DISTRIBUTION_LAYER = 3;
    public static final boolean DEFAULT_IS_DEBUG_MODE = false;
    public static final boolean DEFAULT_APPLY_LIBRARY_OPTIMIZATION = false;
    public static final String DEBUG_MODE_KEY = "debug_mode";
    public static final String LOG_TYPE_KEY = "log_type";
    public static final String STEP_LIMIT_KEY = "step_limit";
    public static final String DEBUG_PORT_KEY = "debug_port";
    public static final String VARIABLE_LAYER_KEY = "variable_layer";
    public static final String PRE_CHECK_KEY = "pre_check";
    public static final String RUN_ID_KEY = "run_id";
    public boolean isDebugMode;
    public boolean applyLibraryOptimization;
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
        this.javaTracerJarPath = JavaTracerConfig.detectJavaTracerJarPath();
    }

    public static String detectJavaTracerJarPath() {
        String javaTracerJarPath = JavaTracerConfig.class.getProtectionDomain().getCodeSource()
            .getLocation().getPath();
        if (javaTracerJarPath.startsWith("/")) {
            javaTracerJarPath = javaTracerJarPath.substring(1);
        }
        return javaTracerJarPath;
    }

    public static JavaTracerConfig getInstance() {
        return InstanceHolder.instance;
    }

    public JavaTracerAgentParameters genParameters() {
        JavaTracerAgentParameters builder = new JavaTracerAgentParameters();
        return builder;
    }

    @Override
    public void update(JavaTracerAgentParameters parameters) {

    }

    public String getJavaTracerJarPath() {
        return this.javaTracerJarPath;
    }

    private static final class InstanceHolder {

        private static final JavaTracerConfig instance = new JavaTracerConfig();
    }

}
