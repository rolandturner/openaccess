
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
 * A node that is created for queries with a non-null result spefied.
 * This creates necc. metadata from the parsed structure to be used by the application.
 */
public class ResultNode extends Node {
    public static final int[] EMPTY_INT_ARRAY = new int[0];

    private boolean distinct;
    private int containsVarNodes = 0;
    private int size;


    public ResultNode(Node child) {
        childList = child;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public String toString() {
        return super.toString() + " distinct = " + distinct;
    }

    /**
     * Resolve field refs and so on relative to the compiler. This must
     * recursively resolve any child nodes.
     */
    public void resolve(QueryParser comp, ClassMetaData cmd, boolean ordering) {
        for (Node n = childList; n != null; n = n.next) {
            n.resolve(comp, cmd, ordering);
            size++;
        }
    }

    public int getResultSize() {
        return size;
    }

    /**
     * Abstract method to force all nodes to implement visitor pattern
     */
    public Field visit(MemVisitor visitor, Object obj) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean processForVarNodes() {
        if (containsVarNodes == 0) {
            if (containsVarNodeImp(this)) {
                containsVarNodes = 1;
            } else {
                containsVarNodes = -1;
            }
        }
        return (containsVarNodes == 1);
    }

    private boolean containsVarNodeImp(Node n) {
        if (n == null) return false;
        if (n instanceof VarNodeIF) {
            ((VarNodeIF)n).setUsedInProjection(true);
            return true;
        }
        if (n instanceof FieldNavNode) {
            if (((FieldNavNode)n).var != null) {
                ((FieldNavNode)n).var.setUsedInProjection(true);
                return true;
            }
        }

        if (n.childList != null) {
            if (containsVarNodeImp(n.childList)) {
                return true;
            }
        }
        for (Node nn = n.next; nn != null; nn = nn.next) {
            if (containsVarNodeImp(nn)) return true;
        }
        return false;
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
        return visitor.visitResultNode(this, results);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveResultNode(this, msg);
    }
}
