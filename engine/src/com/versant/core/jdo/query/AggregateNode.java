
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

import com.versant.core.metadata.ClassMetaData;

/**
 * Node to represent aggregate expressions
 */
public class AggregateNode extends UnaryNode {

    public static final int TYPE_SUM = 1;
    public static final int TYPE_AVG = 2;
    public static final int TYPE_COUNT = 3;
    public static final int TYPE_MIN = 4;
    public static final int TYPE_MAX = 5;

    private int type;

    public AggregateNode(Node child, int type) {
        super(child);
        this.asValue = child.asValue;
        this.type = type;
    }

    public AggregateNode() {
        type = TYPE_COUNT;
    }

    /**
     * Resolve field refs and so on relative to the compiler. This must
     * recursively resolve any child nodes.
     */
    public void resolve(QueryParser comp, ClassMetaData cmd, boolean ordering) {
        childList.resolve(comp, cmd, ordering);
    }

    public String getTypeString() {
        switch (type) {
            case AggregateNode.TYPE_AVG:
                return "AVG";
            case AggregateNode.TYPE_COUNT:
                return "COUNT";
            case AggregateNode.TYPE_MAX:
                return "MAX";
            case AggregateNode.TYPE_MIN:
                return "MIN";
            case AggregateNode.TYPE_SUM:
                return "SUM";
        }
        return "UNKNOWN(" + type + ")";
    }

    public int getType() {
        return type;
    }

    public String toString() {
        return super.toString() + "Type " + getTypeString() + " as " + asValue;
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
        return visitor.visitAggregateNode(this, results);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveAggregateNode(this, msg);
    }
}
