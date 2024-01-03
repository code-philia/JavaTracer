package org.cophi.javatracer.utils;

public class ClassNameUtils {

    private ClassNameUtils() {

    }

    public static String canonicalToBinaryName(String canonicalName) {
        return canonicalName.replace('.', '/');
    }

}
