
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

import javax.jdo.JDOUserException;

/**
 * This is thrown when a missing field is accessed in a detached graph.
 */
public class VersantDetachedFieldAccessException extends JDOUserException {

    public VersantDetachedFieldAccessException() {
    }

    public VersantDetachedFieldAccessException(String s) {
        super(s);
    }

    public VersantDetachedFieldAccessException(String s,
            Throwable[] throwables) {
        super(s, throwables);
    }

    public VersantDetachedFieldAccessException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public VersantDetachedFieldAccessException(String s, Object o) {
        super(s, o);
    }

    public VersantDetachedFieldAccessException(String s,
            Throwable[] throwables, Object o) {
        super(s, throwables, o);
    }

    public VersantDetachedFieldAccessException(String s, Throwable throwable,
            Object o) {
        super(s, throwable, o);
    }
}
