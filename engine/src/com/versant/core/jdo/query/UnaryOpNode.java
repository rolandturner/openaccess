
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
 * A unary operation.
 */
public class UnaryOpNode extends UnaryNode {

    public static final int OP_MINUS = 1;
    public static final int OP_PLUS = 2;
    public static final int OP_TILDE = 3;
    public static final int OP_BANG = 4;

    /**
     * The operation.
     */
    public int op;

    public UnaryOpNode(Node child, int op) {
        super(child);
        this.op = op;
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
      return visitor.visitUnaryOpNode(this, results);
    }

    public String toString() {
        return super.toString() + " " + toOpString(op);
    }

    public static String toOpString(int op) {
        switch (op) {
            case OP_MINUS:
                return "-";
            case OP_PLUS:
                return "+";
            case OP_TILDE:
                return "~";
            case OP_BANG:
                return "!";
        }
        return "Unknown(" + op + ")";
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitUnaryOpNode(this, obj);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveUnaryOpNode(this, msg);
    }
}

