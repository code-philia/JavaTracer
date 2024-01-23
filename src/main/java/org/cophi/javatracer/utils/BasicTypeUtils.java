package org.cophi.javatracer.utils;

import org.apache.bcel.Const;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;

public class BasicTypeUtils {

    public static void appendObjectConvertInstruction(Type returnType, InstructionList newInsns,
        ConstantPoolGen constPool) {
        if (!Type.VOID.equals(returnType) && returnType instanceof BasicType) {
            newInsns.append(
                new INVOKESTATIC(getValueOfMethodIdx((BasicType) returnType, constPool)));
        }
    }

    public static int getToPrimitiveValueMethodIdx(BasicType type, ConstantPoolGen cpg) {
        int idx = switch (type.getType()) {
            case Const.T_INT -> cpg.addMethodref("java/lang/Integer", "intValue", "()I");
            case Const.T_BOOLEAN -> cpg.addMethodref("java/lang/Boolean", "booleanValue", "()Z");
            case Const.T_FLOAT -> cpg.addMethodref("java/lang/Float", "floatValue", "()F");
            case Const.T_CHAR -> cpg.addMethodref("java/lang/Character", "charValue", "()C");
            case Const.T_DOUBLE -> cpg.addMethodref("java/lang/Double", "doubleValue", "()D");
            case Const.T_LONG -> cpg.addMethodref("java/lang/Long", "longValue", "()J");
            case Const.T_SHORT -> cpg.addMethodref("java/lang/Short", "shortValue", "()S");
            case Const.T_BYTE -> cpg.addMethodref("java/lang/Byte", "byteValue", "()B");
            default -> throw new IllegalArgumentException("Unhandled type: " + type);
        };
        return idx;
    }

    public static int getValueOfMethodIdx(BasicType type, ConstantPoolGen cpg) {
        int idx = switch (type.getType()) {
            case Const.T_INT ->
                cpg.addMethodref("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            case Const.T_BOOLEAN ->
                cpg.addMethodref("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
            case Const.T_FLOAT ->
                cpg.addMethodref("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
            case Const.T_CHAR -> cpg.addMethodref("java/lang/Character", "valueOf",
                "(C)Ljava/lang/Character;");
            case Const.T_DOUBLE ->
                cpg.addMethodref("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
            case Const.T_LONG ->
                cpg.addMethodref("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
            case Const.T_SHORT ->
                cpg.addMethodref("java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
            case Const.T_BYTE ->
                cpg.addMethodref("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
            default -> throw new IllegalArgumentException("Unhandled type: " + type);
        };
        return idx;
    }

    public static ReferenceType getCorrespondingPrimitiveType(BasicType type) {
        return switch (type.getType()) {
            case Const.T_INT -> ObjectType.getInstance(Integer.class.getName());
            case Const.T_BOOLEAN -> ObjectType.getInstance(Boolean.class.getName());
            case Const.T_FLOAT -> ObjectType.getInstance(Float.class.getName());
            case Const.T_CHAR -> ObjectType.getInstance(Character.class.getName());
            case Const.T_DOUBLE -> ObjectType.getInstance(Double.class.getName());
            case Const.T_LONG -> ObjectType.getInstance(Long.class.getName());
            case Const.T_SHORT -> ObjectType.getInstance(Short.class.getName());
            case Const.T_BYTE -> ObjectType.getInstance(Byte.class.getName());
            default -> throw new IllegalArgumentException("Unhandled type: " + type);
        };

    }
}
