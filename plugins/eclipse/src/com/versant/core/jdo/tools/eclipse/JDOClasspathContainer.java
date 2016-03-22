
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
package com.versant.core.jdo.tools.eclipse;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * A classpath container that contains all the nec. jdo genie jars.
 * 
 */
public class JDOClasspathContainer implements IClasspathContainer {
    private IClasspathEntry[] entries;

    public JDOClasspathContainer(IClasspathEntry[] entries) {
        this.entries = entries;
    }

    public IClasspathEntry[] getClasspathEntries() {
        return entries;
    }

    public String getDescription() {
        return "Versant OpenAccess Libs";
    }

    public int getKind() {
        return K_APPLICATION;
    }

    public IPath getPath() {
        return new Path("JDO_CONTAINER");
    }

}
