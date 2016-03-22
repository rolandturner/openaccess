
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
package com.versant.core.util.classhelper;

import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

public abstract class ClassHelper {



	private static	ClassHelper	impl = null;

	protected ClassHelper() {}

	public static ClassHelper get() {
		if(impl == null)
		{
			
			impl = new com.versant.core.util.classhelper.jdk12.ClassHelperImpl();
			

		}

		return impl;
	}



	public abstract Class classForName(String clazz, boolean validate,
									   ClassLoader loader)
		throws ClassNotFoundException;

	public abstract ClassLoader getContextClassLoader(Thread thread);

	public abstract ClassLoader getSystemClassLoader();

	public abstract void setAccessible(Field field, boolean value);

	public abstract void setAccessible(Constructor ctor, boolean value);

	public abstract Object getFieldValue(Field field, Object obj) throws IllegalAccessException;
}
