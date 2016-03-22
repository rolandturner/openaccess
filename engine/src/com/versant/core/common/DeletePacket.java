
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
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.util.OIDObjectOutput;
import com.versant.core.util.OIDObjectInput;

import java.io.*;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Collection of OIDs and States to be deleted. If this is constucted with
 * keepStates == false then then states array is null and added states will
 * be silently dropped. If keepStates == true then the array will be not
 * null but the entry for a given OID may still be null if this information
 * was not available. In this case the datastore will have to read the
 * object prior to deleting it.
 */
public final class DeletePacket {

    private static final int INITIAL_CAPACITY = 20;
    private static final int GROW_FACTOR = 2;

    private ModelMetaData jmd;
    private boolean keepStates;
    public OID[] oids = new OID[INITIAL_CAPACITY];
    public State[] states;

    private int size;

    /**
     * This is for Externalizable.
     */
    public DeletePacket() {
    }

    public DeletePacket(ModelMetaData jmd) {
        this.jmd =jmd;
        keepStates = jmd.sendStateOnDelete;
        if (keepStates) states = new State[INITIAL_CAPACITY];
    }

    /**
     * Add a state to the container to be deleted.
     */
    public void add(OID oid, State state) {
        if (Debug.DEBUG) {
            if (oid.isNew()) {
                BindingSupportImpl.getInstance().internal("oid is new: " + oid);
            }
            // make sure untyped OIDs are not added
            if (oid.getAvailableClassMetaData() == null) {
                BindingSupportImpl.getInstance().internal("oid is untyped: " +
                        oid);
            }
        }
        resize();
        if (keepStates) states[size] = state;
        oids[size++] = oid;
    }

    /**
     * The number of entries in the container.
     */
    public int size() {
        return size;
    }

    /**
     * Is the container empty?
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Check if the arrays needs to grow some more.
     */
    private void resize() {
        int length = oids.length;
        if (length == size) {
            int growTo = (length + 1) * GROW_FACTOR;
            OID[] tmpOIDs = new OID[growTo];
            System.arraycopy(oids, 0, tmpOIDs, 0, length);
            oids = tmpOIDs;
            if (keepStates) {
                State[] tmpStates = new State[growTo];
                System.arraycopy(states, 0, tmpStates, 0, length);
                states = tmpStates;
            }
        }
    }

    /**
     * Clear the container for reuse.
     */
    public void clear() {
        oids = new OID[INITIAL_CAPACITY];
        if (keepStates) {
            states = new State[INITIAL_CAPACITY];
        }
        size = 0;
    }

    public boolean isKeepStates() {
        return keepStates;
    }

    /**
     * Sort the OIDs. This is a NOP if keepStates is true.
     */
    public void sortOIDs(Comparator comp) {
        if (keepStates) return;
        states = null;
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

    public void dump() {
        StringBuffer sb = new StringBuffer("DeleteOIDStateContainer: ");
        for (int i = 0; i < oids.length; i++) {
            OID oid = oids[i];
            if (oid == null) break;
            sb.append("\nOID = " + oid.toSString());
            if (keepStates) {
                sb.append("\nState = " + states[i]);
            }
            sb.append("\nNext");
        }
        System.out.println(sb.toString());
    }

    public void writeExternal(OIDObjectOutput out) throws IOException {
        out.writeBoolean(keepStates);
        out.writeInt(size);
        if (keepStates) {
            for (int i = 0; i < size; i++) {
                out.writeShort(oids[i].getClassIndex());
                out.writeWithoutCMD(oids[i]);
                if (states[i] == null) {
                    out.writeByte(0);
                } else {
                    out.writeByte(1);
                    states[i].writeExternal(out);
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                out.write(oids[i]);
            }
        }
    }

    public void readExternal(OIDObjectInput in) throws IOException,
            ClassNotFoundException {
        jmd = in.getModelMetaData();
        keepStates = in.readBoolean();
        size = in.readInt();
        oids = new OID[size];
        if (keepStates) {
            states = new State[size];
            for (int i = 0; i < size; i++) {
                final ClassMetaData cmd = jmd.classes[in.readShort()];
                oids[i] = in.readOID(cmd);
                if (in.readByte() == 1) {
                    states[i] = cmd.createState();
                    states[i].readExternal(in);
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                oids[i] = in.readOID();
            }
        }
    }

    /**
     * Iterate over the OIDs.
     */
    public Iterator iterator() {
        return new Iter();
    }

    public class Iter implements Iterator {

        private int pos;

        public boolean hasNext() {
            return pos < size - 1;
        }

        public Object next() {
            return oids[pos++];
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
