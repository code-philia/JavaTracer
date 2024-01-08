package org.cophi.javatracer.projects.compiler;

import org.cophi.javatracer.configs.MavenProjectConfig;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.log.Log;

public class ProjectCompilerFactory {

    private ProjectCompilerFactory() {
        throw new IllegalStateException(Log.genMessage("Utility class", this.getClass()));
    }

    public static ProjectCompiler getProjectCompiler(final ProjectConfig projectConfig) {
        return switch (projectConfig.getJavaBuildTool()) {
            case MAVEN -> new MavenProjectCompiler((MavenProjectConfig) projectConfig);
            case DEFAULT -> null;
            case AUTO -> null;
            default -> throw new IllegalStateException(
                Log.genMessage("Unexpected value: ", projectConfig.getJavaBuildTool().toString()));
        };
    }

}
