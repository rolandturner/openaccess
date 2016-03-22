
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

import com.versant.core.metadata.MDStatics;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Event logged when data is fetched for several instances at once.
 */
public class SmFetchBulkEvent extends SmFetchEventBase {

    private int inputSize;
    private String[] oids;

    public SmFetchBulkEvent(int storageManagerId, int type, int inputSize,
            String fetchGroup, String fieldName) {
        super(storageManagerId, type, fieldName, fetchGroup);
        this.inputSize = inputSize;
    }

    public int getInputSize() {
        return inputSize;
    }

    /**
     * Return the OIDs requested.
     */
    public String[] getOids() {
        return oids;
    }

    public void setOids(String[] oids) {
        this.oids = oids;
    }

    /**
     * Get a long description for this event (e.g. the query text).
     */
    public String getDescription() {
        return inputSize + " input OID(s) " +
                (fieldName == null ? "" : fieldName + " ") +
                returnedSize + " state(s)";
    }

    /**
     * Get a list of all the ClassAndOID's for all the input OIDs.
     */
    public List getInputOIDs() {
        if (oids == null) return Collections.EMPTY_LIST;
        int n = oids.length;
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            String oid = oids[i];
            int j = oid.indexOf(MDStatics.OID_CHAR_SEPERATOR);
            int classId = Integer.parseInt(oid.substring(0, j));
            a.add(new ClassAndOID(oid, getNameForClassID(classId)));
        }
        return a;
    }

}
