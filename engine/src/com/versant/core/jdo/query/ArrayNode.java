
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
 * Dont't know what this is for.
 */
public class ArrayNode extends Node {

    public Node args;
    public String name;

    public ArrayNode() {
    }

    public void dump(String indent) {
        super.dump(indent);
        if(name != null) { Debug.OUT.println(indent+ "name = "+name); }
        else{Debug.OUT.println(indent+ "no_name"); }
        if(args != null ) { args.dump(indent+" "); }
    }

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitArrayNode(this, obj);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveArrayNode(this, msg);
    }
}
