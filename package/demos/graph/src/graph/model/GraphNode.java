
/*
 * Copyright (c) 1998 - 2005 Versant Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Versant Corporation - initial API and implementation
 */
package graph.model;

import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Iterator;

/**
 * A node in an directed graph. It has a set of incoming and outgoing edges
 * in a many-to-many.
 */
public class GraphNode {

    private String name;
    private Set inEdges = new HashSet();
    private Set outEdges = new HashSet();

    public GraphNode() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set getInEdges() {
        return Collections.unmodifiableSet(inEdges);
    }

    public void addInEdge(GraphNode node) {
        inEdges.add(node);
        node.outEdges.add(this);
    }

    public void removeInEdge(GraphNode node) {
        inEdges.remove(node);
        node.outEdges.remove(this);
    }

    public Set getOutEdges() {
        return Collections.unmodifiableSet(outEdges);
    }

    public void addOutEdge(GraphNode node) {
        outEdges.add(node);
        node.inEdges.add(this);
    }

    public void removeOutEdge(GraphNode node) {
        outEdges.remove(node);
        node.inEdges.remove(this);
    }

    public String toString() {
        return name;
    }

}
