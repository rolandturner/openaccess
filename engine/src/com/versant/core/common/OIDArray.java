
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
package com.versant.core.common;

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.util.OIDObjectOutput;
import com.versant.core.util.OIDObjectInput;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * This is an array of OIDs that expands when full. It implements
 * Externalizable to flatten the OIDs for fast Serialization.
 */
public final class OIDArray {

    public OID[] oids = new OID[20];
    private int size;

    public OIDArray() {
    }

    public OIDArray(OIDArray toCopy) {
        size = toCopy.size;
        oids = new OID[size];
        System.arraycopy(toCopy.oids, 0, oids, 0, size);
    }

    public void add(OID oid) {
        if (size == oids.length) {
            OID[] t = new OID[size * 2];
            System.arraycopy(oids, 0, t, 0, size);
            oids = t;
        }
        oids[size++] = oid;
    }

    public void add(OID[] a, int offset, int length) {
        if (size + length > oids.length) {
            int n = size + length;
            OID[] t = new OID[n];
            System.arraycopy(oids, 0, t, 0, size);
            oids = t;
        }
        System.arraycopy(a, offset, oids, size, length);
        size += length;
    }

    /**
     * Is oid in our list? This checks using a linear search.
     */
    public boolean contains(OID oid) {
        for (int i = size - 1; i >= 0; i--) {
            if (oids[i].equals(oid)) {
                return true;
            }
        }
        return false;
    }

    public void readExternal(OIDObjectInput in) throws IOException,
            ClassNotFoundException {
        size = in.readInt();
        oids = new OID[size];
        for (int i = 0; i < size; i++) {
            oids[i] = in.readOID();
        }
    }

    public void writeExternal(OIDObjectOutput out) throws IOException {
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            out.write(oids[i]);
        }
    }

    public void clear() {
        Arrays.fill(oids, null);
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Sort the OIDs.
     */
    public void sort(Comparator comp) {
        if (size <= 1) return;
        if (size == 2) {
            if (comp.compare(oids[0], oids[1]) > 0) {
                OID t = oids[0];
                oids[0] = oids[1];
                oids[1] = t;
            }
            return;
        }
        Arrays.sort(oids, 0, size, comp);
    }

    /**
     * Dump to sysout.
     */
    public void dump() {
        for (int i = 0; i < size; i++) {
            ClassMetaData c = oids[i].getAvailableClassMetaData();
            System.out.println("[" + i + "] = " + oids[i] +
                    " rgi " + c.referenceGraphIndex + " index " + c.index);
        }
    }

    /**
     * Get our OIDs into a new array.
     */
    public OID[] getOIDs() {
        OID[] tmpOIDs = new OID[size];
        for (int i = 0; i < size; i++) {
            tmpOIDs[i] = oids[i];
        }
        return tmpOIDs;
    }

    /**
     * Copy our OIDs into an array at position index.
     */
    public void copy(OID[] dest, int index) {
        System.arraycopy(oids, 0, dest, index, size);
    }

    /**
     * Get the OID at index.
     */
    public OID get(int index) {
        return oids[index];
    }

}
