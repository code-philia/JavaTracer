package org.cophi.javatracer.instrumentation.instr.instruction.info;

import org.cophi.javatracer.instrumentation.instr.ConstantPoolTrimmer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.cophi.javatracer.model.trace.ConstWrapper;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

public class SerializableLineInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8259583902255062219L;
    private final byte[] instructionList;
    private final ArrayList<Integer> interestedPCs;
    private final String[] localVars;
    private final HashMap<Integer, ConstWrapper> constPool;

    public SerializableLineInfo(InstructionList il, ArrayList<Integer> pcs,
        LocalVariableTable varTable, ConstantPoolGen cpg) {
        this.instructionList = il.getByteCode();
        this.interestedPCs = pcs;
        this.constPool = ConstantPoolTrimmer.trim(cpg);
        if (varTable != null) {
            LocalVariable[] varArray = varTable.getLocalVariableTable();
            this.localVars = new String[varArray.length];
            for (int i = 0; i < varArray.length; i++) {
                localVars[i] = varArray[i].getName();
            }
        } else {
            // no vars used
            this.localVars = new String[0];
        }

    }

    public List<InstructionHandle> getInstructions() {
        InstructionList il = new InstructionList(instructionList);
        ArrayList<InstructionHandle> instructions = new ArrayList<>();
        HashSet<Integer> pcSet = new HashSet<>(interestedPCs);

        Iterator<InstructionHandle> iter = il.iterator();
        while (iter.hasNext()) {
            InstructionHandle ih = iter.next();
            if (pcSet.contains(ih.getPosition())) {
                instructions.add(ih);
            }
        }
        return instructions;
    }

    public String[] getLocalVars() {
        return this.localVars;
    }

    public HashMap<Integer, ConstWrapper> getConstPool() {
        return this.constPool;
    }

    public String toString() {
        List<InstructionHandle> instructions = this.getInstructions();
        StringBuilder sb = new StringBuilder();
        for (InstructionHandle ih : instructions) {
            sb.append(ih.toString());
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
