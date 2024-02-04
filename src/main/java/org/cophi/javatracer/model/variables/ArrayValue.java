package org.cophi.javatracer.model.variables;

public class ArrayValue extends ReferenceValue {

    private static final long serialVersionUID = -1194381666885038425L;
    private String componentType;

    public ArrayValue(boolean isNull, boolean isRoot, Variable var) {
        super(isNull, isRoot, var);
    }

    @Override
    public VarValue clone() {
        ArrayValue clonedValue = new ArrayValue(this.isNull, isRoot, this.variable.clone());
        clonedValue.setUniqueID(uniqueID);
        clonedValue.setComponentType(componentType);
        clonedValue.setParents(this.getParents());
        clonedValue.setChildren(this.getChildren());
        return clonedValue;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(variable + ":");
        buffer.append(getReferenceID());
        String print = buffer.toString();

        return print;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getElementId(int i) {
        return String.format("[%s]", i);
    }
}