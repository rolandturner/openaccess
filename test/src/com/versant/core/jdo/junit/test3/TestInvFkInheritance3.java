
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
package com.versant.core.jdo.junit.test3;

import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test3.model.fabian.LiquidSub;
import com.versant.core.jdo.junit.test3.model.fabian.Acto;
import com.versant.core.jdo.VersantPersistenceManager;

import javax.jdo.*;

import junit.framework.Assert;

/**
 * Tests for one-to-many and many-to-many unmanaged relationships.
 */
public class TestInvFkInheritance3 extends VersantTestCase {

    /**
     * Make sure that consistency checking for one-to-many relationships
     * works properly.
     */
    public void testOneToManyConsistencyCheck() throws Exception {
        if (true) {
            // this test fails randomly - we have to figure out why sometime
            broken();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        // check: detail (many) in collection on master but has been deleted
        pm.currentTransaction().begin();
        LiquidSub liquidSub = new LiquidSub("pkLic0", "pkSol0", 10);
        Acto acto = new Acto("pkActo0", "pkLic0", "pkSol0", 50);
        liquidSub.getActos().add(acto);
        pm.makePersistent(liquidSub);
        pm.makePersistent(acto);
        pm.deletePersistent(acto);
        try {
            // acto has been deleted so this should fail
            pm.currentTransaction().commit();
            fail("expected JDOFatalUserException");
        } catch (JDOFatalUserException e) {
            // good
            System.out.println("Got: " + e);
        }

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistentAll(new Object[]{liquidSub, acto});
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure that fake one-to-many works properly.
     */
    public void testOneToManyArrayList() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        ((VersantPersistenceManager)pm).setCheckModelConsistencyOnCommit(false);

        // make sure relationship is not completed either way when instances
        // are made persistent
        pm.currentTransaction().begin();

        LiquidSub liquidSub1 = new LiquidSub("pkLic1", "pkSol1", 5);
        Acto acto1 = new Acto("pkActo1", "pkLic1", "pkSol1", 10);
        liquidSub1.add(acto1);
        pm.makePersistent(liquidSub1);

        LiquidSub liquidSub2 = new LiquidSub("pkLic2", "pkSol2", 10);
        pm.makePersistent(liquidSub2);
        Acto acto2 = new Acto("pkActo2", "pkLic2", "pkSol2", 20);
        liquidSub2.add(acto2);

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();

        // Make sure acto1 is not linked to liquidSub1 after commit as the back
        // reference was not set. Make sure that acto2 is linked to liquidSub2
        // as its back reference was set
        Assert.assertTrue(liquidSub1.getActos().contains(acto1));
        Assert.assertTrue(liquidSub2.getActos().contains(acto2));

        pm.currentTransaction().commit();

        // remove acto2 from liquidSub2
        pm.currentTransaction().begin();
        pm.deletePersistent(acto2);
        pm.currentTransaction().commit();

        // make sure change was done properly
        pm.currentTransaction().begin();
        Assert.assertFalse(liquidSub2.getActos().contains(acto2));
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistentAll(new Object[]{liquidSub1, liquidSub2, acto1});
        pm.currentTransaction().commit();

        pm.close();
    }
}
