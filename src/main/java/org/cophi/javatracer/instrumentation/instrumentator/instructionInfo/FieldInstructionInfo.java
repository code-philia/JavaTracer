package org.cophi.javatracer.instrumentation.instrumentator.instructionInfo;

import org.apache.bcel.Const;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.cophi.javatracer.utils.SignatureUtils;

public class FieldInstructionInfo extends ReadWriteInstructionInfo {

    public static final short[] storeOpCodes = {Const.PUTFIELD, Const.PUTSTATIC};
    protected String referenceType;
    protected Type fieldBcType;

    public FieldInstructionInfo(final InstructionHandle instructionHandle, final int lineNumber) {
        super(instructionHandle, lineNumber);
    }

    public FieldInstructionInfo(final InstructionHandle instructionHandle, final int lineNumber,
        final ConstantPoolGen constantPoolGen) {
        this(instructionHandle, lineNumber);
        if (instructionHandle.getInstruction() instanceof FieldInstruction fieldInstruction) {
            ReferenceType referenceType = fieldInstruction.getReferenceType(constantPoolGen);
            this.setFieldBcType(fieldInstruction.getFieldType(constantPoolGen));
            this.setReferenceType(referenceType.getSignature());
            this.setVarStackSize(referenceType.getSize());
            this.setVarType(fieldInstruction.getSignature(constantPoolGen));
            this.setVarName(fieldInstruction.getFieldName(constantPoolGen));
            this.setIsStore(this.existIn(fieldInstruction.getOpcode(), storeOpCodes));
        } else {
            throw new IllegalArgumentException(
                "Given instruction is not a field instruction:" + instructionHandle);
        }
    }


    public Type getFieldBcType() {
        return fieldBcType;
    }

    public void setFieldBcType(Type fieldBcType) {
        this.fieldBcType = fieldBcType;
    }

    public String getFieldName() {
        return this.getVarName();
    }

    public int getFieldStackSize() {
        return this.fieldBcType.getSize();
    }

    public String getFieldType() {
        return this.getVarType();
    }

    @Override
    public FieldInstruction getInstruction() {
        return (FieldInstruction) super.getInstruction();
    }

    public boolean isComputationalType1() {
        return getFieldStackSize() == 1;
    }

    public boolean isComputationalType2() {
        return getFieldStackSize() == 2;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = SignatureUtils.signatureToName(referenceType);
    }
}
