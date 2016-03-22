
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

import com.versant.core.common.Debug;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import javax.transaction.*;
import javax.jdo.PersistenceManager;

/**
 * XAResource implementation.
 */
public class XAResourceImp implements XAResource, Synchronization {
    private ManagedPMConnection mc;
    private Xid currentXid;
    private TxUtils txUtils;

    private int txState;
    public static final int TX_INACTIVE = 0;
    public static final int TX_STARTED = 1;
    public static final int TX_FAIL = 2;
    public static final int TX_PREPARED = 4;
    public static final int TX_SUSPENDED = 8;
    private int txTimeout;

    public XAResourceImp(ManagedPMConnection mc, TxUtils txUtils) {
        this.mc = mc;
        this.txUtils = txUtils;
    }

    public void afterCompletion(int i) {
    }

    public void beforeCompletion() {
        flushOnSync();
    }

    /**
     * Flush to the underlying connection on a tx synch call. This will change the
     * status to prepared.
     */
    private void flushOnSync() {
        if (Debug.DEBUG) {
            System.out.println("\n\n\nXAResourceImp.flushOnSync");
            try {
                System.out.println("txUtils.getTransactionManager().getStatus() = "
                        + TxUtils.getTxStatus(txUtils.getTransactionManager().getStatus()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        final PersistenceManager pm = getPM();
        if (pm.currentTransaction().isActive()) pm.flush();
        txState = TX_PREPARED;
    }

    private PersistenceManager getPM() {
        try {
            return mc.getPmForPMConnection();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private boolean checkId(Xid xid) {
        if (!this.currentXid.equals(xid)) {
            return false;
        }
        return true;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
        if (Debug.DEBUG) {
            System.out.println("\n\nXAResourceImp.commit: " + xid + " onePhase " + onePhase);
        }
        if (checkId(xid)) {
            try {
                if (onePhase && txState == TX_STARTED) {
                    getPM().currentTransaction().commit();
                } else if (this.txState == TX_PREPARED) {
                    getPM().currentTransaction().commit();
                } else if (this.txState == TX_INACTIVE) {
                    return;
                } else {
                    throw new XAException("Unable to commit unexpected state: state = " +
                            txState + " for xid = " + xid);
                }

                this.txState = TX_INACTIVE;
                this.currentXid = null;
            } catch (XAException ex) {
                ex.printStackTrace();
                throw ex;
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new XAException("Could not commit : " + ex.getMessage());
            }
        }
    }

    public void end(Xid xid, int flags) throws XAException {
        if (Debug.DEBUG) {
            System.out.println("\n\nXAResourceImp.end");
        }
        if (checkId(xid)) {
            switch (flags) {
                case TMSUCCESS:
                    this.txState = TX_STARTED;
                    break;
                case TMFAIL:
                    this.txState = TX_FAIL;
                    break;
                case TMSUSPEND:
                    this.txState = TX_SUSPENDED;
                    break;
                default:
                    throw new XAException(
                            "Unable to end transaction = " + xid + " unhandled flag = " + flags);
            }
        }
    }

    public void forget(Xid xid) throws XAException {
        if (Debug.DEBUG) {
            System.out.println("\n\nXAResourceImp.forget");
        }
        if (this.currentXid.equals(xid)) {
            this.txState = TX_STARTED;
        }
    }

    public int getTransactionTimeout() throws XAException {
        if (Debug.DEBUG) {
            System.out.println("\n\nXAResourceImp.getTransactionTimeout");
        }
        return txTimeout;
    }

    public boolean isSameRM(XAResource xaResource) throws XAException {
        if (Debug.DEBUG) {
            System.out.println("\n\nXAResourceImp.isSameRM: " + (this == xaResource));
            System.out.println("xaResource instanceOf " + (xaResource instanceof XAResourceImp));
            System.out.println("xaResource = " + xaResource);
        }
        return xaResource == this;
    }

    public int prepare(Xid xid) throws XAException {
        if (Debug.DEBUG) {
            System.out.println("\n\nXAResourceImp.prepare");
        }
        if (checkId(xid)) {
            if (txState == TX_STARTED) {
                try {
                    getPM().flush();
                    this.txState = TX_PREPARED;
                    return XA_OK;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new XAException(
                            "Could not prepare commit : " + ex.getMessage());
                }
            } else if (this.txState == TX_PREPARED || this.txState == TX_SUSPENDED) {
                return XA_OK;
            } else {
                throw new XAException(
                        "Wrong state to commit phase one on : state = " + this.txState);
            }
        }
        return XA_OK;
    }

    public Xid[] recover(int i) throws XAException {
        if (Debug.DEBUG) {
            System.out.println("\n\nXAResourceImp.recover");
        }
//        Xid[] xids = txState == TX_PREPARED ? new Xid[]{currentXid} : null;
//        return xids;
        return null;
    }

    public void rollback(Xid xid) throws XAException {
        if (Debug.DEBUG) {
            System.out.println("\n\nXAResourceImp.rollback");
        }
        if (checkId(xid)) {
            try {
                if (this.txState != TX_INACTIVE) {
                    getPM().currentTransaction().rollback();
                    this.txState = TX_INACTIVE;
                }
                this.currentXid = null;
            } catch (Exception e) {
                throw new XAException("Could not rollback: " + e.getMessage());
            }
        }
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        if (Debug.DEBUG) {
            System.out.println("\n\nXAResourceImp.setTransactionTimeout");
        }
        if (seconds < -1) {
            return false;
        } else {
            this.txTimeout = seconds;
        }
        return true;
    }

    public void start(Xid xid, int flags) throws XAException {
        if (Debug.DEBUG) {
            System.out.println("\n\nXAResourceImp.start: " + getFlagString(flags));
        }
        switch (flags) {
            case TMNOFLAGS:
            case TMJOIN:
                begin(xid);
                break;
            case TMRESUME:
                if (checkId(xid)) {
                    resume(xid);
                }
                break;
            default:
                throw new XAException(
                        "Unsupported state for method start state = " + flags);
        }
    }

    private void resume(Xid xid) throws XAException {
        if (this.txState != TX_SUSPENDED) {
            throw new XAException(
                    "May not resume a transaction that was not suspended");
        }
        this.txState = TX_STARTED;
    }

    private void begin(Xid xid) throws XAException {
        if (this.txState == TX_INACTIVE) {
            this.currentXid = xid;
            try {
                getPM().currentTransaction().begin();
                this.txState = TX_STARTED;
            } catch (Exception e) {
                throw new XAException(
                        "Could not begin a transaction : " + e.getMessage());
            }
        } else if (this.txState == TX_STARTED
                || this.txState == TX_PREPARED
                || this.txState == TX_SUSPENDED) {
            // Transaction on this pm has started already. Since beans will
            // share this pm, there will be multple calls to begin.
            return;
        } else {
            throw new XAException(
                    "Could not begin a transaction in state = " + txState);
        }
    }

    private String getFlagString(int flag) {
        switch (flag) {
            case 8388608:
                return "TMENDRSCAN";
            case 536870912:
                return "TMFAIL";
            case 2097152:
                return "TMJOIN";
            case 0:
                return "TMNOFLAGS";
            case 1073741824:
                return "TMONEPHASE";
            case 134217728:
                return "TMRESUME";
            case 16777216:
                return "TMSTARTRSCAN";
            case 67108864:
                return "TMSUCCESS";
            case 33554432:
                return "TMSUSPEND";
            default:
                return "UNKNOWN";
        }
    }
}
