
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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import com.versant.core.common.*;

/**
 * This is a wrapper instance used to represent an entry in the pm ManagedCache.
 * It can either represent a OID-State pair or an OID-PCStateMan pair.
 * It both cases the reference type can be weak or soft or hard.
 */
public class PMCacheEntry {

    /**
     * The soft, weak or hard ref.
     */
    private Object ref;
    public int txnCounter;  //at what txn were the data current?
    /**
     * This is a linked list of items to process at the end of the tx.
     */
    public PMCacheEntry processListNext;
    public PMCacheEntry processListPrev;

    public PMCacheEntry next;
    public PMCacheEntry prev;

    public int hash;
    /**
     * The type of ref(soft, weak or hard)
     * @see com.versant.core.jdo.VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK
     * @see com.versant.core.jdo.VersantPersistenceManager.PM_CACHE_REF_TYPE_SOFT
     * @see com.versant.core.jdo.VersantPersistenceManager.PM_CACHE_REF_TYPE_STRONG
     */
    private int type;
    /**
     * The oid this mapping is done on.
     */
    public OID mappedOID;

    public PMCacheEntry() {
    }

    public PMCacheEntry(int type, PCStateMan sm, ReferenceQueue queue, int txnCnt) {
        LocalPMCache.checkRefType(type);
        this.txnCounter = txnCnt;
        this.type = type;
        if (sm.oid.getRealOID() == null) {
            this.hash = sm.oid.hashCode();
            this.mappedOID = sm.oid;
        } else {
            this.mappedOID = sm.oid.getRealOID();
            this.hash = sm.oid.getRealOID().hashCode();
        }

        switch (type) {
            case VersantPersistenceManager.PM_CACHE_REF_TYPE_STRONG:
                ref = sm;
                break;
            case VersantPersistenceManager.PM_CACHE_REF_TYPE_SOFT:
                ref = new SoftCacheEntryRef(sm, queue, this);
                break;
            case VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK:
                ref = new WeakCacheEntryRef(sm, queue, this);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("Unknown option: " + type);
        }
    }

    public PMCacheEntry(int type, OID oid, State state, ReferenceQueue queue, int txnCnt) {
        LocalPMCache.checkRefType(type);
        this.txnCounter = txnCnt;
        this.type = type;
        this.hash = oid.hashCode();
        this.mappedOID = oid;
        if (mappedOID == null) {
            throw BindingSupportImpl.getInstance().internal("");
        }
        switch (type) {
            case VersantPersistenceManager.PM_CACHE_REF_TYPE_STRONG:
                ref = state;
                break;
            case VersantPersistenceManager.PM_CACHE_REF_TYPE_SOFT:
                ref = new SoftCacheEntryRef(state, queue, this);
                break;
            case VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK:
                ref = new WeakCacheEntryRef(state, queue, this);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("Unknown option: " + type);
        }
    }

    public void unlinkProcessList() {
        if (processListNext != null) {
            processListNext.processListPrev = null;
        }
        if (processListPrev != null) {
            processListPrev.processListNext = null;
        }

        processListNext = null;
        processListPrev = null;
    }

    public void unlinkNextList() {
        if (prev != null) {
            prev.next = next;
        }
        if (next != null) {
            next.prev = prev;
        }
        next = null;
        prev = null;
    }

    /**
     * Upgrade the ref from a oid-state pair to a oid-pcstateman pair.
     */
    public PCStateMan upgradeToSm(PCStateMan sm, ReferenceQueue queue) {
        sm.cacheEntry = this;
        switch (type) {
            case VersantPersistenceManager.PM_CACHE_REF_TYPE_STRONG:
                ref = sm;
                break;
            case VersantPersistenceManager.PM_CACHE_REF_TYPE_SOFT:
                ((Reference)ref).clear();
                ref = new SoftCacheEntryRef(sm, queue, this);
                break;
            case VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK:
                ((Reference)ref).clear();
                ref = new WeakCacheEntryRef(sm, queue, this);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("Unknown option: " + type);
        }
        return sm;
    }

    public void setNext(PMCacheEntry nextEntry) {
        if (Debug.DEBUG) {
            if (nextEntry == this) {
                throw BindingSupportImpl.getInstance().internal("");
            }
        }
        next = nextEntry;
        if (next != null) {
            next.prev = this;
        }
    }

    public int hashCode() {
        return hash;
    }

    /**
     * Called when this is being remapped with a real oid.
     */
    public void reHash(OID newOID) {
        mappedOID = newOID;
        hash = newOID.hashCode();
    }

    /**
     * Return the referenced instance.
     * Either state of sm.
     */
    public Object get() {
        if (ref instanceof Reference) {
            return ((Reference)ref).get();
        }
        return ref;
    }

	public boolean hasReference(Object obj) {
		return (obj == ref);
	}

    public void clear() {
        if (ref instanceof Reference) {
            ((Reference)ref).clear();
        }
        ref = null;
    }

    public void reset() {
        clear();
        next = null;
        prev = null;
        processListNext = null;
        processListPrev = null;
    }

    public String toString() {
        return "CacheEntryBase@" + System.identityHashCode(this) + " oid  " + (mappedOID == null ? "null" : mappedOID.toStringImp());
    }

    public void changeToRefType(ReferenceQueue queue, int newType) {
        LocalPMCache.checkRefType(newType);

        if (this.type == newType) return;
        Object oldRef = get();
        clear();

        this.type = newType;
        switch (newType) {
            case VersantPersistenceManager.PM_CACHE_REF_TYPE_STRONG:
                ref = oldRef;
                break;
            case VersantPersistenceManager.PM_CACHE_REF_TYPE_SOFT:
                ref = new SoftCacheEntryRef(oldRef, queue, this);
                break;
            case VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK:
                ref = new WeakCacheEntryRef(oldRef, queue, this);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("Unknown option: " + type);
        }
    }
}
