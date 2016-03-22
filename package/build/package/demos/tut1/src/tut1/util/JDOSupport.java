
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
package tut1.util;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import java.util.Properties;

/**
 * Methods to get and close PMs as well as start the JDO Genie server. This
 * maintains a PM per Thread. However since this is a GUI app all work is
 * done on the event dispatch thread so there is really only one pm. Doing
 * it this way makes it possible to use JDO aware business logic in different
 * environments (gui, web or ejb) without having to change it.<p>
 * <p/>
 * This class includes a main method to start a standalong JDO Genie server
 * for running the demo in 3 tier mode.<p>
 *
 */
public class JDOSupport {

    private static PersistenceManagerFactory pmf;
    private static boolean remote;
    private static ThreadLocal pmHolder = new ThreadLocal();

    private JDOSupport() {
    }

    /**
     * Init the JDO Genie server. If host is not null then this is the
     * name of the remote JDO Genie server to connect to.
     */
    public static void init(String host) throws Exception {
        if (pmf != null) throw new IllegalStateException("Already initialized");
        ClassLoader loader = JDOSupport.class.getClassLoader();
        Properties props = new Properties();
        props.load(loader.getResourceAsStream("versant.properties"));
        remote = host != null;
        if (remote) props.setProperty("host", host);
        pmf = JDOHelper.getPersistenceManagerFactory(props);
    }

    public static void shutdown() {
        System.out.println("JDOSupport.shutdown");
        if (pmf != null) { 
            System.out.println("  Shutting down PMF");
	    pmf.close();
            pmf = null;
        }
    }

    public static PersistenceManagerFactory getPMF() {
        return pmf;
    }

    /**
     * Get the PM for the current thread. This will create one if the thread
     * does not have one and start a new tx.
     */
    public static PersistenceManager getPM() {
        System.out.println("JDOSupport.getPM");
        PersistenceManager pm = (PersistenceManager)pmHolder.get();
        if (pm == null) {
            System.out.println("  getPersistenceManager()");
            pmHolder.set(pm = getPMF().getPersistenceManager());
            pm.currentTransaction().begin();
        }
        return pm;
    }

    /**
     * Close the PM for the current thread. This is a NOP if there is no
     * PM associated with the thread.
     */
    public static void closePM() {
        System.out.println("JDOSupport.closePM");
        PersistenceManager pm = (PersistenceManager)pmHolder.get();
        if (pm != null) {
            System.out.println("  close()");
            pmHolder.set(null);
            pm.close();
        }
    }

    /**
     * If the current thread does not have a PM then this is a NOP. If there
     * is a active tx then do a commit and start a new tx. If there is no
     * active tx then start one.
     */
    public static void commit() {
        System.out.println("JDOSupport.commit");
        PersistenceManager pm = (PersistenceManager)pmHolder.get();
        if (pm != null) {
            if (pm.currentTransaction().isActive()) {
                System.out.println("  commit()");
                pm.currentTransaction().commit();
            }
            pm.currentTransaction().begin();
            System.out.println("  begin()");
        }
    }

    /**
     * If the current thread does not have a PM then this is a NOP. If there
     * is a active tx then do a rollback and start a new tx. If there is no
     * active tx then start one.
     */
    public static void rollback() {
        System.out.println("JDOSupport.rollback");
        PersistenceManager pm = (PersistenceManager)pmHolder.get();
        if (pm != null) {
            if (pm.currentTransaction().isActive()) {
                System.out.println("  rollback()");
                pm.currentTransaction().rollback();
            }
            System.out.println("  begin()");
            pm.currentTransaction().begin();
        }
    }

    /**
     * If the current thread does not have a PM then this is a NOP. If there
     * is a active tx then refresh all transactional instances (i.e. make
     * changes made by other PMs visible to us). If there is no active tx
     * then start one.
     */
    public static void refresh() {
        System.out.println("JDOSupport.refresh");
        PersistenceManager pm = (PersistenceManager)pmHolder.get();
        if (pm != null) {
            if (pm.currentTransaction().isActive()) {
                System.out.println("  refreshAll()");
                pm.refreshAll();
            } else {
                System.out.println("  begin()");
                pm.currentTransaction().begin();
            }
        }
    }
}

