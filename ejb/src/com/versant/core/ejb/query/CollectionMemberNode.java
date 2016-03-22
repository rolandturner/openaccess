
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
 * A collection_member_declaration.
 */
public class CollectionMemberNode extends Node {

    private PathNode path;
    private String identifier;

    public CollectionMemberNode(PathNode path, String identifier) {
        this.path = path;
        this.identifier = identifier;
    }

    public PathNode getPath() {
        return path;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveCollectionMemberNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append("IN (");
        s.append(path);
        s.append(") AS ");
        s.append(identifier);
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        path.resolve(rc);
    }

}

