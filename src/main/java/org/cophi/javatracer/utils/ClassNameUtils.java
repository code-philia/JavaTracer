package org.cophi.javatracer.utils;

public class ClassNameUtils {

    private ClassNameUtils() {

    }

    public static String canonicalToClassURIName(String canonicalName) {
        return canonicalName.replace('.', '/');
    }

    public static String classURINameToCanonicalName(String binaryName) {
        return binaryName.replace('/', '.');
    }

}
