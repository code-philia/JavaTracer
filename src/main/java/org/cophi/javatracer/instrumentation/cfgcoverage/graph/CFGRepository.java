package org.cophi.javatracer.instrumentation.cfgcoverage.graph;

import org.cophi.javatracer.codeanalysis.bytecode.ByteCodeMethodFinder;
import org.cophi.javatracer.codeanalysis.bytecode.ByteCodeParser;
import org.cophi.javatracer.codeanalysis.bytecode.CFG;
import org.cophi.javatracer.codeanalysis.bytecode.CFGConstructor;
import org.cophi.javatracer.codeanalysis.bytecode.CFGNode;
import org.cophi.javatracer.codeanalysis.bytecode.MethodFinderByLine;
import org.cophi.javatracer.instrumentation.AgentLogger;
import org.cophi.javatracer.instrumentation.cfgcoverage.InstrumentationUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.cophi.javatracer.model.ClassLocation;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Method;
import org.cophi.javatracer.configs.AppJavaClassPath;
import org.cophi.javatracer.utils.ClassUtils;
import org.cophi.javatracer.utils.SignatureUtils;

public class CFGRepository {

    public void clearCache() {
        Repository.clearCache();
    }

    public CFGInstance createCfgInstance(ClassLocation methodLocation,
        AppJavaClassPath appJavaClassPath) {
        return findCfg(methodLocation, appJavaClassPath);
//		CFGInstance cfgInstance = findCfg(methodLocation, appJavaClassPath);
//		CFG cfg = cfgInstance.getCfg();
//		CFG cloneCfg = new CFG();
//		cloneCfg.setStartNode(cfg.getStartNode());
//		cloneCfg.setMethod(cfg.getMethod());
//		for (CFGNode exitNode : cfg.getExitList()) {
//			cloneCfg.addExitNode(exitNode);
//		}
//		for (CFGNode node : cfg.getNodeList()) {
//			cloneCfg.addNode(node);
//		}
//		return new CFGInstance(cloneCfg, cfgInstance.getNodeList(), cfgInstance.getUnitCfgNodeIds());
    }

    public CFGInstance findCfg(ClassLocation methodLocation, AppJavaClassPath appJavaClassPath) {
        String methodId = InstrumentationUtils.getMethodId(methodLocation.getClassCanonicalName(),
            methodLocation.getMethodSign());
        ByteCodeMethodFinder finder;
        if (methodLocation.getLineNumber() >= 0) {
            finder = new MethodFinderByLine(methodLocation);
        } else {
            finder = new MethodFinderByMethodSignature(methodLocation);
        }
        ByteCodeParser.parse(methodLocation.getClassCanonicalName(), finder, appJavaClassPath);
        Method method = finder.getMethod();
        if (method == null || method.isAbstract() || (method.getCode() == null)) {
            AgentLogger.debug(String.format("Cannot find method: %s", methodId));
            return new CFGInstance(null, methodId, Collections.emptyList());
        }
        CFGConstructor cfgConstructor = new CFGConstructor();
        CFG cfg = cfgConstructor.constructCFG(method.getCode());
        cfg.setMethod(method);
        List<CFGNode> nodeList = new ArrayList<CFGNode>(cfg.getNodeList());
        Collections.sort(nodeList, new Comparator<CFGNode>() {

            @Override
            public int compare(CFGNode o1, CFGNode o2) {
                return Integer.compare(o1.getIdx(), o2.getIdx());
            }

        });
        /* fill up line number info */
        for (CFGNode node : nodeList) {
            node.setLineNo(method.getLineNumberTable()
                .getSourceLine(node.getInstructionHandle().getPosition()));
        }

        methodId = InstrumentationUtils.getMethodId(methodLocation.getClassCanonicalName(), method);

        return new CFGInstance(cfg, methodId, nodeList);
    }

    private static class MethodFinderByMethodSignature extends ByteCodeMethodFinder {

        private final List<String> methodNames;
        private final String methodSign;

        public MethodFinderByMethodSignature(ClassLocation loc) {
            String methodName = SignatureUtils.extractMethodName(loc.getMethodSign());
            if (methodName.equals(ClassUtils.getSimpleName(loc.getClassCanonicalName()))) {
                this.methodNames = Arrays.asList("<init>", "<clinit>");
            } else {
                this.methodNames = List.of(methodName);
            }
            this.methodSign = SignatureUtils.extractSignature(loc.getMethodSign());
        }

        public void visitMethod(Method method) {
            if (this.methodNames.contains(method.getName()) && this.methodSign.equals(
                method.getSignature())) {
                setMethod(method);
            }
        }
    }

}
