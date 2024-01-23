package org.cophi.javatracer.configs;

import org.cophi.javatracer.exceptions.MissingParameterException;
import org.cophi.javatracer.instrumentation.agents.AgentParameters;

public class EntryPoint implements AgentParameters {

    public static final String MAIN_METHOD_NAME = "name";
    public static final String MAIN_METHOD_Signature = "([Ljava/lang/String;)V";
    public static final String CLASS_NAME_KEY = "entry_point_class_name";
    public static final String METHOD_NAME_KEY = "entry_point_method_name";
    public static final String METHOD_SIGNATURE_KEY = "entry_point_method_descriptor";

    protected String className;
    protected String methodName;
    protected String methodSignature;

    protected EntryPoint() {
        this.className = null;
        this.methodName = null;
        this.methodSignature = null;
    }

    public EntryPoint(final String className, final String methodName,
        final String methodDescriptor) {
        this.className = className;
        this.methodName = methodName;
        this.methodSignature = methodDescriptor;
    }

    public EntryPoint(final String className) {
        this(className, EntryPoint.MAIN_METHOD_NAME, EntryPoint.MAIN_METHOD_Signature);
    }

    public static EntryPoint parseParameter(final JavaTracerAgentParameters parameters) {
        try {
            EntryPoint entryPoint = new EntryPoint();
            entryPoint.update(parameters);
            return entryPoint;
        } catch (MissingParameterException e) {
            return null;
        }
    }

    @Override
    public JavaTracerAgentParameters genParameters() {
        JavaTracerAgentParameters parameters = new JavaTracerAgentParameters();
        parameters.setParameter(EntryPoint.CLASS_NAME_KEY, this.className);
        parameters.setParameter(EntryPoint.MAIN_METHOD_NAME, this.methodName);
        parameters.setParameter(EntryPoint.MAIN_METHOD_Signature, this.methodSignature);
        return parameters;
    }

    @Override
    public void update(JavaTracerAgentParameters parameters) {
        this.setClassName(parameters.getParameter(EntryPoint.CLASS_NAME_KEY));
        this.setMethodName(parameters.getParameter(EntryPoint.METHOD_NAME_KEY));
        this.setMethodSignature(parameters.getParameter(EntryPoint.METHOD_SIGNATURE_KEY));
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    public boolean matchMethod(final String methodName, final String methodDescriptor) {
        return this.methodName.equals(methodName) && (this.methodSignature == null
            || this.methodName.equals(methodDescriptor));
    }
}
