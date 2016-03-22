
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
package com.versant.core.vds;

import com.versant.core.vds.util.Loid;
import com.versant.core.vds.logging.VdsReadEvent;
import com.versant.core.vds.logging.VdsGroupReadEvent;
import com.versant.core.vds.logging.VdsLogEvent;
import com.versant.core.vds.metadata.VdsField;
import com.versant.core.common.Debug;
import com.versant.core.common.MapEntries;
import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.metadata.*;
import com.versant.core.server.StateContainer;
import com.versant.core.logging.LogEventStore;
import com.versant.core.common.BindingSupportImpl;
import com.versant.odbms.DatastoreManager;
import com.versant.odbms.LockMode;
import com.versant.odbms.Options;
import com.versant.odbms.model.DatastoreObject;
import com.versant.odbms.model.UserSchemaClass;
import com.versant.odbms.GroupOperationResult;

/**
 * This will efficiently fetch the graph of objects defined by a collection
 * of root objects (all same class) and a FetchGroup. It uses the minimum
 * number of group reads.
 */
public final class StateFetchQueue {

    private final ModelMetaData jmd;
    private final StateContainer container;
    private final DatastoreManager dsi;
    private final LockMode lockMode;
    private final short readObjectOptions;
    private final LogEventStore pes;

    private long[] loids;
    private OID[] oids;
    private State[] states;
    private FetchGroup[] fetchGroups;
    private FetchGroupField[] fields;
    private int size;

    private static final int INITIAL_CAPACITY = 32;

    public StateFetchQueue(ModelMetaData jmd, StateContainer container,
            DatastoreManager dsi, LockMode lockMode,
            short readObjectOptions, LogEventStore pes) {
        this.jmd = jmd;
        this.container = container;
        this.dsi = dsi;
        this.lockMode = lockMode;
        this.readObjectOptions = readObjectOptions;
        this.pes = pes;
    }

    /**
	 * Fetch the graph described by the object for oid and fetchGroup. The
	 * state for oid is returned and all other prefetched states are added
	 * to the container. The current parameter holds data we already have
	 * for the object or null if none. This is used to avoid having to fetch
	 * the main object just to get the LOID for the template class instance
	 * of a secondary field.
	 */
	public State fetch(OID oid, State current, FetchGroup fetchGroup) {
	    if (Debug.DEBUG) {
	        System.out.println("*** StateFetchQueue.fetch " +
	                Loid.asString(oid.getLongPrimaryKey()) +
	                " current " + current);
	    }
	
	    // If the fetch group contains any primary fields or current is null
	    // then we must read the main object. The secondary fields need LOIDs
	    // from the main object to fetch their template class instances.
	    State state;
        ClassMetaData cmd;
	    if (current == null) {
            long loid = oid.getLongPrimaryKey();
            DatastoreObject dso = new DatastoreObject(loid);
            readObject(dso, loid);
	        cmd = getClassMetaData(dso);
	        state = cmd.createState();
	        ((VdsState)state).readPrimaryFieldsFromDSO(dso);
	    } else {
	    	state = current.getCopy();
            cmd = state.getClassMetaData(jmd);
	    }

        // convert null fetchGroup to dfg for untyped OID
        if (fetchGroup == null) fetchGroup = cmd.fetchGroups[0];

	    // Add fetches for secondary fields and referenced objects to the
	    // queue according to the fetch group.
        addReferencedObjectsToQueue(state, fetchGroup);

	    // Process any prefetched instances in the queue and return.
	    processQueue();
	    return state;
	}

    /**
     * Add to the queue of States to be fetched.
     */
    public void add(OID oid, FetchGroup fg) {
        if (loids == null || size == loids.length) expand();
        oids[size] = oid;
        loids[size] = oid.getLongPrimaryKey();
        fetchGroups[size++] = fg;
    }

    /**
     * Fetch all of the OIDs added to the queue so far. The objects are added
     * to the container supplied in the constructor.
     * @see #add(com.versant.core.common.OID, com.versant.core.metadata.FetchGroup)
     */
    public void fetch() {
        processQueue();
    }

    /**
     * Add to the queue of States to be fetched.
     */
    private void add(OID oid, State current, FetchGroup fg) {
        if (loids == null || size == loids.length) expand();
        oids[size] = oid;
        loids[size] = oid.getLongPrimaryKey();
        states[size] = current;
        fetchGroups[size++] = fg;
    }

    /**
     * Add a template class instance to the queue to be fetched.
     */
    private void add(long loid, State current, FetchGroupField field) {
        if (loids == null || size == loids.length) expand();
        loids[size] = loid;
        states[size] = current;
        fields[size++] = field;
    }

    /**
     * Expand our parallel arrays.
     */
    private void expand() {
        if (loids == null) {
            loids = new long[INITIAL_CAPACITY];
            oids = new OID[INITIAL_CAPACITY];
            states = new State[INITIAL_CAPACITY];
            fetchGroups = new FetchGroup[INITIAL_CAPACITY];
            fields = new FetchGroupField[INITIAL_CAPACITY];
        } else {
            int oldLen = loids.length;
            int n = oldLen * 3 / 2 + 1;
            long[] la = new long[n];
            System.arraycopy(loids, 0, la, 0, oldLen);
            loids = la;
            OID[] oa = new OID[n];
            System.arraycopy(oids, 0, oa, 0, oldLen);
            oids = oa;
            State[] ca = new State[n];
            System.arraycopy(states, 0, ca, 0, oldLen);
            states = ca;
            FetchGroup[] ga = new FetchGroup[n];
            System.arraycopy(fetchGroups, 0, ga, 0, oldLen);
            fetchGroups = ga;
            FetchGroupField[] fa = new FetchGroupField[n];
            System.arraycopy(fields, 0, fa, 0, oldLen);
            fields = fa;
        }
    }

    /**
     * Recursively process all the queue entries adding States to the container
     * as required.
     */
    private void processQueue() {
        for (int pos = 0; pos < size;) {

            // read DSOs for all of the loids between pos and size
            int batchSize = size - pos;
            if (batchSize == 1) {
                // read and process the object
                if (Debug.DEBUG) System.out.println("Queued loids[" + pos + "] = " + Loid.asString(loids[pos]));
                DatastoreObject dso = new DatastoreObject(loids[pos]);
                readObject(dso, loids[pos]);
                // downgrade locks to NOLOCK if necessary
                if (readObjectOptions == Options.DOWNGRADE_LOCKS_OPTION) {
                	releaseLock(dso);
                }
                processDSO(dso, pos);
                ++pos;
            } else {
                // do group read
                DatastoreObject[] dsoBatch = new DatastoreObject[batchSize];
                for (int i = 0; i < batchSize; i++) {
                    dsoBatch[i] = new DatastoreObject(loids[pos + i]);
                }
                groupReadObjects(dsoBatch, pos);
                // downgrade locks to NOLOCK if necessary
                if (readObjectOptions == Options.DOWNGRADE_LOCKS_OPTION) {
                    groupReleaseLocks(dsoBatch, pos);
                }
                // process the batch of DSOs
                for (int i = 0; i < batchSize; i++, pos++) {
                    processDSO(dsoBatch[i], pos);
                }
            }
        }
    }

    /**
     * Process the DSO for the queue entry at pos.
     */
    private void processDSO(DatastoreObject dso, int pos) {
        ClassMetaData cmd = getClassMetaData(dso);

        if (cmd != null) {
            // PC instance. See if the container wants the State.
            OID oid = oids[pos];
            FetchGroup fg = fetchGroups[pos];
            if (!container.isStateRequired(oid, fg)) return;

            // Create and fill a State and put it in the container
            State state = cmd.createState();
            ((VdsState)state).readPrimaryFieldsFromDSO(dso);
            oid.resolve(state);
            container.addState(oid, state);

            // Now add all referenced objects in the fg to the queue
            // to be fetched and also queue fetches for secondary
            // fields.
            if (fg == null) fg = cmd.fetchGroups[0];
            else fg.resolve(cmd);
            addReferencedObjectsToQueue(state, fg);

        } else {
            // Secondary field template class instance.
            // Read the data, store it in the state and then add all
            // referenced objects to the queue.
            FetchGroupField f = fields[pos];
            FieldMetaData fmd = f.fmd;
            Object data = ((VdsField)fmd.storeField).readFromDSO(dso);
            State state = states[pos];
            state.setInternalObjectField(fmd.stateFieldNo, data);
            if (f.nextFetchGroup != null || f.nextKeyFetchGroup != null) {
                addReferencedObjectsToQueue(state, f);
            }
        }
    }

    /**
     * Add all referenced objects for fields in fg for state to the queue to
     * be fetched. These will be PC instances and instances of VDS template
     * classes. All the superclass fetch groups are processed recursively.
     */
    private void addReferencedObjectsToQueue(State state, FetchGroup fg) {
        FetchGroupField[] fields = fg.fields;
        for (int i = fields.length - 1; i >= 0; i--) {
            FetchGroupField f = fields[i];
            FieldMetaData fmd = f.fmd;
            if (fmd.secondaryField) {
                // the template class instance needs to be fetched so add it
                // to the queue
                long loid = state.getLongFieldInternal(
                        ((VdsField)fmd.storeField).getLoidField().stateFieldNo);
                if (loid != 0) {
                	add(loid, state, f);
                }
                else {
                	Object[] data = null; 
                	state.setInternalObjectField(fmd.stateFieldNo, data);
                }
            } else if (fmd.category == MDStatics.CATEGORY_POLYREF
                    ||  f.nextFetchGroup != null || f.nextKeyFetchGroup != null) {
                // all the data for the field will already be in state so
                // we can add the referenced objects
                addReferencedObjectsToQueue(state, f);
            }
        }
        if (fg.superFetchGroup != null) {
            addReferencedObjectsToQueue(state, fg.superFetchGroup);
        }
    }

    /**
     * Add all of the objects referenced by f in state to the queue. This must
     * only be called for fields referencing PC instances i.e. nextFetchGroup
     * or nextKeyFetchGroup is not null.
     */
    private void addReferencedObjectsToQueue(State state, FetchGroupField f) {
        if (f.doNotFetchObject) return;
        OID oid;
        OID[] oids;
        FetchGroup next;
        FieldMetaData fmd = f.fmd;
        switch (fmd.category) {

            case MDStatics.CATEGORY_POLYREF:
            case MDStatics.CATEGORY_REF:
                oid = (OID)state.getInternalObjectField(fmd.stateFieldNo);
                if (oid != null) addIfRequired(oid, f.nextFetchGroup);
                break;

            case MDStatics.CATEGORY_COLLECTION:
            case MDStatics.CATEGORY_ARRAY:
                oids = (OID[])state.getInternalObjectField(fmd.stateFieldNo);
                if (oids == null) break;
                next = f.nextFetchGroup;
                for (int i = 0; i < oids.length; i++) {
                	if (oids[i] != null)
                		addIfRequired(oids[i], next);
                }
                break;

            case MDStatics.CATEGORY_MAP:
                MapEntries me = (MapEntries)
                        state.getInternalObjectField(fmd.stateFieldNo);
                if (me == null) break;
                next = f.nextFetchGroup;
                if (next != null) {     // values are OIDs
                    Object[] values = me.values;
                    for (int i = values.length - 1; i >= 0; i--) {
                        addIfRequired((OID)values[i], next);
                    }
                }
                next = f.nextKeyFetchGroup;
                if (next != null) {     // keys are OIDs
                    Object[] keys = me.keys;
                    for (int i = keys.length - 1; i >= 0; i--) {
                        addIfRequired((OID)keys[i], next);
                    }
                }
                break;

            default:
                throw BindingSupportImpl.getInstance().internal("Unhandled category " +
                        MDStaticUtils.toCategoryString(fmd.category));
        }
    }

    /**
     * Add oid to the queue to be fetched if it is required by the container.
     */
    private void addIfRequired(OID oid, FetchGroup next) {
        if (container.isStateRequired(oid, next)) {
            add(oid, null, next);
        }
    }

    /**
     * Format an array of LOIDs into a space separated String with a leading
     * space.
     */
    private static String formatLoids(long[] loids) {
        StringBuffer s = new StringBuffer();
        int n = loids.length;
        for (int i = 0; i < n; i++) {
            s.append(' ');
            s.append(Loid.asString(loids[i]));
        }
        return s.toString();
    }

    /**
     * Get the ClassMetaData for a DSO or null if it is not for a PC instance.
     */
    private static ClassMetaData getClassMetaData(DatastoreObject dso) {
        UserSchemaClass us = (UserSchemaClass)dso.getSchemaClass().getUserObject();
        if (us == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "Null 'UserSchemaClass' [getUserObject()] for dso '" + dso + "'");
        }
        return (ClassMetaData)us.getUserObject();
    }


    /**
     * Read a group of objects logging an event if required. The loids are
     * assumed to be at position pos in loids if an event is logged.
     */
    private void groupReadObjects(DatastoreObject[] dsoBatch, int pos) {
        VdsGroupReadEvent ev;
        if (pes.isFine()) {
            long[] a = new long[dsoBatch.length];
            System.arraycopy(loids, pos, a, 0, a.length);
            ev = new VdsGroupReadEvent(VdsLogEvent.GROUP_READ, 0,
                    lockMode, readObjectOptions, a);
            pes.log(ev);
        } else {
            ev = null;
        }
        try {
            GroupOperationResult res =
                    dsi.groupReadObjects(dsoBatch, lockMode,
                            readObjectOptions);
            if (res != null) {
                throw BindingSupportImpl.getInstance().objectNotFound(
                        "Objects not found:" + formatLoids(
                                res.objectsNotFound));
            }
        } catch (RuntimeException e) {
            if (ev != null) ev.setErrorMsg(e);
            throw e;
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    /**
     * Read a group of objects logging an event if required. The loids are
     * assumed to be at position pos in loids if an event is logged.
     */
    private void groupReleaseLocks(DatastoreObject[] dsoBatch, int pos) {
        VdsGroupReadEvent ev;
        if (pes.isFine()) {
            long[] a = new long[dsoBatch.length];
            System.arraycopy(loids, pos, a, 0, a.length);
            ev = new VdsGroupReadEvent(VdsLogEvent.GROUP_LOCK, 0,
                    LockMode.NOLOCK,
                    Options.DOWNGRADE_LOCKS_OPTION, a);
            pes.log(ev);
        } else {
            ev = null;
        }
        try {
            dsi.groupLockObjects(dsoBatch, LockMode.NOLOCK,
                    Options.DOWNGRADE_LOCKS_OPTION);
        } catch (RuntimeException e) {
            if (ev != null) ev.setErrorMsg(e);
            throw e;
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    /**
     * Read a group of objects logging an event if required. The loids are
     * assumed to be at position pos in loids if an event is logged.
     */
    private void releaseLock(DatastoreObject dso) {
        VdsReadEvent ev;
        if (pes.isFine()) {
            ev = new VdsReadEvent(VdsLogEvent.LOCK, 0,
                    LockMode.NOLOCK,
                    Options.DOWNGRADE_LOCKS_OPTION, dso.getLOID());
            pes.log(ev);
        } else {
            ev = null;
        }
        try {
            dsi.lockObject(dso, LockMode.NOLOCK,
                    Options.DOWNGRADE_LOCKS_OPTION);
        } catch (RuntimeException e) {
            if (ev != null) ev.setErrorMsg(e);
            throw e;
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    /**
     * Read an object logging an event if required.
     */
    private void readObject(DatastoreObject dso, long loid) {
        VdsReadEvent ev;
        if (pes.isFine()) {
            ev = new VdsReadEvent(VdsLogEvent.READ, 0, lockMode,
                    readObjectOptions, loid);
            pes.log(ev);
        } else {
            ev = null;
        }
        try {
            if (!dsi.readObject(dso, lockMode, readObjectOptions)) {
                throw BindingSupportImpl.getInstance().objectNotFound(
                        "Object not found: " + Loid.asString(loid));
            }
        } catch (RuntimeException e) {
            if (ev != null) ev.setErrorMsg(e);
            throw e;
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

}

