
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
package com.versant.core.server;

import com.versant.core.common.OID;
import com.versant.core.common.State;

/**
 * This is implemented by containers that holds oid-state pairs and is used for
 * transport to client.
 */
public interface StateContainer extends StateReceiver {
    /**
     * This is called to indicate that the state is in the process of
     * being fetched.
     */
    public void visited(OID oid);
    /**
     * Check if OID is contained.
     */
    public boolean containsKey(Object key);
    /**
     * Add an oid state pair. This is called for a direct add.
     */
    public State add(OID key, State value);
    /**
     * Return the state for the oid if contains.
     */
    public State get(Object key);
}
