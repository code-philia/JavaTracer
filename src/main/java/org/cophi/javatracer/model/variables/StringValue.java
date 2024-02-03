package org.cophi.javatracer.model.variables;

public class StringValue extends PrimitiveValue {

    public static final String TYPE = "String";
    private static final long serialVersionUID = -4758264964156129332L;

    public StringValue(String val, boolean isRoot, Variable var) {
        super(val, isRoot, var);
        var.setType(TYPE);
    }

    @Override
    public VarValue clone() {
        StringValue clonedValue = new StringValue(this.stringValue, isRoot, this.variable.clone());
        clonedValue.setParents(this.getParents());
        clonedValue.setChildren(this.getChildren());
        return clonedValue;
    }

    @Override
    protected boolean needToRetrieveValue() {
        return false;
    }
}

