
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

import com.versant.core.common.Debug;

import java.io.PrintStream;

/**
 * Package element from a .jdo file.
 */
public final class JdoPackage extends JdoElement {

    public String name;
    public JdoClass[] classes;
    public JdoExtension[] extensions;
    public JdoRoot parent;

    public JdoElement getParent() { return parent; }

    /**
     * Get information for this element to be used in building up a
     * context string.
     * @see #getContext
     */
    public String getSubContext() {
        return "package[" + name + "]";
    }

    public String toString() {
        return getSubContext();
    }

    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this);
        String is = indent + "  ";
        if (classes != null) {
            for (int i = 0; i < classes.length; i++) {
                classes[i].dump(out, is);
            }
        }
        if (extensions != null) {
            for (int i = 0; i < extensions.length; i++) {
                extensions[i].dump(out, is);
            }
        }
    }

}

