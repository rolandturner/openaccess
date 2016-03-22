
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
 * An 'and' node. These may participate in joins.
 */
public class AndNode extends Node {

    public AndNode() {
    }



    public Object accept(NodeVisitor visitor, Object[] results) {
      return visitor.visitAndNode(this, results);
    }

    /**
     * Create a new instance of us.
     */
    protected AndNode createInstance() {
        return new AndNode();
    }

    /**
     * Simplify this node tree as much as possible.
     */
    protected void normalizeImp() {
        if (getClass() != AndNode.class) {
            super.normalizeImp();
            return;
        }
        // merge children of nested AndNode's into our child list
        Node prev = null;
        for (Node n = childList; n != null;) {
            n.normalizeImp();
            if (n.getClass() == AndNode.class) {
                if (n.childList == null) {
                    // no children?? remove it from the list
                    n = n.next;
                    if (prev == null) {
                        childList = n;
                    } else {
                        prev.next = n;
                    }
                } else {
                    // walk to the end of n's childList and splice that into ours
                    // in place of n
                    Node pos;
                    for (pos = n.childList; pos.next != null; pos = pos.next) {
                        pos.parent = this;
                    }
                    pos.parent = this;
                    pos.next = n.next;
                    if (prev == null) {
                        childList = n.childList;
                    } else {
                        prev.next = n.childList;
                    }
                    prev = pos;
                    n = n.next;
                }
            } else {
                prev = n;
                n = n.next;
            }
        }
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitAndNode(this, obj);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveAndNode(this, msg);
    }

}
