
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
/**
 * class: javax.persistence.PersistenceException
 */
package javax.persistence;

/**
 * @author Rick George
 * @version 1.0, 8/15/05
 */
public class PersistenceException extends Exception {

	/**
	 * 
	 */
	public PersistenceException() {
		super();
	}

	/**
	 * @param arg0
	 */
	public PersistenceException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public PersistenceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 */
	public PersistenceException(Throwable arg0) {
		super(arg0);
	}

}
