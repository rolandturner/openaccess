
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
package javax.persistence;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Implements the <code>FilenameFilter</code> for finding par files.
 * 
 * @author Rick George
 * @version 1.0, 8/15/05
 * @see java.io.File
 * @see java.io.File#list(java.io.FilenameFilter)
 */
class ParFilter implements FilenameFilter {
    /**
     * Tests if a file is a par file to be included in a file list.
     * @param   dir    the directory in which the file was found.
     * @param   name   the name of the file.
     * @return  <code>true</code> if and only if the name should be
     * included in the file list; <code>false</code> otherwise.
     */
	public boolean accept(File dir, String name) {
		return name.endsWith(".par");
	}
}
