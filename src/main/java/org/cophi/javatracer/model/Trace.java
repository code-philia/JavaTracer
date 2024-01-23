package org.cophi.javatracer.model;

public class Trace {

    protected long threadId;
    protected String threadName;

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public int size() {
        return 0;
    }
}
