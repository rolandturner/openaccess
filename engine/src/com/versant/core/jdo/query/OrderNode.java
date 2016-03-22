
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
 * An order specification.
 */
public class OrderNode extends Node {

    public static final int ORDER_ASCENDING = 0;
    public static final int ORDER_DESCENDING = 1;

    public int order;

    public OrderNode() {
    }

    public String toString() {
        return super.toString() +
            (order == ORDER_ASCENDING ? " ascending" : " descending");
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitOrderNode(this, obj);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveOrderNode(this, msg);
    }

    public Object accept(NodeVisitor v, Object[] results) {
        return v.visitOrderNode(this, results);
      }
}

