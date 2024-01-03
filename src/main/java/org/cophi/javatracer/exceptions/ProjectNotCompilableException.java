package org.cophi.javatracer.exceptions;

import java.io.Serial;

public class ProjectNotCompilableException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public ProjectNotCompilableException(String details) {
        super(details);
    }

    public ProjectNotCompilableException(Throwable cause) {
        super(cause);
    }
}
