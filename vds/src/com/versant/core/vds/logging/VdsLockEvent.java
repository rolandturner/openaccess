
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
package com.versant.core.vds.logging;

import com.versant.core.vds.util.Loid;
import com.versant.odbms.LockMode;

/**
 * Read of single LOID.
 */
public class VdsLockEvent extends VdsLogEvent {

    private long loid;
    private String lockModeString;

    public VdsLockEvent(long txId, LockMode lockMode, long loid) {
        super(LOCK, txId, null);
        this.loid = loid;
        lockModeString = lockMode.toString();
    }

    public String getDescription() {
        return Loid.asString(loid) + " " + getLockMode();
    }

    public long getLoid() {
        return loid;
    }

    public String getLoidStr() {
        return Loid.asString(loid);
    }

    public String getLockMode() {
        return lockModeString;
    }

}

