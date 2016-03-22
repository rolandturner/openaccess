
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
package com.versant.core.jdo.junit.test1;

import junit.framework.Assert;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test1.model.*;

import javax.jdo.PersistenceManager;
import javax.jdo.JDOObjectNotFoundException;

/**
 * First simple VDS test cases.
 */
public class TestMap extends VersantTestCase {

    /**
     * Test create retrieve update delete for a Testable instance.
     */
    public Object testCRUD(Testable pc) {
        PersistenceManager pm1 = pmf().getPersistenceManager();

        // store a Testable instance
        pm1.currentTransaction().begin();
        pm1.makePersistent(pc);
        pm1.currentTransaction().commit();
        Object oid = pm1.getObjectId(pc);
        System.out.println("oid = " + oid);

        
        // get it back and update it
        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().begin();
        Testable pc2 = (Testable)pm2.getObjectById(oid, true);
        Assert.assertTrue(pc.equals(pc2));
        pc2.update();
        pm2.currentTransaction().commit();

        // check the update worked and delete it
        PersistenceManager pm3 = pmf().getPersistenceManager();
        pm3.currentTransaction().begin();
        Object pc3 = pm3.getObjectById(oid, true);
        Assert.assertTrue(pc2.equals(pc3));
        pm3.deletePersistent(pc3);
        pm3.currentTransaction().commit();

        

        // make sure it is gone
        PersistenceManager pm4 = pmf().getPersistenceManager();
        pm4.currentTransaction().begin();
        try {
            pm4.getObjectById(oid, true);
            Assert.assertFalse(true);
        } catch (JDOObjectNotFoundException e) {
            // good
        }
        pm4.currentTransaction().commit();

        pm1.close();
        pm2.close();
        pm3.close();
        pm4.close();
        
        return oid;
    }

    /**
     * Test create retrieve update delete for a Friend instance.
     */
    public void testStringKeyStringValueMap() {
        StringKeyStringValueMap pc = new StringKeyStringValueMap();
        pc.getMap().put("key1","value1");
        pc.getMap().put("key2","value2");
        testCRUD(pc);
    }
    public void testStringKeyPCValueMap() {
        StringKeyPCValueMap pc = new StringKeyPCValueMap();
        pc.getMap().put("key1",new Simple(10));
        pc.getMap().put("key2",new Simple(20));
        testCRUD(pc);
    }
    public void testPCKeyPCValueMap() {
        PCKeyPCValueMap pc = new PCKeyPCValueMap();
        pc.getMap().put(new Simple(10),new Simple(20));
        pc.getMap().put(new Simple(30),new Simple(60));
        testCRUD(pc);
    }
    public void testPCKeyStringValueMap() {
        PCKeyStringValueMap pc = new PCKeyStringValueMap();
        pc.getMap().put(new Simple(10), "value1");
        pc.getMap().put(new Simple(20), "value2");
        testCRUD(pc);
    }
    public void testObjectFieldWithPCValue() {
        ObjectFieldWithPCValue pc = new ObjectFieldWithPCValue();
        testCRUD(pc);
    }
    
    public void testNullMap() {
        PCKeyStringValueMap pc = new PCKeyStringValueMap();
        pc.setMap(null);
        testCRUD(pc);
        
    }


}

