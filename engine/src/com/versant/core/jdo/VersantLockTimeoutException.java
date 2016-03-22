
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
package com.versant.core.jdo;

import javax.jdo.JDOFatalDataStoreException;

/**
 * This exception is thrown for lock timeouts in the backend. 
 */
public class VersantLockTimeoutException extends JDOFatalDataStoreException {

    public VersantLockTimeoutException() {
    }

    public VersantLockTimeoutException(String s) {
        super(s);
    }

    public VersantLockTimeoutException(String s,
            Throwable[] throwables) {
        super(s, throwables);
    }

    public VersantLockTimeoutException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public VersantLockTimeoutException(String s, Object o) {
        super(s, o);
    }
}
