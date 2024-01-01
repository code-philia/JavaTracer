package configs.projectconfigs;

import configs.JavaTracerConfig;
import exceptions.ProjectNotCompilableException;
import java.util.Objects;
import log.Log;
import utils.MavenUtils;

public class MavenProjectConfig extends ProjectConfig {

    protected String mavenHome = MavenUtils.getMavenHome();

    public MavenProjectConfig() {
        this(null, false);
    }

    public MavenProjectConfig(final String projectRootPath) {
        this(projectRootPath, true);
    }

    public MavenProjectConfig(final String projectRootPath, final boolean autoFill) {
        super();
        Objects.requireNonNull(projectRootPath,
            Log.genMessage("Given projectRootPath cannot be null", this.getClass()));
        if (projectRootPath.isBlank()) {
            throw new IllegalArgumentException(
                Log.genMessage("Given projectRootPath cannot be blank", this.getClass()));
        }
        this.projectRootPath = projectRootPath;
        this.setJavaBuildTools(JavaBuildTools.MAVEN);
        if (autoFill) {
            this.updateFrameworkInfo();
        }
    }

    public String getMavenHome() {
        return this.mavenHome;
    }

    public void setMavenHome(final String mavenHome) {
        this.mavenHome = mavenHome;
    }

    @Override
    public void updateFrameworkInfo() {
        this.sourceCodePath = MavenUtils.getSourceCodePath(this.projectRootPath);
        this.testCodePath = MavenUtils.getTestCodePath(this.projectRootPath);
        this.addClassPath(MavenUtils.getTargetSourceCodePath(this.projectRootPath));
        this.addClassPath(MavenUtils.getTargetTestCodePath(this.projectRootPath));
        this.addClassPath(JavaTracerConfig.getInstance().getJavaTracerJarPath());
        try {
            for (String classPath : MavenUtils.getClassPaths(this.projectRootPath,
                this.mavenHome)) {
                this.addClassPath(classPath);
            }
        } catch (ProjectNotCompilableException e) {
            Log.error("Maven is not supported");
        }
    }

}
