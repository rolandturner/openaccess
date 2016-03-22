
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
package com.versant.core.vds.tools.jdo;

import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.*;
import java.util.ArrayList;
/**
 *
 * JDOClassLoader
 *
 * Mar 20, 2004
 * @author ppoddar
 *
 */
public class FileURLClassLoader extends URLClassLoader {
/** Constrcuts a receiver that will use the specified classpath elements and
 * its parent classloader to load its classes.
 *
 * @param classpaths an array of strings that will be converted to file-based
 * URLs to locate classes for loading.
 * @param parent will be the parent  classloader of this receiver.
 *
 * @throws MalformedURLException if any of the given classpath elements do not
 * constitute a valid file-based URL.
 */
    public FileURLClassLoader(String[] classpaths,ClassLoader parent) throws MalformedURLException {
		super(toURL(classpaths),parent);
	}
    /** Constrcuts a receiver that will use the specified classpaths and
     * its parent classloader to load its classes.
     *
     * @param classpaths string possibly separted by <code>File.pathSeparator</code>
     * whose each element will be converted to file-based
     * URLs to locate classes for loading.
     * @param parent will be the parent  classloader of this receiver.
     *
     * @throws MalformedURLException if any of the given classpath elements do not
     * constitute a valid file-based URL.
     */
	public FileURLClassLoader(String classpath,ClassLoader parent) throws MalformedURLException {
		this(classpath.split(File.pathSeparator),parent);
	}
   private static URL[] toURL(String[] classpaths) throws MalformedURLException {
   	ArrayList result = new ArrayList();
   	for (int i=0; i<classpaths.length; i++) {
   		File file = new File(classpaths[i]);
		result.add(file.toURL());
   	}
   	return (URL[])result.toArray(new URL[classpaths.length]);
   }
   public static void main(String[] args) throws Exception {
	FileURLClassLoader cl = new FileURLClassLoader(args[0], ClassLoader.getSystemClassLoader());
	Class c = Class.forName(args[1],true,cl);
	System.err.println("Loaded " + c + " using " + args[0]);
   }
}
