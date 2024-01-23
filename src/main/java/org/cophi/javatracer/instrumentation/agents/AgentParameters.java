package org.cophi.javatracer.instrumentation.agents;

import org.cophi.javatracer.configs.JavaTracerAgentParameters;


public interface AgentParameters {

    JavaTracerAgentParameters genParameters();

    void update(JavaTracerAgentParameters parameters);
}
