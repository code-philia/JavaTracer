package org.cophi.javatracer.instrumentation.filters;

import java.util.List;
import org.apache.bcel.classfile.Method;
import org.cophi.javatracer.instrumentation.instrumentator.instructionInfo.LineInstructionInfo;

public abstract class UserFilter {

    public abstract void filterInstructions(final List<LineInstructionInfo> instructions,
        final String className, final Method method);

    public abstract boolean isInstrumentableClass(final String className);

    public abstract boolean isInstrumentableMethod(final String className, final Method method);


}
