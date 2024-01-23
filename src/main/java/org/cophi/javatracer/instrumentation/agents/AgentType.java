package org.cophi.javatracer.instrumentation.agents;

public enum AgentType {
    DEFAULT_AGENT(DefaultAgent.class);

    final Class<? extends JavaTracerAgent> clazz;

    AgentType(Class<? extends JavaTracerAgent> clazz) {
        this.clazz = clazz;
    }

    public static AgentType fromString(String agentType) {
        for (AgentType type : AgentType.values()) {
            if (type.toString().equalsIgnoreCase(agentType)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown agent type: " + agentType);
    }

    @Override
    public String toString() {
        return this.clazz.getCanonicalName();
    }
}
