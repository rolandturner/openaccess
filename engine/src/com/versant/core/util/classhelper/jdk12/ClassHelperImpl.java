
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
package com.versant.core.util.classhelper.jdk12;

public class ClassHelperImpl 
	extends com.versant.core.util.classhelper.ClassHelper {

	public Class classForName(String clazz, boolean validate,
							  ClassLoader loader) throws ClassNotFoundException
	{
		return Class.forName(clazz, validate, loader);
	}

	public ClassLoader getContextClassLoader(Thread thread)
	{
		return thread.getContextClassLoader();
	}
	public ClassLoader getSystemClassLoader()
	{
		return ClassLoader.getSystemClassLoader();
	}

	public void setAccessible(java.lang.reflect.Field field, boolean value)
	{
		field.setAccessible(value);
	}

	public void setAccessible(java.lang.reflect.Constructor ctor, boolean value)
	{
		ctor.setAccessible(value);
	}

	public Object getFieldValue(java.lang.reflect.Field field, Object obj)
		throws IllegalAccessException 
	{
		return field.get(obj);
	}
}
