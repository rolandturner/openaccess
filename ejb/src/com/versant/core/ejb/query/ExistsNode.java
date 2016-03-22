
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
 * EXISTS.
 */
public class ExistsNode extends Node {

    private boolean not;
    private SelectNode subquery;

    public ExistsNode(boolean not, SelectNode subquery) {
        this.not = not;
        this.subquery = subquery;
    }

    public boolean isNot() {
        return not;
    }

    public SelectNode getSubquery() {
        return subquery;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveExistsNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        if (not) {
            s.append("NOT ");
        }
        s.append("EXISTS (");
        s.append(subquery);
        s.append(')');
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        subquery.resolve(rc);
    }

}

