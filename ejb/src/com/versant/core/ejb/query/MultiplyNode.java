
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
 * arithmetic_expression.
 */
public class MultiplyNode extends BinaryNode {

    public static final int MULTIPLY = 1;
    public static final int DIVIDE = 2;

    private Node left;
    private int op;
    private Node right;

    public MultiplyNode(Node left, int op, Node right) {
        super(left, right);
        this.op = op;
    }

    public int getOp() {
        return op;
    }

    public String getOpStr() {
        switch (op) {
            case MULTIPLY:  return "*";
            case DIVIDE:    return "/";
        }
        return "<? op " + op + " ?>";
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveMultiplyNode(this, msg);
    }

    public String toStringImp() {
        return left + " " + getOpStr() + " " + right;
    }

}

