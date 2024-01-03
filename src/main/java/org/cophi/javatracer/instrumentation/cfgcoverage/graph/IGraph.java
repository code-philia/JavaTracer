package org.cophi.javatracer.instrumentation.cfgcoverage.graph;

import java.util.List;

public interface IGraph<T extends IGraphNode<T>> {

    List<T> getNodeList();

    List<T> getExitList();

}
