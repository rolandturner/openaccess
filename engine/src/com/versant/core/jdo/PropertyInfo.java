
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
package com.versant.core.jdo;

import java.io.Serializable;

/**
 * Serializable information about a property. For properties that are
 * configurable beans themselves the type will be TYPE_SERVER_BEAN and
 * the children array will be filled in. The value for these is a bean
 * defined status object. It should have a useful but short toString().
 */
public class PropertyInfo implements Serializable, Comparable {

    public static final int TYPE_SERVER_BEAN = 1;
    public static final int TYPE_INT = 2;
    public static final int TYPE_STRING = 3;
    public static final int TYPE_BOOLEAN = 4;

    private String name;
    private int type;
    private String displayName;
    private String description;
    private Object value;
    private PropertyInfo[] children;

    public PropertyInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public PropertyInfo[] getChildren() {
        return children;
    }

    public void setChildren(PropertyInfo[] children) {
        this.children = children;
    }

    /**
     * Order by name.
     */
    public int compareTo(Object o) {
        return name.compareTo(((PropertyInfo)o).name);
    }

    public String toString() {
        if (type == TYPE_SERVER_BEAN) {
            return displayName;
        }
        return displayName + ": " + value;
    }

    /**
     * Dump to System.out.
     */
    public void dump(String indent) {
        System.out.println(indent + this);
        if (type == TYPE_SERVER_BEAN) {
            indent = indent + "  ";
            int n = children.length;
            for (int i = 0; i < n; i++) children[i].dump(indent);
        }
    }

    /**
     * Find the child with name or null if none.
     */
    public PropertyInfo findChild(String name) {
        if (children == null) return null;
        for (int i = children.length - 1; i >= 0; i--) {
            PropertyInfo child = children[i];
            if (child.name.equals(name)) return child;
        }
        return null;
    }
}
