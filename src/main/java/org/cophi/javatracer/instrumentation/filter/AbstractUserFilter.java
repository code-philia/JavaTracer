package org.cophi.javatracer.instrumentation.filter;

import org.cophi.javatracer.instrumentation.instr.instruction.info.LineInstructionInfo;
import java.util.List;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.LineNumberGen;

public abstract class AbstractUserFilter {

    public boolean isInstrumentableClass(String className) {
        return true;
    }

    public boolean isInstrumentableMethod(String className, Method method,
        LineNumberGen[] lineNumbers) {
        return true;
    }

    public void filter(List<LineInstructionInfo> lineInsnInfos, String className, Method method) {
        // do nothing by default
    }

}
