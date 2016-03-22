
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
package com.versant.core.ejb;

import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.NewObjectOID;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * This is a cache that keeps references to instances that are managed by an EntityManager.
 * The type of reference can be weak/soft/hard.
 *
 *
 * -- Mapping of instance by oid. --
 * We will support both application and datastore identity for ejb3.
 *
 *
 * Map only by a sinle id(new or real).
 * If the instance only has a new oid then it will be mapped by new.
 * If the instance is new but has already a real oid then we must keep a separate
 * mapping of real to new. This realToNew mapping can be cleared on commit.
 *
 * If the instance is 'existing' then it is mapped by the real oid.
 *
 */
public class LocalCache {
    /**
     * This is the set of managed instances.
     */
    private final LinkedListEntrySet set = new LinkedListEntrySet();
    private final Map realToNewMap = new HashMap();

    public LocalCache() {
    }

    /**
     * Add a new created instance to the instance cache.
     * If there is such an mapping?
     * @param oid
     * @param sm
     */
    public NewObjectOID addNewInstance(NewObjectOID oid, StateManagerImp sm) {
        if (set.contains(oid)) {
            throw BindingSupportImpl.getInstance().internal("There is already an mapping for '" + oid + "' ");
        }
        set.addEntry(new CacheEntry(oid, sm));
        if (oid.realOID != null) {
            mapByRealOID(oid);
        }
        return oid;
    }

    /**
     * Map the instance by its real oid.
     * @param oid
     */
    public void mapByRealOID(NewObjectOID oid) {
        Object replaced = realToNewMap.put(oid.realOID, oid);
        if (replaced != null && !oid.equals(replaced)) {
            throw BindingSupportImpl.getInstance().internal(
                    "The new instance is already mapped by another non-eqaul real-oid");
        }
    }

    /**
     * Add a sm to the cache. This method does not expect to find a existing
     * mapping by this id in the cache.
     * @param oid
     * @param sm
     */
    public StateManagerImp add(OID oid, StateManagerImp sm) {
        CacheEntry e = (CacheEntry) set.get(oid);
        if (e == null) {
            set.addEntry(createEntry(oid, sm));
        } else {
            if (e.val == null) {
                throw BindingSupportImpl.getInstance().internal("Cache entry with no value");
            } else if (!(e.val instanceof State)) {
                /**
                 * This exception migh occor if the user assigned a already existing
                 * id to a new instance and persisted it.
                 */
                throw new RuntimeException("There is already an instance in the cache with same id");
            } else {
                e.val = sm;
            }
        }
        return sm;
    }

    public void add(OID oid, State state) {
        CacheEntry e = (CacheEntry) set.get(oid);
        if (e == null) {
            set.addEntry(createEntry(oid, state));
        } else {
            Object value = e.val;
            if (value instanceof StateManagerImp) {
                StateManagerImp sm = (StateManagerImp) value;
                sm.updateState(state);
            } else {
                ((State) value).updateNonFilled(state);
            }
        }
    }

    public void remove(OID oid) {
    }

    public Object get(OID oid) {
        CacheEntry e = (CacheEntry) set.get(oid);
        if (e != null) {
            return e.getValue();
        }
        OID realOID = (OID) realToNewMap.get(oid);
        if (realOID != null) {
            e = (CacheEntry) set.get(realOID);
            if (e != null) {
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * Create a entry instance to be used. Must determine if this should be a weak/soft/hard entry.
     * @param oid
     * @param value
     */
    private CacheEntry createEntry(OID oid, Object value) {
        return new CacheEntry(oid, value);
    }

    public void clear() {
        set.clear();
        realToNewMap.clear();
    }


    /**
     * This is a wrapper instance that holds oid-pcstateman or oid-state pairs in the cache.
     */
    public class CacheEntry extends LinkedListEntrySet.LinkedEntry {
        private Object val;
        /**
         * This is a field that is used to determine if this instance is out of sync with the
         * current running jdo transaction.
         */
        public int localTxVersion;

        public CacheEntry(OID key, Object val) {
            super(key);
            this.val = val;
        }

        public Object getValue() {
            return val;
        }
    }

    public void processReferenceQueue() {
    }

    public Iterator getCacheIterator() {
        processReferenceQueue();
        return set.createLinkedListIterator();
    }
}
