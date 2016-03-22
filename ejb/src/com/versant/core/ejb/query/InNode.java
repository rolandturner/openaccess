
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
 * in_expression.
 */
public class InNode extends Node {

    // todo make sure path is PathNode in annotate
    private Node path;
    private boolean not;
    private Node inList;

    public InNode(Node arg, boolean not, Node inList) {
        this.path = arg;
        this.not = not;
        this.inList = inList;
    }

    public PathNode getPath() {
        return (PathNode)path;
    }

    public boolean isNot() {
        return not;
    }

    public Node getInList() {
        return inList;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveInNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append(path);
        if (not) {
            s.append(" NOT");
        }
        s.append(" IN ");
        s.append('(');
        s.append(inList);
        for (Node e = inList.getNext(); e != null; e = e.getNext()) {
            s.append(", ");
            s.append(e);
        }
        s.append(')');
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        path.resolve(rc);
        resolve(inList, rc);
    }

}

