package model.value;

import model.variable.Variable;
import model.variable.VirtualVar;

public class VirtualValue extends VarValue {

    private static final long serialVersionUID = 8295559919201412983L;

    public VirtualValue(boolean isRoot, Variable variable) {
        this.isRoot = isRoot;
        this.variable = variable;
    }

    @Override
    public boolean isTheSameWith(GraphNode node) {
        if (node instanceof VirtualValue thatValue) {

            return this.getStringValue().equals(thatValue.getStringValue());
        }

        return false;
    }

    public boolean isOfPrimitiveType() {
        if (this.variable instanceof VirtualVar var) {
            return var.isOfPrimitiveType();
        }

        return false;
    }

    @Override
    public VarValue clone() {
        VirtualValue clonedValue = new VirtualValue(isRoot, variable.clone());
        return clonedValue;
    }
}
