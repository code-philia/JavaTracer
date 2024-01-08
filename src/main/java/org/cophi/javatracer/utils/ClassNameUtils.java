package org.cophi.javatracer.utils;

public class ClassNameUtils {

    private ClassNameUtils() {

    }

    public static String canonicalToClassFileName(String canonicalName) {
        return canonicalName.replace('.', '/');
    }

    public static String classFileNameToCanonicalName(String binaryName) {
        return binaryName.replace('/', '.');
    }

}
