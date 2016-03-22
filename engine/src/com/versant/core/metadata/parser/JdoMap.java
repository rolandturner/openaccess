
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
 * Map element from a .jdo file.
 */
public final class JdoMap extends JdoElement {

    public String keyType;
    public int embeddedKey;
    public String valueType;
    public int embeddedValue;
    public JdoExtension[] extensions;
    public JdoField parent;

    public JdoElement getParent() { return parent; }

    /**
     * Get information for this element to be used in building up a
     * context string.
     * @see #getContext
     */
    public String getSubContext() {
        return "map";
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("map keyType=");
        s.append(keyType);
        s.append(" embeddedKey=");
        s.append(MDStaticUtils.toTriStateString(embeddedKey));
        s.append(" valueType=");
        s.append(valueType);
        s.append(" embeddedValue=");
        s.append(MDStaticUtils.toTriStateString(embeddedValue));
        return s.toString();
    }

    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this);
        String is = indent + "  ";
        if (extensions != null) {
            for (int i = 0; i < extensions.length; i++) {
                extensions[i].dump(out, is);
            }
        }
    }

    /**
     * Get the fully qualified name of our value type (or null if none).
     */
    public String getValueTypeQName() {
        return getQName(valueType);
    }

    /**
     * Get the fully qualified name of our key type (or null if none).
     */
    public String getKeyTypeQName() {
        return getQName(keyType);
    }

    private String getQName(String n) {
        if (n == null) return null;
        int i = n.indexOf('.');
        if (i >= 0) return n;
        String packageName = parent.parent.parent.name;
        if (packageName.length() == 0) return n;
        return packageName + '.' + n;
    }

    public JdoMap createCopy(JdoField field) {
        JdoMap tmp = new JdoMap();
        tmp.parent = field;
        tmp.keyType = keyType;
        tmp.embeddedKey = embeddedKey;
        tmp.valueType = valueType;
        tmp.embeddedValue = embeddedValue;

        if (extensions != null) {
            tmp.extensions = new JdoExtension[extensions.length];
            for (int i = 0; i < extensions.length; i++) {
                tmp.extensions[i] = extensions[i].createCopy(tmp);
            }
        }
        return tmp;
    }

    /**
     * May not update key of value types. Only columns may be renamed.
     */
    public void synchronizeForHorizontal(JdoMap map) {
        if (!valueType.equals(map.valueType) || !keyType.equals(map.keyType)) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "May not override the key of value type of map. " +
                    "\nUpdated: " + this + "\nOriginal: " + map);
        }

        if (map.extensions != null) {
            JdoExtension[] copy = new JdoExtension[map.extensions.length];
            for (int i = 0; i < map.extensions.length; i++) {
                copy[i] = map.extensions[i].createCopy(this);
            }
            if (extensions != null) {
                JdoExtension.synchronize3(extensions, copy, Collections.EMPTY_SET, false);
            } else {
                extensions = copy;
            }
        }
    }
}

