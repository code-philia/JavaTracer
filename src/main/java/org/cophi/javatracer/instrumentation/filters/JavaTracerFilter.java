package org.cophi.javatracer.instrumentation.filters;

import groovy.lang.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.bcel.classfile.Method;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.instrumentation.agents.LoadedClassRecord;
import org.cophi.javatracer.instrumentation.agents.LoadedClassRecord.ClassLoadedType;
import org.cophi.javatracer.instrumentation.instrumentator.instructionInfo.LineInstructionInfo;
import org.cophi.javatracer.utils.NamingUtils;

/**
 * This class is used to filter out the class that do not need to be instrumented.
 *
 * @author WYK, Lin Yun
 */
@Singleton
public class JavaTracerFilter {

    /**
     * Singleton instance
     */
    private static volatile JavaTracerFilter instance = null;
    /**
     * The set of folders that contains the class files of the target project. <br/> For example, if
     * the target project is Maven project, then this list should contain the absolute path of
     * "target/classes" and "target/test-classes" folders.
     */
    protected Set<String> binFolders = new HashSet<>();

    /**
     * We use the regular expression to match the class names for included external class. <br/>
     */
    protected WildcardMatcher includedClassesMatcher = null;
    /**
     * We use the regular expression to match the class names for excluded external class. <br/>
     */
    protected WildcardMatcher excludedClassesMatcher = null;

    protected List<UserFilter> userFilter = new ArrayList<>();

    private JavaTracerFilter() {
    }

    public static JavaTracerFilter getInstance() {
        synchronized (JavaTracerFilter.class) {
            if (instance == null) {
                instance = new JavaTracerFilter();
            }
        }
        return instance;
    }

    public void filterInstructions(final List<LineInstructionInfo> instructions,
        final String className,
        final Method method) {
        for (UserFilter filter : this.userFilter) {
            filter.filterInstructions(instructions, className, method);
        }
    }

    public void initialize(final ProjectConfig projectConfig) {
        final Path projectRootDir = Paths.get(projectConfig.getProjectRootPath());
        for (String classPathStr : projectConfig.getClasspaths()) {
            Path classPath = Paths.get(classPathStr);
            if (classPath.startsWith(projectRootDir)) {
                File binFolder = classPath.toFile();
                if (binFolder.exists() && binFolder.isDirectory()) {
                    this.binFolders.add(binFolder.toString());
                }
            }
        }
        this.includedClassesMatcher = new WildcardMatcher(
            projectConfig.getIncludedClassNames());
        this.excludedClassesMatcher = new WildcardMatcher(
            projectConfig.getExcludedClassNames());
    }

    public boolean isExcludedByUser(final String className) {
        return this.excludedClassesMatcher.matches(className);
    }

    public boolean isIncludedByUser(final String className) {
        return this.includedClassesMatcher.matches(className);
    }

    /**
     * Only the class pass the filter will be instrumented<br/>
     * <ol>
     *     <li>JDF classes stated in {@code JDKFilter} will not pass the filter, even user specify it</li>
     *     <li>Internal classes will pass the filter unless user specify</li>
     *     <li>External classes will not pass the filter unless user specify</li>
     *     <li>Boostrap classes will not pass the filter unless user specify</li>
     *     <li>Class pass the user filter</li>
     * </ol>
     *
     * @param className  Target class canonical name. E.g. {@code org.example.Class}.
     * @param sourcePath Source code path of the class. If it is internal class, it should be a
     *                   folder containing target {@code .class} file. If it is external class, it
     *                   should be path of the {@code .jar} file. If it is boostrap class, then it
     *                   should be null.
     * @return {@code True} if the class pass the filter. Otherwise, {@code False}.
     * @see JDKFilter
     * @see ProjectConfig#getExcludedClassNames
     * @see ProjectConfig#getIncludedClassNames
     */
    public boolean isInstrumentableClass(final String className, final String sourcePath) {
        if (JDKFilter.contains(className)) {
            this.updateRecord(className, ClassLoadedType.EXTERNAL, false);
            return false;
        }

        for (UserFilter filter : this.userFilter) {
            if (!filter.isInstrumentableClass(className)) {
                this.updateRecord(className, ClassLoadedType.EXTERNAL, false);
                return false;
            }
        }

        ClassLoadedType classLoadedType = this.detectClassType(sourcePath);
        if (this.isIncludedByUser(className)) {
            this.updateRecord(className, classLoadedType, true);
            return true;
        }

        if (this.isExcludedByUser(className)) {
            this.updateRecord(className, classLoadedType, false);
            return false;
        }

        if (classLoadedType == ClassLoadedType.BOOSTRAP
            || classLoadedType == ClassLoadedType.EXTERNAL) {
            this.updateRecord(className, classLoadedType, false);
            return false;
        } else {
            this.updateRecord(className, classLoadedType, true);
            return true;
        }
    }

    public boolean isInstrumentableMethod(final String className, final Method method) {
        for (UserFilter filter : this.userFilter) {
            if (!filter.isInstrumentableMethod(className, method)) {
                return false;
            }
        }
        return true;
    }

    protected ClassLoadedType detectClassType(final String sourcePath) {
        if (sourcePath == null) {
            return ClassLoadedType.BOOSTRAP;
        }
        if (this.binFolders.contains(sourcePath)) {
            return ClassLoadedType.INTERNAL;
        } else {
            return ClassLoadedType.EXTERNAL;
        }
    }

    protected boolean isInternalClass(final String classPath) {
        return this.binFolders.contains(classPath);
    }

    protected boolean matchExternalIncludes(final String classFileName, boolean match) {
        final String className = NamingUtils.classBinaryNameToCanonicalName(classFileName);
        if (!match && (this.includedClassesMatcher != null)) {
            match = this.includedClassesMatcher.matches(className);
        }
        if (this.excludedClassesMatcher != null) {
            match &= !this.excludedClassesMatcher.matches(className);
        }
        return match;
    }

    protected void updateRecord(final String className, final ClassLoadedType classLoadedType,
        final boolean included) {
        LoadedClassRecord.getInstance().updateRecord(className, classLoadedType, included);
    }
}
