
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
 * NULL or IS NOT NULL.
 */
public class NullCompNode extends Node {

    private Node arg;
    private boolean not;

    public NullCompNode(Node arg, boolean not) {
        this.arg = arg;
        this.not = not;
    }

    public Node getArg() {
        return arg;
    }

    public boolean isNot() {
        return not;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveNullCompNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append(arg);
        s.append(not ? " IS NOT NULL" : " IS NULL");
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        arg.resolve(rc);
    }

}

