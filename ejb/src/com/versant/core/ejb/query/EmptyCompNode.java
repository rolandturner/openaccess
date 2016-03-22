
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
 * IS EMPTY and IS NOT EMPTY.
 */
public class EmptyCompNode extends Node {

    // todo make sure path is PathNode in annotate
    private Node path;
    private boolean not;

    public EmptyCompNode(Node path, boolean not) {
        this.path = path;
        this.not = not;
    }

    public PathNode getPath() {
        return (PathNode)path;
    }

    public boolean isNot() {
        return not;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveEmptyCompNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append(path);
        s.append(not ? " IS NOT EMPTY" : " IS EMPTY");
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        path.resolve(rc);
    }

}

