
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
 * AND.
 */
public class AndNode extends Node {

    private Node argsList;

    public AndNode(Node argsList) {
        this.argsList = argsList;
    }

    public Node getArgsList() {
        return argsList;
    }

    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveAndNode(this, msg);
    }

    public String toStringImp() {
        StringBuffer s = new StringBuffer();
        s.append(argsList);
        if (argsList != null) {
            for (Node e = argsList.getNext(); e != null; e = e.getNext()) {
                s.append(" AND ");
                s.append(e);
            }
        }
        return s.toString();
    }

    public void resolve(ResolveContext rc) {
        resolve(argsList, rc);
    }

}

