package org.cophi.javatracer.recommendation;

import org.cophi.javatracer.model.trace.Trace;
import org.cophi.javatracer.model.trace.TraceNode;
import org.cophi.javatracer.model.variables.VarValue;

public class SimpleDetailInspector extends DetailInspector {

    @Override
    public DetailInspector clone() {
        DetailInspector inspector = new SimpleDetailInspector();
        if (this.inspectingRange != null) {
            inspector.setInspectingRange(this.inspectingRange.clone());
        }
        return inspector;
    }

    public TraceNode recommendDetailNode(TraceNode currentNode, Trace trace, VarValue wrongValue) {
        TraceNode nextNode;
        if (currentNode.getOrder() > this.inspectingRange.endNode.getOrder()) {
            nextNode = this.inspectingRange.startNode;
        } else {
            nextNode = trace.getExecutionList().get(currentNode.getOrder());
        }
        return nextNode;
    }


}
