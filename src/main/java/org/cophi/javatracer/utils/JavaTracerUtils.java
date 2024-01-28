package org.cophi.javatracer.utils;

import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

public class JavaTracerUtils {

    protected static final String ARG_TYPE_SEPARATOR = ":";

    private JavaTracerUtils() {
    }

    public static String getMethodFullName(final String className, final String methodName) {
        return className + "#" + methodName;
    }

    public static String encodeArgumentName(final MethodGen methodGen) {
        return JavaTracerUtils.encodeArgumentNames(JavaTracerUtils.getArgumentNames(methodGen));
    }

    public static String encodeArgumentNames(final String[] argumentNames) {
        return String.join(JavaTracerUtils.ARG_TYPE_SEPARATOR, argumentNames);
    }

    public static String encodeArgumentTypes(final Type[] argumentTypes) {
        return StringUtils.join(JavaTracerUtils.ARG_TYPE_SEPARATOR, (Object) argumentTypes);
    }

    public static String encodeArgumentTypes(final MethodGen methodGen) {
        return JavaTracerUtils.encodeArgumentTypes(methodGen.getArgumentTypes());
    }

    public static String[] getArgumentNames(final MethodGen methodGen) {
        String methodString = methodGen.toString();
        String args = methodString.substring(methodString.indexOf("(") + 1,
            methodString.indexOf(")"));
        String[] argList = args.split(",");
        for (int i = 0; i < argList.length; i++) {
            argList[i] = argList[i].trim();
            argList[i] = argList[i].substring(argList[i].indexOf(" ") + 1);
        }
        return argList;
    }
}
