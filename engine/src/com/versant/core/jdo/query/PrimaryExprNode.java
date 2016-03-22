
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
package com.versant.core.jdo.query;

/**
 * Container for method calls and array indexing.
 */
public class PrimaryExprNode extends Node {

    public PrimaryExprNode() {
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitPrimaryExprNode(this, obj);
    }

    /**
     * Simplify this node tree as much as possible.
     */
    protected void normalizeImp() {
        super.normalizeImp();
        // convert this tree:
        //   PrimaryExprNode [this]
        //     FieldNavNode data (cast Address)
        // into:
        //   FieldNavNode data (cast: Address)
        if (parent != null && childList instanceof FieldNavNode) {
            parent.replaceChild(this, childList);
        }
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arrivePrimaryExprNode(this, msg);
    }

}
