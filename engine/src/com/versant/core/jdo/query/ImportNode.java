
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
 * An import declaration for a query.
 */
public class ImportNode extends LeafNode {

    public String name;
    public boolean all;

    public ImportNode() {
    }

    public String toString() {
        return super.toString() + ": " + name + (all ? "*" : "");
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitImportNode(this, obj);
	}

    public void dump(String indent) {
        Debug.OUT.println(indent + this);
    }

    public String getClassName() {
        if (all) return null;
        int i = name.lastIndexOf('.');
        if (i >= 0) return name.substring(i + 1);
        return name;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveImportNode(this, msg);
    }

}
