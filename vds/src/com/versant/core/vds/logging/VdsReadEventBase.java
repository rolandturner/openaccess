
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

import com.versant.odbms.LockMode;
import com.versant.odbms.Options;

/**
 * A read or group read.
 */
public class VdsReadEventBase extends VdsLogEvent {

    private String lockModeString;
    private int readObjectOptions;

    public VdsReadEventBase(int type, long txId, String descr, LockMode lockMode,
            int readObjectOptions) {
        super(type, txId, descr);
        this.lockModeString = lockMode.toString();
        this.readObjectOptions = readObjectOptions;
    }

    public String getLockMode() {
        return lockModeString;
    }

    public int getReadObjectOptions() {
        return readObjectOptions;
    }

    public String getReadObjectOptionsStr() {
        if (readObjectOptions == 0) return "NONE";
        StringBuffer s = new StringBuffer();
        s.append(readObjectOptions);
        if ((readObjectOptions & Options.DOWNGRADE_LOCKS_OPTION) != 0) {
            s.append(" DOWNGRADE_LOCKS");
        }
        if ((readObjectOptions & Options.NO_WAIT_OPTION) != 0) {
            s.append(" NO_WAIT");
        }
        if ((readObjectOptions & Options.CHECK_TIMESTAMPS_OPTION) != 0) {
            s.append(" CHECK_TIMESTAMPS");
        }
        return s.toString();
    }

}

