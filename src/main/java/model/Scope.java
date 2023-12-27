package model;

import model.trace.TraceNode;

public interface Scope {

    boolean containsNodeScope(TraceNode node);

    boolean containLocation(ClassLocation location);

    boolean isLoop();
}
