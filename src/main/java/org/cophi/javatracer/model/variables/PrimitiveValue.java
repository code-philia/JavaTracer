package org.cophi.javatracer.model.variables;

import org.cophi.javatracer.algorithm.GraphNode;

/**
 * @author Yun Lin
 */
public class PrimitiveValue extends VarValue {

    private static final long serialVersionUID = 2434374560748851890L;

    public PrimitiveValue(String strVal, boolean isRoot, Variable variable) {
        super(isRoot, variable);
        this.stringValue = strVal;
    }

    @Override
    public String getHeapID() {
        return null;
    }

    @Override
    public String getManifestationValue() {
        return stringValue + " (id=" + variable.getVarID() + ")";
    }

    @Override
    public VarValue clone() {
        PrimitiveValue clonedValue = new PrimitiveValue(this.getStringValue(), isRoot,
            this.variable.clone());
        clonedValue.setParents(this.getParents());
        clonedValue.setChildren(this.getChildren());
        return clonedValue;
    }

    @Override
    public String toString() {
        return String.format("(%s:%s)", getVarName(), this.stringValue);
    }

    public String getPrimitiveType() {
        return this.variable.getType();
    }

    public void setPrimitiveType(String type) {
        this.variable.setType(type);
    }

    @Override
    public boolean isTheSameWith(GraphNode nodeAfter) {
        if (nodeAfter instanceof PrimitiveValue) {
            PrimitiveValue pv = (PrimitiveValue) nodeAfter;
            return this.getStringValue().equals(pv.getStringValue());
        }
        return false;
    }


}