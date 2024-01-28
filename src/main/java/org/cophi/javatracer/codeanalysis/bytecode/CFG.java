package org.cophi.javatracer.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;

public class CFG implements IGraph<CFGNode> {

    protected Method method;
    protected Map<Integer, CFGNode> nodeList = new HashMap<>();
    protected CFGNode startNode;
    protected List<CFGNode> exitList = new ArrayList<>();

    protected Code code;

    public CFG(final Code code) {
        this.code = code;
    }

    public void addExitNode(CFGNode node) {
        this.exitList.add(node);
    }

    public void addNode(CFGNode node) {
        if (!this.nodeList.containsKey(node.getInstructionHandle().getPosition())) {
            this.nodeList.put(node.getInstructionHandle().getPosition(), node);
        }
    }

    public boolean contains(CFGNode node) {
        return this.nodeList.containsKey(node.getInstructionHandle().getPosition());
    }

    public CFGNode findNode(InstructionHandle handle) {
        return this.nodeList.get(handle.getPosition());
    }

    public CFGNode findNode(int offset) {
        return this.nodeList.get(offset);
    }

    public CFGNode findOrCreateNewNode(InstructionHandle handle) {
        CFGNode node = findNode(handle);
        if (node == null) {
            node = new CFGNode(handle);
            this.nodeList.put(handle.getPosition(), node);
        }

        return node;
    }

    public int getEndLine() {
        int max = -1;
        for (LineNumber ln : code.getLineNumberTable().getLineNumberTable()) {
            int line = ln.getLineNumber();
            if (max == -1) {
                max = line;
            } else {
                max = (max > line) ? max : line;
            }
        }

        return max;
    }

    public List<CFGNode> getExitList() {
        return exitList;
    }

    public List<CFGNode> getNodeList() {
        return new ArrayList<>(nodeList.values());
    }

    public int getLineNumber(CFGNode node) {
        return this.code.getLineNumberTable()
            .getSourceLine(node.getInstructionHandle().getPosition());
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public CFGNode getStartNode() {
        return startNode;
    }

    public void setStartNode(CFGNode startNode) {
        this.startNode = startNode;
    }

    public int size() {
        return nodeList.size();
    }

}