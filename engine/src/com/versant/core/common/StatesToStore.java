
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

import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.util.OIDObjectOutput;
import com.versant.core.util.OIDObjectInput;
import com.versant.core.util.FastExternalizable;

import java.io.*;
import java.util.Iterator;

/**
 * Collection of OIDs and States to be persisted.
 */
public final class StatesToStore implements FastExternalizable {

    private static final int breakPoint = 100;
    private static final int initialSize = 50;

    private ModelMetaData jmd;

    private static final int INITIAL_CAPACITY = 20;
    private static final int GROW_SIZE = 2;

    public OID[] oids = new OID[INITIAL_CAPACITY];
    public State[] states = new State[INITIAL_CAPACITY];
    public State[] origStates = new State[INITIAL_CAPACITY];

    private int size;           // the number of entries
    private boolean fullSort;

    // These fields keep track of OIDs/instances of objectid-class'es and
    // classes to evict from l2 cache if the transaction commits (epc = Evict
    // Post Commit).
    public boolean epcAll;      // evict everything from l2 cache on commit
    public OID[] epcOids;       // may be null
    public int[] epcClasses;    // of class index, may be null
    public int epcClassCount;

    public StatesToStore() {
    }

    public StatesToStore(ModelMetaData jmd) {
        this.jmd = jmd;
    }

    /**
     * Is a full topological sort required to persist the states in this
     * graph? This will be true if any of the states are for new objects
     * and are using a post-insert key generator.
     *
     * @see com.versant.core.server.PersistGraph
     * @see com.versant.core.server.PersistGraphFullSort
     */
    public boolean isFullSortRequired() {
        return fullSort;
    }

    /**
     * Add a state to the container to be persisted.
     * @param state The state containing the changed fields
     * @param origState The state containing original field values
     * @param fullSort If true then the fullSort flag is set on the container
     */
    public void add(OID oid, State state, State origState, boolean fullSort) {
        resize();
        oids[size] = oid;
        states[size] = state;
        origStates[size++] = origState;
        if (fullSort) this.fullSort = true;
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
        final int length = oids.length;
        if (length == size) {
            final int growTo = (length + 1) * GROW_SIZE;
            final OID[] tmpOIDs = new OID[growTo];
            final State[] tmpStates = new State[growTo];
            final State[] tmpOStates = new State[growTo];

            for (int i = 0; i < length; i++) {
                tmpOIDs[i] = oids[i];
                tmpStates[i] = states[i];
                tmpOStates[i] = origStates[i];
            }

            oids = tmpOIDs;
            states = tmpStates;
            origStates = tmpOStates;
        }
    }

    /**
     * Clear the container for reuse.
     */
    public void clear() {
        if (size > breakPoint) {
            oids = new OID[initialSize];
            states = new State[initialSize];
            origStates = new State[initialSize];
        } else {
            final int n = size;
            for (int i = 0; i < n; i++) {
                oids[i] = null;
                states[i] = null;
                origStates[i] = null;
            }
        }
        size = 0;
        fullSort = false;
        epcAll = false;
        epcClassCount = 0;
        epcClasses = null;
        epcOids = null;
    }

    public void dump() {
        StringBuffer sb = new StringBuffer("StoreOIDStateContainer: ");
        for (int i = 0; i < oids.length; i++) {
            OID oid = oids[i];
            if (oid == null) break;
            sb.append("\nOID = " + oid.toSString());
            sb.append("\nState = " + states[i]);
            sb.append("\nOrigState = " + origStates[i]);
            sb.append("\nNext");
        }
        System.out.println(sb.toString());
    }

    public void dumpEPC() {
        System.out.println("StatesToStore.dumpEPC");
        System.out.println("epcAll = " + epcAll);
        System.out.println("epcOids = " + epcOids);
        if (epcOids != null) {
            System.out.println("epcOids.length = " + epcOids.length);
        }
        System.out.println("epcClasses = " + epcClasses);
        if (epcClasses != null) {
            System.out.println("epcClasses.length = " + epcClasses.length);
        }
        System.out.println("epcClassCount = " + epcClassCount);

    }

    public void writeExternal(OIDObjectOutput out) throws IOException {
        out.writeBoolean(fullSort);
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            State state = states[i];
            OID oid = oids[i];
            oid.resolve(state);
            out.writeInt(state.getClassIndex());
            out.writeWithoutCMD(oid);
            state.writeExternal(out);
            if (origStates[i] == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                origStates[i].writeExternal(out);
            }
        }
        writeExternalEpc(out);
    }

    public void readExternal(OIDObjectInput in) throws IOException,
                ClassNotFoundException {
        jmd = in.getModelMetaData();
        fullSort = in.readBoolean();
        size = in.readInt();
        oids = new OID[size];
        states = new State[size];
        origStates = new State[size];
        for (int i = 0; i < size; i++) {
            ClassMetaData cmd = jmd.classes[in.readInt()];
            oids[i] = in.readOID(cmd);
            states[i] = cmd.createState();
            states[i].readExternal(in);
            if (in.readBoolean()) {
                origStates[i] = cmd.createState();
                origStates[i].readExternal(in);
            }
        }
        readExternalEpc(in);
    }

    private void writeExternalEpc(OIDObjectOutput out) throws IOException {
        out.writeBoolean(epcAll);
        if (epcOids == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            int n = 0;
            for (; n < epcOids.length; ) {
                if (epcOids[n] == null) break;
                n++;
            }
            out.writeInt(n);
            for (int i = 0; i < n; i++) {
                OID oid = epcOids[i];
                if (oid.isNew()) {
                    throw BindingSupportImpl.getInstance().internal(
                            "New oids should not be included for eviction");
                }
                out.write(oid);
            }
        }
        if (epcClasses == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            out.writeInt(epcClasses.length);
            for (int i = 0; i < epcClasses.length; i++) {
                out.writeInt(epcClasses[i]);
            }
        }
        out.writeInt(epcClassCount);
    }

    private void readExternalEpc(OIDObjectInput in) throws IOException,
            ClassNotFoundException {
        epcAll = in.readBoolean();
        if (in.readByte() != 0) {
            int n = in.readInt();
            OID[] oids = epcOids = new OID[n];
            for (int i = 0; i < n; i++) {
                oids[i] = in.readOID();
            }
        }
        if (in.readByte() != 0) {
            int n = in.readInt();
            int[] ia = epcClasses = new int[n];
            for (int i = 0; i < n; i++) {
                ia[i] = in.readInt();
            }
        }
        epcClassCount = in.readInt();
    }

    /**
     * Iterate over the OIDs.
     */
    public Iterator iteratorForOIDs() {
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
