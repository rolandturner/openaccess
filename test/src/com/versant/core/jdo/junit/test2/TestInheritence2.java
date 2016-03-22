
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
package com.versant.core.jdo.junit.test2;

import com.versant.core.jdo.junit.test2.model.hierarchy.Four;
import com.versant.core.jdo.junit.test2.model.hierarchy.RefHierarchy;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.logging.LogEvent;
import com.versant.core.jdbc.logging.JdbcLogEvent;
import com.versant.core.logging.LogEvent;
import com.versant.core.logging.LogEvent;

import javax.jdo.PersistenceManager;

import junit.framework.Assert;

/**
 * More inheritance tests.
 */
public class TestInheritence2 extends VersantTestCase {

    private int lastExecEventId;

    /**
     * Make sure the name of the column for a reference to a subclass can be
     * changed.
     */
    public void testMappingRefToSubclass() throws Exception {
    	if (!isSQLSupported()) // SQL
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        RefHierarchy rf = new RefHierarchy("rf", null);
        pm.makePersistent(rf);
        findExecSQL(null);  // clear events
        pm.currentTransaction().commit();

        // make sure correct column name was used for the insert
        Assert.assertTrue(findExecSQL("ref_twoxxx"));

        pm.currentTransaction().begin();
        Assert.assertTrue(rf.getRefTwo() == null);
        pm.deletePersistent(rf);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Find all exec events since the last call and check of any of them
     * contain sql.
     */
    private boolean findExecSQL(String sql) {
        LogEvent[] ea = pmf().getNewPerfEvents(lastExecEventId);
        if (ea == null) return false;
        lastExecEventId = ea[ea.length - 1].getId();
        for (int i = 0; i < ea.length; i++) {
            LogEvent pe = ea[i];
            if (pe instanceof JdbcLogEvent) {
                JdbcLogEvent e = (JdbcLogEvent)pe;
                if (e.getType() == JdbcLogEvent.STAT_EXEC || e.getType() == JdbcLogEvent.STAT_EXEC_UPDATE) {
                    if (sql != null && e.getDescr().indexOf(sql) >= 0) return true;
                }
            }
        }
        return false;
    }

    public void testFourLevelHierarchyLosingField() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // persist a Four instance
        pm.currentTransaction().begin();
        Four four = new Four(0);
        pm.makePersistent(four);
        pm.currentTransaction().commit();

        // check that it is ok
        pm.currentTransaction().begin();
        four.check(0);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(four);
        pm.currentTransaction().commit();

        pm.close();
    }


}


