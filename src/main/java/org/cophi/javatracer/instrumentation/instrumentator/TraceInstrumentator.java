package org.cophi.javatracer.instrumentation.instrumentator;

import java.util.List;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ClassGenException;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.generic.Type;
import org.cophi.javatracer.configs.EntryPoint;
import org.cophi.javatracer.configs.JavaTracerConfig;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.instrumentation.agents.LoadedClassRecord;
import org.cophi.javatracer.instrumentation.filters.JavaTracerFilter;
import org.cophi.javatracer.instrumentation.instrumentator.instructionInfo.ArrayInstructionInfo;
import org.cophi.javatracer.instrumentation.instrumentator.instructionInfo.FieldInstructionInfo;
import org.cophi.javatracer.instrumentation.instrumentator.instructionInfo.LineInstructionInfo;
import org.cophi.javatracer.instrumentation.instrumentator.instructionInfo.LocalVariableInstructionInfo;
import org.cophi.javatracer.instrumentation.instrumentator.instructionInfo.ReadWriteInstructionInfo;
import org.cophi.javatracer.instrumentation.tracer.Tracer;
import org.cophi.javatracer.instrumentation.tracer.Tracer.Methods;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.utils.JavaTracerUtils;
import org.cophi.javatracer.utils.NamingUtils;

public class TraceInstrumentator extends AbstractInstrumentator {

    protected static final String TRACER_VAR_NAME = "$tracer";
    protected static final String TEMP_VAR_NAME = "$tempVar";
    protected int tempVarIdx = 0;

    protected JavaTracerInstructionFactory factory = null;
    protected LocalVariableGen tracerVar = null;
    protected LocalVariableGen classNameVar = null;
    protected LocalVariableGen methodIdVar = null;

    public TraceInstrumentator(final ProjectConfig projectConfig) {
        super(projectConfig);
    }

    public LocalVariableGen addTempVar(final MethodGen methodGen, final Type type,
        final InstructionHandle instructionHandle) {
        return methodGen.addLocalVariable(this.nextTemVarName(), type, instructionHandle,
            instructionHandle.getNext());
    }

    public boolean checkIsThreadMethod(final Method method, final JavaClass javaClass) {
        if (method.getName().equals("run")) {
            try {
                for (JavaClass interfaceClass : javaClass.getAllInterfaces()) {
                    if (interfaceClass.getClassName().equals(Runnable.class.getCanonicalName())) {
                        return true;
                    }
                }
                for (JavaClass superClass : javaClass.getSuperClasses()) {
                    if (superClass.getClassName().equals(Thread.class.getCanonicalName())) {
                        return true;
                    }
                }
            } catch (ClassNotFoundException e) {
                Log.error(e.getLocalizedMessage(), this.getClass());
            }
        }
        return false;
    }

    @Override
    public byte[] instrument(final String className, JavaClass javaClass) {
        ClassGen classGen = new ClassGen(javaClass);
        this.factory = new JavaTracerInstructionFactory(classGen);

        JavaTracerFilter filter = JavaTracerFilter.getInstance();
        JavaClass newJavaClass = null;
        for (Method method : javaClass.getMethods()) {

            if (method.isNative() || method.isAbstract() || method.getCode() == null) {
                // Only instrument the method with code
                continue;
            }
            try {
                final boolean isEntryMethod = this.checkIsEntryMethod(className, method);
                final boolean isThreadMethod = this.checkIsThreadMethod(method, javaClass);
                if (!filter.isInstrumentableMethod(className, method)) {
                    continue;
                }
                boolean isInternalClass = LoadedClassRecord.getInstance()
                    .isInternalClass(className);
                MethodGen newMethod = this.instrumentMethod(classGen, method, isInternalClass,
                    isEntryMethod, isThreadMethod);

                if (this.isBytecodeExceedLimit(newMethod)) {
                    Log.warn("Method exceed maximum instruction offset: "
                            + JavaTracerUtils.getMethodFullName(className, method.getName()),
                        this.getClass());
                } else {
                    newMethod.getInstructionList().setPositions(true);
                    newMethod.setMaxLocals();
                    newMethod.setMaxStack();
                    classGen.replaceMethod(method, newMethod.getMethod());
                }

                newJavaClass = classGen.getJavaClass();
                newJavaClass.setConstantPool(classGen.getConstantPool().getFinalConstantPool());
            } catch (Exception e) {
                Log.error(e.getLocalizedMessage(), this.getClass());
            }
        }

        Log.info("Instrument: " + className);
        if (newJavaClass != null) {
            return newJavaClass.getBytes();
        }
        return null;
    }

    protected void appendInstruction(final InstructionList instructionList,
        final InstructionList newList, InstructionHandle instructionHandle) {
        this.updateTarget(instructionHandle, instructionHandle, newList.getEnd());
        instructionList.append(instructionHandle, newList);
    }

    protected boolean checkIsEntryMethod(final String className, final Method method) {
        final EntryPoint entryPoint = this.projectConfig.getEntryPoint();
        if (entryPoint != null && className.equals(entryPoint.getClassName())) {
            return entryPoint.matchMethod(method.getName(), method.getSignature());
        }
        return false;
    }

    protected InstructionList getReadWriteFieldInstructions(
        final FieldInstructionInfo fieldInstructionInfo) {
        FieldInstruction instruction = fieldInstructionInfo.getInstruction();
        if (instruction instanceof PUTFIELD) {
            return this.factory.createInvokeWriteFieldMethod(this.tracerVar, fieldInstructionInfo,
                this.classNameVar, this.methodIdVar);
        } else if (instruction instanceof PUTSTATIC) {
            return this.factory.createInvokeWriteStaticFieldMethod(this.tracerVar,
                fieldInstructionInfo, this.classNameVar, this.methodIdVar);
        } else if (instruction instanceof GETFIELD) {
            return this.factory.createInvokeReadFieldMethod(this.tracerVar, fieldInstructionInfo,
                this.classNameVar, this.methodIdVar);
        } else if (instruction instanceof GETSTATIC) {
            return this.factory.createInvokeReadStaticFieldMethod(this.tracerVar,
                fieldInstructionInfo, this.classNameVar, this.methodIdVar);
        } else {
            return null;
        }
    }

    protected void injectCodeInitTracer(final MethodGen methodGen,
        final ConstantPoolGen constantPoolGen, final List<LineInstructionInfo> lineInstructionInfo,
        final boolean isInternalClass, final boolean isEntryMethod) {

        final InstructionList newInstructions = new InstructionList();
        if (isEntryMethod) {
            newInstructions.append(this.factory.createInvokeTracerMethod(Methods.START));
        }

        // Store className variable
        final String className = NamingUtils.classBinaryNameToCanonicalName(
            methodGen.getClassName());
        newInstructions.append(this.factory.createAssignStringToVar(className, this.classNameVar));

        // Store method signature variable
        final String methodId = NamingUtils.genMethodId(className, methodGen);
        newInstructions.append(
            this.factory.createAssignStringToVar(methodId, this.methodIdVar));

        final int methodStartLineNumber = lineInstructionInfo.stream().mapToInt(
                LineInstructionInfo::getLineNumber).min()
            .orElse(LineInstructionInfo.UNKNOWN_LINE_NUMBER);
        final int methodEndLineNumber = lineInstructionInfo.stream().mapToInt(
                LineInstructionInfo::getLineNumber).max()
            .orElse(LineInstructionInfo.UNKNOWN_LINE_NUMBER);
        final String encodedMethodArgumentNames = JavaTracerUtils.encodeArgumentName(methodGen);
        final String encodedMethodArgumentTypes = JavaTracerUtils.encodeArgumentTypes(methodGen);
        final LocalVariableGen argumentObjectsVar = this.createMethodParamTypesObjectArrayVar(
            methodGen, constantPoolGen, newInstructions.getStart(), newInstructions,
            this.nextTemVarName());
        newInstructions.append(this.factory.createInvokeCreateTracerMethod_ExecutionTracer(
            isInternalClass, this.classNameVar, this.methodIdVar, methodStartLineNumber,
            methodEndLineNumber, encodedMethodArgumentNames, encodedMethodArgumentTypes,
            argumentObjectsVar));
        newInstructions.append(
            InstructionFactory.createStore(this.tracerVar.getType(), this.tracerVar.getIndex()));

        InstructionList instructionList = methodGen.getInstructionList();
        InstructionHandle startInstruction = instructionList.getStart();
        instructionList.insert(startInstruction, newInstructions);
        newInstructions.dispose();
    }

    protected void injectCodeTracerExit(final InstructionList instructionList,
        final LineInstructionInfo lineInstructionInfo, final boolean isEntryMethod,
        final boolean isThreadMethod) {
        for (InstructionHandle instructionHandle : lineInstructionInfo.getExitInstructions()) {
            InstructionList newInstructions = this.factory.createInvokeHitMethodEndMethod(
                this.tracerVar, this.classNameVar, this.methodIdVar,
                lineInstructionInfo.getLineNumber());
            if (isEntryMethod || isThreadMethod) {
                newInstructions.append(this.factory.createInvokeExitProgramMethod(
                    this.tracerVar, this.classNameVar, this.methodIdVar));
            }
            this.insertInstructionHandle(instructionList, newInstructions, instructionHandle);
            newInstructions.dispose();
        }
    }

    protected void injectCodeTracerHitLine(final InstructionList instructionList,
        final LineInstructionInfo lineInstructionInfo) {
        InstructionList newList;
        if (lineInstructionInfo.hasExceptionTarget()) {
            newList = this.factory.createInvokeHitExceptionTarget(tracerVar,
                lineInstructionInfo.getLineNumber(), classNameVar, methodIdVar);
        } else {
            final int readVarCount = lineInstructionInfo.countReadInstructions();
            final int writtenVarCount = lineInstructionInfo.countWrittenInstructions();
            newList = this.factory.createInvokeHitLineMethod(tracerVar,
                lineInstructionInfo.getLineNumber(), classNameVar, methodIdVar,
                readVarCount, writtenVarCount, lineInstructionInfo.getInstructionHandles());
        }
        this.insertInstructionHandle(instructionList, newList,
            lineInstructionInfo.getLineInstructionHandle());
        newList.dispose();
    }

    protected void injectCodeTracerInvokeMethod(final LineInstructionInfo lineInstructionInfo,
        final MethodGen methodGen, final boolean isInternalClass) {
        for (InstructionHandle instructionHandle : lineInstructionInfo.getInvokeInstructions()) {
            InvokeInstruction instruction = (InvokeInstruction) instructionHandle.getInstruction();
            final String className = instruction.getClassName(methodGen.getConstantPool());
            if (instruction instanceof INVOKESPECIAL && "java.lang.Object".equals(
                className) && methodGen.getName().equals("<init>")) {
                continue;
            }
//            LocalVariableGen argObjsVar = this.addTempVar(methodGen,
//                new ArrayType(Type.OBJECT, 1), instructionHandle);
            InstructionList newInstructions = this.factory.createInvokeHitInvokeMethod(
                this.tracerVar, instructionHandle, this.classNameVar, this.methodIdVar,
                isInternalClass, lineInstructionInfo.getLineNumber(), this.nextTemVarName(),
                methodGen);
            this.insertInstructionHandle(methodGen.getInstructionList(), newInstructions,
                instructionHandle);
            newInstructions.dispose();
//
            if (isInternalClass) {
                InstructionList afterInvokeInstructions = this.factory.createInvokeAfterInvokeMethod(
                    this.tracerVar, instructionHandle, this.classNameVar, this.methodIdVar,
                    lineInstructionInfo.getLineNumber());
                this.appendInstruction(methodGen.getInstructionList(), afterInvokeInstructions,
                    instructionHandle);
                afterInvokeInstructions.dispose();
            }
        }
    }

    protected void injectCodeTracerReadWriteInstruction(final InstructionList instructionList,
        final ReadWriteInstructionInfo readWriteInstructionInfo, final MethodGen methodGen) {
        InstructionList newInstructions = null;
        if (readWriteInstructionInfo instanceof FieldInstructionInfo fieldInstructionInfo) {
            newInstructions = this.getReadWriteFieldInstructions(fieldInstructionInfo);
        } else if (readWriteInstructionInfo instanceof ArrayInstructionInfo arrayInstructionInfo) {
            LocalVariableGen arrayElementTempVar = this.addTempVar(methodGen,
                arrayInstructionInfo.getElementType(), arrayInstructionInfo.getInstructionHandle());
            if (arrayInstructionInfo.isStore()) {
                newInstructions = this.factory.createInvokeWriteArrayElementVarMethod(tracerVar,
                    arrayInstructionInfo, classNameVar, methodIdVar, arrayElementTempVar);
            } else {
                newInstructions = this.factory.createInvokeReadArrayElementVarMethod(tracerVar,
                    arrayInstructionInfo, classNameVar, methodIdVar, arrayElementTempVar);
            }
        } else if (readWriteInstructionInfo instanceof LocalVariableInstructionInfo localVarInstructionInfo) {
            if (localVarInstructionInfo.getInstruction() instanceof IINC) {
                newInstructions = this.factory.createInvokeIINCLocalVarMethod(tracerVar,
                    localVarInstructionInfo, classNameVar, methodIdVar,
                    methodGen.isStatic());
            } else {
                newInstructions = this.factory.createInvokeReadWriteLocalVarMethod(tracerVar,
                    localVarInstructionInfo, classNameVar, methodIdVar);
            }
        }

        if (newInstructions != null && newInstructions.getLength() > 0) {
            InstructionHandle instructionHandle = readWriteInstructionInfo.getInstructionHandle();
            this.insertInstructionHandle(instructionList, newInstructions, instructionHandle);
            if (!readWriteInstructionInfo.isStore()) {
                this.updateTarget(instructionHandle, instructionHandle.getPrev(),
                    instructionHandle.getNext());
                try {
                    instructionList.delete(instructionHandle);
                } catch (TargetLostException e) {
                    Log.error("Failed to delete instruction: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
            newInstructions.dispose();
        }
    }

    protected void injectCodeTracerReturn(final InstructionList instructionList,
        final LineInstructionInfo lineInstructionInfo) {
        for (InstructionHandle instructionHandle : lineInstructionInfo.getReturnInstructions()) {
            InstructionList newInstructions;
            ReturnInstruction instruction = (ReturnInstruction) instructionHandle.getInstruction();
            if (instruction instanceof RETURN) {
                newInstructions = this.factory.createInvokeHitVoidReturnMethod(this.tracerVar,
                    this.classNameVar, this.methodIdVar,
                    lineInstructionInfo.getLineNumber());
            } else {
                newInstructions = this.factory.createInvokeHitReturnMethod(this.tracerVar,
                    this.classNameVar, this.methodIdVar, instructionHandle,
                    lineInstructionInfo.getLineNumber());
            }
            this.insertInstructionHandle(instructionList, newInstructions, instructionHandle);
            newInstructions.dispose();
        }
    }

    protected void injectCodeTracerStart(final InstructionList instructionList) {

    }

    /**
     * Insert a new instruction list before the insertLocation
     *
     * @param instructionList The instruction list that to be inserted
     * @param newList         New instruction list
     * @param insertLocation  New instruction list will be added before this location
     */
    protected void insertInstructionHandle(InstructionList instructionList,
        InstructionList newList, final InstructionHandle insertLocation) {
        this.updateTarget(insertLocation, newList.getStart(), insertLocation);
        instructionList.insert(insertLocation, newList);
    }

    protected MethodGen instrumentMethod(final ClassGen classGen, final Method method,
        final boolean isInternalClass, final boolean isEntryMethod, final boolean isThreadMethod) {
        final String classBinaryName = NamingUtils.canonicalToClassBinaryName(
            classGen.getClassName());
        final ConstantPoolGen constantPoolGen = classGen.getConstantPool();
        final MethodGen methodGen = new MethodGen(method, classBinaryName, constantPoolGen);
        // Fill up the missing variables in localVariableTable
        LocalVariableSupporter.fillUpVariableTable(methodGen, method, constantPoolGen);

        /*
        Create the following variable and insert them into this method
        1. $className:          String          Name of class that this method belong to
        2. $methodSignature:    String          Signature of this method
        3. $tracer:             ExecutionTracer ExecutionTracer instance
         */
        this.classNameVar = methodGen.addLocalVariable(
            AbstractInstrumentator.CLASS_NAME_VAR_NAME,
            Type.STRING, null, null);
        this.methodIdVar = methodGen.addLocalVariable(
            AbstractInstrumentator.METHOD_ID_VAR_NAME, Type.STRING, null, null);
        this.tracerVar = methodGen.addLocalVariable(TraceInstrumentator.TRACER_VAR_NAME,
            Type.getType(Tracer.class), null, null);
        this.tempVarIdx = 0;

        InstructionList instructionList = methodGen.getInstructionList();
        InstructionHandle startInstruction = instructionList.getStart();
        if (startInstruction == null) {
            // Empty method
            return null;
        }

        // Analysis the instruction in every line
        List<LineInstructionInfo> lineInstructionInfoList = LineInstructionInfo.buildLineInstructions(
            classGen, methodGen, method, isInternalClass);

        // Filter out the line instructions that do not need to be instrumented
        JavaTracerFilter.getInstance()
            .filterInstructions(lineInstructionInfoList, classGen.getClassName(), method);

        // Instrument the instruction by line
        for (LineInstructionInfo lineInstructionInfo : lineInstructionInfoList) {
            this.injectCodeTracerHitLine(instructionList, lineInstructionInfo);

            for (ReadWriteInstructionInfo readWriteInstructionInfo : lineInstructionInfo.getReadWriteInstructionHandles()) {
                this.injectCodeTracerReadWriteInstruction(instructionList,
                    readWriteInstructionInfo, methodGen);
            }

            this.injectCodeTracerInvokeMethod(lineInstructionInfo, methodGen,
                isInternalClass);
            this.injectCodeTracerReturn(instructionList, lineInstructionInfo);
            this.injectCodeTracerExit(instructionList, lineInstructionInfo, isEntryMethod,
                isThreadMethod);
            lineInstructionInfo.dispose();
        }

        this.injectCodeInitTracer(methodGen, classGen.getConstantPool(), lineInstructionInfoList,
            isInternalClass, isEntryMethod);

        return methodGen;
    }

    protected boolean isByteCodeExceedLimit(final MethodGen methodGen) {
        try {
            return methodGen.getInstructionList().getByteCode().length >= 65534;
        } catch (Exception e) {
            return true;
        }
    }

    protected boolean isBytecodeExceedLimit(MethodGen methodGen) {
        final long limit = JavaTracerConfig.getInstance().getMaxMethodInstructionOffset();
        return methodGen.getInstructionList().getByteCode().length >= limit;
    }

    /**
     * Update instruction targeter. <br/>
     * <p>
     * When inserting an instruction, we need to update the targeter to ensure that all branch
     * instructions like {@code goto}, {@code if}, {@code jsr}, etc., that target the inserted
     * instruction are updated correctly. <br/>
     * <p>
     * It is important because when an instruction is inserted, the byte offsets of all subsequent
     * instructions are changed. Therefore, if the targeters is not updated, the branch instructions
     * could jump to the wrong location.
     *
     * @param oldPosition Insertion Position
     * @param newStart    New first instruction
     * @param newEnd      New last instruction
     */
    protected void updateTarget(InstructionHandle oldPosition, InstructionHandle newStart,
        InstructionHandle newEnd) {
        InstructionTargeter[] itList = oldPosition.getTargeters();
        if (itList != null) {
            for (InstructionTargeter it : itList) {
                if (it instanceof CodeExceptionGen exception) {
                    if (exception.getStartPC() == oldPosition) {
                        exception.setStartPC(newStart);
                    }
                    if (exception.getEndPC() == oldPosition) {
                        exception.setEndPC(newEnd);
                    }
                    if (exception.getHandlerPC() == oldPosition) {
                        exception.setHandlerPC(newStart);
                    }
                } else if (it instanceof LocalVariableGen localVarGen) {
                    boolean targeted = false;
                    if (localVarGen.getStart() == oldPosition) {
                        targeted = true;
                        localVarGen.setStart(newStart);
                    }
                    if (localVarGen.getEnd() == oldPosition) {
                        targeted = true;
                        localVarGen.setEnd(newEnd);
                    }
                    if (!targeted) {
                        throw new ClassGenException(
                            "Not targeting " + oldPosition + ", but {" + localVarGen.getStart()
                                + ", " + localVarGen.getEnd() + "}");
                    }
                } else {
                    it.updateTarget(oldPosition, newStart);
                }
            }
        }
    }

    private String nextTemVarName() {
        return TEMP_VAR_NAME + (++this.tempVarIdx);
    }
}
