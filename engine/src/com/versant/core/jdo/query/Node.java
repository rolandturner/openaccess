
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
import com.versant.core.common.CmdBitSet;

import com.versant.core.common.BindingSupportImpl;

/**
 * A node in the parse tree for the query.
 */
public abstract class Node {

    /**
     * Our parent node (null if this is the root node).
     */
    public Node parent;
    /**
     * This makes it easy to form linked lists of nodes. This is
     * faster and uses less memory than arrays of nodes.
     */
    public Node next;
    /**
     * Linked list of children formed using their next fields.
     */
    public Node childList;

    public String asValue;

    public Node() {
    }

    public String toString() {
        return toStr();
    }

    private String toStr() {
        String n = getClass().getName();
        int i = n.lastIndexOf('.');
        if (i >= 0) n = n.substring(i + 1);
        return n + "@" + Integer.toHexString(System.identityHashCode(this));
    }

    /**
     * Dump debugging info to System.out.
     */
    public void dump(String indent) {
        dumpThis(indent);
        indent = indent + "  ";
        for (Node c = childList; c != null; c = c.next) c.dump(indent);
    }

    /**
     * Dump without children.
     */
    protected void dumpThis(String indent) {
        Debug.OUT.println(indent + this + " parent " +
            (parent == null ? "(null)" : parent.toStr()));
    }

    /**
     * Dump as a list.
     */
    public void dumpList() {
        dumpThis("");
        for (Node c = next; c != null; c = c.next) c.dumpThis(" -> ");
    }

    /**
     * Resolve field refs and so on relative to the compiler. This must
     * recursively resolve any child nodes.
     */
    public void resolve(QueryParser comp, ClassMetaData cmd, boolean ordering) {
        if (Debug.DEBUG) System.out.println("### Node.resolve " + this);        
        for (Node c = childList; c != null; c = c.next) c.resolve(comp, cmd, ordering);
    }

    /**
     * Set the parent link on all our children.
     */
    public void setParentOnChildren() {
        for (Node c = childList; c != null; c = c.next) c.parent = this;
    }

    /**
     * Replace one node with another.
     */
    public void replaceChild(Node old, Node nw) {
        if (childList == old) {
            nw.next = childList.next;
            childList = nw;
            nw.parent = this;
            return;
        }
        for (Node c = childList; ; ) {
            Node next = c.next;
            if (next == old) {
                nw.next = next.next;
                c.next = nw;
                nw.parent = this;
                return;
            }
            if (next == null) break;
            c = next;
        }
        throw BindingSupportImpl.getInstance().internal("no such Node: " + old);
    }

    /**
     * Insert one node (nw) before another (pos).
     */
    public void insertChildBefore(Node pos, Node nw) {
        if (childList == pos) {
            nw.next = pos;
            childList = nw;
            nw.parent = this;
            return;
        }
        for (Node c = childList; ; ) {
            Node next = c.next;
            if (next == pos) {
                nw.next = pos;
                c.next = nw;
                nw.parent = this;
                return;
            }
            if (next == null) break;
            c = next;
        }
        throw BindingSupportImpl.getInstance().internal("no such Node: " + pos);
    }

    /**
     * Simplify this node tree as much as possible.
     */
    public final void normalize() {
        normalizeImp();
        if (Debug.DEBUG) checkIntegrity();
    }

    /**
     * Simplify this node tree as much as possible.
     */
    protected void normalizeImp() {
        for (Node c = childList; c != null; c = c.next) {
            c.normalizeImp();
        }
    }

    /**
     * Abstract method to force all nodes to implement visitor pattern
     */
    public abstract Field visit(MemVisitor visitor, Object obj);

    public Object accept(NodeVisitor visitor, Object[] results) {
        throw BindingSupportImpl.getInstance().internal("Not supported for node " + 
                this.getClass().getName());
    }

    /**
     * Implement this in nodes to udpate the ClassMetaData depency of the graph.
     * This is used for query eviction.
     * 
     * @param bitSet
     */
    public void updateEvictionDependency(CmdBitSet bitSet) {
    }

    /**
     * Check the integrity of this node. This is used during debugging to
     * check that all the pointers work out. Currently it just makes sure
     * that all our children list us as their parents.
     */
    public void checkIntegrity() {
        for (Node i = childList; i != null; i = i.next) {
            if (i.parent != this) {
                throw new IllegalStateException(
                        "Bad child node parent reference:\n" +
                        "Parent: " + this + "\n" +
                        "Child: " + i);
            }
        }
        for (Node i = childList; i != null; i = i.next) i.checkIntegrity();
    }

    /**
     * Invoke v's arriveXXX method for the node.
     */
    public abstract Object arrive(NodeVisitor v, Object msg);

}
