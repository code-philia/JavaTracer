package org.cophi.javatracer.core;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import org.cophi.javatracer.configs.JavaTracerAgentParameters;
import org.cophi.javatracer.configs.JavaTracerConfig;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.instrumentation.agents.JavaTracerAgent;
import org.cophi.javatracer.instrumentation.agents.JavaTracerAgentFactory;
import org.cophi.javatracer.instrumentation.filters.JavaTracerFilter;
import org.cophi.javatracer.log.LogConfig;

public class Premain {

    public static void premain(String argentArgs, Instrumentation inst)
        throws UnmodifiableClassException {

        JavaTracerAgentParameters parameters = JavaTracerAgentParameters.parse(argentArgs);
        JavaTracerConfig.getInstance().update(parameters);
        LogConfig.getInstance().update(parameters);

        ProjectConfig projectConfig = new ProjectConfig();
        projectConfig.update(parameters);

        JavaTracerFilter.getInstance().initialize(projectConfig);

        JavaTracerAgent agent = JavaTracerAgentFactory.createAgent(
            JavaTracerConfig.getInstance().getAgentType(), projectConfig, inst);

        agent.addTransformers();

        Class<?>[] classes = inst.getAllLoadedClasses();
        for (Class<?> clazz : classes) {
            if (inst.isModifiableClass(clazz) && inst.isRetransformClassesSupported()
                && !ClassLoader.class.equals(clazz) && !Thread.class
                .equals(clazz)) {
                inst.retransformClasses(clazz);
            }
        }
    }
}
