package instrumentation.cfgcoverage;

import java.util.LinkedHashMap;
import utils.StopTimer;

public class AgentStopTimer extends StopTimer {

    private final long vmStartupTime;
    private final long agentPreStartup;

    public AgentStopTimer(String module, long vmStartupTime, long agentPreStartup) {
        super(module);
        this.vmStartupTime = vmStartupTime;
        this.agentPreStartup = agentPreStartup;
    }

    @Override
    public synchronized LinkedHashMap<String, Long> getTimeResults() {
        LinkedHashMap<String, Long> timeResults = new LinkedHashMap<>();
        timeResults.put("JVM startup time", vmStartupTime);
        timeResults.put("Agent startup time", agentPreStartup);
        timeResults.putAll(super.getTimeResults());
        return timeResults;
    }
}
