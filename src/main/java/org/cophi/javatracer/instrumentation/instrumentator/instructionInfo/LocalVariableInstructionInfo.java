package org.cophi.javatracer.instrumentation.instrumentator.instructionInfo;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.Type;

public class LocalVariableInstructionInfo extends ReadWriteInstructionInfo {

    public static final short[] storeOpCodes = {Const.FSTORE, Const.IINC, Const.DSTORE,
        Const.ASTORE,
        Const.ISTORE, Const.LSTORE};
    protected int varScopeStartLine;
    protected int varScopeEndLine;

    public LocalVariableInstructionInfo(final InstructionHandle instructionHandle,
        final int lineNumber) {
        super(instructionHandle, lineNumber);
    }

    public LocalVariableInstructionInfo(final InstructionHandle instructionHandle,
        final int lineNumber, final ConstantPoolGen constantPoolGen,
        final Method method, final String locationId) {
        this(instructionHandle, lineNumber);
        if (instructionHandle.getInstruction() instanceof LocalVariableInstruction localVariableInstruction) {
            final LocalVariableTable localVariableTable = method.getLocalVariableTable();
            LocalVariable localVariable = null;
            if (localVariableTable != null) {
                localVariable = localVariableTable.getLocalVariable(
                    localVariableInstruction.getIndex(),
                    instructionHandle.getPosition() + localVariableInstruction.getLength());
            }

            final Type type = localVariableInstruction.getType(constantPoolGen);
            if (localVariable == null) {
                String localVarName = String.format("%s:%s", locationId,
                    instructionHandle.getPosition());
                this.setVarName(localVarName);
                String localVarTypeSign = type.getSignature();
                this.setVarType(localVarTypeSign);
                this.setIsStore(this.existIn(localVariableInstruction.getCanonicalTag(),
                    LocalVariableInstructionInfo.storeOpCodes));
                this.setVarStackSize(type.getSize());
                this.setVarScopeStartLine(LineInstructionInfo.UNKNOWN_LINE_NUMBER);
                this.setVarScopeEndLine(LineInstructionInfo.UNKNOWN_LINE_NUMBER);
            } else {
                this.setVarName(localVariable.getName());
                this.setVarType(localVariable.getSignature());
                this.setIsStore(this.existIn(localVariableInstruction.getCanonicalTag(),
                    LocalVariableInstructionInfo.storeOpCodes));
                this.setVarStackSize(type.getSize());
                int startPC = -1;
                int endPC = -1;
                for (LocalVariable var : localVariableTable.getLocalVariableTable()) {
                    if (var.getIndex() == localVariable.getIndex()) {
                        if (startPC == -1) {
                            startPC = var.getStartPC();
                            endPC = startPC + var.getLength();
                        } else {
                            int sPC = var.getStartPC();
                            int ePC = sPC + var.getLength();

                            if (sPC < startPC) {
                                startPC = sPC;
                            }

                            if (ePC > endPC) {
                                endPC = ePC;
                            }
                        }
                    }
                }

                final LineNumberTable lineNumberTable = method.getLineNumberTable();
                this.setVarScopeStartLine(lineNumberTable.getSourceLine(startPC));
                this.setVarScopeEndLine(lineNumberTable.getSourceLine(endPC));

            }
        } else {
            throw new IllegalArgumentException(
                "Given instruction is not local variable instruction: " + instructionHandle);
        }
    }

    @Override
    public LocalVariableInstruction getInstruction() {
        return (LocalVariableInstruction) super.getInstruction();
    }

    public int getVarScopeEndLine() {
        return varScopeEndLine;
    }

    public void setVarScopeEndLine(int varScopeEndLine) {
        this.varScopeEndLine = varScopeEndLine;
    }

    public int getVarScopeStartLine() {
        return varScopeStartLine;
    }

    public void setVarScopeStartLine(int varScopeStartLine) {
        this.varScopeStartLine = varScopeStartLine;
    }
}
