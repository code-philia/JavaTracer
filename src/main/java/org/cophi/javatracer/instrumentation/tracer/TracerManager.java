package org.cophi.javatracer.instrumentation.tracer;

import groovy.lang.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public final class TracerManager {

    /**
     * If the thread is in {@code TRACKING} status, then steps will be recorded. <br/>
     */
    public static final long TRACKING = 0;
    /**
     * If the thread is in {@code UNTRACKING} status, then steps will not be recorded. <br/>
     */
    public static final long UNTRACKING = 1;
    /**
     * The initial length of the thread array.
     */
    private static final int INIT_LENGTH = 10;
    /**
     * The default thread id.
     */
    private static final long DEFAULT_THREAD_ID = -1L;
    /**
     * The default status of the thread.
     */
    private static final long DEFAULT_STATUS = TracerManager.TRACKING;
    private static TracerManager INSTANCE = null;
    private final List<Long> stoppedThreads = new ArrayList<>();
    /**
     * Store the status of each thread. <br/> The first dimension is the thread id. <br/> The second
     * dimension is the status of the thread. <br/>
     * <p>
     * If the thread status is {@code TRACKING}, then steps will be recorded. <br/> If the thread
     * status is {@code UNTRACKING}, then steps will not be recorded. <br/>
     * </p>
     */
    volatile long[][] lockedThreadIds = new long[TracerManager.INIT_LENGTH][2];
    private volatile TracerState state = TracerState.INIT;

    private TracerManager() {
        for (int i = 0; i < lockedThreadIds.length; i++) {
            this.lockedThreadIds[i][0] = TracerManager.DEFAULT_THREAD_ID;
            this.lockedThreadIds[i][1] = TracerManager.DEFAULT_STATUS;
        }
    }

    public static TracerManager getInstance() {
        synchronized (TracerManager.class) {
            if (INSTANCE == null) {
                INSTANCE = new TracerManager();
            }
        }
        return INSTANCE;
    }

    public void addStoppedThread(final long threadId) {
        this.stoppedThreads.add(threadId);
    }

    public TracerState getState() {
        return state;
    }

    public void setState(TracerState state) {
        this.state = state;
    }

    public List<Long> getStoppedThreads() {
        return stoppedThreads;
    }

    /**
     * Check if the target thread is in {@code UNTRACKING} status.
     *
     * @param threadId Target thread id
     * @return {@code true} if the target thread is in {@code UNTRACKING} status. {@code false}
     * otherwise.
     */
    public synchronized boolean isLocked(final long threadId) {
        for (long[] lockedThreadId : lockedThreadIds) {
            if (lockedThreadId[0] == threadId) {
                return lockedThreadId[1] == TracerManager.UNTRACKING;
            }
        }
        return false;
    }

    /**
     * Set the target thread into {@code UNTRACKING} status.
     *
     * @param threadId Target thread id
     */
    public synchronized boolean lock(final long threadId) {
        boolean isOriginallyLocked = this.isLocked(threadId);
        if (!isOriginallyLocked) {
            for (long[] lockedThreadId : lockedThreadIds) {
                if (lockedThreadId[0] == threadId) {
                    lockedThreadId[1] = TracerManager.UNTRACKING;
                }
            }
        }
        return isOriginallyLocked;
    }

    public synchronized void stopRecordingCurrentThread() {
        long threadId = Thread.currentThread().getId();
        this.lock(threadId);
        this.addStoppedThread(threadId);
    }

    /**
     * Set the target thread into {@code TRACKING} status.
     *
     * @param threadId Target thread id
     */
    public synchronized void unlock(final long threadId) {
        // Increase the thread array size if the array is full.
        if (this.isArrayFull()) {
            this.increaseSize();
        }
        final int idx = this.findFirstAvailableIndex(threadId);
        this.lockedThreadIds[idx][0] = threadId;
        this.lockedThreadIds[idx][1] = TracerManager.TRACKING;
    }

    /**
     * Find the first available index in the thread array.
     *
     * @return The first available index in the thread array. Return {@code -1} if the array is
     * full.
     */
    private int findFirstAvailableIndex(final long threadId) {
        for (int i = 0; i < this.lockedThreadIds.length; i++) {
            if (this.lockedThreadIds[i][0] == TracerManager.DEFAULT_THREAD_ID ||
                this.lockedThreadIds[i][0] == threadId) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Increase the thread array size.
     */
    private void increaseSize() {
        long[][] newLockedThreadIds = new long[this.lockedThreadIds.length + 2][2];
        for (int i = 0; i < this.lockedThreadIds.length; i++) {
            newLockedThreadIds[i][0] = this.lockedThreadIds[i][0];
            newLockedThreadIds[i][1] = this.lockedThreadIds[i][1];
        }
        for (int i = this.lockedThreadIds.length; i < newLockedThreadIds.length; i++) {
            newLockedThreadIds[i][0] = TracerManager.DEFAULT_THREAD_ID;
            newLockedThreadIds[i][1] = TracerManager.DEFAULT_STATUS;
        }
        this.lockedThreadIds = newLockedThreadIds;
    }

    /**
     * Check if the thread array is full.
     *
     * @return {@code true} if the thread array is full. {@code false} otherwise.
     */
    private boolean isArrayFull() {
        return this.lockedThreadIds[this.lockedThreadIds.length - 1][0]
            != TracerManager.DEFAULT_THREAD_ID;
    }
}
