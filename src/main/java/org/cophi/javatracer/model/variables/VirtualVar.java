package org.cophi.javatracer.model.variables;

import org.cophi.javatracer.utils.PrimitiveUtils;

/**
 * A virtual variable is a variable connecting the read/written relation between a return statement
 * and a method invocation.
 *
 * @author "linyun"
 */
public class VirtualVar extends Variable {

    public static final String VIRTUAL_TYPE = "RV";
    public static final String VIRTUAL_PREFIX = "vir:";
    private static final long serialVersionUID = -1820253033144176089L;

    public VirtualVar(String name, String type) {
        super(name, type);
    }

    @Override
    public Variable clone() {
        VirtualVar var = new VirtualVar(variableName, type);
        var.setVarID(varID);
        return var;
    }

    @Override
    public String getSimpleName() {
        return this.variableName;
    }

    public boolean isOfPrimitiveType() {
        return PrimitiveUtils.isPrimitiveTypeOrString(type);
    }
}
