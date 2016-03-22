
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

import com.versant.core.common.BindingSupportImpl;

/**
 * A method call.
 */
public class MethodNode extends Node {

    public Node args;
    private int method;

    public static final int STARTS_WITH = 1;
    public static final int ENDS_WITH = 2;
    public static final int CONTAINS = 3;
    public static final int IS_EMPTY = 4;
    public static final int CONTAINS_KEY = 5;
    public static final int TO_LOWER_CASE = 6;
    public static final int SQL = 7;
    public static final int CONTAINS_PARAM = 8;

    public MethodNode() {
    }

    public MethodNode( int method ) {
		this.method = method;
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
      return visitor.visitMethodNode(this, results);
    }

    public String getName() {
        switch (method) {
            case STARTS_WITH:   return "startsWith";
            case ENDS_WITH:     return "endsWith";
            case CONTAINS:      return "contains";
            case IS_EMPTY:      return "isEmpty";
            case CONTAINS_KEY:  return "containsKey";
            case TO_LOWER_CASE: return "toLowerCase";
            case SQL:           return "sql";
        }
        return "Unknown(" + method + ")";
    }

    public int getMethod() {
        return method;
    }

    public void setName(String name) {
        if (name.equals("startsWith")) method = STARTS_WITH;
        else if (name.equals("endsWith")) method = ENDS_WITH;
        else if (name.equals("contains")) method = CONTAINS;
        else if (name.equals("isEmpty")) method = IS_EMPTY;
        else if (name.equals("containsKey")) method = CONTAINS_KEY;
        else if (name.equals("toLowerCase")) method = TO_LOWER_CASE;
        else if (name.equals("sql")) method = SQL;
        else throw BindingSupportImpl.getInstance().invalidOperation("Invalid method name: '" + name + "'");
    }

    public String toString() {
        return super.toString() + " : " + getName();
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitMethodNode(this, obj);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveMethodNode(this, msg);
    }

}

