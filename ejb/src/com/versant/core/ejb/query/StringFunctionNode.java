
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
 * Functions that return strings.
 */
public class StringFunctionNode extends Node {

    public static final int CONCAT = 1;
    public static final int SUBSTRING = 2;
    public static final int TRIM = 3;
    public static final int LOWER = 4;
    public static final int UPPER = 5;

    public static final int TRIM_LEADING = 1;
    public static final int TRIM_TRAILING = 2;
    public static final int TRIM_BOTH = 3;

    private int function;
    private Node argList;
    private int trimSpec;
    private LiteralNode trimChar;

    public StringFunctionNode(int function, Node argList) {
        this.function = function;
        this.argList = argList;
    }

    public StringFunctionNode(int trimSpec, LiteralNode trimChar, Node argList) {
        this(TRIM, argList);
        this.trimSpec = trimSpec;
        this.trimChar = trimChar;
    }

    public int getFunction() {
        return function;
    }

    public Node getArgList() {
        return argList;
    }

    public int getTrimSpec() {
        return trimSpec;
    }

    public LiteralNode getTrimChar() {
        return trimChar;
    }

    public String getFunctionStr() {
        switch (function) {
            case CONCAT:        return "CONCAT";
            case SUBSTRING:     return "SUBSTRING";
            case TRIM:          return "TRIM";
            case LOWER:         return "LOWER";
            case UPPER:         return "UPPER";
        }
        return "<? function " + function + " ?>";
    }

    public String getTrimSpecStr() {
        switch (trimSpec) {
            case TRIM_LEADING:  return "LEADING";
            case TRIM_TRAILING: return "TRAILING";
            case TRIM_BOTH:     return "BOTH";
        }
        return "<? trimSpec " + trimSpec + "?>";
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveStringFunctionNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append(getFunctionStr());
        s.append('(');
        if (function == TRIM) {
            if (trimChar != null) {
                s.append(getTrimSpecStr());
                s.append(' ');
                s.append(trimChar);
                s.append(" FROM ");
            }
        }
        if (argList != null) {
            argList.appendList(s);
        }
        s.append(')');
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        resolve(argList, rc);
    }

}

