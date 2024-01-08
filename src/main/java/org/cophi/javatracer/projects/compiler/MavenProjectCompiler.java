package org.cophi.javatracer.projects.compiler;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.cophi.javatracer.configs.MavenProjectConfig;
import org.cophi.javatracer.exceptions.ProjectNotCompilableException;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.utils.MavenUtils;

public class MavenProjectCompiler implements ProjectCompiler {

    protected final String projectRootPath;
    protected final String mavenHome;

    public MavenProjectCompiler(final String projectRootPath, final String mavenHome) {
        this.projectRootPath = projectRootPath;
        this.mavenHome = mavenHome;
    }

    public MavenProjectCompiler(final String projectRootPath) {
        this(projectRootPath, MavenUtils.getMavenHome());
    }

    public MavenProjectCompiler(final MavenProjectConfig mavenProjectConfig) {
        this(mavenProjectConfig.getProjectRootPath(), mavenProjectConfig.getMavenHome());
    }

    @Override
    public void compileProject() throws ProjectNotCompilableException {
        if (this.mavenHome == null || this.mavenHome.isBlank()) {
            throw new ProjectNotCompilableException(Log.genMessage(
                "Maven home is not set. Please make sure that Maven is available or set the path in the preference page",
                this.getClass()));
        }

        InvocationRequest request = new DefaultInvocationRequest();

        Path pomPath = Path.of(MavenUtils.getPomPath(projectRootPath));
        request.setPomFile(pomPath.toFile());
        request.setGoals(Arrays.asList("-q", "clean", "test-compile"));

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(this.mavenHome));

        try {
            final InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                throw new ProjectNotCompilableException(Log.genMessage(
                    "Maven project is not compilable. Please make sure that the project is compilable",
                    this.getClass()));
            }
        } catch (MavenInvocationException e) {
            throw new ProjectNotCompilableException(e);
        }
    }

}
