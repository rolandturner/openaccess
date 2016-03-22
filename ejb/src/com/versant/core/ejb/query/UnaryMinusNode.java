
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
 * Unary minus.
 */
public class UnaryMinusNode extends Node {

    private Node arg;

    public UnaryMinusNode(Node arg) {
        this.arg = arg;
    }

    public Node getArg() {
        return arg;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveUnaryMinusNode(this, msg);
    }

    public String toStringImp() {
        return "-" + arg;
    }

    public void resolve(ResolveContext rc) {
        arg.resolve(rc);
    }

}

