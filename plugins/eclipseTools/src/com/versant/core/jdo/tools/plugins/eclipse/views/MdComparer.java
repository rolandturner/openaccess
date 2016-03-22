
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
 * Created on Feb 15, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.versant.core.jdo.tools.plugins.eclipse.views;

import org.eclipse.jface.viewers.IElementComparer;

/**
 * @author dirk
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MdComparer implements IElementComparer {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IElementComparer#equals(java.lang.Object, java.lang.Object)
	 */
	public boolean equals(Object a, Object b) {
		Class classA = a.getClass();
		Class classB = b.getClass();
		if(!classA.equals(classB)){
			return false;
		}
		if(a instanceof Comparable){
			Comparable ca = (Comparable)a;
			return ca.compareTo(b) == 0;
		}else if(a instanceof Comparable){
			Comparable cb = (Comparable)b;
			return cb.compareTo(a) == 0;
		}else{
			return a.equals(b);
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IElementComparer#hashCode(java.lang.Object)
	 */
	public int hashCode(Object element) {
		return element.hashCode();
	}
}
