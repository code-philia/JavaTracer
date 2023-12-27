package instrumentation;

import instrumentation.precheck.TraceInfo;
import java.util.ArrayList;
import java.util.List;
import model.BreakPoint;
import model.ClassLocation;
import model.trace.Trace;
import model.trace.TraceNode;
import utils.FileUtils;
import utils.StringUtils;

public class StepMismatchChecker {

    private static final boolean check = false;

    public static void logPrecheckSteps(TraceInfo info) {
        if (!check) {
            return;
        }
        FileUtils.writeFile("E:/lyly/WorkingFolder/step_precheck.txt",
            StringUtils.join(info.getSteps(), "\n"));
        AgentLogger.debug("size = " + info.getStepTotal());
    }

    public static void logNormalSteps(Trace trace) {
        if (!check) {
            return;
        }
        List<ClassLocation> locs = new ArrayList<>();
        for (TraceNode node : trace.getExecutionList()) {
            BreakPoint bkp = node.getBreakPoint();
            locs.add(new ClassLocation(bkp.getClassCanonicalName(), bkp.getMethodSign(),
                bkp.getLineNumber()));
        }
        FileUtils.writeFile("E:/lyly/WorkingFolder/step_run.txt", StringUtils.join(locs, "\n"));
        AgentLogger.debug("Trace size = " + trace.getExecutionList().size());
    }

}
