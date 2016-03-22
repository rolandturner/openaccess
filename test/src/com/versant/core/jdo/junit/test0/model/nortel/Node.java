
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
package com.versant.core.jdo.junit.test0.model.nortel;

import java.util.Collection;
import java.util.HashMap;

public class Node {

    private String name;
    private Node parent;   // Parent node
    private HashMap children;   // sub-nodes
    private MyInterface values;

    // Default empty constructor required by JDO
    public Node() {
        name = new String("");
    }

    public Node(String value) {
        this.name = value;
    }

    public Node(String value, Node parent) {
        this.name = value;
        this.parent = parent;
        this.parent.addChild(this);

    }

    public void setValues(AbstractClass values) {
        this.values = values;
    }

    public MyInterface getValues() {
        return values;
    }

    public Object getName() {
        return name;
    }

    public String getDN() {
        if (parent != null) {
            return parent.getDN() + "." + (String) getName();
        }
        return (String) getName();
    }

    public Node getParent() {
        return parent;
    }

    public Node getChild(Object value) {
        if (null == children) return null;
        return (Node) children.get(value);
    }

    public void addChild(Node node) {
        if (null == children) {
            children = new HashMap();
        }
        children.put(node.getName(), node);

    }

    public Collection childrenKeys() {
        return children.keySet();
    }

    public String toString() {
        if (parent == null) {
            return name.toString();
        } else {
            return parent.toString() + "." + name.toString();
        }
    }
}
