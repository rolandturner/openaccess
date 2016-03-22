
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
package com.versant.core.jdo.tools.enhancer;

import java.io.*;
import java.util.*;


public class GrepFile {
    private FileFilter fileFilter;

    private HashMap jdoFiles = new HashMap();


	public GrepFile(ArrayList dirs) throws IOException{
		for (Iterator iterator = dirs.iterator();iterator.hasNext();) {
			File file = (File) iterator.next();
			searchInDir(file.toString(),true);
		}
	}

    public Set getJdoFiles(){
        return jdoFiles.keySet();
    }



    private void searchInDir(String startDir, boolean recurse) throws IOException {
        fileFilter = new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()){
                    return true;
                } else if (f.getName().endsWith(".class")){
                    return true;
                } else {
                    return false;
                }
            }
        };

        searchInDir_aux(new File(startDir), recurse);
    }

    private void searchInDir_aux(File dir, boolean recurse) throws IOException {
        File[] contents = dir.listFiles(fileFilter);
        for (int i=0; i<contents.length; i++) {
            File fileToCheck = contents[i];
	        if (fileToCheck.toString().endsWith(".class")){
	            jdoFiles.put(fileToCheck.toString(),"");
            } else if (recurse) {
                searchInDir_aux(fileToCheck, recurse);
            }
        }
    }
}






