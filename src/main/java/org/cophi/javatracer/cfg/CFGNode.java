package org.cophi.javatracer.cfg;

import java.util.ArrayList;
import java.util.List;
import org.apache.bcel.Const;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.Select;

public class CFGNode implements IGraphNode<CFGNode> {

    private final List<CFGNode> controlDependentees = new ArrayList<>();
    private int idx; // index of instruction in instructionList
    private int lineNo; // optional
    private InstructionHandle instructionHandle;
    private List<CFGNode> parents = new ArrayList<>();
    private List<CFGNode> children = new ArrayList<>();
    private BlockNode blockNode;

    public CFGNode(InstructionHandle insHandle) {
        super();
        this.instructionHandle = insHandle;
    }

    public void addChild(CFGNode child) {
        this.children.add(child);
    }

    public void addControlDominatee(CFGNode child) {
        this.controlDependentees.add(child);

    }

    public void addParent(CFGNode parent) {
        this.parents.add(parent);
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    public void setBlockNode(BlockNode blockNode) {
        this.blockNode = blockNode;
    }

    public List<CFGNode> getChildren() {
        return children;
    }

    public List<CFGNode> getParents() {
        return parents;
    }

    public void setParents(List<CFGNode> parents) {
        this.parents = parents;
    }

    public void setChildren(List<CFGNode> children) {
        this.children = children;
    }

    public List<CFGNode> getControlDependentees() {
        return controlDependentees;
    }

    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("node[%d,%s,line %d]", idx,
            Const.getOpcodeName(instructionHandle.getInstruction().getOpcode()), lineNo));
        if (!children.isEmpty()) {
            sb.append(", branches={");
            for (int i = 0; i < children.size(); ) {
                CFGNode child = children.get(i++);
                sb.append(String.format("node[%d,%s,line %d]", child.idx,
                    Const.getOpcodeName(child.instructionHandle.getInstruction().getOpcode()),
                    child.lineNo));
                if (i < children.size()) {
                    sb.append(",");
                }
            }
            sb.append("}");
        }
        return sb.toString();
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public InstructionHandle getInstructionHandle() {
        return instructionHandle;
    }

    public void setInstructionHandle(InstructionHandle insHandle) {
        this.instructionHandle = insHandle;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    @Override
    public int hashCode() {
        return this.instructionHandle.getPosition();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CFGNode otherNode) {
            return this.instructionHandle.getPosition() == otherNode.getInstructionHandle()
                .getPosition();
        }

        return false;
    }

    @Override
    public String toString() {
        return getDisplayString();
    }

    public boolean isBranch() {
        return getChildren().size() > 1;
    }

    public boolean isConditional() {
        return this.instructionHandle.getInstruction() instanceof Select
            || this.instructionHandle.getInstruction() instanceof IfInstruction;
    }
}
