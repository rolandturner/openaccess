
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
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test0.model.*;
import com.versant.core.jdo.junit.TestFailedException;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.jdo.junit.Utils;

import javax.jdo.Extent;
import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.JDOObjectNotFoundException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 */
public class TestDeletes extends VersantTestCase {

    public TestDeletes(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite("Deletes");
        String[] a = new String[]{
            "testDeleteMasterDetailWithLTable",
            "testDeleteWithDependentMap",
            "testDeleteSubClassWithDependentCollection",
            "testDeleteTransient",
            "testDeletePNew",
            "testDeleteP2",
            "testDeleteMaster",
            "testDeleteMaster2",
            "testWriteOnToBeDeletedInstance",
            "testReadOnToBeDeletedInstance",
            "testDeleteLinkTable",
            "testDeleteRefObject",
            "testDependentRefInSuperclass",
            "testRemoveElementFromDependentFKCollection",
            "testDeleteSecondaryField",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestDeletes(a[i]));
        }
        return s;
    }

    /**
     * Check that secondary instances are cleaned up on delete.
     */
    public void testDeleteSecondaryField() throws Exception {
        if (!isVds()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ColNuke cn = new ColNuke();
        cn.getList().add(new Address("str0"));
        cn.getList().add(new Address("str1"));
        pm.makePersistent(cn);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        cn.getList(); // load the list
        pm.deletePersistent(cn);
        pm.currentTransaction().commit();
        pm.close();

        // TODO make sure that the SCO in VDS was nuked and no extra reads done
    }

    public void testDeleteMasterDetailWithLTable() throws Exception {
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 1; i++) {
            Master2 master = new Master2();
            Detail2 detail = new Detail2();
            master.add(detail);

            pm.makePersistent(detail);
            pm.makePersistent(master);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        Extent terminals = null;
        Iterator i = null;

        pm.currentTransaction().begin();
        terminals = pm.getExtent(Master2.class, false);

        i = terminals.iterator();
        while (i.hasNext()) {
            Master2 master2 = (Master2)i.next();

            List details = master2.getDetails();
            for (int j = 0; j < details.size(); j++) {
                Detail2 detail2 = (Detail2)details.get(j);
                detail2.setMaster(null);
            }

            pm.deletePersistentAll(master2.getDetails());
            master2.getDetails().clear();

            pm.deletePersistent(master2);
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDeleteRefObject() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        Address address = new Address();
        p.setAddress(address);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(p);
        pm.deletePersistent(address);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDeleteWithDependentMap() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mapModel = new MapModel();
        mapModel.getDependentMap().put("bla", new Person("toBeDeleted"));
        pm.makePersistent(mapModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        mapModel.getDependentMap();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(mapModel);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDeleteSubClassWithDependentCollection() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Employee emp = new Employee();
        emp.getPersons().add(new Person("bla"));
        pm.makePersistent(emp);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(emp);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        emp = (Employee)pm.getObjectById(id, true);
        pm.deletePersistent(emp);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDeleteTransient() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        try {
            pm.deletePersistent(p);
            Utils.fail("Deleting a transient instance must throw an exception");
        } catch (TestFailedException e) {
            throw e;
        } catch (Exception e) {
            //ignore
        }
        pm.close();
    }

    public void testDeletePNew() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.deletePersistent(p);
        Utils.assertTrue(Utils.isPNewDeleted(p));
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test the delete with a list of refs filled.
     *
     * @throws Exception
     */
    public void testDeleteP2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        List persons = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("collPerson" + i);
            persons.add(p);
        }

        Person p = new Person("name");
        p.setPersonsList(persons);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(p);
        Utils.assertTrue(Utils.isPDeleted(p));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testWriteOnToBeDeletedInstance() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.deletePersistent(p);
        try {
            p.setName("name2");
            Utils.fail(
                    "Must not be allowed to write to instance that is marked for deletion");
        } catch (TestFailedException e) {
            throw e;
        } catch (Exception e) {
            //ignore
        }
        pm.close();

    }

    public void testReadOnToBeDeletedInstance() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        p.getName();
        pm.deletePersistent(p);
        try {
            p.getName();
            Utils.fail(
                    "Must not be allowed to read from instance that is marked for deletion");
        } catch (TestFailedException e) {
            throw e;
        } catch (Exception e) {
            //ignore
        }
        pm.close();

    }

    public void testDeleteMaster() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Master master = new Master();
        pm.makePersistent(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master.getDetailSize();
        pm.deletePersistent(master);
        pm.currentTransaction().commit();
        pm.close();

    }

    public void testDeleteMaster2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Master master = new Master();
        Detail detail = new Detail();
        master.add(detail);
        pm.makePersistent(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Object mID = pm.getObjectId(master);
        Object dID = pm.getObjectId(detail);
        master.getDetailSize();
        pm.deletePersistent(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        try {
            pm.getObjectById(mID, true);
            Utils.fail("Must be deleted");
        } catch (JDOException e) {
            //expected
        }
        try {
            pm.getObjectById(dID, true);
            Utils.fail("Must be deleted");
        } catch (JDOException e) {
            //expected
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Check that link tables are cleaned up on delete.
     */
    public void testDeleteLinkTable() throws Exception {
        if (!isSQLSupported())
        	return;

        if (isRemote()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ColNuke cn = new ColNuke();
        cn.getList().add(new Address("str0"));
        cn.getList().add(new Address("str1"));
        pm.makePersistent(cn);
        Object oid = pm.getObjectId(cn);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        cn = (ColNuke)pm.getObjectById(oid, true);
        cn.getList().add(new Address("str2"));
        cn.getList().add(new Address("str3"));
        pm.deletePersistent(cn);
        pm.currentTransaction().commit();
        pm.close();

        // make sure the link table is empty
        java.sql.Connection con = pmf().getJdbcConnection(null);
        Statement stat = con.createStatement();
        ResultSet rs = stat.executeQuery(
                "select count(*) from col_nuke_address");
        rs.next();
        if (rs.getInt(1) != 0) {
            throw new TestFailedException("Rows left in link table");
        }
        rs.close();
        stat.close();
        con.close();

        nuke(ColNuke.class);
        nuke(Address.class);
    }

    /**
     * Test that dependent reference in base class is nuked when subclass
     * instance is nuked [164].
     */
    public void testDependentRefInSuperclass() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Adherent a = new Adherent("piggy", new Address("oinker lane"), 10);
        pm.makePersistent(a);
        Object oid = pm.getObjectId(a);
        Object addrOid = pm.getObjectId(a.getAddress());
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        a = (Adherent)pm.getObjectById(oid, true);
        pm.deletePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        try {
            pm.getObjectById(addrOid, true);
            throw new TestFailedException("dependent address not nuked");
        } catch (JDOObjectNotFoundException e) {
            // good - it has been nuked
        }
        pm.close();

        nuke(Adherent.class);
    }

    /**
     * Check that removing an element from a dependent foreign key collection
     * results in the element being deleted [149]. The delete-orphans extension
     * must have been set.
     */
    public void testRemoveElementFromDependentFKCollection() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	System.out.println("\n*** Persisting Master with 2 Detail's");
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Master m = new Master();
        Detail d = new Detail(10);
        m.add(d);
        m.add(new Detail(20));
        pm.makePersistent(m);
        Object masterOid = pm.getObjectId(m);
        Object detailOid = pm.getObjectId(d);
        pm.currentTransaction().commit();
        pm.close();

        System.out.println("\n*** Removing 1 Detail from list");
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        m = (Master)pm.getObjectById(masterOid, true);
        d = (Detail)pm.getObjectById(detailOid, true);
        m.remove(d);
        pm.currentTransaction().commit();
        pm.close();

        System.out.println(
                "\n*** Checking that it is gone and the other is still there");
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        try {
            pm.getObjectById(detailOid, true);
            throw new TestFailedException("detail not deleted");
        } catch (JDOObjectNotFoundException e) {
            // good
        }
        m = (Master)pm.getObjectById(masterOid, true);
        List l = m.getDetails();
        Assert.assertEquals(l.size(), 1);
        Assert.assertEquals(((Detail)l.get(0)).getNum(), 20);
        pm.currentTransaction().commit();
        pm.close();

        nuke(Detail.class);
        nuke(Master.class);
    }

}
