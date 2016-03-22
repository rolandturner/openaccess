
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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.versant.core.common.Debug;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test0.model.*;
import com.versant.core.jdo.junit.TestFailedException;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.util.IntArray;

import javax.jdo.JDODataStoreException;
import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @keep-all
 */
public class TestApplicationPK extends VersantTestCase {

    public TestApplicationPK(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testCreateAppIdOnMapPers1",
            "testCreateAppIdOnMapPers2",
            "testCreateAppIdOnMapPers3",
            "testCreateAppIdOnMapPers4",
            "testReadPKFieldOnNewInstance",
            "testGetObjectIdOnNewInstance",
            "testGetObjectIdOnNewInstance2",
            "testSimleAP_PK1",
            "testSimleAP_PK2",
            "testGetObjectID",
            "testChangeID",
            "testAppIdInheritedFromBase",
            "testGetObjectByID",
            "testQueryForAppIDObjects",
            "testAppIdString",
            "testReadAppIdKeyValueAfterCommit",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestApplicationPK(a[i]));
        }
        return s;
    }

    /**
     * Test the working of obtaining appId instance from AppId instance
     * before commit.
     */
    public void testCreateAppIdOnMapPers1() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        nuke(SimpleAP_KeyGen.class);
        pmf().evictAll();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        List created = new ArrayList();
        SimpleAP_KeyGen simpleAP_keyGen = null;

        for (int i = 0; i < 3; i++) {
            simpleAP_keyGen = new SimpleAP_KeyGen();
            simpleAP_keyGen.setIdNo(i);
            simpleAP_keyGen.setName("name" + i);
            created.add(simpleAP_keyGen);
        }
        pm.makePersistentAll(created);

        simpleAP_keyGen = (SimpleAP_KeyGen)pm.getObjectById(
                new SimpleAP_PK_KeyGen("2"), true);
        Assert.assertEquals("name2", simpleAP_keyGen.getName());
        Assert.assertEquals(2, simpleAP_keyGen.getIdNo());

        simpleAP_keyGen = (SimpleAP_KeyGen)pm.getObjectById(
                new SimpleAP_PK_KeyGen("0"), true);
        Assert.assertEquals("name0", simpleAP_keyGen.getName());
        Assert.assertEquals(0, simpleAP_keyGen.getIdNo());

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        simpleAP_keyGen = (SimpleAP_KeyGen)pm.getObjectById(
                new SimpleAP_PK_KeyGen("2"), true);
        Assert.assertEquals("name2", simpleAP_keyGen.getName());
        Assert.assertEquals(2, simpleAP_keyGen.getIdNo());

        simpleAP_keyGen = (SimpleAP_KeyGen)pm.getObjectById(
                new SimpleAP_PK_KeyGen("0"), true);
        Assert.assertEquals("name0", simpleAP_keyGen.getName());
        Assert.assertEquals(0, simpleAP_keyGen.getIdNo());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(created);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testCreateAppIdOnMapPers2() {
        if (!isApplicationIdentitySupported()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        List created = new ArrayList();
        SimpleAP_KeyGen simpleAP_keyGen = null;

        for (int i = 0; i < 3; i++) {
            simpleAP_keyGen = new SimpleAP_KeyGen();
            simpleAP_keyGen.setIdNo(i);
            simpleAP_keyGen.setName("name" + i);
            created.add(simpleAP_keyGen);
        }
        pm.makePersistentAll(created);

        simpleAP_keyGen = (SimpleAP_KeyGen)pm.getObjectById(
                new SimpleAP_PK_KeyGen("2"), true);
        Assert.assertEquals("name2", simpleAP_keyGen.getName());
        Assert.assertEquals(2, simpleAP_keyGen.getIdNo());

        simpleAP_keyGen = (SimpleAP_KeyGen)pm.getObjectById(
                new SimpleAP_PK_KeyGen("0"), true);
        Assert.assertEquals("name0", simpleAP_keyGen.getName());
        Assert.assertEquals(0, simpleAP_keyGen.getIdNo());

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        simpleAP_keyGen = (SimpleAP_KeyGen)pm.getObjectById(
                new SimpleAP_PK_KeyGen("2"), true);
        Assert.assertEquals("name2", simpleAP_keyGen.getName());
        Assert.assertEquals(2, simpleAP_keyGen.getIdNo());

        simpleAP_keyGen = (SimpleAP_KeyGen)pm.getObjectById(
                new SimpleAP_PK_KeyGen("0"), true);
        Assert.assertEquals("name0", simpleAP_keyGen.getName());
        Assert.assertEquals(0, simpleAP_keyGen.getIdNo());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(created);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testCreateAppIdOnMapPers3() {
        if (!isApplicationIdentitySupported()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        List created = new ArrayList();
        SimpleAP_KeyGen simpleAP_keyGen = null;

        for (int i = 0; i < 3; i++) {
            simpleAP_keyGen = new SimpleAP_KeyGen();
            simpleAP_keyGen.setName("name" + i);
            created.add(simpleAP_keyGen);
        }
        pm.makePersistentAll(created);

        try {
            simpleAP_keyGen = (SimpleAP_KeyGen)pm.getObjectById(
                    new SimpleAP_PK_KeyGen("2"), false);
            Utils.fail(
                    "getObjectById for key generator instances should always check if exist in db");
        } catch (JDODataStoreException e) {
            //expected
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(created);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testCreateAppIdOnMapPers4() {
        if (!isApplicationIdentitySupported()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        List created = new ArrayList();
        AppIdString inst = null;

        for (int i = 0; i < 3; i++) {
            inst = new AppIdString();
            inst.setId("" + i);
            inst.setName("name" + i);
            created.add(inst);
        }
        pm.makePersistentAll(created);

        inst = (AppIdString)pm.getObjectById(new AppIdString.ID("2"), true);
        Assert.assertEquals("name2", inst.getName());
        Assert.assertEquals("2", inst.getId());

        inst = (AppIdString)pm.getObjectById(new AppIdString.ID("0"), true);
        Assert.assertEquals("name0", inst.getName());
        Assert.assertEquals("0", inst.getId());

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        inst = (AppIdString)pm.getObjectById(new AppIdString.ID("2"), true);
        Assert.assertEquals("name2", inst.getName());
        Assert.assertEquals("2", inst.getId());

        inst = (AppIdString)pm.getObjectById(new AppIdString.ID("0"), true);
        Assert.assertEquals("name0", inst.getName());
        Assert.assertEquals("0", inst.getId());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(created);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testGetObjectIdOnNewInstance() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        nuke(SimpleAP_KeyGen.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        List created = new ArrayList();
        SimpleAP_KeyGen simpleAP_keyGen = null;

        for (int i = 0; i < 3; i++) {
            simpleAP_keyGen = new SimpleAP_KeyGen();
            simpleAP_keyGen.setName("name" + i);
            created.add(simpleAP_keyGen);

        }

        pm.makePersistentAll(created);

        simpleAP_keyGen = (SimpleAP_KeyGen)created.get(1);
        Object id1 = pm.getObjectId(simpleAP_keyGen);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SimpleAP_KeyGen.class, "name == \"name1\"");
        List results = (List)q.execute();

        Object id2 = pm.getObjectId(results.get(0));
        Assert.assertEquals(id1, id2);

        pm.currentTransaction().commit();
        pm.close();
    }

    public void testGetObjectIdOnNewInstance2() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        nuke(SimpleAP_KeyGen.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        List created = new ArrayList();
        SimpleAP_KeyGen simpleAP_keyGen = null;

        for (int i = 0; i < 3; i++) {
            simpleAP_keyGen = new SimpleAP_KeyGen();
            simpleAP_keyGen.setName("name" + i);
            created.add(simpleAP_keyGen);

        }

        pm.makePersistentAll(created);

        simpleAP_keyGen = (SimpleAP_KeyGen)created.get(1);

        Object id1 = pm.getObjectId(simpleAP_keyGen);
        Object id2 = pm.getObjectId(simpleAP_keyGen);
        Object id3 = pm.getObjectId(simpleAP_keyGen);
        Object id4 = pm.getObjectId(simpleAP_keyGen);
        Object id5 = pm.getObjectId(simpleAP_keyGen);
        Assert.assertEquals(id1, id2);
        Assert.assertEquals(id1, id3);
        Assert.assertEquals(id1, id4);
        Assert.assertEquals(id1, id4);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SimpleAP_KeyGen.class, "name == \"name1\"");
        List results = (List)q.execute();

        Object id6 = pm.getObjectId(results.get(0));
        Assert.assertEquals(id1, id6);

        pm.currentTransaction().commit();
        pm.close();

    }

    public void testReadPKFieldOnNewInstance() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        nuke(SimpleAP_KeyGen.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        List created = new ArrayList();
        SimpleAP_KeyGen simpleAP_keyGen = null;

        for (int i = 0; i < 3; i++) {
            simpleAP_keyGen = new SimpleAP_KeyGen();
            simpleAP_keyGen.setName("name" + i);
            created.add(simpleAP_keyGen);

        }

        pm.makePersistentAll(created);

        simpleAP_keyGen = (SimpleAP_KeyGen)created.get(1);

        IntArray ids = new IntArray();
        for (int i = 0; i < created.size(); i++) {
            simpleAP_keyGen = (SimpleAP_KeyGen)created.get(i);
//            pm.getObjectId(simpleAP_keyGen);
            ids.add(simpleAP_keyGen.getIdNo());
        }

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SimpleAP_KeyGen.class,
                "name.startsWith(\"name\")");
        q.setOrdering("name ascending");
        List results = (List)q.execute();
        Assert.assertEquals(3, results.size());

        for (int i = 0; i < results.size(); i++) {
            simpleAP_keyGen = (SimpleAP_KeyGen)results.get(i);
            Assert.assertEquals(ids.get(i), simpleAP_keyGen.getIdNo());
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * If app. pk is used and the user specifies a key gen the the field must
     * be filled in by the store. This field should be filled in after the store
     * commit is finished.
     *
     * @throws Exception
     */
    public void testSimleAP_PK1() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        List created = new ArrayList();
        SimpleAP_KeyGen simpleAP_keyGen = null;

        for (int i = 0; i < 10; i++) {
            simpleAP_keyGen = new SimpleAP_KeyGen();
            simpleAP_keyGen.setName("name");
            created.add(simpleAP_keyGen);
            pm.makePersistentAll(created);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Iterator iterator = created.iterator();
        while (iterator.hasNext()) {
            if (((SimpleAP_KeyGen)iterator.next()).getIdNo() <= 0) {
                Utils.fail("keygen problem");
            }
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * If app. pk is used and the user specifies a key gen the the field must
     * be filled in by the store. This field should be filled in after the store
     * commit is finished.
     *
     * @throws Exception
     */
    public void testSimleAP_PK2() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        List created = new ArrayList();
        SimpleAP_KeyGen simpleAP_keyGen = null;

        for (int i = 0; i < 10; i++) {
            simpleAP_keyGen = new SimpleAP_KeyGen();
            simpleAP_keyGen.setName("name");
            created.add(simpleAP_keyGen);
            pm.makePersistentAll(created);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Iterator iterator = created.iterator();
        while (iterator.hasNext()) {
            SimpleAP_KeyGen simple = ((SimpleAP_KeyGen)iterator.next());
            if (simple.getIdNo() <= 0) {
                throw new TestFailedException("Key field not filled in");
            }
//            Debug.out.println("@@@@@@@@@@@ id = " + simple.getIdNo());
//            Debug.out.println("@@@@@@@@@@@ name = " + simple.getName());

        }
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * This is a test to obtain a app pk from jdo and check that it is not null.
     *
     * @throws Exception
     */
    public void testGetObjectID() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        nuke(SimpleAP.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SimpleAP_KeyGen simpleKG = new SimpleAP_KeyGen();
        simpleKG.setName("nameKG");
        SimpleAP simple = new SimpleAP();
        simple.setName("name");
        simple.setIdNo(1);

        pm.makePersistent(simple);
        pm.makePersistent(simpleKG);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertNotNull(pm.getObjectId(simple));
        Assert.assertNotNull(pm.getObjectId(simpleKG));
        Assert.assertEquals("1", pm.getObjectId(simple).toString());
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * This is a test to try and change the id of the instance. It is not currently
     * allowed and should fail.
     *
     * @throws Exception
     */
    public void testChangeID() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        int idInt = (int)TIME1;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SimpleAP_KeyGen simpleKG = new SimpleAP_KeyGen();
        simpleKG.setName("nameKG");
        SimpleAP simple = new SimpleAP();
        simple.setIdNo(idInt);
        simple.setName("name");

        pm.makePersistent(simple);
        pm.makePersistent(simpleKG);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        try {
            simple.setIdNo(44);
            Assert.fail(
                    "Must not be able to change application id field as it is not supported as yet.");
        } catch (JDOException e) {
            if (Debug.DEBUG) {
                Debug.OUT.println(e.getMessage());
            }

            //ignore
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testAppIdInheritedFromBase() throws Exception {
    	if (!isApplicationIdentitySupported())
    	{
    		unsupported();
    		return;
    	}
        int idInt = (int)TIME2;
        if (Debug.DEBUG) {
            Debug.OUT.println("############## 111 idInt = " + idInt);
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SimpleAPSub simpleAPSub = new SimpleAPSub();
        simpleAPSub.setIdNo(idInt);
        simpleAPSub.setName("sub");
        pm.makePersistent(simpleAPSub);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertNotNull(pm.getObjectId(simpleAPSub));
        Assert.assertEquals(String.valueOf(idInt),
                pm.getObjectId(simpleAPSub).toString());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testGetObjectByID() throws Exception {
    	if (!isApplicationIdentitySupported())
    	{
    		unsupported();
    		return;
    	}
        int idInt = (int)TIME3;
        if (Debug.DEBUG) {
            Debug.OUT.println("############## 222 idInt = " + idInt);
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SimpleAPSub simpleAPSub = new SimpleAPSub();
        simpleAPSub.setIdNo(idInt);
        simpleAPSub.setName("sub");
        pm.makePersistent(simpleAPSub);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        SimpleAPSub simpleAPSub2 = (SimpleAPSub)pm.getObjectById(
                new SimpleAP_PK(String.valueOf(idInt)), false);
        Assert.assertNotNull(pm.getObjectId(simpleAPSub2));
        Assert.assertNotNull(pm.getObjectId(simpleAPSub));
        Assert.assertEquals(String.valueOf(idInt),
                pm.getObjectId(simpleAPSub).toString());
        Assert.assertEquals(String.valueOf(idInt),
                pm.getObjectId(simpleAPSub2).toString());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testQueryForAppIDObjects() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        nuke(SimpleAP_KeyGen.class);
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SimpleAP_KeyGen simpleKG = null;
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p" + i);
            simpleKG = new SimpleAP_KeyGen();
            simpleKG.setName("n" + i);
            p.setSimpleAP_keyGen(simpleKG);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        Iterator iter = pm.getExtent(Person.class, false).iterator();
        for (; iter.hasNext();) {
            Person ps = (Person)iter.next();
            if (ps.getSimpleAP_keyGen().getIdNo() == 0) {
                throw new TestFailedException("The id must be set");
            }
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test application identity with a non-primitive PK field e.g. a String.
     */
    public void testAppIdString() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        pm.makePersistent(new AppIdString("foo", "bar"));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Object oid = new AppIdString.ID("foo");
        AppIdString o = (AppIdString)pm.getObjectById(oid, true);
        Assert.assertEquals(o.getId(), "foo");
        Assert.assertEquals(o.getName(), "bar");
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test to read app id after commit with retainValues == false.
     * The test is to ensure that the app id field is still available.
     *
     * @throws Exception
     */
    public void testReadAppIdKeyValueAfterCommit() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        nuke(SimpleAP.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        List created = new ArrayList();
        SimpleAP simpleAP = null;

        for (int i = 0; i < 3; i++) {
            simpleAP = new SimpleAP();
            simpleAP.setIdNo(i + 1);
            simpleAP.setName("name" + simpleAP.getIdNo());
            created.add(simpleAP);
        }

        pm.makePersistentAll(created);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        simpleAP = (SimpleAP)created.get(0);
        simpleAP.getName();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        simpleAP = (SimpleAP)created.get(0);
        Assert.assertEquals(1, simpleAP.getIdNo());
        pm.currentTransaction().commit();

        pm.close();
    }

}

