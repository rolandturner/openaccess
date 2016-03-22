
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
/*
 * Created on Jul 15, 2004
 *
 * Copyright Versant Corporation 2003-2005, All rights reserved
 */

package com.versant.core.vds.tools.jdo;

import java.io.File;
import java.util.*;
import java.io.*;

import com.versant.core.vds.util.ConnectionURL;
import com.versant.core.vds.VdsConfig;
import com.versant.core.common.Debug;
import com.versant.odbms.DatastoreManager;

/**
 * AbstractJdoTool
 *
 * @author zyue
 *
 */
public abstract class AbstractJdoTool extends com.versant.core.vds.tools.AbstractTool {

	/** Loads a property file. If the file is directly loadable then the properties
	 * are read from the file. Otherwise, the file is located in the classpath
	 * of the current thread's context.
	 * <p>
	 * Finally, all the loaded properties are overridden by the corresponding
	 * System property, if any.
	 *
	 * @param propertiesFileName Name of the properties file. Null is not allowed.
	 * @return null if file can not be loaded or propertiesFileName is null.
	 * @throws IOException
	 */
	public Properties loadProperties(String propertiesFileName) throws Exception {
//		assert propertiesFileName!=null;
        if (Debug.DEBUG) {
            Debug.assertInternal(propertiesFileName != null,
                    "Properties file name is null");
        }
		Properties p = new Properties();
		File file = new File(propertiesFileName);
		String fileName = propertiesFileName;
		if (file.exists()) {
			fileName = file.getAbsolutePath();
			log("loading configuration from file " + fileName);
			p.load(new FileInputStream(propertiesFileName));
		} else {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			log("loading configuration ["+ propertiesFileName + "] from classpath ");
			InputStream	is = cl.getResourceAsStream(propertiesFileName);
			if (is==null)
			throw new RuntimeException(propertiesFileName + " not available in current classpath\r\n"
			   + splitString(System.getProperty("java.class.path"),File.pathSeparator));
			p.load(is);
		}
		overrideBySystemProperty(p);
		//p.store(System.out,"======= JDO Configuration ["+fileName+"] ===========");
		return p;
	}

	protected void overrideBySystemProperty(Properties p) {
		Enumeration names = p.propertyNames();
		while (names.hasMoreElements()) {
			String key = (String) names.nextElement();
			if (System.getProperty(key) != null) {
				p.setProperty(System.getProperty(key), System.getProperty(key));
			}
		}
	}

	protected String splitString(String s, String separator) {
		String[] parts = s.split(separator);
		StringBuffer tmp = new StringBuffer();
		for (int i = 0; i < parts.length; i++)
			tmp.append(parts[i]).append("\r\n");
		return tmp.toString();
	}
	protected void log(String s) {
		System.out.println(s);
	}
	protected void output( String s) {
		System.out.println(s);
	}
}
