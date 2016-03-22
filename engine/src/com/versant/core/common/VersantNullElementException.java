
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
package com.versant.core.common;

import javax.jdo.JDOUserException;

/**
 * This is thrown when a null element is found in our added to a collection.
 */
public class VersantNullElementException extends JDOUserException {

    public VersantNullElementException() {
    }

    public VersantNullElementException(String msg) {
        super(msg);
    }

    public VersantNullElementException(String msg, Throwable[] nested) {
        super(msg, nested);
    }

    public VersantNullElementException(String msg, Throwable nested) {
        super(msg, nested);
    }

}
