
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
 * Read/lock of a group of LOIDs.
 */
public class VdsGroupReadEvent extends VdsReadEventBase {

    private long[] loids;

    public VdsGroupReadEvent(int type, long txId, LockMode lockMode,
            int readObjectOptions, long[] loids) {
        super(type, txId, null, lockMode, readObjectOptions);
        this.loids = loids;
    }

    public String getDescription() {
        return loids.length + " LOID(s) " + getLockMode() +
                " " + getReadObjectOptionsStr() + " " + Loid.asString(loids, 10);
    }

    public long[] getLoids() {
        return loids;
    }

    public String getLoidsStr() {
        return Loid.asString(loids, 0);
    }
}

