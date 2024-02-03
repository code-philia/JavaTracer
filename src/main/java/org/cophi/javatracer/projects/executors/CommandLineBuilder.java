package org.cophi.javatracer.projects.executors;

import java.util.ArrayList;
import java.util.List;
import org.cophi.javatracer.configs.JavaTracerAgentParameters;
import org.cophi.javatracer.configs.JavaTracerConfig;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.log.LogConfig;

public class CommandLineBuilder {

    protected static final String REMOVE_JAVA_FORMAT = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:%s";

    public static List<String> buildCommand(final ProjectConfig projectConfig) {

        final JavaTracerConfig javaTracerConfig = JavaTracerConfig.getInstance();

        List<String> commands = new ArrayList<>();

        final String javaPath = projectConfig.getJavaHome().getExePath();
        commands.add(javaPath);

        final String projectRootPath = projectConfig.getProjectRootPath();
        commands.add("-noverify");
        commands.add("-Xmx30g");
        commands.add("-XX:+UseG1GC");
        commands.add("-Duser.dir=" + projectRootPath);

        if (javaTracerConfig.isDebugMode) {
            commands.add(String.format(REMOVE_JAVA_FORMAT, javaTracerConfig.getDebugPort()));
        }

        final String javaTracerJarPath = javaTracerConfig.getJavaTracerJarPath();
        commands.add("-javaagent:" + javaTracerJarPath + "="
            + CommandLineBuilder.constructJavaTraceAgentParameter(projectConfig));

        commands.add("-cp");
        commands.add(String.join(";", projectConfig.getClasspaths()));
        commands.add(projectConfig.getLaunchClass());

        if (projectConfig.isRunningTestCase()) {
            commands.add(projectConfig.getTestCase().testClassName);
            commands.add(projectConfig.getTestCase().testMethodName);
            commands.add(String.valueOf(projectConfig.getTestCase().testCaseType));
        }

        return commands;
    }

    protected static String constructJavaTraceAgentParameter(ProjectConfig projectCOnfig) {
        JavaTracerAgentParameters parameters = new JavaTracerAgentParameters();
        parameters.update(JavaTracerConfig.getInstance().genParameters());
        parameters.update(projectCOnfig.genParameters());
        parameters.update(LogConfig.getInstance().genParameters());
        return parameters.toString();
    }

}
