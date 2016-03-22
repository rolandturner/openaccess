
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
 * Not sure what this is for.
 */
public class AsValueNode extends LeafNode {

    public String value;

    public AsValueNode(String value) {
        this.value = value;
    }

    public Object accept(NodeVisitor visitor, Object[] results) {
        return visitor.visitAsValueNode(this, results);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveAsValueNode(this, msg);
    }
}
