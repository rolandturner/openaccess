
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
 * A literal.
 */
public class LiteralNode extends Node {

    public static final int LONG = 1;
    public static final int DOUBLE = 2;
    public static final int BOOLEAN = 3;
    public static final int STRING = 4;

    private int type;
    private long longValue;
    private double doubleValue;
    private String stringValue;
    private boolean booleanValue;

    public LiteralNode(int type, String s) {
        this.type = type;
        int len, c;
        switch (type) {
            case LONG:
                len = s.length();
                c = s.charAt(len - 1);
                if (c == 'l' || c == 'L') {
                    s = s.substring(0, len - 1);
                }
                if (s.startsWith("0x")) {
                    longValue = Long.parseLong(s.substring(2), 16);
                } else {
                    longValue = Long.parseLong(s);
                }
                break;
            case DOUBLE:
                len = s.length();
                c = s.charAt(len - 1);
                if (c == 'f' || c == 'F' || c == 'd' || c == 'D') {
                    s = s.substring(0, len - 1);
                }
                doubleValue = Double.parseDouble(s);
                break;
            case BOOLEAN:
                s = s.toUpperCase();
                booleanValue = "TRUE".equals(s);
                break;
            case STRING:
                stringValue = s.substring(1, s.length() - 1);
                break;
            default:
                throw new IllegalArgumentException("Invalid type " + type +
                        " for " + s);
        }
    }

    public int getType() {
        return type;
    }

    public long getLongValue() {
        return longValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public boolean getBooleanValue() {
        return booleanValue;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveLiteralNode(this, msg);
    }

    public String toStringImp() {
        switch (type) {
            case LONG:          return Long.toString(longValue) + "L";
            case DOUBLE:        return Double.toString(doubleValue);
            case BOOLEAN:       return booleanValue ? "TRUE" : "FALSE";
            case STRING:        return "'" + stringValue + "'";
        }
        return "<? type " + type + "?>";
    }

}

