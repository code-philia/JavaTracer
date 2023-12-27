package instrumentation;

import instrumentation.AgentParams.LogType;
import instrumentation.cfgcoverage.CoverageAgent;
import instrumentation.cfgcoverage.CoverageAgentParams;
import instrumentation.precheck.PrecheckAgent;
import java.lang.instrument.Instrumentation;

/**
 * @author Yun Lin
 */
public class AgentFactory {

    public static CommandLine cmd;

    public static Agent createAgent(CommandLine cmd, Instrumentation inst) {
//		instrumentation = inst;

        Agent agent = null;

        if (cmd.getBoolean(CoverageAgentParams.OPT_IS_COUNT_COVERAGE, false)) {
            agent = new CoverageAgent(cmd);
            agent.setInstrumentation(inst);
        } else if (cmd.getBoolean(AgentParams.OPT_PRECHECK, false)) {
            agent = new PrecheckAgent(cmd, inst);
            agent.setInstrumentation(inst);
        } else {
            agent = new TraceAgent(cmd);
            agent.setInstrumentation(inst);
        }

        AgentLogger.setup(LogType.valuesOf(cmd.getStringList(AgentParams.OPT_LOG)));

        return agent;
    }
}
