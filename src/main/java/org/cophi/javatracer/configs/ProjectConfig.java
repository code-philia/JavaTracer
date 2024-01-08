package org.cophi.javatracer.configs;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.cophi.javatracer.core.AgentParameters;
import org.cophi.javatracer.exceptions.ProjectConfigException;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.testcase.TestCase;
import org.cophi.javatracer.testcase.runner.AbstractTestCaseRunner;

public class ProjectConfig implements AgentParameters {

    public static final String WORKING_DIR_KEY = "working_dir";
    public static final String LAUNCH_CLASS_KEY = "launch_class";
    public static final String JAVA_HOME_KEY = "java_home";
    public static final String IS_TEST_CASE_KEY = "is_test_case";
    public static final String TEST_CASE_CLASS_NAME_KEY = "test_case_class_name";
    public static final String TEST_CASE_METHOD_NAME_KEY = "test_case_method_name";

    protected final static String TEST_CASE_LAUNCH_CLASS = AbstractTestCaseRunner.class.getCanonicalName();
    protected final static String NULL = "null";
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

    public JavaTracerAgentParameters genParameters() {
        JavaTracerAgentParameters parameters = new JavaTracerAgentParameters();
        parameters.setParameter(ProjectConfig.JAVA_HOME_KEY,
            this.javaHome.path());
        parameters.setParameter(ProjectConfig.LAUNCH_CLASS_KEY,
            this.getLaunchClass());
        parameters.setParameter(ProjectConfig.IS_TEST_CASE_KEY,
            String.valueOf(this.isRunningTestCase));
        parameters.setParameter(ProjectConfig.TEST_CASE_CLASS_NAME_KEY,
            this.testCase == null ? ProjectConfig.NULL : this.testCase.testClassName);
        parameters.setParameter(ProjectConfig.TEST_CASE_METHOD_NAME_KEY,
            this.testCase == null ? ProjectConfig.NULL : this.testCase.testMethodName);
        return parameters;
    }

    @Override
    public void update(JavaTracerAgentParameters parameters) {
        this.setJavaHome(new JavaHome(parameters.getParameter(ProjectConfig.JAVA_HOME_KEY)));
        this.setLaunchClass(parameters.getParameter(ProjectConfig.LAUNCH_CLASS_KEY));
        this.setIsTestCase(Boolean.parseBoolean(
            parameters.getParameter(ProjectConfig.IS_TEST_CASE_KEY)));
        if (this.isRunningTestCase()) {
            this.setTestCase(new TestCase(
                parameters.getParameter(ProjectConfig.TEST_CASE_CLASS_NAME_KEY),
                parameters.getParameter(ProjectConfig.TEST_CASE_METHOD_NAME_KEY)));
        }
        Log.debug("test launch class: " + this.getTestCase().testClassName);
    }

    public List<String> getClassPaths() {
        return classPaths;
    }

    public void setClassPaths(List<String> classPaths) {
        Objects.requireNonNull(classPaths,
            Log.genMessage("The given classPaths is null.", this.getClass()));
        this.classPaths = classPaths;
    }

    public JavaBuildTools getJavaBuildTool() {
        return this.javaBuildTools;
    }

    public JavaHome getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(JavaHome javaHome) {
        this.javaHome = javaHome;
    }

    public String getLaunchClass() {
        return this.isRunningTestCase ? ProjectConfig.TEST_CASE_LAUNCH_CLASS : this.launchClass;
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
        this.setIsTestCase(testCase != null);
        this.testCase = testCase;
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

    public void setJavaBuildTools(final JavaBuildTools javaBuildTools) {
        Objects.requireNonNull(javaBuildTools,
            Log.genMessage("The given java build tools is null.", this.getClass()));
        this.javaBuildTools = javaBuildTools;
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