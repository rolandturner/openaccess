
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
 * Comparision expression.
 */
public class CompNode extends BinaryNode {

    public static final int EQ = 1; // =
    public static final int GT = 2; // >
    public static final int GE = 3; // >=
    public static final int LT = 4; // <
    public static final int LE = 5; // <=
    public static final int NE = 6; // <>

    private int op;

    public CompNode(Node left, int op, Node right) {
        super(left, right);
        this.op = op;
    }

    public int getOp() {
        return op;
    }

    public String getOpStr() {
        switch (op) {
            case EQ:    return "=";
            case GT:    return ">";
            case GE:    return ">=";
            case LT:    return "<";
            case LE:    return "<=";
            case NE:    return "<>";
        }
        return "<? op " + op + " ?>";
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveCompNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append(left);
        s.append(' ');
        s.append(getOpStr());
        s.append(' ');
        s.append(right);
        return s.toString();
    }

}

