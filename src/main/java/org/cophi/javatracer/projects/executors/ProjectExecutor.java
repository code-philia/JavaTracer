package org.cophi.javatracer.projects.executors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.exceptions.ProjectNotCompilableException;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.projects.compiler.ProjectCompiler;
import org.cophi.javatracer.projects.compiler.ProjectCompilerFactory;

public class ProjectExecutor {

    protected final ProjectConfig projectConfig;

    public ProjectExecutor(final ProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
    }

    public void executeProject() throws ProjectNotCompilableException {

        ProjectCompiler projectCompiler = ProjectCompilerFactory.getProjectCompiler(projectConfig);
        projectCompiler.compileProject();

        List<String> commands = CommandLineBuilder.buildCommand(projectConfig);
        Log.debug("Instrumentation Command: " + String.join(" ", commands));

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(Path.of(projectConfig.getProjectRootPath()).toFile());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
