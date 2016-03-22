
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

import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.Status;
import javax.naming.InitialContext;
import javax.jdo.JDOFatalException;
import java.lang.reflect.Method;

/**
 * This is a util class for tx stuff.
 */
public class TxUtils {

    private TransactionManager txManager;

    /**
     * The one that works
     */
    private String txManagerName;

    /**
     * A list of possible jndiNames for txManager lookup names.
     */
    private String[] jndiNames = null;

    private String defaultTxManagerName;

    /**
     * The the default tx name.
     *
     * @param name
     */
    public TxUtils(String name) {
        this.defaultTxManagerName = name;
        jndiNames = new String[]{
            txManagerName /* The name that works, to minimize trials*/,
            defaultTxManagerName/* The supplied name */,
            "java:/TransactionManager" /* From the spec */,
            "javax.transaction.TransactionManager" /* Also from the spec */,
            "TransactionManager" /* Why do we have so may of them ? */,
            "javax.jts.TransactionManager" /* For some servers */,
            "java:/DefaultDomain/TransactionManager",
            "java:comp/pm/TransactionManager",
            "java:comp/TransactionManager",
            "java:pm/TransactionManager",
            System.getProperty("jta.TransactionManager")};
        findTransactionManager();
        log("TransactionManager: " + txManager);
    }

    public TxUtils(TransactionManager txManager) {
        this.txManager = txManager;
    }

    private void log(String msg) {
        System.out.println("Versant Open Access: " + msg);
    }

    private void findTransactionManager() {
        if (txManager != null) return;

        for (int i = 0; i < jndiNames.length && txManager == null; i++) {
            try {
                txManager = (TransactionManager) new InitialContext().lookup(jndiNames[i]);
                txManagerName = jndiNames[i];

            } catch (Throwable e) {
            }
        }
        if (txManager != null) {
            log("Found TransactionManager with JNDI name '" + txManagerName + "'");
        }

        //for ws 5
        if (txManager == null) {
            try {
                Class cls = Class.forName("com.ibm.ejs.jts.jta.TransactionManagerFactory");
                Method m = cls.getMethod("getTransactionManager", null);
                m.setAccessible(true);
                txManager = (TransactionManager) m.invoke(null, null);
            } catch (Exception e) {
                //ignore
            }
            if (txManager != null) {
                log("Found TransactionManager for WebShere 5");
            }
        }
        //for ws 5
        if (txManager == null) {
            try {
                Class cls = Class.forName("com.ibm.ws.Transaction.TransactionManagerFactory");
                Method m = cls.getMethod("getTransactionManager", null);
                m.setAccessible(true);
                txManager = (TransactionManager) m.invoke(null, null);
            } catch (Exception e) {
                //ignore
            }
            if (txManager != null) {
                log("Found TransactionManager for WebShere 5");
            }
        }

        //for ws 4
        if (txManager == null) {
            try {
                Class cls = Class.forName("com.ibm.ejs.jts.jta.JTSXA");
                Method m = cls.getMethod("getTransactionManager", null);
                m.setAccessible(true);
                txManager = (TransactionManager) m.invoke(null, null);
            } catch (Exception e) {
                //ignore
            }
            if (txManager != null) {
                log("Found TransactionManager for WebShere 4");
            }
        }
        // for borland
        if (txManager == null) {
            try {
                Class cls = Class.forName("com.inprise.visitransact.jta.TransactionManagerImpl");
                Method m = cls.getMethod("getTransactionManagerImpl", null);
                m.setAccessible(true);
                txManager = (TransactionManager) m.invoke(null, null);
            } catch (Exception e) {
                //ignore
            }
            if (txManager != null) {
                log("Found TransactionManager For Borland Enterprise Server");
            }
        }
        // for Arjuna
        if (txManager == null) {
            try {
                Class cls = Class.forName("com.arjuna.jta.JTA_TransactionManager");
                Method m = cls.getMethod("transactionManager", null);
                m.setAccessible(true);
                txManager = (TransactionManager) m.invoke(null, null);
            } catch (Exception e) {
                //ignore
            }
            if (txManager != null) {
                log("Found TransactionManager for Arjuna");
            }
        }
        // for sun one
        if (txManager == null) {
            try {
                Class cls = Class.forName("com.sun.enterprise.Switch");
                Method m = cls.getMethod("getSwitch", null);
                Object sw = m.invoke(null, null);
                m = sw.getClass().getMethod("getTransactionManager", null);
                txManager = (TransactionManager) m.invoke(sw, null);
            } catch (Exception e) {
                //ignore
            }
            if (txManager != null) {
                log("Found TransactionManager for Sun One");
            }
        }
        // for J2EE ref impl
        if (txManager == null) {
            try {
                Class cls = Class.forName("com.sun.jts.jta.TransactionManagerImpl");
                Method m = cls.getMethod("getTransactionManagerImpl", null);
                m.setAccessible(true);
                txManager = (TransactionManager) m.invoke(null, null);
            } catch (Exception e) {
                //ignore
            }
            if (txManager != null) {
                log("Found TransactionManager for J2EE SDK");
            }
        }
        // for HP
        if (txManager == null) {
            try {
                Class cls = Class.forName("com.bluestone.jta.SaTransactionManagerFactory");
                Method m = cls.getMethod("SaGetTransactionManager", null);
                m.setAccessible(true);
                txManager = (TransactionManager) m.invoke(null, null);
            } catch (Exception e) {
                //ignore
            }
            if (txManager != null) {
                log("Found TransactionManager for HP Bluestone Total-e-Server");
            }
        }
        // for open ejb
        if (txManager == null) {
            try {
                Class cls = Class.forName("org.openejb.OpenEJB");
                Method m = cls.getMethod("getTransactionManager", null);
                m.setAccessible(true);
                txManager = (TransactionManager) m.invoke(null, null);
            } catch (Exception e) {
                //ignore
            }
            if (txManager != null) {
                log("Found TransactionManager for OpenEJB");
            }
        }

        if (txManager == null) {
            throw new JDOFatalException("Unable to find TransactionManager: " +
                    "set TxManagerName (in JCA configuration files) " +
                    "to the jndi name of your TransactionManager");
        }
    }

    /**
     * Return the current tx for the calling thread.
     */
    public Transaction currentTransaction() {
        Transaction tx = null;
        try {
            tx = txManager.getTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            throw new JDOFatalException(e.getMessage());
        }
        return tx;
    }

    public TransactionManager getTransactionManager() {
        return txManager;
    }

    public static String getTxStatus(int status) {
        switch(status) {
            case Status.STATUS_ACTIVE:
                return "STATUS_ACTIVE";
            case Status.STATUS_COMMITTED:
                return "STATUS_COMMITTED";
            case Status.STATUS_COMMITTING:
                return "STATUS_COMMITTING";
            case Status.STATUS_MARKED_ROLLBACK:
                return "STATUS_MARKED_ROLLBACK";
            case Status.STATUS_NO_TRANSACTION:
                return "STATUS_NO_TRANSACTION";
            case Status.STATUS_PREPARED:
                return "STATUS_NO_TRANSACTION";
            case Status.STATUS_PREPARING:
                return "STATUS_PREPARING";
            case Status.STATUS_ROLLEDBACK:
                return "STATUS_ROLLEDBACK";
            case Status.STATUS_ROLLING_BACK:
                return "STATUS_ROLLING_BACK";
            case Status.STATUS_UNKNOWN:
                return "STATUS_UNKNOWN";
            default:
                return "UNKOWN";
        }
    }
}

