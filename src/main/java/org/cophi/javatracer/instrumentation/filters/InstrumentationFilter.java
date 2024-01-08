package org.cophi.javatracer.instrumentation.filters;

import groovy.lang.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.utils.ClassNameUtils;

/**
 * This class is used to filter out the class that do not need to be instrumented.
 *
 * @author WYK, Lin Yun
 */
@Singleton
public class InstrumentationFilter {

    /**
     * Singleton instance
     */
    private static volatile InstrumentationFilter instance = null;
    /**
     * The set of folders that contains the class files of the target project. <br/> For example, if
     * the target project is Maven project, then this list should contain the absolute path of
     * "target/classes" and "target/test-classes" folders.
     */
    protected Set<String> binFolders = new HashSet<>();
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
    /**
     * We use the regular expression to match the class names for included external class. <br/>
     */
    protected WildcardMatcher includedLibraryClassesMatcher = null;
    /**
     * We use the regular expression to match the class names for excluded external class. <br/>
     */
    protected WildcardMatcher excludedLibraryClassesMatcher = null;

    private InstrumentationFilter() {
    }

    public static InstrumentationFilter getInstance() {
        synchronized (InstrumentationFilter.class) {
            if (instance == null) {
                instance = new InstrumentationFilter();
            }
        }
        return instance;
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
        ;
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

    public void initialize(final ProjectConfig projectConfig) {
        this.clearRecord();
        final Path projectRootDir = Paths.get(projectConfig.getProjectRootPath());
        for (String classPathStr : projectConfig.getClassPaths()) {
            Path classPath = Paths.get(classPathStr);
            if (classPath.startsWith(projectRootDir)) {
                File binFolder = classPath.toFile();
                if (binFolder.exists() && binFolder.isDirectory()) {
                    this.binFolders.add(binFolder.toString());
                }
            }
        }
        this.includedLibraryClassesMatcher = new WildcardMatcher(
            projectConfig.getIncludedClassNames());
        this.excludedLibraryClassesMatcher = new WildcardMatcher(
            projectConfig.getExcludedClassNames());
    }

    public boolean isExcludedByUser(final String className) {
        return this.excludedLibraryClassesMatcher.matches(className);
    }

    public boolean isIncludedByUser(final String className) {
        return this.includedLibraryClassesMatcher.matches(className);
    }

    /**
     * Only the class pass the filter will be instrumented<br/>
     * <ol>
     *     <li>JDF classes stated in {@code JDKFilter} will not pass the filter, even user specify it</li>
     *     <li>Internal classes will pass the filter unless user specify</li>
     *     <li>External classes will not pass the filter unless user specify</li>
     *     <li>Boostrap classes will not pass the filter unless user specify</li>
     * </ol>
     *
     * @param classFileName Target class file name. E.g. {@code org/example/Class}.
     * @param sourcePath    Source code path of the class. If it is internal class, it should be a
     *                      folder containing target {@code .class} file. If it is external class,
     *                      it should be path of the {@code .jar} file. If it is boostrap class,
     *                      then it should be null.
     * @return {@code True} if the class pass the filter. Otherwise, {@code False}.
     * @see JDKFilter
     * @see ProjectConfig#getExcludedClassNames
     * @see ProjectConfig#getIncludedClassNames
     */
    public boolean pass(final String classFileName, final String sourcePath) {
        final String className = ClassNameUtils.classFileNameToCanonicalName(classFileName);
        if (!JDKFilter.contains(ClassNameUtils.classFileNameToCanonicalName(classFileName))) {
            this.updateRecord(className, ClassType.EXTERNAL, false);
            return false;
        }

        ClassType classType = this.detectClassType(sourcePath);

        if (this.isIncludedByUser(className)) {
            this.updateRecord(className, classType, true);
            return true;
        }

        if (this.isExcludedByUser(className)) {
            this.updateRecord(className, classType, false);
            return false;
        }

        if (classType == ClassType.BOOSTRAP || classType == ClassType.EXTERNAL) {
            this.updateRecord(className, classType, false);
            return false;
        } else {
            this.updateRecord(className, classType, true);
            return true;
        }
    }

    protected ClassType detectClassType(final String sourcePath) {
        if (sourcePath == null) {
            return ClassType.BOOSTRAP;
        }
        if (this.binFolders.contains(sourcePath)) {
            return ClassType.INTERNAL;
        } else {
            return ClassType.EXTERNAL;
        }
    }

    protected boolean isInternalClass(final String classPath) {
        return this.binFolders.contains(classPath);
    }

    protected boolean matchExternalIncludes(final String classFileName, boolean match) {
        final String className = ClassNameUtils.classFileNameToCanonicalName(classFileName);
        if (!match && (this.includedLibraryClassesMatcher != null)) {
            match = this.includedLibraryClassesMatcher.matches(className);
        }
        if (this.excludedLibraryClassesMatcher != null) {
            match &= !this.excludedLibraryClassesMatcher.matches(className);
        }
        return match;
    }

    protected void updateRecord(final String className, final ClassType classType,
        final boolean included) {
        switch (classType) {
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
                    Log.genMessage("Unexpected class type: " + classType, this.getClass()));
        }
    }

    protected enum ClassType {
        INTERNAL,
        EXTERNAL,
        BOOSTRAP,
    }
}
