
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
package com.versant.core.server;

import com.versant.core.common.Debug;
import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.metadata.ModelMetaData;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * A graph of OID's and State's used to represent a collection of objects
 * to be persisted. This class implements algorithms to sort the graph
 * for persisting in the correct order and to detect cycles. This class
 * only does a partial sort of the graph and is suitable for graphs that
 * do not contain any new instances with post-insert key generators.
 */
public class PersistGraph extends OIDGraph {

    protected ModelMetaData jmd;
    protected OID[] oids;
    protected State[] oldStates;
    protected State[] newStates;
    protected Map oidIndexMap;
    
    public boolean optimistic; // Carries this flag for VDS transaction

    public PersistGraph(ModelMetaData jmd, int maxSize) {
        this.jmd = jmd;
        oids = new OID[maxSize];
        oldStates = new State[maxSize];
        newStates = new State[maxSize];
        size = 0;
        oidIndexMap = new HashMap();
    }

    /**
     * Empty the graph so it can be reused.
     */
    public void clear() {
        for (int i = size - 1; i >= 0; i--) {
            oids[i] = null;
            oldStates[i] = null;
            newStates[i] = null;
        }
        size = 0;
        oidIndexMap.clear();
    }

    /**
     * Add a node to the graph.
     */
    public void add(OID oid, State oldState, State newState) {
        oids[size] = oid;
        oldStates[size] = oldState;
        newStates[size] = newState;
        oidIndexMap.put(oid, new Integer(size++));
    }

    /**
     * How many nodes are in the graph?
     */
    public int size() {
        return size;
    }

    /**
     * Get the OID at index.
     */
    public OID getOID(int index) {
        return oids[index];
    }

    /**
     * Get the old State at index.
     */
    public State getOldState(int index) {
        return oldStates[index];
    }

    /**
     * Get the new State at index.
     */
    public State getNewState(int index) {
        return newStates[index];
    }

    /**
     * Dump the graph to System.out for debugging.
     */
    public void dump() {
        for (int i = 0; i < size; i++) dump(i);
    }

    /**
     * Dump entries at index to System.out for debugging.
     */
    public void dump(int index) {
        if (Debug.DEBUG) {
            System.out.println("[" + index + "] " + oids[index] + " " +
                    newStates[index]);
        }
    }

    /**
     * Find the index of OID in the graph or less than 0 if not found.
     * Calling this method after persist has been called will return incorrect
     * results.
     *
     * @see #indexOfAfterPersist
     */
    public int indexOf(OID oid) {
        Object o = oidIndexMap.get(oid);
        return o == null ? -1 : ((Integer)o).intValue();
    }

    /**
     * Find the index of OID in the graph or less than 0 if not found.
     * This method may be called after persist has been called. It will
     * build the OID to index map the first time it is called.
     *
     * @see #indexOf
     */
    public int indexOfAfterPersist(OID oid) {
        if (oidIndexMap.size() == 0) {
            OID[] oids = this.oids;
            for (int i = size - 1; i >= 0; i--) {
                oidIndexMap.put(oids[i], new Integer(i));
            }
        }
        Object o = oidIndexMap.get(oid);
        return o == null ? -1 : ((Integer)o).intValue();
    }

    /**
     * Bump up timestamps etc.
     */
    public void doAutoSets() {
        Date now = new Date();
        for (int i = 0; i < size; i++) {
            OID oid = oids[i];
            State ns = newStates[i];
            if (ns == null) break;
            ns.setClassMetaData(oid.getClassMetaData());
            if (oid.isNew()) {
                ns.updateAutoSetFieldsCreated(now);
            } else {
                ns.updateAutoSetFieldsModified(now, oldStates[i]);
            }
        }
    }

    /**
     * Compare graph entries at and a and b. Return 0 if equal, less than 0
     * if a is less than b or greater than 0 if a is greater than b. This
     * orders entries by class referenceGraphIndex, by new objects first,
     * by field numbers.
     */
    protected int compare(int a, int b) {
        final OID oidA = oids[a];
        final OID oidB = oids[b];

        int diff = oidB.getClassMetaData().referenceGraphIndex
                - oidA.getClassMetaData().referenceGraphIndex;
        if (diff != 0) return diff;

        // by new objects
        boolean newA = oidA.isNew();
        boolean newB = oidB.isNew();
        if (newA && !newB) return -1;
        if (!newA && newB) return +1;

        // by field numbers
        return newStates[a].compareToPass1(newStates[b]);
    }

    /**
     * Swap entries.
     */
    protected void swap(int index1, int index2) {
        OID tempStr = oids[index1];
        State tempOldState = oldStates[index1];
        State tempNewState = newStates[index1];

        oids[index1] = oids[index2];
        oldStates[index1] = oldStates[index2];
        newStates[index1] = newStates[index2];

        oids[index2] = tempStr;
        oldStates[index2] = tempOldState;
        newStates[index2] = tempNewState;
    }
}
