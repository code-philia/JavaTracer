package org.cophi.javatracer.instrumentation.instrumentator.instructionInfo;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LocalVariableInstruction;

public class ReadWriteInstructionInfoFactory {

    private ReadWriteInstructionInfoFactory() {
        throw new IllegalStateException("Factory class");
    }

    public static ReadWriteInstructionInfo createRWInstructionInfo(
        final InstructionHandle instructionHandle, final int lineNumber, final
    ConstantPoolGen constantPoolGen, final Method method, final String locationId) {

        Instruction instruction = instructionHandle.getInstruction();
        if (instruction instanceof FieldInstruction) {
            return new FieldInstructionInfo(instructionHandle, lineNumber, constantPoolGen);
        } else if (instruction instanceof ArrayInstruction) {
            return new ArrayInstructionInfo(instructionHandle, lineNumber, constantPoolGen);
        } else if (instruction instanceof LocalVariableInstruction) {
            return new LocalVariableInstructionInfo(instructionHandle, lineNumber, constantPoolGen,
                method, locationId);
        } else {
            return null;
        }
    }
}
