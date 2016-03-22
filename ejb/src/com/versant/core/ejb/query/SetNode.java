
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
 * Entry in an update list.
 */
public class SetNode extends Node {

    private String identifier;
    private String fieldName;
    private Node value;

    public SetNode(String identifier, String fieldName, Node value) {
        this.identifier = identifier;
        this.fieldName = fieldName;
        this.value = value;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Node getValue() {
        return value;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveSetNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        if (identifier != null) {
            s.append(identifier);
            s.append('.');
        }
        s.append(fieldName);
        s.append(" = ");
        s.append(value);
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        value.resolve(rc);
    }

}

