
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

import com.versant.lib.bcel.generic.InstructionHandle;

/**
 *
 */
public class TableSwitchHelper implements Comparable{
	public int match;
	public InstructionHandle target;
	/**
	 * Implements Comparable to order all fields by name
	 *
	 */
	public int compareTo(Object o){
		TableSwitchHelper other = (TableSwitchHelper)o;
		if (match == other.match){
		    return 0;
		} else if (match < other.match){
			return -1;
		} else {
		    return 1;
		}
	}

	public boolean equals(Object other){
		if (other != null && getClass() == other.getClass()){
			TableSwitchHelper otherTableSwitchHelper = (TableSwitchHelper)other;
			return match == otherTableSwitchHelper.match;
		} else {
		    return false;
		}
	}

	public int hashCode(){
		Integer i = new Integer(match);
	    return 13 * i.hashCode();
	}
}
