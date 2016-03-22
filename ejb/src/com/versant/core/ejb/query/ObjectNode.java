
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
 * OBJECT(identification_variable).
 */
public class ObjectNode extends Node {

    private String identifier;

    private NavBase navBase;

    public ObjectNode(String identifier) {
        this.identifier = identifier;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveObjectNode(this, msg);
    }

    public String toStringImp() {
        return "OBJECT(" + identifier + ")";
    }

    public NavBase getNavBase() {
        return navBase;
    }

    public void resolve(ResolveContext rc) {
        navBase = rc.checkIdVarExists(identifier, this);
    }

}

