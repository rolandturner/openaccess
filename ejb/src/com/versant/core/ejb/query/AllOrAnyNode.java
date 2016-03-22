
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
 * ALL ANY SOME.
 */
public class AllOrAnyNode extends Node {

    private boolean all;
    private SelectNode subquery;

    public AllOrAnyNode(boolean all, SelectNode subquery) {
        this.all = all;
        this.subquery = subquery;
    }

    public boolean isAll() {
        return all;
    }

    public SelectNode getSubquery() {
        return subquery;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveAllOrAnyNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append(all ? "ALL (" : "ANY (");
        s.append(subquery);
        s.append(')');
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        subquery.resolve(rc);
    }

}

