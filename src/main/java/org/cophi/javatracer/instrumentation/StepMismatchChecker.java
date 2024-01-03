package org.cophi.javatracer.instrumentation;

import org.cophi.javatracer.instrumentation.precheck.TraceInfo;
import java.util.ArrayList;
import java.util.List;
import org.cophi.javatracer.model.BreakPoint;
import org.cophi.javatracer.model.ClassLocation;
import org.cophi.javatracer.model.trace.Trace;
import org.cophi.javatracer.model.trace.TraceNode;
import org.cophi.javatracer.utils.FileUtils;
import org.cophi.javatracer.utils.StringUtils;

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
