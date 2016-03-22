
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
package com.versant.core.jdo.junit.test0;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test0.model.Address;
import com.versant.core.jdo.junit.test0.model.testl2cache.*;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.VersantPersistenceManagerFactory;
import com.versant.core.common.config.ConfigParser;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.JDOHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

/**
 * Some tests for the L2 cache.
 */
public class TestL2cache extends VersantTestCase {

    public TestL2cache(String name) {
        super(name);
    }

    /**
     * Make sure that the results of an automatically closed query go into the
     * level 2 cache correctly (OA-164).
     */
    public void testIteratedQueryResultsCached() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(true);
        pm.currentTransaction().setOptimistic(true);

        pm.currentTransaction().begin();
        pm.makePersistent(new Address("medway"));
        pm.currentTransaction().commit();
        
        pm.currentTransaction().begin();
        
        Query q = pm.newQuery(Address.class, "street == p");
        q.declareParameters("String p");
        Collection ans = (Collection)q.execute("medway");
        for (Iterator i = ans.iterator(); i.hasNext(); ) {
            System.out.println("got = " + i.next());
        }

        System.out.println("$$$ Executing query again to check level 2 cache");
        
        findExecQuerySQL();
        ans = (Collection)q.execute("medway");
        for (Iterator i = ans.iterator(); i.hasNext(); ) {
            System.out.println("got = " + i.next());
        }
        String sql = findExecQuerySQL();
        System.out.println("$$$ sql = " + sql);
        Assert.assertNull(sql);
        
        pm.close();
   }    

    public void testL2cache() throws Exception {
        VersantPersistenceManagerFactory pmf = (VersantPersistenceManagerFactory)pmf();

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf.getPersistenceManager();
        pm.currentTransaction().begin();

        Village village = new Village();
        village.setCode("EH");

        Country country = new Country();
        country.setCode("NL");

        CountryRegion countryRegion = new CountryRegion();
        countryRegion.setCode("NL_N");

        country.addRegion(countryRegion);

        village.setRegion(countryRegion);

        ArrangementType arrangementType = new ArrangementType();
        arrangementType.setCode("CHR");

        WcccArrangement wcccArrangement = new WcccArrangement();
        wcccArrangement.setArrangementType(arrangementType);
        wcccArrangement.setIsdeliveryservice('Y');

        arrangementType.setWcccArrangement(wcccArrangement);

        village.getArrangementTypes().add(arrangementType);

        pm.makePersistent(village);

        pm.currentTransaction().commit();
        pm.close();
        pm = (VersantPersistenceManager)pmf.getPersistenceManager();
//        pm.currentTransaction().begin();
        pm.currentTransaction().setNontransactionalRead(true);
        Query query = pm.newQuery(Village.class);
        Collection villages = (Collection)query.execute();
        for (Iterator iter = villages.iterator(); iter.hasNext();) {
            village = (Village)iter.next();
            Assert.assertNotNull(village.getArrangementTypes());
        }
//        pm.currentTransaction().rollback();
        pm.close();
        pm = (VersantPersistenceManager)pmf.getPersistenceManager();
//        pm.currentTransaction().begin();
        pm.currentTransaction().setNontransactionalRead(true);
        query = pm.newQuery(Village.class);
        villages = (Collection)query.execute();
        for (Iterator iter = villages.iterator(); iter.hasNext();) {
            village = (Village)iter.next();
            Assert.assertNotNull(village.getRegion());
            Assert.assertNotNull(village.getRegion().getCountry());
            Assert.assertNotNull(village.getArrangementTypes());
        }
//        pm.currentTransaction().rollback();
        pm.close();
    }
}
