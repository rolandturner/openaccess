
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

import com.versant.core.jdo.PCStateMan;
import com.versant.core.jdo.VersantStateManager;
import com.versant.core.server.QueryResultWrapper;
import com.versant.core.server.CompiledQuery;
import com.versant.core.common.*;
import com.versant.core.storagemanager.ExecuteQueryReturn;

import javax.jdo.*;
import javax.jdo.spi.PersistenceCapable;

/**
 * Proxy instance to the real EntityManager.
 */
public class EMProxy implements VersantPMProxy {
    private EntityManagerImp em;

    public EMProxy(EntityManagerImp em) {
        this.em = em;
    }

    public EntityManagerImp getEm() {
        checkClosed();
        return em;
    }

    public void detach() {
        em = null;
    }

    private void checkClosed() {
        if (em == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The EntityManager is closed");
        }
    }

    public int getObjectsById(Object[] a, int count, Object[] data,
                              int stateFieldNo, int index) {
        return getEm().getObjectsById(a, count, data, stateFieldNo, index);
    }

    public OID getInternalOID(PersistenceCapable pc) {
        return getEm().getInternalOID(pc);
    }

    public PCStateMan getInternalSM(PersistenceCapable pc) {
        return getEm().getInternalSM(pc);
    }

    public VersantStateManager getVersantStateManager(PersistenceCapable pc) {
        return getEm().getVersantStateManager(pc);
    }

    public PersistenceManager getPersistenceManager() {
        return getEm().getPersistenceManager();
    }

    public Object getObjectByIdForState(OID oid, int stateFieldNo,
                                        int navClassIndex, OID fromOID) {
        return getEm().getObjectByIdForState(oid, stateFieldNo, navClassIndex,
                fromOID);
    }

    public Object getObjectById(Object oid, boolean validate) {
        return em.getObjectById(oid, validate);
    }

    public QueryResultWrapper executeQuery(CompiledQuery cq, Object[] params) {
        return em.getStorageManager().executeQuery(em, null, cq, params);
    }

    public QueryResultContainer getNextQueryResult(QueryResultWrapper aQrs, int skipAmount) {
        checkClosed();
        return em.getStorageManager().fetchNextQueryResult(em,
                ((ExecuteQueryReturn)aQrs).getRunningQuery(), skipAmount);
    }

    public void closeQuery(QueryResultWrapper qrw) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void flushIfDepOn(int[] bits) {
        em.flushIfDepOn(bits);
    }

    public void processLocalCacheReferenceQueue() {
    }

    public void addToCache(StatesReturned container) {
        em.addToCache(container);
    }

    public QueryResultContainer getAbsolute(QueryResultWrapper qrsIF, int index, int fetchAmount) {
        return null;
    }

    public int getResultCount(QueryResultWrapper qrsIF) {
        return 0;
    }

    public int getQueryRowCount(CompiledQuery cq, Object[] params) {
        return 0;
    }

    public QueryResultContainer getAllQueryResults(CompiledQuery cq, Object[] params) {
        return em.getStorageManager().executeQueryAll(em, null, cq, params);
    }

    public void setMasterOnDetail(PersistenceCapable detail, int managedFieldNo,
                                  PersistenceCapable master, boolean removeFromCurrentMaster) {
    }

    public Object getObjectField(PersistenceCapable pc, int fieldNo) {
        return null;
    }

    public boolean isRetainValues() {
        return getEm().isRetainValues();
    }
}
