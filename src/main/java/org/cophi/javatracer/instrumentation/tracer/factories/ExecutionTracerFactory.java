package org.cophi.javatracer.instrumentation.tracer.factories;

import org.cophi.javatracer.instrumentation.tracer.ExecutionTracer;

public class ExecutionTracerFactory extends TracerFactory<ExecutionTracer> {

    private static ExecutionTracerFactory INSTANCE = null;

    private ExecutionTracerFactory() {
        super();
    }

    public static ExecutionTracerFactory getInstance() {
        synchronized (ExecutionTracerFactory.class) {
            if (INSTANCE == null) {
                INSTANCE = new ExecutionTracerFactory();
            }
        }
        return INSTANCE;
    }

    @Override
    protected ExecutionTracer initTracer(long threadId, final String threadName) {
        return new ExecutionTracer(threadId, threadName, projectConfig);
    }
}
