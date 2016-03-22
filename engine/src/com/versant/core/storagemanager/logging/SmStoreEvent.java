
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

import com.versant.core.storagemanager.StorageManager;

/**
 * Info on a store operation.
 */
public class SmStoreEvent extends SmStatesReturnedEvent {

    private int toStoreSize;
    private String[] toStoreOIDs;
    private int toDeleteSize;
    private String[] toDeleteOIDs;

    private boolean returnFieldsUpdatedBySM;
    private int storeOption;
    private boolean evictClasses;

    public SmStoreEvent(int storageManagerId, boolean returnFieldsUpdatedBySM,
            int storeOption, boolean evictClasses) {
        super(storageManagerId, StorageManagerEvent.STORE);
        this.returnFieldsUpdatedBySM = returnFieldsUpdatedBySM;
        this.storeOption = storeOption;
        this.evictClasses = evictClasses;
    }

    public boolean isReturnFieldsUpdatedBySM() {
        return returnFieldsUpdatedBySM;
    }

    public int getStoreOption() {
        return storeOption;
    }

    public String getStoreOptionStr() {
        switch (storeOption) {
            case StorageManager.STORE_OPTION_COMMIT:
                return "COMMIT";
            case StorageManager.STORE_OPTION_FLUSH:
                return "FLUSH";
            case StorageManager.STORE_OPTION_PREPARE:
                return "PREPARE";
        }
        return "UNKNOWN(" + storeOption + ")";
    }

    public boolean isEvictClasses() {
        return evictClasses;
    }

    public String getDescription() {
        StringBuffer s = new StringBuffer();
        s.append(toStoreSize);
        s.append(" OID(s) delete ");
        s.append(toDeleteSize);
        s.append(" OID(s) ");
        s.append(getStoreOption());
        if (returnFieldsUpdatedBySM) {
            s.append(" returnFieldsUpdatedBySM");
        }
        if (evictClasses) {
            s.append(" evictClasses");
        }
        return s.toString();
    }

    public int getToStoreSize() {
        return toStoreSize;
    }

    public void setToStoreSize(int toStoreSize) {
        this.toStoreSize = toStoreSize;
    }

    public String[] getToStoreOIDs() {
        return toStoreOIDs;
    }

    public void setToStoreOIDs(String[] toStoreOIDs) {
        this.toStoreOIDs = toStoreOIDs;
    }

    public int getToDeleteSize() {
        return toDeleteSize;
    }

    public void setToDeleteSize(int toDeleteSize) {
        this.toDeleteSize = toDeleteSize;
    }

    public String[] getToDeleteOIDs() {
        return toDeleteOIDs;
    }

    public void setToDeleteOIDs(String[] toDeleteOIDs) {
        this.toDeleteOIDs = toDeleteOIDs;
    }

}
