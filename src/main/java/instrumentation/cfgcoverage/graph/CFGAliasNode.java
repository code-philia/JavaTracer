package instrumentation.cfgcoverage.graph;

import codeanalysis.bytecode.CFGNode;

public class CFGAliasNode extends CFGNode {

    private final CFGNode orgNode;

    public CFGAliasNode(CFGNode orgNode) {
        super(orgNode.getInstructionHandle());
        this.orgNode = orgNode;
    }

    @Override
    public String toString() {
        return super.toString() + "_alias";
    }

    public CFGNode getOrgNode() {
        return orgNode;
    }
}
