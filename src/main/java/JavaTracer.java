import configs.JavaHome;
import configs.JavaTracerConfig;
import configs.projectconfigs.MavenProjectConfig;
import configs.projectconfigs.ProjectConfig;
import exceptions.ProjectNotCompilableException;
import log.Log;
import log.LogType;
import model.trace.Trace;
import projectsexecutors.MavenProjectExecutor;
import testcase.TestCase;

public class JavaTracer {

    protected ProjectConfig config;

    public JavaTracer(final ProjectConfig config) {
        this.config = config;
    }

    public static void main(String[] args) throws ProjectNotCompilableException {
        JavaHome javaHome = new JavaHome("C:\\Program Files\\Java\\jdk-17");
        System.out.println(javaHome);
        MavenProjectConfig config = new MavenProjectConfig(
            "C:\\Users\\WYK\\IdeaProjects\\JavaTracer\\src\\test\\bugs\\MavenDummyProject",
            true);
        config.setJavaHome(javaHome);
        config.setLaunchClass("org.example.Main");

        TestCase testCase = new TestCase("org.example.TestClassTest", "add");
        config.setTestCase(testCase);

        JavaTracerConfig javaTracerConfig = JavaTracerConfig.getInstance();
        javaTracerConfig.logType = LogType.DEBUG;

        MavenProjectExecutor executor = new MavenProjectExecutor(config);
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
