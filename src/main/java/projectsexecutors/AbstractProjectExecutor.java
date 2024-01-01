package projectsexecutors;

import exceptions.ProjectNotCompilableException;

public abstract class AbstractProjectExecutor implements ProjectExecutor {

    public AbstractProjectExecutor() {

    }

    @Override
    public abstract void executeProject() throws ProjectNotCompilableException;

}
