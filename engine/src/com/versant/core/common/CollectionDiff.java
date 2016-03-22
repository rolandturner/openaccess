
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

import com.versant.core.metadata.MDStatics;
import com.versant.core.util.OIDObjectInput;
import com.versant.core.util.OIDObjectOutput;
import com.versant.core.util.FastExternalizable;

import java.io.*;


/**
 * This is the base class for classes that hold the differences between two
 * collections or maps. A subclass of this is stored in the new state when
 * changes to the the collection are persisted.
 */
public abstract class CollectionDiff implements FastExternalizable, Cloneable {

    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    public VersantFieldMetaData fmd;

    /**
     * This is a new or replaced collection and therefore everything
     * in the store must be deleted if it does exist.
     */
    public static final int STATUS_NEW = 1;

    public int status;

    /**
     * The inserted values.
     */
    public Object[] insertedValues;

    public CollectionDiff() {
    }

    public CollectionDiff(VersantFieldMetaData fmd) {
        this.fmd = fmd;
    }

    public String toString() {
        return "<CollectionDiff: status = " + status + ": amount added ="
                + (insertedValues != null ? insertedValues.length : -1) + ">";
    }

    protected void write(OIDObjectOutput out, int typeCode, boolean pc,
            Object[] data)
            throws IOException {
        int n = data == null ? 0 : data.length;
//        System.out.println("%%% CollectionDiff.write " + n);
        out.writeInt(n);
        if (pc) {
            for (int i = 0; i < n; i++) {
                out.write((OID)data[i]);
            }
        } else {
            for (int i = 0; i < n; i++) {
                out.writeObject(data[i]);
            }
        }
    }

    protected Object[] read(OIDObjectInput in, int typeCode, boolean pc)
            throws IOException, ClassNotFoundException {
        int n = in.readInt();
//        System.out.println("%%% CollectionDiff.read " + n);
        if (n <= 0) {
            return null;
        }
        Object[] data = new Object[n];
        if (pc) {
            for (int i = 0; i < n; i++) {
                data[i] = in.readOID();
            }
        } else {
            for (int i = 0; i < n; i++) {
                data[i] = in.readObject();
            }
        }
        return data;
    }

    public void writeExternal(OIDObjectOutput out) throws IOException {
        out.writeByte(status);
        write(out, fmd.getElementTypeCode(), fmd.isElementTypePC(),
                insertedValues);
    }

    public void readExternal(OIDObjectInput in) throws IOException,
            ClassNotFoundException {
        status = in.readByte();
        insertedValues = read(in, fmd.getElementTypeCode(),
                fmd.isElementTypePC());
    }

    protected Object clone() throws CloneNotSupportedException {
        CollectionDiff cloned = (CollectionDiff)super.clone();
        cloned.insertedValues = new Object[insertedValues.length];
        System.arraycopy(insertedValues, 0, cloned.insertedValues, 0,
                insertedValues.length);
        return cloned;
    }

    public void dump() {
    }

}
