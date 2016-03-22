
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

import com.versant.core.metadata.ClassMetaData;

/**
 * This is created for repeat usages of a parameter in an expression. It
 * delegates method calls back to the original ParamNode.
 */
public class ParamNodeProxy extends LeafNode {

    private ParamNode paramNode;

    public ParamNodeProxy(ParamNode p) {
        this.paramNode = p;
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
      return visitor.visitParamNode(this.paramNode, results);
    }

    public ParamNode getParamNode() {
        return paramNode;
    }

    public String toString() {
        return super.toString() + " for " + paramNode;
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitParamNodeProxy(this, obj);
    }

    public String getType() {
        return paramNode.getType();
    }

    public String getIdentifier() {
        return paramNode.getIdentifier();
    }

    public Class getCls() {
        return paramNode.getCls();
    }

    public ClassMetaData getCmd() {
        return paramNode.getCmd();
    }

    public Object getValue() {
        return paramNode.getValue();
    }

    public int getIndex() {
        return paramNode.getIndex();
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveParamNodeProxy(this, msg);
    }

}

