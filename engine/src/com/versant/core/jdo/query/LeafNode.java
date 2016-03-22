
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

import com.versant.core.common.BindingSupportImpl;

/**
 * This is a node with no children. Extending Node (which has children) to
 * make this class may seem strange but lots of casting is saved in all the
 * code the traverses Node trees.
 */
public class LeafNode extends Node {

    /**
     * Set the parent link on all our children.
     */
    public void setParentOnChildren() {
    }

    /**
     * Replace one node with another.
     */
    public void replaceChild(Node old, Node nw) {
        throw BindingSupportImpl.getInstance().internal("replaceChild called on LeafNode");
    }

    /**
     * Simplify this node tree as much as possible.
     */
    public void normalizeImp() {
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitLeafNode(this, obj);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveLeafNode(this, msg);
    }
}
