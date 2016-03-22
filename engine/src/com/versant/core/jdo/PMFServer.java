
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

/**
 * PMFs with remote access enabled will create these to handle requests from
 * remote clients. These can also be added to an existing PMF (see
 * {@link VersantPMFInternal#addPMFServer(PMFServer)}.
 */
public interface PMFServer {

    /**
     * Initialize to accept connections for the pmf. This is not called on
     * PMFServer's added to an existing PMF.
     */
    public void init(VersantPMFInternal pmf);

    /**
     * Start accepting connections.  This is not called on
     * PMFServer's added to an existing PMF.
     */
    public void start() throws Exception;

    /**
     * Close this server releasing all resources.
     */
    public void close();

}

