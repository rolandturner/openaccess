
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

import com.versant.core.common.BindingSupportImpl;

/**
 * This is a node with exactly two children. It is optimized to take
 * advantage of this.
 */
public class BinaryNode extends Node {

    public BinaryNode() {
    }

    public BinaryNode(Node left, Node right) {
        childList = left;
		if (left != null) {
			left.next = right;
			left.parent = this;
		}
		if (right != null) {
			right.parent = this;
        }
    }

    public final Node getLeft() {
        return childList;
    }

    public final Node getRight() {
        return childList.next;
    }

    /**
     * Resolve field refs and so on relative to the compiler. This must
     * recursively resolve any child nodes.
     */
    public void resolve(QueryParser comp, ClassMetaData cmd, boolean ordering) {
        childList.resolve(comp, cmd, false);
        childList.next.resolve(comp, cmd, false);
    }

    /**
     * Replace one node with another.
     */
    public void replaceChild(Node old, Node nw) {
        if (childList == old) {
            nw.next = childList.next;
            childList = nw;
        } else if (childList.next == old) {
            childList.next = nw;
            nw.next = null;
        } else {
            throw BindingSupportImpl.getInstance().internal("no such Node: " + old);
        }
        nw.parent = this;
    }

    /**
     * Simplify this node tree as much as possible.
     */
    public void normalizeImp() {
        childList.normalizeImp();
        childList.next.normalizeImp();

        // swap left and right nodes if a literal or param is on the left
        if (childList instanceof LiteralNode
                || childList instanceof ParamNode
                || childList instanceof ParamNodeProxy) {
            swapLeftAndRight();
        }
    }

    /**
     * Swap left and right nodes.
     */
    protected void swapLeftAndRight() {
        Node t = childList;
        childList = childList.next;
        childList.next = t;
        t.next = null;
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitBinaryNode(this, obj);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveBinaryNode(this, msg);
    }
}
