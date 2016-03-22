
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

import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Debug;

import javax.jdo.PersistenceManagerFactory;
import javax.jdo.JDOUserException;
import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import javax.transaction.Transaction;
import java.io.PrintWriter;
import java.util.*;

/**
 * Implemention of {@link ManagedConnection}.
 */
public class ManagedPMConnection implements ManagedConnection {
    private final ManagedPMConnectionFactory mcf;
    private VersantPersistenceManager pm;
    private final Collection listeners = new ArrayList();
    private PrintWriter logWriter;
    private final ManagedConnectionMetaData metaData;
    private PMRequestInfo requestInfo;
    /**
     * Field to indicate if this is for a container managed instance.
     */
    private boolean containerManaged;
    private LocalTransactionImp localTransaction;
    private XAResourceImp xaResource;
    /**
     * The transaction that we are registered with.
     */
    private Transaction registeredTx;

    public ManagedPMConnection(final ManagedPMConnectionFactory mcf,
                               PMRequestInfo requestInfo) throws ResourceException {
        this.mcf = mcf;
        this.requestInfo = requestInfo;

        metaData = new ManagedConnectionMetaData() {
            public String getEISProductName() throws ResourceException {
                return "Versant Open Access";
            }

            public String getEISProductVersion() throws ResourceException {
                return mcf.getVersion();
            }

            public int getMaxConnections() throws ResourceException {
                return 0;
            }

            public String getUserName() throws ResourceException {
                return "versant";
            }
        };
    }

    public synchronized void addConnectionEventListener(ConnectionEventListener cel) {
        listeners.add(cel);
    }

    public synchronized void removeConnectionEventListener(ConnectionEventListener cel) {
        listeners.remove(cel);
    }

    void fireConnectionEvent(int eventType, Object connection) {
        if (containerManaged
                && eventType != ConnectionEvent.CONNECTION_CLOSED
                && eventType != ConnectionEvent.CONNECTION_ERROR_OCCURRED) {
            //the other three events must only be fired in BMT mode
            return;
        }

        //to make it reentrant safe
        ConnectionEventListener[] listeners =
                (ConnectionEventListener[]) this.listeners.toArray(new ConnectionEventListener[]{});
        ConnectionEvent ev = new ConnectionEvent(this, eventType);
        if (eventType == ConnectionEvent.CONNECTION_CLOSED) {
            ev.setConnectionHandle(connection);
        }
        for (int i = 0; i < listeners.length; i++) {
            switch (eventType) {
                case ConnectionEvent.CONNECTION_CLOSED:
                    listeners[i].connectionClosed(ev);
                    break;
                case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
                    listeners[i].connectionErrorOccurred(ev);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
                    listeners[i].localTransactionCommitted(ev);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
                    listeners[i].localTransactionRolledback(ev);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
                    listeners[i].localTransactionStarted(ev);
            }
        }
    }

    /**
     * Return a LocalTransaction to be used by the application server. The
     * PMF and the pm might be null at this stage. If the project file(*.jdogenie)
     * is not available at this stage then the pmf can not be created.
     */
    public LocalTransaction getLocalTransaction() throws ResourceException {
        try {
            if (localTransaction == null) {
                localTransaction = new LocalTransactionImp(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResourceException(e.getMessage());
        }
        return localTransaction;
    }

    public synchronized XAResource getXAResource() {
        if (xaResource == null) {
            xaResource = new XAResourceImp(this, mcf.getTxUtils());
        }
        return xaResource;
    }

    private PersistenceManagerFactory getPMF(boolean lazy) throws Exception {
        return mcf.getPMF(lazy);
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return this.logWriter;
    }

    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return this.metaData;
    }


    public void setLogWriter(PrintWriter printwriter) throws ResourceException {
        this.logWriter = printwriter;
    }

    public void cleanup() throws ResourceException {
        registeredTx = null;
        if (pm != null) {
            pm.close();
            pm = null;
        }

    }

    public Object getConnection(Subject subject,
                                ConnectionRequestInfo cri) throws ResourceException {
        try {
            if (registeredTx == null) {
                registeredTx = mcf.getTxUtils().currentTransaction();
            }
            registerForTxSync();
            getPmForPMConnection();
        } catch (Exception e) {
            throw new JDOUserException(e.getMessage(), e);
        }
        return new PMConnection(pm, this);
    }

    /**
     * Register with the currect transaction for synchronisation.
     * This is only needed if we are using jdbc connection from a pool that will
     * enlist the connections with the current transaction. We need the synchronization
     * to ensure that we flush to the connection before it is 'prepared'
     */
    private void registerForTxSync() throws Exception {
        if (xaResource == null) return;
        if (!mcf.isEnlistedDataSource()) return;
        
        Transaction tx = mcf.getTxUtils().currentTransaction();
        if (tx == null) return;
        if (registeredTx == null) {
            registeredTx = tx;
            tx.registerSynchronization(xaResource);
        } else if (!tx.equals(registeredTx)) {
            throw BindingSupportImpl.getInstance().internal(
                    "The 'registeredTx' is set to a 'non-current' transaction");
        }
    }

    /**
     * Used to obtain actual pm if pm was not available at time of creation. The PMF
     * must be creatable at this time.
     */
    synchronized VersantPersistenceManager getPmForPMConnection() throws Exception {
        //must check for re-deployment
        if (mcf.shouldRestart()) {
            pm = null;
        }
        if (pm == null) {
            pm = (VersantPersistenceManager)getPMF(false).getPersistenceManager();
        }
        return pm;
    }

    public void destroy() throws ResourceException {
        registeredTx = null;
        if (pm != null) pm.close();
        pm = null;
    }


    public void associateConnection(Object connection) {
        ((PMConnection) connection).associateMe(this);
    }

    public ManagedPMConnectionFactory getMCFactory() {
        return mcf;
    }

    public PMRequestInfo getRequestInfo() {
        return requestInfo;
    }
//  TODO Take this out for WebSphere
//    public void setRequestInfo(PMRequestInfo requestInfo) {
//        this.requestInfo = requestInfo;
//    }

    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }

    ManagedConnection getManagedConnection() {
        return this;
    }

    private void log(String msg) {
        System.out.println(Thread.currentThread().getName() + " " + msg);
    }

    /**
     * Callback from the localtransaction to indication if this is a connection
     * for a container managed instance.
     */
    public void setContainerManaged(boolean b) {
        containerManaged = b;
    }

    public boolean getContainerManaged() {
        return containerManaged;
    }

    public Transaction getCurrentTransaction() {
        return registeredTx;
    }

    public void setCurrentTransaction(Transaction tx) {
        registeredTx = tx;
    }
}
