
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

import com.versant.core.logging.LogEvent;
import com.versant.core.logging.LogEvent;

/**
 * VDS related performance events.
 */
public class VdsLogEvent extends LogEvent {

    protected int type;
    protected String descr;

    public static final int READ = 1;
    public static final int GROUP_READ = 2;
    public static final int GROUP_LOCK = 3;
    public static final int LOCK = 4;
    public static final int GROUP_WRITE = 5;
    public static final int CLOSE = 6;
    public static final int BEGIN = 7;
    public static final int COMMIT = 8;
    public static final int ROLLBACK = 9;

    public VdsLogEvent(int type, long txId, String descr) {
        this.datastoreTxId = txId;
        this.type = type;
        this.descr = descr;
    }

    /**
     * Get a short descriptive name for this event.
     */
    public String getName() {
        switch (type) {
            case READ:          return "vds.read";
            case GROUP_READ:    return "vds.group.read";
            case GROUP_LOCK:    return "vds.group.lock";
            case LOCK:          return "vds.lock";
            case GROUP_WRITE:   return "vds.group.write";
            case CLOSE:         return "vds.close";
            case BEGIN:         return "vds.begin";
            case COMMIT:        return "vds.commit";
            case ROLLBACK:      return "vds.rollback";
        }
        return "vds.UNKNOWN(" + type + ")";
    }

    public String toString() {
        String d = getDescription();
        if (d == null) {
            return getName();
        } else {
            StringBuffer s = new StringBuffer();
            s.append(getName());
            for (int n = 22 - s.length(); n > 0; n--) s.append(' ');
            s.append(d);
            return s.toString();
        }
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    /**
     * Get a long description for this event (e.g. the query text).
     */
    public String getDescription() {
        return descr;
    }

    /**
     * Get the type of this event (READ etc).
     */
    public int getType() {
        return type;
    }
}
