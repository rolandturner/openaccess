
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
/*
 * Created on Aug 23, 2004
 *
 * Copyright Versant Corportaion.
 * All rights reserved 2004-05
 */
package com.versant.core.jdo.junit.test1;

import java.util.Collection;
import java.util.Random;
import com.versant.core.jdo.junit.VersantTestCase;
import javax.jdo.*;
import junit.framework.Assert;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.junit.test1.model.*;

/** Tests basic datastore connection pooling. This test opens <code>N</code>
 * PersistenceManagers each reading a single object. The datastore is configured
 * with a pool of <code>M</code> physical connections where <code>N>>M</code>.
 * @author ppoddar
 *
 */
public class TestQueryConnection extends VersantTestCase  {
    Object _oid;
    int    N = 100; // No of independent threads;
    public void setUp() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(false);
        pm.currentTransaction().begin();
        Simple simple = new Simple();
        simple.setAge(20);
        pm.makePersistent(simple);
        pm.currentTransaction().commit();
        
        _oid = JDOHelper.getObjectId(simple);
    }
    public void testSerialAccess() {
        for (int i=0; i<1000; i++){
            PersistenceManager pm = pmf().getPersistenceManager();
//            pm.currentTransaction().begin();
            Query query = pm.newQuery(Simple.class);
            Collection result = (Collection)query.execute();
//            pm.currentTransaction().rollback();
            pm.close();
            
        }
        
    }
}

