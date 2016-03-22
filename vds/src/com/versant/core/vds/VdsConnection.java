
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
package com.versant.core.vds;

import com.versant.core.common.Debug;

import javax.jdo.JDOFatalInternalException;
import com.versant.odbms.DatastoreManager;

/**
 * A connection to VDS server wrapped for event logging and pooling.
 * Physical connection to VDS is called {@link com.versant.odbms.DatastoreManager}.
 * These physical connections used to pool underlying sockets
 * for sending RPC calls to VDS server. Genie connection management, on the other hand,
 * views connection at a semantically higher level and requires to pool DatastoreInterface
 * itself. So we need a concept of DatastoreInterface that can be pooled. 
 * However, COBRA the native Versant/Java interface would not like to depend on Genie
 * and hence the genesis of this class that wraps a DatastoreInterface and makes it
 * <em>poolable</em>(?) by {@link VdsConnectionPool VdsConnectionPool}.
 *
 * @see VdsConnectionPool
 */
public final class VdsConnection   {

    private final VdsConnectionPool pool;
    private final DatastoreManager con;
    private boolean destroyed; 	// connection has been timedout
    VdsConnection prev, next;

    public boolean idle;   		// is connection idle (i.e. in pool)?
    public int age; 			// number of times con has been returned to the pool
    private long lastActivityTime;
    // Time when something last happened on this connection. This is used
    // to cleanup active connections that are stuck.

    public VdsConnection(VdsConnectionPool pool, DatastoreManager con){
        this.pool = pool;
        this.con  = con;
    }

    public void fatalReset(){
        close();
    }
     
    public void testCloseNotRelease() {
        
    }

    public VdsConnectionPool getPool() {
        return pool;
    }

    /**
     * Get the real connection. A transaction is started if none is active.
     * If a tx is started and server is not null then a new TxId is
     * assigned.
     */
    public DatastoreManager getCon() {
        if (!con.isTransactionActive()) {
            con.beginTransaction();
        }
        return con;
    }

    public synchronized long getLastActivityTime() {
        return lastActivityTime;
    }

    public synchronized void updateLastActivityTime() {
        lastActivityTime = System.currentTimeMillis();
    }


    /**
     * This is just going to return the connection to the pool.
     */
    public void close()  {
        pool.returnConnection(this);
    }

    public boolean isClosed()  {
        checkIdle();
        return con.isClosed();
    }

    void checkIdle() {
	    if (Debug.DEBUG) {
	        if (idle) throw new JDOFatalInternalException("con in pool");
	    }
    }

    public void commit()  {
        if (Debug.DEBUG) {
            System.out.println("%%% VdsConnection.commit");
        }
        checkIdle();
        con.commitTransaction();
        con.beginTransaction();
   }

    public void rollback()  {
        if (Debug.DEBUG) {
            System.out.println("%%% VdsConnection.rollback");
        }
        con.rollbackTransaction();
        con.beginTransaction();
    }

    /**
     * Realy close this connection i.e. do not return it to the pool.
     */
    public void closeRealConnection()  {
        con.close();
    }

    /**
     * Has this connection been destroyed?
     * @see #destroy()
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Forceably close the real connection and set a flag to make sure this
     * connection will not go back in the pool. Any exceptions on close
     * are silently discarded. This is a NOP if the connection has already
     * been destroyed.
     * @see #isDestroyed()
     */
    public void destroy() {
        if (destroyed) return;
        this.destroyed = true;
        closeRealConnection();
    }

}

