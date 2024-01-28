package org.cophi.javatracer.utils;

import org.apache.bcel.generic.MethodGen;

/**
 * Utility classes used to handle the naming convention of class and method in JavaTracer
 */
public class NamingUtils {

    public static final String NAME_SEPARATOR = "#";

    private NamingUtils() {

    }

    /**
     * Convert canonical name to class URI name <br/>
     * <p>
     * For example, {@code org.example.Foo} will be converted to {@code org/example/Foo}
     * </p>
     *
     * @param canonicalName Canonical name
     * @return Class binary name
     */
    public static String canonicalToClassBinaryName(String canonicalName) {
        return canonicalName.replace('.', '/');
    }

    /**
     * Convert binary class name to canonical name <br/> For example, {@code org/example/Foo} will
     * be converted to {@code org.example.Foo}
     *
     * @param binaryName Binary name of class
     * @return Canonical name
     */
    public static String classBinaryNameToCanonicalName(String binaryName) {
        return binaryName.replace('/', '.');
    }

    /**
     * Generate method id with the format {@code {className}#{methodName}{methodDescriptor}}
     * <p>
     * For example: {@code org.example.Foo#bar()V}
     * </p>
     *
     * @param className        Canonical name of class that method belongs to
     * @param methodName       Method name
     * @param methodDescriptor Method descriptor
     * @return Method id
     */
    public static String genMethodId(final String className, final String methodName,
        final String methodDescriptor) {
        return className + NAME_SEPARATOR + methodName + methodDescriptor;
    }

    /**
     * Extract canonical name of class from method id <br/> For example, you will get
     * {@code org.example.class} from method id {@code org.example.class#method()V}
     *
     * @param methodID Method id
     * @return Canonical name of class
     */
    public static String getClassNameFromMethodId(final String methodID) {
        return methodID.split(NAME_SEPARATOR)[0];
    }

    /**
     * Extract method name from method id <br/> For example, you will get {@code method} from method
     * id {@code org.example.class#method()V}
     *
     * @param methodId Method id
     * @return Method name
     */
    public static String getMethodNameFromMethodId(final String methodId) {
        return methodId.substring(methodId.indexOf(NAME_SEPARATOR) + 1,
            methodId.indexOf("("));
    }

    /**
     * Extract method descriptor from method id <br/> For example, you will get {@code ()V} from
     * method id {@code org.example.class#method()V}
     *
     * @param methodId Method id
     * @return Method descriptor
     */
    public static String getMethodDescriptorFromMethodId(final String methodId) {
        return methodId.substring(methodId.indexOf("("));
    }

    /**
     * Extract method signature from method id <br/> For example, you will get {@code method()V}
     * from method id {@code org.example.class#method()V}
     *
     * @param className Canonical name of class
     * @param methodGen methodGen object
     * @return Method signature
     */
    public static String genMethodId(final String className, final MethodGen methodGen) {
        // In bcel library, the method MethodGen#getSignature is actually returning the method descriptor
        return NamingUtils.genMethodId(className, methodGen.getName(), methodGen.getSignature());
    }

    /**
     * Generate method id with the format {@code {className}#{methodSignature}}. <br/> For example,
     * {@code org.example.Foo#bar()V}
     *
     * @param className       Canonical class name
     * @param methodSignature Method signature
     * @return Method id
     */
    public static String genMethodId(final String className, final String methodSignature) {
        return className + NAME_SEPARATOR + methodSignature;
    }

    public static String getCompilationUnit(final String className) {
        // The case which this function is missing to handle: Non-public top level class
        if (className.contains("$")) {
            return className.substring(0, className.indexOf("$"));
        } else {
            return className;
        }
    }

    public static String extractPackageName(final String className) {
        return className.substring(0, className.lastIndexOf("."));
    }


}
