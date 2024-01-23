package org.cophi.javatracer.configs;

import groovy.lang.Singleton;
import org.cophi.javatracer.instrumentation.agents.AgentParameters;
import org.cophi.javatracer.instrumentation.agents.AgentType;

@Singleton
public class JavaTracerConfig implements AgentParameters {

    public static final int DEFAULT_STEP_LIMIT = 100000;
    public static final int DEFAULT_DEBUG_PORT = 9000;
    public static final int DEFAULT_VARIABLE_LAYER = 2;
    public static final int DEFAULT_DISTRIBUTION_LAYER = 3;
    public static final boolean DEFAULT_IS_DEBUG_MODE = false;
    public static final boolean DEFAULT_APPLY_LIBRARY_OPTIMIZATION = false;
    public static final AgentType DEFAULT_AGENT_TYPE = AgentType.DEFAULT_AGENT;
    public static final long DEFAULT_MAX_METHOD_INSTRUCTION_OFFSET = 65534;
    public static final String DEFAULT_DATASET_FOLDER = "C:\\Users\\WYK\\Desktop\\neo4j-dataset";
    public static final String DEBUG_MODE_KEY = "debug_mode";
    public static final String LOG_TYPE_KEY = "log_type";
    public static final String STEP_LIMIT_KEY = "step_limit";
    public static final String DEBUG_PORT_KEY = "debug_port";
    public static final String VARIABLE_LAYER_KEY = "variable_layer";
    public static final String RUN_ID_KEY = "run_id";
    public static final String AGENT_TYPE_KEY = "agent_type";
    public static final String APPLY_LIBRARY_OPTIMIZATION_KEY = "apply_library_optimization";
    public static final String MAX_METHOD_INSTRUCTION_OFFSET_KEY = "max_method_instruction_offset";
    public static final String DATASET_FOLDER_KEY = "dataset_folder";
    public static final String DISTRIBUTION_LAYER_KEY = "distribution_layer";

    public boolean isDebugMode = JavaTracerConfig.DEFAULT_IS_DEBUG_MODE;
    public boolean applyLibraryOptimization = JavaTracerConfig.DEFAULT_APPLY_LIBRARY_OPTIMIZATION;
    protected int stepLimit = JavaTracerConfig.DEFAULT_STEP_LIMIT;
    protected int debugPort = JavaTracerConfig.DEFAULT_DEBUG_PORT;
    protected int variableLayer = JavaTracerConfig.DEFAULT_VARIABLE_LAYER;
    protected int distributionLayer = JavaTracerConfig.DEFAULT_DISTRIBUTION_LAYER;
    protected String datasetFolder = JavaTracerConfig.DEFAULT_DATASET_FOLDER;
    protected String javaTracerJarPath = JavaTracerConfig.detectJavaTracerJarPath();
    protected long maxMethodInstructionOffset = JavaTracerConfig.DEFAULT_MAX_METHOD_INSTRUCTION_OFFSET;
    protected AgentType agentType = JavaTracerConfig.DEFAULT_AGENT_TYPE;

    private JavaTracerConfig() {
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
        JavaTracerAgentParameters parameters = new JavaTracerAgentParameters();
        parameters.setParameter(JavaTracerConfig.DEBUG_MODE_KEY, String.valueOf(this.isDebugMode));
        parameters.setParameter(JavaTracerConfig.STEP_LIMIT_KEY, String.valueOf(this.stepLimit));
        parameters.setParameter(JavaTracerConfig.DEBUG_PORT_KEY, String.valueOf(this.debugPort));
        parameters.setParameter(JavaTracerConfig.VARIABLE_LAYER_KEY,
            String.valueOf(this.variableLayer));
        parameters.setParameter(JavaTracerConfig.APPLY_LIBRARY_OPTIMIZATION_KEY,
            String.valueOf(this.applyLibraryOptimization));
        parameters.setParameter(JavaTracerConfig.AGENT_TYPE_KEY, this.agentType.toString());
        parameters.setParameter(JavaTracerConfig.MAX_METHOD_INSTRUCTION_OFFSET_KEY,
            String.valueOf(this.maxMethodInstructionOffset));
        parameters.setParameter(JavaTracerConfig.DATASET_FOLDER_KEY, this.datasetFolder);
        parameters.setParameter(JavaTracerConfig.DISTRIBUTION_LAYER_KEY,
            String.valueOf(this.distributionLayer));
        return parameters;
    }

    @Override
    public void update(JavaTracerAgentParameters parameters) {
        this.setDebugMode(
            Boolean.parseBoolean(parameters.getParameter(JavaTracerConfig.DEBUG_MODE_KEY)));
        this.setStepLimit(
            Integer.parseInt(parameters.getParameter(JavaTracerConfig.STEP_LIMIT_KEY)));
        this.setDebugPort(
            Integer.parseInt(parameters.getParameter(JavaTracerConfig.DEBUG_PORT_KEY)));
        this.setVariableLayer(
            Integer.parseInt(parameters.getParameter(JavaTracerConfig.VARIABLE_LAYER_KEY)));
        this.setApplyLibraryOptimization(Boolean.parseBoolean(
            parameters.getParameter(JavaTracerConfig.APPLY_LIBRARY_OPTIMIZATION_KEY)));
        this.setAgentType(
            AgentType.fromString(parameters.getParameter(JavaTracerConfig.AGENT_TYPE_KEY)));
        this.setMaxMethodInstructionOffset(Long.parseLong(
            parameters.getParameter(JavaTracerConfig.MAX_METHOD_INSTRUCTION_OFFSET_KEY)));
        this.setDatasetFolder(parameters.getParameter(JavaTracerConfig.DATASET_FOLDER_KEY));
        this.setDistributionLayer(
            Integer.parseInt(parameters.getParameter(JavaTracerConfig.DISTRIBUTION_LAYER_KEY)));
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public void setAgentType(AgentType agentType) {
        this.agentType = agentType;
    }

    public String getDatasetFolder() {
        return datasetFolder;
    }

    public void setDatasetFolder(String datasetFolder) {
        this.datasetFolder = datasetFolder;
    }

    public int getDebugPort() {
        return debugPort;
    }

    public void setDebugPort(int debugPort) {
        this.debugPort = debugPort;
    }

    public int getDistributionLayer() {
        return distributionLayer;
    }

    public void setDistributionLayer(int distributionLayer) {
        this.distributionLayer = distributionLayer;
    }

    public String getJavaTracerJarPath() {
        return this.javaTracerJarPath;
    }

    public void setJavaTracerJarPath(String javaTracerJarPath) {
        this.javaTracerJarPath = javaTracerJarPath;
    }

    public long getMaxMethodInstructionOffset() {
        return maxMethodInstructionOffset;
    }

    public void setMaxMethodInstructionOffset(long maxMethodInstructionOffset) {
        this.maxMethodInstructionOffset = maxMethodInstructionOffset;
    }

    public int getStepLimit() {
        return stepLimit;
    }

    public void setStepLimit(int stepLimit) {
        this.stepLimit = stepLimit;
    }

    public int getVariableLayer() {
        return variableLayer;
    }

    public void setVariableLayer(int variableLayer) {
        this.variableLayer = variableLayer;
    }

    public boolean isApplyLibraryOptimization() {
        return applyLibraryOptimization;
    }

    public void setApplyLibraryOptimization(boolean applyLibraryOptimization) {
        this.applyLibraryOptimization = applyLibraryOptimization;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public void setDebugMode(boolean debugMode) {
        isDebugMode = debugMode;
    }

    private static final class InstanceHolder {

        private static final JavaTracerConfig instance = new JavaTracerConfig();
    }
}
