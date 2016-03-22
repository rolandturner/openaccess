
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
package com.versant.core.jdo.tools.enhancer.utils;

import java.util.*;
import java.io.*;

import com.versant.lib.bcel.classfile.*;
import com.versant.lib.bcel.generic.*;
import com.versant.lib.bcel.Constants;
import com.versant.lib.bcel.*;
import com.versant.lib.bcel.Repository;

/**
 * The FileInfoUtil class is a helper class that gets the location of a class file on
 * the file system
 *
 */
public class FileInfoUtil {

	private ArrayList list = new ArrayList();
	private ArrayList allClassesList;
	private String fileSeparator;
	private char charfileSeparator;

	public FileInfoUtil() {
		String paths = System.getProperty("java.class.path");
		String pathSeparator = System.getProperty("path.separator");
		fileSeparator = System.getProperty("file.separator");
		charfileSeparator = fileSeparator.charAt(0);
		StringTokenizer to = new StringTokenizer(paths,pathSeparator,false);
		while (to.hasMoreElements()){
			String path = (String)to.nextElement();
			if (!path.endsWith(".jar")){
				list.add(path);
			}
		}
	}

    public void setPath(String[] paths){
        for (int i = 0; i < paths.length; i++) {
            list.add(paths[i]);
        }
    }

	public List getAllFiles(){
	    Iterator iter = list.iterator();
		allClassesList = new ArrayList();
		while (iter.hasNext()){
		    String path = (String)iter.next();
			File file = new File(path);
			addFilesToList(file);
		}
		return allClassesList;
	}

	private void addFilesToList(File file){
	    if (file.isFile()){
			if (file.getName().endsWith(".class")){
			    allClassesList.add(file);
	        }
	    } else {
		    File [] files = file.listFiles();
			for (int i = 0;i < files.length ;i++){
				addFilesToList(files[i]);
			}
	    }
	}

	/**
	 * Returns a File object representing the location of a class file on the
	 * file system for a given class name.
	 *
	 * @param fullClassName in format ("com.xyz.hr.Employee").
	 * @return File object of where class file is on file system.
	 * @throws FileNotFoundException if the class file could not be found
	 */
	public File getClassFile(String fullClassName)throws FileNotFoundException{
        String paths = System.getProperty("java.class.path");
		String partFileName = fullClassName.replace('.',charfileSeparator);
		String partFile = fileSeparator+partFileName+".class";
		Iterator iter = list.iterator();
		while (iter.hasNext()){
			String testPath = (String)iter.next();
			File testFile = new File(testPath+partFile);
			if (testFile.exists()){
				return testFile;
			}
		}

		throw new FileNotFoundException("The following class was not found on class path : "+
			fullClassName+"\nCommon error is that your class path was not set properly");
	}

	/**
	 * Returns a File object representing the location of a resource file on the
	 * file system for a given resource name.
	 *
	 * @param resourceFile in format ("za.jdo").
	 * @return File object of where resource file is on file system.
	 * @throws FileNotFoundException if the resource file could not be found
	 */
	public File getResourceFile(String resourceFile)throws FileNotFoundException{

		String partFile = fileSeparator+resourceFile;
		Iterator iter = list.iterator();
		while (iter.hasNext()){
			String testPath = (String)iter.next();
			File testFile = new File(testPath+partFile);
			if (testFile.exists()){
				return testFile;
			}
		}



		throw new FileNotFoundException("The following resource was not found on class path : "+
			resourceFile+"\nCommon error is that your class path was not set properly");
	}
	public boolean classExists(String clazz) throws ClassNotFoundException {
		JavaClass javaClass = Repository.lookupClass(clazz);
		if (javaClass != null)return true;
		return false;

	}

}
