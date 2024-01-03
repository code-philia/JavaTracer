/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package org.cophi.javatracer.model.value;

import org.cophi.javatracer.model.variable.Variable;

/**
 * @author LLT
 */
public class StringValue extends PrimitiveValue {

    public static final String TYPE = "String";
    private static final long serialVersionUID = -4758264964156129332L;

    public StringValue(String val, boolean isRoot, Variable var) {
        super(val, isRoot, var);
        var.setType(TYPE);
    }

    @Override
    protected boolean needToRetrieveValue() {
        return false;
    }

    @Override
    public VarValue clone() {
        StringValue clonedValue = new StringValue(this.stringValue, isRoot, this.variable.clone());
        clonedValue.setParents(this.getParents());
        clonedValue.setChildren(this.getChildren());
        return clonedValue;
    }
}
