package org.cophi.javatracer.instrumentation.instrumentator.instructionInfo;

import org.apache.bcel.Const;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.Type;

public class ArrayInstructionInfo extends ReadWriteInstructionInfo {

    public static final short[] storeOpCodes = {Const.AASTORE,
        Const.FASTORE, Const.LASTORE,
        Const.CASTORE, Const.IASTORE, Const.BASTORE, Const.SASTORE, Const.DASTORE};
    protected Type elementType;

    public ArrayInstructionInfo(final InstructionHandle instructionHandle, final int lineNumber) {
        super(instructionHandle, lineNumber);
    }

    public ArrayInstructionInfo(final InstructionHandle instructionHandle, final int lineNumber,
        final ConstantPoolGen constantPoolGen) {
        this(instructionHandle, lineNumber);
        if (instructionHandle.getInstruction() instanceof ArrayInstruction arrayInstruction) {
            Type elementType = arrayInstruction.getType(constantPoolGen);
            this.setElementType(elementType);
            this.setVarType(elementType.getSignature());
            this.setVarStackSize(elementType.getSize());
            this.setIsStore(
                this.existIn(arrayInstruction.getOpcode(), ArrayInstructionInfo.storeOpCodes));
        } else {
            throw new IllegalArgumentException(
                "Given instruction is not a array instruction:" + instructionHandle);
        }
    }

    public Type getElementType() {
        return elementType;
    }

    public void setElementType(Type elementType) {
        this.elementType = elementType;
    }

    @Override
    public ArrayInstruction getInstruction() {
        return (ArrayInstruction) super.getInstruction();
    }
}
