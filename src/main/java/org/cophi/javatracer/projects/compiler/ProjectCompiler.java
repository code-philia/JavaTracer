package org.cophi.javatracer.projects.compiler;

import org.cophi.javatracer.exceptions.ProjectNotCompilableException;

@FunctionalInterface
public interface ProjectCompiler {

    void compileProject() throws ProjectNotCompilableException;

}
