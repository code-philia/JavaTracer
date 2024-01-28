package org.cophi.javatracer.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.List;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;
import org.cophi.javatracer.configs.ProjectConfig;

public class InstructionVisitor extends ByteCodeVisitor {

    private String methodSignature;
    private ProjectConfig projectConfig;
    private List<InstructionHandle> instructionList = new ArrayList<>();
    private Method method;

    public InstructionVisitor(String methodSignature, ProjectConfig projectConfig) {
        super();
        this.methodSignature = methodSignature;
        this.setProjectConfig(projectConfig);
    }

    public List<InstructionHandle> getInstructionList() {
        return instructionList;
    }

    public void setInstructionList(List<InstructionHandle> instructionList) {
        this.instructionList = instructionList;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    public void setProjectConfig(ProjectConfig appJavaClassPath) {
        this.projectConfig = appJavaClassPath;
    }

    public void visitMethod(Method method) {
        Code code = method.getCode();
        if (code == null) {
            return;
        }

        if (isIntrestedMethod(method, methodSignature)) {
            List<InstructionHandle> insHandles = findCorrespondingInstructions(code);
            this.setInstructionList(insHandles);
            this.setMethod(method);
        }

    }

    private boolean isIntrestedMethod(Method method, String methodSignature2) {
        String ms = method.getSignature();
        String methodName = method.getName();
        String methodSig = methodName + ms;

        String comparedMethodSig = methodSignature2.substring(methodSignature2.indexOf("#") + 1);
        return methodSig.equals(comparedMethodSig);
    }
}
