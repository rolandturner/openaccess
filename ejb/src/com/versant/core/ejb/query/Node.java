
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
package com.versant.core.ejb.query;

/**
 * An expression. Has a next pointer so these can be easily chained together
 * into lists.
 */
public abstract class Node {

    private Node next;

    public Node() {
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void appendList(StringBuffer s) {
        appendList(s, ", ");
    }

    public void appendList(StringBuffer s, String separator) {
        s.append(this);
        if (next != null) {
            for (Node i = next; i != null; i = i.next) {
                s.append(separator);
                s.append(i);
            }
        }
    }

    public String toString() {
        String s = getClass().getName();
        int i = s.lastIndexOf('.') + 1;
        int j = s.lastIndexOf("Node");
        StringBuffer b = new StringBuffer();
        b.append('{');
        if (j >= 0) {
            b.append(s.substring(i, j));
        } else {
            b.append(s.substring(i));
        }
        b.append(' ');
        b.append(toStringImp());
        b.append('}');
        return b.toString();
    }

    public abstract String toStringImp();

    /**
     * Attach type information to the Nodes using rc.
     */
    public void resolve(ResolveContext rc) {
    }

    /**
     * Call resolve on each node in list. NOP if list is null.
     */
    protected static void resolve(Node list, ResolveContext rc) {
        for (; list != null; list = list.next) {
            list.resolve(rc);
        }
    }

    /**
     * Invoke v's arriveXXX method for the node.
     */
    public abstract Object arrive(NodeVisitor v, Object msg);

}

