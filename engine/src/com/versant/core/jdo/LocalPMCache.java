
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
package com.versant.core.jdo;

import com.versant.core.common.*;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStatics;

import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.Iterator;

/**
 * This is an implementation PM managed cache that uses a linked list to
 * reference the colisions in the collection.
 */
public final class LocalPMCache {
    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 16;
    /**
     * The load factor used when none specified in constructor.
     **/
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /**
     * The load factor for the hash table.
     */
    final float loadFactor;
    /**
     * The next size value at which to resize (capacity * load factor).
     */
    int threshold;
    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * Reference queue for cleared WeakKeys
     */
    public ReferenceQueue queue = new ReferenceQueue();
    private VersantPersistenceManagerImp pm;
    private final TransactionalList processList = new TransactionalList();
    private  boolean overWriteMode;

    private int currentRefType = VersantPersistenceManager.PM_CACHE_REF_TYPE_SOFT;

    /**
     * Array of value table slots.
     */
    private PMCacheEntry[] m_keyTable;

    /**
     * The number of key-value mappings contained in this identity hash map.
     */
    transient int size;

    private final int createdSize;
    private final int createdThreshold;

    public LocalPMCache() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        createdThreshold = threshold = 
            (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        m_keyTable = new PMCacheEntry[DEFAULT_INITIAL_CAPACITY];
        createdSize = DEFAULT_INITIAL_CAPACITY;
    }

    public LocalPMCache(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public LocalPMCache(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity)
            capacity <<= 1;

        this.loadFactor = loadFactor;
        createdThreshold = threshold = (int)(capacity * loadFactor);
        m_keyTable = new PMCacheEntry[capacity];
        createdSize = initialCapacity;
    }

    public int getCurrentRefType() {
        return currentRefType;
    }

    public void setCurrentRefType(int currentRefType) {
        checkRefType(currentRefType);
        this.currentRefType = currentRefType;
    }

    public static void checkRefType(int currentRefType) {
        if (currentRefType != VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK
                && currentRefType != VersantPersistenceManager.PM_CACHE_REF_TYPE_SOFT
                && currentRefType != VersantPersistenceManager.PM_CACHE_REF_TYPE_STRONG) {
            throw BindingSupportImpl.getInstance().invalidOperation("The option '"
                    + currentRefType + "' is not a valid choice for a PMCacheRefType.");
        }
    }

    public boolean isOverWriteMode() {
        return overWriteMode;
    }

    public void setOverWriteMode(boolean overWriteMode) {
        this.overWriteMode = overWriteMode;
    }

    public void setPm(VersantPersistenceManagerImp pm) {
        this.pm = pm;
    }

    /**
     * Add a newly created sm to the managed cache.
     */
    public PCStateMan add(PCStateMan sm) {
        //this will create a realOID if needed
        sm.getRealOIDIfAppId();
        addSm(sm.oid.getAvailableOID(), sm);
        addForProcessing(sm);
        if (sm.isTx()) pm.addTxStateObject(sm);
        return sm;
    }

    public PCStateMan add(OID oid, State state, PCStateMan[] sms) {
        return addState(oid, state, true, sms);
    }

    /**
     * Provide the oid-state pair to local cache. This will not result in a PCStateman
     * being created.
     */
    public void addStateOnly(OID oid, State state) {
        addState(oid, state, false, null);
    }

    /**
     * This must create a CacheEntryBase for the sm.
     */
    public PMCacheEntry createCacheKey(PCStateMan sm) {
        if (sm.cacheEntry != null) {
            throw BindingSupportImpl.getInstance().internal("StateManager already has a PMCacheEntry");
        }
        return sm.cacheEntry = new PMCacheEntry(currentRefType, sm, queue, pm.getTxnCounter());
    }

    /**
     * This must create a CacheEntryBase for the sm.
     */
    public PMCacheEntry createCacheKey(OID oid, State state) {
        return new PMCacheEntry(currentRefType, oid, state, queue, pm.getTxnCounter());
    }

    /**
     * Add to the head of the processList.
     */
    public PCStateMan addForProcessing(PCStateMan sm) {
        if (!pm.isActive()) return sm;
        processList.add(sm);
        return sm;
    }

    PCStateMan updateSm(State value, PCStateMan sm, OID key) {
        if (value == NULLState.NULL_STATE) {
            /**
             * Must throw exception as the instance was deleted from under us
             */
            throw BindingSupportImpl.getInstance().objectNotFound("No row for " +
                    sm.getClassMetaData().storeClass + " " + key.toSString());
        }

        if (sm != null) {
            sm.updateWith(value, pm, overWriteMode);
            addForProcessing(sm);
        }
        return sm;
    }

    /**
     * Process the ReferenceQueue holding keys for GCed values.
     */
    public void processReferenceQueue() {
        processReferenceQueueImp();
    }

    /**
     * Remove all invalidated entries from the map, that is, remove all entries
     * whose keys have been discarded.  This method should be invoked once by
     * each public mutator in this class.  We don't invoke this method in
     * public accessors because that can lead to surprising
     * ConcurrentModificationExceptions.
     */
    private void processReferenceQueueImp() {
        PMCacheEntryOwnerRef ref;
        while ((ref = (PMCacheEntryOwnerRef)queue.poll()) != null) {
            PMCacheEntry ce = ref.getOwner();
			if (!ce.hasReference(ref))
				// the ref of the PMCacheEntry might have changed,
				// due to PMCacheEntry.upgradeToSm(). In this case,
				// we must not remove the entry. 

				continue;
            removeImp(ce, m_keyTable, indexFor(ce.mappedOID.hashCode(), m_keyTable.length));
        }
    }

    public void doCommit(boolean retainValues) {
        processReferenceQueueImp();
        //only do processList
        Iterator iter = processList.iterator();
        while (iter.hasNext()) {
            PMCacheEntry ce = (PMCacheEntry) iter.next();
            PCStateMan sm = (PCStateMan) ce.get();
            if (sm != null) {
                sm.commit(pm);
            }
        }
        processList.clear();
    }

    public void doRollback(boolean retainValues) {
        processReferenceQueueImp();
        Iterator iter = processList.iterator();
        while (iter.hasNext()) {
            PMCacheEntry ce = (PMCacheEntry) iter.next();
            PCStateMan sm = (PCStateMan) ce.get();
            if (sm != null) {
                sm.rollback();
            }
        }
        processList.clear();
    }

    public void doRefresh(boolean strict) {
        Iterator iter = processList.iterator();
        while (iter.hasNext()) {
            PMCacheEntry ce = (PMCacheEntry) iter.next();
            PCStateMan sm = (PCStateMan) ce.get();
            if (sm != null) {
                sm.refresh();
            }
        }
    }
    
    public void doMarkReloadNeeded()
    {
        processReferenceQueueImp();  
        PMCacheEntry[] src = m_keyTable;
        for (int j = 0; j < src.length; j++) {
            PMCacheEntry e = src[j];
            while (e != null) {
                Object o = e.get();
                if (o != null && (o instanceof PCStateMan)) {
                    ((PCStateMan)o).setLoadRequiredIfNeeded();
                }
                e = e.next;
            }
        }                      
    }

    /**
     * This add the real oid of the NewOID to the mapping.
     */
    public void addRealOID(PCStateMan sm) {
        reMapWithRealOID(sm);
    }

    private void reMapWithRealOID(PCStateMan sm) {
        if (Debug.DEBUG) {
            validate();
            if (!sm.oid.isNew()) {
                throw BindingSupportImpl.getInstance().internal("The instance is not new");
            }
            if (sm.cacheEntry.mappedOID != sm.oid) {
                throw BindingSupportImpl.getInstance().internal("The instance is not mapped with its newOID");
            }
            if (sm.oid.getRealOID() == null) {
                throw BindingSupportImpl.getInstance().internal("The realOID may not be null: " + sm.oid);
            }
        }

        if (sm.cacheEntry.mappedOID == sm.oid.getRealOID()) return;
        final int currentIndex = indexFor(sm.cacheEntry.hash, m_keyTable.length);
        final int newIndex = indexFor(sm.oid.getRealOID().hashCode(), m_keyTable.length);

        if (currentIndex == newIndex) {
            OID realOID = sm.oid.getRealOID();
            sm.cacheEntry.reHash(realOID);
            realOID.resolve(sm.state);
            return;
        }
        //remove from current pos
        if (sm.cacheEntry.prev == null) {
            m_keyTable[currentIndex] = sm.cacheEntry.next;
        }
        sm.cacheEntry.unlinkNextList();

        OID realOID = sm.oid.getRealOID();
        sm.cacheEntry.reHash(realOID);
        realOID.resolve(sm.state);

        sm.cacheEntry.setNext(m_keyTable[newIndex]);
        m_keyTable[newIndex] = sm.cacheEntry;
    }

    public void checkModelConsistency() {
        processReferenceQueueImp();
        PMCacheEntry[] src = m_keyTable;
        for (int j = 0; j < src.length; j++) {
            PMCacheEntry e = src[j];
            if (e != null) {
                Object val = e.get();
                if (val != null && (val instanceof PCStateMan)) {
                    ((PCStateMan)val).checkModelConsistency();
                }
                e = e.next;
            }
        }
    }

    /**
     * If the instance for this oid is already managed then it will be updated
     * with the state information.
     * Else the instance will be managed with this state.
     */
    protected PCStateMan addState(OID key, State value, boolean manage, PCStateMan[] addSm) {
        if (Debug.DEBUG) validate();
        final PMCacheEntry[] m_keyTable = this.m_keyTable;
        final int hash = key.hashCode();
        final int i = indexFor(hash, m_keyTable.length);

        for (PMCacheEntry e = m_keyTable[i]; e != null; e = e.next) {
            if (e.hashCode() == hash && eq(key, e.mappedOID)) {
                Object o = e.get();
                if (o == null) {
                    removeImp(e, m_keyTable, i);
                    break;
                }
                //state is cleared and freshly populated if the "doRefreshPNTs" option was set
                //and only in an active optimistic txn and if there is a managed instance
                //(and not just a State), then this managed instance must be in PNT state
                boolean txnCounterIsInvalid = pm.doRefreshPNTObjects(e.txnCounter) && pm.isActiveOptimistic(); 
                if (o instanceof PCStateMan) {
                    PCStateMan sm = (PCStateMan)o;
                    if( txnCounterIsInvalid )
                    {
                        if( sm.isPNonTx() )
                        {
                            sm.state.clear();
                            //read-access to pk fields is not mediated, so the loaded status in PCStateMan does not matter
                            sm.reset();
                        }
                        e.txnCounter = pm.getTxnCounter(); 
                    }
                    return updateSm(value, sm, key);                 
                } else {
                    State currentState = (State)o;
                    if (value == NULLState.NULL_STATE) {
                        removeImp(e, m_keyTable, i);
                        return null;
                    } else {
                        if (overWriteMode || txnCounterIsInvalid) {
                            currentState.clear();                         
                            if( txnCounterIsInvalid )
                            {
                                final ClassMetaData cmd = currentState.getClassMetaData();
                                if( cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION )
                                {
                                    currentState.copyFields(key); 
                                }
                                e.txnCounter = pm.getTxnCounter();   
                            }  
                        }                          
                        if (manage) {
                            value.updateNonFilled(currentState);
                            return e.upgradeToSm(addSm[0] = pm.reManage(key, value), queue);
                        } else {
                            currentState.updateNonFilled(value);
                            return null;
                        } 
                    }
                }               
            }
        }
        if (Debug.DEBUG) validate();
        if (value == NULLState.NULL_STATE) {
            //ignore
            return null;
        }
        //add new entry
        PMCacheEntry ce;
        PCStateMan sm = null;
        if (manage) {
            //create sm and add
            ce = createCacheKey(sm = addSm[0] = pm.reManage(key, value));
        } else {
            //just add the oid-state pair
            ce = createCacheKey(key, value);
        }
        ce.setNext(m_keyTable[i]);
        m_keyTable[i] = ce;
        if (size++ >= threshold) resize(2 * m_keyTable.length);
        if (Debug.DEBUG) validate();
        return sm;
    }

    /**
     * Remove the entry. Note that this is a NOP if it is not in the cache.
     */
    private void removeImp(PMCacheEntry e, final PMCacheEntry[] m_keyTable, final int i) {
        if (Debug.DEBUG) validate();
        if (m_keyTable[i] == null) return;
        if (m_keyTable[i] == e) {
            m_keyTable[i] = e.next;
            clearCE(e);
        } else {
            for (PMCacheEntry ce = m_keyTable[i].next; ce != null; ce = ce.next) {
                if (ce == e) {
                    clearCE(e);
                    break;
                }
            }

        }
        if (Debug.DEBUG) validate();
    }

    private void clearCE(PMCacheEntry e) {
        e.unlinkNextList();
        e.clear();
        size--;
    }

    /**
     * This method does not replace the {@link PCStateMan} if the {@link OID} is present.
     * If the mapping exist it will be verified.
     * If the mapping does not exist it will be created.
     */
    public PCStateMan addSm(OID key, PCStateMan value) {
        key = key.getAvailableOID();
        final PMCacheEntry[] m_keyTable = this.m_keyTable;
        final int hash = key.hashCode();
        final int i = indexFor(hash, m_keyTable.length);

        for (PMCacheEntry e = m_keyTable[i]; e != null; e = e.next) {
            if (e.hash == hash && eq(key, e.mappedOID)) {
                Object o = e.get();
                if (o != null) {
                    if (o != null && o != value) {
                        throw BindingSupportImpl.getInstance().internal("Inconsistent mapping for id '" + key.toPkString() + "'");
                    }
                    return value;
                }
                remove(e);
                break;
            }
        }
        //add new entry
        PMCacheEntry ce = value.cacheEntry;
        if (ce == null) {
            ce = createCacheKey(value);
        }

        ce.setNext(m_keyTable[i]);
        m_keyTable[i] = ce;
        if (size++ >= threshold) resize(2 * m_keyTable.length);

        if (Debug.DEBUG) validate();
        return value;
    }

    public void setInterceptDfgFieldAccess(boolean on) {
        processReferenceQueueImp();

        PMCacheEntry[] src = m_keyTable;
        for (int j = 0; j < src.length; j++) {
            PMCacheEntry e = src[j];
            while (e != null) {
                Object o = e.get();
                if (o != null && (o instanceof PCStateMan)) {
                    ((PCStateMan)o).setInterceptDfgFieldAccess(on);
                }
                e = e.next;
            }
        }
    }

    public void evict() {
        PMCacheEntry[] src = m_keyTable;
        for (int j = 0; j < src.length; j++) {
            PMCacheEntry e = src[j];
            while (e != null) {
                Object o = e.get();
                if (o != null && (o instanceof PCStateMan)) {
                    ((PCStateMan)o).evict();
                }
                e = e.next;
            }
        }
        processReferenceQueueImp();
    }

    /**
     * Return the state if present. This will not result in the PCStateMan being
     * created if not currently managed.
     */
    public State getStateByOID(OID oid) {
        Object value = getValueByOid(oid);
        if (value == null) return null;
        if (value instanceof PCStateMan) {
            return ((PCStateMan) value).state;
        } else {
            return (State) value;
        }
    }

    /**
     * Do we have a State or PCStateMan for the oid? Note the data may be
     * evicted at any time depending on the reference type.
     */
    public boolean contains(OID oid) {
        return (getValueByOid(oid) != null);
    }

    /**
     * If the sm is already managed then return it. If the oid and state is present
     * then manage it and return it. Else return null.
     */
    public PCStateMan getByOID(OID oid, boolean manage) {
        PMCacheEntry ce = getByOID(oid.getAvailableOID());
        if (ce == null) return null;
        Object value = ce.get();
        if (value == null) {
            remove(ce);
            return null;
        }
        if (value instanceof PCStateMan) {
            return (PCStateMan) value;
        }
        if (!manage) return null;
        return ce.upgradeToSm(pm.reManage(oid, (State)value), queue);
    }

    /**
     * If the sm is already managed then return it. If the oid and state is present
     * then manage it and return it. Else return null. This looks up using the
     * oid as is and does not assume that realOID has been set.
     */
    public PCStateMan getByNewObjectOID(NewObjectOID oid) {
        PMCacheEntry ce = getByOID(oid);
        if (ce == null) return null;
        Object value = ce.get();
        if (value == null) {
            remove(ce);
            return null;
        }
        if (value instanceof PCStateMan) {
            return (PCStateMan) value;
        }
        return ce.upgradeToSm(pm.reManage(oid, (State)value), queue);
    }

    private Object getValueByOid(OID oid) {
        PMCacheEntry ce = getByOID(oid.getAvailableOID());
        if (ce == null) return null;
        Object value = ce.get();
        if (value == null) {
            remove(ce);
            return null;
        }
        return value;
    }

    private PMCacheEntry getByOID(OID oid) {
        final int hash = oid.hashCode();
        for (PMCacheEntry e = m_keyTable[indexFor(hash, m_keyTable.length)]; e != null; e = e.next) {
            if (e.hash == hash && eq(oid, e.mappedOID)) {
                return e;
            }
        }
        return null;
    }

    public void clear() {
        PMCacheEntry[] src = m_keyTable;
        for (int j = 0; j < src.length; j++) {
            PMCacheEntry e = src[j];
            PMCacheEntry next;
            while (e != null) {
                next = e.next;
                e.reset();
                e.next = null;
                e.prev = null;
                e = next;
            }
        }
        processList.clear();
        size = 0;
        m_keyTable = new PMCacheEntry[createdSize];
        threshold = createdThreshold;        
    }

    public boolean inProcessList(PCStateMan sm) {
        return processList.contains(sm.cacheEntry);
    }

    public void remove(PCStateMan sm) {
        remove(sm.cacheEntry);
    }

    /**
     * Remove entry from collection. This must also ensure that the entry
     * is removed from the processList.
     */
    private void remove(PMCacheEntry ce) {
        if (Debug.DEBUG) validate();
        int hash = ce.mappedOID.hashCode();
        int i = indexFor(hash, m_keyTable.length);
        PMCacheEntry e = m_keyTable[i];
        for (;e != null; e = e.next) {
            if (e.hash == hash && eq(ce.mappedOID, e.mappedOID)) {
                removeImp(e, m_keyTable, i);
            }
        }
        if (Debug.DEBUG) validate();
    }

    /**
     * This will validate the consistency of the cache. Only for debugging
     */
    private Set[] validate() {
        int count = 0;
//        ObjectHashSet[] sets = {new ObjectHashSet(size, ObjectHashSet.IDENTITY_COMP), new ObjectHashSet(size, ObjectHashSet.IDENTITY_COMP)};
        PMCacheEntry[] src = m_keyTable;
        for (int j = 0; j < src.length; j++) {
            PMCacheEntry e = src[j];
            while (e != null) {
                int index = indexFor(e.mappedOID.hashCode(), m_keyTable.length);
                if (index != j) {
                    throw BindingSupportImpl.getInstance().internal("The entry is not at the correct pos");
                }
//                Object val = e.get();
//                if (!sets[0].add(e)) {
//                    throw BindingSupportImpl.getInstance().internal("The entry "
//                            + e + " is more than once in the cache");
//                }
//                if (val != null) {
//                    if (!sets[1].add(val)) {
//                        throw BindingSupportImpl.getInstance().internal("The value " + val + " is in the cache more than once");
//                    }
//                }
                count++;
                e = e.next;
            }
        }

        if (count != size) {
            throw BindingSupportImpl.getInstance().internal("The counted size == " + count + " but size is " + size);
        }
//        return sets;
        return null;
    }

    public void dump() {
        System.out.println("\n\n\nLocalPMCache.dump: START");
        PMCacheEntry[] src = m_keyTable;
        for (int j = 0; j < src.length; j++) {
            PMCacheEntry e = src[j];
            boolean first = true;
            while (e != null) {
                if (first) {
                    System.out.println("j = " + j);
                    first = false;
                }
                System.out.println("e = " + e);
                e = e.next;
            }
        }
        System.out.println("LocalPMCache.dump: END \n\n\n");
    }

    private void resize(int newCapacity) {
        if (Debug.DEBUG) validate();
        PMCacheEntry[] oldTable = m_keyTable;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        PMCacheEntry[] newTable = new PMCacheEntry[newCapacity];
        transfer(newTable);
        m_keyTable = newTable;
        threshold = (int)(newCapacity * loadFactor);
        if (Debug.DEBUG) validate();
    }

    /**
     * Transfer all entries from current table to newTable.
     */
    private void transfer(PMCacheEntry[] newTable) {
        PMCacheEntry[] src = m_keyTable;
        int newCapacity = newTable.length;
        size = 0;
        for (int j = 0; j < src.length; j++) {
            PMCacheEntry e = src[j];
            if (e != null) {
                //skip entries with gc'd refs
                for (;;) {
                    if (e == null) break;
                    if (e.get() == null) {
                        e.clear();
                        e = e.next;
                    } else {
                        break;
                    }
                }
                if (e == null) continue;
                src[j] = null;

                do {
                    size++;
                    PMCacheEntry next = e.next;
                    int i = indexFor(e.hash, newCapacity);
                    e.unlinkNextList();
                    e.setNext(newTable[i]);
                    newTable[i] = e;
                    e = next;
                } while (e != null);
            }
        }
    }

    /**
     * Check for equality of non-null reference x and possibly-null y.
     */
    static boolean eq(OID x, OID y) {
        return x == y || x.equals(y);
    }

    /**
     * Returns index for hash code h.
     */
    static int indexFor(int h, int length) {
        return h & (length-1);
    }

    /**
     * How many keys are in the cache?
     */
    public int size() {
        return size;
    }

}
