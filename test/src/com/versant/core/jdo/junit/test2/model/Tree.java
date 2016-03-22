
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
package com.versant.core.jdo.junit.test2.model;

import javax.jdo.PersistenceManager;
import javax.jdo.JDOHelper;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Tree structure for testing recursive relationships and the DFG.
 * @keep-all
 */
public class Tree {

    private String name;
    private Tree parent = null;               // in dfg
    private List children = new ArrayList();  // inverse Tree.parent, in dfg

    public Tree(String name) {
        this.name = name;
    }

    public List getChildren() {
        return new ArrayList(children);
    }

    public void addChild(Tree c) {
        children.add(c);
    }

    public void removeChild(Tree c) {
        children.remove(c);
    }

    public String getName() {
        return name;
    }

    public Tree getParent() {
        return parent;
    }

    public String toString() {
        return name + children;
    }

    public void nuke() {
        for (Iterator i = children.iterator(); i.hasNext(); ) {
            ((Tree)i.next()).nuke();
        }
        PersistenceManager pm = JDOHelper.getPersistenceManager(this);
        pm.deletePersistent(this);
    }

}

 
