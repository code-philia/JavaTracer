package org.cophi.javatracer.model.location;

import java.util.Objects;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.utils.NamingUtils;

/**
 * ClassLocation represent a location in source code file
 */
public class ClassLocation implements Cloneable {

    public static final String UNKNOWN_METHOD_SIGN = "unknown";
    /**
     * Name of compilation unit that this location belong to
     */
    protected final String declaringCompilationUnitName;
    /**
     * Name of this class <br/> Canonical name is used (e.g. org.example.Foo)
     */
    protected String className;
    /**
     * Method signature
     */
    protected String methodSignature;
    /**
     * Line number of location
     */
    protected int lineNumber;
    /**
     * Location id
     */
    protected String id;
    /**
     * Path to the source code file
     */
    private String sourceCodePath;

    /**
     * Constructor
     *
     * @param className       Canonical name of class
     * @param methodSignature Method signature
     * @param lineNumber      Line number
     * @param sourceCodePath  Path to source code file
     */
    public ClassLocation(String className, String methodSignature, int lineNumber,
        String sourceCodePath) {
        Objects.requireNonNull(className,
            Log.genMessage("Class name cannot be null", this.getClassCanonicalName()));
        Objects.requireNonNull(methodSignature,
            Log.genMessage("Method signature cannot be null", this.getClassCanonicalName()));
        if (lineNumber < 0) {
            throw new IllegalArgumentException(
                Log.genMessage("Line number cannot be negative", this.getClassCanonicalName()));
        }
        this.className = className;
        this.methodSignature = methodSignature;
        this.lineNumber = lineNumber;
        this.id = NamingUtils.genMethodId(className, methodSignature);
        this.declaringCompilationUnitName = NamingUtils.getCompilationUnit(className);
        this.sourceCodePath = sourceCodePath;
    }

    public ClassLocation(final String className, final String methodSignature,
        final int lineNumber) {
        this(className, methodSignature, lineNumber, null);
    }

    /**
     * Generate location id
     *
     * @param className  Canonical name of class
     * @param lineNumber Line number
     * @return Location id
     */
    public static String genId(final String className, final int lineNumber) {
        return String.format("%s:%s", className, lineNumber);
    }

    /**
     * Get class name.
     *
     * @return Class name
     */
    public String getClassCanonicalName() {
        return className;
    }

    /**
     * Get declaring compilation unit name
     *
     * @return Declaring compilation unit name
     */
    public String getDeclaringCompilationUnitName() {
        return this.declaringCompilationUnitName;
    }

    /**
     * Get line number
     *
     * @return Line number
     */
    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getMethodName() {
        if (methodSignature == null) {
            return null;
        }

        if (methodSignature.contains("#")) {
            return methodSignature.substring(methodSignature.indexOf("#") + 1,
                methodSignature.indexOf("("));
        }

        return methodSignature;
    }

    public String getMethodSign() {
        return methodSignature;
    }

    public void setMethodSign(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    /**
     * Get path to source code file
     *
     * @return Path to source code file
     */
    public String getSourceCodePath() {
        return this.sourceCodePath;
    }

    /**
     * Set path to source code file
     *
     * @param sourceCodePath Path to source code file
     */
    public void setSourceCodePath(String sourceCodePath) {
        this.sourceCodePath = sourceCodePath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return className.hashCode() * prime + lineNumber;
    }

    @Override
    public boolean equals(Object otherObj) {
        if (!(otherObj instanceof ClassLocation otherClassLocation)) {
            return false;
        }

        return this.className.equals(otherClassLocation.className)
            && this.lineNumber == otherClassLocation.lineNumber;
    }

    @Override
    public Object clone() {
        ClassLocation location = new ClassLocation(className, methodSignature, lineNumber);
        location.sourceCodePath = this.sourceCodePath;
        return location;
    }

    @Override
    public String toString() {
        return this.className + ", line=" + this.lineNumber;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setId(String id) {
        this.id = id;
    }
}
