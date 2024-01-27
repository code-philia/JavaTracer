package org.cophi.javatracer.cfg;

import java.util.List;

public interface IGraphNode<T extends IGraphNode<T>> {

    List<T> getChildren();

    List<T> getParents();
}
