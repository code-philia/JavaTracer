package org.cophi.javatracer.model.scope;

import org.cophi.javatracer.model.location.ClassLocation;
import org.cophi.javatracer.model.trace.TraceNode;

public interface Scope {

    boolean containLocation(ClassLocation location);

    boolean containsNodeScope(TraceNode node);

    boolean isLoop();
}
