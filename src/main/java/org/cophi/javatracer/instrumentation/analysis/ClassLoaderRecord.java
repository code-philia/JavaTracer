package org.cophi.javatracer.instrumentation.analysis;

import java.util.HashSet;
import java.util.Set;

public class ClassLoaderRecord {

    private static ClassLoaderRecord instance = null;
    protected Set<String> loadedInternalClassesName = new HashSet<>();
    protected Set<String> loadedExternalClassesName = new HashSet<>();
    protected Set<String> loadedBootstrapClassesName = new HashSet<>();

    private ClassLoaderRecord() {

    }

    public static ClassLoaderRecord getInstance() {
        synchronized (ClassLoaderRecord.class) {
            if (instance == null) {
                instance = new ClassLoaderRecord();
            }
        }
        return instance;
    }

    public void addLoadedBootstrapClassName(String className) {
        this.loadedBootstrapClassesName.add(className);
    }

    public void addLoadedExternalClassName(String className) {
        this.loadedExternalClassesName.add(className);
    }

    public void addLoadedInternalClassName(String className) {
        this.loadedInternalClassesName.add(className);
    }

    public void clearBootstrapClasses() {
        this.loadedBootstrapClassesName.clear();
    }

    public void clearData() {
        this.clearInternalClasses();
        this.clearExternalClasses();
        this.clearBootstrapClasses();
    }

    public void clearExternalClasses() {
        this.loadedExternalClassesName.clear();
    }

    public void clearInternalClasses() {
        this.loadedInternalClassesName.clear();
    }

    public Set<String> getLoadedBootstrapClassesName() {
        return this.loadedBootstrapClassesName;
    }

    public Set<String> getLoadedExternalClassesName() {
        return this.loadedExternalClassesName;
    }

    public Set<String> getLoadedInternalClassesName() {
        return this.loadedInternalClassesName;
    }

    public boolean isLoadedBootstrapClass(final String className) {
        return this.loadedBootstrapClassesName.contains(className);
    }

    public boolean isLoadedExternalClass(final String className) {
        return this.loadedExternalClassesName.contains(className);
    }

    public boolean isLoadedIntercalClass(final String className) {
        return this.loadedInternalClassesName.contains(className);
    }
}
