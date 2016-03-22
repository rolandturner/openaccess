
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
 * What is this for?
 */
public class ArgNode extends Node {

    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitArgNode(this, obj);
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveArgNode(this, msg);
    }
}
