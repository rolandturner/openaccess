
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
package com.versant.core.storagemanager.logging;

import com.versant.core.logging.LogEvent;

/**
 * Base for StorageManager events.
 */
public class StorageManagerEvent extends LogEvent {

    private int storageManagerId;
    private int type;

    public static final int BEGIN = 1;
    public static final int COMMIT = 2;
    public static final int ROLLBACK = 3;
    public static final int FETCH = 4;
    public static final int FETCH_BULK = 5;
    public static final int STORE = 6;
    public static final int COMPILE = 7;
    public static final int EXEC = 8;
    public static final int EXEC_ALL = 9;
    public static final int EXEC_COUNT = 10;
    public static final int FETCH_BATCH = 11;
    public static final int FETCH_INDEX = 12;
    public static final int FETCH_COUNT = 13;
    public static final int QUERY_CLOSE = 14;

    public StorageManagerEvent(int storageManagerId, int type) {
        this.storageManagerId = storageManagerId;
        this.type = type;
    }

    public String getTypeStr() {
        switch (type) {
            case BEGIN:         return "sm.begin";
            case COMMIT:        return "sm.commit";
            case ROLLBACK:      return "sm.rollback";
            case FETCH:         return "sm.fetch";
            case FETCH_BULK:    return "sm.fetch.bulk";
            case STORE:         return "sm.store";
            case COMPILE:       return "sm.compile";
            case EXEC:          return "sm.exec";
            case EXEC_ALL:      return "sm.exec.all";
            case EXEC_COUNT:    return "sm.exec.count";
            case FETCH_BATCH:   return "sm.fetch.batch";
            case FETCH_INDEX:   return "sm.fetch.index";
            case FETCH_COUNT:   return "sm.fetch.count";
            case QUERY_CLOSE:   return "sm.query.close";
        }
        return "UNKNOWN(" + type + ")";
    }

    public String getName() {
        return getTypeStr();
    }

    public String getDescription() {
        return "";
    }

    public int getStorageManagerId() {
        return storageManagerId;
    }

    public int getType() {
        return type;
    }

}
