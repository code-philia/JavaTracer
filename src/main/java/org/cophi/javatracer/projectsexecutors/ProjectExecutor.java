package org.cophi.javatracer.projectsexecutors;

import org.cophi.javatracer.exceptions.ProjectNotCompilableException;

public interface ProjectExecutor {

    void executeProject() throws ProjectNotCompilableException;
}
