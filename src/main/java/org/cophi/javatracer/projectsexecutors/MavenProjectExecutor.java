package org.cophi.javatracer.projectsexecutors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.cophi.javatracer.configs.JavaTracerConfig;
import org.cophi.javatracer.configs.MavenProjectConfig;
import org.cophi.javatracer.exceptions.ProjectNotCompilableException;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.utils.MavenUtils;

public class MavenProjectExecutor extends AbstractProjectExecutor {

    protected final MavenProjectConfig mavenProjectConfig;

    public MavenProjectExecutor(final MavenProjectConfig mavenProjectConfig) {
        this.mavenProjectConfig = mavenProjectConfig;
    }

    @Override
    public void executeProject() throws ProjectNotCompilableException {
        final String projectRootPath = this.mavenProjectConfig.getProjectRootPath();
        final String mavenHome = this.mavenProjectConfig.getMavenHome();
        if (!MavenUtils.isCompiled(projectRootPath)) {
            MavenUtils.compileTargetProject(projectRootPath, mavenHome);
        }

        List<String> commands = new ArrayList<>();
        commands.add(this.mavenProjectConfig.getJavaHome().getExePath());
        commands.add("-Duser.dir=" + projectRootPath);
        commands.add(
            "-javaagent:" + JavaTracerConfig.getInstance().getJavaTracerJarPath());
        commands.add("-cp");
        commands.add(String.join(";", this.mavenProjectConfig.getClassPaths()));
        commands.add(this.mavenProjectConfig.getLaunchClass());

        if (this.mavenProjectConfig.isRunningTestCase()) {
            commands.add(this.mavenProjectConfig.getTestCase().testClassName);
            commands.add(this.mavenProjectConfig.getTestCase().testMethodName);
            commands.add(String.valueOf(this.mavenProjectConfig.getTestCase().testCaseType));
        }

        Log.debug("Executing command: " + String.join(" ", commands));
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(Path.of(this.mavenProjectConfig.getProjectRootPath()).toFile());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
