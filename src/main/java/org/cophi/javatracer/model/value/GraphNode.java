package org.cophi.javatracer.model.value;

import java.util.List;

public interface GraphNode {

    List<? extends GraphNode> getChildren();

    List<? extends GraphNode> getParents();

    /**
     * This method compares the labels of two nodes. It will not further compare their children.
     *
     * @param node
     * @return
     */
    boolean match(GraphNode node);

    /**
     * compare the content of two graph nodes
     *
     * @param node
     * @return
     */
    boolean isTheSameWith(GraphNode node);

    //public String getStringValue();
}
