package org.cophi.javatracer.model.trace;

import java.util.ArrayList;
import java.util.List;
import org.cophi.javatracer.model.variables.VarValue;
import org.cophi.javatracer.model.variables.Variable;
import org.cophi.javatracer.utils.Settings;


public class PathInstance {

    private TraceNode startNode;
    private TraceNode endNode;
    private ArrayList<SourceLine> lineTrace;
    private ArrayList<SourceLine> patternTrace;

    public PathInstance() {
    }

    public PathInstance(TraceNode node1, TraceNode node2) {

        if (node1.getOrder() < node2.getOrder()) {
            this.startNode = node1;
            this.endNode = node2;
        } else {
            this.startNode = node2;
            this.endNode = node1;
        }

        this.setLineTrace(generateSourceLineTrace());
        this.setPatternTrace(reducePattern(lineTrace));
    }

    /**
     * Find the variable which causes the jump of label path of the pattern.
     */
    public Variable findCausingVar() {
        Variable causingVariable = null;
        TraceNode producer = getStartNode();
        for (VarValue readVar : getEndNode().getReadVariables()) {
            for (VarValue writtenVar : producer.getWrittenVariables()) {
                if (writtenVar.getVarID().equals(readVar.getVarID())) {
                    causingVariable = readVar.getVariable();
                    break;
                }
            }
        }

        return causingVariable;
    }

    public TraceNode getEndNode() {
        return endNode;
    }

    public void setEndNode(TraceNode endNode) {
        this.endNode = endNode;
    }

    public ArrayList<SourceLine> getLineTrace() {
        return lineTrace;
    }

    public void setLineTrace(ArrayList<SourceLine> lineTrace) {
        this.lineTrace = lineTrace;
    }

    public String getPatternKey() {
        StringBuffer buffer = new StringBuffer();
        for (SourceLine line : this.patternTrace) {
            buffer.append(line.toString());
            buffer.append(";");
        }

        return buffer.toString();
    }

    public ArrayList<SourceLine> getPatternTrace() {
        return patternTrace;
    }

    public void setPatternTrace(ArrayList<SourceLine> patternTrace) {
        this.patternTrace = patternTrace;
    }

    public TraceNode getStartNode() {
        return startNode;
    }

    public void setStartNode(TraceNode startNode) {
        this.startNode = startNode;
    }

    public boolean isPotentialCorrect() {
        if (startNode.getReadVarCorrectness(Settings.interestedVariables, false)
            == TraceNode.READ_VARS_CORRECT &&
            endNode.getReadVarCorrectness(Settings.interestedVariables, false)
                == TraceNode.READ_VARS_CORRECT) {
            return true;
        } else if (startNode.getReadVarCorrectness(Settings.interestedVariables, false)
            != TraceNode.READ_VARS_CORRECT &&
            endNode.getReadVarCorrectness(Settings.interestedVariables, false)
                != TraceNode.READ_VARS_CORRECT) {
            return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<SourceLine> reducePattern(ArrayList<SourceLine> instanceLineTrace) {
        ArrayList<SourceLine> pattern = (ArrayList<SourceLine>) instanceLineTrace.clone();
        while (!containsNoDuplication(pattern)) {
            List<List<SourceLine>> ranges = divideRange(pattern);
            boolean isMergeHappen = false;

            List<List<SourceLine>> toBeRemovedRangeList = new ArrayList<>();
            List<SourceLine> deheadSourceLineList = new ArrayList<>();

            /**
             * mark which range should be removed and which source line should be
             * deheaded (mark it as no a loop head).
             */
            for (int i = 0; i < ranges.size() - 1; i++) {
                List<SourceLine> range = ranges.get(i);
                List<SourceLine> nextRange = ranges.get(i + 1);

                if (rangeEqual(range, nextRange)) {
                    toBeRemovedRangeList.add(nextRange);
                    deheadSourceLineList.add(range.get(0));
                    isMergeHappen = true;
                }
            }

            for (List<SourceLine> toBeRemovedRange : toBeRemovedRangeList) {
                ranges.remove(toBeRemovedRange);
            }

            pattern.clear();
            for (List<SourceLine> range : ranges) {
                pattern.addAll(range);
            }

            /**
             * If the source line of a step is deheaded (set isLoopHead as false), then all the steps
             * on the same source line should be deheaded as well.
             *
             * In addition, if the merge does not happen, we should dehead all the steps so that the
             * loop process is done (i.e., no loop head exists in the path).
             */
            for (SourceLine line : pattern) {
                boolean isDeheadLine = false;
                for (SourceLine deheadSourceLine : deheadSourceLineList) {
                    if (line.equals(deheadSourceLine)) {
                        isDeheadLine = true;
                        break;
                    }
                }

                if (isDeheadLine || !isMergeHappen) {
                    line.setLoopHead(false);
                }
            }


        }
        System.currentTimeMillis();
        return pattern;
    }

    private boolean containsNoDuplication(
        ArrayList<SourceLine> instanceLineTrace) {
        for (SourceLine line : instanceLineTrace) {
            if (line.isLoopHead()) {
                return false;
            }
        }

        return true;
    }

    private List<List<SourceLine>> divideRange(ArrayList<SourceLine> pattern) {
        List<List<SourceLine>> ranges = new ArrayList<>();

        int startIndex = 0;
        for (int i = 1; i < pattern.size(); i++) {
            SourceLine line = pattern.get(i);
            if (line.isLoopHead) {
                List<SourceLine> range = new ArrayList<>();
                for (int j = startIndex; j < i; j++) {
                    range.add(pattern.get(j));
                }
                ranges.add(range);
                startIndex = i;
            }
        }

        List<SourceLine> range = new ArrayList<>();
        for (int j = startIndex; j < pattern.size(); j++) {
            range.add(pattern.get(j));
        }
        ranges.add(range);

        return ranges;
    }

    private ArrayList<SourceLine> generateSourceLineTrace() {
        TraceNode node = startNode;
        ArrayList<SourceLine> lineTrace = new ArrayList<>();
        while (node.getOrder() <= endNode.getOrder()) {
            SourceLine sourceLine = new SourceLine(node.getClassCanonicalName(),
                node.getLineNumber(), node.isLoopCondition());
            lineTrace.add(sourceLine);

            node = node.getStepInNext();
            if (node == null) {
                break;
            }
        }
        return lineTrace;
    }

    private boolean rangeEqual(List<SourceLine> range, List<SourceLine> nextRange) {
        if (range.size() == nextRange.size()) {
            for (int i = 0; i < range.size(); i++) {
                SourceLine thisLine = range.get(i);
                SourceLine thatLine = range.get(i);

                if (!thisLine.equals(thatLine)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public class SourceLine {

        private String className;
        private int lineNumber;

        private boolean isLoopHead;

        public SourceLine(String className, int lineNumber, boolean isLoopHead) {
            super();
            this.className = className;
            this.lineNumber = lineNumber;
            this.setLoopHead(isLoopHead);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null) {
                if (obj instanceof SourceLine) {
                    SourceLine line = (SourceLine) obj;
                    return line.getLineNumber() == lineNumber && line.getClassName()
                        .equals(className);
                }
            }

            return false;
        }

        @Override
        public String toString() {
            return className + ":" + lineNumber;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public boolean isLoopHead() {
            return isLoopHead;
        }

        public void setLoopHead(boolean isLoopHead) {
            this.isLoopHead = isLoopHead;
        }
    }
}