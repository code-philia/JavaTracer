package org.cophi.javatracer.model.variables;

public class ConstantVar extends Variable {

    private static final long serialVersionUID = 3271962488703645239L;
    private String value;

    public ConstantVar(String name, String type) {
        super(name, type);
        this.value = name;
    }

    @Override
    public Variable clone() {
        return new ConstantVar(variableName, type);
    }

    @Override
    public String getSimpleName() {
        return value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
