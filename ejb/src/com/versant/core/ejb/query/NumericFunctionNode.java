
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
 * Functions that return numerics.
 */
public class NumericFunctionNode extends Node {

    public static final int LENGTH = 1;
    public static final int LOCATE = 2;
    public static final int ABS = 3;
    public static final int SQRT = 4;
    public static final int MOD = 5;
    public static final int BIT_LENGTH = 6;

    private int function;
    private Node args;

    public NumericFunctionNode(int function, Node args) {
        this.function = function;
        this.args = args;
    }

    public int getFunction() {
        return function;
    }

    public Node getArgs() {
        return args;
    }

    public String getFunctionStr() {
        switch (function) {
            case LENGTH:        return "LENGTH";
            case LOCATE:        return "LOCATE";
            case ABS:           return "ABS";
            case SQRT:          return "SQRT";
            case MOD:           return "MOD";
            case BIT_LENGTH:    return "BIT_LENGTH";
        }
        return "<? function " + function + " ?>";
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveNumericFunctionNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append(getFunctionStr());
        s.append('(');
        if (args != null) {
            args.appendList(s);
        }
        s.append(')');
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        args.resolve(rc);
    }

}

