
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
 * @author Rick George
 * @version 1.0, 9/4/05
 */
package com.versant.persistence;

public class XmlException extends Exception {
	private static final long serialVersionUID = -2264046461796387387L;

	/**
	 * 
	 */
	public XmlException() {
		super();
	}

	/**
	 * @param message
	 */
	public XmlException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public XmlException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public XmlException(Throwable cause) {
		super(cause);
	}

}
