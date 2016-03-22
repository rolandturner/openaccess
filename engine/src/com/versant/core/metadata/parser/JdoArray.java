
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

import java.io.PrintStream;
import java.util.Collections;

/**
 * Array element from a .jdo file.
 */
public final class JdoArray extends JdoElement {

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
        return "array";
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("array embeddedElement=");
        s.append(MDStaticUtils.toTriStateString(embeddedElement));
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

    public void synchronizeForHorizontal(JdoArray array) {
        if (array.extensions != null) {
            JdoExtension[] copy = new JdoExtension[array.extensions.length];
            for (int i = 0; i < array.extensions.length; i++) {
                copy[i] = array.extensions[i].createCopy(this);
            }
            if (extensions != null) {
                JdoExtension.synchronize3(extensions, copy, Collections.EMPTY_SET, false);
            } else {
                extensions = copy;
            }
        }
    }

    public JdoArray createCopy(JdoField jdoField) {
        JdoArray tmp = new JdoArray();
        tmp.parent = jdoField;
        tmp.embeddedElement = embeddedElement;

        if (extensions != null) {
            tmp.extensions = new JdoExtension[extensions.length];
            for (int i = 0; i < extensions.length; i++) {
                tmp.extensions[i] = extensions[i].createCopy(tmp);
            }
        }
        return tmp;
    }
}

