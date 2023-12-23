package utils;

import java.lang.reflect.Method;

/**
 * Utility methods for dealing with method signatures.
 *
 * @author LLT
 */
public class SignatureUtils {

  /** Compute the JVM method descriptor for the method. */
  public static String getSignature(Method meth) {
    StringBuilder sb = new StringBuilder();

    sb.append("(");

    Class<?>[] params = meth.getParameterTypes(); // avoid clone
    for (Class<?> param : params) {
      sb.append(getSignature(param));
    }
    sb.append(")");
    sb.append(getSignature(meth.getReturnType()));
    return sb.toString();
  }

  public static boolean isSignature(String signature) {
    if (signature.length() == 1) {
      return true;
    } else {
      return (signature.startsWith("L") && signature.endsWith(";")) || signature.startsWith("[");
    }
  }

  public static String signatureToName(String signature) {
    signature = trimSignature(signature);

    StringBuilder arrayString = new StringBuilder();
    int startArray = signature.indexOf("[");
    int endArray = signature.lastIndexOf("[");

    if (startArray != -1) {
      arrayString.append("[]".repeat(Math.max(0, endArray - startArray + 1)));
      signature = signature.substring(endArray + 1);
    }

    switch (signature) {
      case "I" -> {
        return "int" + arrayString;
      }
      case "B" -> {
        return "byte" + arrayString;
      }
      case "J" -> {
        return "long" + arrayString;
      }
      case "F" -> {
        return "float" + arrayString;
      }
      case "D" -> {
        return "double" + arrayString;
      }
      case "S" -> {
        return "short" + arrayString;
      }
      case "C" -> {
        return "char" + arrayString;
      }
      case "Z" -> {
        return "boolean" + arrayString;
      }
      case "V" -> {
        return "void";
      }
    }

    signature = signature.substring(1);
    signature = signature.replace("/", ".");
    signature += arrayString;

    return signature;
  }

  /** Compute the JVM signature for the class. */
  public static String getSignature(Class<?> clazz) {
    String type = null;
    if (clazz.isArray()) {
      Class<?> cl = clazz;
      int dimensions = 0;
      while (cl.isArray()) {
        dimensions++;
        cl = cl.getComponentType();
      }
      type = "[".repeat(Math.max(0, dimensions)) + getSignature(cl);
    } else if (clazz.isPrimitive()) {
      if (clazz == Integer.TYPE) {
        type = "I";
      } else if (clazz == Byte.TYPE) {
        type = "B";
      } else if (clazz == Long.TYPE) {
        type = "J";
      } else if (clazz == Float.TYPE) {
        type = "F";
      } else if (clazz == Double.TYPE) {
        type = "D";
      } else if (clazz == Short.TYPE) {
        type = "S";
      } else if (clazz == Character.TYPE) {
        type = "C";
      } else if (clazz == Boolean.TYPE) {
        type = "Z";
      } else if (clazz == Void.TYPE) {
        type = "V";
      }
    } else {
      type = getSignature(clazz.getName());
    }
    return type;
  }

  public static String getSignature(String className) {
    return "L" + className.replace('.', '/') + ";";
  }

  public static String extractMethodName(String methodNameOrSign) {
    int endNameIdx = methodNameOrSign.indexOf("(");
    if (endNameIdx < 0) {
      return methodNameOrSign;
    }
    String fullMethodName = methodNameOrSign.substring(0, endNameIdx);
    if (fullMethodName.contains(".")) {
      return fullMethodName.substring(fullMethodName.lastIndexOf(".") + 1);
    }
    return fullMethodName;
  }

  public static String extractSignature(String methodNameAndSign) {
    int endNameIdx = methodNameAndSign.indexOf("(");
    if (endNameIdx > 1) {
      return methodNameAndSign.substring(endNameIdx);
    }
    return methodNameAndSign;
  }

  public static String trimSignature(String typeSign) {
    return typeSign.replace(";", "");
  }

  public static String createMethodNameSign(String methodName, String signature) {
    return methodName + signature;
  }

  public static String createMethodNameSign(Method method) {
    return createMethodNameSign(method.getName(), getSignature(method));
  }
}
