
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
package com.versant.core.jdo.junit.test0.model.beta;

public class TopObj extends BottomObj{

	transient String comeBack;

	/**
	 * @param _v
	 */
	public TopObj(String _v) {
		super(_v);
		// TODO Auto-generated constructor stub
	}

	BottomObj one;

	/**
	 * @return
	 */
	public BottomObj getOne() {
		return one;
	}

	/**
	 * @param obj
	 */
	public void setOne(BottomObj obj) {
		one = obj;
	}

	/**
	 * @return
	 */
	public String getComeBack() {
		return comeBack;
	}

	/**
	 * @param string
	 */
	public void setComeBack(String string) {
		comeBack = string;
	}

}
