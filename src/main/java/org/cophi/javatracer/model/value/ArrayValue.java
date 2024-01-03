/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package org.cophi.javatracer.model.value;

import org.cophi.javatracer.model.variable.Variable;


public class ArrayValue extends ReferenceValue {

    private static final long serialVersionUID = -1194381666885038425L;
    private String componentType;

    public ArrayValue(boolean isNull, boolean isRoot, Variable var) {
        super(isNull, isRoot, var);
    }

    public String toString() {
        String print = "array("
            + componentType + "): "
            + getReferenceID();

        return print;
    }

    public String getElementId(int i) {
        return String.format("[%s]", i);
    }


    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
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
}
