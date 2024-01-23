package org.cophi.javatracer.configs;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.cophi.javatracer.exceptions.ProjectConfigException;
import org.cophi.javatracer.instrumentation.agents.AgentParameters;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.testcase.TestCase;
import org.cophi.javatracer.testcase.runner.AbstractTestCaseRunner;

public class ProjectConfig implements AgentParameters {

    public static final String mainDescriptor = "main([Ljava/lang/String;)V";
    public static final String WORKING_DIR_KEY = "working_dir";
    public static final String LAUNCH_CLASS_KEY = "launch_class";
    public static final String JAVA_HOME_KEY = "java_home";
    public static final String IS_TEST_CASE_KEY = "is_test_case";
    public static final String TEST_CASE_CLASS_NAME_KEY = "test_case_class_name";
    public static final String TEST_CASE_METHOD_NAME_KEY = "test_case_method_name";
    public static final String SOURCE_CODE_PATH_KEY = "source_code_path";
    public static final String TEST_CODE_PATH_KEY = "test_code_path";
    public static final String INCLUDE_CLASS_KEY = "include_classes";
    public static final String EXCLUDE_CLASS_KEY = "exclude_classes";
    public static final String JAVA_BUILD_TOOL_KEY = "java_build_tool";
    public static final String CLASS_NAME_DELIMITER = ">";
    public static final String CLASS_PATHS_KEY = "class_paths";
    public static final String REQUIRE_METHOD_SPLITTING_KEY = "require_method_splitting";
    protected final static String TEST_CASE_LAUNCH_CLASS = AbstractTestCaseRunner.class.getCanonicalName();
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
    protected EntryPoint entryPoint = null;
    protected boolean requireMethodSplitting = false;

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
            this.testCase == null ? JavaTracerAgentParameters.NULL : this.testCase.testClassName);
        parameters.setParameter(ProjectConfig.TEST_CASE_METHOD_NAME_KEY,
            this.testCase == null ? JavaTracerAgentParameters.NULL : this.testCase.testMethodName);
        parameters.setParameter(ProjectConfig.WORKING_DIR_KEY, this.projectRootPath);
        parameters.setParameter(ProjectConfig.SOURCE_CODE_PATH_KEY, this.sourceCodePath);
        parameters.setParameter(ProjectConfig.TEST_CODE_PATH_KEY, this.testCodePath);
        parameters.setParameter(ProjectConfig.INCLUDE_CLASS_KEY,
            this.includedClassNames.isEmpty() ? JavaTracerAgentParameters.NULL :
                String.join(ProjectConfig.CLASS_NAME_DELIMITER, this.includedClassNames));
        parameters.setParameter(ProjectConfig.EXCLUDE_CLASS_KEY,
            this.excludedClassName.isEmpty() ? JavaTracerAgentParameters.NULL :
                String.join(ProjectConfig.CLASS_NAME_DELIMITER, this.excludedClassName));
        parameters.setParameter(ProjectConfig.JAVA_BUILD_TOOL_KEY,
            this.javaBuildTools.name());
        parameters.setParameter(ProjectConfig.CLASS_PATHS_KEY,
            this.classPaths.isEmpty() ? JavaTracerAgentParameters.NULL :
                String.join(ProjectConfig.CLASS_NAME_DELIMITER, this.classPaths));
        parameters.setParameter(ProjectConfig.REQUIRE_METHOD_SPLITTING_KEY,
            String.valueOf(this.requireMethodSplitting));
        if (this.entryPoint != null) {
            parameters.update(this.entryPoint.genParameters());
        }
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
        this.setProjectRootPath(parameters.getParameter(ProjectConfig.WORKING_DIR_KEY));
        this.setSourceCodePath(parameters.getParameter(ProjectConfig.SOURCE_CODE_PATH_KEY));
        this.setTestCodePath(parameters.getParameter(ProjectConfig.TEST_CODE_PATH_KEY));

        final String includeClassesStr = parameters.getParameter(ProjectConfig.INCLUDE_CLASS_KEY);
        if (!includeClassesStr.equals(JavaTracerAgentParameters.NULL)) {
            this.setIncludedClassNames(List.of(includeClassesStr.split(
                ProjectConfig.CLASS_NAME_DELIMITER)));
        }

        final String excludeClassesStr = parameters.getParameter(ProjectConfig.EXCLUDE_CLASS_KEY);
        if (!excludeClassesStr.equals(JavaTracerAgentParameters.NULL)) {
            this.setExcludedClassNames(List.of(excludeClassesStr.split(
                ProjectConfig.CLASS_NAME_DELIMITER)));
        }
        this.setJavaBuildTools(JavaBuildTools.valueOf(
            parameters.getParameter(ProjectConfig.JAVA_BUILD_TOOL_KEY)));

        final String classPathStr = parameters.getParameter(ProjectConfig.CLASS_PATHS_KEY);
        if (!classPathStr.equals(JavaTracerAgentParameters.NULL)) {
            this.setClassPaths(List.of(classPathStr.split(
                ProjectConfig.CLASS_NAME_DELIMITER)));
        }

        this.setRequireMethodSplitting(
            Boolean.parseBoolean(
                parameters.getParameter(ProjectConfig.REQUIRE_METHOD_SPLITTING_KEY)));

        this.entryPoint = EntryPoint.parseParameter(parameters);
    }

    public List<String> getClassPaths() {
        return classPaths;
    }

    public void setClassPaths(List<String> classPaths) {
        Objects.requireNonNull(classPaths,
            Log.genMessage("The given classPaths is null.", this.getClass()));
        this.classPaths = classPaths;
    }

    public EntryPoint getEntryPoint() {
        return this.entryPoint;
    }

    public void setEntryPoint(final EntryPoint entryPoint) {
        this.entryPoint = entryPoint;
    }

    public List<String> getExcludedClassNames() {
        return this.excludedClassName;
    }

    public void setExcludedClassNames(List<String> excludedClassNames) {
        this.excludedClassName = excludedClassNames;
    }

    public List<String> getIncludedClassNames() {
        return includedClassNames;
    }

    public void setIncludedClassNames(List<String> includedClassNames) {
        this.includedClassNames = includedClassNames;
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
        this.setEntryPoint(new EntryPoint(launchClass));
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

    public boolean requireMethodSplitting() {
        return this.requireMethodSplitting;
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

    public void setRequireMethodSplitting(final boolean requireMethodSplitting) {
        this.requireMethodSplitting = requireMethodSplitting;
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
