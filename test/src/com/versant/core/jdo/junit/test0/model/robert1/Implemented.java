
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
package com.versant.core.jdo.junit.test0.model.robert1;

public class Implemented implements NeedToImplement {

	String theString;
    
	/* (non-Javadoc)
	 * @see test.NeedToImplement#thisMethod()
	 */
	public Implemented( String init ){
		this.theString = init;
	}
	public String thisMethod() {

		return theString;
	}

}
