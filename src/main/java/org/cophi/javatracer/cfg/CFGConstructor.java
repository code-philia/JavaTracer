package org.cophi.javatracer.cfg;

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

public class CFGConstructor {

    /**
     * Source that used to construct the CFG
     */
    protected Code code;

    public CFG constructCFG(final Code code) {
        this.code = code;
        CFG cfg = new CFG();

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
}
