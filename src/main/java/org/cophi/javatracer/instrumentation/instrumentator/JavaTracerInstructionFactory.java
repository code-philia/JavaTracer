package org.cophi.javatracer.instrumentation.instrumentator;

import java.util.List;
import org.apache.bcel.generic.AALOAD;
import org.apache.bcel.generic.AASTORE;
import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.DUP2;
import org.apache.bcel.generic.DUP2_X1;
import org.apache.bcel.generic.DUP2_X2;
import org.apache.bcel.generic.DUP_X1;
import org.apache.bcel.generic.DUP_X2;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.POP2;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.SWAP;
import org.apache.bcel.generic.Type;
import org.cophi.javatracer.instrumentation.agents.DefaultAgent;
import org.cophi.javatracer.instrumentation.instrumentator.instructionInfo.ArrayInstructionInfo;
import org.cophi.javatracer.instrumentation.instrumentator.instructionInfo.FieldInstructionInfo;
import org.cophi.javatracer.instrumentation.instrumentator.instructionInfo.LocalVariableInstructionInfo;
import org.cophi.javatracer.instrumentation.tracer.ExecutionTracer;
import org.cophi.javatracer.instrumentation.tracer.Tracer.Methods;
import org.cophi.javatracer.utils.BasicTypeUtils;
import org.cophi.javatracer.utils.JavaTracerUtils;
import org.cophi.javatracer.utils.NamingUtils;

public class JavaTracerInstructionFactory extends InstructionFactory {

    public JavaTracerInstructionFactory(ClassGen cg, ConstantPoolGen cp) {
        super(cg, cp);
    }

    public JavaTracerInstructionFactory(ConstantPoolGen cp) {
        super(cp);
    }

    public JavaTracerInstructionFactory(ClassGen cg) {
        super(cg);
    }

    public InstructionList createAssignStringToVar(final String value,
        final LocalVariableGen localVariableGen) {
        final ConstantPoolGen constantPoolGen = this.getConstantPool();
        InstructionList instructionList = new InstructionList();
        instructionList.append(new PUSH(constantPoolGen, value));
        instructionList.append(new ASTORE(localVariableGen.getIndex()));
        return instructionList;
    }

    public InstructionList createInvokeAfterInvokeMethod(final LocalVariableGen tracerVar,
        final InstructionHandle insnHandler, final LocalVariableGen classNameVar,
        final LocalVariableGen methodSigVar, final int lineNumber) {
        InvokeInstruction insn = (InvokeInstruction) insnHandler.getInstruction();
        Type returnType = insn.getReturnType(this.getConstantPool());
        InstructionList newInsns = new InstructionList();
        /* on stack: [objectRef]/[], returnValue */
        boolean revisit = !Type.VOID.equals(returnType);
//					&& ((insnHandler.getNext() == null)
//					|| !(insnHandler.getNext().getInstruction() instanceof POP));

        final boolean isInvokeStatic = insn instanceof INVOKESTATIC;
        if (isInvokeStatic) {
            if (Type.VOID.equals(returnType)) {
                /* on stack: [empty] */
                newInsns.append(new ALOAD(tracerVar.getIndex())); // $tracer
                newInsns.append(new ACONST_NULL()); // $tracer, returnValue
            } else if (returnType.getSize() == 1) {
                /* on stack: returnValue */
                newInsns.append(new DUP()); // returnValue, returnValue
                newInsns.append(
                    new ALOAD(tracerVar.getIndex())); // returnValue, returnValue, $tracer
                newInsns.append(new SWAP()); // returnValue, $tracer, returnValue
            } else { // 2
                /* on stack: returnValue* */
                newInsns.append(new DUP2()); // returnValue*, returnValue*
                newInsns.append(
                    new ALOAD(tracerVar.getIndex())); // returnValue*, returnValue*, $tracer
                newInsns.append(new DUP_X2()); // returnValue*, $tracer, returnValue*, $tracer
                newInsns.append(new POP()); // returnValue*, $tracer, returnValue*
            }
            BasicTypeUtils.appendObjectConvertInstruction(returnType, newInsns,
                this.getConstantPool());
            newInsns.append(
                new ACONST_NULL()); // (returnValue(*)), $tracer, returnValue*, objectRef
            /* no redundant obj on stack */
        } else {
            if (Type.VOID.equals(returnType)) {
                // objectRef
                newInsns.append(new ALOAD(tracerVar.getIndex())); // objectRef, $tracer
                newInsns.append(new SWAP()); // $tracer, objectRef
                newInsns.append(new ACONST_NULL()); // $tracer, objectRef, returnValue_null
                newInsns.append(new SWAP()); // $tracer, returnValue_null, objectRef
                // do nothing
            } else if (returnType.getSize() == 1) {
                // objectRef, returnValue
                newInsns.append(new DUP()); // objectRef, returnValue, returnValue
                newInsns.append(new DUP_X2()); // returnValue, objectRef, returnValue, returnValue
                newInsns.append(new POP()); // returnValue, objectRef, returnValue
                newInsns.append(new ALOAD(
                    tracerVar.getIndex())); // [returnValue], objectRef, returnValue, $trace
                newInsns.append(
                    new DUP_X2()); // [returnValue], $trace, objectRef, returnValue, $trace
                newInsns.append(new POP()); // [returnValue], $trace, objectRef, returnValue
                BasicTypeUtils.appendObjectConvertInstruction(returnType, newInsns,
                    this.getConstantPool());
                newInsns.append(new SWAP()); // [returnValue], $trace, returnValue, objectRef
                // returnValue, objectRef
            } else { // 2
                /* on stack: objectRef, returnValue* */
                newInsns.append(new DUP2_X1()); // returnValue*, objectRef, returnValue*
                BasicTypeUtils.appendObjectConvertInstruction(returnType, newInsns,
                    this.getConstantPool()); // returnValue*, objectRef, returnValue
                newInsns.append(new ALOAD(
                    tracerVar.getIndex())); // [returnValue*], objectRef, returnValue, $trace
                newInsns.append(
                    new DUP_X2()); // [returnValue*], $trace, objectRef, returnValue, $trace
                newInsns.append(new POP()); // [returnValue*], $trace, objectRef, returnValue
                newInsns.append(new SWAP()); // [returnValue*], $trace, returnValue, objectRef
            }
            // $tracer, returnValue, objectRef
        }

        final String className = insn.getClassName(this.getConstantPool());
        final String methodName = insn.getMethodName(this.getConstantPool());
        // In bcel library, the getSignature here return the method descriptor.
        final String methodDescriptor = insn.getSignature(this.getConstantPool());
        final String methodId = NamingUtils.genMethodId(className, methodName, methodDescriptor);
        newInsns.append(new PUSH(this.getConstantPool(), methodId));
        newInsns.append(new PUSH(this.getConstantPool(), lineNumber));
        newInsns.append(new ALOAD(classNameVar.getIndex()));
        newInsns.append(new ALOAD(methodSigVar.getIndex()));
        newInsns.append(new PUSH(this.getConstantPool(), revisit));
        newInsns.append(this.createInvokeTracerMethod(Methods.AFTER_INVOKE));
        return newInsns;
    }

    public InstructionList createInvokeCreateTracerMethod_ExecutionTracer(
        final boolean isInternalClass, final LocalVariableGen classNameVar,
        final LocalVariableGen methodSignatureVar,
        final int methodStartLine, final int methodEndLine, final String paramsNamesCode,
        final String paramsTypeSignCode, final LocalVariableGen parameters) {
        final InstructionList instructionList = new InstructionList();
        final ConstantPoolGen constantPoolGen = this.getConstantPool();
        instructionList.append(new PUSH(constantPoolGen, isInternalClass));
        instructionList.append(new ALOAD(classNameVar.getIndex()));
        instructionList.append(new ALOAD(methodSignatureVar.getIndex()));
        instructionList.append(new PUSH(constantPoolGen, methodStartLine));
        instructionList.append(new PUSH(constantPoolGen, methodEndLine));
        instructionList.append(new PUSH(constantPoolGen, paramsNamesCode));
        instructionList.append(new PUSH(constantPoolGen, paramsTypeSignCode));
        instructionList.append(new ALOAD(parameters.getIndex()));

        final int idx = constantPoolGen.addMethodref(
            ExecutionTracer.Methods.GET_TRACER.getDeclareClassBinaryName(),
            ExecutionTracer.Methods.GET_TRACER.getMethodName(),
            ExecutionTracer.Methods.GET_TRACER.getDescriptor());
        instructionList.append(new INVOKESTATIC(idx));
        return instructionList;
    }

    public InstructionList createInvokeExitProgramMethod(final LocalVariableGen tracerVar,
        final LocalVariableGen classNameVar, final LocalVariableGen methodSigVar) {
        InstructionList newInsns = new InstructionList();
        int index = this.getConstantPool()
            .addInterfaceMethodref(DefaultAgent.Methods.EXIT_PROGRAM.getDeclareClassBinaryName(),
                DefaultAgent.Methods.EXIT_PROGRAM.getMethodName(),
                DefaultAgent.Methods.EXIT_PROGRAM.getDescriptor());
        newInsns.append(new ALOAD(classNameVar.getIndex()));
        newInsns.append(new ALOAD(methodSigVar.getIndex()));
        newInsns.append(new INVOKESTATIC(index));
        return newInsns;
    }

    public InstructionList createInvokeHitExceptionTarget(final LocalVariableGen tracerVar,
        final int lineNumber, final LocalVariableGen classNameVar,
        final LocalVariableGen methodSignatureVar) {
        final InstructionList instructionList = new InstructionList();
        final ConstantPoolGen constantPoolGen = this.getConstantPool();

        // Load tracer variable instance to call the method
        instructionList.append(new ALOAD(tracerVar.getIndex()));

        // Push argument accordingly, the order matter
        // Check org.cophi.javatracer.instrumentation.tracer.Tracer for method arguments
        instructionList.append(new PUSH(constantPoolGen, lineNumber));
        instructionList.append(new ALOAD(classNameVar.getIndex()));
        instructionList.append(new ALOAD(methodSignatureVar.getIndex()));

        // Invoke the target method
        instructionList.append(this.createInvokeTracerMethod(Methods.HIT_EXCEPTION_TARGET));

        return instructionList;
    }

    public InstructionList createInvokeHitInvokeMethod(final LocalVariableGen tracerVar,
        final InstructionHandle insnHandler, final LocalVariableGen classNameVar,
        final LocalVariableGen methodSigVar, final boolean isInternalClass, final int lineNumber,
        final String varName, final MethodGen methodGen) {
        final InvokeInstruction insn = (InvokeInstruction) insnHandler.getInstruction();
        final String className = insn.getClassName(this.getConstantPool());
        InstructionList newInsns = new InstructionList();
        Methods tracerMethod = Methods.HIT_INVOKE;
        boolean isInvokeStatic = insn instanceof INVOKESTATIC;
        if (isInvokeStatic) {
            tracerMethod = Methods.HIT_INVOKE_STATIC;
        }
        /* on stack: (objectRef)+, arg1(*), arg2(*), ... */
        Type returnType = insn.getReturnType(this.getConstantPool());//getReferenceType(constPool);
        Type[] argTypes = insn.getArgumentTypes(this.getConstantPool());
        /*
         * argObjsVar to keep args. Object[] temp = Object[] {arg1(*), arg2(*), ...}
         */
        LocalVariableGen argObjsVar = methodGen.addLocalVariable(varName,
            new ArrayType(Type.OBJECT, 1), insnHandler, insnHandler.getNext());
        newInsns.append(new PUSH(this.getConstantPool(), argTypes.length));
        newInsns.append(new ANEWARRAY(this.getConstantPool().addClass(Object.class.getName())));
        argObjsVar.setStart(newInsns.append(new ASTORE(argObjsVar.getIndex())));
        /* store args */
        for (int i = (argTypes.length - 1); i >= 0; i--) {
            newInsns.append(new ALOAD(argObjsVar.getIndex()));
            // [objectRef, arg1, arg2, ...] argn, tempVar

            Type argType = argTypes[i];
            /* swap */
            if (argType.getSize() == 1) {
                newInsns.append(new SWAP()); // tempVar, argn
                newInsns.append(new PUSH(this.getConstantPool(), i)); // tempVar, argn, idx
                newInsns.append(new SWAP()); // tempVar, idx, argn
            } else {
                // argn*, tempVar
                /* swap */
                newInsns.append(new DUP_X2()); // tempVar, argn*, tempVar
                newInsns.append(new POP()); // tempVar, argn*
                newInsns.append(new PUSH(this.getConstantPool(), i)); // tempVar, argn*, idx
                newInsns.append(new DUP_X2()); // tempVar, idx, argn*, idx
                newInsns.append(new POP()); // tempVar, idx, argn*
            }
            if (argType instanceof BasicType) {
                newInsns.append(
                    new INVOKESTATIC(
                        BasicTypeUtils.getValueOfMethodIdx((BasicType) argType,
                            this.getConstantPool())));
            }
            newInsns.append(new AASTORE());
        }
        if (!isInvokeStatic) {
            /* duplicate objectRef */
            newInsns.append(new DUP()); // objectRef, objectRef
            newInsns.append(new ALOAD(tracerVar.getIndex()));
            newInsns.append(new SWAP()); // objectRef, tracer, objectRef
        } else {
            /* empty stack */
            newInsns.append(new ALOAD(tracerVar.getIndex())); // tracer
        }
        newInsns.append(new PUSH(this.getConstantPool(), className));
        // ([objectRef], objectRef),

        String sig = insn.getSignature(this.getConstantPool());
        String methodName = insn.getMethodName(this.getConstantPool());
        String mSig = className + "#" + methodName + sig;
        int stringIndex = this.getConstantPool().lookupString(mSig);
        if (stringIndex == -1) {
            stringIndex = this.getConstantPool().addString(mSig);
        }
        newInsns.append(new PUSH(this.getConstantPool(), mSig));
        // ([objectRef], objectRef), invokeType, methodName

        newInsns.append(new ALOAD(argObjsVar.getIndex()));
        // ([objectRef], objectRef), invokeType, methodName, params

        newInsns.append(
            new PUSH(this.getConstantPool(), JavaTracerUtils.encodeArgumentTypes(argTypes)));
        // (objectRef), invokeType, methodName, params, paramTypesCode

        newInsns.append(new PUSH(this.getConstantPool(), returnType.getSignature()));
        // (objectRef), invokeType, methodName, params, paramTypesCode, returnTypeSign

        newInsns.append(new PUSH(this.getConstantPool(), lineNumber));
        newInsns.append(new ALOAD(classNameVar.getIndex()));
        newInsns.append(new ALOAD(methodSigVar.getIndex()));
        newInsns.append(this.createInvokeTracerMethod(tracerMethod));

        /* on stack: (objectRef) */
        // duplicate objectRef to use in afterInvoke
        if (isInternalClass && !isInvokeStatic) {
            newInsns.append(new DUP()); // objectRef, objectRef
        }

        /* restore arg values */
        for (int i = 0; i < argTypes.length; i++) {
            Type argType = argTypes[i];
            newInsns.append(new ALOAD(argObjsVar.getIndex())); // load argObjs[]
            newInsns.append(new PUSH(this.getConstantPool(), i)); // arg_idx
            newInsns.append(new AALOAD()); // -> argObjs[arg_idx]
            if (argType instanceof BasicType) {
                newInsns.append(this.createCheckCast(
                    BasicTypeUtils.getCorrespondingPrimitiveType((BasicType) argType)));
                newInsns.append(new INVOKEVIRTUAL(
                    BasicTypeUtils.getToPrimitiveValueMethodIdx((BasicType) argType,
                        this.getConstantPool())));
            }
        }

        return newInsns;
    }

    public InstructionList createInvokeHitLineMethod(final LocalVariableGen tracerVar,
        final int lineNumber, final LocalVariableGen classNameVar,
        final LocalVariableGen methodSignatureVar, final int readVarCount,
        final int writtenVarCount,
        final List<InstructionHandle> byteCodeList) {
        InstructionList instructionList = new InstructionList();
        final ConstantPoolGen constantPoolGen = this.getConstantPool();

        // Load variable instance to call the method
        instructionList.append(new ALOAD(tracerVar.getIndex()));

        // Push argument accordingly, the order matter
        // Check org.cophi.javatracer.instrumentation.tracer.Tracer for method arguments
        instructionList.append(new PUSH(constantPoolGen, lineNumber));
        instructionList.append(new ALOAD(classNameVar.getIndex()));
        instructionList.append(new ALOAD(methodSignatureVar.getIndex()));
        instructionList.append(new PUSH(constantPoolGen, readVarCount));
        instructionList.append(new PUSH(constantPoolGen, writtenVarCount));
        StringBuilder sb = new StringBuilder();
        for (InstructionHandle instructionHandle : byteCodeList) {
            sb.append(instructionHandle.getInstruction().toString()).append(":");
        }
        instructionList.append(new PUSH(constantPoolGen, sb.toString()));

        // Invoke the target method
        instructionList.append(this.createInvokeTracerMethod(Methods.HIT_LINE));

        return instructionList;
    }

    public InstructionList createInvokeHitMethodEndMethod(final LocalVariableGen tracerVar,
        final LocalVariableGen classNameVar, final LocalVariableGen methodSignatureVar,
        final int lineNumber) {
        InstructionList newInsns = new InstructionList();
        newInsns.append(new ALOAD(tracerVar.getIndex()));
        newInsns.append(new PUSH(this.getConstantPool(), lineNumber));
        newInsns.append(new ALOAD(classNameVar.getIndex()));
        newInsns.append(new ALOAD(methodSignatureVar.getIndex()));
        newInsns.append(this.createInvokeTracerMethod(Methods.HIT_METHOD_END));
        return newInsns;
    }

    public InstructionList createInvokeHitReturnMethod(final LocalVariableGen tracerVar,
        final LocalVariableGen classNameVar, final LocalVariableGen methodSignatureVar,
        final InstructionHandle insnHandler, final int lineNumber) {
        InstructionList newInsns = new InstructionList();
        ReturnInstruction insn = (ReturnInstruction) insnHandler.getInstruction();
        Type type = insn.getType();
        if (insnHandler.getPrev().getInstruction() instanceof ACONST_NULL) {
            newInsns.append(new ALOAD(tracerVar.getIndex())); // val, tracer
            newInsns.append(new ACONST_NULL());
        } else {
            /* on stack: value */
            if (type.getSize() == 1) {
                newInsns.append(new DUP()); // val, val
                newInsns.append(new ALOAD(tracerVar.getIndex())); // val, val, tracer
                newInsns.append(new SWAP()); // val, tracer, val
            } else {
                newInsns.append(new DUP2()); // val*, val*
                newInsns.append(new ALOAD(tracerVar.getIndex())); // val*, val*, tracer
                newInsns.append(new DUP_X2()); // val*, tracer, val*, tracer
                newInsns.append(new POP()); // val*, tracer, val*
            }

            if (type instanceof BasicType) {
                newInsns.append(
                    new INVOKESTATIC(
                        BasicTypeUtils.getValueOfMethodIdx((BasicType) type,
                            this.getConstantPool())));
            }
        }
        newInsns.append(new PUSH(this.getConstantPool(), type.getSignature()));
        // val*, tracer, val*, returnGeneralType

        newInsns.append(new PUSH(this.getConstantPool(), lineNumber));
        // val*, tracer, val*, returnGeneralType, line

        newInsns.append(new ALOAD(classNameVar.getIndex()));
        newInsns.append(new ALOAD(methodSignatureVar.getIndex()));
        newInsns.append(this.createInvokeTracerMethod(Methods.HIT_RETURN));
        return newInsns;
    }

    public InstructionList createInvokeHitVoidReturnMethod(final LocalVariableGen tracerVar,
        final LocalVariableGen classNameVar, final LocalVariableGen methodSignatureVar,
        final int lineNumber) {
        InstructionList newInstructions = new InstructionList();
        newInstructions.append(new ALOAD(tracerVar.getIndex()));
        newInstructions.append(new PUSH(this.getConstantPool(), lineNumber));
        newInstructions.append(new ALOAD(classNameVar.getIndex()));
        newInstructions.append(new ALOAD(methodSignatureVar.getIndex()));
        newInstructions.append(this.createInvokeTracerMethod(Methods.HIT_VOID_RETURN));
        return newInstructions;
    }

    public InstructionList createInvokeIINCLocalVarMethod(final LocalVariableGen tracerVar,
        final LocalVariableInstructionInfo info, final LocalVariableGen classNameVar,
        final LocalVariableGen methodSignVar, final boolean isStatic) {
        IINC insn = (IINC) info.getInstruction();
        // ignore reference to self if it the method is non-static
        if (insn.getIndex() == 0 && !isStatic) {
            return null;
        }
        InstructionList newInsns = new InstructionList();
        Type type = insn.getType(this.getConstantPool());

        /* tracer */
        newInsns.append(new ALOAD(tracerVar.getIndex()));
        /* load current value */
        newInsns.append(InstructionFactory.createLoad(type, insn.getIndex())); // $tracer, value
        newInsns.append(new INVOKESTATIC(
            BasicTypeUtils.getValueOfMethodIdx((BasicType) type, this.getConstantPool())));

        /* iinc */
        newInsns.append(insn.copy()); // $tracer, value

        /* load valueAfter */
        newInsns.append(
            InstructionFactory.createLoad(type, insn.getIndex())); // $tracer, value, valueAfter
        newInsns.append(new INVOKESTATIC(
            BasicTypeUtils.getValueOfMethodIdx((BasicType) type, this.getConstantPool())));

        newInsns.append(new PUSH(this.getConstantPool(), info.getVarName()));
        // $tracer, value, valueAfter, varName

        newInsns.append(new PUSH(this.getConstantPool(), info.getVarType()));
        // $tracer, value, valueAfter, varName, varType

        newInsns.append(new PUSH(this.getConstantPool(), info.getLineNumber()));
        // $tracer, value, valueAfter, varName, line

        newInsns.append(new PUSH(this.getConstantPool(), insn.getIndex()));
        // $tracer, value, valueAfter, varName, bcLocalVarIdx

        newInsns.append(new PUSH(this.getConstantPool(), info.getVarScopeStartLine()));
        // $tracer, value, valueAfter, varName, bcLocalVarIdx, varScopeStartLine

        newInsns.append(new PUSH(this.getConstantPool(), info.getVarScopeEndLine()));
        // $tracer, value, valueAfter, varName, bcLocalVarIdx, varScopeStartLine, varScopeEndLine

        newInsns.append(new ALOAD(classNameVar.getIndex()));
        newInsns.append(new ALOAD(methodSignVar.getIndex()));
        newInsns.append(this.createInvokeTracerMethod(Methods.IINC_LOCAL_VAR));

        return newInsns;
    }

    public InstructionList createInvokeReadArrayElementVarMethod(final LocalVariableGen tracerVar,
        final ArrayInstructionInfo info, final LocalVariableGen classNameVar,
        final LocalVariableGen methodSigVar, final LocalVariableGen arrEleTempVar) {
        InstructionList newInstructions = new InstructionList();

        ArrayInstruction insn = info.getInstruction();

        newInstructions.append(new DUP2()); // [arrRef, idx], arrRef, idx
        newInstructions.append(insn.copy()); // arrRef, idx, val
        InstructionHandle tempVarStartPos = newInstructions
            .append(InstructionFactory.createStore(info.getElementType(),
                arrEleTempVar.getIndex())); // arrRef,
        // idx
        /* waiting list (empty): [] */

        arrEleTempVar.setStart(tempVarStartPos);
        /* working on active list: arrRef, idx */
        newInstructions.append(new ALOAD(tracerVar.getIndex())); // arrRef, idx, tracer
        newInstructions.append(new DUP_X2()); // tracer, arrRef, idx, tracer
        newInstructions.append(new POP()); // tracer, arrRef, idx
        newInstructions.append(
            InstructionFactory.createLoad(info.getElementType(), arrEleTempVar.getIndex()));
        // tracer, arrRef, idx, val

        if (info.getElementType() instanceof BasicType) {
            newInstructions.append(
                new INVOKESTATIC(
                    BasicTypeUtils.getValueOfMethodIdx((BasicType) info.getElementType(),
                        this.getConstantPool())));
        }
        newInstructions.append(new PUSH(this.getConstantPool(), info.getVarType()));
        // tracer, arrRef, idx, val, eleType

        newInstructions.append(new PUSH(this.getConstantPool(), info.getLineNumber()));
        // tracer, arrRef, idx, val, eleType, line

        newInstructions.append(new ALOAD(classNameVar.getIndex()));
        newInstructions.append(new ALOAD(methodSigVar.getIndex()));
        newInstructions.append(this.createInvokeTracerMethod(Methods.READ_ARRAY_ELEMENT_VAR));
        /* restore element value for use */
        newInstructions.append(
            InstructionFactory.createLoad(info.getElementType(), arrEleTempVar.getIndex())); // val
        /*
         * at this point : For Store Instruction case: arrRef, idx, val For Load
         * Instruction case: val
         */
        return newInstructions;
    }

    public InstructionList createInvokeReadFieldMethod(final LocalVariableGen tracerVar,
        final FieldInstructionInfo info, final LocalVariableGen classNameVar,
        final LocalVariableGen methodSigVar) {
        InstructionList newInstructions = new InstructionList();

        // stack: obj (refValue)
        GETFIELD insn = (GETFIELD) info.getInstruction();
        newInstructions.append(new DUP()); // obj, obj
        newInstructions.append(new GETFIELD(insn.getIndex())); // obj, val (*)
        if (info.isComputationalType1()) {
            newInstructions.append(new DUP_X1()); // [val], obj, val
            newInstructions.append(new ALOAD(tracerVar.getIndex()));
            // [val], obj, val, tracer

            newInstructions.append(new DUP_X2()); // [val], tracer, obj, val, tracer
            newInstructions.append(new POP()); // [val], tracer, obj, val
        } else {
            // obj, val*

            newInstructions.append(new DUP2_X1()); // [val*], obj, val*
            /* swap */
            newInstructions.append(new DUP2_X1()); // [val*], val*, obj, val*
            newInstructions.append(new POP2()); // [val*], val*, obj

            /* push tracer */
            newInstructions.append(new ALOAD(tracerVar.getIndex())); // [val*], val*, obj, tracer
            /* bring tracer to the bottom */
            newInstructions.append(new DUP2_X2()); // [val*], obj, tracer, val*, obj, tracer
            newInstructions.append(new POP()); // [val*], obj, tracer, val*, obj
            /* swap obj, var* */
            newInstructions.append(new DUP_X2()); // [val*], obj, tracer, obj, val*, obj
            newInstructions.append(new POP()); // [val*, obj], tracer, obj, val*
        }
        Type fieldType = info.getFieldBcType();
        if (fieldType instanceof BasicType) {
            newInstructions.append(new INVOKESTATIC(
                BasicTypeUtils.getValueOfMethodIdx((BasicType) fieldType, this.getConstantPool())));
        }
        newInstructions.append(new PUSH(this.getConstantPool(),
            info.getFieldName())); // [val*, obj], tracer, obj, val, fieldName
        newInstructions.append(new PUSH(this.getConstantPool(),
            info.getFieldType())); // [val*, obj], tracer, obj, val, fieldName, fieldTypeSignature
        newInstructions.append(new PUSH(this.getConstantPool(),
            info.getLineNumber())); // [val*, obj], tracer, obj, val, fieldName, fieldTypeSignature, line
        newInstructions.append(new ALOAD(classNameVar.getIndex()));
        newInstructions.append(new ALOAD(methodSigVar.getIndex()));
        newInstructions.append(this.createInvokeTracerMethod(Methods.READ_FIELD));
        // record -> [val] or [val*, obj]
        if (info.isComputationalType2()) {
            newInstructions.append(new POP());
        }
        return newInstructions;
    }

    public InstructionList createInvokeReadStaticFieldMethod(final LocalVariableGen tracerVar,
        final FieldInstructionInfo info, final LocalVariableGen classNameVar,
        final LocalVariableGen methodSigVar) {
        InstructionList newInstructions = new InstructionList();
        GETSTATIC insn = (GETSTATIC) info.getInstruction();
        newInstructions.append(insn); // val
        /* duplicate field value */
        if (info.isComputationalType1()) {
            newInstructions.append(new DUP()); // [val], val
            newInstructions.append(new ALOAD(tracerVar.getIndex())); // [val], val,
            // tracer
            newInstructions.append(new SWAP()); // [val], tracer, val
        } else {
            newInstructions.append(new DUP2()); // val*, val*
            newInstructions.append(new ALOAD(tracerVar.getIndex())); // [val*], val*, tracer
            /* swap */
            newInstructions.append(new DUP_X2()); // [val*], tracer, val*, tracer
            newInstructions.append(new POP()); // [val*], tracer, val*
        }
        Type fieldType = info.getFieldBcType();
        if (fieldType instanceof BasicType) {
            newInstructions.append(new INVOKESTATIC(
                BasicTypeUtils.getValueOfMethodIdx((BasicType) fieldType, this.getConstantPool())));
        }
        newInstructions.append(new PUSH(this.getConstantPool(), info.getReferenceType()));
        // tracer, val*, refType
        newInstructions.append(new PUSH(this.getConstantPool(), info.getFieldName()));
        // tracer, val*, refType, fieldName
        newInstructions.append(new PUSH(this.getConstantPool(), info.getFieldType()));
        // tracer, val*, refType, fieldName, fieldType
        newInstructions.append(new PUSH(this.getConstantPool(), info.getLineNumber()));
        // tracer, val*, refType, fieldName, fieldType, line
        newInstructions.append(new ALOAD(classNameVar.getIndex()));
        newInstructions.append(new ALOAD(methodSigVar.getIndex()));
        newInstructions.append(this.createInvokeTracerMethod(Methods.READ_STATIC_FIELD));
        return newInstructions;
    }

    public InstructionList createInvokeReadWriteLocalVarMethod(final LocalVariableGen tracerVar,
        final LocalVariableInstructionInfo insnInfo, final LocalVariableGen classNameVar,
        final LocalVariableGen methodSigVar) {
        LocalVariableInstruction insn = insnInfo.getInstruction();
        // ignore reference to self
//		if (insn.getIndex() == 0) {
//			return null;
//		}
        InstructionList newInsns = new InstructionList();
        Type type = insn.getType(this.getConstantPool());
        /*
         * for load instruction, we need to execute the load instruction first
         * to get the value of local variable, then onward, the logic would be
         * the same for both case, load & store
         */
        final boolean isStore = insnInfo.isStore();
        Methods tracerMethod = isStore ? Methods.WRITE_LOCAL_VAR : Methods.READ_LOCAL_VAR;
        if (!isStore) {
            newInsns.append(insn.copy()); // value
        }
        /* invoke tracer */
        if (isStore && insnInfo.getInstructionHandle().getPrev()
            .getInstruction() instanceof ACONST_NULL) {
            newInsns.append(new ALOAD(tracerVar.getIndex())); // value, $tracer
            newInsns.append(new ACONST_NULL());
        } else {
            if (insnInfo.isComputationalType1()) {
                newInsns.append(new DUP()); // [value], value
                newInsns.append(new ALOAD(tracerVar.getIndex())); // [value], value, $tracer
                newInsns.append(new SWAP()); //  [value], $tracer, value
            } else { // stack size = 2
                newInsns.append(new DUP2()); // [value*], value*
                newInsns.append(new ALOAD(tracerVar.getIndex())); // [value*], value*, $tracer
                newInsns.append(new DUP_X2()); // [value*], $tracer, value*, $tracer
                newInsns.append(new POP()); // [value*], $tracer, value*
            }
        }
        if (type instanceof BasicType) {
            newInsns.append(new INVOKESTATIC(
                BasicTypeUtils.getValueOfMethodIdx((BasicType) type, this.getConstantPool())));
        }

        newInsns.append(new PUSH(this.getConstantPool(), insnInfo.getVarName()));
        // [value(*)], $tracer, value, varName

        newInsns.append(new PUSH(this.getConstantPool(), insnInfo.getVarType()));
        // [value(*)], $tracer, value, varName, varType

        newInsns.append(new PUSH(this.getConstantPool(), insnInfo.getLineNumber()));
        // [value(*)], $tracer, value, varName, line

        newInsns.append(new PUSH(this.getConstantPool(), insn.getIndex()));
        // [value(*)], $tracer, value, varName, bcLocalVarIdx

        newInsns.append(new PUSH(this.getConstantPool(), insnInfo.getVarScopeStartLine()));
        // [value(*)], $tracer, value, varName, bcLocalVarIdx, varScopeStartLine

        newInsns.append(new PUSH(this.getConstantPool(), insnInfo.getVarScopeEndLine()));
        // [value(*)], $tracer, value, varName, bcLocalVarIdx, varScopeStartLine, varScopeEndLine

        newInsns.append(new ALOAD(classNameVar.getIndex()));
        newInsns.append(new ALOAD(methodSigVar.getIndex()));
        newInsns.append(this.createInvokeTracerMethod(tracerMethod));
        return newInsns;
    }

    /**
     * Create the instruction that invoke the target tracer method
     *
     * @param method Target tracer method
     * @return Method Invoking Instruction
     * @see org.cophi.javatracer.instrumentation.tracer.Tracer
     */
    public Instruction createInvokeTracerMethod(final Methods method) {
        // Create a reference to target method in constant pool
        final ConstantPoolGen constantPoolGen = this.getConstantPool();
        final int idx = constantPoolGen.addInterfaceMethodref(method.getDeclareClassBinaryName(),
            method.getMethodName(), method.getDescriptor());

        // Invoke the method by the reference
        return new INVOKEINTERFACE(idx, method.getArgumentNumber());
    }

    public InstructionList createInvokeWriteArrayElementVarMethod(final LocalVariableGen tracerVar,
        final ArrayInstructionInfo info, final LocalVariableGen classNameVar,
        final LocalVariableGen methodSigVar, final LocalVariableGen arrEleTempVar) {
        InstructionList newInstructions = new InstructionList();

        InstructionHandle tempVarStartPos;
        tempVarStartPos = newInstructions
            .append(InstructionFactory.createStore(info.getElementType(),
                arrEleTempVar.getIndex())); // arrRef,
        // idx
        newInstructions.append(new DUP2()); // [arrRef, idx], arrRef, idx
        /* in waiting list: [arrRef, idx] */

        arrEleTempVar.setStart(tempVarStartPos);
        /* working on active list: arrRef, idx */
        newInstructions.append(new ALOAD(tracerVar.getIndex())); // arrRef, idx, tracer
        newInstructions.append(new DUP_X2()); // tracer, arrRef, idx, tracer
        newInstructions.append(new POP()); // tracer, arrRef, idx
        newInstructions.append(
            InstructionFactory.createLoad(info.getElementType(), arrEleTempVar.getIndex()));
        // tracer, arrRef, idx, val

        if (info.getElementType() instanceof BasicType) {
            newInstructions.append(
                new INVOKESTATIC(
                    BasicTypeUtils.getValueOfMethodIdx((BasicType) info.getElementType(),
                        this.getConstantPool())));
        }
        newInstructions.append(new PUSH(this.getConstantPool(), info.getVarType()));
        // tracer, arrRef, idx, val, eleType

        newInstructions.append(new PUSH(this.getConstantPool(), info.getLineNumber()));
        // tracer, arrRef, idx, val, eleType, line

        newInstructions.append(new ALOAD(classNameVar.getIndex()));
        newInstructions.append(new ALOAD(methodSigVar.getIndex()));
        newInstructions.append(this.createInvokeTracerMethod(Methods.WRITE_ARRAY_ELEMENT_VAR));
        /* restore element value for use */
        newInstructions.append(
            InstructionFactory.createLoad(info.getElementType(), arrEleTempVar.getIndex())); // val
        /*
         * at this point : For Store Instruction case: arrRef, idx, val For Load
         * Instruction case: val
         */
        return newInstructions;
    }

    public InstructionList createInvokeWriteFieldMethod(final LocalVariableGen tracerVar, final
    FieldInstructionInfo info, final LocalVariableGen classNameVar,
        final LocalVariableGen methodSigVar) {
        InstructionList newInstructions = new InstructionList();
        /*
         * on stack: obj, value
         */
        if (info.isNextToAconstNull()) {
            newInstructions.append(new POP()); // obj
            newInstructions.append(new DUP()); // obj, obj
            newInstructions.append(new ALOAD(tracerVar.getIndex())); // obj, obj, tracer
            newInstructions.append(new SWAP()); // obj, tracer, obj
            newInstructions.append(new ACONST_NULL()); // obj, tracer, obj, null
        } else if (info.isComputationalType1()) {
            newInstructions.append(new DUP2()); // Duplicates object and value:
            // [obj, val], obj, val

            newInstructions.append(new ALOAD(tracerVar.getIndex()));
            // [obj, val], obj, val, tracer

            newInstructions.append(new DUP_X2());
            // [obj, val], tracer, obj, val, tracer

            newInstructions.append(new POP());
            // [obj, val], tracer, obj, val

        } else {
            // obj, val*

            newInstructions.append(new DUP2_X1()); // val*, obj, val*
            newInstructions.append(new POP2());  // val*, obj
            newInstructions.append(new DUP_X2()); // obj, val*, obj
            newInstructions.append(new DUP_X2()); // obj, obj, val*, obj
            newInstructions.append(new POP()); // obj, obj, val*
            newInstructions.append(new DUP2_X1()); // obj, val*, obj, val*

            /* swap obj, var* */
            newInstructions.append(new DUP2_X1()); // [obj, val*], val*, obj, val*
            newInstructions.append(new POP2()); // [obj, val*], val*, obj

            /* push tracer */
            newInstructions.append(
                new ALOAD(tracerVar.getIndex())); // [obj, val*], val*, obj, tracer
            /* bring tracer to the bottom */
            newInstructions.append(new DUP2_X2()); // [obj, val*], obj, tracer, val*, obj, tracer
            newInstructions.append(new POP()); // [obj, val*], obj, tracer, val*, obj

            /* swap obj, var* */
            newInstructions.append(new DUP_X2()); // [obj, val*], obj, tracer, obj, val*, obj
            newInstructions.append(new POP()); // [obj, val*], obj, tracer, obj, val*
        }
        Type fieldType = info.getFieldBcType();
        if (fieldType instanceof BasicType) {
            newInstructions.append(new INVOKESTATIC(
                BasicTypeUtils.getValueOfMethodIdx((BasicType) fieldType,
                    this.getConstantPool())));
        }
        newInstructions.append(new PUSH(this.getConstantPool(), info.getFieldName()));
        // [obj || (obj, val) || (obj, val*, obj)], tracer, obj, val, fieldName

        newInstructions.append(new PUSH(this.getConstantPool(), info.getFieldType()));
        // [obj || (obj, val) || (obj, val*, obj)],tracer, obj, val, fieldName, fieldTypeSignature

        newInstructions.append(new PUSH(this.getConstantPool(), info.getLineNumber()));
        // [obj || (obj, val) || (obj, val*, obj)], tracer, obj, val, fieldName, fieldTypeSignature, line

        newInstructions.append(new ALOAD(classNameVar.getIndex()));
        newInstructions.append(new ALOAD(methodSigVar.getIndex()));

        // Invoke the method
        newInstructions.append(this.createInvokeTracerMethod(Methods.WRITE_FIELD));
        // record -> [obj || (obj, val) || (obj, val*, obj)]

        if (info.isNextToAconstNull()) {
            newInstructions.append(new ACONST_NULL());
        } else if (info.isComputationalType2()) {
            newInstructions.append(new POP());
        }
        return newInstructions;
    }

    public InstructionList createInvokeWriteStaticFieldMethod(final LocalVariableGen tracerVar,
        final FieldInstructionInfo info, final LocalVariableGen classNameVar,
        final LocalVariableGen methodSigVar) {
        InstructionList newInstructions = new InstructionList();
        if (info.isNextToAconstNull()) {
            newInstructions.append(new ALOAD(tracerVar.getIndex())); // tracer
            newInstructions.append(new ACONST_NULL()); // tracer, val
        } else if (info.getFieldStackSize() == 1) {
            newInstructions.append(new DUP()); // val
            newInstructions.append(new ALOAD(tracerVar.getIndex())); // val, tracer
            newInstructions.append(new SWAP()); // tracer, val
        } else {
            newInstructions.append(new DUP2()); // val*
            newInstructions.append(new ALOAD(tracerVar.getIndex())); // val*, tracer
            newInstructions.append(new DUP_X2()); // tracer, val*, tracer
            newInstructions.append(new POP()); // tracer, val*
        }
        Type fieldType = info.getFieldBcType();
        if (fieldType instanceof BasicType) {
            newInstructions.append(new INVOKESTATIC(
                BasicTypeUtils.getValueOfMethodIdx((BasicType) fieldType, this.getConstantPool())));
        }
        newInstructions.append(new PUSH(this.getConstantPool(), info.getReferenceType()));
        // tracer, val*, refType
        newInstructions.append(new PUSH(this.getConstantPool(), info.getFieldName()));
        // tracer, val*, refType, fieldName
        newInstructions.append(new PUSH(this.getConstantPool(), info.getFieldType()));
        // tracer, val*, refType, fieldName, fieldType
        newInstructions.append(new PUSH(this.getConstantPool(), info.getLineNumber()));
        // tracer, val*, refType, fieldName, fieldType, line
        newInstructions.append(new ALOAD(classNameVar.getIndex()));
        newInstructions.append(new ALOAD(methodSigVar.getIndex()));
        newInstructions.append(this.createInvokeTracerMethod(Methods.WRITE_STATIC_FIELD));
        return newInstructions;
    }


}
