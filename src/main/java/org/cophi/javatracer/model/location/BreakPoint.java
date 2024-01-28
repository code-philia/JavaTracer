package org.cophi.javatracer.model.location;

import java.util.ArrayList;
import java.util.List;
import org.cophi.javatracer.model.scope.ControlScope;
import org.cophi.javatracer.model.scope.SourceScope;
import org.cophi.javatracer.model.variables.Variable;
import org.cophi.javatracer.utils.NamingUtils;

public class BreakPoint extends ClassLocation {

    private List<Variable> readVariables = new ArrayList<>();
    private List<Variable> writtenVariables = new ArrayList<>();

    private List<Variable> allVisibleVariables = new ArrayList<>();

    private boolean isReturnStatement;

    private boolean isStartOfClass = false;

    private boolean isConditional;
    private boolean isBranch;

    /**
     * The reason to differentiate control scope and loop scope is that (1) control scope include
     * more than a code block, e.g., a statement outside a block can be control dependent on a
     * statement inside a block. (2) in contrast, loop scope can only include the statements inside
     * a code block.
     */
    private ControlScope controlScope;
    private SourceScope loopScope;
    private List<ClassLocation> targets = new ArrayList<>();
    private boolean isSourceVersion;

    public BreakPoint(String className, String methodSignature, int linNum) {
        super(className, methodSignature, linNum);
    }

    public BreakPoint(String className, String declaringCompilationUnitName, String methodSign,
        int lineNo) {
        super(className, methodSign, lineNo);
    }

    public void addReadVariable(Variable var) {
        if (!this.readVariables.contains(var)) {
            this.readVariables.add(var);
        }
    }

    public void addTarget(ClassLocation target) {
        if (!this.targets.contains(target)) {
            this.targets.add(target);
        }
    }

    public void addWrittenVariable(Variable var) {
        if (!this.writtenVariables.contains(var)) {
            this.writtenVariables.add(var);
        }
    }

    public List<Variable> getAllVisibleVariables() {
        return allVisibleVariables;
    }

    public void setAllVisibleVariables(List<Variable> allVisibleVariables) {
        this.allVisibleVariables = allVisibleVariables;
    }

    public ControlScope getControlScope() {
        return controlScope;
    }

    public void setControlScope(ControlScope conditionScope) {
        this.controlScope = conditionScope;
    }

    public SourceScope getLoopScope() {
        return loopScope;
    }

    public void setLoopScope(SourceScope loopScope) {
        this.loopScope = loopScope;
    }

    public String getPackageName() {
        return NamingUtils.extractPackageName(this.className);
    }

    public List<Variable> getReadVariables() {
        return readVariables;
    }

    public void setReadVariables(List<Variable> readVariables) {
        this.readVariables = readVariables;
    }

    public String getShortMethodSignature() {
        String methodSig = this.methodSignature;
        String shortSig = methodSig.substring(methodSig.indexOf("#") + 1, methodSig.length());

        return shortSig;
    }

    public List<ClassLocation> getTargets() {
        return targets;
    }

    public void setTargets(List<ClassLocation> targets) {
        this.targets = targets;
    }

    public List<Variable> getWrittenVariables() {
        return writtenVariables;
    }

    public void setWrittenVariables(List<Variable> writtenVariables) {
        this.writtenVariables = writtenVariables;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
            * result
            + ((this.className == null) ? 0 : this.className
            .hashCode());
        result = prime * result + this.lineNumber;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (obj instanceof ClassLocation other) {
            return this.className.equals(other.getClassCanonicalName())
                && this.lineNumber == other.getLineNumber();
        }

        return false;
    }

    public Object clone() {
        ClassLocation location = (ClassLocation) super.clone();
        BreakPoint point = new BreakPoint(location.getClassCanonicalName(),
            this.methodSignature, this.lineNumber);
        point.setAllVisibleVariables(allVisibleVariables);
        point.setControlScope(controlScope);
        point.setConditional(isConditional);
        point.setReturnStatement(isReturnStatement);
        point.setLoopScope(loopScope);
        point.setTargets(targets);
        point.setReadVariables(readVariables);
        point.setWrittenVariables(readVariables);
        return point;
    }

    @Override
    public String toString() {
        return "BreakPoint [classCanonicalName=" + this.className
            + ", lineNo=" + this.lineNumber + ", methodName=" + this.methodSignature
            + "]";
    }

    public boolean isBranch() {
        return isBranch;
    }

    public void setBranch(boolean isBranch) {
        this.isBranch = isBranch;
    }

    public boolean isConditional() {
        return this.isConditional;
    }

    public void setConditional(boolean isConditional) {
        this.isConditional = isConditional;
    }

    public boolean isReturnStatement() {
        return isReturnStatement;
    }

    public void setReturnStatement(boolean isReturnStatement) {
        this.isReturnStatement = isReturnStatement;
    }

    public boolean isSourceVersion() {
        return isSourceVersion;
    }

    public void setSourceVersion(boolean isSourceVersion) {
        this.isSourceVersion = isSourceVersion;
    }

    public boolean isStartOfClass() {
        return isStartOfClass;
    }

    public void setStartOfClass(boolean isStartOfClass) {
        this.isStartOfClass = isStartOfClass;
    }

    public void mergeControlScope(ControlScope locationScope) {
        if (this.controlScope == null) {
            this.controlScope = locationScope;
        } else {
            for (ClassLocation location : locationScope.getRangeList()) {
                if (this.controlScope != null) {
                    ControlScope thisScope = this.controlScope;
                    if (!thisScope.containLocation(location)) {
                        thisScope.addLocation(location);
                    }
                }
            }
        }
    }

    public boolean valid() {
        return this.lineNumber > 0;
    }
}
