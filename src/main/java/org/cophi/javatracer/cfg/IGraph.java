package org.cophi.javatracer.cfg;

import java.util.List;

public interface IGraph<T extends IGraphNode<T>> {

    List<T> getExitList();

    List<T> getNodeList();
}
