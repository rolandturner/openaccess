
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

import com.versant.core.jdo.junit.test2.model.*;
import com.versant.core.jdo.junit.test2.model.refpk.Branch;
import com.versant.core.jdo.junit.test2.model.poly.*;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.junit.TestFailedException;
import com.versant.core.vds.util.Loid;

import javax.jdo.*;

import junit.framework.Assert;

import java.util.List;
import java.util.ArrayList;

/**
 * Tests for Object and interface references and collections.
 */
public class TestPolyRef2 extends VersantTestCase {

    /**
     * Test LOID to/from String.
     */
    public void testLOIDStringOps() throws Exception {
        if (!isVds()) {
            unsupported();
            return;
        }
        new LOIDTest(0, 0, 0, "0.0.0").test();
        new LOIDTest(1, 1, 1, "1.1.1").test();
        new LOIDTest(1, 2, 3, "1.2.3").test();
        new LOIDTest(0x7FFF, 0xFFFF, 0xFFFFFFFF, "32767.65535.4294967295").test();
    }

    private static class LOIDTest {
        int dbId;
        int objId1;
        long objId2;
        String str;

        public LOIDTest(int dbId, int objId1, long objId2, String str) {
            this.dbId = dbId;
            this.objId1 = objId1;
            this.objId2 = objId2;
            this.str = str;
        }

        public void test() {
            System.out.println("testing " + str);

            long loid = Loid.asValue(str);
            assertEquals(str, Loid.asString(loid));
            Loid o = new Loid(loid);
            assertEquals(dbId, o.getDatabaseId());
            assertEquals(objId1, o.getObjectId1());
            assertEquals(objId2, o.getObjectId2());
            assertEquals(str, Loid.asString(loid));
            assertEquals(loid, Loid.asValue(str));
        }
    }

    /**
     * Make sure the LOID related OID and getObject methods on VersantPM
     * work.
     */
    public void testLOIDLookup() throws Exception {
        if (true) {
            broken();
            return;
        }
        if (!isVds()) {
            unsupported();
            return;
        }

        /*
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        // make sure the LOID is allocated as soon as it is asked for
        pm.currentTransaction().begin();
        Address pc = new Address("street");
        assertEquals(0L, pm.getLOID(pc));
        pm.makePersistent(pc);
        long loid = pm.getLOID(pc);
        assertTrue(loid != 0);
        String loidStr = pm.getLOIDAsString(pc);
        assertEquals(Loid.asString(loid), loidStr);
        pm.currentTransaction().commit();

        // make sure lookup of hollow instance in local cache works and does
        // not make any calls the back end if validate false and loads
        // dfg if validate is true
        pm.currentTransaction().begin();
        findFetchEvents();
        assertTrue(pc == pm.getObjectByLOID(loid, false));
        assertEquals(0, findFetchEvents().length);
        assertTrue(pm.isHollow(pc));
        assertTrue(pc == pm.getObjectByLOID(loid, true));
        assertEquals(1, findFetchEvents().length);
        assertFalse(pm.isHollow(pc));
        pm.currentTransaction().commit();

        // check lookup by String
        pm.currentTransaction().begin();
        assertTrue(pc == pm.getObjectByLOIDString(loidStr, false));
        pm.currentTransaction().commit();

        // get a new PM to wipe local cache
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();

        // make sure lookup of instance not in local cache works
        pm.currentTransaction().begin();
        findFetchEvents();
        pc = (Address)pm.getObjectByLOID(loid, false); // validate ignored
        assertNotNull(pc);
        assertEquals(1, findFetchEvents().length);
        assertFalse(pm.isHollow(pc));
        assertEquals("street", pc.getStreet());
        pm.currentTransaction().commit();

        pm.close();
        */
    }

    /**
     * Test CRUD on a List of Object.
     */
    public void testObjectListCRUD() throws Exception {
        if (!isCollectionOfObjectSupported()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ObjectList o = new ObjectList("o");
        o.getList().add(new Address("a1"));
        Industry i1 = new Industry("i1");
        o.getList().add(i1);
        pm.makePersistent(o);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("o [a1 null, i1]", o.toString());
        Industry i0 = new Industry("i0");
        o.getList().set(0, i0);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("o [i0, i1]", o.toString());
        ArrayList a = new ArrayList();
        a.add(i1);
        a.add(i0);
        o.setList(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("o [i1, i0]", o.toString());
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test insert, navigation, updating and dependent deletion of a polyref.
     */
    public void testInsertNavigateUpdateDependent() throws Exception {
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);
        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "somecity");
        PolyRefHolder h = new PolyRefHolder("h", a);
        pm.makePersistent(h);
        Object hoid = pm.getObjectId(h);
        Object aoid = pm.getObjectId(a);
        pm.currentTransaction().commit();

        // get new PM so we get new instances of h and a
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);

        // navigate through data reference
        pm.currentTransaction().begin();
        h = (PolyRefHolder)pm.getObjectById(hoid, true);
        a = (Address)pm.getObjectById(aoid, true);
        Assert.assertTrue(h.getData() == a);
        // update data reference
        Product p = new Product(123, 456);
        h.setData(p);
        pm.currentTransaction().commit();

        // make sure p is in data
        pm.currentTransaction().begin();
        Assert.assertTrue(h.getData() == p);
        Object poid = pm.getObjectId(p);
        pm.currentTransaction().commit();

        // cleanup a and h
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(h);
        pm.currentTransaction().commit();

        // make sure that p is also gone
        pm.currentTransaction().begin();
        try {
            p = (Product)pm.getObjectById(poid, true);
            //System.out.println("p.getInterval() = " + p.getInterval());
            throw new TestFailedException("expected VersantObjectNotFoundException");
        } catch (JDOObjectNotFoundException e) {
            System.out.println("Good: " + e);
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test insert, navigation, updating and dependent deletion of a polyref
     * automatically persistent through a persistent interface.
     */
    public void testInsertNavigateUpdateDependentIFace() throws Exception {
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);
        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "somecity");
        PolyRefHolderIFace h = new PolyRefHolderIFace("h", a);
        pm.makePersistent(h);
        Object hoid = pm.getObjectId(h);
        Object aoid = pm.getObjectId(a);
        pm.currentTransaction().commit();

        // get new PM so we get new instances of h and a
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);

        // navigate through data reference
        pm.currentTransaction().begin();
        h = (PolyRefHolderIFace)pm.getObjectById(hoid, true);
        a = (Address)pm.getObjectById(aoid, true);
        Assert.assertTrue(h.getData() == a);
        // update data reference
        Product p = new Product(123, 456);
        h.setData(p);
        pm.currentTransaction().commit();

        // make sure p is in data
        pm.currentTransaction().begin();
        Assert.assertTrue(h.getData() == p);
        Object poid = pm.getObjectId(p);
        pm.currentTransaction().commit();

        // cleanup a and h
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(h);
        pm.currentTransaction().commit();

        // make sure that p is also gone
        pm.currentTransaction().begin();
        try {
            p = (Product)pm.getObjectById(poid, true);
            //System.out.println("p.getInterval() = " + p.getInterval());
            throw new TestFailedException("expected JDOObjectNotFoundException");
        } catch (JDOObjectNotFoundException e) {
            System.out.println("Good: " + e);
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test insert, navigation, updating and dependent deletion of a polyref
     * using String class-id values.
     */
    public void testInsertNavigateUpdateDependentStr() throws Exception {
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);
        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "somecity");
        PolyRefHolderStr h = new PolyRefHolderStr("h", a);
        pm.makePersistent(h);
        Object hoid = pm.getObjectId(h);
        Object aoid = pm.getObjectId(a);
        pm.currentTransaction().commit();

        // get new PM so we get new instances of h and a
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);

        // navigate through data reference
        pm.currentTransaction().begin();
        h = (PolyRefHolderStr)pm.getObjectById(hoid, true);
        a = (Address)pm.getObjectById(aoid, true);
        Assert.assertTrue(h.getData() == a);
        // update data reference
        Product p = new Product(123, 456);
        h.setData(p);
        pm.currentTransaction().commit();

        // make sure p is in data
        pm.currentTransaction().begin();
        Assert.assertTrue(h.getData() == p);
        Object poid = pm.getObjectId(p);
        pm.currentTransaction().commit();

        // cleanup a and h
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(h);
        pm.currentTransaction().commit();

        // make sure that p is also gone
        pm.currentTransaction().begin();
        try {
            p = (Product)pm.getObjectById(poid, true);
            //System.out.println("p.getInterval() = " + p.getInterval());
            throw new TestFailedException("expected JDOObjectNotFoundException");
        } catch (JDOObjectNotFoundException e) {
            System.out.println("Good: " + e);
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test insert, navigation, updating and dependent deletion of a polyref
     * using custom int class-id values.
     */
    public void testInsertNavigateUpdateDependentInt() throws Exception {
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);
        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "somecity");
        PolyRefHolderInt h = new PolyRefHolderInt("h", a);
        pm.makePersistent(h);
        Object hoid = pm.getObjectId(h);
        Object aoid = pm.getObjectId(a);
        pm.currentTransaction().commit();

        // get new PM so we get new instances of h and a
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);

        // navigate through data reference
        pm.currentTransaction().begin();
        h = (PolyRefHolderInt)pm.getObjectById(hoid, true);
        a = (Address)pm.getObjectById(aoid, true);
        Assert.assertTrue(h.getData() == a);
        // update data reference
        Product p = new Product(123, 456);
        h.setData(p);
        pm.currentTransaction().commit();

        // make sure p is in data
        pm.currentTransaction().begin();
        Assert.assertTrue(h.getData() == p);
        Object poid = pm.getObjectId(p);
        pm.currentTransaction().commit();

        // cleanup a and h
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(h);
        pm.currentTransaction().commit();

        // make sure that p is also gone
        pm.currentTransaction().begin();
        try {
            p = (Product)pm.getObjectById(poid, true);
            //System.out.println("p.getInterval() = " + p.getInterval());
            throw new TestFailedException("expected JDOObjectNotFoundException");
        } catch (JDOObjectNotFoundException e) {
            System.out.println("Good: " + e);
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test insert, navigation, updating and dependent deletion of a polyref
     * to comp pk classes.
     */
    public void testInsertNavigateUpdateDependentComp() throws Exception {
        if (!isApplicationIdentitySupported()) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);
        pm.currentTransaction().begin();
        CompoundPkClass a = new CompoundPkClass(65, 66, "AB");
        PolyRefHolderComp h = new PolyRefHolderComp("h", a);
        pm.makePersistent(h);
        Object hoid = pm.getObjectId(h);
        Object aoid = pm.getObjectId(a);
        pm.currentTransaction().commit();

        // get new PM so we get new instances of h and a
        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);

        // navigate through data reference
        pm.currentTransaction().begin();
        h = (PolyRefHolderComp)pm.getObjectById(hoid, true);
        a = (CompoundPkClass)pm.getObjectById(aoid, true);
        Assert.assertTrue(h.getData() == a);
        // update data reference
        Branch b = new Branch(100, "branch100");
        com.versant.core.jdo.junit.test2.model.refpk.Customer p =
                new com.versant.core.jdo.junit.test2.model.refpk.Customer(b, 200, "c200");
        h.setData(p);
        pm.currentTransaction().commit();

        // make sure p is in data
        pm.currentTransaction().begin();
        Assert.assertTrue(h.getData() == p);
        Object poid = pm.getObjectId(p);
        pm.currentTransaction().commit();

        // cleanup a and h
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(h);
        pm.deletePersistent(b);
        pm.currentTransaction().commit();

        // make sure that p is also gone
        pm.currentTransaction().begin();
        try {
            p = (com.versant.core.jdo.junit.test2.model.refpk.Customer)pm.getObjectById(poid, true);
            //System.out.println("p.getInterval() = " + p.getInterval());
            throw new TestFailedException("expected JDOObjectNotFoundException");
        } catch (JDOObjectNotFoundException e) {
            System.out.println("Good: " + e);
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test fetching a polyref in the default fetch group.
     */
    public void testFetchDFGPolyRef() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        PolyRefHolderDFG h = new PolyRefHolderDFG("oink", new Address("st", "ct"));
        pm.makePersistent(h);
        Object hoid = pm.getObjectId(h);
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        h = (PolyRefHolderDFG)pm.getObjectById(hoid, true);
        Assert.assertEquals("st", ((Address)h.getData()).getStreet());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(h.getData());
        pm.deletePersistent(h);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test polyref used as a query parameter.
     */
    public void testQueryParam() throws Exception {
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "somecity");
        PolyRefHolder h = new PolyRefHolder("h", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolder.class, "data == p");
        q.declareParameters("Object p");
        List ans = (List)q.execute(a);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(h);
        try {
            a.getCity();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test polyref using String class-id's used as a query parameter.
     */
    public void testQueryParamStr() throws Exception {
    	
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "somecity");
        PolyRefHolderStr h = new PolyRefHolderStr("h", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolderStr.class, "data == p");
        q.declareParameters("Object p");
        List ans = (List)q.execute(a);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(h);
        try {
            a.getCity();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test polyref of comp pks used as a query parameter.
     */
    public void testQueryParamComp() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        CompoundPkClass a = new CompoundPkClass(65, 66, "AB");
        PolyRefHolderComp h = new PolyRefHolderComp("h", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolderComp.class, "data == p");
        q.declareParameters("Object p");
        List ans = (List)q.execute(a);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(h);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test polyref using custom int class-id's used as a query parameter.
     */
    public void testQueryParamInt() throws Exception {
    	
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "somecity");
        PolyRefHolderInt h = new PolyRefHolderInt("h", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolderInt.class, "data == p");
        q.declareParameters("Object p");
        List ans = (List)q.execute(a);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(h);
        try {
            a.getCity();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test navigation though cast polyref in a query.
     */
    public void testQueryNavigation() throws Exception {

    	VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "ct");
        PolyRefHolder h = new PolyRefHolder("h", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolder.class, "((Address)data).city == p");
        q.declareParameters("String p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute("ct");
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(h);
        try {
            a.getCity();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test navigation though cast polyref using String class-id's in a query.
     */
    public void testQueryNavigationStr() throws Exception {
    	
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "ct");
        PolyRefHolderStr h = new PolyRefHolderStr("h", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolderStr.class, "((Address)data).city == p");
        q.declareParameters("String p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute("ct");
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(h);
        try {
            a.getCity();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test navigation though cast polyref using custom int class-id's in a
     * query.
     */
    public void testQueryNavigationInt() throws Exception {

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "ct");
        PolyRefHolderInt h = new PolyRefHolderInt("h", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolderInt.class, "((Address)data).city == p");
        q.declareParameters("String p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute("ct");
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(h);
        try {
            a.getCity();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test navigation though cast polyref of comp pks in a query.
     */
    public void testQueryNavigationComp() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        CompoundPkClass a = new CompoundPkClass(65, 66, "AB");
        PolyRefHolderComp h = new PolyRefHolderComp("h", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolderComp.class, "((CompoundPkClass)data).name == p");
        q.declareParameters("String p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute("AB");
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(h);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test contains(param) though cast polyref in a query.
     */
    public void testQueryContainsParam() throws Exception {

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Customer cust = new Customer("piggy");
        Contact contact = new Contact("piggy@oink", null);
        cust.addContact(contact);
        PolyRefHolder h = new PolyRefHolder("h", cust);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Query q = pm.newQuery(PolyRefHolder.class, "((Customer)data).contacts.contains(p)");
        q.declareParameters("Contact p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute(contact);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(contact);
//        pm.deletePersistent(cust);
        pm.deletePersistent(h);
        try {
            cust.getName();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test contains(param) though cast polyref usign String class-id's in a
     * query.
     */
    public void testQueryContainsParamStr() throws Exception {

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Customer cust = new Customer("piggy");
        Contact contact = new Contact("piggy@oink", null);
        cust.addContact(contact);
        PolyRefHolderStr h = new PolyRefHolderStr("h", cust);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Query q = pm.newQuery(PolyRefHolderStr.class, "((Customer)data).contacts.contains(p)");
        q.declareParameters("Contact p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute(contact);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(contact);
//        pm.deletePersistent(cust);
        pm.deletePersistent(h);
        try {
            cust.getName();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test contains(param) though cast polyref usign custom int class-id's in
     * a query.
     */
    public void testQueryContainsParamInt() throws Exception {

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Customer cust = new Customer("piggy");
        Contact contact = new Contact("piggy@oink", null);
        cust.addContact(contact);
        PolyRefHolderInt h = new PolyRefHolderInt("h", cust);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Query q = pm.newQuery(PolyRefHolderInt.class, "((Customer)data).contacts.contains(p)");
        q.declareParameters("Contact p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute(contact);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(contact);
//        pm.deletePersistent(cust);
        pm.deletePersistent(h);
        try {
            cust.getName();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test contains(param) though cast polyref to comp pks in a query.
     */
    public void testQueryContainsParamComp() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;
    	
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

//        pm.currentTransaction().begin();
//        CompoundPkClassWithList a = new CompoundPkClassWithList(65, 66, "AB");
//        Contact contact = new Contact("piggy@oink", null);
//        a.getList().add(contact);
//        PolyRefHolderComp h = new PolyRefHolderComp("h", a);
//        pm.makePersistent(h);
//        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        CompoundPkClassWithList a = new CompoundPkClassWithList(65, 66, "AB");
        CompoundPkClass element = new CompoundPkClass(67, 68, "CD");
        a.getList().add(element);
        PolyRefHolderComp h = new PolyRefHolderComp("h", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Query q = pm.newQuery(PolyRefHolderComp.class,
                "((CompoundPkClassWithList)data).list.contains(p)");
        q.declareParameters("CompoundPkClass p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute(element);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(element);
        pm.deletePersistent(h);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test startsWith(param) though cast polyref in a query.
     */
    public void testQueryStartsWithParam() throws Exception {

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "ct");
        PolyRefHolder h = new PolyRefHolder("h", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolder.class, "((Address)data).city.startsWith(p)");
        q.declareParameters("String p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute("c");
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
//        pm.deletePersistent(a);
        pm.deletePersistent(h);
        try {
            a.getCity();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test startsWith(param) though cast polyref using String class-id's in a
     * query.
     */
    public void testQueryStartsWithParamStr() throws Exception {

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "ct");
        PolyRefHolderStr h = new PolyRefHolderStr("h", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolderStr.class, "((Address)data).city.startsWith(p)");
        q.declareParameters("String p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute("c");
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
//        pm.deletePersistent(a);
        pm.deletePersistent(h);
        try {
            a.getCity();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test startsWith(param) though cast polyref using custom int class-id's
     * in a query.
     */
    public void testQueryStartsWithParamInt() throws Exception {

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "ct");
        PolyRefHolderInt h = new PolyRefHolderInt("h", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolderInt.class, "((Address)data).city.startsWith(p)");
        q.declareParameters("String p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute("c");
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
//        pm.deletePersistent(a);
        pm.deletePersistent(h);
        try {
            a.getCity();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test startsWith(param) though cast polyref to comp pks in a query.
     */
    public void testQueryStartsWithParamComp() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        CompoundPkClass a = new CompoundPkClass(65, 66, "AB");
        PolyRefHolderComp h = new PolyRefHolderComp("h", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolderComp.class, "((CompoundPkClass)data).name.startsWith(p)");
        q.declareParameters("String p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute("A");
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
//        pm.deletePersistent(a);
        pm.deletePersistent(h);
        try {
            a.getName();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test contains(variable) navigating through a polyref.
     */
    public void testQueryContainsVariable() throws Exception {

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Customer cust = new Customer("piggy");
        Contact contact = new Contact("piggy@oink", null);
        cust.addContact(contact);
        PolyRefHolder h = new PolyRefHolder("h", cust);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Query q = pm.newQuery(PolyRefHolder.class,
            "((Customer)data).contacts.contains(v) && v.email == \"piggy@oink\"");
        q.declareVariables("Contact v");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(contact);
//        pm.deletePersistent(cust);
        pm.deletePersistent(h);
        try {
            cust.getName();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test contains(variable) navigating through a polyref.
     */
    public void testQueryContainsVariableStr() throws Exception {

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Customer cust = new Customer("piggy");
        Contact contact = new Contact("piggy@oink", null);
        cust.addContact(contact);
        PolyRefHolderStr h = new PolyRefHolderStr("h", cust);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Query q = pm.newQuery(PolyRefHolderStr.class,
            "((Customer)data).contacts.contains(v) && v.email == \"piggy@oink\"");
        q.declareVariables("Contact v");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(contact);
//        pm.deletePersistent(cust);
        pm.deletePersistent(h);
        try {
            cust.getName();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test contains(variable) navigating through a polyref using custom
     * int class-id's.
     */
    public void testQueryContainsVariableInt() throws Exception {

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Customer cust = new Customer("piggy");
        Contact contact = new Contact("piggy@oink", null);
        cust.addContact(contact);
        PolyRefHolderInt h = new PolyRefHolderInt("h", cust);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Query q = pm.newQuery(PolyRefHolderInt.class,
            "((Customer)data).contacts.contains(v) && v.email == \"piggy@oink\"");
        q.declareVariables("Contact v");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute();
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(contact);
//        pm.deletePersistent(cust);
        pm.deletePersistent(h);
        try {
            cust.getName();
            fail("Expected read exception on deleted instance");
        } catch (JDOUserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test navigation though two polyref casts in a query.
     */
    public void testQueryDoubleNavigation() throws Exception {

    	VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "ct");
        PolyRefHolder h2 = new PolyRefHolder("h2", a);
        PolyRefHolder h = new PolyRefHolder("h", h2);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolder.class,
                "((Address)((PolyRefHolder)data).data).city == p");
        q.declareParameters("String p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute("ct");
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(h);
        pm.deletePersistent(h2);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test navigation though three polyref casts in a query.
     */
    public void testQueryTripleNavigation() throws Exception {

    	VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "ct");
        PolyRefHolder h3 = new PolyRefHolder("h2", a);
        PolyRefHolder h2 = new PolyRefHolder("h2", h3);
        PolyRefHolder h = new PolyRefHolder("h", h2);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PolyRefHolder.class,
                "((Address)((PolyRefHolder)((PolyRefHolder)data).data).data).city == p");
        q.declareParameters("String p");
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        List ans = (List)q.execute("ct");
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(h);
        pm.deletePersistent(h2);
        pm.deletePersistent(h3);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test polyref navigated in the order by for a query.
     */
    public void testQueryOrderBy() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("a1", "ct");
        Address a2 = new Address("a2", "ct");
        PolyRefHolder h = new PolyRefHolder("h", a);
        PolyRefHolder h2 = new PolyRefHolder("h2", a2);
        pm.makePersistent(h);
        pm.makePersistent(h2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Query q = pm.newQuery(PolyRefHolder.class);
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        q.setOrdering("((Address)data).street ascending");
        List ans = (List)q.execute();
        Assert.assertEquals(2, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        Assert.assertTrue(ans.get(1) == h2);
        q.closeAll();

        q = pm.newQuery(PolyRefHolder.class);
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        q.setOrdering("((Address)data).street descending");
        ans = (List)q.execute();
        Assert.assertEquals(2, ans.size());
        Assert.assertTrue(ans.get(0) == h2);
        Assert.assertTrue(ans.get(1) == h);
        q.closeAll();

        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(a2);
        pm.deletePersistent(h);
        pm.deletePersistent(h2);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test polyref using String class-id's navigated in the order by for a
     * query.
     */
    public void testQueryOrderByStr() throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("a1", "ct");
        Address a2 = new Address("a2", "ct");
        PolyRefHolderStr h = new PolyRefHolderStr("h", a);
        PolyRefHolderStr h2 = new PolyRefHolderStr("h2", a2);
        pm.makePersistent(h);
        pm.makePersistent(h2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Query q = pm.newQuery(PolyRefHolderStr.class);
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        q.setOrdering("((Address)data).street ascending");
        List ans = (List)q.execute();
        Assert.assertEquals(2, ans.size());
        Assert.assertTrue(ans.get(0) == h);
        Assert.assertTrue(ans.get(1) == h2);
        q.closeAll();

        q = pm.newQuery(PolyRefHolderStr.class);
        q.declareImports("import com.versant.core.jdo.junit.test2.model.*");
        q.setOrdering("((Address)data).street descending");
        ans = (List)q.execute();
        Assert.assertEquals(2, ans.size());
        Assert.assertTrue(ans.get(0) == h2);
        Assert.assertTrue(ans.get(1) == h);
        q.closeAll();

        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(a2);
        pm.deletePersistent(h);
        pm.deletePersistent(h2);
        pm.currentTransaction().commit();

        pm.close();
    }

}
