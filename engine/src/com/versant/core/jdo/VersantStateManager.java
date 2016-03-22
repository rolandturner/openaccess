
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

import com.versant.core.common.OID;

import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;

/**
 * Internal interface for StateManager.
 */
public interface VersantStateManager extends StateManager {

    void makeDirty(PersistenceCapable persistenceCapable, int managedFieldNo);

    public void fillNewAppPKField(int fieldNo);

    public OID getOID();

    public PersistenceCapable getPersistenceCapable();
}
