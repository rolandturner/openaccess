
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
import java.util.ArrayList;
import java.util.Collections;

/**
 * Base class for events logged for operations that return states.
 */
public class SmStatesReturnedEvent extends StorageManagerEvent {

    protected int returnedSize;
    private String[] returnedOIDs;
    private int[] lookupClassIDs;
    private String[] lookupClassNames;

    public SmStatesReturnedEvent(int storageManagerId, int type) {
        super(storageManagerId, type);
    }

    /**
     * How many states were returned in the packet? This includes the state
     * asked for and all prefetched data.
     */
    public int getReturnedSize() {
        return returnedSize;
    }

    public void setReturnedSize(int returnedSize) {
        this.returnedSize = returnedSize;
    }

    /**
     * Get all the OIDs for all of the states included in the response packet.
     */
    public String[] getReturnedOIDs() {
        return returnedOIDs;
    }

    public void setReturnedOIDs(String[] returnedOIDs) {
        this.returnedOIDs = returnedOIDs;
    }

    /**
     * Set the classIDs and corresponding class names for all classID lookup
     * for this event. This information can be used to display the class names
     * for the classIDs in this event.
     * @see #getNameForClassID(int)
     */
    public void setLookupClasses(int[] classIDs, String[] classNames) {
        this.lookupClassIDs = classIDs;
        this.lookupClassNames = classNames;
    }

    public int[] getLookupClassIDs() {
        return lookupClassIDs;
    }

    public String[] getLookupClassNames() {
        return lookupClassNames;
    }

    /**
     * Convert a class ID for a class in this event into its name. Returns
     * null if no match found.
     */
    public String getNameForClassID(int id) {
        int n = lookupClassIDs.length;
        for (int i = 0; i < n; i++) {
            if (lookupClassIDs[i] == id) return lookupClassNames[i];
        }
        return null;
    }

    /**
     * Get a list of all the ClassAndOID's for all the data in the return
     * packet.
     */
    public List getPacketEntries() {
        if (returnedOIDs == null) return Collections.EMPTY_LIST;
        int n = returnedOIDs.length;
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            String oid = returnedOIDs[i];
            int j = oid.indexOf(MDStatics.OID_CHAR_SEPERATOR);
            int classId = Integer.parseInt(oid.substring(0, j));
            a.add(new ClassAndOID(oid, getNameForClassID(classId)));
        }
        return a;
    }

    /**
     * An entry in the data packet.
     */
    public static class ClassAndOID {

        private String oid;
        private String className;

        public ClassAndOID(String oid, String className) {
            this.oid = oid;
            this.className = className;
        }

        public String getOid() {
            return oid;
        }

        public String getClassName() {
            return className;
        }

        public void setOid(String oid) {
            this.oid = oid;
        }

        public void setClassName(String className) {
            this.className = className;
        }
    }

}
