package org.cophi.javatracer.model.variables;

import org.cophi.javatracer.algorithm.GraphNode;

public class VirtualValue extends VarValue {

    private static final long serialVersionUID = 8295559919201412983L;

    private long uniqueId;

    public VirtualValue(boolean isRoot, Variable variable, long uniqueID) {
        this.isRoot = isRoot;
        this.variable = variable;
        this.uniqueId = uniqueID;
    }

    @Override
    public String getHeapID() {
        return String.valueOf(uniqueId);
    }

    @Override
    public VarValue clone() {
        VirtualValue clonedValue = new VirtualValue(isRoot, variable.clone(), this.uniqueId);
        return clonedValue;
    }

    public boolean isOfPrimitiveType() {
        if (this.variable instanceof VirtualVar) {
            VirtualVar var = (VirtualVar) this.variable;
            return var.isOfPrimitiveType();
        }

        return false;
    }

    @Override
    public boolean isTheSameWith(GraphNode node) {
        if (node instanceof VirtualValue) {
            VirtualValue thatValue = (VirtualValue) node;

            return this.getStringValue().equals(thatValue.getStringValue());
        }

        return false;
    }
}