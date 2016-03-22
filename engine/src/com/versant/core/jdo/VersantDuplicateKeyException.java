
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
 * This exception is thrown when duplicate primary keys are detected by the
 * backend. 
 */
public class VersantDuplicateKeyException extends JDOFatalDataStoreException {

    public VersantDuplicateKeyException() {
    }

    public VersantDuplicateKeyException(String s) {
        super(s);
    }

    public VersantDuplicateKeyException(String s,
            Throwable[] throwables) {
        super(s, throwables);
    }

    public VersantDuplicateKeyException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public VersantDuplicateKeyException(String s, Object o) {
        super(s, o);
    }
}
