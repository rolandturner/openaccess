
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
 * MIN, MAX etc.
 */
public class AggregateNode extends Node {

    public static final int AVG = 1;
    public static final int MAX = 2;
    public static final int MIN = 3;
    public static final int SUM = 4;
    public static final int COUNT = 5;

    private int op;
    private boolean distinct;
    private PathNode path;

    public AggregateNode(int op, boolean distinct, PathNode path) {
        this.op = op;
        this.distinct = distinct;
        this.path = path;
    }

    public int getOp() {
        return op;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public PathNode getPath() {
        return path;
    }

    public String getOpStr() {
        switch (op) {
            case AVG:   return "AVG";
            case MAX:   return "MAX";
            case MIN:   return "MIN";
            case SUM:   return "SUM";
            case COUNT: return "COUNT";
        }
        return "<? op " + op + "?>";
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveAggregateNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append(getOpStr());
        s.append('(');
        if (distinct) {
            s.append("DISTINCT ");
        }
        s.append(path);
        s.append(')');
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        path.resolve(rc);
    }

}

