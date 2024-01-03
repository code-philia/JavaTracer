package org.cophi.javatracer.instrumentation;

import java.lang.instrument.Instrumentation;
import org.cophi.javatracer.configs.javatracer.JavaTracerAgentParameters;
import org.cophi.javatracer.configs.javatracer.JavaTracerConfig;
import org.cophi.javatracer.configs.projectconfigs.ProjectConfig;
import org.cophi.javatracer.instrumentation.transformers.MethodNameTransformer;
import org.cophi.javatracer.log.LogConfig;

public class JavaTracerAgent {

    public static void premain(String argentArgs, Instrumentation inst) {
        JavaTracerAgentParameters parameters = JavaTracerAgentParameters
            .parse(argentArgs);
        JavaTracerConfig.getInstance().update(parameters);
        LogConfig.getInstance().update(parameters);
        ProjectConfig projectConfig = new ProjectConfig();
        projectConfig.update(parameters);
        inst.addTransformer(new MethodNameTransformer(projectConfig));
    }

}
