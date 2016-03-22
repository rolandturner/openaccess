
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
package com.versant.core.jdo.query;

/**
 * Node to represent 'Group By'.
 */
public class GroupingNode extends Node {

    public Node havingNode;

    /**
     * Abstract method to force all nodes to implement visitor pattern
     */
    public Field visit(MemVisitor visitor, Object obj) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String toString() {
        return super.toString() + " HAVING " + (havingNode != null);
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
        return visitor.visitGroupingNode(this, results);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveGroupingNode(this, msg);
    }

}
