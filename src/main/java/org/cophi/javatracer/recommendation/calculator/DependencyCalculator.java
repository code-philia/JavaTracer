package org.cophi.javatracer.recommendation.calculator;

import java.util.ArrayList;
import java.util.List;
import org.apache.bcel.classfile.Method;
import org.cophi.javatracer.codeanalysis.bytecode.ByteCodeParser;
import org.cophi.javatracer.codeanalysis.bytecode.CFG;
import org.cophi.javatracer.codeanalysis.bytecode.CFGConstructor;
import org.cophi.javatracer.codeanalysis.bytecode.CFGNode;
import org.cophi.javatracer.codeanalysis.bytecode.MethodFinderBySignature;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.model.location.BreakPoint;

public class DependencyCalculator {

    private ProjectConfig projectConfig;

    public DependencyCalculator(ProjectConfig appJavaClassPath) {
        this.projectConfig = appJavaClassPath;
    }

    public Dependency calculateDependency(BreakPoint testPoint, BreakPoint avoidPoint) {
        String methodSign = testPoint.getMethodSign();
        String sign = methodSign.substring(methodSign.indexOf("#") + 1, methodSign.length());
        MethodFinderBySignature finder = new MethodFinderBySignature(sign);
        ByteCodeParser.parse(testPoint.getClassCanonicalName(), finder, projectConfig);
        Method method = finder.getMethod();

        CFGConstructor constructor = new CFGConstructor();
        CFG cfg = constructor.buildCFGWithControlDomiance(method.getCode());

        List<CFGNode> beforeList = getBeforeList(cfg, testPoint);
        List<CFGNode> afterList = getAfterList(cfg, testPoint, avoidPoint);

        int controlDependency = 0;
        for (CFGNode beforeNode : beforeList) {
            for (CFGNode afterNode : afterList) {
                if (beforeNode.getControlDependentees().contains(afterNode)) {
                    controlDependency++;
                }
            }
        }

        constructor.constructDataDependency(cfg);

        int dataDependency = 0;
        for (CFGNode beforeNode : beforeList) {
            for (CFGNode afterNode : afterList) {
                if (beforeNode.getUseSet().contains(afterNode)) {
                    dataDependency++;
                }
            }
        }

        return new Dependency(dataDependency, controlDependency);

    }

    private List<CFGNode> getAfterList(CFG cfg, BreakPoint testPoint, BreakPoint avoidPoint) {

        int upperBound = avoidPoint.getLineNumber();

        if (!testPoint.getMethodSign().equals(avoidPoint.getMethodSign())) {
            upperBound = cfg.getEndLine();
        }

        List<CFGNode> list = new ArrayList<>();
        for (CFGNode node : cfg.getNodeList()) {
            int line = cfg.getLineNumber(node);
            if (line >= testPoint.getLineNumber() &&
                line <= upperBound) {
                list.add(node);
            }
        }

        return list;
    }

    private List<CFGNode> getBeforeList(CFG cfg, BreakPoint testPoint) {
        List<CFGNode> list = new ArrayList<>();
        for (CFGNode node : cfg.getNodeList()) {
            if (cfg.getLineNumber(node) < testPoint.getLineNumber()) {
                list.add(node);
            }
        }

        return list;
    }
}
