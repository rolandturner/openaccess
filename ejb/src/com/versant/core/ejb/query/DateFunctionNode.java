
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
 * Functions that return date/time.
 */
public class DateFunctionNode extends Node {

    public static final int CURRENT_DATE = 1;
    public static final int CURRENT_TIME = 2;
    public static final int CURRENT_TIMESTAMP = 3;

    private int function;

    public DateFunctionNode(int function) {
        this.function = function;
    }

    public int getFunction() {
        return function;
    }

    public String getFunctionStr() {
        switch (function) {
            case CURRENT_DATE:        return "CURRENT_DATE";
            case CURRENT_TIME:        return "CURRENT_TIME";
            case CURRENT_TIMESTAMP:   return "CURRENT_TIMESTAMP";
        }
        return "<? function " + function + " ?>";
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveDateFunctionNode(this, msg);
    }

    public String toStringImp() {
        return getFunctionStr();
    }

}

