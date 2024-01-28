package org.cophi.javatracer.model.variables;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represent the variable used in the target program
 */
public abstract class Variable implements Serializable, Cloneable {

    /**
     * Used when the type of the variable type is unknown
     */
    public static final String UNKNOWN_TYPE = "unknown type";
    @Serial
    private static final long serialVersionUID = -1126075634497698926L;
    /**
     * Indicate this is read variable
     */
    public static String READ = "read";
    /**
     * Indicate this is written variable
     */
    public static String WRITTEN = "written";

    /**
     * Type of the variable
     */
    protected String type;
    /**
     * Name of the variable
     */
    protected String variableName;

    /**
     * AliasVarId is the JVM heap address
     */
    protected String aliasVarID;

    /**
     * The id of an object (non-primitive type) is its object id + the order of trace node defining
     * it, e.g., 100.a:33 .
     * <br><br>
     * If a variable is a non-static field, its id is: its parent's object id + field name + the
     * order of trace node defining it, e.g., 100.a:33 ; if it is a static field, its id is: its
     * field name + the order of trace node defining it, e.g., Class.a:33; if it is an array
     * element, its id is: its parent's object id + index + the order of trace node defining it,
     * e.g., 100[1]:33 ; if it is a local variable, its id is: its scope (i.e., class[startLine,
     * endLine]) + variable name + invocation_layer + the order of trace node defining it,
     * invocation_layer is for distinguish recursive methods. e.g., com/Main{12, 21}a-3:33 ; if it
     * is a virtual variable, its id is: "virtualVar" + the order of the relevant
     * return-trace-node.
     * <br><br>
     * Note that if the user want to concanate a variable ID, such as local variable ID, field ID,
     * etc. He or she should use the following three static method: <br>
     *
     * <code>Variable.concanateFieldVarID()</code><br>
     * <code>Variable.concanateArrayElementVarID()</code><br>
     * <code>Variable.concanateLocalVarID()</code><br>
     */
    protected String varID;

    public Variable(String name, String type) {
        this.variableName = name;
        this.type = type;
    }

    public static String concatenateFieldVarID(String parentID, String fieldName) {
        return parentID + "." + fieldName;
    }

    public static String concatenateArrayElementVarID(String parentID, String indexValueString) {
        return parentID + "[" + indexValueString + "]";
    }

    public static String concatenateLocalVarID(String className, String varName, int startLine,
        int endLine) {
        String clazzName = className.replace(".", "/");
        return clazzName + "{" + startLine + "," + endLine + "}" + varName;
    }

    public static String truncateSimpleID(String completeVarID) {
        if (completeVarID == null) {
            return null;
        }

        if (completeVarID.contains(":")) {
            return completeVarID.substring(0, completeVarID.indexOf(":"));
        } else {
            return completeVarID;
        }
    }

    public static String truncateStepOrder(String completeVarID) {
        if (completeVarID.contains(":")) {
            return completeVarID.substring(completeVarID.indexOf(":") + 1);
        } else {
            return "";
        }
    }

    public abstract Variable clone();

    public String getAliasVarID() {
        return aliasVarID;
    }

    public void setAliasVarID(String aliasVarID) {
        this.aliasVarID = aliasVarID;
    }

    public String getName() {
        return variableName;
    }

    public void setName(String variableName) {
        this.variableName = variableName;
    }

    public abstract String getSimpleName();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVarID() {
        return varID;
    }

    /**
     * Note that if the user want to concatenate a variable ID, such as local variable ID, field ID,
     * etc. He or she should use the following three static method: <br>
     *
     * <code>Variable.concanateFieldVarID()</code><br>
     * <code>Variable.concanateArrayElementVarID()</code><br>
     * <code>Variable.concanateLocalVarID()</code><br>
     *
     * @param varID
     */
    public void setVarID(String varID) {
        this.varID = varID;
    }
}
