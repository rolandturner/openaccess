
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
package com.versant.core.jdo.tools.enhancer;

import java.util.*;

import java.lang.reflect.*;
import java.io.*;

/**
 * An verification error.
 * @keep-all
 */
public class VerifyException extends Exception {

    private Throwable exception;

    /**
     * Create new for msg and chained exception.
     */
    public VerifyException(String msg, Throwable exception) {
        super(msg);
        this.exception = exception;
    }

    /**
     * Create new for msg.
     */
    public VerifyException(String msg) {
        this(msg, null);
    }

    /**
     * Create new for chained exception.
     */
    public VerifyException(Throwable exception) {
        this(exception.getMessage(), exception);
    }

    /**
     * Get the chained (wrapped) exception or null if none.
     */
    public Throwable getException() { return exception; }

    /**
     * Print our stack trace and that of the chained exception (if any).
     */
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        if (exception != null) {
            s.println("Chained exception:");
            exception.printStackTrace(s);
        }
    }

    /**
     * Print our stack trace and that of the chained exception (if any).
     */
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        if (exception != null) {
            s.println("Chained exception:");
            exception.printStackTrace(s);
        }
    }

}
