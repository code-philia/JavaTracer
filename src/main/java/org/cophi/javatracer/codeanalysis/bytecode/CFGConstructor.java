package org.cophi.javatracer.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.JsrInstruction;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.cophi.javatracer.model.variables.ArrayElementVar;
import org.cophi.javatracer.model.variables.FieldVar;
import org.cophi.javatracer.model.variables.LocalVar;
import org.cophi.javatracer.model.variables.Variable;

public class CFGConstructor {

    /**
     * Source that used to construct the CFG
     */
    protected Code code;

    public CFG buildCFGWithControlDomiance(Code code) {
        this.code = code;

        CFG cfg = constructCFG(code);

        constructPostDomination(cfg);
        constructControlDependency(cfg);

//		constructDataDependency(cfg);

        return cfg;
    }

    public CFG constructCFG(final Code code) {
        this.code = code;
        CFG cfg = new CFG(code);

        CFGNode previousNode = null;
        InstructionList instructionList = new InstructionList(code.getCode());
        for (InstructionHandle instructionHandle : instructionList) {
            CFGNode node = cfg.findOrCreateNewNode(instructionHandle);
            if (previousNode != null) {
                Instruction instruction = previousNode.getInstructionHandle().getInstruction();
                if (this.isNonJumpInstruction(instruction)) {
                    node.addParent(previousNode);
                    previousNode.addChild(node);
                }
            } else {
                cfg.setStartNode(node);
            }

            if (instructionHandle.getInstruction() instanceof Select switchInstruction) {
                for (InstructionHandle targetHandle : switchInstruction.getTargets()) {
                    CFGNode targetNode = cfg.findOrCreateNewNode(targetHandle);
                    targetNode.addParent(node);
                    node.addChild(targetNode);
                }

                InstructionHandle targetHandle = switchInstruction.getTarget();
                if (targetHandle != null) {
                    CFGNode targetNode = cfg.findOrCreateNewNode(targetHandle);
                    if (!node.getChildren().contains(targetNode)) {
                        targetNode.addParent(node);
                        node.addChild(targetNode);
                    }
                }
            } else if (instructionHandle.getInstruction() instanceof BranchInstruction branchInstruction) {
                InstructionHandle target = branchInstruction.getTarget();
                CFGNode targetNode = cfg.findOrCreateNewNode(target);
                targetNode.addParent(node);
                node.addChild(targetNode);
            }
            previousNode = node;
        }

        this.attachTryCatchControlFlow(cfg, code);
        this.setExitNodes(cfg);

        int idx = 0;
        for (InstructionHandle instructionHandle : instructionList) {
            cfg.findNode(instructionHandle).setIdx(idx++);
        }
        return cfg;
    }

    /**
     * This method can only be called after the post domination relation is built in {@code cfg}.
     * Given a branch node, I traverse its children in first order. All its non-post-dominatees are
     * its control dependentees.
     *
     * @param cfg
     */
    public void constructControlDependency(CFG cfg) {
        for (CFGNode branchNode : cfg.getNodeList()) {
            if (branchNode.isBranch()) {
                computeControlDependentees(branchNode, branchNode.getChildren());
            }
        }
    }

    public void constructDataDependency(CFG cfg) {
        for (CFGNode node : cfg.getNodeList()) {
            node.parseReadWrittenVariable(code);
            node.intializeGenSet();
        }

        System.currentTimeMillis();

        boolean change = true;
        while (change) {
            change = false;

            for (CFGNode node : cfg.getNodeList()) {
                List<CFGNode> oldOutSet = clone(node.getOutSet());

                List<CFGNode> inSet = new ArrayList<>();
                for (CFGNode parent : node.getParents()) {
                    union(inSet, parent.getOutSet());
                }
                union(inSet, node.getOutSet());
                union(inSet, node.getGenSet());

                List<CFGNode> newOutSet = kill(inSet, node);
                node.setOutSet(newOutSet);

                if (!equal(oldOutSet, newOutSet)) {
                    change = true;
                }
            }
        }

        for (CFGNode useNode : cfg.getNodeList()) {
            for (CFGNode defNode : useNode.getOutSet()) {
                boolean isRDChain = isRDChain(defNode, useNode);
                if (isRDChain) {
                    useNode.addDefineNode(defNode);
                    defNode.addUseNode(useNode);
                }
            }
        }
    }

    public void constructPostDomination(CFG cfg) {
        /** connect basic post domination relation */
        for (CFGNode node : cfg.getNodeList()) {
            node.addPostDominatee(node);
            for (CFGNode parent : node.getParents()) {
                if (!parent.isBranch()) {
                    node.addPostDominatee(parent);
                    for (CFGNode postDominatee : parent.getPostDominatee()) {
                        node.addPostDominatee(postDominatee);
                    }
                }
            }
        }

        /** extend */
        extendPostDominatee(cfg);
    }

    protected void attachTryCatchControlFlow(final CFG cfg, final Code code) {
        CodeException[] exceptions = code.getExceptionTable();
        if (exceptions == null) {
            return;
        }

        for (CodeException exception : exceptions) {
            int start = exception.getStartPC();
            int end = exception.getEndPC();
            int handle = exception.getHandlerPC();
            CFGNode targetNode = cfg.findNode(handle);

            for (int i = start; i <= end; i++) {
                CFGNode sourceNode = cfg.findNode(i);
                if (sourceNode != null && sourceNode != targetNode) {
                    sourceNode.addChild(targetNode);
                    targetNode.addParent(sourceNode);
                }
            }
        }
    }

    protected boolean isNonJumpInstruction(Instruction instruction) {
        return !(instruction instanceof GotoInstruction)
            && !(instruction instanceof ReturnInstruction) && !(instruction instanceof ATHROW) &&
            !(instruction instanceof JsrInstruction) && !(instruction instanceof Select);
    }

    protected void setExitNodes(final CFG cfg) {
        for (CFGNode node : cfg.getNodeList()) {
            if (node.getChildren().isEmpty()) {
                cfg.addExitNode(node);
            } else {
                if (node.getInstructionHandle().getInstruction() instanceof ReturnInstruction) {
                    cfg.addExitNode(node);
                }
            }
        }
    }

    private boolean allBranchTargetsReachedByDominatees(CFGNode branchNode, CFGNode postDominator) {
        for (CFGNode target : branchNode.getChildren()) {
            if (!postDominator.canReachDominatee(target)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkBranchDomination(CFGNode branchNode, CFGNode postDominator) {
        boolean isExpend = false;
        if (allBranchTargetsReachedByDominatees(branchNode, postDominator)) {
            if (!postDominator.getPostDominatee().contains(branchNode)) {
                isExpend = true;
                postDominator.getPostDominatee().add(branchNode);
            }
        }
        return isExpend;
    }

    private List<CFGNode> clone(List<CFGNode> outSet) {
        List<CFGNode> list = new ArrayList<>();
        for (CFGNode node : outSet) {
            list.add(node);
        }
        return list;
    }

    private void computeControlDependentees(CFGNode branchNode, List<CFGNode> list) {
        for (CFGNode child : list) {
            if (!child.canReachDominatee(branchNode) && !branchNode.getControlDependentees()
                .contains(child)) {
                branchNode.addControlDominatee(child);
                computeControlDependentees(branchNode, child.getChildren());
            }
        }

    }

    private boolean equal(List<CFGNode> oldOutSet, List<CFGNode> newOutSet) {
        if (oldOutSet.size() != newOutSet.size()) {
            return false;
        }

        int count = 0;
        for (CFGNode n : oldOutSet) {
            for (CFGNode m : newOutSet) {
                if (n.equals(m)) {
                    count++;
                    break;
                }
            }
        }

        return count == oldOutSet.size();
    }

    private void extendPostDominatee(CFG cfg) {
        boolean isClose = false;

        while (!isClose) {
            isClose = true;
            for (CFGNode nodei : cfg.getNodeList()) {
                if (nodei.isBranch()) {
                    for (CFGNode nodej : cfg.getNodeList()) {
                        if (!nodei.equals(nodej)) {
                            boolean isAppend = checkBranchDomination(nodei, nodej);
                            isClose = isClose && !isAppend;
                        }
                    }
                }
            }
        }
    }

    private boolean isRDChain(CFGNode defNode, CFGNode useNode) {
        for (Variable var1 : defNode.getWrittenVars()) {
            for (Variable var2 : useNode.getReadVars()) {
                if (isSame(var1, var2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSame(Variable var1, Variable var2) {
        if (var1 instanceof FieldVar && var2 instanceof FieldVar) {
            FieldVar fVar1 = (FieldVar) var1;
            FieldVar fVar2 = (FieldVar) var2;
            return fVar1.getDeclaringType().equals(fVar2.getDeclaringType()) &&
                fVar1.getName().equals(fVar2.getName());
        } else if (var1 instanceof LocalVar && var2 instanceof LocalVar) {
            LocalVar lVar1 = (LocalVar) var1;
            LocalVar lVar2 = (LocalVar) var2;
            return lVar1.getByteCodeIndex() == lVar2.getByteCodeIndex();
        } else if (var1 instanceof ArrayElementVar && var2 instanceof ArrayElementVar) {
            ArrayElementVar aVar1 = (ArrayElementVar) var1;
            ArrayElementVar aVar2 = (ArrayElementVar) var2;
            return aVar1.getType().equals(aVar2.getType());
        }

        return false;
    }

    private List<CFGNode> kill(List<CFGNode> inSet, CFGNode node) {
        Iterator<CFGNode> iter = inSet.iterator();
        while (iter.hasNext()) {
            CFGNode inNode = iter.next();
            for (Variable var1 : inNode.getWrittenVars()) {
                for (Variable var2 : node.getWrittenVars()) {
                    if (isSame(var1, var2) && !node.getGenSet().contains(inNode)) {
                        iter.remove();
                    }
                }
            }
        }

        return inSet;
    }

    private void union(List<CFGNode> inSet, List<CFGNode> genSet) {
        for (CFGNode genNode : genSet) {
            if (!inSet.contains(genNode)) {
                inSet.add(genNode);
            }
        }
    }
}
