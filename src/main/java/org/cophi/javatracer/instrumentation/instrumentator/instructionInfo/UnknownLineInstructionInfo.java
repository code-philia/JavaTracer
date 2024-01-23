package org.cophi.javatracer.instrumentation.instrumentator.instructionInfo;

import java.util.Arrays;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.cophi.javatracer.instrumentation.agents.LoadedClassRecord;

public class UnknownLineInstructionInfo extends LineInstructionInfo {

    public UnknownLineInstructionInfo(ClassGen classGen,
        MethodGen methodGen,
        Method method) {
        this.lineNumber = -1;
        this.instructionHandles = Arrays.asList(
            methodGen.getInstructionList().getInstructionHandles());
        boolean isInternalClass = LoadedClassRecord.getInstance()
            .isInternalClass(classGen.getClassName());

        final String locationId = this.genLocationId(classGen, method);
        this.readWriteInstructionInfos = this.extractRWInstructions(locationId, isInternalClass,
            classGen.getConstantPool(), method);
        this.invokeInstructions = this.extractInvokeInstructions();
        this.returnInstructions = this.extractReturnInstructions();
    }

    protected String genLocationId(final ClassGen classGen, final Method method) {
        return classGen.getClassName() + "." + method.getName();
    }
}
