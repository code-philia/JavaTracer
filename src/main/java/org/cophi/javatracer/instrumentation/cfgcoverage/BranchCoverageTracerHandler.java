package org.cophi.javatracer.instrumentation.cfgcoverage;

import org.cophi.javatracer.instrumentation.cfgcoverage.CoverageAgent.ICoverageTracerHandler;
import org.cophi.javatracer.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import org.cophi.javatracer.instrumentation.cfgcoverage.runtime.AgentRuntimeData;

public class BranchCoverageTracerHandler implements ICoverageTracerHandler {

    @Override
    public CoverageOutput getCoverageOutput() {
        CoverageSFlowGraph coverageGraph = AgentRuntimeData.coverageFlowGraph;
        CoverageOutput coverageOutput = new CoverageOutput(coverageGraph);
        return coverageOutput;
    }

    @Override
    public void reset() {
        AgentRuntimeData.coverageFlowGraph.clearData();
        AgentRuntimeData.currentTestIdxMap.clear();
    }

}
