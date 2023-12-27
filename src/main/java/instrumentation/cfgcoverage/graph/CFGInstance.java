package instrumentation.cfgcoverage.graph;

import codeanalysis.bytecode.CFG;
import codeanalysis.bytecode.CFGNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CFGInstance {

    private final CFG cfg;
    private final List<CFGNode> nodeList; // same order as in InstrucionList.
    private final List<UniqueNodeId> unitCfgNodeIds;
    private int cfgExtensionLayer;
    private final Set<CFGNode> hasAliasNodes = new HashSet<>();

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

    public boolean hasAlias(CFGNode curNode) {
        return hasAliasNodes.contains(curNode);
    }

    public CFG getCfg() {
        return cfg;
    }

    public List<CFGNode> getNodeList() {
        return nodeList;
    }

    public boolean isEmpty() {
        return cfg == null;
    }

    public List<UniqueNodeId> getUnitCfgNodeIds() {
        return unitCfgNodeIds;
    }

    @Override
    public String toString() {
        return "CFGInstance [cfg=" + cfg + ", nodeList=" + nodeList + "]";
    }

    public int getCfgExtensionLayer() {
        return cfgExtensionLayer;
    }

    public void setCfgExtensionLayer(int cfgExtensionLayer) {
        this.cfgExtensionLayer = cfgExtensionLayer;
    }

    public int size() {
        return nodeList.size();
    }

    public UniqueNodeId getUnitCfgNodeId(CFGNode node) {
        return unitCfgNodeIds.get(node.getIdx());
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

        public String getMethodId() {
            return methodId;
        }

        public int getLocalNodeIdx() {
            return localNodeIdx;
        }

        public boolean match(String methodId, int nodeLocalIdx) {
            return this.methodId.equals(methodId) && this.localNodeIdx == nodeLocalIdx;
        }

        @Override
        public String toString() {
            return String.format("UniqueNodeId [%s, idx=%s, line=%s", methodId, localNodeIdx, line);
        }

        public int getLine() {
            return line;
        }
    }

}
