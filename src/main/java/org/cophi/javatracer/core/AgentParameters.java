package org.cophi.javatracer.core;

import org.cophi.javatracer.configs.JavaTracerAgentParameters;


public interface AgentParameters {

    JavaTracerAgentParameters genParameters();

    void update(JavaTracerAgentParameters parameters);
}
