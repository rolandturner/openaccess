
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
 * BETWEEN.
 */
public class BetweenNode extends Node {

    private Node arg;
    private boolean not;
    private Node from;
    private Node to;

    public BetweenNode(Node arg, boolean not, Node from,
            Node to) {
        this.arg = arg;
        this.not = not;
        this.from = from;
        this.to = to;
    }

    public Node getArg() {
        return arg;
    }

    public boolean isNot() {
        return not;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveBetweenNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append(arg);
        if (not) {
            s.append(" NOT");
        }
        s.append(" BETWEEN ");
        s.append(from);
        s.append(" AND ");
        s.append(to);
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        arg.resolve(rc);
        from.resolve(rc);
        to.resolve(rc);
    }

}

