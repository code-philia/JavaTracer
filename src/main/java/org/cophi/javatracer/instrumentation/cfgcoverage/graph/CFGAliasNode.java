package org.cophi.javatracer.instrumentation.cfgcoverage.graph;

import org.cophi.javatracer.codeanalysis.bytecode.CFGNode;

public class CFGAliasNode extends CFGNode {

    private final CFGNode orgNode;

    public CFGAliasNode(CFGNode orgNode) {
        super(orgNode.getInstructionHandle());
        this.orgNode = orgNode;
    }

    public CFGNode getOrgNode() {
        return orgNode;
    }

    @Override
    public String toString() {
        return super.toString() + "_alias";
    }
}
