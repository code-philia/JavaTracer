package org.cophi.javatracer.instrumentation.tracer.factories;

import groovy.lang.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.instrumentation.tracer.Tracer;

@Singleton
public abstract class TracerFactory<T extends Tracer> {

    public static final int INVALID_THREAD_ID = -1;

    protected Map<Long, T> store = new HashMap<>();
    protected long mainThreadId = TracerFactory.INVALID_THREAD_ID;
    protected ProjectConfig projectConfig = null;

    protected TracerFactory() {
    }

    public synchronized T get(final long threadId) {
        return this.store.get(threadId);
    }

    public List<T> getAllThreadTracer() {
        return new ArrayList<>(this.store.values());
    }

    public synchronized long getMainThreadId() {
        return this.mainThreadId;
    }

    public synchronized void setMainThreadId(final long mainThreadId) {
        this.mainThreadId = mainThreadId;
    }

    public T getMainThreadTracer() {
        if (this.mainThreadId == TracerFactory.INVALID_THREAD_ID) {
            return null;
        }
        return this.getOrCreateTracer(this.mainThreadId, null);
    }

    public synchronized T getOrCreateTracer(final long threadId, final String threadName) {
        if (this.store.containsKey(threadId)) {
            return this.store.get(threadId);
        } else {
            T tracer = this.initTracer(threadId, threadName);
            this.store.put(threadId, tracer);
            return tracer;
        }
    }

    public void setProjectConfig(final ProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
    }

    protected abstract T initTracer(final long threadId, final String threadName);
}
