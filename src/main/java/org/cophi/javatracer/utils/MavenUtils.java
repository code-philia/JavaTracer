package org.cophi.javatracer.utils;

import org.cophi.javatracer.exceptions.ProjectNotCompilableException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.cophi.javatracer.log.Log;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

public class MavenUtils {

    public static final String SRC_PATH = Paths.get("src", "main", "java").toString();
    public static final String TEST_PATH = Paths.get("src", "test", "java").toString();
    public static final String POM = "pom.xml";
    public static final String TARGET_SRC_PATH = Paths.get("target", "classes").toString();
    public static final String TARGET_TEST_PATH = Paths.get("target", "test-classes").toString();

    private MavenUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getMavenHome() {
        return System.getenv("MAVEN_HOME");
    }

    public static String getSourceCodePath(final String projectRootPath) {
        return Paths.get(projectRootPath, SRC_PATH).toString();
    }

    public static String getTestCodePath(final String projectRootPath) {
        return Paths.get(projectRootPath, TEST_PATH).toString();
    }

    public static String getPomPath(final String projectRootPath) {
        return Paths.get(projectRootPath, POM).toString();
    }

    public static String getTargetSourceCodePath(final String projectRootPath) {
        return Paths.get(projectRootPath, TARGET_SRC_PATH).toString();
    }

    public static String getTargetTestCodePath(final String projectRootPath) {
        return Paths.get(projectRootPath, TARGET_TEST_PATH).toString();
    }

    public static List<String> getClassPaths(final String projectRootPath, final String MAVEN_HOME)
        throws ProjectNotCompilableException {
        if (MAVEN_HOME == null) {
            throw new ProjectNotCompilableException(
                Log.genMessage("Maven is not supported", MavenUtils.class));
        }

        InvocationRequest request = new DefaultInvocationRequest();
        Path pomPath = Path.of(MavenUtils.getPomPath(projectRootPath));
        request.setPomFile(pomPath.toFile());
        request.setGoals(Collections.singletonList("dependency:build-classpath"));

        MavenOutputHandler outputHandler = new MavenOutputHandler();
        request.setOutputHandler(outputHandler);

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(MAVEN_HOME));

        try {
            final InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                throw new IllegalStateException("Build failed.");
            }

            List<String> classPaths = new ArrayList<>();
            for (String output : outputHandler.getOutput()) {
                Collections.addAll(classPaths, output.split(";"));
            }
            return classPaths;
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getClassPaths(final String projectRootPath)
        throws ProjectNotCompilableException {
        return MavenUtils.getClassPaths(projectRootPath, MavenUtils.getMavenHome());
    }

    /**
     * Check if the project is compiled. <br/> Note that it will only check is the .class files
     * exist in the target/classes directory.
     *
     * @param projectRootPath The root path of the project.
     * @return {@code True} if the project is compiled, {@code False} otherwise.
     */
    public static boolean isCompiled(final String projectRootPath) {
        File classesDir = new File(MavenUtils.getTargetSourceCodePath(projectRootPath));
        if (classesDir.exists() && classesDir.isDirectory()) {
            File[] classFiles = classesDir.listFiles((dir, name) -> name.endsWith(".class"));
            return classFiles != null && classFiles.length > 0;
        }
        return false;
    }

    public static void compileTargetProject(final String projectRootPath, final String MAVEN_HOME)
        throws ProjectNotCompilableException {
        if (MAVEN_HOME == null) {
            throw new ProjectNotCompilableException(
                Log.genMessage("Maven is not supported", MavenUtils.class));
        }

        InvocationRequest request = new DefaultInvocationRequest();

        Path pomPath = Path.of(MavenUtils.getPomPath(projectRootPath));
        request.setPomFile(pomPath.toFile());
        request.setGoals(Arrays.asList("-q", "clean", "test-compile"));

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(MAVEN_HOME));

        try {
            final InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                throw new IllegalStateException("Build failed.");
            }
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void compileTargetProject(final String projectRootPath)
        throws ProjectNotCompilableException {
        MavenUtils.compileTargetProject(projectRootPath, MavenUtils.getMavenHome());
    }

    protected static class MavenOutputHandler implements InvocationOutputHandler {

        private final List<String> output = new ArrayList<>();

        @Override
        public void consumeLine(String line) {
            if (!line.startsWith("[")) {
                output.add(line);
            }
        }

        public List<String> getOutput() {
            return output;
        }
    }
}
