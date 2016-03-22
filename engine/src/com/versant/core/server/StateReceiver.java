
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
import com.versant.core.metadata.FetchGroup;
import com.versant.core.common.OID;

/**
 * Classes that can receive extra states from a DataStore implement this.
 */
public interface StateReceiver {

    /**
     * This is a callback for DataStore instances to use when they have
     * additional data above that requested.
     *
     * @param fetchGroup This is relative to the class of oid
     * @return True if the receiver may accept a State for the oid. The store
     *         should construct a State and call addState. If this is false then
     *         the store should discard its extra data. This two step process prevents
     *         the construction of unecessary State objects.
     * @see #addState
     */
    public boolean isStateRequired(OID oid, FetchGroup fetchGroup);

    /**
     * This is a callback for DataStore instances to use when they have
     * additional data they want to provide.
     *
     * @see #isStateRequired
     */
    public void addState(OID oid, State state);
}
