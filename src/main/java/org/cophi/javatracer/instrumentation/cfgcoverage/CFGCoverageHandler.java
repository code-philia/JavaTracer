package org.cophi.javatracer.instrumentation.cfgcoverage;

import org.cophi.javatracer.instrumentation.cfgcoverage.CoverageAgent.ICoverageTracerHandler;
import org.cophi.javatracer.instrumentation.cfgcoverage.graph.CoveragePath;
import org.cophi.javatracer.instrumentation.cfgcoverage.graph.CoverageSFNode;
import org.cophi.javatracer.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import org.cophi.javatracer.instrumentation.cfgcoverage.runtime.AgentRuntimeData;
import org.cophi.javatracer.instrumentation.cfgcoverage.runtime.CoverageTracer;
import org.cophi.javatracer.instrumentation.cfgcoverage.runtime.MethodExecutionData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.cophi.javatracer.utils.CollectionUtils;

public class CFGCoverageHandler implements ICoverageTracerHandler {

    @Override
    public CoverageOutput getCoverageOutput() {
        CoverageSFlowGraph coverageGraph = AgentRuntimeData.coverageFlowGraph;
        Map<List<Integer>, List<Integer>> pathMap = new HashMap<>(); // path to tcs
        for (Entry<Integer, List<MethodExecutionData>> entry : CoverageTracer.methodExecsOnASingleTcMap.entrySet()) {
            for (MethodExecutionData methodExecData : entry.getValue()) {
                CollectionUtils.getListInitIfEmpty(pathMap, methodExecData.getExecPathId())
                    .add(entry.getKey());
                methodExecData.calculateBranchFitnessMap(coverageGraph);
            }
        }
        List<CoveragePath> coveredPaths = new ArrayList<>(pathMap.size());
        for (Entry<List<Integer>, List<Integer>> entry : pathMap.entrySet()) {
            CoveragePath path = new CoveragePath();
            path.setCoveredTcs(entry.getValue());
            List<CoverageSFNode> nodes = new ArrayList<>();
            for (int nodeId : entry.getKey()) {
                nodes.add(coverageGraph.getNodeList().get(nodeId));
            }
            path.setPath(nodes);
            coveredPaths.add(path);
        }
        coverageGraph.setCoveragePaths(coveredPaths);
        CoverageOutput coverageOutput = new CoverageOutput(coverageGraph);
        coverageOutput.setInputData(CoverageTracer.methodExecsOnASingleTcMap);
        return coverageOutput;
    }

    @Override
    public void reset() {
        AgentRuntimeData.coverageFlowGraph.clearData();
        CoverageTracer.methodExecsOnASingleTcMap.clear();
        CoverageTracer.rtStore.clear();
    }

}
