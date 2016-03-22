
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
 * An 'or' node.
 */
public class OrNode extends AndNode {

    public OrNode() {
    }



    public Object accept(NodeVisitor visitor, Object[] results) {
      return visitor.visitOrNode(this, results);
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitOrNode(this, obj);
    }

    /**
     * Create a new instance of us.
     */
    protected AndNode createInstance() {
        return new OrNode();
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveOrNode(this, msg);
    }

}

