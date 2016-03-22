
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
package com.versant.core.jdo.tools.workbench;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author Dirk le Roux
 * Date: Mar 17, 2004
 */
public class JarFileFilter extends FileFilter{
    public boolean accept(File f) {
        if (f.isDirectory()) return true;
        String n = f.getName();
        return n.toLowerCase().endsWith(".jar") || n.toLowerCase().endsWith(".zip");
    }
    public String getDescription() {
        return "Jar Files (*.jar, *.zip)";
    }
}
