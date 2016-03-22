
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

import javax.jdo.JDOFatalInternalException;

/**
 * Thrown when an unimplemented code path is followed.
 */
public class NotImplementedException extends JDOFatalInternalException {

    public NotImplementedException() {
    }

    public NotImplementedException(String msg) {
        super(msg);
    }

    public NotImplementedException(String msg, Throwable[] nested) {
        super(msg, nested);
    }

    public NotImplementedException(String msg, Throwable nested) {
        super(msg, nested);
    }
}
