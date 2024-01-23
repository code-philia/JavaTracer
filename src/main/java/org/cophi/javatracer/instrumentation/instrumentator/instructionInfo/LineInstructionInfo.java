package org.cophi.javatracer.instrumentation.instrumentator.instructionInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReturnInstruction;
import org.cophi.javatracer.cfg.CFG;
import org.cophi.javatracer.cfg.CFGConstructor;
import org.cophi.javatracer.cfg.CFGNode;
import org.cophi.javatracer.instrumentation.agents.LoadedClassRecord;
import org.cophi.javatracer.utils.StringUtils;

/**
 * Store the instructions executed in one line of the code
 */
public class LineInstructionInfo {

    public static final int UNKNOWN_LINE_NUMBER = -1;
    /**
     * Line number of code
     */
    protected int lineNumber;
    /**
     * List of instruction that executed in target line
     */
    protected List<InstructionHandle> instructionHandles;
    /**
     * List of instruction that read or write a variable in target line
     */
    protected List<ReadWriteInstructionInfo> readWriteInstructionInfos;
    /**
     * List of instruction that invoke methods in target line
     */
    protected List<InstructionHandle> invokeInstructions;
    /**
     * List of instruction that return in target line
     */
    protected List<InstructionHandle> returnInstructions;
    /**
     * List of instruction that exit the method in target line
     */
    protected List<InstructionHandle> exitInstructions;
    /**
     * {@code True} if this line contain exception target
     */
    protected boolean hasExceptionTarget;

    public LineInstructionInfo() {
    }

    public LineInstructionInfo(final ClassGen classGen, final MethodGen methodGen,
        final Method method, final
    LineNumberGen lineNumberGen) {
        this.lineNumber = lineNumberGen.getSourceLine();

        // extractInstructions should be invoked before the other methods.
        this.instructionHandles = this.extractInstructions(lineNumber,
            methodGen.getInstructionList(),
            method.getLineNumberTable());
        final String locationId = this.genLocationId(classGen, method, lineNumberGen);
        final boolean isInternalClass = LoadedClassRecord.getInstance()
            .isInternalClass(classGen.getClassName());
        this.readWriteInstructionInfos = this.extractRWInstructions(locationId, isInternalClass,
            classGen.getConstantPool(), method);
        this.invokeInstructions = this.extractInvokeInstructions();
        this.returnInstructions = this.extractReturnInstructions();
        this.exitInstructions = this.extractExitInstructions(method);
        this.hasExceptionTarget = this.detectExceptionTarget(methodGen);
    }

    public static List<LineInstructionInfo> buildLineInstructions(final ClassGen classGen,
        MethodGen methodGen, Method method, boolean isInternalClass) {
        List<LineInstructionInfo> instructionInfoList = new ArrayList<>();
        Set<Integer> visitedLineNumbers = new HashSet<>();
        for (LineNumberGen lineNumberGen : methodGen.getLineNumbers()) {
            if (!visitedLineNumbers.contains(lineNumberGen.getSourceLine())) {
                instructionInfoList.add(
                    new LineInstructionInfo(classGen, methodGen, method, lineNumberGen));
                visitedLineNumbers.add(lineNumberGen.getSourceLine());
            }
        }

        // If class does not contain line number
        if (visitedLineNumbers.isEmpty()) {
            instructionInfoList.add(new UnknownLineInstructionInfo(classGen, methodGen, method));
        }

        return instructionInfoList;
    }

    public int countReadInstructions() {
        return (int) this.readWriteInstructionInfos.stream()
            .filter(instruction -> !instruction.isStore).count();
    }

    public int countWrittenInstructions() {
        return (int) this.readWriteInstructionInfos.stream()
            .filter(ReadWriteInstructionInfo::isStore).count();
    }

    /**
     * Free the memory
     */
    public void dispose() {
        this.instructionHandles = null;
        this.readWriteInstructionInfos = null;
        this.invokeInstructions = null;
        this.returnInstructions = null;
    }

    public List<InstructionHandle> getExitInstructions() {
        return exitInstructions;
    }

    public InstructionHandle getFirstInstruction() {
        if (this.instructionHandles.isEmpty()) {
            return null;
        }
        return this.instructionHandles.get(0);
    }

    public List<InstructionHandle> getInstructionHandles() {
        return instructionHandles;
    }

    public List<InstructionHandle> getInvokeInstructions() {
        return invokeInstructions;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public List<ReadWriteInstructionInfo> getReadWriteInstructionHandles() {
        return readWriteInstructionInfos;
    }

    public List<InstructionHandle> getReturnInstructions() {
        return returnInstructions;
    }

    public boolean hasExceptionTarget() {
        return hasExceptionTarget;
    }

    protected boolean detectExceptionTarget(final MethodGen methodGen) {
        for (CodeExceptionGen codeExceptionGen : methodGen.getExceptionHandlers()) {
            InstructionHandle exceptionHandle = codeExceptionGen.getHandlerPC();
            if (this.instructionHandles.contains(exceptionHandle)) {
                return true;
            }
        }
        return false;
    }

    protected List<InstructionHandle> extractExitInstructions(final Method method) {
        CFGConstructor constructor = new CFGConstructor();
        CFG cfg = constructor.constructCFG(method.getCode());

        List<InstructionHandle> exitInstructionHandles = new ArrayList<>();
        for (InstructionHandle instructionHandle : this.instructionHandles) {
            for (CFGNode cfgNode : cfg.getExitList()) {
                if (cfgNode.getInstructionHandle().getPosition()
                    == instructionHandle.getPosition()) {
                    exitInstructionHandles.add(instructionHandle);
                }
            }
        }
        return exitInstructionHandles;
    }

    /**
     * Extract the instruction that belong to the target line number
     *
     * @param lineNumber      Target line number
     * @param instructionList Instruction list of target method
     * @param lineNumberTable Line number table of target method
     * @return List of instruction that belong to the target line number
     */
    protected List<InstructionHandle> extractInstructions(final int lineNumber,
        final InstructionList instructionList,
        final LineNumberTable lineNumberTable) {
        List<InstructionHandle> instructions = new ArrayList<>();
        for (InstructionHandle ih = instructionList.getStart(); ih != null; ih = ih.getNext()) {
            final int instructionLineNumber = lineNumberTable.getSourceLine(ih.getPosition());
            if (instructionLineNumber == lineNumber) {
                instructions.add(ih);
            }
        }
        return instructions;
    }

    protected List<InstructionHandle> extractInvokeInstructions() {
        List<InstructionHandle> invokeInstructions = new ArrayList<>();
        for (InstructionHandle instructionHandle : this.instructionHandles) {
            Instruction instruction = instructionHandle.getInstruction();
            if (instruction instanceof InvokeInstruction) {
                invokeInstructions.add(instructionHandle);
            }
        }
        return invokeInstructions;
    }

    protected List<ReadWriteInstructionInfo> extractRWInstructions(final String locationId,
        final boolean isInternalClass, final ConstantPoolGen constantPoolGen, final Method method) {
        List<ReadWriteInstructionInfo> readWriteInstructionInfos = new ArrayList<>();
        for (InstructionHandle instructionHandle : instructionHandles) {
            Instruction instruction = instructionHandle.getInstruction();

            // For external classes, only store the case of writing fields or array element
            if (!(isInternalClass || (instruction instanceof FieldInstruction)
                || (instruction instanceof ArrayInstruction))) {
                continue;
            }

            ReadWriteInstructionInfo info = ReadWriteInstructionInfoFactory.createRWInstructionInfo(
                instructionHandle, lineNumber, constantPoolGen, method, locationId);
            if (info != null) {
                readWriteInstructionInfos.add(info);
            }

        }
        return readWriteInstructionInfos;
    }

    protected List<InstructionHandle> extractReturnInstructions() {
        List<InstructionHandle> returnInstructions = new ArrayList<>();
        for (InstructionHandle instructionHandle : this.instructionHandles) {
            if (instructionHandle.getInstruction() instanceof ReturnInstruction) {
                returnInstructions.add(instructionHandle);
            }
        }
        return returnInstructions;
    }

    protected String genLocationId(final ClassGen classGen, final Method method,
        final LineNumberGen lineNumberGen) {
        final String className = classGen.getClassName();
        final String methodName = method.getName();
        final int lineNumber = lineNumberGen.getSourceLine();
        return StringUtils.dotJoin(className, methodName, lineNumber);
    }
}
