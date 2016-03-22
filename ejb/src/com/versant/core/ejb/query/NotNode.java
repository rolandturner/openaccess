
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
 * NOT.
 */
public class NotNode extends Node {

    private Node exp;

    public NotNode(Node exp) {
        this.exp = exp;
    }

    public Node getExp() {
        return exp;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveNotNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append("NOT ");
        s.append(exp);
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        exp.resolve(rc);
    }

}

