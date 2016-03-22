
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

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.common.CmdBitSet;

/**
 * Base class for Nav and NavRoot holding the children list and associated
 * methods.
 */
public abstract class NavBase {

    /**
     * Fields navigated from us.
     */
    protected NavField children;

    /**
     * Store specific info attached to us.
     */
    public Object storeObject;

    public NavBase() {
    }

    /**
     * Field the child with name and matching outer and fetch or null if none.
     */
    public NavField findChild(String name, boolean outer, boolean fetch) {
        NavField i;
        for (i = children; i != null; i = i.getNext()) {
            if (i.getFmd().name.equals(name) && i.isOuter() == outer
                    && i.isFetch() == fetch) {
                return i;
            }
        }
        return null;
    }

    /**
     * Get our root.
     */
    public abstract NavRoot getRoot();

    /**
     * Get the meta data used to resolve navigation from us in paths.
     */
    public abstract ClassMetaData getNavClassMetaData();

    /**
     * Convert our children into a nice String.
     */
    protected void childrenToString(StringBuffer s) {
        if (children != null) {
            if (children.getNext() == null) {
                s.append(" -> ");
                s.append(children);
            } else {
                for (NavField f = children; f != null; f = f.getNext()) {
                    s.append(" [ -> ");
                    s.append(children);
                    s.append("] ");
                }
            }
        }
    }

    /**
     * Add all classes referenced by this node and its children recursivley
     * to bits. This is used to figure out class dependencies for flushing
     * and eviction.
     */
    public void addInvolvedClasses(CmdBitSet bits) {
        bits.addPlus(getNavClassMetaData());
        for (NavField i = children; i != null; i = i.getNext()) {
            i.addInvolvedClasses(bits);    
        }
    }

    public NavField getChildren() {
        return children;
    }

}

