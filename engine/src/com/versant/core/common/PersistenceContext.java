
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

import com.versant.core.jdo.PCStateMan;
import com.versant.core.jdo.VersantStateManager;

import javax.jdo.spi.PersistenceCapable;
import javax.jdo.PersistenceManager;

/**
 * Internal interface that is used for common funtionality between
 * different implementations of pm(eg jdo's pm and ejb3's em).
 */
public interface PersistenceContext {
    public OID getInternalOID(final PersistenceCapable pc);

    Object getObjectById(Object oid, boolean b);

    PCStateMan getInternalSM(PersistenceCapable pc);

    VersantStateManager getVersantStateManager(PersistenceCapable pc);

    PersistenceManager getPersistenceManager();

    Object getObjectByIdForState(OID oid, int stateFieldNo,
            int navClassIndex, OID fromOID);
}
