package org.cophi.javatracer.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;
import org.cophi.javatracer.model.variables.ArrayElementVar;
import org.cophi.javatracer.model.variables.FieldVar;
import org.cophi.javatracer.model.variables.LocalVar;
import org.cophi.javatracer.model.variables.Variable;
import org.cophi.javatracer.utils.SignatureUtils;

public class CFGNode implements IGraphNode<CFGNode> {

    private InstructionHandle instructionHandle;
    private List<CFGNode> parents = new ArrayList<>();
    private List<CFGNode> children = new ArrayList<>();

    private HashSet<CFGNode> postDominatee = new HashSet<>();

    private List<CFGNode> controlDependentees = new ArrayList<>();
    private int idx; // index of instruction in instructionList
    private List<Variable> readVars = new ArrayList<>();
    private List<Variable> writtenVars = new ArrayList<>();
    private List<CFGNode> genSet = new ArrayList<>();
    private List<CFGNode> outSet = new ArrayList<>();
    private List<CFGNode> defineSet = new ArrayList<>();
    private List<CFGNode> useSet = new ArrayList<>();
    private BlockNode blockNode;
    private int lineNo;

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

    public void addDefineNode(CFGNode node) {
        if (!this.defineSet.contains(node)) {
            this.defineSet.add(node);
        }
    }

    public void addParent(CFGNode parent) {
        this.parents.add(parent);
    }

    public void addPostDominatee(CFGNode node) {
        this.postDominatee.add(node);
    }

    public void addReadVariable(Variable var) {
        this.readVars.add(var);
    }

    public void addUseNode(CFGNode node) {
        if (!this.useSet.contains(node)) {
            this.useSet.add(node);
        }
    }

    public void addWrittenVariable(Variable var) {
        this.writtenVars.add(var);
    }

    public boolean canReachDominatee(CFGNode target) {
        HashSet<CFGNode> visitedNodes = new HashSet<>();
        return canReachDominatee(target, visitedNodes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CFGNode) {
            CFGNode otherNode = (CFGNode) obj;
            return this.instructionHandle.getPosition() == otherNode.getInstructionHandle()
                .getPosition();
        }

        return false;
    }

    @Override
    public String toString() {
        return "CFGNode [insHandle=" + instructionHandle + "]";
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

    public List<CFGNode> getDefineSet() {
        return defineSet;
    }

    public void setDefineSet(List<CFGNode> defineSet) {
        this.defineSet = defineSet;
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

    public List<CFGNode> getGenSet() {
        return genSet;
    }

    public void setGenSet(List<CFGNode> genSet) {
        this.genSet = genSet;
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

    public List<CFGNode> getOutSet() {
        return outSet;
    }

    public void setOutSet(List<CFGNode> outSet) {
        this.outSet = outSet;
    }

    public HashSet<CFGNode> getPostDominatee() {
        return postDominatee;
    }

    public void setPostDominatee(HashSet<CFGNode> originalSet) {
        this.postDominatee = originalSet;

    }

    public List<Variable> getReadVars() {
        return readVars;
    }

    public void setReadVars(List<Variable> readVars) {
        this.readVars = readVars;
    }

    public List<CFGNode> getUseSet() {
        return useSet;
    }

    public void setUseSet(List<CFGNode> useSet) {
        this.useSet = useSet;
    }

    public List<Variable> getWrittenVars() {
        return writtenVars;
    }

    public void setWrittenVars(List<Variable> writtenVars) {
        this.writtenVars = writtenVars;
    }

    public void intializeGenSet() {
        if (!this.getWrittenVars().isEmpty()) {
            this.genSet.add(this);
        }
    }

    public boolean isBranch() {
        return getChildren().size() > 1;
    }

    public boolean isConditional() {
        return this.instructionHandle.getInstruction() instanceof Select
            || this.instructionHandle.getInstruction() instanceof IfInstruction;
    }

    public void parseReadWrittenVariable(Code code) {

        ConstantPoolGen pool = new ConstantPoolGen(code.getConstantPool());
        InstructionHandle insHandle = getInstructionHandle();

        if (insHandle.getInstruction() instanceof FieldInstruction) {
            FieldInstruction gIns = (FieldInstruction) insHandle.getInstruction();
            String fullFieldName = gIns.getFieldName(pool);
            if (fullFieldName != null) {
                /** rw being true means read; and rw being false means write. **/
                boolean rw = insHandle.getInstruction().getName().toLowerCase().contains("get");
                ;
                boolean isStatic = insHandle.getInstruction().getName().toLowerCase()
                    .contains("static");

                String type = gIns.getFieldType(pool).getSignature();
                type = SignatureUtils.signatureToName(type);

                String declaringType = gIns.getReferenceType(pool).getSignature();
                FieldVar var = new FieldVar(isStatic, fullFieldName, type, declaringType);

                if (rw) {
                    addReadVariable(var);
                } else {
                    addWrittenVariable(var);
                }
            }
        } else if (insHandle.getInstruction() instanceof LocalVariableInstruction) {
            LocalVariableInstruction lIns = (LocalVariableInstruction) insHandle.getInstruction();
            Type type = lIns.getType(pool);

            String name;
            LocalVariable variable = code.getLocalVariableTable()
                .getLocalVariable(lIns.getIndex(), insHandle.getPosition());
            if (null != variable) {
                name = variable.getName();
            } else {
                name = String.valueOf(lIns.getIndex());
            }

            LocalVar var = new LocalVar(name, SignatureUtils.signatureToName(type.getSignature()),
                null, -1);
            var.setByteCodeIndex(lIns.getIndex());
            if (insHandle.getInstruction() instanceof IINC) {
                addReadVariable(var);
                addWrittenVariable(var);
            } else if (insHandle.getInstruction() instanceof LoadInstruction) {
                addReadVariable(var);
            } else if (insHandle.getInstruction() instanceof StoreInstruction) {
                addWrittenVariable(var);
            }
        } else if (insHandle.getInstruction() instanceof ArrayInstruction) {
            ArrayInstruction aIns = (ArrayInstruction) insHandle.getInstruction();
            String typeSig = aIns.getType(pool).getSignature();
            String typeName = SignatureUtils.signatureToName(typeSig);

            if (insHandle.getInstruction().getName().toLowerCase().contains("load")) {
                ArrayElementVar var = new ArrayElementVar(null, typeName, null);
                addReadVariable(var);
            } else if (insHandle.getInstruction().getName().toLowerCase().contains("store")) {
                ArrayElementVar var = new ArrayElementVar(null, typeName, null);
                addWrittenVariable(var);
            }
        }

    }

    private boolean canReachDominatee(CFGNode target, HashSet<CFGNode> visitedNodes) {
        for (CFGNode postDominatee : this.getPostDominatee()) {
            if (visitedNodes.contains(postDominatee)) {
                continue;
            }
            visitedNodes.add(postDominatee);

            if (postDominatee.equals(target)) {
                return true;
            } else if (!postDominatee.equals(this)) {
                boolean can = postDominatee.canReachDominatee(target, visitedNodes);
                if (can) {
                    return true;
                }
            }
        }

        return false;
    }
}
