package org.cophi.javatracer.instrumentation.agents;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.instrumentation.tracer.ExecutionTracer;
import org.cophi.javatracer.instrumentation.tracer.InstrumentMethods;
import org.cophi.javatracer.instrumentation.tracer.TracerManager;
import org.cophi.javatracer.instrumentation.tracer.TracerState;
import org.cophi.javatracer.instrumentation.tracer.factories.ExecutionTracerFactory;
import org.cophi.javatracer.instrumentation.transformers.TestRunnerTransformer;
import org.cophi.javatracer.instrumentation.transformers.TraceTransformer;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.utils.ClassNameUtils;

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
            ExecutionTracerFactory.getInstance().getMainThreadTracer();
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

    }

    public enum Methods implements InstrumentMethods {
        START_TEST("_startTest", String.class, String.class),
        FINISH_TEST("_finishTest", String.class, String.class),
        EXIT_PROGRAM("_exitProgram", String.class);

        private final String methodName;
        private final int argumentNumber;
        private final String descriptor;
        private final String declareClassName;

        Methods(final String methodName, Class<?>... argumentTypes) {
            try {
                final Class<?> clazz = DefaultAgent.class;
                Method method = clazz.getDeclaredMethod(methodName, argumentTypes);
                this.declareClassName = ClassNameUtils.canonicalToClassURIName(
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
        public String getDeclareClassName() {
            return this.declareClassName;
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
