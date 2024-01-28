package org.cophi.javatracer.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.cophi.javatracer.model.trace.PotentialCorrectPatternList;
import org.cophi.javatracer.model.trace.Trace;
import org.cophi.javatracer.model.trace.TraceNode;
import org.cophi.javatracer.model.variables.AttributionVar;
import org.cophi.javatracer.model.variables.UserInterestedVariables;
import org.cophi.javatracer.recommendation.StepRecommender;

public class CheckingState {

    private int currentNodeOrder;
    private int currentNodeCheckTime;
    private Map<AttributionVar, Double> currentNodeSuspicousScoreMap = new HashMap<AttributionVar, Double>();

    private StepRecommender recommender;

    private int traceCheckTime;

    private UserInterestedVariables interestedVariables;
    private HashSet<Integer> wrongPathNodeOrder;

    private PotentialCorrectPatternList potentialCorrectPatterns;

    public int getCurrentNodeCheckTime() {
        return currentNodeCheckTime;
    }

    public void setCurrentNodeCheckTime(int currentNodeCheckTime) {
        this.currentNodeCheckTime = currentNodeCheckTime;
    }

    public int getCurrentNodeOrder() {
        return currentNodeOrder;
    }

    public void setCurrentNodeOrder(int currentNodeOrder) {
        this.currentNodeOrder = currentNodeOrder;
    }

    public Map<AttributionVar, Double> getCurrentNodeSuspicousScoreMap() {
        return currentNodeSuspicousScoreMap;
    }

    public void setCurrentNodeSuspicousScoreMap(
        Map<AttributionVar, Double> currentNodeSuspicousScoreMap) {
        this.currentNodeSuspicousScoreMap = currentNodeSuspicousScoreMap;
    }

    public UserInterestedVariables getInterestedVariables() {
        return interestedVariables;
    }

    public void setInterestedVariables(UserInterestedVariables interestedVariables) {
        this.interestedVariables = interestedVariables;
    }

    public PotentialCorrectPatternList getPotentialCorrectPatterns() {
        return potentialCorrectPatterns;
    }

    public void setPotentialCorrectPatterns(PotentialCorrectPatternList potentialCorrectPatterns) {
        this.potentialCorrectPatterns = potentialCorrectPatterns;
    }

    public StepRecommender getRecommender() {
        return recommender;
    }

    public void setRecommender(StepRecommender recommender) {
        this.recommender = recommender;
    }

    public int getTraceCheckTime() {
        return traceCheckTime;
    }

    public void setTraceCheckTime(int traceCheckTime) {
        this.traceCheckTime = traceCheckTime;
    }

    public HashSet<Integer> getWrongPathNodeOrder() {
        return wrongPathNodeOrder;
    }

    public void setWrongPathNodeOrder(HashSet<Integer> wrongPathNodeOrder) {
        this.wrongPathNodeOrder = wrongPathNodeOrder;
    }

    @SuppressWarnings("unchecked")
    public void recordCheckingState(TraceNode currentNode,
        StepRecommender recommender, Trace trace,
        UserInterestedVariables interestedVariables,
        HashSet<Integer> wrongPathNodeOrder,
        PotentialCorrectPatternList potentialCorrectPatterns) {

        this.currentNodeOrder = currentNode.getOrder();
        this.currentNodeCheckTime = currentNode.getCheckTime();
        Map<AttributionVar, Double> map = currentNode.getSuspicousScoreMap();
        for (AttributionVar var : map.keySet()) {
            this.currentNodeSuspicousScoreMap.put(var, map.get(var));
        }

        this.recommender = recommender.clone();

        this.traceCheckTime = trace.getCheckTime();

        this.interestedVariables = interestedVariables.clone();
        this.wrongPathNodeOrder = (HashSet<Integer>) wrongPathNodeOrder.clone();

        this.potentialCorrectPatterns = potentialCorrectPatterns.clone();
    }


}