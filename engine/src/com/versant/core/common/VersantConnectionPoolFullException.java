
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

import javax.jdo.JDODataStoreException;

/**
 * This is thrown when a connection pool has run out of connections.
 */
public class VersantConnectionPoolFullException extends JDODataStoreException {

    public VersantConnectionPoolFullException(String msg) {
        super(msg);
    }

}
