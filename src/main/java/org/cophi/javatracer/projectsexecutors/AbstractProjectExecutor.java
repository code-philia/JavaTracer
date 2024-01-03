package org.cophi.javatracer.projectsexecutors;

import org.cophi.javatracer.exceptions.ProjectNotCompilableException;

public abstract class AbstractProjectExecutor implements ProjectExecutor {

    public AbstractProjectExecutor() {

    }

    @Override
    public abstract void executeProject() throws ProjectNotCompilableException;

}
