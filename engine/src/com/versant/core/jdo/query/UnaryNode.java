
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

import com.versant.core.common.Debug;

import com.versant.core.common.BindingSupportImpl;

/**
 * A node with a single child. This is optimized to treat childList as a
 * single node and not a list.
 */
public class UnaryNode extends Node {

    public UnaryNode() {
    }

    public UnaryNode(Node child) {
        childList = child;
        child.parent = this;
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
      return visitor.visitUnaryNode(this, results);
    }
    /**
     * Resolve field refs and so on relative to the compiler. This must
     * recursively resolve any child nodes.
     */
    public void resolve(QueryParser comp, ClassMetaData cmd, boolean ordering) {
        if (Debug.DEBUG) System.out.println("### UnaryNode.resolve " + this);                
        childList.resolve(comp, cmd, false);
    }

    /**
     * Replace one node with another.
     */
    public void replaceChild(Node old, Node nw) {
        if (childList == old) childList = nw;
        else throw BindingSupportImpl.getInstance().internal("no such Node: " + old);
        nw.parent = this;
        nw.next = null;
    }

    /**
     * Simplify this node tree as much as possible.
     */
    protected void normalizeImp() {
		if (childList != null)
			childList.normalizeImp();
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitUnaryNode(this, obj);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveUnaryNode(this, msg);
    }

}
