package instrumentation.output;

import instrumentation.instr.instruction.info.SerializableLineInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.BreakPoint;
import model.ClassLocation;
import model.ControlScope;
import model.SourceScope;
import model.trace.Trace;
import model.trace.TraceNode;
import model.value.VarValue;
import utils.FileUtils;

public class TraceOutputReader extends OutputReader {

    private String traceExecFolder;
    private HashMap<Integer, SerializableLineInfo> opcodeTable;

    public TraceOutputReader(InputStream in) {
        super(in);
    }

    public TraceOutputReader(InputStream in, String traceExecFolder,
        HashMap<Integer, SerializableLineInfo> opcodeTable) {
        super(in);
        this.traceExecFolder = traceExecFolder;
        this.opcodeTable = opcodeTable;
    }

    public List<Trace> readTrace() throws IOException {
        int traceNo = readVarInt();
        if (traceNo == 0) {
            return null;
        }

        List<Trace> traceList = new ArrayList<Trace>();
        for (int i = 0; i < traceNo; i++) {
            Trace trace = new Trace(null);
            readString(); // projectName
            readString(); // projectVersion
            readString(); // launchClass
            readString(); // launchMethod
            trace.setMain(readBoolean());
            trace.setThreadName(readString());
            trace.setThreadId(Long.parseLong(readString()));
            trace.setIncludedLibraryClasses(readFilterInfo());
            trace.setExcludedLibraryClasses(readFilterInfo());
            List<BreakPoint> locationList = readLocations();
            trace.setExecutionList(readSteps(trace, locationList));
            readStepVariableRelation(trace);

            traceList.add(trace);
        }

        return traceList;
    }

    private List<String> readFilterInfo() throws IOException {
        boolean inFile = readBoolean();
        if (inFile) {
            if (traceExecFolder == null) {
                throw new IllegalArgumentException("missing define traceExecFolder!");
            }
            String fileName = readString();
            String filePath = FileUtils.getFilePath(traceExecFolder, fileName);
            return utils.FileUtils.readLines(filePath);
        } else {
            return readSerializableList();
        }
    }

    private List<BreakPoint> readLocations() throws IOException {
        int bkpTotal = readVarInt();
        int numOfClasses = readVarInt();
        List<BreakPoint> allLocs = new ArrayList<>(bkpTotal);
        for (int i = 0; i < numOfClasses; i++) {
            int lines = readVarInt();
            if (lines <= 0) {
                continue;
            }
            String declaringCompilationUnitName = readString();
            for (int j = 0; j < lines; j++) {
                BreakPoint loc = readLocation(declaringCompilationUnitName);
                allLocs.add(loc);
            }
        }
        return allLocs;
    }

    private List<TraceNode> readSteps(Trace trace, List<BreakPoint> locationList)
        throws IOException {
        int size = readVarInt();
        List<TraceNode> allSteps = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            TraceNode node = new TraceNode(null, null, i + 1, trace, null);
            allSteps.add(node);
        }
        for (int i = 0; i < size; i++) {
            TraceNode step = allSteps.get(i);
            step.setBreakPoint(locationList.get(readVarInt()));
//			SerializableLineInfo lineInfo = opcodeTable.get(step.getBreakPoint().hashCode());
//			if (lineInfo != null) {
//				step.setInstructions(lineInfo.getInstructions());
//				step.setStackVariables(lineInfo.getLocalVars());
//				step.setConstPool(lineInfo.getConstPool());
//			} else {
//				step.setInstructions(null);
//			}
            step.setTimestamp(readLong());
            TraceNode controlDominator = readNode(allSteps);
            step.setControlDominator(controlDominator);
            if (controlDominator != null) {
                controlDominator.addControlDominatee(step);
            }
            // step_in
            TraceNode stepIn = readNode(allSteps);
            step.setStepInNext(stepIn);
            if (stepIn != null) {
                stepIn.setStepInPrevious(step);
            }
            // step_over
            TraceNode stepOver = readNode(allSteps);
            step.setStepOverNext(stepOver);
            if (stepOver != null) {
                stepOver.setStepOverPrevious(step);
            }
            // invocation_parent
            TraceNode invocationParent = readNode(allSteps);
            step.setInvocationParent(invocationParent);
            if (invocationParent != null) {
                invocationParent.addInvocationChild(step);
            }
            // loop_parent
            TraceNode loopParent = readNode(allSteps);
            step.setLoopParent(loopParent);
            if (loopParent != null) {
                loopParent.addLoopChild(step);
            }
            step.setException(readBoolean());
            step.setBytecode(readString());
            step.addInvokingMethod(readString());
            TraceNode invokeMatchNode = readNode(allSteps);
            step.setInvokingMatchNode(invokeMatchNode);
        }
        readRWVarValues(allSteps, false);
        readRWVarValues(allSteps, true);
        return allSteps;
    }

    protected List<VarValue> readVarValue() throws IOException {
        return readSerializableList();
    }

    private void readRWVarValues(List<TraceNode> allSteps, boolean isWrittenVar)
        throws IOException {
        int i = 0;
        while (i < allSteps.size()) {
            List<List<VarValue>> varsCol = readSerializableList();
            for (List<VarValue> vars : varsCol) {
                if (isWrittenVar) {
                    allSteps.get(i++).setWrittenVariables(vars);
                } else {
                    allSteps.get(i++).setReadVariables(vars);
                }
            }
        }
    }

    private TraceNode readNode(List<TraceNode> allSteps) throws IOException {
        int nodeOrder = readVarInt();
        if (nodeOrder <= 0) {
            return null;
        }
        return allSteps.get(nodeOrder - 1);
    }

    private BreakPoint readLocation(String declaringCompilationUnitName) throws IOException {
        String classCanonicalName = readString();
        String methodSig = readString();
        int lineNo = readVarInt();
        boolean isConditional = readBoolean();
        boolean isBranch = readBoolean();
        boolean isReturnStatement = readBoolean();
        BreakPoint location = new BreakPoint(classCanonicalName, declaringCompilationUnitName,
            methodSig, lineNo);
        location.setConditional(isConditional);
        location.setBranch(isBranch);
        location.setReturnStatement(isReturnStatement);
        location.setControlScope(readControlScope());
        location.setLoopScope(readLoopScope());
        return location;
    }

    private ControlScope readControlScope() throws IOException {
        int rangeSize = readVarInt();
        if (rangeSize == 0) {
            return null;
        }
        ControlScope scope = new ControlScope();
        scope.setLoop(readBoolean());
        for (int i = 0; i < rangeSize; i++) {
            ClassLocation controlLoc = new ClassLocation(readString(), null, readVarInt());
            scope.addLocation(controlLoc);
        }
        return scope;
    }

    private SourceScope readLoopScope() throws IOException {
        int size = readVarInt();
        if (size == 0) {
            return null;
        }
        String className = readString();
        int startLine = readVarInt();
        int endLine = readVarInt();
        SourceScope scope = new SourceScope(className, startLine, endLine);

        return scope;
    }

    private void readStepVariableRelation(Trace trace) throws IOException {
//		Map<String, StepVariableRelationEntry> stepVariableTable = trace.getStepVariableTable();
//		int size = readVarInt();
//		for (int i = 0; i < size; i++) {
//			StepVariableRelationEntry entry = new StepVariableRelationEntry(readString());
//			int producerSize = readVarInt();
//			for (int p = 0; p < producerSize; p++) {
//				entry.addProducer(readNode(trace.getExecutionList()));
//				readVarInt();
//			}
//			int consumerSize = readVarInt();
//			for (int p = 0; p < consumerSize; p++) {
//				entry.addConsumer(readNode(trace.getExecutionList()));
//				readVarInt();
//			}
//			stepVariableTable.put(entry.getVarID(), entry);
//		}
    }


}
