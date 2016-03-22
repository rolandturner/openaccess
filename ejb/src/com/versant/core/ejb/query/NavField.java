
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

import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.ClassMetaData;

/**
 * Navigation through a field.
 */
public class NavField extends NavBase {

    private FieldMetaData fmd;  // the field being navigated
    private boolean outer;      // navigate with outer join semantics
    private boolean fetch;      // is this navigation for a fetch?

    private NavBase parent; // our parent
    private NavField next;  // our next sibling

    /**
     * This will append us to our parents list of children.
     */
    public NavField(FieldMetaData fmd, NavBase parent, boolean outer,
            boolean fetch) {
        this.fmd = fmd;
        this.outer = outer;
        this.fetch = fetch;
        setParent(parent);
    }

    /**
     * Remove us from our current parent (if any) and add us to np.
     */
    public void setParent(NavBase np) {
        if (parent == np) {
            return;
        }
        if (parent != null) {
            NavField p = null;
            for (NavField i = parent.children; i != this; i = i.next);
            if (p == null) {
                parent.children = next;
            } else {
                p.next = next;
            }
            next = null;
        }
        parent = np;
        if (parent != null) {
            if (parent.children == null) {
                parent.children = this;
            } else {
                NavField i;
                for (i = parent.children; i.next != null; i = i.next);
                i.next = this;
            }
        }
    }

    /**
     * Get the field being navigated.
     */
    public FieldMetaData getFmd() {
        return fmd;
    }

    public NavRoot getRoot() {
        return parent == null ? null : parent.getRoot();
    }

    public NavBase getParent() {
        return parent;
    }

    public NavField getNext() {
        return next;
    }

    public boolean isOuter() {
        return outer;
    }

    public boolean isFetch() {
        return fetch;
    }

    public ClassMetaData getNavClassMetaData() {
        return fmd.getRefOrValueClassMetaData();
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(fmd.name);
        childrenToString(s);
        return s.toString();
    }

}

