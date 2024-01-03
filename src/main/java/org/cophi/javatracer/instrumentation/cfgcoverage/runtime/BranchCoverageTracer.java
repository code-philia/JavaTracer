package org.cophi.javatracer.instrumentation.cfgcoverage.runtime;

import org.cophi.javatracer.instrumentation.AgentLogger;
import org.cophi.javatracer.instrumentation.cfgcoverage.graph.CoverageSFNode;

public class BranchCoverageTracer extends EmptyCoverageTracer implements ICoverageTracer {

    private final String testcase;
    private CoverageSFNode currentNode;
    private volatile boolean canceled = false;

    public BranchCoverageTracer(int currentTcIdx) {
        this.testcase = AgentRuntimeData.coverageFlowGraph.getCoveredTestcases().get(currentTcIdx);
    }

    public synchronized static ICoverageTracer _getTracer(String methodId) {
        try {
            long threadId = Thread.currentThread().getId();
            Integer currentTcIdx = AgentRuntimeData.currentTestIdxMap.get(threadId);
            if (currentTcIdx == null) {
                return getInstance();
            }
            BranchCoverageTracer tracer = new BranchCoverageTracer(currentTcIdx);
            AgentRuntimeData.register(tracer, threadId, currentTcIdx);
            return tracer;
        } catch (Throwable t) {
            AgentLogger.error(t);
            return getInstance();
        }
    }

    @Override
    public void _reachNode(String methodId, int nodeIdx) {
        if (canceled) {
            return;
        }
        if (currentNode == null) {
            currentNode = AgentRuntimeData.coverageFlowGraph.getStartNode();
        } else {
            CoverageSFNode branch = currentNode.getCorrespondingBranch(methodId, nodeIdx);
            if (branch != null) {
                currentNode.markCoveredBranch(branch, testcase);
                currentNode = branch;
            } else {
                AgentLogger.debug(
                    String.format("cannot find branch %s:%d of node %d [testcase=%s]", methodId,
                        nodeIdx,
                        currentNode.getCvgIdx(), testcase));
                return;
            }
        }
        currentNode.addCoveredTestcase(testcase);
    }

    @Override
    public void shutDown() {
        this.canceled = true;
    }
}
