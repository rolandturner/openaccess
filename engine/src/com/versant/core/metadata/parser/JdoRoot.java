
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
 * Root element of a .jdo file.
 */
public final class JdoRoot extends JdoElement {

    public String name;
    public JdoPackage[] packages;

    public JdoElement getParent() { return null; }

    public String getSubContext() {
        return name;
    }

    public String toString() {
        return getSubContext();
    }

    /**
     * Does this file contain only meta data for queries?
     */
    public boolean isQueryMetaData() {
        return name.endsWith(".jdoquery");
    }

    public void dump() {
        dump(Debug.OUT);
    }

    public void dump(PrintStream out) {
        out.println(this);
        for (int i = 0; i < packages.length; i++) {
            packages[i].dump(out, "  ");
        }
    }

}
