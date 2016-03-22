
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

import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.common.Debug;
import com.versant.core.common.BindingSupportImpl;

import java.io.PrintStream;
import java.util.Collections;

/**
 * Collection element from a .jdo file.
 */
public final class JdoCollection extends JdoElement {

    public String elementType;
    public int embeddedElement;
    public JdoExtension[] extensions;
    public JdoField parent;

    public JdoElement getParent() { return parent; }

    /**
     * Get information for this element to be used in building up a
     * context string.
     * @see #getContext
     */
    public String getSubContext() {
        return "collection";
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("collection elementType=");
        s.append(elementType);
        s.append(" embeddedElement=");
        s.append(MDStaticUtils.toTriStateString(embeddedElement));
        return s.toString();
    }

    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this);
        if (extensions != null) {
            for (int i = 0; i < extensions.length; i++) {
                extensions[i].dump(out, indent + "  ");
            }
        }
    }

    /**
     * Get the fully qualified name of our element type (or null if none).
     */
    public String getElementTypeQName() {
        if (elementType == null) return null;
        int i = elementType.indexOf('.');
        if (i >= 0) return elementType;
        String packageName = parent.parent.parent.name;
        if (packageName.length() == 0) return elementType;
        return packageName + '.' + elementType;
    }

    public JdoCollection createCopy(JdoField parent) {
        JdoCollection tmp = new JdoCollection();
        tmp.parent = parent;
        tmp.elementType = elementType;
        tmp.embeddedElement = embeddedElement;

        if (extensions != null) {
            tmp.extensions = new JdoExtension[extensions.length];
            for (int i = 0; i < extensions.length; i++) {
                tmp.extensions[i] = extensions[i].createCopy(tmp);
            }
        }
        return tmp;
    }

    /**
     *
     */
    public void synchronizeForHorizontal(JdoCollection col) {
        if (!elementType.equals(col.elementType)) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "May not override the element type of collection. " +
                    "\nAttemptin to change 'element-type' from "
                    + col.elementType + " to " + elementType);
        }

        if (col.extensions != null) {
            JdoExtension[] copy = new JdoExtension[col.extensions.length];
            for (int i = 0; i < col.extensions.length; i++) {
                copy[i] = col.extensions[i].createCopy(this);
            }
            if (extensions != null) {
                JdoExtension.synchronize3(extensions, copy, Collections.EMPTY_SET, false);
            } else {
                extensions = copy;
            }
        }
    }

    /**
     * Create the extension if it does not exist, else update it if overwrite is true
     * @param key
     * @param value
     * @param overwrite
     */
    public JdoExtension findCreate(int key, String value, boolean overwrite) {
        if (extensions == null) {
            extensions = new JdoExtension[] {createChild(key, value, this)};
            return extensions[0];
        }

        JdoExtension ext = JdoExtension.find(key, extensions);
        if (ext == null) {
            extensions = addExtension(extensions, (ext = createChild(key, value, this)));
        } else if (overwrite) {
            ext.value = value;
        }
        return ext;
    }
}

