
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
 * This is created for repeat usages of a variable in an expression. It
 * delegates method calls back to the original VarNode.
 */
public class VarNodeProxy extends LeafNode implements VarNodeIF {

    private VarNode varNode;

    public VarNodeProxy(VarNode v) {
        this.varNode = v;
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
      return visitor.visitVarNode(this.varNode, results);
    }

    public VarNode getVarNode() {
        return varNode;
    }

    public String toString() {
        return super.toString() + " for " + varNode;
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitVarNodeProxy(this, obj);
    }

    public String getType() {
        return varNode.getType();
    }

    public String getIdentifier() {
        return varNode.getIdentifier();
    }

    public Class getCls() {
        return varNode.getCls();
    }

    public void setUsedInProjection(boolean usedInProjection) {
        varNode.setUsedInProjection(usedInProjection);
    }

    public boolean isUsedInProjection() {
        return varNode.isUsedInProjection();
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveVarNodeProxy(this, msg);
    }

}
