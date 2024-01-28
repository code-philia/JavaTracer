package org.cophi.javatracer.recommendation;


import org.cophi.javatracer.model.trace.Trace;
import org.cophi.javatracer.model.trace.TraceNode;
import org.cophi.javatracer.model.variables.VarValue;

public abstract class DetailInspector {

    protected InspectingRange inspectingRange;

    public abstract DetailInspector clone();

    public InspectingRange getInspectingRange() {
        return inspectingRange;
    }

    public void setInspectingRange(InspectingRange inspectingRange) {
        this.inspectingRange = inspectingRange;
    }

    public abstract TraceNode recommendDetailNode(TraceNode currentNode, Trace trace,
        VarValue wrongValue);

}
