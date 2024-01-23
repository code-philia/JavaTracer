package org.cophi.javatracer.projects.executors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.io.IOUtils;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.exceptions.ProjectNotCompilableException;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.projects.compiler.ProjectCompiler;
import org.cophi.javatracer.projects.compiler.ProjectCompilerFactory;

public class ProjectExecutor {

    protected static ExecutorService executorService = Executors.newCachedThreadPool();
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
//        processBuilder.inheritIO();
//        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            // Handle the output stream
            BufferedReader outputReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String outputLine;
            while ((outputLine = outputReader.readLine()) != null) {
                System.out.println(outputLine);
            }
            // Handle the error stream
            BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println(errorLine);
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean isProcessRunning(final Process process) {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    protected void setupInputStream(final Process process) {
        final InputStream is = process.getInputStream();
        final InputStreamReader streamReader = new InputStreamReader(is);
        ProjectExecutor.executorService.execute(() -> {
            BufferedReader br = new BufferedReader(streamReader);
            String line;
            try {
                while (isProcessRunning(process) && ((line = br.readLine()) != null)) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(streamReader);
                IOUtils.closeQuietly(br);
                IOUtils.closeQuietly(is);
            }
        });
    }


}
