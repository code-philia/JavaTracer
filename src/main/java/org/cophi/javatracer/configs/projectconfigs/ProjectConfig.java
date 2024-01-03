package org.cophi.javatracer.configs.projectconfigs;

import org.cophi.javatracer.configs.javatracer.JavaHome;
import org.cophi.javatracer.exceptions.ProjectConfigException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.testcase.TestCase;

public class ProjectConfig {

    protected final static String TEST_CASE_LAUNCH_CLASS = "testcase.runner.AbstractTestCaseRunner";
    protected String projectRootPath = null;
    protected JavaHome javaHome = null;
    protected List<String> classPaths = new LinkedList<>();
    protected String launchClass = null;
    protected boolean isRunningTestCase = false;
    protected TestCase testCase = null;
    protected String sourceCodePath = null;
    protected String testCodePath = null;
    protected List<String> includedClassNames = new LinkedList<>();
    protected List<String> excludedClassName = new LinkedList<>();
    protected JavaBuildTools javaBuildTools = JavaBuildTools.DEFAULT;

    public ProjectConfig() {
    }

    public void addClassPath(final String classPath) {
        this.verifyPathString(classPath);
        this.classPaths.add(classPath);
    }

    public List<String> getClassPaths() {
        return classPaths;
    }

    public void setClassPaths(List<String> classPaths) {
        Objects.requireNonNull(classPaths,
            Log.genMessage("The given classPaths is null.", this.getClass()));
        this.classPaths = classPaths;
    }

    public JavaBuildTools getJavaBuildTools() {
        return this.javaBuildTools;
    }

    public void setJavaBuildTools(final JavaBuildTools javaBuildTools) {
        Objects.requireNonNull(javaBuildTools,
            Log.genMessage("The given java build tools is null.", this.getClass()));
        this.javaBuildTools = javaBuildTools;
    }

    public JavaHome getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(JavaHome javaHome) {
        this.javaHome = javaHome;
    }

    public String getLaunchClass() {
        return this.launchClass;
    }

    public void setLaunchClass(final String launchClass) {
        Objects.requireNonNull(launchClass,
            Log.genMessage("The given launch class is null.", this.getClass()));
        if (launchClass.isEmpty()) {
            throw new IllegalArgumentException(
                Log.genMessage("The given launch class is empty.", this.getClass()));
        }
        this.launchClass = launchClass;
    }

    public String getProjectRootPath() {
        return projectRootPath;
    }

    public void setProjectRootPath(String projectRootPath) {
        this.verifyPathString(projectRootPath);
        this.projectRootPath = projectRootPath;
    }

    public String getSourceCodePath() {
        return sourceCodePath;
    }

    public void setSourceCodePath(String sourceCodePath) {
        this.verifyPathString(sourceCodePath);
        this.sourceCodePath = sourceCodePath;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        if (testCase == null) {
            this.setIsTestCase(false);
        } else {
            this.setIsTestCase(true);
            this.setLaunchClass(ProjectConfig.TEST_CASE_LAUNCH_CLASS);
            this.testCase = testCase;
        }
    }

    public String getTestCodePath() {
        return testCodePath;
    }

    public void setTestCodePath(String testCodePath) {
        this.verifyPathString(testCodePath);
        this.testCodePath = testCodePath;
    }

    public boolean isRunningTestCase() {
        return this.isRunningTestCase;
    }

    public void setIsTestCase(final boolean isTestCase) {
        this.isRunningTestCase = isTestCase;
        if (!this.isRunningTestCase) {
            this.testCase = null;
        }
    }

    public void updateFrameworkInfo() {
        throw new UnsupportedOperationException(
            Log.genMessage("This method can only be called by subclasses.", this.getClass()));
    }

    public void verify() {
        if (this.projectRootPath == null || Files.notExists(Paths.get(this.projectRootPath))) {
            throw new ProjectConfigException(
                Log.genMessage("Project root path is not set or invalid.", this.getClass()));
        }
        if (this.javaHome == null || !this.javaHome.isValid()) {
            throw new ProjectConfigException(
                Log.genMessage("Java home is not set or invalid.", this.getClass()));
        }
        if (this.classPaths == null || this.classPaths.isEmpty()) {
            throw new ProjectConfigException(
                Log.genMessage("Class paths are not set.", this.getClass()));
        }
        for (String classPath : this.classPaths) {
            if (Files.notExists(Paths.get(classPath))) {
                throw new ProjectConfigException(
                    Log.genMessage("Class path: " + classPath + " is invalid.", this.getClass()));
            }
        }
        if (this.isRunningTestCase && this.testCase == null) {
            throw new ProjectConfigException(
                Log.genMessage("Test case record is not set.", this.getClass()));
        }
    }

    protected void verifyPathString(final String string) {
        Objects.requireNonNull(Log.genMessage("The given path string is null.", this.getClass()));
        if (string.isEmpty()) {
            throw new IllegalArgumentException(
                Log.genMessage("The given path string is empty.", this.getClass()));
        }
    }

}
