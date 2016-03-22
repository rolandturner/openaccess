
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
import com.versant.odbms.Options;

/**
 * Write a group of LOIDs.
 */
public class VdsGroupWriteEvent extends VdsLogEvent {

    private long[] loids;
    private short options;
    private long[] notFound;
    private long[] writeFailed;

    public VdsGroupWriteEvent(long[] loids, short options) {
        super(GROUP_WRITE, 0, null);
        this.loids = loids;
        this.options = options;
    }

    public String getDescription() {
        return loids.length + " LOID(s) " + getOptionsStr() +
                "  " + getLoidsStr();
    }

    public long[] getLoids() {
        return loids;
    }

    public String getLoidsStr() {
        return Loid.asString(loids, 10);
    }

    public short getOptions() {
        return options;
    }

    public String getOptionsStr() {
        if (options == 0) return "NONE";
        StringBuffer s = new StringBuffer();
        if ((options & Options.CHECK_TIMESTAMPS_OPTION) != 0) {
            s.append("CHECK_TIMESTAMPS");
        }
        return s.toString();
    }

    public long[] getNotFound() {
        return notFound;
    }

    public void setNotFound(long[] notFound) {
        this.notFound = notFound;
    }

    public long[] getWriteFailed() {
        return writeFailed;
    }

    public void setWriteFailed(long[] writeFailed) {
        this.writeFailed = writeFailed;
    }

}

