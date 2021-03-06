
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
 * DELETE FROM.
 */
public class DeleteNode extends Node {

    private String schemaName;
    private String identifier;
    private Node where;

    public DeleteNode(String schemaName, String identifier, Node where) {
        this.schemaName = schemaName;
        this.identifier = identifier;
        this.where = where;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Node getWhere() {
        return where;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveDeleteNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append("UPDATE ");
        s.append(schemaName);
        s.append(" AS ");
        s.append(identifier);
        if (where != null) {
            s.append("\nWHERE ");
            s.append(where);
        }
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        if (where != null) {
            where.resolve(rc);
        }
    }

}

