
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
package com.versant.core.jdo.junit.test2.model.fake;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * A node in a tree. It has a set of children mapped using an unmanaged
 * one-to-many to a parent field.
 */
public class TreeNode {

    private String name;
    private Set children = new HashSet();

    public TreeNode() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set getChildren() {
        return Collections.unmodifiableSet(children);
    }

    public void addChild(TreeNode node) {
        children.add(node);
    }

    public void removeChild(TreeNode node) {
        children.remove(node);
    }

    public String toString() {
        return name;
    }

}

