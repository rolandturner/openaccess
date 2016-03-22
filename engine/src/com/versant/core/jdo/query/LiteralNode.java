
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
 * A literal e.g 5 or "abc".
 */
public class LiteralNode extends LeafNode {

    public static final int TYPE_STRING = 1;
    public static final int TYPE_OTHER = 2;

    public static final int TYPE_BOOLEAN = 3;
    public static final int TYPE_CHAR =  4;
    public static final int TYPE_LONG = 5;
    public static final int TYPE_DOUBLE = 7;

    public static final int TYPE_NULL = 8;

    /**
     * Type of literal.
     */
    public int type;
    /**
     * Value of literal.
     */
    public String value;

    public LiteralNode(Node parent, int type, String value) {
        this.parent = parent;
        this.type = type;
        this.value = value;
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
      return visitor.visitLiteralNode(this, results);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(super.toString());
        s.append(' ');
        s.append(value);
        s.append(' ');
        s.append(toTypeStr(type));
        return s.toString();
    }

    private static String toTypeStr(int t) {
        switch (t) {
            case TYPE_STRING:   return "STRING";
            case TYPE_OTHER:    return "OTHER";
            case TYPE_BOOLEAN:  return "BOOLEAN";
            case TYPE_CHAR:     return "CHAR";
            case TYPE_LONG:     return "LONG";
            case TYPE_DOUBLE:   return "DOUBLE";
            case TYPE_NULL:     return "NULL";
        }
        return "UNKNOWN(" + t + ")";
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitLiteralNode(this, obj);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveLiteralNode(this, msg);
    }

}

