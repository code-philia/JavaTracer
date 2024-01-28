package org.cophi.javatracer.model.scope;

import java.util.ArrayList;
import java.util.List;
import org.cophi.javatracer.model.location.ClassLocation;
import org.cophi.javatracer.model.trace.TraceNode;

public class ControlScope implements Scope {

    private List<ClassLocation> rangeList = new ArrayList<>();
    private boolean isLoop;

    public ControlScope() {

    }

    public ControlScope(List<ClassLocation> rangeList, boolean isLoop) {
        this.rangeList = rangeList;
        this.isLoop = isLoop;
    }

    public void addLocation(ClassLocation location) {
        this.rangeList.add(location);

    }

    @Override
    public boolean containLocation(ClassLocation location) {
        return this.rangeList.contains(location);
    }

    public boolean containsNodeScope(TraceNode node) {
        return this.containLocation(node.getBreakPoint());
    }

    public boolean isLoop() {
        return isLoop;
    }

    public void setLoop(boolean isLoop) {
        this.isLoop = isLoop;
    }

    public List<ClassLocation> getRangeList() {
        return rangeList;
    }

    public void setRangeList(List<ClassLocation> rangeList) {
        this.rangeList = rangeList;
    }

    @Override
    public String toString() {
        return "LocationScope [isLoop=" + isLoop + ", rangeList=" + rangeList + "]";
    }

}
