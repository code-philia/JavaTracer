package instrumentation.cfgcoverage;

import instrumentation.Agent;
import instrumentation.AgentLogger;
import instrumentation.CommandLine;
import instrumentation.cfgcoverage.CoverageAgentParams.CoverageCollectionType;
import instrumentation.cfgcoverage.graph.CoverageGraphConstructor;
import instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import instrumentation.cfgcoverage.instr.CoverageInstrumenter;
import instrumentation.cfgcoverage.instr.CoverageTransformer;
import instrumentation.cfgcoverage.instr.MethodInstructionsInfo;
import instrumentation.cfgcoverage.output.CoverageOutputWriter;
import instrumentation.cfgcoverage.runtime.AgentRuntimeData;
import instrumentation.cfgcoverage.runtime.value.ValueExtractor;
import instrumentation.filter.GlobalFilterChecker;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import configs.AppJavaClassPath;
import utils.StopTimer;

public class CoverageAgent extends Agent {

    private static ICoverageTracerHandler tracerHandler;
    private final CoverageAgentParams agentParams;
    private final CoverageInstrumenter instrumenter;
    private final CoverageTransformer coverageTransformer;
    private StopTimer timer;

    public CoverageAgent(CommandLine cmd) {
        this.agentParams = new CoverageAgentParams(cmd);
        coverageTransformer = new CoverageTransformer(agentParams);
        instrumenter = coverageTransformer.getInstrumenter();
        switch (agentParams.getCoverageType()) {
            case BRANCH_COVERAGE:
                tracerHandler = new BranchCoverageTracerHandler();
                break;
            case UNCIRCLE_CFG_COVERAGE:
                tracerHandler = new CFGCoverageHandler();
                break;
        }
    }

    public static void _storeCoverage(OutputStream outStream, Boolean reset) {
        try {
            AgentLogger.debug("Saving coverage...");
//			CoverageAgent coverageAgent = (CoverageAgent) Agent.getAgent();
            CoverageOutput coverageOutput = tracerHandler.getCoverageOutput();
            @SuppressWarnings("resource")
            CoverageOutputWriter coverageOutputWriter = new CoverageOutputWriter(outStream);
            synchronized (coverageOutput.getCoverageGraph()) {
                coverageOutputWriter.writeCfgCoverage(coverageOutput.getCoverageGraph());
                coverageOutputWriter.writeInputData(coverageOutput.getInputData());
                coverageOutputWriter.flush();
                if (reset) {
                    tracerHandler.reset();
                }
            }
        } catch (IOException e) {
            AgentLogger.error(e);
            e.printStackTrace();
        }
        AgentLogger.debug("Finish saving coverage...");
    }

    @Override
    public void startup0(long vmStartupTime, long agentPreStartup) {
        timer = new AgentStopTimer("Tracing program for coverage", vmStartupTime, agentPreStartup);
        timer.newPoint("initGraph");
        AppJavaClassPath appClasspath = agentParams.initAppClasspath();
        GlobalFilterChecker.setup(appClasspath, null, null);
        ValueExtractor.variableLayer = agentParams.getVarLayer();
        CoverageGraphConstructor constructor = new CoverageGraphConstructor();
        CoverageSFlowGraph coverageFlowGraph = constructor.buildCoverageGraph(appClasspath,
            agentParams.getTargetMethod(), agentParams.getCdgLayer(),
            agentParams.getInclusiveMethodIds(),
            agentParams.getCoverageType() == CoverageCollectionType.UNCIRCLE_CFG_COVERAGE);
        AgentRuntimeData.coverageFlowGraph = coverageFlowGraph;
        MethodInstructionsInfo.initInstrInstructions(coverageFlowGraph);
        instrumenter.setEntryPoint(coverageFlowGraph.getStartNode().getStartNodeId().getMethodId());
        timer.newPoint("Execution");
    }

    @Override
    public void shutdown() throws Exception {
        if (agentParams.getDumpFile() != null) {
            timer.newPoint("Saving coverage");
            AgentLogger.debug("Saving coverage...");
            CoverageOutput coverageOutput = tracerHandler.getCoverageOutput();
            coverageOutput.saveToFile(agentParams.getDumpFile());
            AgentLogger.debug(timer.getResultString());
        }
    }

    @Override
    public void startTest(String junitClass, String junitMethod) {
        String testcase = InstrumentationUtils.getMethodId(junitClass, junitMethod);
        int testIdx = AgentRuntimeData.coverageFlowGraph.addCoveredTestcase(testcase);
        AgentRuntimeData.currentTestIdxMap.put(Thread.currentThread().getId(), testIdx);
        AgentLogger.debug(String.format("Start testcase %s, testIdx=%s", testcase, testIdx));
    }

    @Override
    public void exitTest(String testResultMsg, String junitClass, String junitMethod,
        long threadId) {
        Integer testIdx = AgentRuntimeData.currentTestIdxMap.get(threadId);
        AgentLogger.debug(String.format("Exit testcase %s, testIdx=%s, thread=%s",
            InstrumentationUtils.getMethodId(junitClass, junitMethod),
            testIdx, threadId));
        if (testIdx != null) {
            AgentRuntimeData.unregister(threadId, testIdx);
        }
    }

    @Override
    public void finishTest(String junitClass, String junitMethod) {
        // do nothing for now.
    }

    @Override
    public ClassFileTransformer getTransformer0() {
        return coverageTransformer;
    }

    @Override
    public void retransformBootstrapClasses(Instrumentation instrumentation,
        Class<?>[] retransformableClasses)
        throws Exception {
        // do nothing for now.
    }

    @Override
    public boolean isInstrumentationActive0() {
        return true;
    }

    public interface ICoverageTracerHandler {

        CoverageOutput getCoverageOutput();

        void reset();

    }
}
