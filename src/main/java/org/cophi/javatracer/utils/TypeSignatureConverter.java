package org.cophi.javatracer.utils;

public class TypeSignatureConverter {

    private TypeSignatureConverter() {
        throw new IllegalStateException("Utility class");
    }

    public static String convertToClassName(String classSignature) {
        String name = classSignature.replace("/", ".");
        if (name.startsWith("[")) {
            return name;
        } else if (name.startsWith("L") && name.endsWith(";")) {
            return name.substring(1, name.length() - 1);
        }

        return switch (name) {
            case "I" -> "int";
            case "B" -> "byte";
            case "J" -> "long";
            case "F" -> "float";
            case "D" -> "double";
            case "S" -> "short";
            case "C" -> "char";
            case "Z" -> "boolean";
            default -> name;
        };
    }
}
