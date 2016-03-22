
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

import javax.resource.ResourceException;
import javax.resource.spi.LocalTransactionException;
import javax.jdo.PersistenceManager;

/**
 * LocalTransaction as used by the container with container managed transaction.
 * Connection events is not fired from here as per spec. Section 6.7.2
 */
public class LocalTransactionImp implements javax.resource.spi.LocalTransaction {
    private ManagedPMConnection mc;

    public LocalTransactionImp(ManagedPMConnection mc) {
        this.mc = mc;
    }

    private PersistenceManager getPM() {
        try {
            return mc.getPmForPMConnection();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void begin() throws ResourceException {
        if (getPM().currentTransaction().isActive())
            throw new LocalTransactionException("Transaction already running.");
        try {
            mc.setContainerManaged(true);
            getPM().currentTransaction().begin();
        } catch (Throwable t) {
            mc.setContainerManaged(false);
            throw new LocalTransactionException("Error while during begin()", t.getMessage());
        }
    }

    public void commit() throws ResourceException {
        if (!getPM().currentTransaction().isActive()) {
            throw new LocalTransactionException("Transaction not running.");
        }
        try {
            getPM().currentTransaction().commit();
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            throw new LocalTransactionException("Error while during commit()", t.getMessage());
        } finally {
            mc.destroy();
            mc.setContainerManaged(false);
        }
    }

    public void rollback() throws ResourceException {
        if (!getPM().currentTransaction().isActive())
            throw new LocalTransactionException("Transaction not running.");
        try {
            getPM().currentTransaction().rollback();
        } catch (Throwable t) {
            throw new LocalTransactionException("Error while during rollback()", t.getMessage());
        } finally {
            mc.destroy();
            mc.setContainerManaged(false);
        }
    }
}
