
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

import com.versant.core.metadata.FetchGroup;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.server.*;

import java.io.*;
import java.util.*;

import com.versant.core.storagemanager.ApplicationContext;
import com.versant.core.util.OIDObjectInput;
import com.versant.core.util.OIDObjectOutput;
import com.versant.core.util.FastExternalizable;

/**
 * This keeps a map of OID -> State and can also track the order that the
 * OID State pairs are added.
 */
public final class StatesReturned implements StateContainer, FastExternalizable {

    private EntrySet data = new EntrySet();
    private transient ApplicationContext context;
    //The first direct oid added.
    private OID directOid;

    public transient StatesReturned next;

    public StatesReturned() {
    }

    public StatesReturned(ApplicationContext context) {
        setContext(context);
    }

    public ApplicationContext getContext() {
        return context;
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    public int size() {
        return data.size();
    }

    /**
     * This Iterator returns Entry{@link com.versant.core.common.EntrySet.Entry} instances each with an OID key and
     * State value.
     */
    public Iterator iterator() {
        return data.iterator();
    }

    /**
     * This Iterator returns OID instances.
     */
    public Iterator iteratorForOIDs() {
        return data.newKeyIterator();
    }

    /**
     * This is called to indicate that we are busy resolving the state for
     * this OID. This is used to stop infinite recursion.
     */
    public void visited(OID oid) {
        addImp(oid, null, false);
    }

    public void ensureDirect(Object[] oids) {
        if (oids == null) return;
        for (int i = 0; i < oids.length; i++) {
            if (oids[i] == null) break;
            addImp((OID)oids[i], null, true);
        }
    }

    /**
     * - If contain this oid and the state is null then definitly return false.
     * This is to avoid recursive fetching.
     * - If contain the oid and the state is not null then check if the state
     * contains the fg.
     * - If not contain this oid then ask delegate stateReceiver
     */
    public boolean isStateRequired(OID oid, FetchGroup fetchGroup) {
        if (data.contains(oid)) {
            EntrySet.Entry e = data.get(oid);
            State s = (State) e.getValue();
            if (s == null) {
                return false;
            }
            return !s.containsFetchGroup(fetchGroup);
        }
        return context == null || context.isStateRequired(oid, fetchGroup);
    }

    /**
     * Callback to add indirect state.
     */
    public void addState(OID oid, State state) {
        if (Debug.DEBUG) {
            if (state == null) {
                throw BindingSupportImpl.getInstance().internal("Null state not allowed");
            }
        }
        addIndirect(oid, state);
    }

    /**
     * Add an entry to the table. If the key is already present in the table,
     * this replaces the existing value associated with the key.
     */
    public State add(OID key, State value) {
        addImp(key, value, true);
        return value;
    }

    private void addImp(OID oid, State state, boolean directFlag) {
        if (oid == null) {
            throw BindingSupportImpl.getInstance().illegalArgument(
                    "null oid not supported");
        }
        checkCompatible(state, oid);

        if (directFlag && directOid == null) {
            directOid = oid;
        }

        EntrySet.Entry e = data.get(oid);
        if (e == null) {
            data.add(oid, state, directFlag);
        } else {
            State current = (State) e.getValue();
            if (current == null) {
                e.setValue(state);
            } else if (state != null && current != state) {
                current.updateNonFilled(state);
            }
            if (directFlag) e.setDirect(true);
        }
    }

    /**
     * Check if the oid and state are compatible.
     */
    private void checkCompatible(State state, OID oid) {
        if (state != null && !(state instanceof NULLState)) {
            ClassMetaData keyCmd = oid.getAvailableClassMetaData();
            ClassMetaData stateCmd = state.getClassMetaData();
            // If data member of a class is of type interface, and if the
            // backend is Versant DB, the 'oid' will be of type VdsUntypedOID.
            // That class has just dummy implementations, or may throw excpetions,
            // or returns null.  So, check if keyCmd is null.  If it is, don't 
            // check for compatibility.
            if (null != keyCmd && !stateCmd.isAncestorOrSelf(keyCmd)) {
                System.out.println("\n\n\nkeyCmd = " + keyCmd.qname);
                System.out.println("stateCmd = " + stateCmd.qname);
                throw BindingSupportImpl.getInstance().internal("OID State mismatch");
            }
        }
    }

    public void addIndirect(OID key, State value) {
        addImp(key, value, false);
    }

    /**
     * Check if an entry is present in the table. This method is supplied to
     * support the use of values matching the reserved not found value.
     */
    public boolean containsKey(Object key) {
        return data.contains((OID) key);
    }

    /**
     * Find an entry in the table.
     */
    public State get(Object key) {
        EntrySet.Entry e = data.get((OID) key);
        if (e == null) return null;
        return (State) e.getValue();
    }

    public EntrySet.Entry getEntry(Object key) {
        return data.get((OID) key);
    }

    public void clear() {
        data.clear();
        directOid = null;
    }

    /**
     * Get the first direct OID or null if none.
     */
    public OID getDirectOID() {
        return directOid;
    }

    public void addIndirectOIDs(CachedQueryResult cacheContainer) {
        if (cacheContainer.indirectOIDs == null) {
            cacheContainer.indirectOIDs = new OIDArray();
        }
        OIDArray indirectOIDs = cacheContainer.indirectOIDs;

        for (Iterator i = iteratorForOIDs(); i.hasNext(); ) {
            OID oid = (OID)i.next();
            indirectOIDs.add(oid);
        }
    }

    public void writeExternal(OIDObjectOutput out) throws IOException {
        int n = data.size();
        out.writeInt(n);
        for (Iterator i = iterator(); i.hasNext(); ) {
            EntrySet.Entry e = (EntrySet.Entry)i.next();
            OID oid = (OID)e.getKey();
            State state = (State)e.getValue();
            if (state == null) {
                out.writeShort(-1);
                out.write(oid);
            } else if (state == NULLState.NULL_STATE) {
                out.writeShort(-2);
                out.write(oid);
            } else {
                out.writeShort(state.getClassIndex());
                oid.resolve(state);
                out.writeWithoutCMD(oid);
                state.writeExternal(out);
            }
            out.writeBoolean(e.direct);
        }
        out.write(directOid);
    }

    public void readExternal(OIDObjectInput in) throws IOException,
            ClassNotFoundException {
        ModelMetaData jmd = in.getModelMetaData();
        int n = in.readInt();
        for (int i = 0; i < n; i++) {
            OID oid;
            State state;
            int ci = in.readShort();
            switch (ci) {
                case -1:
                    oid = in.readOID();
                    state = null;
                    break;
                case -2:
                    oid = in.readOID();
                    state = NULLState.NULL_STATE;
                    break;
                default:
                    ClassMetaData cmd = jmd.classes[ci];
                    oid = in.readOID(cmd);
                    state = cmd.createState();
                    state.readExternal(in);
            };
            data.add(oid, state, in.readBoolean());
        }
        directOid = in.readOID();
    }

    public void dump() {
        System.out.println("-- StatesReturned");
        System.out.println("Map:");
        for (Iterator i = iterator(); i.hasNext(); ) {
            EntrySet.Entry e = (EntrySet.Entry)i.next();
            OID oid = (OID)e.getKey();
            State state = (State)e.getValue();
            System.out.println(oid.toStringImp() + " -> " + state + " direct " + e.isDirect());
        }
        System.out.println("---");
    }

}
