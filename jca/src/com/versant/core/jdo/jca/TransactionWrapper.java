
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
package com.versant.core.jdo.jca;

import com.versant.core.common.BindingSupportImpl;

import javax.jdo.PersistenceManager;
import javax.jdo.JDOUserException;
import javax.resource.spi.ConnectionEvent;
import javax.transaction.Synchronization;

/**
 * Transaction wrapper for the pm.currentTransaction.
 */
public class TransactionWrapper implements javax.jdo.Transaction {
    private ManagedPMConnection mc;
    private PersistenceManager realPM;
    private PersistenceManager proxyPM;

    TransactionWrapper(ManagedPMConnection mc, PersistenceManager realPM, PersistenceManager proxyPM) {
        this.mc = mc;
        this.realPM = realPM;
        this.proxyPM = proxyPM;
    }

    private void checkMC() {
        if (mc == null) {
            throw BindingSupportImpl.getInstance().invalidOperation("PersistenceManager closed!");
        }
    }

    public void close() {
        mc = null;
        realPM = null;
        proxyPM = null;
    }

    //--- javax.jdo.Transaction ---
    public void begin() {
        checkMC();
        if (mc.getContainerManaged())
            throw new JDOUserException("Must not be called in CMT mode!");

        mc.fireConnectionEvent(ConnectionEvent.LOCAL_TRANSACTION_STARTED, null);
        realPM.currentTransaction().begin();
    }

    public void commit() {
        checkMC();
        if (mc.getContainerManaged())
            throw new JDOUserException("Must not be called in CMT mode!");

        mc.fireConnectionEvent(ConnectionEvent.LOCAL_TRANSACTION_COMMITTED, null);
        realPM.currentTransaction().commit();
    }

    public void rollback() {
        checkMC();
        if (mc.getContainerManaged())
            throw new JDOUserException("Must not be called in CMT mode!");

        mc.fireConnectionEvent(ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK, null);
        realPM.currentTransaction().rollback();
    }

    public boolean isActive() {
        checkMC();
        return realPM.currentTransaction().isActive();
    }

    public void setNontransactionalRead(boolean nontransactionalRead) {
        checkMC();
        realPM.currentTransaction().setNontransactionalRead(nontransactionalRead);
    }

    public boolean getNontransactionalRead() {
        checkMC();
        return realPM.currentTransaction().getNontransactionalRead();
    }

    public void setNontransactionalWrite(boolean nontransactionalWrite) {
        checkMC();
        realPM.currentTransaction().setNontransactionalWrite(nontransactionalWrite);
    }

    public boolean getNontransactionalWrite() {
        checkMC();
        return realPM.currentTransaction().getNontransactionalWrite();
    }

    public void setRetainValues(boolean retainValues) {
        checkMC();
        realPM.currentTransaction().setRetainValues(retainValues);
    }

    public boolean getRetainValues() {
        checkMC();
        return realPM.currentTransaction().getRetainValues();
    }

    public void setRestoreValues(boolean restoreValues) {
        checkMC();
        realPM.currentTransaction().setRestoreValues(restoreValues);
    }

    public boolean getRestoreValues() {
        checkMC();
        return realPM.currentTransaction().getRestoreValues();
    }

    public void setOptimistic(boolean optimistic) {
        checkMC();
        realPM.currentTransaction().setOptimistic(optimistic);
    }

    public boolean getOptimistic() {
        checkMC();
        return realPM.currentTransaction().getOptimistic();
    }

    public void setSynchronization(Synchronization sync) {
        checkMC();
        realPM.currentTransaction().setSynchronization(sync);
    }

    public Synchronization getSynchronization() {
        checkMC();
        return realPM.currentTransaction().getSynchronization();
    }

    public PersistenceManager getPersistenceManager() {
        checkMC();
        return proxyPM;
    }

    public boolean getRollbackOnly() {
        checkMC();
        return realPM.currentTransaction().getRollbackOnly();
    }

    public void setRollbackOnly() {
        checkMC();
        realPM.currentTransaction().setRollbackOnly();
    }
}
