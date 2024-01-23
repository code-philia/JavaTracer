package org.cophi.javatracer.instrumentation.instrumentator.instructionInfo;

import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.cophi.javatracer.utils.SignatureUtils;

public class ReadWriteInstructionInfo {

    protected InstructionHandle instructionHandle;
    protected int lineNumber;
    protected String varName;
    protected String varType;
    protected int varStackSize;
    protected boolean isStore;

    public ReadWriteInstructionInfo(final InstructionHandle instructionHandle,
        final int lineNumber) {
        this.instructionHandle = instructionHandle;
        this.lineNumber = lineNumber;
    }

    public Instruction getInstruction() {
        return this.getInstructionHandle().getInstruction();
    }

    public InstructionHandle getInstructionHandle() {
        return instructionHandle;
    }

    public void setInstructionHandle(InstructionHandle instructionHandle) {
        this.instructionHandle = instructionHandle;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public int getVarStackSize() {
        return varStackSize;
    }

    public void setVarStackSize(int varStackSize) {
        this.varStackSize = varStackSize;
    }

    public String getVarType() {
        return varType;
    }

    public void setVarType(String varType) {
        this.varType = SignatureUtils.signatureToName(varType);
    }

    public boolean isComputationalType1() {
        return this.varStackSize == 1;
    }

    public boolean isComputationalType2() {
        return this.varStackSize == 2;
    }

    public boolean isNextToAconstNull() {
        InstructionHandle prevInstruction = this.instructionHandle.getPrev();
        return prevInstruction != null && prevInstruction.getInstruction() instanceof ACONST_NULL;
    }

    public boolean isStore() {
        return isStore;
    }

    public void setIsStore(boolean store) {
        isStore = store;
    }

    protected boolean existIn(final short opCode, final short... targetOpCodes) {
        for (short targetOpCode : targetOpCodes) {
            if (opCode == targetOpCode) {
                return true;
            }
        }
        return false;
    }
}
