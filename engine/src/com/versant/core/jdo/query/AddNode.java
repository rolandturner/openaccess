
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

import com.versant.core.common.Debug;

/**
 * An additive expression (multiple nodes separated by '+' or '-').
 */
public class AddNode extends Node {

    public static final int OP_PLUS = 0;
    public static final int OP_MINUS = 1;

    /**
     * The operators. There will be one less entry here than the nodes.
     * Example: ops[0] is between nodes[0] and nodes[1].
     */
    public int[] ops;

    public AddNode() {
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
      return visitor.visitAddNode(this, results);
    }

    /**
     * Dump debugging info to System.out.
     */
    public void dump(String indent) {
        dumpThis(indent);
        indent = indent + "  ";
        int i = 0;
        for (Node c = childList; c != null; c = c.next, i++) {
            c.dump(indent);
            if (i < ops.length) {
                Debug.OUT.println(indent + toOpString(ops[i]));
            }
        }
    }

    public static String toOpString(int op) {
        switch (op) {
            case OP_PLUS:   return "+";
            case OP_MINUS:  return "-";
        }
        return "Uknown(" + op + ")";
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitAddNode(this,obj);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveAddNode(this, msg);
    }

}
