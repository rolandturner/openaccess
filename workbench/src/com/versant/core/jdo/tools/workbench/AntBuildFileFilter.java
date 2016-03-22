
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
 * FileFilter that shows Ant .xml files and directories.
 * @keep-all
 */
public class AntBuildFileFilter extends FileFilter {

    public static final String EXT = ".xml";

    public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(EXT);
    }

    public String getDescription() {
        return "Ant Build Files (*" + EXT + ")";
    }
}

 
