package org.cophi.javatracer.instrumentation.agents;

import java.lang.instrument.Instrumentation;
import org.cophi.javatracer.configs.ProjectConfig;

public abstract class JavaTracerAgent {

    protected final ProjectConfig projectConfig;

    protected final Instrumentation instrumentation;

    public JavaTracerAgent(final ProjectConfig projectConfig,
        final Instrumentation instrumentation) {
        this.projectConfig = projectConfig;
        this.instrumentation = instrumentation;
    }

    public abstract void addTransformers();

    public abstract void removeTransformers();

}
