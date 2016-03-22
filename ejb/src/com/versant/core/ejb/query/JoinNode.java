
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
 * A join or fetch_join.
 */
public class JoinNode extends Node {

    private boolean outer;
    private boolean fetch;
    private PathNode path;
    private String identifier;

    private NavField navField;

    public JoinNode(boolean left, boolean fetch, PathNode path,
            String identifier) {
        this.outer = left;
        this.fetch = fetch;
        this.path = path;
        this.identifier = identifier;
    }

    public boolean isOuter() {
        return outer;
    }

    public boolean isFetch() {
        return fetch;
    }

    public PathNode getPath() {
        return path;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveJoinNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        if (outer) {
            s.append("LEFT OUTER ");
        }
        s.append("JOIN ");
        if (fetch) {
            s.append("FETCH ");
        }
        s.append(path);
        if (navField != null) {
            s.append('%');
            s.append(navField.getFmd().name);
        }
        s.append(" AS ");
        s.append(identifier);
        return s.toString();
    }

    public NavField getNavField() {
        return navField;
    }

    public void resolve(ResolveContext rc) {
        rc.checkIdVarDoesNotExist(identifier, this);
        NavBase res = rc.resolveJoinPath(path, outer, fetch);
        if (!(res instanceof NavField)) {
            rc.createUserException("Expected field navigation path: " +
                    path.toStringImp(), path);
        }
        navField = (NavField)res;
    }

}

