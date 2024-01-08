package org.cophi.javatracer;

import org.cophi.javatracer.configs.JavaHome;
import org.cophi.javatracer.configs.MavenProjectConfig;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.exceptions.ProjectNotCompilableException;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.model.Trace;
import org.cophi.javatracer.projects.executors.ProjectExecutor;
import org.cophi.javatracer.testcase.TestCase;

public class JavaTracer {

    protected ProjectConfig config;

    public JavaTracer(final ProjectConfig config) {
        this.config = config;
    }

    public static void main(String[] args) throws ProjectNotCompilableException {
        JavaHome javaHome = new JavaHome("C:\\Program Files\\Java\\jdk-17");
        System.out.println(javaHome);
        MavenProjectConfig config = new MavenProjectConfig(
            "C:\\Users\\WYK\\IdeaProjects\\JavaTracer\\src\\test\\bugs\\lang22",
            true);
        config.setJavaHome(javaHome);
        config.setLaunchClass("org.example.Main");

        TestCase testCase = new TestCase("org.apache.commons.lang3.math.FractionTest",
            "testReducedFactory_int_int");
        config.setTestCase(testCase);

        ProjectExecutor executor = new ProjectExecutor(config);
        executor.executeProject();
    }

    public Trace genMainTrace() {
        if (this.config == null) {
            throw new IllegalStateException(
                Log.genMessage("Project config is not set", this.getClass()));
        }

        this.config.verify();

        return null;
    }

    public ProjectConfig getConfig() {
        return config;
    }

    public void setConfig(ProjectConfig config) {
        this.config = config;
    }
}
