package projectsexecutors;

import exceptions.ProjectNotCompilableException;

public interface ProjectExecutor {

    void executeProject() throws ProjectNotCompilableException;
}
