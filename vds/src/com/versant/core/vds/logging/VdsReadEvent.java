
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
public class VdsReadEvent extends VdsReadEventBase {

    private long loid;

    public VdsReadEvent(int type, long txId, LockMode lockMode,
            int readObjectOptions, long loid) {
        super(type, txId, null, lockMode, readObjectOptions);
        this.loid = loid;
    }

    public String getDescription() {
        return Loid.asString(loid) + " " + getLockMode() +
                " " + getReadObjectOptionsStr();
    }

    public long getLoid() {
        return loid;
    }

    public String getLoidStr() {
        return Loid.asString(loid);
    }
}

