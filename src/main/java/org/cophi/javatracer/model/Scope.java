package org.cophi.javatracer.model;

import org.cophi.javatracer.model.trace.TraceNode;

public interface Scope {

    boolean containsNodeScope(TraceNode node);

    boolean containLocation(ClassLocation location);

    boolean isLoop();
}
