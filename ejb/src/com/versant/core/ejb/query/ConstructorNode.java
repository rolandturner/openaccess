
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
 * NEW constructor_name(arg, ... arg).
 */
public class ConstructorNode extends Node {

    private String name;
    private Node argsList;

    public ConstructorNode(String name, Node argsList) {
        this.name = name;
        this.argsList = argsList;
    }

    public String getName() {
        return name;
    }

    public Node getArgsList() {
        return argsList;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveConstructorNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append("NEW ");
        s.append(name);
        s.append('(');
        s.append(argsList);
        for (Node e = argsList.getNext(); e != null; e = e.getNext()) {
            s.append(", ");
            s.append(e);
        }
        s.append(')');
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        resolve(argsList, rc);
    }

}

