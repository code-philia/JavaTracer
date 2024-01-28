package org.cophi.javatracer.utils;

import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

public class JavaTracerUtils {

    protected static final String ARG_TYPE_SEPARATOR = ":";

    private JavaTracerUtils() {
    }

    public static Object[] generateCommonNodeList(Object[] nodeList1,
        Object[] nodeList2) {
        int[][] commonLengthTable = buildLeveshteinTable(nodeList1, nodeList2);

        int commonLength = commonLengthTable[nodeList1.length][nodeList2.length];
        Object[] commonList = new Object[commonLength];

        for (int k = commonLength - 1, i = nodeList1.length, j = nodeList2.length;
            (i > 0 && j > 0); ) {
            if (nodeList1[i - 1].equals(nodeList2[j - 1])) {
                commonList[k] = nodeList1[i - 1];
                k--;
                i--;
                j--;
            } else {
                if (commonLengthTable[i - 1][j] >= commonLengthTable[i][j - 1]) {
                    i--;
                } else {
                    j--;
                }
            }
        }

        return commonList;
    }

    public static int[][] buildLeveshteinTable(Object[] nodeList1, Object[] nodeList2) {
        int[][] commonLengthTable = new int[nodeList1.length + 1][nodeList2.length + 1];
        for (int i = 0; i < nodeList1.length + 1; i++) {
            commonLengthTable[i][0] = 0;
        }
        for (int j = 0; j < nodeList2.length + 1; j++) {
            commonLengthTable[0][j] = 0;
        }

        for (int i = 1; i < nodeList1.length + 1; i++) {
            for (int j = 1; j < nodeList2.length + 1; j++) {
                if (nodeList1[i - 1].equals(nodeList2[j - 1])) {
                    commonLengthTable[i][j] = commonLengthTable[i - 1][j - 1] + 1;
                } else {
                    commonLengthTable[i][j] =
                        (commonLengthTable[i - 1][j] >= commonLengthTable[i][j - 1])
                            ? commonLengthTable[i - 1][j]
                            : commonLengthTable[i][j - 1];
                }

            }
        }

        return commonLengthTable;
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
