
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
package com.versant.core.jdo.junit;

import javax.jdo.JDOFatalUserException;

public class TestFailedException extends JDOFatalUserException {

    public TestFailedException(String message) {
        super(message);
    }

    public TestFailedException(Throwable nested) {
        super(nested.getClass().getName() + ": " + nested.getMessage(), nested);
    }

}
