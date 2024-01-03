package org.cophi.javatracer.instrumentation.cfgcoverage.graph;

import org.cophi.javatracer.codeanalysis.bytecode.CFG;
import org.cophi.javatracer.codeanalysis.bytecode.CFGNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CFGInstance {

    private final CFG cfg;
    private final List<CFGNode> nodeList; // same order as in InstrucionList.
    private final List<UniqueNodeId> unitCfgNodeIds;
    private final Set<CFGNode> hasAliasNodes = new HashSet<>();
    private int cfgExtensionLayer;

    public CFGInstance(CFG unitCfg, String methodId, List<CFGNode> nodeList) {
        this.cfg = unitCfg;
        this.nodeList = nodeList;
        unitCfgNodeIds = new ArrayList<UniqueNodeId>(nodeList.size());
        for (CFGNode node : nodeList) {
            unitCfgNodeIds.add(new UniqueNodeId(methodId, node.getIdx(), node.getLineNo()));
        }
    }

    public CFGInstance(CFG unitCfg, List<CFGNode> nodeList, List<UniqueNodeId> unitCfgNodeIds) {
        this.cfg = unitCfg;
        this.nodeList = nodeList;
        this.unitCfgNodeIds = unitCfgNodeIds;
    }

    public void addAliasNode(CFGAliasNode aliasNode) {
        aliasNode.setIdx(nodeList.size());
        nodeList.add(aliasNode);
        unitCfgNodeIds.add(getUnitCfgNodeId(aliasNode.getOrgNode()));
        hasAliasNodes.add(aliasNode.getOrgNode());
    }

    public CFG getCfg() {
        return cfg;
    }

    public int getCfgExtensionLayer() {
        return cfgExtensionLayer;
    }

    public void setCfgExtensionLayer(int cfgExtensionLayer) {
        this.cfgExtensionLayer = cfgExtensionLayer;
    }

    public List<CFGNode> getNodeList() {
        return nodeList;
    }

    public UniqueNodeId getUnitCfgNodeId(CFGNode node) {
        return unitCfgNodeIds.get(node.getIdx());
    }

    public List<UniqueNodeId> getUnitCfgNodeIds() {
        return unitCfgNodeIds;
    }

    public boolean hasAlias(CFGNode curNode) {
        return hasAliasNodes.contains(curNode);
    }

    public boolean isEmpty() {
        return cfg == null;
    }

    public int size() {
        return nodeList.size();
    }

    @Override
    public String toString() {
        return "CFGInstance [cfg=" + cfg + ", nodeList=" + nodeList + "]";
    }

    public static class UniqueNodeId {

        String methodId;
        int localNodeIdx;
        int line;

        public UniqueNodeId(String methodId, int idx, int line) {
            this.methodId = methodId;
            this.localNodeIdx = idx;
            this.line = line;
        }

        public int getLine() {
            return line;
        }

        public int getLocalNodeIdx() {
            return localNodeIdx;
        }

        public String getMethodId() {
            return methodId;
        }

        public boolean match(String methodId, int nodeLocalIdx) {
            return this.methodId.equals(methodId) && this.localNodeIdx == nodeLocalIdx;
        }

        @Override
        public String toString() {
            return String.format("UniqueNodeId [%s, idx=%s, line=%s", methodId, localNodeIdx, line);
        }
    }

}
