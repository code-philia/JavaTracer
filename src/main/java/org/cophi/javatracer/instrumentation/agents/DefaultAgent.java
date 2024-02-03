package org.cophi.javatracer.instrumentation.agents;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.instrumentation.tracer.ExecutionTracer;
import org.cophi.javatracer.instrumentation.tracer.InstrumentMethods;
import org.cophi.javatracer.instrumentation.tracer.TracerManager;
import org.cophi.javatracer.instrumentation.tracer.TracerState;
import org.cophi.javatracer.instrumentation.tracer.factories.ExecutionTracerFactory;
import org.cophi.javatracer.instrumentation.transformers.TestRunnerTransformer;
import org.cophi.javatracer.instrumentation.transformers.TraceTransformer;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.model.trace.Trace;
import org.cophi.javatracer.model.trace.TraceNode;
import org.cophi.javatracer.model.variables.VarValue;
import org.cophi.javatracer.utils.NamingUtils;

public class DefaultAgent extends JavaTracerAgent {

    protected static volatile Boolean isShutDowned = false;

    private static DefaultAgent INSTANCE = null;

    private DefaultAgent(final ProjectConfig projectConfig, Instrumentation instrumentation) {
        super(projectConfig, instrumentation);
    }

    public static DefaultAgent getInstance(final ProjectConfig projectConfig,
        final Instrumentation instrumentation) {
        synchronized (DefaultAgent.class) {
            if (DefaultAgent.INSTANCE == null) {
                DefaultAgent.INSTANCE = new DefaultAgent(projectConfig, instrumentation);
            }
            return DefaultAgent.INSTANCE;
        }
    }

    public static void _startTest(final String junitClassName, final String junitMethodName) {
        TracerManager.getInstance().setState(TracerState.TEST_STARTED);
    }

    public static void _finishTest(final String junitClassName, final String junitMethodName) {
        TracerManager.getInstance().setState(TracerState.SHUTDOWN);
    }

    public static void _exitProgram(final String programMessage) {
        if (Thread.currentThread().getName().equals("main")) {
            TracerManager.getInstance()
                .lock(ExecutionTracerFactory.getInstance().getMainThreadTracer().getThreadId());
            DefaultAgent.waitForAllInterestedThreadStop();
            DefaultAgent.stop();
            TracerManager.getInstance()
                .unlock(ExecutionTracerFactory.getInstance().getMainThreadId());
            // Force program to exit to avoid getting stuck by background running threads
            Runtime.getRuntime().exit(1);
        } else {
            TracerManager.getInstance().stopRecordingCurrentThread();
        }
    }

    protected static synchronized void stop() {
        synchronized (DefaultAgent.isShutDowned) {
            try {
                if (!DefaultAgent.isShutDowned) {
                    // Do not need argument because it should be initialized before.
                    DefaultAgent agent = DefaultAgent.getInstance(null, null);
                    agent.removeTransformers();
                    Class<?>[] classes = agent.getRetransformableClasses();
                    if (classes != null) {
                        agent.instrumentation.retransformClasses(classes);
                    }
                    agent.shutDown();
                }
                DefaultAgent.isShutDowned = true;
            } catch (Throwable e) {
                Log.error(e.getMessage(), DefaultAgent.class);
                DefaultAgent.isShutDowned = true;
            }
        }
    }

    protected static void waitForAllInterestedThreadStop() {
        boolean allInterestedThreadsStop = false;
        while (!allInterestedThreadsStop) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean needToContinue = false;
            for (ExecutionTracer executionTracer : ExecutionTracerFactory.getInstance()
                .getAllThreadTracer()) {
                if (executionTracer.getThreadName().equals("main")
                    || executionTracer.getTrace().size() == 0) {
                    continue;
                }

                if (TracerManager.getInstance().getStoppedThreads()
                    .contains(executionTracer.getThreadId())) {
                    needToContinue = true;
                    break;
                }
            }
            allInterestedThreadsStop = !needToContinue;
        }
    }

    @Override
    public void addTransformers() {
        this.instrumentation.addTransformer(new TraceTransformer(this.projectConfig), true);
        this.instrumentation.addTransformer(new TestRunnerTransformer(this.projectConfig), true);
    }

    @Override
    public void removeTransformers() {
        this.instrumentation.removeTransformer(new TraceTransformer(this.projectConfig));
        this.instrumentation.removeTransformer(new TestRunnerTransformer(this.projectConfig));
    }

    public Class<?>[] getRetransformableClasses() {
        List<Class<?>> candidates = new ArrayList<>();
        Set<String> bootstrapIncludes = LoadedClassRecord.getInstance().includedBootstrapClasses;
        Set<String> includedLibraryClasses = LoadedClassRecord.getInstance().includedExternalClasses;
        if (bootstrapIncludes.isEmpty() && includedLibraryClasses.isEmpty()) {
            return null;
        }
        Class<?>[] classes = this.instrumentation.getAllLoadedClasses();
        for (Class<?> c : classes) {
            if (bootstrapIncludes.contains(c.getName().replace(".", "/"))
                || includedLibraryClasses.contains(c.getName())) {
                if (this.instrumentation.isModifiableClass(c)
                    && this.instrumentation.isRetransformClassesSupported()
                    && !ClassLoader.class.equals(c)) {
                    candidates.add(c);
                }
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.toArray(new Class<?>[0]);
    }

    public void shutDown() {
        TracerManager.getInstance().setState(TracerState.SHUTDOWN);
        List<ExecutionTracer> tracers = ExecutionTracerFactory.getInstance().getAllThreadTracer();
        for (ExecutionTracer tracer : tracers) {
            Trace trace = tracer.getTrace();

            this.addConditionResult(trace);
            this.changeRedefinedVarID(trace);
            this.matchArrayElementName(trace);
        }
    }

    /**
     * Insert condition result into branch node. Condition is naively defined as follow: <br><br>
     * <p>
     * The condition result is true when the stepOverNext node is the next line of code. Otherwise,
     * the condition result is false.
     *
     * @param trace Target trace
     */
    private void addConditionResult(final Trace trace) {
        for (TraceNode node : trace.getExecutionList()) {
            if (node.isBranch()) {
                TraceNode stepOverNext = node.getStepOverNext();
                boolean conditionResult = stepOverNext == null ? false
                    : node.getLineNumber() + 1 == stepOverNext.getLineNumber();
                node.insertConditionResult(conditionResult);
            }
        }
    }

    private void changeRedefinedVarID(Trace trace) {
        Map<String, String> mapping = new HashMap<>();
        for (TraceNode node : trace.getExecutionList()) {
            for (VarValue readVar : node.getReadVariables()) {
                String varID = readVar.getVarID();
                if (!mapping.containsKey(varID)) {
                    mapping.put(varID, varID);
                } else {
                    String newID = mapping.get(varID);
                    readVar.setVarID(newID);
                }
            }

            for (VarValue writeVar : node.getWrittenVariables()) {
                String varID = writeVar.getVarID();
                if (mapping.containsKey(varID)) {
                    String newID = writeVar.getVarID() + "-" + node.getOrder();
                    mapping.put(varID, newID);
                    writeVar.setVarID(newID);
                }
            }
        }
    }

    private String extractIndexFromName(final String name) {
        // Define the pattern for matching text within square brackets
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");

        // Create a matcher with the input string
        Matcher matcher = pattern.matcher(name);

        // Find and print all matches
        String indexStr = "";
        while (matcher.find()) {
            indexStr = matcher.group(1); // Group 1 contains the text within brackets
        }

        return indexStr;
    }

    private void matchArrayElementName(final Trace trace) {
        /*
         * Element variables' name is the address of the array it belongs to,
         * which is not readable to human, this function will replace the
         * address by the name of array variable.
         */

        // First, store the name of all variables that have children
        Map<String, String> parentNameMap = new HashMap<>();
        for (TraceNode node : trace.getExecutionList()) {
            List<VarValue> variables = new ArrayList<>();
            variables.addAll(node.getReadVariables());
            variables.addAll(node.getWrittenVariables());
            for (VarValue var : variables) {
                if (!var.getChildren().isEmpty()) {
                    parentNameMap.put(var.getAliasVarID(), var.getVarName());
                }
            }
        }

        // Second, for every element in array, replace the name
        for (TraceNode node : trace.getExecutionList()) {
            List<VarValue> variables = new ArrayList<>();
            variables.addAll(node.getReadVariables());
            variables.addAll(node.getWrittenVariables());
            for (VarValue var : variables) {
                if (var.isElementOfArray()) {
                    // If the variable is element of array, then it must have one parent
                    final VarValue parent = var.getParents().get(0);
                    final String aliasID = parent.getVarID();
                    if (parentNameMap.containsKey(aliasID)) {
                        final String parentName = parentNameMap.get(aliasID);
                        final String indexStr = this.extractIndexFromName(var.getVarName());
                        final String newVarName = parentName + "[" + indexStr + "]";
                        var.getVariable().setName(newVarName);
                    }
                }
            }
        }

        // Last, also change the element variable of the array variable children
        for (TraceNode node : trace.getExecutionList()) {
            List<VarValue> variables = new ArrayList<>();
            variables.addAll(node.getReadVariables());
            variables.addAll(node.getWrittenVariables());
            for (VarValue var : variables) {
                if (!var.getChildren().isEmpty()) {
                    for (VarValue child : var.getChildren()) {
                        if (child.isElementOfArray()) {
                            final String indexStr = this.extractIndexFromName(child.getVarName());
                            final String newVarName = var.getVarName() + "[" + indexStr + "]";
                            child.getVariable().setName(newVarName);
                        }
                    }
                }
            }
        }
    }

    public enum Methods implements InstrumentMethods {
        START_TEST("_startTest", String.class, String.class),
        FINISH_TEST("_finishTest", String.class, String.class),
        EXIT_PROGRAM("_exitProgram", String.class);

        private final String methodName;
        private final int argumentNumber;
        private final String descriptor;
        private final String declareClassBinaryName;

        Methods(final String methodName, Class<?>... argumentTypes) {
            try {
                final Class<?> clazz = DefaultAgent.class;
                Method method = clazz.getDeclaredMethod(methodName, argumentTypes);
                this.declareClassBinaryName = NamingUtils.canonicalToClassBinaryName(
                    method.getDeclaringClass().getName());
                this.descriptor = MethodType.methodType(method.getReturnType(),
                    method.getParameterTypes()).toMethodDescriptorString();
                this.argumentNumber = this.isStatic(method) ? method.getParameterCount()
                    : method.getParameterCount() + 1;
                this.methodName = methodName;
            } catch (NoSuchMethodException e) {
                Log.fetal("Cannot find target method: " + methodName, this.getClass());
                Log.fetal(e.getMessage(), this.getClass());
                throw new RuntimeException(e);
            }
        }

        @Override
        public int getArgumentNumber() {
            return this.argumentNumber;
        }

        @Override
        public String getDeclareClassBinaryName() {
            return this.declareClassBinaryName;
        }

        @Override
        public String getDescriptor() {
            return this.descriptor;
        }

        @Override
        public String getMethodName() {
            return this.methodName;
        }

        private boolean isStatic(final Method method) {
            return Modifier.isStatic(method.getModifiers());
        }
    }
}
