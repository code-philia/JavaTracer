package org.cophi.javatracer.instrumentation.agents;

import java.lang.instrument.Instrumentation;
import org.cophi.javatracer.configs.ProjectConfig;

public class JavaTracerAgentFactory {

    public static JavaTracerAgent createAgent(final AgentType agentType,
        final ProjectConfig projectConfig, Instrumentation instrumentation) {
        return switch (agentType) {
            case DEFAULT_AGENT -> DefaultAgent.getInstance(projectConfig, instrumentation);
        };
    }

}
