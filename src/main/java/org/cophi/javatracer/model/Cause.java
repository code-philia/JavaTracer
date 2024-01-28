package org.cophi.javatracer.model;

import org.cophi.javatracer.model.trace.TraceNode;

public class Cause {

    private TraceNode buggyNode;
    private String wrongVariableID = null;

    private boolean isWrongPath = false;
    private TraceNode recommendedNode;

    public Cause clone() {
        Cause clonedCause = new Cause();
        clonedCause.buggyNode = this.buggyNode;
        clonedCause.isWrongPath = this.isWrongPath;
        clonedCause.wrongVariableID = this.wrongVariableID;
        clonedCause.recommendedNode = this.recommendedNode;

        return clonedCause;
    }

    public TraceNode getBuggyNode() {
        return buggyNode;
    }

    public void setBuggyNode(TraceNode buggyNode) {
        this.buggyNode = buggyNode;
    }

    public TraceNode getRecommendedNode() {
        return recommendedNode;
    }

    public void setRecommendedNode(TraceNode recommendedNode) {
        this.recommendedNode = recommendedNode;
    }

    public String getWrongVariableID() {
        return wrongVariableID;
    }

    public void setWrongVariableID(String wrongVariableID) {
        this.wrongVariableID = wrongVariableID;
    }

    public boolean isCausedByWrongPath() {
        return this.isWrongPath;
    }

    public boolean isCausedByWrongVariable() {
        return this.wrongVariableID != null;
    }

    public boolean isWrongPath() {
        return isWrongPath;
    }

    public void setWrongPath(boolean isWrongPath) {
        this.isWrongPath = isWrongPath;
    }


}