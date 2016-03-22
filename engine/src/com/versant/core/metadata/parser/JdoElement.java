
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
package com.versant.core.metadata.parser;

import com.versant.core.metadata.MDStatics;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Base class for elements in the tree.
 */
public abstract class JdoElement implements Serializable, MDStatics {

    public abstract JdoElement getParent();

    /**
     * Get nicely formatted context information for an error message or
     * debugging. This follows the parent chain.
     */
    public String getContext() {
        ArrayList a = new ArrayList();
        for (JdoElement e = this; e != null; e = e.getParent()) a.add(e);
        StringBuffer s = new StringBuffer();
        boolean first = true;
        for (int i = a.size() - 1; i >= 0; i--) {
            if (first) {
                first = false;
                s.append("--> ");
            } else s.append('/');
            s.append(((JdoElement)a.get(i)).getSubContext());
        }
        return s.toString();
    }

    /**
     * Get information for this element to be used in building up a
     * context string.
     * @see #getContext
     */
    public abstract String getSubContext();

    public JdoExtension[] addExtension(JdoExtension[] exts, JdoExtension e) {
        JdoExtension[] tmp = new JdoExtension[exts.length + 1];
        System.arraycopy(exts, 0, tmp, 0, exts.length);
        tmp[exts.length] = e;
        return tmp;
    }

    public JdoExtension createChild(int key, String value, JdoElement parent) {
        JdoExtension e = new JdoExtension();
        e.key = key;
        e.value = value;
        e.parent = parent;
        return e;
    }

}

