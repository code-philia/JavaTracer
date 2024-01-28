package org.cophi.javatracer.model.scope;

import org.cophi.javatracer.model.location.ClassLocation;
import org.cophi.javatracer.model.trace.TraceNode;

public class SourceScope implements Scope {

    private String className;
    private int startLine;
    private int endLine;

    /**
     * whether the scope contains some jump statements such as break, continue, return, and throw.
     */
    private boolean hasJumpStatement;
    private boolean isLoopScope;

    public SourceScope() {
    }

    public SourceScope(String className, int startLine, int endLine, boolean isLoopScope) {
        super();
        this.className = className;
        this.startLine = startLine;
        this.endLine = endLine;
        this.isLoopScope = isLoopScope;
    }

    public SourceScope(String className, int start, int end) {
        super();
        this.className = className;
        this.startLine = start;
        this.endLine = end;
    }

    @Override
    public boolean containLocation(ClassLocation location) {
        String nodeClassName = location.getDeclaringCompilationUnitName();

        if (nodeClassName.equals(className)) {
            int line = location.getLineNumber();
            return line >= startLine && line <= endLine;
        }

        return false;
    }

    public boolean containsNodeScope(TraceNode node) {
        return containLocation(node.getBreakPoint());
    }

    public boolean isLoop() {
        return isLoopScope;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public boolean hasJumpStatement() {
        return this.hasJumpStatement;
    }

    public void setHasJumpStatement(boolean hasJumpStatement) {
        this.hasJumpStatement = hasJumpStatement;
    }

    public void setLoopScope(boolean isLoopScope) {
        this.isLoopScope = isLoopScope;
    }
}
