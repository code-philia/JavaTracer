package org.cophi.javatracer.recommendation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;
import org.cophi.javatracer.codeanalysis.bytecode.CallGraph;
import org.cophi.javatracer.codeanalysis.bytecode.MethodNode;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.model.location.BreakPoint;
import org.cophi.javatracer.model.trace.Trace;
import org.cophi.javatracer.model.trace.TraceNode;
import org.cophi.javatracer.model.variables.ArrayElementVar;
import org.cophi.javatracer.model.variables.FieldVar;
import org.cophi.javatracer.model.variables.LocalVar;
import org.cophi.javatracer.model.variables.VarValue;
import org.cophi.javatracer.model.variables.Variable;


public class SeedStatementFinder {

    private CallGraph graph;

    public Map<MethodNode, List<InstructionHandle>> findSeedStatemets(VarValue specificVar,
        TraceNode start, TraceNode end) {
        Trace trace = start.getTrace();
        Variable var = specificVar.getVariable();
        if (var instanceof FieldVar || var instanceof ArrayElementVar) {
            Map<MethodNode, List<InstructionHandle>> seeds = matchVarWithCallGraph(trace,
                specificVar);
            return seeds;
        } else if (var instanceof LocalVar) {
            Map<MethodNode, List<InstructionHandle>> seeds = matchVarLocally(start, specificVar);
            return seeds;
        }

        return null;

    }

    private void createOrAppend(MethodNode node, List<InstructionHandle> defs,
        Map<MethodNode, List<InstructionHandle>> allSeedMethods) {
        if (!allSeedMethods.containsKey(node)) {
            allSeedMethods.put(node, defs);
        } else {
            List<InstructionHandle> existingDefs = allSeedMethods.get(node);
            for (InstructionHandle def : defs) {
                if (!existingDefs.contains(def)) {
                    existingDefs.add(def);
                }
            }
            allSeedMethods.put(node, existingDefs);
        }
    }

    private Map<MethodNode, List<InstructionHandle>> findAllSeedMethods(
        List<MethodNode> seedMethods, Set<MethodNode> executedAppMethods, Variable var) {
        Map<MethodNode, List<InstructionHandle>> allSeedMethods = new HashMap<>();
        for (MethodNode node : seedMethods) {
            List<InstructionHandle> defs = node.findVariableDefinition(var);
            createOrAppend(node, defs, allSeedMethods);

            System.currentTimeMillis();
            Map<MethodNode, List<InstructionHandle>> allCallers = node.getAllCallers();
            for (MethodNode caller : allCallers.keySet()) {
                List<InstructionHandle> hList = allCallers.get(caller);
//				allSeedMethods.put(caller, hList);
                createOrAppend(caller, hList, allSeedMethods);
            }
        }

//		Iterator<MethodNode> iter = allSeedMethods.keySet().iterator();
//		while(iter.hasNext()){
//			MethodNode node = iter.next();
//			if(!executedAppMethods.contains(node)){
//				iter.remove();
//			}
//		}

        return allSeedMethods;
    }

    private Map<MethodNode, List<InstructionHandle>> matchVarLocally(TraceNode start,
        VarValue specificVar) {
        Map<MethodNode, List<InstructionHandle>> map = new HashMap<>();

        Trace trace = start.getTrace();
        Method method = new CallGraph(trace.getProjectConfig(),
            trace.getIncludedLibraryClasses()).
            findByteCodeMethod(start.getBreakPoint());

        MethodNode methodNode = new MethodNode(trace.getProjectConfig().getClassLoader(),
            start.getMethodSign(), method);
        List<InstructionHandle> list = methodNode.findVariableDefinition(specificVar.getVariable());

        map.put(methodNode, list);

        return map;
    }

    private Map<MethodNode, List<InstructionHandle>> matchVarWithCallGraph(Trace trace,
        VarValue specificVar) {
        ProjectConfig appClassPath = trace.getProjectConfig();

        List<BreakPoint> collectedBreakPoints = trace.allLocations();
        Set<MethodNode> executedAppMethods = new HashSet<>();

        long t1 = System.currentTimeMillis();
        graph = new CallGraph(appClassPath, trace.getIncludedLibraryClasses());

        for (BreakPoint location : collectedBreakPoints) {
            MethodNode node = graph.findOrCreateMethodNode(location);
            executedAppMethods.add(node);
        }

        long t2 = System.currentTimeMillis();
        System.out.println("Time for building call graph: " + (t2 - t1));

        List<MethodNode> seeds = matchVariableDefinition(graph, specificVar.getVariable());
        Map<MethodNode, List<InstructionHandle>> allSeeds =
            findAllSeedMethods(seeds, executedAppMethods, specificVar.getVariable());

//		System.currentTimeMillis();
        return allSeeds;
    }

    private List<MethodNode> matchVariableDefinition(CallGraph callGraph, Variable var) {
        List<MethodNode> list = new ArrayList<>();
        for (String methodSign : callGraph.getMethodMaps().keySet()) {
            MethodNode method = callGraph.getMethodMaps().get(methodSign);
            if (method.getMethod().getCode() != null) {
                List<InstructionHandle> defs = method.findVariableDefinition(var);
                if (!defs.isEmpty()) {
                    list.add(method);
                }
            }

        }
        return list;
    }

}
