
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

import com.versant.core.common.CmdBitSet;

/**
 * This Node is added to the tree to bind 'unbound' variables to the extent
 * of the variables class.
 */
public class VarBindingNode extends LeafNode {

    private final VarNode var;

    public VarBindingNode(VarNode var) {
        this.var = var;
    }

    public VarNode getVar() {
        return var;
    }

    public String toString() {
        return super.toString() + " " + var;
    }

    public void updateEvictionDependency(CmdBitSet bitSet) {
        var.updateEvictionDependency(bitSet);
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
        return visitor.visitVarBindingNode(this, results);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveVarBindingNode(this, msg);
    }

}
