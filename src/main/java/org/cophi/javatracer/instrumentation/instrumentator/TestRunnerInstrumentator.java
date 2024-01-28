package org.cophi.javatracer.instrumentation.instrumentator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.cophi.javatracer.instrumentation.agents.DefaultAgent;
import org.cophi.javatracer.testcase.runner.AbstractTestCaseRunner.Methods;
import org.cophi.javatracer.utils.NamingUtils;

public class TestRunnerInstrumentator implements JavaTracerInstrumentator {

    protected JavaTracerInstructionFactory factory = null;

    @Override
    public byte[] instrument(String className, byte[] classfileBuffer) throws IOException {
        ClassParser cp = new ClassParser(new ByteArrayInputStream(classfileBuffer), className);
        JavaClass javaClass = cp.parse();
        ClassGen classGen = new ClassGen(javaClass);
        ConstantPoolGen constantPoolGen = classGen.getConstantPool();

        for (Method method : javaClass.getMethods()) {
            final MethodGen methodGen = new MethodGen(method,
                NamingUtils.canonicalToClassBinaryName(className), constantPoolGen);
            if (Methods.EXIT_PROGRAM.getMethodName().equals(method.getName())) {
                this.injectMethodCall(methodGen, classGen, DefaultAgent.Methods.EXIT_PROGRAM);
            } else if (Methods.TEST_FINISHED.getMethodName().equals(method.getName())) {
                this.injectMethodCall(methodGen, classGen, DefaultAgent.Methods.FINISH_TEST);
            } else if (Methods.TEST_STARTED.getMethodName().equals(method.getName())) {
                this.injectMethodCall(methodGen, classGen, DefaultAgent.Methods.START_TEST);
            }
            classGen.replaceMethod(method, methodGen.getMethod());
        }
        JavaClass newJavaClass = classGen.getJavaClass();
        newJavaClass.setConstantPool(constantPoolGen.getFinalConstantPool());
        return newJavaClass.getBytes();
    }

    protected void injectMethodCall(final MethodGen methodGen, final ClassGen classGen,
        final DefaultAgent.Methods invokeMethod) {

        // Create method reference
        final int methodIdx = classGen.getConstantPool().addMethodref(
            NamingUtils.canonicalToClassBinaryName(invokeMethod.getDeclareClassBinaryName()),
            invokeMethod.getMethodName(), invokeMethod.getDescriptor());
        InstructionList newInstructions = new InstructionList();
        LocalVariableTable localVariableTable = methodGen.getLocalVariableTable(classGen
            .getConstantPool());
        for (int paramIdx = 0; paramIdx < invokeMethod.getArgumentNumber(); paramIdx++) {
            LocalVariable localVar = localVariableTable.getLocalVariable(paramIdx + 1, 0);
            Type varType = methodGen.getArgumentType(paramIdx);
            newInstructions.append(InstructionFactory.createLoad(varType, localVar.getIndex()));
        }
        newInstructions.append(new INVOKESTATIC(methodIdx));

        InstructionList instructionList = methodGen.getInstructionList();
        InstructionHandle startInstruction = instructionList.getStart();
        InstructionHandle pos = instructionList.insert(startInstruction, newInstructions);
        updateTargeters(startInstruction, pos);
        instructionList.setPositions();
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
    }

    private void updateTargeters(InstructionHandle oldPos, InstructionHandle newPos) {
        InstructionTargeter[] itList = oldPos.getTargeters();
        if (itList != null) {
            for (InstructionTargeter it : itList) {
                if (!(it instanceof CodeExceptionGen)) {
                    it.updateTarget(oldPos, newPos);
                }
            }
        }
    }
}
