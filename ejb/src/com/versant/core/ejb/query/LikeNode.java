
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
 * LIKE.
 */
public class LikeNode extends Node {

    // todo make sure path is PathNode in annotate
    private Node path;
    private boolean not;
    private Node pattern;
    private Node escape;

    public LikeNode(Node path, boolean not, Node pattern,
            Node escape) {
        this.path = path;
        this.not = not;
        this.pattern = pattern;
        this.escape = escape;
    }

    public PathNode getPath() {
        return (PathNode)path;
    }

    public boolean isNot() {
        return not;
    }

    public Node getPattern() {
        return pattern;
    }

    public Node getEscape() {
        return escape;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveLikeNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append(path);
        if (not) {
            s.append(" NOT");
        }
        s.append(" LIKE ");
        s.append(pattern);
        if (escape != null) {
            s.append(" ESCAPE ");
            s.append(escape);
        }
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        path.resolve(rc);
        if (escape != null) {
            escape.resolve(rc);
        }
        pattern.resolve(rc);
    }

}

