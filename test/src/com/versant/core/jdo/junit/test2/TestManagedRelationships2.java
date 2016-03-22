
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

import junit.framework.Assert;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test2.model.Detail;
import com.versant.core.jdo.junit.test2.model.Master;
import com.versant.core.jdo.junit.test2.model.Material;
import com.versant.core.jdo.junit.test2.model.alex.MenuItem;
import com.versant.core.jdo.junit.TestFailedException;

import javax.jdo.PersistenceManager;
import javax.jdo.JDOObjectNotFoundException;

/**
 * Tests for one-to-many and many-to-many managed relationships.
 */
public class TestManagedRelationships2 extends VersantTestCase {

    /**
     * Test Alex's tree persistence problem with the AUTOINC key generator.
     */
    public void testTreePersistenceOneToManyAutoInc() throws Exception {
        if (!isJdbc() || !getSubStoreInfo().isAutoIncSupported()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        MenuItem root = new MenuItem("root");
        MenuItem subA = new MenuItem("subA");
        subA.addSubMenu(new MenuItem("subA1"));
        subA.addSubMenu(new MenuItem("subA2"));
        subA.addSubMenu(new MenuItem("subA3"));
        root.addSubMenu(subA);
        MenuItem subB = new MenuItem("subB");
        subB.addSubMenu(new MenuItem("subB1"));
        subB.addSubMenu(new MenuItem("subB2"));
        root.addSubMenu(subB);
        pm.makePersistent(root);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("[subA, subB]", root.getSubMenuListStr());
        Assert.assertEquals("[subA1, subA2, subA3]", subA.getSubMenuListStr());
        Assert.assertEquals("[subB1, subB2]", subB.getSubMenuListStr());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test a recursive one-to-many relationship. Randy Watsons first case.
     */
    public void testOneToManyTreeRandyWatson1() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);

        Material m1, m2;

        System.out.println("\n*** Create a[b] in same tx");
        pm.currentTransaction().begin();
        pm.makePersistent(m1 = new Material("a"));
        pm.makePersistent(m2 = new Material("b"));
        m1.addAlias(m2);
        Assert.assertEquals("a[b]", m1.toString());
        Assert.assertTrue(m1 == m2.getAliasedTo());
        pm.currentTransaction().commit();

        System.out.println("\n*** Check still ok in next tx, same PM");
        pm.currentTransaction().begin();
        Assert.assertEquals("a[b]", m1.toString());
        Assert.assertTrue(m1 == m2.getAliasedTo());
        Object oidM1 = pm.getObjectId(m1);
        Object oidM2 = pm.getObjectId(m2);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);

        System.out.println("\n*** Check still ok new PM and nuke em");
        pm.currentTransaction().begin();
        m1 = (Material)pm.getObjectById(oidM1, true);
        m2 = (Material)pm.getObjectById(oidM2, true);
        Assert.assertEquals("a[b]", m1.toString());
        Assert.assertTrue(m1 == m2.getAliasedTo());
        m1.nuke();
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test a recursive one-to-many relationship. Randy Watsons 2nd case.
     */
    public void testOneToManyTreeRandyWatson2() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);

        Material m1, m2, m3;

        System.out.println("\n*** Create a[b] c in same tx");
        pm.currentTransaction().begin();
        pm.makePersistent(m1 = new Material("a"));
        pm.makePersistent(m2 = new Material("b"));
        pm.makePersistent(m3 = new Material("c"));
        m1.addAlias(m2);
        Assert.assertEquals("a[b]", m1.toString());
        Assert.assertTrue(m1 == m2.getAliasedTo());
        pm.currentTransaction().commit();

        System.out.println("\n*** Change to a[b, c]");
        pm.currentTransaction().begin();
        m1.addAlias(m3);
        Assert.assertEquals("a[b, c]", m1.toString());
        Assert.assertTrue(m1 == m2.getAliasedTo());
        Assert.assertTrue(m1 == m3.getAliasedTo());
        pm.currentTransaction().commit();

        System.out.println("\n*** Nuke em");
        pm.currentTransaction().begin();
        Assert.assertEquals("a[b, c]", m1.toString());
        Assert.assertTrue(m1 == m2.getAliasedTo());
        Assert.assertTrue(m1 == m3.getAliasedTo());
        m1.nuke();
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test deleting the master in a one-to-many relationship with dependent
     * true.
     */
    public void testOneToManyDependentNukeMaster() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        Master master;
        Detail detail;

        pm.currentTransaction().begin();
        pm.makePersistent(master = new Master("master1"));
        pm.makePersistent(detail = new Detail("detail1"));
        master.addDetail(detail);
        Object oidMaster = pm.getObjectId(master);
        Object oidDetail = pm.getObjectId(detail);
        pm.currentTransaction().commit();

        // delete the master and make sure the detail also goes
        pm.currentTransaction().begin();
        pm.deletePersistent(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertDeleted(pm, oidMaster);
        assertDeleted(pm, oidDetail);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test removing a detail from a Master with dependent true. The detail
     * should not be deleted.
     */
    public void testOneToManyDependentRemoveDetail() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;
 
        PersistenceManager pm = pmf().getPersistenceManager();

        Master master;
        Detail detail;

        pm.currentTransaction().begin();
        pm.makePersistent(master = new Master("master2"));
        pm.makePersistent(detail = new Detail("detail2"));
        master.addDetail(detail);
        Object oidMaster = pm.getObjectId(master);
        Object oidDetail = pm.getObjectId(detail);
        pm.currentTransaction().commit();

        // remove the detail from the master and delete the master
        pm.currentTransaction().begin();
        detail.setMaster(null);
        pm.deletePersistent(master);
        pm.currentTransaction().commit();

        // make sure the detail is still around and the master is gone
        pm.currentTransaction().begin();
        assertDeleted(pm, oidMaster);
        Assert.assertTrue(detail == pm.getObjectById(oidDetail, true));
        pm.currentTransaction().commit();

        pm.close();
    }

    private void assertDeleted(PersistenceManager pm, Object oid) {
        try {
            pm.getObjectById(oid, true);
            throw new TestFailedException("Object still exists: " + oid);
        } catch (JDOObjectNotFoundException e) {
            // good
        }
    }

}
