package org.cophi.javatracer.utils;


import org.apache.bcel.classfile.Method;

public class MethodUtils {

    private MethodUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getMethodFullName(final String className, final Method method) {
        String sb = className
            + "#"
            + method.getName()
            + method.getSignature().replace(";", ":");
        return sb;
    }
}
