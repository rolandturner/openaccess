
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
 * Classes that perform tasks in the background on the server implement this.
 * @see LogDownloader
 */
public interface VersantBackgroundTask extends Runnable {

    /**
     * Get ready to work with pmf. This is called before run is invoked.
     */
    public void setPmf(VersantPersistenceManagerFactory pmf) throws Exception;

    /**
     * Stop running. This will be called during the server shutdown
     * process. The event log and performance metric snapshot buffers are
     * still accessable. This should set a flag so that the run method will
     * return when interrupted.
     */
    public void shutdown();

}

