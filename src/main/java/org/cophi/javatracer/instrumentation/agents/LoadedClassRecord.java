package org.cophi.javatracer.instrumentation.agents;

import java.util.HashSet;
import java.util.Set;
import org.cophi.javatracer.log.Log;

public class LoadedClassRecord {

    private static LoadedClassRecord INSTANCE = null;
    /**
     * Set of internal class names loaded during runtime. Internal classes are the classes defined
     * in the target project. It serves are a record of filtering.
     */
    protected Set<String> includedInternalClasses = new HashSet<>();
    /**
     * Set of excluded internal classes. Internal classes are the classes defined in the target
     * project. It serves are a record of filtering.
     */
    protected Set<String> excludedInternalClasses = new HashSet<>();
    /**
     * Set of included external class names. External classes are the classes that used, but not
     * defined by the target project. For example, some external libraries. JavaTracer allow user to
     * specify which external classes should be instrumented. <br/> It serves are a record of
     * filtering.
     */
    protected Set<String> includedExternalClasses = new HashSet<>();
    /**
     * Set of excluded external class names. External classes are the classes that used, but not
     * defined by the target project. For example, some external libraries. JavaTracer allow user to
     * specify which external classes should not be instrumented. <br/> It serves are a record of
     * filtering.
     */
    protected Set<String> excludedExternalClasses = new HashSet<>();
    /**
     * Set of included boostrap class names. Bootstrap classes are the classes that loaded by the
     * bootstrap class loader. <br/> It serves are a record of filtering.
     */
    protected Set<String> includedBootstrapClasses = new HashSet<>();
    /**
     * Set of excluded boostrap class names. Bootstrap classes are the classes loaded by the
     * bootstrap class loader. <br/> It serves are a record of filtering.
     */
    protected Set<String> excludedBootstrapClasses = new HashSet<>();

    private LoadedClassRecord() {
    }

    public static LoadedClassRecord getInstance() {
        synchronized (LoadedClassRecord.class) {
            if (LoadedClassRecord.INSTANCE == null) {
                LoadedClassRecord.INSTANCE = new LoadedClassRecord();
            }
        }
        return LoadedClassRecord.INSTANCE;
    }

    public void clearExcludedBoostrapClasses() {
        this.excludedBootstrapClasses.clear();
    }

    public void clearExcludedExternalClasses() {
        this.excludedExternalClasses.clear();
    }

    public void clearExcludedInternalClasses() {
        this.excludedInternalClasses.clear();
    }

    public void clearIncludedBoostrapClasses() {
        this.includedBootstrapClasses.clear();
    }

    public void clearIncludedExternalClasses() {
        this.includedExternalClasses.clear();
    }

    public void clearIncludedInternalClasses() {
        this.includedInternalClasses.clear();
    }

    public void clearRecord() {
        this.clearIncludedInternalClasses();
        this.clearExcludedInternalClasses();
        this.clearIncludedExternalClasses();
        this.clearExcludedExternalClasses();
        this.clearIncludedBoostrapClasses();
        this.clearExcludedBoostrapClasses();
    }

    public boolean isExclusive(final String className) {
        return !this.includedInternalClasses.contains(className);
    }

    public boolean isInternalClass(final String className) {
        return this.includedInternalClasses.contains(className);
    }

    public void updateRecord(final String className, final ClassLoadedType classLoadedType,
        final boolean included) {
        switch (classLoadedType) {
            case INTERNAL:
                if (included) {
                    this.includedInternalClasses.add(className);
                } else {
                    this.excludedInternalClasses.add(className);
                }
                break;
            case EXTERNAL:
                if (included) {
                    this.includedExternalClasses.add(className);
                } else {
                    this.excludedExternalClasses.add(className);
                }
                break;
            case BOOSTRAP:
                if (included) {
                    this.includedBootstrapClasses.add(className);
                } else {
                    this.excludedBootstrapClasses.add(className);
                }
                break;
            default:
                throw new RuntimeException(
                    Log.genMessage("Unexpected class type: " + classLoadedType, this.getClass()));
        }
    }

    public enum ClassLoadedType {
        INTERNAL,
        EXTERNAL,
        BOOSTRAP,
    }
}
