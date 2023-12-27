package instrumentation;

import instrumentation.filter.GlobalFilterChecker;
import instrumentation.runtime.ExecutionTracer;
import instrumentation.runtime.IExecutionTracer;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LLT
 * <p>
 * Agent
 * <p>
 * * Record the testcases running process: _startTest: _finishTest: _exitProgram:
 */
public abstract class Agent {

    //	private static IAgent agent;
    private static String programMsg = "";
    private volatile static Boolean shutdowned = false;
    private static int numberOfThread = 1;
    private static Instrumentation instrumentation;

    /**
     * This method will be instrumented at the end of main() method.
     *
     * @param programMsg
     */
    public static void _exitProgram(String programMsg) {
        if (Thread.currentThread().getName().equals("main")) {
            ExecutionTracer.getMainThreadStore().lock();
            Agent.programMsg = programMsg;

            boolean allInterestedThreadsStop = false;
            while (!allInterestedThreadsStop) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                boolean needToConitnue = false;

                for (IExecutionTracer tracer : ExecutionTracer.getAllThreadStore()) {
                    if (tracer instanceof ExecutionTracer eTracer) {

                        if (eTracer.getThreadName().equals("main")
                            || eTracer.getTrace().size() == 0) {
                            continue;
                        }

                        if (!ExecutionTracer.stoppedThreads.contains(eTracer.getThreadId())) {
                            needToConitnue = true;
                            break;

                        }
                    }
                }

                allInterestedThreadsStop = !needToConitnue;
            }

            stop();
            ExecutionTracer.getMainThreadStore().unLock();
            Runtime.getRuntime().exit(
                1); // force program to exit to avoid getting stuck by background running threads.
        } else {
            /**
             * we do not record the thread any more
             */
//			long threadId = Thread.currentThread().getId();
            ExecutionTracer.stopRecordingCurrendThread();
        }
    }

    public static void _exitTest(String testResultMsg, String junitClass, String junitMethod,
        Long threadId) {
        Agent agent = AgentFactory.createAgent(AgentFactory.cmd, instrumentation);
        agent.exitTest(testResultMsg, junitClass, junitMethod, threadId);
    }

    public static String getProgramMsg() {
        return programMsg;
    }

    public static synchronized void stop() {
        synchronized (shutdowned) {
            try {
                if (!shutdowned) {
                    Agent agent = AgentFactory.createAgent(AgentFactory.cmd, instrumentation);
                    instrumentation.removeTransformer(agent.getTransformer0());
                    Class<?>[] retransformableClasses = getRetransformableClasses(instrumentation);
                    if (retransformableClasses != null) {
                        instrumentation.retransformClasses(retransformableClasses);
                    }
                    agent.shutdown();
                }
                shutdowned = true;
            } catch (Throwable e) {
                AgentLogger.error(e);
                shutdowned = true;
            }
        }
    }

    private static Class<?>[] getRetransformableClasses(Instrumentation inst) {
        AgentLogger.debug("Collect classes to reset instrumentation....");
        List<Class<?>> candidates = new ArrayList<Class<?>>();
        List<String> bootstrapIncludes = GlobalFilterChecker.getInstance().getBootstrapIncludes();
        List<String> includedLibraryClasses = GlobalFilterChecker.getInstance()
            .getIncludedLibraryClasses();
        if (bootstrapIncludes.isEmpty() && includedLibraryClasses.isEmpty()) {
            return null;
        }
        Class<?>[] classes = inst.getAllLoadedClasses();
        for (Class<?> c : classes) {
            if (bootstrapIncludes.contains(c.getName().replace(".", "/"))
                || includedLibraryClasses.contains(c.getName())) {
                if (inst.isModifiableClass(c) && inst.isRetransformClassesSupported()
                    && !ClassLoader.class.equals(c)) {
                    candidates.add(c);
                }
            }
        }
        AgentLogger.debug(candidates.size() + " retransformable candidates");
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.toArray(new Class<?>[candidates.size()]);
    }

    public static void _startTest(String junitClass, String junitMethod) {
        try {
            Agent agent = AgentFactory.createAgent(AgentFactory.cmd, instrumentation);
            agent.startTest(junitClass, junitMethod);
        } catch (Throwable e) {
            AgentLogger.error(e);
        }
    }

    public static void _finishTest(String junitClass, String junitMethod) {
        try {
            Agent agent = AgentFactory.createAgent(AgentFactory.cmd, instrumentation);
            agent.finishTest(junitClass, junitMethod);
        } catch (Throwable e) {
            AgentLogger.error(e);
        }
    }

    public static String extractJarPath() {
        return null;
    }

//	public Agent(CommandLine cmd, Instrumentation inst) {
//		instrumentation = inst;
//		if (cmd.getBoolean(CoverageAgentParams.OPT_IS_COUNT_COVERAGE, false)) {
//			agent = new CoverageAgent(cmd);
//		} else if (cmd.getBoolean(AgentParams.OPT_PRECHECK, false)) {
//			agent = new PrecheckAgent(cmd, instrumentation);
//		} else {
//			agent = new TraceAgent(cmd);
//		}
//		AgentLogger.setup(LogType.valuesOf(cmd.getStringList(AgentParams.OPT_LOG)));
//	}

    public static void _onStartThread() {
        if (!shutdowned) {
            numberOfThread++;
        }
    }

    public static int getNumberOfThread() {
        return numberOfThread;
    }

    public static boolean isInstrumentationActive() {
        Agent agent = AgentFactory.createAgent(AgentFactory.cmd, instrumentation);
        return agent.isInstrumentationActive0();
    }

    public abstract void startup0(long vmStartupTime, long agentPreStartup);

    public abstract void shutdown() throws Exception;

    public abstract void startTest(String junitClass, String junitMethod);

    public abstract void finishTest(String junitClass, String junitMethod);

    public abstract ClassFileTransformer getTransformer0();

    public abstract void retransformBootstrapClasses(Instrumentation instrumentation,
        Class<?>[] retransformableClasses)
        throws Exception;

    public abstract void exitTest(String testResultMsg, String junitClass, String junitMethod,
        long threadId);

    public abstract boolean isInstrumentationActive0();

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public void setInstrumentation(Instrumentation inst) {
        instrumentation = inst;
    }

    public void startup(long vmStartupTime, long agentPreStartup) {
        startup0(vmStartupTime, agentPreStartup);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Agent.stop();
            }
        });
    }

    public ClassFileTransformer getTransformer() {
        return getTransformer0();
    }

    public void retransformClasses(Class<?>[] retransformableClasses) throws Exception {
        Agent agent = AgentFactory.createAgent(AgentFactory.cmd, instrumentation);
        agent.retransformBootstrapClasses(instrumentation, retransformableClasses);
    }

//	public static IAgent getAgent() {
//		return agent;
//	}
}
