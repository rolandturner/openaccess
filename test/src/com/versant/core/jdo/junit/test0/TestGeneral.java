
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

import com.versant.core.common.CmdBitSet;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test0.model.*;
import com.versant.core.jdo.junit.test0.model.cr.*;
import com.versant.core.jdo.junit.test0.model.jin.Department;
import com.versant.core.jdo.junit.test0.model.serbe.M2MBook;
import com.versant.core.jdo.junit.test0.model.serbe.M2MPerson;
import com.versant.core.jdo.junit.test0.model.jochen.ServiceClientList;
import com.versant.core.jdo.junit.test0.model.jochen.ServiceClient;
import com.versant.core.jdo.junit.test0.model.beta.TopObj;
import com.versant.core.jdo.junit.test0.model.beta.BottomObj;
import com.versant.core.jdo.junit.test0.model.beta2.BottomObj2;
import com.versant.core.jdo.junit.test0.model.beta2.TopObj2;
import com.versant.core.jdo.junit.test0.model.patrikb.Bank;
import com.versant.core.jdo.junit.test0.model.patrikb.Contact;
import com.versant.core.jdo.junit.test0.model.patrikb.Client;
import com.versant.core.jdo.junit.test0.model.sharedDiscr.Service;
import com.versant.core.jdo.junit.test0.model.sharedDiscr.Material;
import com.versant.core.jdo.junit.test0.model.robert1.OwnerMany;
import com.versant.core.jdo.junit.test0.model.robert1.NeedToImplement;
import com.versant.core.jdo.junit.test0.model.robert1.Implemented;
import com.versant.core.jdo.junit.test0.model.nortel.AbstractClass;
import com.versant.core.jdo.junit.test0.model.nortel.Node;
import com.versant.core.jdo.junit.test0.model.nortel.ValueT1;
import com.versant.core.jdo.junit.test0.model.nortel.MyInterface;
import com.versant.core.jdo.junit.test0.model.huyi.SystemPriviledge;
import com.versant.core.jdo.junit.test0.model.huyi.Role;
import com.versant.core.jdo.junit.test0.model.huyi.Account;
import com.versant.core.jdo.*;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.logging.LogEvent;
import com.versant.core.jdbc.conn.JDBCConnectionPool;
import com.versant.core.jdbc.conn.PooledConnection;
import com.versant.core.jdo.junit.TestFailedException;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.storagemanager.LRUStorageCache;

import javax.jdo.*;
import javax.jdo.spi.PersistenceCapable;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.math.BigDecimal;

public class TestGeneral extends VersantTestCase {

    public TestGeneral(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite("General");
        String[] a = new String[]{
            "testRollbackDeletedInstance",
            "testLargeResultFineGrainedTx",
            "testDeletes",
            "testDateArray",
            "testFgLoad",
            "testRollbackTransactionalObject",
            "testAddOneBookWithExistingPersons",
            "testNonAsciiStrings2",
            "testInListDeletes",
            "testInListDeletes2",
            "testInListDeletes3",
            "testInListDeletes4",
            "testMakePers",
            "testOIDEquals",
            "testUserJdbcConCloseOnNonTxUsage",
            "testGetObjectById3",
            "testNonTxRead",
            /* BAD "testSortOnList", */
            "testSCOFieldAfterCommitWithRetainValues",
            "testSCOFieldAfterCommitWithRetainValues1",
            "testScoOnManyPC",
            "testScoOnManyPC2",
            "testLocaleMap",
            "testListModel",
            "testMapModel2",
            "testOrderingOnThis1",
            /* BAD "testOrderingOnThis2", */
            "testCircularRef",
            "testCircularRef2",
            "testTransient",
            "testRefsInDFG",
            "testRefsInDFG2",
            "testQueryWithOpenJdbcConnection",
            "testClosePM",
            "testGetObjectById2",
            "testExceptionInQFlush",
            "testPrimDefaultValue",
            "testOidSerialization",
            "testOidSerialization2",
            "testOidSerialization3",
            "testOidSerialization4",
            "testOidSerialization5",
            "testOidSerialization6",
            "testColRead",
            "testSerialization",
            "testDeepGraphMakeTransient",
            "testGetObjectById",
            "testAppIdBla",
            "testCmdBitSet",
/*
            "testOuterJoinFetch",
            */
            "testDeepGraph",
            "testMakeDirty",
            "testMakeDirty2",
            "testMakeDirty3",
            "testClassIDAndIndexAPI",
            "testDefaultFieldRefs",
            "testGenCollectionTypes",
            "testOIDInDFGNull",
            "testOIDInDFGNonNull",
            "testTxField",
            "testRetrieve",
            "testInnerClass",
            "testPreStore1",
            /* BAD "testDependentMove", */
            "testGetConnectionDriverName",
            "testGetConnectionURL",
            "testInsertOrder",
            "testBla1",
            "testBla2",
            "testBla",
            "testFlush",
            "testFlush2",
            "testFlush3",
            "testFlush4",
            "testFlush5",
            "testFlush6",
            "testFlush7",
            /* BAD "testGC", */
            /* BAD "testAbstractSuper", */
            /* BAD "test007", */
            /* BAD "test007delete", */
            "testStatus",
            "testGetSupOpts",
            "testFieldRetrieval1",
            "testFieldRetrieval2",
            "testForConcurrentUpdate",
            "testForReplaceSCOFieldsWithRetainValues",
            "testForReplaceSCOFieldsWithRetainValuesOnInheritedScheme",
            "testRefList",
            "test2",
            "test1",
            "testIsLoaded",
            "testIsLoaded2",
            "testUpdateRefFieldToNull",
            "testSimpleUpdatesOfPersistentNewInstance",
            "testAutoSetFields",
            "testOptimistic1",
            /* BAD "testOptimistic2", */
            "testPersistenceByReachability",
            "testUpdateStateFromQueryWithRowVersion",
            "testGetObjectIdClass",
            "testnewObjectIdInstanceAppId",
            "testnewObjectIdInstanceDSId",
            "testCommitOnNoActiveTx",
            "testRollBackOnNoActiveTx",
            "testBeginOnActiveTx",
            /* BAD "testNavigateMissingRef", */
            /* BAD "testCreateEmptyConcrete", */
            "testComplexAppId",
            "testComplexAppId2",
            "testServerObject",
            "testCloning",
            "testOptLockingForSCOs",
            "testMakePersistentOnDupInstanceInPMCache",
            "testFieldRefEnhancer",
            "testLocalPMCacheCleanup",
            "testEmptyFG1",
            "testEmptyFG2",
            "testSharedDiscrColumn",
            "testJoinExpOrdering",
            "testOptimisticLockingValue",
            "testOptimisticReleaseCon",
            "testSecondPMF",
            "testQueryCount",
            "testNonTxReadWithDeleteCommit",
            "testNonTxReadWithDeleteCommit2",
            "testDetachAttachModfifyDetached"
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestGeneral(a[i]));
        }
        return s;
    }

    /**
     * OA-187.
     *
     * Instance marked for deletion was not reset on rollback.
     */
    public void testRollbackDeletedInstance() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Department dept = new Department("bla");
        pm.makePersistent(dept);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(dept);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Department.class);
        ((VersantQuery)q).setFetchGroup("fk");
        ((VersantQuery)q).setBounded(true);


        Collection col = (Collection) q.execute();
        dept = (Department)col.iterator().next();
        pm.deletePersistent(dept);
        System.out.println("\n\n BEFORE ROLLBACK");
        pm.currentTransaction().rollback();
        System.out.println("AFTER ROLLBACK\n\n\n");

        pm.currentTransaction().begin();
        pm.refresh(dept);
        dept.setName("bla");
        pm.currentTransaction().commit();
    }

    /**
     * oa-85
     */
    public void testLargeResultFineGrainedTx() {
        PersistenceManager pm = pmf().getPersistenceManager();
		pm.currentTransaction().begin();
		for (int i = 0; i < 10; i++) {
			BookLoan loan = new BookLoan();
			Date now = new Date();
			loan.setBookName("SomeBook"+now);
			loan.setBorrowerId(now.getTime()+i);
			loan.setDateDue(now);
			loan.setIsbn((new Long(now.getTime())).toString());
			loan.setStatus("borrowed");
			pm.makePersistent(loan);
		}
		pm.currentTransaction().commit();
		pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setNontransactionalRead(true);
        Query query = pm.newQuery(BookLoan.class, "status == 'borrowed'");
        ((VersantQuery) query).setFetchSize(1); // we only do this to break it *faster*, not to break it
        Collection loans = (Collection) query.execute(); // non-transactional read is enabled, so this is OK outside of a JDO transaction

        for (Iterator i = loans.iterator(); i.hasNext();) {
            BookLoan loan = (BookLoan) i.next();
            pm.currentTransaction().begin(); // we just want the transaction to wrap a single update, not the whole query processing
            if (loan.getDateDue().getTime() < (new Date()).getTime()) {
                loan.setStatus("overdue");
            }
            //	the commit here appears to set the query containing *all* results to status = com.versant.core.jdbc.JdbcQueryResult.STATUS_FINISHED, even though there are more query results to process
            pm.currentTransaction().commit();
        }

        query.closeAll();
        pm.close();
    }

    public void testDateArray() {
        PersistenceManager pm = pmf().getPersistenceManager();
        DataArrayHolder dah = new DataArrayHolder();
        pm.currentTransaction().begin();
        pm.makePersistent(dah);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(dah);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        dah = (DataArrayHolder) pm.getObjectById(id, true);
        System.out.println("dah.getDateArray() = " + dah.getDateArray());
        pm.close();

    }

    public void testNonTxReadWithDeleteCommit() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for(int i = 0; i < 20; i++) {
            Address a = new Address();
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();
        pm.currentTransaction().setNontransactionalRead(true);

        VersantQuery q = (VersantQuery) pm.newQuery(Address.class);
        q.setFetchSize(1);
        List result = (List) q.execute();
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Address a = (Address) iterator.next();
            pm.currentTransaction().begin();
            pm.deletePersistent(a);
            pm.currentTransaction().commit();
        }
        pm.close();
    }

    public void testNonTxReadWithDeleteCommit2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for(int i = 0; i < 20; i++) {
            Address a = new Address();
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();
        pm.currentTransaction().setNontransactionalRead(true);

        VersantQuery q = (VersantQuery) pm.newQuery(Address.class);
        q.setRandomAccess(true);
        q.setFetchSize(1);
        List result = (List) q.execute();
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Address a = (Address) iterator.next();
            pm.currentTransaction().begin();
            pm.deletePersistent(a);
            pm.currentTransaction().commit();
        }
    }

    public void testQueryCount() {
        if (isVds()) {
            unsupported();
            return;
        }

        int LAST_100_COUNT = 0;
        int NOT_SENT_COUNT = 0;
        int NOT_RECEIVED_COUNT = 0;
        int NOT_INVOICED_COUNT = 0;
        final int INVOICE_TYPE_COUNT = 4;

        int [] ret = new int [INVOICE_TYPE_COUNT];
        int recentListCount = 500;

        for (int i = 0; i < 3; i++) {
            PersistenceManager pm = pmf().getPersistenceManager();
            pm.currentTransaction().begin();
            //pm.setIgnoreCache(true);
            VersantQuery q = (VersantQuery)pm.newQuery(Person.class);
            q.setIgnoreCache(true);
            q.setFilter("");
            q.setResult("count(*)");
            System.out.println("\n\n\n\n 1 TestGeneral.testQueryCount");
            ret[LAST_100_COUNT] = ((Long)q.execute()).intValue();
            if(ret[LAST_100_COUNT] > recentListCount){
                ret[LAST_100_COUNT] = recentListCount;
            }
            //q.closeAll();

            q.setFilter("intField == 1");
            q.setResult("count(*)");
            System.out.println("\n\n\n\n 2 TestGeneral.testQueryCount");
            ret[NOT_SENT_COUNT] = ((Long)q.execute()).intValue();

            //q.closeAll();
            q.setFilter("name == \"bla\"");
            q.setResult("count(*)");
            System.out.println("\n\n\n\n 3 TestGeneral.testQueryCount");
            ret[NOT_RECEIVED_COUNT] = ((Long)q.execute()).intValue();

            // The query is changed to the REQUEST class for the count of NOT INVOICED requests
            q.setClass(Address.class);
            System.out.println("q.getIgnoreCache() = " + q.getIgnoreCache());
            q.setFilter("street == \"bla\"");
            q.setResult("count(*)");
            System.out.println("\n\n\n\n 4 TestGeneral.testQueryCount");
            ret[NOT_INVOICED_COUNT] = ((Long)q.execute()).intValue();
            q.closeAll();
            pm.close();
        }
    }

    public void testDeletes() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        CRPerson p1 = new CRPerson("Hamburger", null, null, new CRAddress(new CRCity("Hamburg"), "Sonnenweg"));
        CRPerson p2 = new CRPerson("Hamburger2", null, null, new CRAddress(new CRCity("Hamburg"), "Jungfernstieg"));
        CRPerson p3 = new CRPerson("Paderborner", null, null, new CRAddress(new CRCity("Paderborn"), "Bahnhofstrasse"));
        CRPerson p4 = new CRPerson("Soester", null, null, new CRAddress(new CRCity("Soest"), "Bahnhofstrasse"));

        CRProfessor e1 = new CRProfessor("Professor 1", null, null, null, 1, 0, 0);
        e1.addSubordinate(p1);
        p3.setSpouse(e1);
        CRProfessor e2 = new CRProfessor("Professor 2", null, null, null, 2, 20000, 0);
        e2.addSubordinate(p4);
        p4.setSpouse(e2);

        CRCourse c1 = new CRCourse("course 1");
        c1.setTeacher(e1);
        e1.addTeaches(c1);
        CRCourse c2 = new CRCourse("course 2");
        c2.setTeacher(e2);
        e2.addTeaches(c2);

        CRStudent s1 = new CRStudent("course 1 student", null, null, null, 0, 0, 0, 1);
        s1.setSpeciality(c1);
        e1.addSubordinate(s1);
        p1.setSpouse(s1);
        s1.setSpouse(p1);
        CRStudent s2 = new CRStudent("course 2 student", null, null, null, 0, 0, 0, 2);
        s2.setSpeciality(c2);
        e2.addSubordinate(s2);
        p2.setSpouse(s2);
        s2.setSpouse(p2);

//        StringCollectionType sc1 = new StringCollectionType();
//        sc1.Value = new System.Collections.Stack();
//        sc1.Value.Push("max");
//        sc1.Value.Push("franz");
//
//        StringCollectionType sc2 = new StringCollectionType();
//        sc2.Value = new System.Collections.Stack();
//        sc2.Value.Push("moritz");
//
//        Console.WriteLine("make persistent");

        pm.makePersistent(p1);
        pm.makePersistent(p2);
        pm.makePersistent(p3);
        pm.makePersistent(p4);
        pm.makePersistent(s1);
        pm.makePersistent(s2);
        pm.makePersistent(c1);
        pm.makePersistent(c2);
        pm.makePersistent(e1);
        pm.makePersistent(e2);

//        os.Add(sc1);
//        os.Add(sc2);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
//        Extent extent = pm.getExtent(CRCourse.class, false);
        VersantQuery q =(VersantQuery) pm.newQuery(CRCourse.class);
        q.setRandomAccess(true);
        for (Iterator iterator = ((Collection)q.execute()).iterator(); iterator.hasNext();) {
            CRCourse person = (CRCourse) iterator.next();
            pm.deletePersistent(person);
        }
        q =(VersantQuery) pm.newQuery(CRPerson.class);
        q.setFetchSize(1);
        q.setRandomAccess(true);
        for (Iterator iterator = ((Collection)q.execute()).iterator(); iterator.hasNext();) {
            CRPerson person = (CRPerson) iterator.next();
            pm.deletePersistent(person);
        }
        pm.currentTransaction().commit();
//                        java.util.Set allClasses =
//                            //Driver.getInstance().getDependentClasses(clazz);
//                            getDependentClasses(clazz);
//                        java.util.Iterator classIt = allClasses.iterator();
//                        while (classIt.hasNext())
//                        {
//                            object next = classIt.next();
//                            if (next is java.lang.Class)
//                                next = ((java.lang.Class)next).ToType();
//                            if (!(next is System.Type))
//                                continue;
//
//                            //comment("deleting dependent class " + next);
//                            Console.WriteLine("### deleting "+((System.Type)next).FullName);
//                            IExtent ext = Versant.Debug.ExtentHelper.GetExtent(os, (System.Type)next);
//                                    //GetExtent(os, (System.Type)next);
//
//                            // override default for immediateRetrieve, which is true
//                            // since 9.5
//                            // Extents.setImmediateRetrieve( iter, false );
//                            foreach (object obj in ext)
//                            {
//                                os.Remove(obj);
//                            }
//                        }
//                        t.Commit();
//                    }
//                    catch (java.lang.Throwable e)
//                    {
//                        if (t != null && t.IsActive)
//                        {
//                            t.Rollback();
//                        }
//                        Console.WriteLine(e);
//                    }
//                    catch (Exception e)
//                    {
//                        if (t != null && t.IsActive)
//                        {
//                            t.Rollback();
//                        }
//                        Console.WriteLine(e);
//                    }


    }

    public void testSetUserObject() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setUserObject("Bla");
        System.out.println("pm.getUserObject() = " + pm.getUserObject());
    }

    /**
     * Make sure we can get a second PMF if hyperdrive is enabled. This
     * makes sure that a new copy of the hyperdrive classes is loaded.
     */
    public void testSecondPMF() {
        if (!isHyperdrive() || !isRemote()) {
            unsupported();
            return;
        }
        Properties p = (Properties)getProperties().clone();
        // use dummy property to make sure we get a brand new PMF instance
        p.setProperty("boo", "" + System.currentTimeMillis());
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(p);
        pmf.close();
    }

    public void testAutoFkCollBackRefInReferenceFetchGroup() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ServiceClientList serviceClientList = new ServiceClientList();
        ServiceClient serviceClient = new ServiceClient();
        serviceClientList.setServiceClients(new ArrayList());
        serviceClientList.getServiceClients().add(serviceClient);
        pm.makePersistent(serviceClientList);

        ((VersantPersistenceManager)pm).flush(true);

        pm.retrieve(serviceClient);
        pm.currentTransaction().commit();
    }

    public void testEagerColFetch() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Department dept = new Department("dept1");
        for (int i = 0; i < 3; i++) {
            com.versant.core.jdo.junit.test0.model.jin.Manager man =
                    new com.versant.core.jdo.junit.test0.model.jin.Manager("man1-" + i, dept);
            dept.getManagers().add(man);
        }
        pm.makePersistent(dept);

        dept = new Department("dept2");
        pm.makePersistent(dept);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Department.class);
        List result = (List) q.execute();
        assertEquals(2, result.size());
        pm.close();
    }

    public void testFgLoad() {
        int depth = 2;
        String fetchGroup = "list1_list2";
//        fetchGroup = "_jdoref";
        createContacts(2);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Contact2 root = lookupContact("contact-0", null, pm);
        if (fetchGroup != null)
            ((com.versant.core.jdo.VersantPersistenceManager) pm)
                    .loadFetchGroup(root, fetchGroup);
        checkLevel(root, 0, depth);
        pm.currentTransaction().rollback();
    }

    /**
     * Create the product catalog.
     */
    private void createContacts(int depth) {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Contact2 contactRoot = new Contact2("contact-0");
        System.out.println("Create " + contactRoot.getName());
        createChildren(contactRoot, 1, depth);
        pm.makePersistent(contactRoot);
        pm.currentTransaction().commit();
        pm.close();
    }

    private void createChildren(Contact2 contact, int level, int depth) {
        Contact2 contact1 = new Contact2(contact.getName() + "-1");
        Contact2 contact2 = new Contact2(contact.getName() + "-2");
        Contact2 contact3 = new Contact2(contact.getName() + "-3");

        java.util.ArrayList li = new java.util.ArrayList();
        li.add(contact1);
        contact.setList1(li);
//        System.out.println("Add1 " + contact1.getName() +
//                " Father: " + contact.getName());

        li = new java.util.ArrayList();
        li.add(contact2);
        contact.setList2(li);
//        System.out.println("Add2 " + contact2.getName() +
//                " Father: " + contact.getName());

        li = new java.util.ArrayList();
        li.add(contact3);
        contact.setList3(li);
//        System.out.println("Add3 " + contact3.getName() +
//                " Father: " + contact.getName());

        if (level < depth) {
            createChildren(contact1, level + 1, depth);
            createChildren(contact2, level + 1, depth);
            createChildren(contact3, level + 1, depth);
        }
    }

    private Contact2 lookupContact(String nameStart, String fg, PersistenceManager pm) {
        Query q = pm.newQuery(Contact2.class);
        q.declareParameters("String ns");
        q.setFilter("name == ns");
        if (fg != null) {
            ((VersantQuery)q).setFetchGroup(fg);
        }
        Collection ans = null;
        try {
            ans = (Collection) q.execute(nameStart);
            System.out.println("Lookup Contact " + nameStart);
            Iterator i = ans.iterator();
            Contact2 next = null;

            while (i.hasNext()) {
                next = (Contact2) i.next();
            }
            return next;
        } finally {
            if (ans != null) q.close(ans);
        }
    }

    private void checkLevel(Contact2 obj, int level, int depth) {
        System.out.println("---------- check " + obj.getName() + " at level "
                + level + " ----------");
        checkList(obj.getList1(), level, depth);
        checkList(obj.getList2(), level, depth);
        checkList(obj.getList3(), level, depth);
    }

    private void checkList(java.util.List li, int level, int depth) {
        if (level < depth) {
            if (li == null) {
                throw new RuntimeException("Error: list should not be null");
            } else if (li.size() == 0) {
                throw new RuntimeException("Error: list should not be empty");
            } else {
                checkLevel((Contact2) li.get(0), level + 1, depth);
            }
        } else if (li != null && li.size() != 0) {
            throw new RuntimeException("Error: list should be null or empty");
        }
    }

    public void testAttachOnTransient() {
        PersistenceManager pm = pmf().getPersistenceManager();
        Address a = new Address();
        Collection col = new ArrayList();
        col.add(a);

        pm.currentTransaction().begin();
        ((VersantPersistenceManager)pm).versantAttachCopy(col, true);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDetachFG() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 3; i++) {
            Person p = new Person();
            p.setName("bla-" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery query = (VersantQuery)pm.newQuery(Person.class);
        query.setFetchGroup("fgOrderAll");
        query.setBounded(true);
        query.setRandomAccess(false);

// Execute the query.
        System.out.println("+++ query.execute...");
        Collection result =  (Collection) query.execute();

        Collection detachedResult = null;
        System.out.println("\n\n\n\n\n+++ detach...");
        detachedResult = ((VersantPersistenceManager)pm).versantDetachCopy(result, "fgOrderAll");

        System.out.println("\n\n\n\n\n\n+++ rollback...");
        pm.currentTransaction().rollback();
        pm.close();
        System.out.println("+++ END detachAll...");
    }

    public void testOptimisticReleaseCon() {
        if (isRemote()) {
            unsupported();
            return;
        }
        ((LRUStorageCache)getLevel2Cache()).setEnabled(false);
        try {
            PersistenceManager pm = pmf().getPersistenceManager();
            pm.currentTransaction().begin();
            Address a1 = new Address("a1");
            pm.makePersistent(a1);
            Object oid = pm.getObjectId(a1);
            pm.currentTransaction().commit();
            pm.close();

            PersistenceManager pm1 = pmf().getPersistenceManager();
            pm1.currentTransaction().setOptimistic(true);
            PersistenceManager pm2 = pmf().getPersistenceManager();
            pm2.currentTransaction().setOptimistic(true);

            pm1.currentTransaction().begin();
            pm2.currentTransaction().begin();
            System.out.println("%%% get a1");
            a1 = (Address)pm1.getObjectById(oid, true);
            System.out.println("%%% get a2");
            Address a2 = (Address)pm2.getObjectById(oid, true);
            System.out.println("%%% a1.setCity(\"City1\")");
            a1.setCity("City1");
            System.out.println("%%% a2.setCity(\"City1\")");
            a2.setCity("City2");
            System.out.println("%%% pm1 commit");
            pm1.currentTransaction().commit();
            try {
                System.out.println("%%% pm2 commit");
                pm2.currentTransaction().commit();
                assertTrue("expected JDOOptimisticVerificationException", false);
            } catch (JDOOptimisticVerificationException e) {
                System.out.println(e);
                // good
            }
            pm1.close();
            pm2.close();
        } finally {
            ((LRUStorageCache)getLevel2Cache()).setEnabled(true);
        }
    }

    public void testRollbackTransactionalObject() {
        PersistenceManager pm = pmf().getPersistenceManager();
//        pm.currentTransaction().setRestoreValues(true);
//        pm.currentTransaction().setNontransactionalWrite(true);
//        pm.currentTransaction().setNontransactionalRead(true);

        pm.currentTransaction().begin();
        Gene aGene = new Gene();
        pm.makePersistent(aGene);
        pm.currentTransaction().commit();

        //test change mGeneCount outside a transaction:
        int original = 42;
        aGene.mGeneCount = original;
        aGene.mGeneCount = -3;
        aGene.mGeneCount = original;

//        aGene.setmGeneCount(original);
//        aGene.setmGeneCount(-3);
//        aGene.setmGeneCount(original);

        //open transaction:
        pm.currentTransaction().begin();

        //change mGeneCount and rollback:
        int changed = 24;
        aGene.setmGeneCount(changed);
//        aGene.setmGeneCount(changed);

        pm.currentTransaction().rollback();

        //Verify mGeneCount rollback to 42. It should not be 24.
        System.out.println("\t geneCount original= " +
                original + " changed = " + changed + " rollback = "
                + aGene.mGeneCount);
        assertTrue(aGene.mGeneCount == original);
        assertFalse(aGene.mGeneCount == changed);
        pm.close();
    }

    public void testAttachDetach1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        SystemPriviledge priv = SystemPriviledge.UPLOAD;
        Role role = Role.newInstance("Manager" + System.currentTimeMillis());
        role.addPriviledge(priv);
        Account newAccount = Account.newInstance("John","12345");
        newAccount.addRole(role);
        // JDOSupport.getPM().makePersistent(role); // uncommet this line make things works
        pm.makePersistent(newAccount);
        pm.currentTransaction().commit();

        Collection col = new ArrayList();
        col.add(role);

        List detached = (List) ((VersantPersistenceManager)pm).versantDetachCopy(col, "fg");
        System.out.println("((Role)detached.get(0)).getRoleName(); = " + ((Role) detached.get(0)).getRoleName());
        pm.close();

    }
    
    /**
     * Testcase for 15-3593/106-1148/oa-162
     *
     * Test if it is possible to set/get any fields of detached instances 
     * after an attach has been done
     *  
     */
    public void testDetachAttachModfifyDetached() {
        PersistenceManager pm = pmf().getPersistenceManager();
        
        // create test objects and detach 
        pm.currentTransaction().begin();

        Address address = new Address("Schlossallee");
        Person person = new Person("Fritz" ,address,new Date());

        pm.makePersistent(person);
        
        Collection col = new ArrayList();
        col.add(person);
        List detachedList = (List) ((VersantPersistenceManager)pm).versantDetachCopy(col, "");
        
        pm.currentTransaction().commit();

        // attach objects and test if field of detached root object and field of referenced object could be still set
        pm.currentTransaction().begin();
            
       ((VersantPersistenceManager)pm).versantAttachCopy(detachedList,true);
   
        Person detachedPerson = (Person)detachedList.get(0);
    
        detachedPerson.setName("Walter");
        detachedPerson.getAddress().setStreet("Parkallee");
        
        assertEquals("Walter",detachedPerson.getName());
        assertEquals("Parkallee",detachedPerson.getAddress().getStreet());
            
        pm.currentTransaction().rollback();
   
        pm.close();

    }

    public void testNonAsciiStrings2() {
        broken();
        if (true) return;
        
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        TextHolder th = new TextHolder();
        th.setText("Observa??o:O conte?do da Wired News ? acess?vel em todas as vers?es de");
        th.setVarcharText("Observa??o:O conte?do da Wired News ? acess?vel em todas as vers?es de");
        pm.makePersistent(th);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(th);

        pm.currentTransaction().begin();
        th = (TextHolder) pm.getObjectById(id, false);
        assertEquals("Observa??o:O conte?do da Wired News ? acess?vel em todas as vers?es de", th.getText());
        assertEquals("Observa??o:O conte?do da Wired News ? acess?vel em todas as vers?es de", th.getVarcharText());
        pm.close();
    }

    /**
     * This must delete using a in list.
     */
    public void testInListDeletes() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Company comp = null;
        for (int j = 0; j < 10; j++) {
            comp = new Company();
            col.add(comp);
            for (int i = 0; i < 10; i++) {
                Employee1 emp = new Employee1();
                emp.setAge(i);
                comp.addEmployees(emp);
            }
            pm.makePersistent(comp);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();

    }

    /**
     * TODO: write comp pk test
     */
    public void testInListDeletes2() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Company comp = null;
        for (int j = 0; j < 11; j++) {
            comp = new Company();
            col.add(comp);
            for (int i = 0; i < 10; i++) {
                Employee1 emp = new Employee1();
                emp.setAge(i);
                comp.addEmployees(emp);
            }
            pm.makePersistent(comp);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
    }

    public void testInListDeletes3() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        if (getDbName().equals("oracle")) {
            broken();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        ComplexAppId comp = null;
        for (int j = 0; j < 11; j++) {
            comp = new ComplexAppId();
            comp.setbID(true);
            comp.setcID(true);
            comp.setlID(new Locale("ZA" + j, "", ""));
            comp.setsID("bla" + j);
            col.add(comp);
            pm.makePersistent(comp);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
    }

    public void testInListDeletes4() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        if (getDbName().equals("oracle")) {
            broken();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        ComplexAppId comp = null;
        long ct = System.currentTimeMillis();
        for (int j = 0; j < 10; j++) {
            comp = new ComplexAppId();
            comp.setbID(true);
            comp.setcID(true);
            comp.setlID(new Locale("ZA" + j, "", ""));
            comp.setsID("bla" + j);
            col.add(comp);
            pm.makePersistent(comp);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
    }

    /**
     * This is a test that shows a bug with managed collections.
     * The List in M2MBook is lazily created on the call to 'addPerson'.
     * This happens after makePersistent and therefore the list is not a SCO
     * and therefore no management takes place.
     */
    public void testAddOneBookWithExistingPersons() {
        if (true) {
            broken();
            return;
        }

		VersantPersistenceManager pm = (VersantPersistenceManager) pmf().getPersistenceManager();
		pm.currentTransaction().begin();
        M2MPerson p1 = new M2MPerson();
        pm.makePersistent(p1);

        M2MPerson p2 = new M2MPerson();
        pm.makePersistent(p2);
        pm.currentTransaction().commit();


        pm.currentTransaction().begin();
        // Create a new book
		M2MBook book = new M2MBook();
		book.setName(M2MBook.getDefaultName4());
        pm.makePersistent(book);

		// Add persons to book
		book.addPerson(p1);
		book.addPerson(p2);

//		// Insert book
		Collection newItems = new ArrayList();
		newItems.add(book);
		newItems = pm.versantAttachCopy(newItems, true);
		pm.currentTransaction().commit();

		// The book is stored, retrieve it from the database and detach it
		newItems = pm.versantDetachCopy(newItems, "WITH_PERSONS");

		// Add the new item to the original collection
//		colBooks.addAll(newItems);
		pm.close();
	}

    /**
     * NB. This test should work without having to add persist-after metadata.
     */
    public void testAutoOneToMany() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ServiceClientList scList = new ServiceClientList();
        pm.makePersistent(scList); // delegates to pm.makePersistent
        scList.createInitialClientList();
        System.out.println("pm.getObjectId(scList) = " + pm.getObjectId(scList));

        ServiceClient serviceclient = new ServiceClient();
        pm.makePersistent(serviceclient);
        scList.getServiceClients().add(serviceclient);
        scList.setDefaultServiceClient(serviceclient);
        pm.currentTransaction().commit();

    }

    public void testOptimisticLockingValue() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address a = new Address();
        pm.makePersistent(a);
        System.out.println("1 ((VersantPersistenceManager)pm).getOptimisticLockingValue(a) = " + ((VersantPersistenceManager) pm).getOptimisticLockingValue(a));
        pm.currentTransaction().commit();

        try {
            System.out.println("2 ((VersantPersistenceManager)pm).getOptimisticLockingValue(a) = " + ((VersantPersistenceManager) pm).getOptimisticLockingValue(a));
            fail("Expected Exception wrt nontx read");
        } catch (Exception e) {
            //ignore
        }
        pm.currentTransaction().setNontransactionalRead(true);
        System.out.println("3 ((VersantPersistenceManager)pm).getOptimisticLockingValue(a) = " + ((VersantPersistenceManager) pm).getOptimisticLockingValue(a));
        pm.currentTransaction().begin();
        System.out.println("4 ((VersantPersistenceManager)pm).getOptimisticLockingValue(a) = " + ((VersantPersistenceManager) pm).getOptimisticLockingValue(a));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testJoinExpOrdering() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        
        if (getDbName().equals("mssql")) {
            //the driver seems broken
            broken();
            return;
        }
        
        PersistenceManager pm = pmf().getPersistenceManager();
        ((VersantPersistenceManager)pm).setCheckModelConsistencyOnCommit(false);
        pm.currentTransaction().begin();
        Bank kb = new Bank("KB", "0100", new Contact("street", 1, "Prague", 12345, "CZ"));
        Bank csob = new Bank("CSOB", "0200", new Contact("csob street", 99, "Prague", 31313, "CZ"));

        Client johnDoe = new Client(new com.versant.core.jdo.junit.test0.model.patrikb.Person("John", "Doe", new Contact("str", 123, "Munich", 13234, "Germany")),
                kb);
        Client janeDoe = new Client(new com.versant.core.jdo.junit.test0.model.patrikb.Person("Jane", "Doe", new Contact("str", 123, "Munich", 13234, "Germany")),
                kb);
        Client gandalfGrey = new Client(new com.versant.core.jdo.junit.test0.model.patrikb.Person("Gandalf", "Grey", new Contact("ulica", 999, "Blava", 90909, "Slovakia")),
                csob);

        com.versant.core.jdo.junit.test0.model.patrikb.Account account = new com.versant.core.jdo.junit.test0.model.patrikb.Account(johnDoe, 0, 123, johnDoe.getBank());
        johnDoe.getAccounts().add(account);

        com.versant.core.jdo.junit.test0.model.patrikb.Account account2 = new com.versant.core.jdo.junit.test0.model.patrikb.Account(janeDoe, 0, 234, janeDoe.getBank());
        janeDoe.getAccounts().add(account2);

        for (int i = 0; i < 10; i++) {
            com.versant.core.jdo.junit.test0.model.patrikb.Transaction tx = new com.versant.core.jdo.junit.test0.model.patrikb.Transaction(new Date(), account, account2, new BigDecimal(i + 5000));
            pm.makePersistent(tx);
        }

        kb.getEmployees().add(new com.versant.core.jdo.junit.test0.model.patrikb.Employee(new com.versant.core.jdo.junit.test0.model.patrikb.Person("Jim", "Beam", new Contact("under the bridge", 1, "London", 12345, "GB")),
                kb));
        kb.getEmployees().add(new com.versant.core.jdo.junit.test0.model.patrikb.Employee(johnDoe.getPerson(), kb));

        System.out.println("Persisting new banks with clients/accounts/transactions");
        pm.makePersistent(kb);
        pm.makePersistent(csob);

        ((VersantPersistenceManager) pm).flush();
        for (Iterator it = pm.getExtent(Bank.class, false).iterator(); it.hasNext();) {
            Bank b = (Bank) it.next();
            System.out.println("Bank " + b.getName() + "has " + b.getAccounts().size() + " accounts\n");
//            System.out.println("Bank %s has %d accounts\n", b.getName(), b.getAccounts().size());
        }
        pm.currentTransaction().commit();



        pm.currentTransaction().begin();
        VersantQuery query = (VersantQuery)pm.newQuery(com.versant.core.jdo.junit.test0.model.patrikb.Transaction.class);
        query.setFetchGroup("clientTransactions");
        query.setFilter("payerAccount.owner.person.firstName == 'John'");
        Collection col = (Collection)query.execute();
        System.out.println("\n\n\n\n col.size() = " + col.size());
        for (Iterator iter = col.iterator(); iter.hasNext();) {
            com.versant.core.jdo.junit.test0.model.patrikb.Transaction transaction = (com.versant.core.jdo.junit.test0.model.patrikb.Transaction)iter.next();
            System.out.println(transaction);
        }
        pm.currentTransaction().commit();
    }

    public void testSharedDiscrColumn() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Service service = new Service();
        long now = System.currentTimeMillis();
        service.setId(new Long(now));
        pm.makePersistent(service);
        Material material = new Material();
        material.setId(new Long(now + 1));
        pm.makePersistent(material);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testTransientTransactional() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().setRestoreValues(true);

        pm.currentTransaction().begin();
        TopObj o = new TopObj("str");
        o.setOne( new BottomObj( "value ") );
        o.setComeBack("original");
        pm.makePersistent(o);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Extent e = pm.getExtent(TopObj.class, false);
        Iterator i = e.iterator();
        TopObj ob = (TopObj)i.next();
        System.out.println("TopObj value: "  + ob.getOne().getValue());
        System.out.println("TopObj original transient value: "  + ob.getComeBack());
        ob.setComeBack("changed");
        System.out.println( "TopObj original transient value: "  + ob.getComeBack() );
        pm.currentTransaction().rollback();
        assertEquals("original", ob.getComeBack());
        pm.close();
    }

    public void testTransientTx2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRestoreValues(true);

        pm.currentTransaction().begin();

		TopObj2 o = new TopObj2();
		o.setOne(new BottomObj2( "original"));
		o.setComeBack("original");
		pm.makePersistent(o);
		pm.currentTransaction().commit();

		pm.currentTransaction().begin();
		Extent e = pm.getExtent(TopObj2.class, false);
		Iterator i = e.iterator();
		TopObj2 ob = (TopObj2)i.next();
		System.out.println( "TopObj bottom value: "  + ob.getOne().getValue());
		System.out.println( "TopObj original transient bottom value: "  + ob.getOne().getValue() );
		ob.setOne(new BottomObj2("changed Value"));
        ob.setComeBack("bla");
		System.out.println( "TopObj changed transient bottom value: "  + ob.getOne().getValue() );
		pm.currentTransaction().rollback();

		System.out.println("TopObj after rollback transient bottom value: "  + ob.getOne().getValue() );

		pm.close();
    }

    public void testNPEWithVdsUntypedOid() {
        if (!isVds()) {
            unsupported();
            return;
        }
        
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        OwnerMany obj = new OwnerMany("OWNERMANY1");
        pm.makePersistent(obj);
        for (int i = 0; i < 10; i++) {
            NeedToImplement imp = new Implemented("IMPLEMENTED" + i);
            obj.addNeedToImplement(imp);
        }
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        System.out.println("Beginning transaction to read the NEEDTOIMPLEMENT");

        Extent ext = pm.getExtent(OwnerMany.class, false);
        Iterator it = ext.iterator();
        while (it.hasNext()) {
            OwnerMany o = (OwnerMany) it.next();
            Iterator implemented = o.getOfNeedToImplement().iterator();
            while (implemented.hasNext()) {
                NeedToImplement need = (NeedToImplement) implemented.next();
                need.thisMethod();
            }
        }
        pm.close();
    }

    public void testEmptyFG1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        BaseClass bc = new BaseClass("b");
        pm.makePersistent(bc);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery vq = (VersantQuery) pm.newQuery(BaseClass.class);
        vq.setFetchGroup("emptyFG");
        List results = (List) vq.execute();
        assertEquals(1, results.size());
        bc = (BaseClass) results.get(0);
        pm.close();
    }

    public void testEmptyFG2() {
        if (!isApplicationIdentitySupported())
            return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        AppIDAbstract1 apId = new AppIDConcrete1();
        apId.setAppIdConcKey(11111);
        pm.makePersistent(apId);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery vq = (VersantQuery) pm.newQuery(AppIDAbstract1.class);
        vq.setFetchGroup("emptyFG");
        List result = (List) vq.execute();
        assertTrue(result.contains(apId));

        vq = (VersantQuery) pm.newQuery(AppIDConcrete1.class);
        vq.setFetchGroup("emptyFG");
        result = (List) vq.execute();
        assertTrue(result.contains(apId));
        pm.close();
    }

    public void testNortel1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        createOneNode("A", pm);
        createOneNode("B", pm);
        createOneNode("C", pm);
        createOneNode("D", pm);

        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Extent e = pm.getExtent(Node.class, false);
        Iterator it = e.iterator();
        while (it.hasNext()) {
            Node n = (Node) it.next();
            MyInterface values = n.getValues();
            if (values != null) {
                Object oid = pm.getObjectId(values);
                System.out.println(oid);
            }
        }

        pm.currentTransaction().rollback();
    }

    private void createOneNode(String name, PersistenceManager pm) {
        Node node = new Node(name);
        pm.makePersistent(node);
        try {
            Class randomClassType = ValueT1.class;
            AbstractClass type = (AbstractClass) randomClassType.newInstance();
            pm.makePersistent(type);
            node.setValues(type);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Make sure the local PM cache is properly cleaned up as things are GCed.
     */
    public void testLocalPMCacheCleanup() {
        PMProxy pm = (PMProxy)pmf().getPersistenceManager();
        pm.setPmCacheRefType(VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK);

        // create some addresses and make sure the local cache is empty
        // shortly after commit
        pm.currentTransaction().begin();
        int n = 10;
        for (int i = 0; i < n; i++) {
            pm.makePersistent(new Address("addr" + i));
        }
        pm.currentTransaction().commit();

        // this relies on gc behaviour so is a bit dodgy - giving things a
        // long time to get sorted out helps
        int i;
        for (i = 0; i < 10; i++) {
            System.gc();
            System.gc();

            // commit of a tx processes the reference queue
            pm.currentTransaction().begin();
            pm.currentTransaction().commit();

            if (pm.getRealPM().getCache().size() == 0) break;

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        assertTrue(i < 10);

        pm.close();
    }

    public void testMakePers() {
        if (!isApplicationIdentitySupported()) {
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SystemPriviledge priv = SystemPriviledge.UPLOAD;
        Role role = Role.newInstance("Manager");
        role.addPriviledge(priv);
        Account newAccount = Account.newInstance("John","12345");
        newAccount.addRole(role);
        // JDOSupport.getPM().makePersistent(role); // uncommet this line make things works
        pm.makePersistent(newAccount);
        pm.currentTransaction().commit();
    }

    public void testFieldRefEnhancer() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        BaseClass base = new BaseClass("base");
        DerivedClass d1 = new DerivedClass("derived", true);
        pm.makePersistent(d1);
        d1.thirdObj = base;
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Object obj = pm.getObjectId(d1);
        DerivedClass d = (DerivedClass) pm.getObjectById(obj, true);
        Assert.assertNotNull(d.thirdObj);
        pm.currentTransaction().commit();
        pm.close();
    }



    public void testOIDEquals() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person person = new Person("a");
        pm.makePersistent(person);

        Object oid1=pm.getObjectId(person);
        Object oid2=pm.getObjectId(person);
        Assert.assertTrue(oid1.equals(oid2));

        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test calling makePersistent on a transient instance of an app identity
     * class with the same primary key as an instance already in the local
     * cache. This should result in an exception on makePersistent with a
     * good error message.
     */
    public void testMakePersistentOnDupInstanceInPMCache() throws Exception {
        if (!isApplicationIdentitySupported()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        SimpleAP sap = new SimpleAP();
        sap.setIdNo(1000);
        pm.makePersistent(sap);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        SimpleAP sap2 = new SimpleAP();
        sap2.setIdNo(1000);
        try {
            pm.makePersistent(sap2);
        } catch (JDOFatalUserException e) {
            System.out.println("Good: " + e);
        }

        pm.close();
    }


    /**
     * Test optimistic locking for multiple updates of an instance referencing
     * a non-embedded collection where some of the updates modify the
     * collection and some do not.
     */
    public void testOptLockingForSCOs() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);

        pm.currentTransaction().begin();
        Person p = new Person("a");
        Person p2 = new Person("b");
        p.addPersonToSet(p2);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.setName(p.getName() + "x");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getPersonsSet().remove(p2);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testCloning() throws CloneNotSupportedException {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        ClassA classA = null;


        for (int i = 0; i < 10; i++) {
            classA = new ClassAC();
            ((ClassAC) classA).setStringAC("stringAC" + i);
            pm.makePersistent(classA);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Extent ex = pm.getExtent(ClassA.class, true);
        Iterator iter = ex.iterator();

        while (iter.hasNext()) {
            ClassAC orig = (ClassAC) iter.next();
            ClassAC clone = (ClassAC) orig.clone();

            Assert.assertNull(((PersistenceCapable)clone).jdoGetObjectId());
            Assert.assertNotNull(((PersistenceCapable) orig).jdoGetObjectId());
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testUserJdbcConCloseOnNonTxUsage() throws Exception {
        if (!isJdbc()) // JDBC specific
        	return;

        if (isRemote()) return;
        if (isDataSource()) return;
        
        PooledConnection jdbcCon = (PooledConnection)pmf().getJdbcConnection(
                null);
        JDBCConnectionPool pool = jdbcCon.getPool();
        Assert.assertEquals(1, pool.getActiveCount());
        jdbcCon.close();
        Assert.assertEquals(0, pool.getActiveCount());
    }

    public void testGetObjectById3() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String name = "name" + System.currentTimeMillis();
        Person p = new Person(name);
        pm.makePersistent(p);
        Object id = pm.getObjectId(p);
        pm.currentTransaction().commit();

        pmf().evictAll();
        pm.currentTransaction().begin();
        Person p2 = (Person)pm.getObjectById(id, true);
        Assert.assertEquals(name, p2.getName());
        pm.close();
    }

    public void testNonTxRead() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes nm = new NonMutableJavaTypes();
        String val = "" + System.currentTimeMillis();
        nm.setStringValue(val);
        pm.makePersistent(nm);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(nm);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().begin();
        if (pm.currentTransaction().getOptimistic()) {
            Utils.fail("Must be ds tx");
        }
        nm = (NonMutableJavaTypes)pm.getObjectById(id, true);
        nm.getIntValue();
        System.out.println(
                "\n\n\n**********************************************************");
        ((VersantPersistenceManager)pm).flush();
        System.out.println(
                "**********************************************************\n\n\n");
//        nm.setIntValue(3);
        nm.setBooleanValue(true);
        nm.getDoubleValue();
        pm.currentTransaction().commit();

        Assert.assertTrue(nm.isBooleanValue());
//        Assert.assertEquals(3, nm.getIntValue());
        pm.close();

    }

    public void testNonTxRead2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes nm = new NonMutableJavaTypes();
        String val = "" + System.currentTimeMillis();
        nm.setStringValue(val);
        pm.makePersistent(nm);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(nm);
        pm.close();

        PersistenceManager pm1 = pmf().getPersistenceManager();
        pm1.currentTransaction().setNontransactionalRead(true);
        NonMutableJavaTypes nm1 = (NonMutableJavaTypes)pm.getObjectById(id,
                true);

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().begin();
        if (pm.currentTransaction().getOptimistic()) {
            Utils.fail("Must be ds tx");
        }
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setFilter("stringValue == p");
        q.declareParameters("String p");
        List results = (List)q.execute(val);
        nm = (NonMutableJavaTypes)results.get(0);
        nm.setIntValue(3);
        nm.setBooleanValue(true);
        pm.currentTransaction().commit();

        Assert.assertTrue(nm.isBooleanValue());
        Assert.assertEquals(3, nm.getIntValue());
        pm.close();

    }

    public void testSCOFieldAfterCommitWithRetainValues() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String name = "" + System.currentTimeMillis();
        Person p = new Person(name);
        Person refP = new Person();
        p.getOrderedRefList().add(refP);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, false);
        p.setIntField(3);

        Query q = pm.newQuery(Person.class);
        q.setFilter("name == param");
        q.declareParameters("String param");
        Person p2 = (Person)((List)q.execute(name)).get(0);
        p2.getName();
        Assert.assertTrue(p.getOrderedRefList() != null);
        Assert.assertEquals(1, p.getOrderedRefList().size());

        //do clean-up
        pm.deletePersistentAll(p.getOrderedRefList());
        pm.deletePersistent(p);
        pm.currentTransaction().commit();
        pm.setIgnoreCache(true);
        pm.close();
    }

    public void testSCOFieldAfterCommitWithRetainValues1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String name = "" + System.currentTimeMillis();
        Person p = new Person(name);
        Person refP = new Person();
        p.getOrderedRefList().add(refP);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, false);
        p.setIntField(3);

        Query q = pm.newQuery(Person.class);
        q.setFilter("name == param");
        q.declareParameters("String param");
        Person p2 = (Person)((List)q.execute(name)).get(0);
        p2.getName();
        Assert.assertTrue(p.getOrderedRefList() != null);
        Assert.assertEquals(1, p.getOrderedRefList().size());

        //do clean-up
        pm.deletePersistentAll(p.getOrderedRefList());
        pm.deletePersistent(p);
        pm.currentTransaction().commit();
        pm.setIgnoreCache(true);
        pm.close();
    }

    public void testSortOnList() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Master master = new Master();
        Detail d1 = new Detail(1);
        Detail d2 = new Detail(2);
        Detail d3 = new Detail(3);
        master.add(d1);
        master.add(d2);
        master.add(d3);
        pm.makePersistent(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, master.getDetailSize());
        Collections.sort(master.getDetails());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, master.getDetailSize());
        Collections.sort(master.getDetails(), Collections.reverseOrder());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, master.getDetailSize());
        Collections.sort(master.getDetails());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, master.getDetailSize());
        Collections.sort(master.getDetails());
        d2.setBla("22");
        master.setBla("44");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, master.getDetailSize());
        Collections.sort(master.getDetails(), Collections.reverseOrder());
        d2.setBla("23");
        master.setBla("45");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, master.getDetailSize());
        Collections.sort(master.getDetails(), Collections.reverseOrder());
        d2.setBla("24");
        master.setBla("46");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, master.getDetailSize());
        Collections.sort(master.getDetails(), Collections.reverseOrder());
        d2.setBla("25");
        master.setBla("47");
        pm.currentTransaction().commit();
    }

    public void testScoOnManyPC() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.setBirthDate(new Date());
        pm.makePersistent(p);
        Person p2 = new Person();
        p2.setBirthDate(p.getBirthDate());
        pm.makePersistent(p2);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testScoOnManyPC2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        Person p = new Person();
        p.setBirthDate(new Date());
        pm.makePersistent(p);
        Person p2 = new Person();
        pm.makePersistent(p2);
        p2.setBirthDate(p.getBirthDate());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testLocaleMap() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MapModel mm = new MapModel();
        mm.getLocaleMap().put("key1", new Locale("de", "DE"));
        pm.makePersistent(mm);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(MapModel.class);
        q.declareImports("import java.util.*;");
        q.declareParameters("Locale pLocale");
        q.setFilter("localeMap.contains(pLocale)");
        List results = (List)q.execute(new Locale("de", "DE"));
        Assert.assertEquals(1, results.size());

        pm.deletePersistent(mm);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testListModel() throws Exception {
    	if (!isOderingOnThisSupported())
    		return;
    	
        nuke(ListModel.class);
        nuke(PCCollectionEntry.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ListModel lm = null;
        int amount1 = 3;
        int amount2 = 2;
        for (int j = 0; j < amount1; j++) {
            lm = new ListModel();
            lm.setVal("5");
            for (int i = 0; i < amount2; i++) {
                lm.getUnOrderedList().add(new PCCollectionEntry(i));
            }
            pm.makePersistent(lm);
        }
        pm.currentTransaction().commit();

        long time1 = doLmQuery(pm, amount1, amount2);
        long time2 = doLmQuery2(pm, amount1, amount2);
        long time3 = doLmQuery3(pm, amount1, amount2, "5");
        long time4 = doLmQuery4(pm, amount1, amount2, "5");
        System.out.println("time 1 = " + time1);
        System.out.println("time 2 = " + time2);
        System.out.println("time 3 = " + time3);
        System.out.println("time 4 = " + time4);

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(ListModel.class);
        List result = (List)q.execute();
        for (int i = 0; i < result.size(); i++) {
            ListModel listModel = (ListModel)result.get(i);
            pm.deletePersistentAll(listModel.getUnOrderedList());
        }
        pm.deletePersistentAll(result);
        pm.currentTransaction().commit();

        pm.close();
    }

    private long doLmQuery(PersistenceManager pm, int amount1, int amount2) {
        long time = System.currentTimeMillis();
        ListModel lm;
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ListModel.class);
//        q.setFilter("!unOrderedList.isEmpty()");
        q.setOrdering("this descending");
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testLM");
        ((VersantQuery)q).setFetchSize(1);
        List result = (List)q.execute();
        lm = (ListModel)result.get(0);
        countExecQueryEvents();
        Assert.assertEquals(amount2, lm.getUnOrderedList().size());
        Assert.assertEquals(0, countExecQueryEvents());

        lm = (ListModel)result.get(1);
        countExecQueryEvents();
        Assert.assertEquals(amount2, lm.getUnOrderedList().size());
        time = (System.currentTimeMillis() - time);
        Assert.assertEquals(0, countExecQueryEvents());
        pm.currentTransaction().commit();
        return time;
    }

    private long doLmQuery3(PersistenceManager pm, int amount1, int amount2,
            String val) {
        long time = System.currentTimeMillis();
        ListModel lm;
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ListModel.class);
//        q.setFilter("!unOrderedList.isEmpty()");
        q.setFilter("val == pVal");
        q.declareParameters("String pVal");
        q.setOrdering("this descending");
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testLM");
        ((VersantQuery)q).setFetchSize(amount1);
        List result = (List)q.execute(val);
        lm = (ListModel)result.get(0);
        countExecQueryEvents();
        Assert.assertEquals(amount2, lm.getUnOrderedList().size());
        Assert.assertEquals(0, countExecQueryEvents());

        lm = (ListModel)result.get(1);
        countExecQueryEvents();
        Assert.assertEquals(amount2, lm.getUnOrderedList().size());
        time = (System.currentTimeMillis() - time);
        Assert.assertEquals(0, countExecQueryEvents());
        pm.currentTransaction().commit();
        return time;
    }

    private long doLmQuery4(PersistenceManager pm, int amount1, int amount2,
            String val) {
        pmf().evictAll();
        long time = System.currentTimeMillis();
        ListModel lm;
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ListModel.class);
        q.setFilter("!unOrderedList.isEmpty() && val == pVal");
        q.declareParameters("String pVal");
        q.setOrdering("this descending");
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testLM");
        ((VersantQuery)q).setFetchSize(amount1);
        List result = (List)q.execute(val);
        lm = (ListModel)result.get(0);
        countExecQueryEvents();
        Assert.assertEquals(amount2, lm.getUnOrderedList().size());
        Assert.assertEquals(0, countExecQueryEvents());

        lm = (ListModel)result.get(1);
        countExecQueryEvents();
        Assert.assertEquals(amount2, lm.getUnOrderedList().size());
        time = (System.currentTimeMillis() - time);
        Assert.assertEquals(0, countExecQueryEvents());
        pm.currentTransaction().commit();
        return time;
    }

    private long doLmQuery2(PersistenceManager pm, int amount1, int amount2) {
        long time = System.currentTimeMillis();
        ListModel lm;
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ListModel.class);
//        q.setFilter("!unOrderedList.isEmpty()");
        q.setOrdering("this descending");
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testLM");
        ((VersantQuery)q).setFetchSize(amount1);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n BEFORE QUERY");
        List result = (List)q.execute();
        Assert.assertEquals(amount1, result.size());
        lm = (ListModel)result.get(0);
        countExecQueryEvents();
        Assert.assertEquals(amount2, lm.getUnOrderedList().size());
        if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());

        lm = (ListModel)result.get(1);
        Assert.assertEquals(amount2, lm.getUnOrderedList().size());
        if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());

        time = (System.currentTimeMillis() - time);
        pm.currentTransaction().commit();
        return time;
    }

    public void testMapModel2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        int amount = 10;

        pm.currentTransaction().begin();
        MapModel mapModel = null;
        for (int i = 0; i < amount; i++) {
            mapModel = new MapModel();
            Person mk1 = new Person("mk1");
            Person mv1 = new Person("mv1");
            Person mk2 = new Person("mk2");
            Person mv2 = new Person("mv2");
            mapModel.getRefRefMap().put(mk1, mv1);
            mapModel.getRefRefMap().put(mk2, mv2);
            pm.makePersistent(mapModel);
        }
        pm.currentTransaction().commit();

        timeQ1(pm, amount, true);
        timeQ1(pm, amount, true);
        timeQ1(pm, amount, true);
        long time = timeQ1(pm, amount, true);
        Thread.sleep(100);
        System.out.println("\n\n\n\n AFTER TIME 1");

        timeQ1(pm, amount, false);
        timeQ1(pm, amount, false);
        timeQ1(pm, amount, false);
        long time1 = timeQ1(pm, amount, false);
        Thread.sleep(100);
        System.out.println("\n\n\n\n AFTER TIME 2");

        timeQ1(pm, amount, true);
        timeQ1(pm, amount, true);
        timeQ1(pm, amount, true);
        long time2 = timeQ1(pm, amount, true);
        Thread.sleep(100);
        System.out.println("\n\n\n\n AFTER TIME 2");

//        timeQ2(pm, amount);
//        timeQ2(pm, amount);
//        timeQ2(pm, amount);
//        long time1 = timeQ2(pm, amount);
//        Thread.sleep(100);
//        System.out.println("\n\n\n\n AFTER TIME 2");

        pm.currentTransaction().begin();
        Query q = pm.newQuery(MapModel.class);
        System.out.println("\n\n\n\n\n\n\nBEFORE DELETE");
        Collection col = (List)q.execute();
        int nn = 0;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            iterator.next();
            nn++;
        }
        if (col.size() != nn) {
            System.out.println("nn = " + nn);
            System.out.println("col.size() = " + col.size());
            fail();
        }

        pm.deletePersistentAll((List)q.execute());
        pm.currentTransaction().commit();
        pm.close();

        System.out.println("time  = " + time);
        System.out.println("time1 = " + time1);
        System.out.println("time2 = " + time2);
    }

    private long timeQ2(PersistenceManager pm, int amount) {
        MapModel mapModel;
        pm.currentTransaction().begin();
        Query q = pm.newQuery(MapModel.class);
        ((VersantQuery)q).setBounded(false);
        List results = (List)q.execute();
        long time1 = System.currentTimeMillis();
        Assert.assertEquals(amount, results.size());
        mapModel = (MapModel)results.get(0);
        countExecQueryEvents();
        Assert.assertEquals(2, mapModel.getRefRefMap().size());
        Assert.assertEquals(0, countExecQueryEvents());
        time1 = (System.currentTimeMillis() - time1);
        pm.currentTransaction().commit();
        return time1;
    }

    private long timeQ1(PersistenceManager pm, int amount, boolean option) {
        MapModel mapModel;
        pm.currentTransaction().begin();
        Query q = pm.newQuery(MapModel.class);
        ((VersantQuery)q).setBounded(option);
        List results = (List)q.execute();
        long time = System.currentTimeMillis();
        Assert.assertEquals(amount, results.size());
        mapModel = (MapModel)results.get(0);
//        countExecQueryEvents();
        Assert.assertEquals(2, mapModel.getRefRefMap().size());
//        Assert.assertEquals(0, countExecQueryEvents());
        time = (System.currentTimeMillis() - time);
        pm.currentTransaction().commit();
        return time;
    }

    public void testOrderingOnThis1() {
    	if (!isOderingOnThisSupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ComplexAppId.class);
        q.setOrdering("this ascending");
        List l = (List)q.execute();
        System.out.println("l = " + l.size());
        pm.close();
    }

//    public void testOrderingOnThis2() {
//        PersistenceManager pm = pmf().getPersistenceManager();
//        pm.currentTransaction().begin();
//        Query q = pm.newQuery(ComplexAppId.class);
//        q.setOrdering("this.sID ascending");
//        List l = (List) q.execute();
//        System.out.println("l = " + l.size());
//        pm.close();
//    }


    public void testCircularRef() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Person p = new Person("root");
        p.setPerson(p);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);

        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, true);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(p);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testCircularRef2() {
    	if (!isSQLSupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Person p = new Person("root");
        p.setPerson(p);
        p.getRefCol().add(p);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);

        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, true);
        countExecQueryEvents();
        p.getRefCol().size();
        Assert.assertEquals(0, countExecQueryEvents());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(p);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Check that the userObject and serverUserObject properties on JDOGeniePMF
     * work.
     */
    public void testServerObject() throws Exception {
        pmf().setUserObject("oink");
        Assert.assertEquals("oink", pmf().getUserObject());

        if (isRemote()) {
            pmf().setServerUserObject("piggy");
            Assert.assertEquals("oink", pmf().getUserObject());
            Assert.assertEquals("piggy", pmf().getServerUserObject());
        } else {
            pmf().setServerUserObject("piggy");
            Assert.assertEquals("piggy", pmf().getUserObject());
            Assert.assertEquals("piggy", pmf().getServerUserObject());
        }
    }

    public void testTransient() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address a = new Address();
        pm.makeTransactional(a);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testRefsInDFG() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Person p = new Person("p" + System.currentTimeMillis());
        Employee emp = new Employee("emp", "1");
        p.setEmployee(emp);
        Employee zemp = new Employee("zemp", "2");
        p.setZmployee(zemp);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        emp.getName();
        p.getName();
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testRefsInDFG2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Person p = new Person("p" + System.currentTimeMillis());
        Address a = new Address("street");
        p.setAddress(a);
        Employee emp = new Employee("emp", "1");
        p.setEmployee(emp);
        Employee zemp = new Employee("zemp", "2");
        p.setZmployee(zemp);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        a.getStreet();
        emp.getName();
        p.getName();
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testQueryWithOpenJdbcConnection() {
        if (!isJdbc()) // JDBC specific
        	return;
        
        if (isRemote()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.setIgnoreCache(false);
        pm.currentTransaction().setNontransactionalRead(true);

        pm.currentTransaction().begin();
        java.sql.Connection con = ((VersantPersistenceManager)pm).getJdbcConnection(
                null);
        Person p = new Person();
        Address a = new Address();
        p.setAddress(a);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        a.setStreet("bla");
        pm.currentTransaction().commit();
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        pmf().evictAll();

        Query q = pm.newQuery(Address.class);
        q.declareParameters("String c");
        q.setFilter("street == c");
        Collection ans = null;
        Address a2 = null;
        try {
            ans = (Collection)q.execute("bla");
            Iterator i = ans.iterator();
            if (i.hasNext()) {
                a2 = (Address)i.next();
            } else {
                Utils.fail("Expected result but found none");
            }
        } finally {
            if (ans != null) q.close(ans);
        }

        pm.close();
    }

    public void testClosePM() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        pm.close();
        if (!pm.isClosed()) {
            Utils.fail(
                    "The pm has been closed but it reports to be still open");
        }
    }

    public void testGetObjectById2() {
        if (!isApplicationIdentitySupported()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        AppIdString a = new AppIdString();
        a.setId("22");
        pm.makePersistent(a);

        String idString = a.getId();
        Object id = pm.getObjectId(a);
        System.out.println("id = " + id);
        AppIdString a2 = (AppIdString)pm.getObjectById(id, false);
        pm.close();
    }

    public void testExceptionInQFlush() {
        if (!isApplicationIdentitySupported()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        AppIdString a = new AppIdString();
        a.setId("22");
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        a = new AppIdString("22", "toFail");
        Query q = pm.newQuery(AppIdString.class);
        ((List)q.execute()).size();

        pm.close();

    }

    public void testPrimDefaultValue() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(3);
        n.setBooleanValue(true);
        pm.makePersistent(n);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        n.isBooleanValue();
        n.getIntValue();
        n.setIntValue(0);
        n.setBooleanValue(false);
        Assert.assertEquals(0, n.getIntValue());
        Assert.assertEquals(false, n.isBooleanValue());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(false, n.isBooleanValue());
        Assert.assertEquals(0, n.getIntValue());
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * This is a test for a bug that occured when running 'remote'.
     * The issue was that oids that was serialised was always 'resolved'. A scenario of such
     * a case is as follows.
     * A ref field with a oid that was prefetched through 'oid's in dfg' mechanism gets navigated.
     * This ref field must be a subclass of another pc class. After nav it will be found that the
     * field would then be of the base class and not of the correct sub-class.
     */
    public void testOidSerialization() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("root");

        TempEmployee tEmp = new TempEmployee();
        tEmp.setName("friend");
        p.setEmployee(tEmp);

        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pmf().evictAll();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, false);
        //read a dfg field to load the dfg fields and also the
        //ref fields oids.
        p.getName();
        Employee emp = p.getEmployee();
        Assert.assertEquals("friend", emp.getName());
        Assert.assertTrue(emp.getClass().equals(TempEmployee.class));

        pm.deletePersistent(p.getEmployee());
        pm.deletePersistent(p);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOidSerialization2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("root");

        TempEmployee tEmp = new TempEmployee();
        tEmp.setName("friend");
        p.setEmployee(tEmp);

        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pmf().evictAll();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, false);
        //read a dfg field to load the dfg fields and also the
        //ref fields oids.
        p.getName();
        Employee emp = p.getEmployee();
        Assert.assertEquals("friend", emp.getName());
        Assert.assertTrue(emp.getClass().equals(TempEmployee.class));

        pm.deletePersistent(p.getEmployee());
        pm.deletePersistent(p);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOidSerialization3() {
        String name = "root" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person(name);

        TempEmployee tEmp = new TempEmployee();
        tEmp.setName("friend");
        p.setEmployee(tEmp);

        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pmf().evictAll();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setFilter("name == p");
        q.declareParameters("String p");
        List l = (List)q.execute(name);
        Assert.assertEquals(1, l.size());

        p = (Person)l.get(0);
        //read a dfg field to load the dfg fields and also the
        //ref fields oids.
        p.getName();
        Employee emp = p.getEmployee();
        Assert.assertEquals("friend", emp.getName());
        Assert.assertTrue(emp.getClass().equals(TempEmployee.class));

        pm.deletePersistent(p.getEmployee());
        pm.deletePersistent(p);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOidSerialization4() {
        String name = "root" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person(name);

        TempEmployee tEmp = new TempEmployee();
        tEmp.setName("friend");
        p.setEmployee(tEmp);

        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

//        pmf().evictAll();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setFilter("name == p");
        q.declareParameters("String p");
        List l = (List)q.execute(name);
        Assert.assertEquals(1, l.size());

        p = (Person)l.get(0);
        //read a dfg field to load the dfg fields and also the
        //ref fields oids.
        p.getName();
        Employee emp = p.getEmployee();
        Assert.assertEquals("friend", emp.getName());
        Assert.assertTrue(emp.getClass().equals(TempEmployee.class));

        pm.deletePersistent(p.getEmployee());
        pm.deletePersistent(p);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOidSerialization5() {
        String name = "root" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person(name);

        TempEmployee tEmp = new TempEmployee();
        tEmp.setName("friend");
        p.setEmployee(tEmp);

        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pmf().evictAll();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setFilter("name == p");
        q.declareParameters("String p");
        List l = (List)q.execute(name);
        Assert.assertEquals(1, l.size());

        p = (Person)l.get(0);
        //read a dfg field to load the dfg fields and also the
        //ref fields oids.
        p.getName();
        p.setIntField(10);
        Employee emp = p.getEmployee();
        Assert.assertEquals("friend", emp.getName());
        Assert.assertTrue(emp.getClass().equals(TempEmployee.class));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getName();
        emp = p.getEmployee();
        Assert.assertEquals("friend", emp.getName());
        Assert.assertTrue(emp.getClass().equals(TempEmployee.class));

        pm.deletePersistent(p.getEmployee());
        pm.deletePersistent(p);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOidSerialization6() {
        String name = "root" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person(name);

        TempEmployee tEmp = new TempEmployee();
        tEmp.setName("friend");
        p.setEmployee(tEmp);

        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pmf().evictAll();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, false);
        p.setIntField(10);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Employee emp = p.getEmployee();
        Assert.assertEquals("friend", emp.getName());
        Assert.assertTrue(emp.getClass().equals(TempEmployee.class));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getName();
        emp = p.getEmployee();
        Assert.assertEquals("friend", emp.getName());
        Assert.assertTrue(emp.getClass().equals(TempEmployee.class));

        pm.deletePersistent(p.getEmployee());
        pm.deletePersistent(p);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testColRead() throws Exception {
        if (!getSubStoreInfo().isScrollableResultSetSupported()) return;

        nuke(TestA.class);
        nuke(TestB.class);
        pmf().evictAll();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        TestA testA = new TestA();
        testA.addToList(new TestB(1));
        pm.makePersistent(testA);
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(TestA.class);
        q.setRandomAccess(true);
        List result = (List)q.execute();
        testA = (TestA)result.get(0);

        testA.addToList(new TestB(2));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testSerialization() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        DeepGraph root = new DeepGraph(4);
        DeepGraph next = new DeepGraph(5);
        root.setNext(next);
        pm.makePersistent(root);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(root);

        pm.currentTransaction().begin();
        pm.makeTransient(root.getNext());
        pm.makeTransient(root);
        pm.close();

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(root);

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        pm.deletePersistent(pm.getObjectById(id, false));
        pm.currentTransaction().commit();
        pm.close();

    }

    public void testGetObjectById() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        pm.makePersistent(p);
        Object id = pm.getObjectId(p);
        Person p2 = (Person)pm.getObjectById(id, true);
        Assert.assertTrue(p == p2);
        pm.close();
    }

    public void testAppIdBla() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        nuke(SimpleAP_KeyGen.class);
        List created = new ArrayList();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SimpleAP_KeyGen s = new SimpleAP_KeyGen();
        created.add(s);
        s.setIdNo(44);
        s.setName("44");
        pm.makePersistent(s);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(44, ((SimpleAP_PK_KeyGen)pm.getObjectId(s)).idNo);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        s = new SimpleAP_KeyGen();
        created.add(s);
        pm.makePersistent(s);
        s = new SimpleAP_KeyGen();
        created.add(s);
        pm.makePersistent(s);
        s = new SimpleAP_KeyGen();
        created.add(s);
        pm.makePersistent(s);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testBitSetPerf() {
        VersantPersistenceManagerImp pm = ((PMProxy)pmf().getPersistenceManager()).getRealPM();
        ModelMetaData jmd = pm.modelMetaData;
        ClassMetaData[] cmds = jmd.classes;
        int[] bitSet = new int[(jmd.classes.length / 32) + 1];
        CmdBitSet cmdBitSet = new CmdBitSet(pm.modelMetaData);

        for (int i = 0; i < 10; i++) {
            System.out.println("time = " + doTime(cmds, bitSet, 10000));
        }

        System.out.println("\n\n\n");
        for (int i = 0; i < 10; i++) {
            System.out.println("time = " + doTime3(cmds, cmdBitSet, 10000));
        }
        pm.close();
    }

    private long doTime(ClassMetaData[] cmds, int[] bitSet, int amount) {
        long start = System.currentTimeMillis();
        for (int j = 0; j < amount; j++) {
            for (int i = 0; i < cmds.length; i++) {
//                if ((bitSet[(cmds[i].index / 32)] & (1 << (cmds[i].index % 32))) == 0) {
                bitSet[(cmds[i].index / 32)] |= (1 << (cmds[i].index % 32));
                ClassMetaData[] subs = cmds[i].pcSubclasses;
                if (subs != null) {
                    for (int k = subs.length - 1; k >= 0; k--) {
//                            if ((bitSet[(sub.index / 32)] & (1 << (sub.index % 32))) == 0) {
                        bitSet[(subs[k].index / 32)] |= (1 << (subs[k].index % 32));
//                                bitSet[(subs[k].index / 32)] = (bitSet[(subs[k].index / 32)] | (1 << (subs[k].index % 32)));
//                            }
                    }
                }
//                }
            }

            //build the cmd[]
            ClassMetaData[] result = new ClassMetaData[cmds.length];
            for (int i = 0; i < bitSet.length; i++) {
                int val = bitSet[i];
                for (int bit = 0; bit < 32; bit++) {
                    if ((val & (1 << bit)) != 0) {
                        result[bit + (i * 32)] = cmds[bit + (i * 32)];
                    }
                }
            }

//            for (int i = 0; i < result.length; i++) {
//                ClassMetaData classMetaData = result[i];
//                System.out.println("classMetaData.index = " + classMetaData.index);
//            }

            //clear the bitset
            for (int i = bitSet.length - 1; i >= 0; i--) {
                bitSet[i] = 0;
            }
        }
        return System.currentTimeMillis() - start;
    }

    private long doTime3(ClassMetaData[] cmds, CmdBitSet bitSet, int amount) {
        long start = System.currentTimeMillis();
        for (int j = 0; j < amount; j++) {
            for (int i = 0; i < cmds.length; i++) {
                bitSet.addPlus(cmds[i]);
            }
            ClassMetaData[] result = bitSet.toArrayIfCacheble();
            bitSet.clear();
        }
        return System.currentTimeMillis() - start;
    }

    public void testCmdBitSet() {
        VersantPersistenceManagerImp pm = ((PMProxy)pmf().getPersistenceManager()).getRealPM();
        ModelMetaData jmd = pm.modelMetaData;
        ClassMetaData[] cmds = jmd.classes;
        CmdBitSet cmdBitSet = new CmdBitSet(pm.modelMetaData);

        for (int i = 0; i < cmds.length; i++) {
            cmdBitSet.addPlus(cmds[i]);
        }
        ClassMetaData[] result = cmdBitSet.toArray();
        Assert.assertEquals(cmds.length, result.length);
        System.out.println("\n\n>>>>> result = " + result);
        cmdBitSet.clear();

        cmds = new ClassMetaData[]{
            pm.modelMetaData.getClassMetaData(Person.class),
            pm.modelMetaData.getClassMetaData(Address.class),
        };
        for (int i = 0; i < cmds.length; i++) {
            cmdBitSet.addPlus(cmds[i]);
        }
        result = cmdBitSet.toArray();
        Assert.assertEquals(cmds.length, result.length);
        System.out.println("\n\n>>>>> result = " + result);
        cmdBitSet.clear();

        cmds = new ClassMetaData[]{
            pm.modelMetaData.getClassMetaData(EmpSuper.class),
            pm.modelMetaData.getClassMetaData(Address.class),
        };
        for (int i = 0; i < cmds.length; i++) {
            cmdBitSet.addPlus(cmds[i]);
        }
        result = cmdBitSet.toArray();
        for (int i = 0; i < result.length; i++) {
            ClassMetaData classMetaData = result[i];
            System.out.println("classMetaData = " + classMetaData);
        }
        Assert.assertEquals(4, result.length);
        System.out.println("\n\n>>>>> result = " + result);
        cmdBitSet.clear();
        pm.close();
    }

    /**
     * Check that a reference in the default fetch group is fetched in the
     * same query as its referrer without any extra queries.
     */
    public void testOuterJoinFetch() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        DeepGraph root = new DeepGraph(100);
        root.setNext(new DeepGraph(101));
        root.getNext().setNext(new DeepGraph(102));
        pm.makePersistent(root);
        pm.currentTransaction().commit();

        // throw away perf events so far
        LogEvent[] a = pmf().getNewPerfEvents(0);
        int lastId = a[a.length - 1].getId();

        pm.currentTransaction().begin();
        // touch all fields in root and root.next - this should generate only 2 queries
        System.out.println("*** root.getAge() = " + root.getAge());
        System.out.println(
                "*** root.getNext().getAge() = " + root.getNext().getAge());
        System.out.println(
                "*** root.getNext().getNext() = " + root.getNext().getNext());
        System.out.println(
                "*** root.getNext().getNext().getAge() = " + root.getNext().getNext().getAge());

        System.out.println("\n*** about to commit");
        pm.currentTransaction().commit();
        System.out.println("*** after commit\n");

        // check that there are only two execQuery events
        a = pmf().getNewPerfEvents(lastId);
        Assert.assertEquals(2, countExecQueryEvents(a));
        lastId = a[a.length - 1].getId();

        // now nuke the graph
        pm.currentTransaction().begin();
        System.out.println("\n*** before pm.deletePersistent(root)");
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        // check that there are only two execQuery events
        a = pmf().getNewPerfEvents(lastId);
        Assert.assertEquals(2, countExecQueryEvents(a));

        pm.close();
    }

    /**
     * Check that we can persist a very deep graph without running out of
     * stack space. This is only run on MySQL as the other databases are
     * too slow and the test takes ages.
     */
    public void testDeepGraph() {
        if (!getDbName().equals("mysql") || isRemote()) {
            unsupported();
            return;
        }
        broken();

        //  TODO put this test back when we have figured out the wierd error
        /*
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        DeepGraph p = new DeepGraph(0);
        DeepGraph root = p;
        for (int i = 1; i < 10000; i++) {
            p.setNext(new DeepGraph(i));
            p = p.getNext();
        }
        pm.makePersistent(root);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
        */
    }

    public void testDeepGraphMakeTransient() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        DeepGraph p = new DeepGraph(0);
        DeepGraph root = p;
        for (int i = 1; i < 100; i++) {
            p.setAge(i);
            p.setNext(new DeepGraph(i));
            p = p.getNext();
        }
        pm.makePersistent(root);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(root);

        //after commit the fields should not be loaded and hence a makeTransient
        //must not include the next field.
        pm.currentTransaction().begin();
        ((VersantPersistenceManager)pm).makeTransientRecursive(root);
        Assert.assertNull(root.getNext());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        root = (DeepGraph)pm.getObjectById(id, false);
        root.getNext();
        ((VersantPersistenceManager)pm).makeTransientRecursive(root);
        Assert.assertNotNull(root.getNext());
        Assert.assertEquals(1, root.getAge());

        Assert.assertNull(root.getNext().getNext());
        Assert.assertEquals(2, root.getNext().getAge());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        root = (DeepGraph)pm.getObjectById(id, false);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testMakeDirty() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address a = new Address();
        a.setStreet("s");
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        JDOHelper.makeDirty(a, "street");
        Assert.assertEquals("s", a.getStreet());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        a.setStreet("s1");
        JDOHelper.makeDirty(a, "street");
        Assert.assertEquals("s1", a.getStreet());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testMakeDirty2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.setName("p1");
        Address a = new Address();
        p.setAddress(a);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        JDOHelper.makeDirty(p, "address");
        Assert.assertEquals(a, p.getAddress());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Address a2 = new Address();
        a2.setStreet("street2");
        p.setAddress(a2);
        JDOHelper.makeDirty(p, "address");
        Assert.assertEquals(a2, p.getAddress());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testMakeDirty3() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        Person p = new Person();
        p.setName("p1");
        Address a = new Address();
        p.setAddress(a);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        JDOHelper.makeDirty(p, "address");
        Assert.assertEquals(a, p.getAddress());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Address a2 = new Address();
        a2.setStreet("street2");
        p.setAddress(a2);
        JDOHelper.makeDirty(p, "address");
        Assert.assertEquals(a2, p.getAddress());
        pm.currentTransaction().rollback();

        pm.currentTransaction().begin();
        Assert.assertEquals(a, p.getAddress());
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Make sure the classid and index APIs on JDOGeniePMF work.
     */
    public void testClassIDAndIndexAPI() {
    	if (!isJdbc())
    		return;
    		
        // check classid
        int classid = pmf().getClassID(Address.class);
        Class cls = pmf().getClassForID(classid);
        Assert.assertEquals(Address.class, cls);

        // check class index
        int index = pmf().getClassIndex(Person.class);
        cls = pmf().getClassForIndex(index);
        Assert.assertEquals(Person.class, cls);

        // jdbc-class-id APIs not supported for remote
        if (isRemote()) return;

        // check jdbc-class-id
        Object jdbcClassId = pmf().getJdbcClassID(Person.class);
        if (jdbcClassId == null) {
            return;
        }
        cls = pmf().getClassForJdbcID(Person.class, jdbcClassId);
        Assert.assertEquals(Person.class, cls);
    }

    public void testDefaultFieldRefs() {
        VersantPersistenceManagerImp pm = ((PMProxy)pmf().getPersistenceManager()).getRealPM();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.setAddress(new Address());
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pm = ((PMProxy)pmf().getPersistenceManager()).getRealPM();
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, false);
        p.getName();

        ClassMetaData cmd = pm.modelMetaData.getClassMetaData(Person.class);
        FieldMetaData fmd = cmd.getFieldMetaData("person");
        PCStateMan sm = pm.getInternalSM((PersistenceCapable)p);

        if (sm.isLoadedImp(fmd.managedFieldNo)) {
            throw new JDOFatalInternalException("This must not be loaded");
        }
        pm.close();

    }

    public void testGenCollectionTypes() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();

        Object[] ps = new Object[4];
        ps[0] = new Person("p0");
        ps[1] = new Person("p1");
        ps[2] = new Person("p2");
        ps[3] = new Person("p3");

        p.setPersonsList(Arrays.asList(ps));
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testComplexAppId() {
        if (!isApplicationIdentitySupported()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ComplexAppId appId = new ComplexAppId();
        appId.setbID(true);
        appId.setcID(true);
        appId.setlID(new Locale("fr", "FR", ""));
        appId.setsID("BLABLA");
        appId.setName("Carl");

        ComplexAppId appId2 = new ComplexAppId();
        appId2.setbID(true);
        appId2.setcID(true);
        appId2.setlID(new Locale("fr", "FR", ""));
        appId2.setsID("BLABLA1");
        appId2.setName("Jaco");
        appId2.setCom(appId);

        ComplexAppId appId3 = new ComplexAppId();
        appId3.setbID(true);
        appId3.setcID(true);
        appId3.setlID(new Locale("fr", "FR", ""));
        appId3.setsID("BLABLA2");
        appId3.setName("Dave");
        appId3.setCom(appId);
        pm.makePersistent(appId);
        pm.makePersistent(appId2);
        pm.makePersistent(appId3);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testComplexAppId2() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        nuke(ComplexAppId.class);

        if (getSubStoreInfo().getDataStoreType().equals("daffodil")) return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        ComplexAppId appId = new ComplexAppId();
        appId.setbID(true);
        appId.setcID(true);
        appId.setlID(new Locale("fr", "FR", ""));
        appId.setsID("BLABLA3");
        appId.setName("Carl");

        ComplexAppId appId2 = new ComplexAppId();
        appId2.setbID(true);
        appId2.setcID(true);
        appId2.setlID(new Locale("fr", "FR", ""));
        appId2.setsID("BLABLA4");
        appId2.setName("Jaco");
        appId2.setCom(appId);

        ComplexAppId appId3 = new ComplexAppId();
        appId3.setbID(true);
        appId3.setcID(true);
        appId3.setlID(new Locale("fr", "FR", ""));
        appId3.setsID("BLABLA5");
        appId3.setName("Dave");
        appId3.setCom(appId);
        pm.makePersistent(appId);
        pm.makePersistent(appId2);
        pm.makePersistent(appId3);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        appId3.setName("bla");
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOIDInDFGNonNull() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("p1");
        Address a = new Address();
        a.setStreet("s1");
        p.setAddress(a);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pmf().evictAll();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, true);
        a = p.getAddress();
        if (a == null) {
            throw new JDOFatalInternalException("the address must be filled");
        }
        pm.close();
    }

    public void testOIDInDFGNull() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("p1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pmf().evictAll();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, true);
        Address a = p.getAddress();
        VersantPersistenceManagerImp realPM = ((PMProxy)pm).getRealPM();
        PCStateMan sm = realPM.getInternalSM((PersistenceCapable)p);
        ClassMetaData cmd = realPM.modelMetaData.getClassMetaData(Person.class);
        if (!sm.isLoadedImp(cmd.getFieldMetaData("address").managedFieldNo)) {
            throw new JDOFatalInternalException("the field must be loaded now");
        }
        pm.close();
    }

    public void testCommitOnNoActiveTx() {
        PersistenceManager pm = pmf().getPersistenceManager();
        try {
            pm.currentTransaction().commit();
        } catch (JDOUserException e) {
            //cool
        }
        pm.close();
    }

    public void testRollBackOnNoActiveTx() {
        PersistenceManager pm = pmf().getPersistenceManager();
        try {
            pm.currentTransaction().rollback();
        } catch (JDOUserException e) {
            //cool
        }
        pm.close();
    }

    public void testBeginOnActiveTx() {
        PersistenceManager pm = pmf().getPersistenceManager();
        try {
            pm.currentTransaction().begin();
            pm.currentTransaction().begin();
        } catch (JDOUserException e) {
            //cool
        }
        pm.close();
    }

    public void testTxField() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.setTxAddress(new Address());
        p.setAddress(new Address());
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(p.getAddress());
        pm.deletePersistent(p);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testRetrieve() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        TempEmployee tempEmployee = new TempEmployee();
        tempEmployee.setDate(new Date());
        pm.makePersistent(tempEmployee);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(tempEmployee);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        TempEmployee emp = (TempEmployee)pm.getObjectById(id, false);
        pm.retrieve(emp);
        pm.makeTransient(emp);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testPrimCollectoin() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        GuiStuff gui = new GuiStuff();
        pm.makePersistent(gui);
        for (int i = 0; i < 20; i++) {
            gui.getPrim().add(new Integer(i));
        }
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(gui);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        gui = (GuiStuff) pm.getObjectById(id, false);
        int count = 0;
        for (Iterator iterator = gui.getPrim().iterator(); iterator.hasNext();) {
            Integer integer = (Integer) iterator.next();
            assertEquals(count++, integer.intValue());
        }
        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testGuiStuff() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        GuiStuff gui = new GuiStuff();
        pm.makePersistent(gui);
        Person3 per1 = new Person3();
        per1.setName("Carl");
        Person p1 = new Person("bla1");
        Person p2 = new Person("bla2");
        Person p3 = new Person("bla3");
        Person p4 = new Person("bla4");
        Person p5 = new Person("bla5");
        Person p6 = new Person("bla6");
        Person p7 = new Person("bla7");
        pm.makePersistent(p1);
        pm.makePersistent(p2);
        pm.makePersistent(p3);
        pm.makePersistent(p4);
        pm.makePersistent(p5);
        pm.makePersistent(p6);
        pm.makePersistent(p7);

        per1.addFriends(p1);
        per1.addFriends(p2);
        per1.addFriends(p3);
        per1.addFriends(p4);
        Person3 per2 = new Person3();
        per2.setName("Jaco");
        per2.addFriends(p5);
        per2.addFriends(p6);
        per2.addFriends(p7);
        Person3 per3 = new Person3();
        per3.setName("Dave");
        Person3 per4 = new Person3();
        per4.setName("Alex");
        Person3 per5 = new Person3();
        per5.setName("Manie");

        gui.getPCTpprim().put(per1, new Integer(1));
        gui.getPCTpprim().put(per2, new Integer(2));
        gui.getPCTpprim().put(per3, new Integer(3));
        gui.getPCTpprim().put(per4, new Integer(4));
        gui.getPCTpprim().put(per5, new Integer(5));

        gui.getPrimTpPC().put(new Integer(10), per1);
        gui.getPrimTpPC().put(new Integer(20), per2);
        gui.getPrimTpPC().put(new Integer(30), per3);
        gui.getPrimTpPC().put(new Integer(40), per4);
        gui.getPrimTpPC().put(new Integer(50), per5);

        gui.getPrimTpprim().put(new Integer(1), new Date());
        gui.getPrimTpprim().put(new Integer(2), new Date());
        gui.getPrimTpprim().put(new Integer(3), new Date());
        gui.getPrimTpprim().put(new Integer(4), new Date());
        gui.getPrimTpprim().put(new Integer(5), new Date());

        gui.getPrim().add(new Integer(1));
        gui.getPrim().add(new Integer(2));
        gui.getPrim().add(new Integer(3));
        gui.getPrim().add(new Integer(4));
        gui.getPrim().add(new Integer(5));

        gui.getPrimStrings().add("Carl");
        gui.getPrimStrings().add("Jaco");
        gui.getPrimStrings().add("Dave");
        gui.getPrimStrings().add("Alex");
        gui.getPrimStrings().add("Manie");

        DateFormat formatter = new SimpleDateFormat("dd MM yyyy");

        gui.setBefore(formatter.parse("28 06 1971"));
        gui.setAfter(formatter.parse("28 06 2045"));

        GuiStuff gui1 = new GuiStuff();
        pm.makePersistent(gui1);

        gui1.setBefore(formatter.parse("28 06 1971"));
        gui1.setAfter(formatter.parse("28 06 2000"));

        pm.currentTransaction().commit();
        pm.close();
    }

    public void testCreateEmptyConcrete() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        EmptyConcrete emptyConcrete = new EmptyConcrete();
        pm.makePersistent(emptyConcrete);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testInnerClass() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Motivation motDeal = new Motivation(Motivation.Type.DEAL, "Deal");
        Motivation motInternal = new Motivation(Motivation.Type.INTERNAL,
                "Internal");
        Motivation motUser = new Motivation(Motivation.Type.USER, "User");

        pm.makePersistent(motDeal);
        pm.makePersistent(motInternal);
        pm.makePersistent(motUser);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Motivation motDeal1 = new Motivation(Motivation.Type.DEAL, "Deal1");
        Motivation motInternal1 = new Motivation(Motivation.Type.INTERNAL,
                "Internal1");
        Motivation motUser1 = new Motivation(Motivation.Type.USER, "User1");

        pm.makePersistent(motDeal1);
        pm.makePersistent(motInternal1);
        pm.makePersistent(motUser1);

        pm.currentTransaction().commit();
        pm.close();
    }

    public void testPreStore1() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        PrestoreModel p = new PrestoreModel();
        p.newB = "bbb";
        p.setA("Aa");
        p.setB("Bb");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (PrestoreModel)pm.getObjectById(id, true);
        Assert.assertEquals("bbb", p.getB());
        Assert.assertEquals("bbb", p.getAddress().getStreet());
        Assert.assertTrue(JDOHelper.isPersistent(p.getAddress()));
        pm.close();
    }

    public void testFlush7() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person main = new Person("main");
        pm.makePersistent(main);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        main.getPersonsList().add(new Person());
        VersantPersistenceManagerImp realPM = ((PMProxy)pm).getRealPM();
        realPM.flushRetainState();
        main.getPersonsList().add(new Person());
        realPM.flushRetainState();
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testGetConnectionURL() {
        PersistenceManager pm = pmf().getPersistenceManager();
        String url = ((VersantPersistenceManager)pm).getConnectionURL("main");
        System.out.println("url = " + url);
        pm.close();
    }

    public void testGetConnectionDriverName() {
        PersistenceManager pm = pmf().getPersistenceManager();
        String driverName = ((VersantPersistenceManager)pm).getConnectionDriverName(
                "main");
        System.out.println("driverName = " + driverName);
        pm.close();
    }

    public void testInsertOrder() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person main = new Person("main");
        pm.makePersistent(main);
        pm.currentTransaction().commit();
        System.out.println(
                ">>>>>>> TestGeneral.testInsertOrder after commit <<<<<<<<<");

        pm.currentTransaction().begin();
        main.getPersonsList().add(new Person());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testBla1() throws Exception {
        PersistenceManager pm1 = pmf().getPersistenceManager();
        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm1.currentTransaction().setOptimistic(true);
        pm2.currentTransaction().setOptimistic(true);

        pm1.currentTransaction().begin();
        Person p = new Person();
        p.setName("name1");
        pm1.makePersistent(p);
        Object id = pm1.getObjectId(p);
        pm1.currentTransaction().commit();

        pm1.currentTransaction().begin();
        p.setName("name2");
        pm1.currentTransaction().commit();

        System.out.println("\n*** Changing from name2 to name3 in pm2");
        pm2.currentTransaction().begin();
        Person p2 = (Person)pm2.getObjectById(id, true);
        Assert.assertEquals("name2", p2.getName());
        p2.setName("name3");
        pm2.currentTransaction().commit();

        System.out.println("\n*** Checking for name3 in pm1");
        pm1.currentTransaction().begin();
        Assert.assertEquals("name3", p.getName());
        pm1.currentTransaction().commit();

        pm1.close();
        pm2.close();
    }

    public void testBla2() throws Exception {
        MapModel p2 = null;
        PersistenceManager pm1 = pmf().getPersistenceManager();
        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm1.currentTransaction().setOptimistic(true);
        pm2.currentTransaction().setOptimistic(true);
        pm1.currentTransaction().begin();
        MapModel p = new MapModel();
        p.getBasicMap().put("key1", "val1");
        pm1.makePersistent(p);
        Object id = pm1.getObjectId(p);
        pm1.currentTransaction().commit();

        pm1.currentTransaction().begin();
        p.getBasicMap().put("key2", "val2");
        pm1.currentTransaction().commit();

        pm2.currentTransaction().begin();
        p2 = (MapModel)pm2.getObjectById(id, true);
        Assert.assertEquals(2, p2.getBasicMap().size());
        p2.getBasicMap().put("key3", "val3");
        pm2.currentTransaction().commit();

        pm1.currentTransaction().begin();
        Assert.assertEquals(3, p.getBasicMap().size());
        pm1.currentTransaction().commit();

        pm1.close();
        pm2.close();
    }

    public void testDependentMove() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        DepMaster depMaster = new DepMaster();
        DepMaster depMaster2 = new DepMaster();
        for (int i = 0; i < 5; i++) {
            DepDetail detail = new DepDetail();
            depMaster.add(detail);
        }
        pm.makePersistent(depMaster);
        pm.makePersistent(depMaster2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        List details = depMaster.getDepDetails();
        for (int i = 0; i < details.size(); i++) {
            DepDetail depDetail = (DepDetail)details.get(i);
            depMaster2.add(depDetail);
        }
        pm.deletePersistent(depMaster);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        List details2 = depMaster2.getDepDetails();
        Assert.assertEquals(details, details2);
        pm.close();
    }

    public void testBla() throws Exception {
        try {
            PersistenceManager pm = pmf().getPersistenceManager();
            pm.currentTransaction().setOptimistic(true);
//        pm.currentTransaction().setRetainValues(true);
            pm.currentTransaction().begin();
            Person main = new Person("main");
            pm.makePersistent(main);
            List list = new ArrayList();
            Set set = new HashSet();
            for (int i = 0; i < 10; i++) {
                Person child = new Person("child" + i);
                child.setPerson(main);
                list.add(child);
                set.add(child);
            }
            main.getPersonsList().addAll(list);
            main.getPersonsSet().addAll(set);
            pm.makePersistentAll(list);
            pm.currentTransaction().commit();

            pm.currentTransaction().begin();
            main.setVal("1");
            pm.currentTransaction().commit();

            pm.currentTransaction().begin();
            Assert.assertEquals(main.getPersonsList(), list);
            Assert.assertEquals(main.getPersonsSet(), set);
            pm.currentTransaction().rollback();

            pm.currentTransaction().begin();
            main.setVal("2");
            pm.currentTransaction().commit();

            pm.currentTransaction().begin();
//            try {
                main.getPersonsList().add(null);
//                throw new TestFailedException(
//                        "expected VersantNullElementException");
//            } catch (VersantNullElementException e) {
// //good
//            }
            pm.currentTransaction().rollback();

            pm.currentTransaction().begin();
            Assert.assertEquals(main.getPersonsList(), list);
            Assert.assertEquals(main.getPersonsSet(), set);
            pm.currentTransaction().rollback();

            pm.currentTransaction().begin();
            main.setVal("3");
            pm.currentTransaction().commit();

            pm.currentTransaction().begin();
            ((Person)main.getPersonsList().get(0)).setVal("1");
            pm.currentTransaction().commit();

            pm.close();
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            throw new JDOFatalException(e.getMessage(), e);
        }
    }

    public void testGetJDBCConnection() throws Exception {
        if (isRemote()) {
            unsupported();
            return;
        }
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        String store = null;
        java.sql.Connection con = pm.getJdbcConnection(store);
        pm.currentTransaction().begin();
        pm.currentTransaction().commit();
        Assert.assertTrue(!con.isClosed());
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        pm.currentTransaction().commit();
        Assert.assertTrue(!con.isClosed());

        pm.close();
        try {
            con.isClosed();
            Assert.fail(
                    "The real connection must be disconnected at this stage");
        } catch (JDOUserException e) {
            //the connection should have been released
        }
    }

    public void testFlush() throws Exception {
        if (isRemote()) {
            return;
        }

        long time;
        long count = 0;
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Master2.class);
        Collection col = (Collection)q.execute();
        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        for (int i = 0; i < 30; i++) {
            Master2 master = new Master2();
            pm.makePersistent(master);
        }
        for (int i = 0; i < 30; i++) {
            Master2 master = new Master2();
            pm.makePersistent(master);
        }
        for (int i = 0; i < 30; i++) {
            Master2 master = new Master2();
            pm.makePersistent(master);
        }
        for (int i = 0; i < 30; i++) {
            Master2 master = new Master2();
            pm.makePersistent(master);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        q = pm.newQuery(Master2.class);
        col = (Collection)q.execute();
        Assert.assertEquals(120, col.size());
        q.closeAll();
        pm.close();

        time = System.currentTimeMillis();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Extent terminals = pm.getExtent(Master2.class, false);

        Iterator i = terminals.iterator();
        while (i.hasNext()) {
            Master2 master2 = (Master2)i.next();
            for (int k = 0; k < 2; k++) {
                Detail2 detail = new Detail2();
                master2.add(detail);
                pm.makePersistent(detail);
                count++;
            }
            pm.flush();
        }
        pm.currentTransaction().commit();

        time = System.currentTimeMillis() - time;
        System.out.println(
                "Finished creating " + count + " messages in " + time + "ms");

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

    public void testFlush5() throws Exception {
        long time;
        long count = 0;
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Master2 master = new Master2();
            pm.makePersistent(master);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        time = System.currentTimeMillis();

        pm.currentTransaction().begin();
        Extent terminals = pm.getExtent(Master2.class, false);

        Iterator i = terminals.iterator();
        while (i.hasNext()) {
            Master2 master2 = (Master2)i.next();
            for (int k = 0; k < 3; k++) {
                Detail2 detail = new Detail2();
                master2.add(detail);
                pm.makePersistent(detail);
                count++;
            }
            pm.flush();
        }
        pm.currentTransaction().commit();
        time = System.currentTimeMillis() - time;
        System.out.println(
                "Finished creating " + count + " messages in " + time + "ms");

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

    public void testFlush6() throws Exception {
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();

        Person detail = new Person("det");
        pm.makePersistent(detail);

        Person p1 = new Person("p1");
        p1.addPersonToList(detail);
        pm.makePersistent(p1);

        Person p2 = new Person("p2");
        p2.addPersonToList(detail);
        pm.makePersistent(p2);

        Person p3 = new Person("p3");
        pm.makePersistent(p3);

        Query q = pm.newQuery(Person.class);
        ((List)q.execute()).size();

        p3.addPersonToList(detail);
        q.closeAll();

        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p3);
        pm.close();

        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = (Person)pm.getObjectById(id, true);
        Assert.assertEquals(1, p.getPersonsList().size());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testFlush2() throws Exception {
        if (isRemote()) {
            return;
        }
//		nuke(PCCollectionEntry.class);
//        nuke(MapModel.class);
//        nuke(Person.class);

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("p1");
        pm.makePersistent(p);
        Object id = pm.getObjectId(p);
        pm.flush();
        Assert.assertEquals("p1", p.getName());
        p.setName("p2");
        pm.flush();
        Assert.assertEquals("p2", p.getName());
        pm.currentTransaction().commit();
        pm.close();

        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, true);
        Assert.assertEquals("p2", p.getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testFlush3() throws Exception {
        nuke(Person.class);
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("p1");
        Address address = new Address();
        address.setStreet("street");
        p.setAddress(address);
        pm.makePersistent(p);
        Object id = pm.getObjectId(p);
        pm.flush();
        Assert.assertEquals("p1", p.getName());
        p.setName("p2");
        p.getAddress().setStreet("street1");
        pm.flush();
        Assert.assertEquals("p2", p.getName());
        Assert.assertEquals("street1", p.getAddress().getStreet());
        pm.currentTransaction().commit();
        pm.close();

        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, true);
        Assert.assertEquals("p2", p.getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testFlush4() throws Exception {
        nuke(Person.class);
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("p1");
        Address address = new Address();
        address.setStreet("street");
        p.setAddress(address);
        pm.makePersistent(p);
        Object id = pm.getObjectId(p);
        pm.flush();
        Assert.assertEquals("p1", p.getName());
        p.setName("p2");
        p.getAddress().setStreet("street1");
        pm.flush();
        Assert.assertEquals(p, pm.getObjectById(id, true));
        Assert.assertEquals("p2", p.getName());
        Assert.assertEquals("street1", p.getAddress().getStreet());
        pm.currentTransaction().commit();
        pm.close();

        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, true);
        Assert.assertEquals("p2", p.getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testGC() throws Exception {
        nuke(Master2.class);
        nuke(Detail2.class);
        long time;
        long count = 0;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 300; i++) {
            Master2 master = new Master2();
            pm.makePersistent(master);
        }
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        for (int i = 0; i < 300; i++) {
            Master2 master = new Master2();
            pm.makePersistent(master);
        }
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        for (int i = 0; i < 300; i++) {
            Master2 master = new Master2();
            pm.makePersistent(master);
        }
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        for (int i = 0; i < 300; i++) {
            Master2 master = new Master2();
            pm.makePersistent(master);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        time = System.currentTimeMillis();

        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().begin();
        Extent terminals = pm.getExtent(Master2.class, false);
        pm.currentTransaction().commit();

        Iterator i = terminals.iterator();
        int counter = 0;
        while (i.hasNext()) {
            counter++;
            pm.currentTransaction().begin();
            Master2 master2 = (Master2)i.next();
            for (int k = 0; k < 300; k++) {
                Detail2 detail = new Detail2();
                detail.setMaster(master2);
                pm.makePersistent(detail);
                count++;
            }
            pm.currentTransaction().commit();
        }
        time = System.currentTimeMillis() - time;

        System.out.println(
                "Finished creating " + count + " messages in " + time + "ms");
    }

    public void testAbstractSuper() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Empty empty = new EmptyExtension();
        pm.makePersistent(empty);
        Object id = pm.getObjectId(empty);

        EmptyContainer emptyContainer = new EmptyContainer();
        emptyContainer.setEmpty(empty);
        pm.makePersistent(emptyContainer);
        Object id2 = pm.getObjectId(emptyContainer);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(empty, emptyContainer.getEmpty());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testStatus() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        PmfStatus pmfStatus = pmf().getPmfStatus();
        System.out.println("pmfStatus = " + pmfStatus);
        pm.close();
    }

    public void testOO7Delete1() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        tx.setOptimistic(true);
        CompositePart compositePart = new CompositePart();
//        BaseAssembly baseAssembly = new BaseAssembly()




        boolean txOpen = false;
        Object[] newCompositePartIds = new Object[10];
        for (int i = 0; i < newCompositePartIds.length; i++) {
            if (!txOpen) {
                tx.begin();
                txOpen = true;
            }
            CompositePart cp = (CompositePart)pm.getObjectById(
                    newCompositePartIds[i], true);
            cp.delete(pm);

            // Commit the current transaction if we are running a multitransaction test
            // or if it is the last iteration.
            if (i == newCompositePartIds.length - 1) {
                tx.commit();
                txOpen = false;
            }
        }
    }

    public void test007() throws Exception {
        System.out.println("\n*** TestDesignRootNull.createModel");

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        // Create Module and its Assembly Hierarchy.
        Module m = new Module(pm, 2, 0);
        pm.makePersistent(m);
        Object mId = pm.getObjectId(m);
        m = null;

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Query q = pm.newQuery(pm.getExtent(DesignObj.class, true));
        List result = (List)q.execute();
        col.addAll(result);
        q = pm.newQuery(pm.getExtent(Manual.class, true));
        result = (List)q.execute();
        col.addAll(result);
        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
    }

    public void test007delete() throws Exception {

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        CompositePart compPart = new CompositePart();
        Document doc = new Document("title", "text");
        compPart.setDocumentation(doc);
        pm.makePersistent(compPart);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(compPart);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Query q = pm.newQuery(pm.getExtent(DesignObj.class, true));
        List result = (List)q.execute();
        col.addAll(result);
        q = pm.newQuery(pm.getExtent(Manual.class, true));
        result = (List)q.execute();
        col.addAll(result);
        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
    }

    public void test2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
//        tx.setOptimistic(true);
//        tx.setRetainValues(true);
//        tx.setRestoreValues(true);
//        tx.setNontransactionalRead(true);
//        tx.setNontransactionalWrite(true);
//        pm.setIgnoreCache(true);

        tx.begin();
        Company company = new Company();
        pm.makePersistent(company);
        company.setName("ACME");
        Employee1 employee = new Employee1();
        employee.setName("John");
        employee.setAge(30);
        employee.setSalary(40000);
        employee.setCompany(company);
        tx.commit();

        tx.begin();
        employee = new Employee1();
        employee.setName("Joe");
        employee.setAge(25);
        employee.setSalary(30000);
        employee.setCompany(company);
        tx.commit();

        tx.begin();
        Manager manager = new Manager();
        manager.setName("Jack");
        manager.setAge(40);
        manager.setSalary(100000);
        manager.setStocks(1000);
        company.setManager(manager);
        company.addEmployees(manager);
        tx.commit();
        pm.close();
    }

    public void testFieldRetrieval1() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Employee employee = new Employee();
        pm.currentTransaction().begin();
        List ps = new ArrayList();
        ps.add(new Person("empP1"));
        employee.setPersons(ps);
        employee.setSuperList(ps);
        pm.makePersistent(employee);
        Assert.assertEquals(1, employee.getPersons().size());
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(employee);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setNontransactionalRead(true);
        employee = (Employee)pm.getObjectById(id, true);
        Assert.assertEquals(1, employee.getPersons().size());
        Assert.assertEquals("empP1",
                ((Person)employee.getPersons().get(0)).getName());
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setNontransactionalRead(true);
        employee = (Employee)pm.getObjectById(id, true);
        Assert.assertEquals(1, employee.getSuperList().size());
        Assert.assertEquals("empP1",
                ((Person)employee.getPersons().get(0)).getName());
        pm.close();
    }

    public void testFieldRetrieval2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        TempEmployee employee = new TempEmployee();
        pm.currentTransaction().begin();
        List ps = new ArrayList();
        ps.add(new Person("empP1"));
        employee.setPersons(ps);
        employee.setSuperList(ps);
        employee.getTmpEmployeeFriends().add(new Person("tmpFriend"));
        pm.makePersistent(employee);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(employee);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setNontransactionalRead(true);
        employee = (TempEmployee)pm.getObjectById(id, true);
        Assert.assertEquals(1, employee.getPersons().size());
        Assert.assertEquals("empP1",
                ((Person)employee.getPersons().get(0)).getName());
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setNontransactionalRead(true);
        employee = (TempEmployee)pm.getObjectById(id, true);
        Assert.assertEquals(1, employee.getSuperList().size());
        Assert.assertEquals("empP1",
                ((Person)employee.getPersons().get(0)).getName());
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setNontransactionalRead(true);
        employee = (TempEmployee)pm.getObjectById(id, true);
        Assert.assertEquals("tmpFriend",
                ((Person)employee.getTmpEmployeeFriends().get(0)).getName());
        Assert.assertEquals(1, employee.getTmpEmployeeFriends().size());
        pm.close();
    }

    public void testGetSupOpts() {
        Debug.OUT.println(pmf().supportedOptions());
    }

    /**
     * This is a test to check valid updating of a field does not
     * cause an concurrent update exceptions.
     *
     * @throws Exception
     */
    public void testForConcurrentUpdate() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = null;

        pm.currentTransaction().begin();
        p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.setName("name1");
        p.getName();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.setName("name2");
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.setName("name1");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.setName("name2");
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        System.out.println("%%% p.setName(\"name1\")");
        p.setName("name1");
        System.out.println("%%% commit()");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        System.out.println("%%% p.setName(\"name2\")");
        p.setName("name2");
        System.out.println("%%% commit()");
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testForReplaceSCOFieldsWithRetainValues() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        long now = TIME1;
        long then = now + 100;

        Person p = new Person("name");
        p.setBirthDate(new Date(now));
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        /**
         * If the field reports itself as dirty then this should be cool
         */
        pm.currentTransaction().begin();
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "###################### before date change ############################");
        }
        p.getBirthDate().setTime(then);
        Utils.assertTrue(Utils.isPDirty(p));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Utils.assertTrue(!Utils.isTDirty(p));
        p.getStringSet().add("added");
        Utils.assertTrue(Utils.isPDirty(p));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testForReplaceSCOFieldsWithRetainValuesOnInheritedScheme()
            throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        long now = TIME1;
        long then = now + 100;

        ClassAB p = new ClassAB();
        p.setDateAB(new Date(now));
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        /**
         * If the field reports itself as dirty then this should be cool
         */
        pm.currentTransaction().begin();
        p.getDateAB().setTime(then);
        Utils.assertTrue(Utils.isPDirty(p));
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * This test a 3 level deep reachability graph for persisitence.
     *
     * @throws Exception
     */
    public void testRefList() throws Exception {
        if (isVds()) {
            /**
             * This test case runs into a locking issue under vds.
             */
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        lockNone(pm);
        pm.currentTransaction().setOptimistic(false);
        pm.currentTransaction().setRetainValues(false);
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().setNontransactionalWrite(false);

        System.out.println("%%% TestGeneral.testRefList begin 1");
        pm.currentTransaction().begin();
        Person3 p = new Person3();
        p.setName("name");

        Person friend1 = new Person("friend");
        List list = new ArrayList();
        list.add(friend1);

        p.setFriends(list);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        System.out.println("%%% TestGeneral.testRefList commit 1");

        PersistenceManager pm2 = pm;
        pm = pmf().getPersistenceManager();
        lockNone(pm);
        System.out.println("%%% TestGeneral.testRefList begin 2");
        pm.currentTransaction().begin();
        Person3 p3 = (Person3)pm.getObjectById(JDOHelper.getObjectId(p), false);
        List tmpList = p3.getFriends();
        Utils.assertEquals(1, tmpList.size());
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "####################### before equality check ############################");
        }
        tmpList.equals(list);
        pm.currentTransaction().commit();
        System.out.println("%%% TestGeneral.testRefList commit 2");

        PersistenceManager pm3 = pm;
        pm = pmf().getPersistenceManager();
        lockNone(pm);
        pm.currentTransaction().setNontransactionalRead(true);
        System.out.println("%%% TestGeneral.testRefList begin 3");
        pm.currentTransaction().begin();
        p3 = (Person3)pm.getObjectById(JDOHelper.getObjectId(p), false);
        Person newP = new Person("friend2");
        list.add(newP);
        p3.getFriends().add(newP);
        pm.currentTransaction().commit();
        System.out.println("%%% TestGeneral.testRefList commit 3");

        PersistenceManager pm4 = pm;
        pm = pmf().getPersistenceManager();
        lockNone(pm);
        pm.currentTransaction().setNontransactionalRead(true);
        System.out.println("%%% TestGeneral.testRefList begin 4");
        pm.currentTransaction().begin();
        p3 = (Person3)pm.getObjectById(JDOHelper.getObjectId(p), false);
        tmpList = p3.getFriends();
        Utils.assertTrue(tmpList.size() == 2);
        tmpList.equals(list);
        p3.getFriends().add(new Person("friend3"));
        pm.currentTransaction().commit();
        System.out.println("%%% TestGeneral.testRefList commit 4");

        System.out.println("%%% TestGeneral.testRefList begin 5");
        pm.currentTransaction().begin();
        tmpList = p3.getFriends();
        for (int i = 0; i < tmpList.size(); i++) {
            if (Debug.DEBUG) {
                Debug.OUT.println("type = " + tmpList.get(i));
            }
            ((Person)tmpList.get(i)).setName("newName" + i);
        }
        pm.currentTransaction().commit();
        System.out.println("%%% TestGeneral.testRefList commit 5");

        PersistenceManager pm5 = pm;
        pm = pmf().getPersistenceManager();
        lockNone(pm);
        System.out.println("%%% TestGeneral.testRefList begin 6");
        pm.currentTransaction().begin();
        p3 = (Person3)pm.getObjectById(JDOHelper.getObjectId(p3), false);
        tmpList = p3.getFriends();
        for (int i = 0; i < tmpList.size(); i++) {
            if (Debug.DEBUG) {
                Debug.OUT.println("type = " + tmpList.get(i));
            }
        }
        pm.currentTransaction().commit();
        System.out.println("%%% TestGeneral.testRefList commit 6");

        PersistenceManager pm6 = pm;
        pm = pmf().getPersistenceManager();
        lockNone(pm);
        System.out.println("%%% TestGeneral.testRefList begin 7");
        pm.currentTransaction().begin();
        Person3 person3 = new Person3();
        person3.setName("LastManStanding");
        pm.makePersistent(person3);

        Extent extent = pm.getExtent(Person3.class, true);
        Iterator iterator = extent.iterator();
        while (iterator.hasNext()) {
            ((Person3)iterator.next()).getFriends();
            if (Debug.DEBUG) {
                Debug.OUT.println("############### found one");
            }
        }
        pm.currentTransaction().commit();
        System.out.println("%%% TestGeneral.testRefList commit 7");

        pm.close();
        pm2.close();
        pm3.close();
        pm4.close();
        pm5.close();
        pm6.close();
    }

    /**
     * This test catches a same instance state in the global cache and used by a client.
     *
     * @throws Exception
     */
    public void test1() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);

        pm.currentTransaction().begin();
        Level1 level1 = new Level1();
        level1.setName("level1");
        Level2 level2 = new Level2();
        level2.setName("level2");
        Level3 level3 = new Level3();
        level3.setName("level3");
        level1.setLevel2(level2);
        level2.setLevel3(level3);
        pm.makePersistent(level1);
        pm.currentTransaction().commit();
        pm.close();


//        pmf = tests.getPMF();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();

        Query q = pm.newQuery();
        q.setClass(Level1.class);
        q.declareParameters("String val");
        q.setFilter("level2.level3.name == val");
        Collection col = (Collection)q.execute("level3");

        Level1 o = null;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            o = (Level1)iterator.next();
            if (Debug.DEBUG) {
                Debug.OUT.println(
                        "######name = " + o.getLevel2().getLevel3().getName());
            }
        }
        q.closeAll();
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setNontransactionalRead(true);
        Level1 l1 = (Level1)pm.getObjectById(JDOHelper.getObjectId(o), false);
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "level3.name = " + l1.getLevel2().getLevel3().getName());
        }
        pm.close();
    }

    /**
     * found that if a field was read and caused the isLoaded to change to <code>true</code>.
     * The next read will not reflect the actual data because the isloaded was not reset and hence the
     * local field was returned to the user.
     * <p/>
     * Possible fix would be to either reset the isloaded for that field or to replace the
     * value on the client
     *
     * @throws Exception
     */
    public void testIsLoaded() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("name", p.getName());
        p.setName("name1");
        Assert.assertEquals("name1", p.getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testIsLoaded2() throws Exception {
//        pmf = tests.getPMF();

        Object id = null;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person2 p = new Person2();
        p.setName1("name1");
        p.setName2("name2");
        p.setName3("name3");

        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        id = JDOHelper.getObjectId(p);
        pm.currentTransaction().commit();
        pm.close();

//        pmf = tests.getPMF();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person2)pm.getObjectById(id, false);
        p.setName2("name22");
        Assert.assertEquals("name1", p.getName1());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testUpdateRefFieldToNull() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        Address address = new Address();
        address.setStreet("street");
        p.setAddress(address);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.setAddress(null);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertNull(p.getAddress());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testSimpleUpdatesOfPersistentNewInstance() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        p.setName("name1");
        Assert.assertEquals("name1", p.getName());
        p.setName("name2");
        Assert.assertEquals("name2", p.getName());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("name2", p.getName());
        pm.currentTransaction().commit();
        pm.close();

    }

    public void testAutoSetFields() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("p");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertNotNull(p.getAutoDate());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOptimistic1() throws Exception {
        nukePersons();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Person p = new Person("main");

        Person child1 = new Person("child1");
        p.addPersonToList(child1);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().setOptimistic(true);
        pm2.currentTransaction().begin();
        Person p2 = (Person)pm2.getObjectById(JDOHelper.getObjectId(p), false);
        p2.addPersonToList(new Person("child2"));
        pm2.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(p.getPersonsList().size() == 2);
        pm.currentTransaction().commit();

        pm.close();
        pm2.close();
    }

    private void nukePersons() throws SQLException {
        nuke(new Class[]{
            TempEmployee.class, Employee.class, EmpSuper.class,
            Person3.class, Person2.class, Person.class});
    }

    /**
     * eg. If a state's <code>String</code> field 'field 1' is filled with say 'name'
     * and a query is done that brings back results that includes this specific state
     * and it contains a more up to date version of the field then it can optionaly be updated
     * if it was not changed by the client. This could lead to more concurrent access.
     * <p/>
     * This test the update of already read data from
     *
     * @throws Exception
     */
    public void testOptimistic2() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        Person p = new Person("main");

        Person child1 = new Person("child1");
        p.addPersonToList(child1);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().setOptimistic(true);
        pm2.currentTransaction().setRetainValues(true);
        pm2.currentTransaction().begin();
        Person p2 = (Person)pm2.getObjectById(JDOHelper.getObjectId(p), false);
        p2.addPersonToList(new Person("child2"));
        pm2.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class, "name == \"main\"");
        Collection col = (Collection)q.execute();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Person o = (Person)iterator.next();
            Assert.assertTrue(o == p);
            Assert.assertTrue(o.getPersonsList().size() == 2);
        }
        Assert.assertTrue(p.getPersonsList().size() == 2);
        pm.currentTransaction().commit();

        pm.close();
        pm2.close();
    }

    /**
     * This tests persistence by reachability. A PC with a col field that hold
     * other PC instances is made persistent. After commit the instances in the
     * col must also be persistent.
     *
     * @throws Exception
     */
    public void testPersistenceByReachability() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("main");
        for (int i = 0; i < 10; i++) {
            p.getPersonsList().add(new Person("child" + 0));
        }
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(p.getPersonsList().size() == 10);
        for (int i = 0; i < p.getPersonsList().size(); i++) {
            Person o = (Person)p.getPersonsList().get(i);
            Assert.assertTrue(JDOHelper.isPersistent(o));
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * This is a test for a bug that accurred when a already managed instance
     * was updated with data from a query. It's rowVersion field was then over
     * wrote.
     */
    public void testUpdateStateFromQueryWithRowVersion() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Employee emp = new Employee();
        emp.getPersons().add(new Person("dude"));
        pm.makePersistent(emp);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        emp.getName();

        emp.getPersons();
        emp.setName("dude2");
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test the various possible args for pm.getObjectIdClass
     */
    public void testGetObjectIdClass() {
        PersistenceManager pm = pmf().getPersistenceManager();
        Class cls = pm.getObjectIdClass(null);
        Assert.assertNull(cls);
        cls = pm.getObjectIdClass(Person.class);
        Assert.assertTrue(VersantOid.class == cls);
        cls = pm.getObjectIdClass(SimpleAP.class);
        Assert.assertTrue(SimpleAP_PK.class == cls);
        cls = pm.getObjectIdClass(String.class);
        Assert.assertNull(cls);
        cls = pm.getObjectIdClass(Abstract1.class);
        Assert.assertNull(cls);
        pm.close();
    }

    public void testnewObjectIdInstanceAppId() throws Exception {
    	if (!isApplicationIdentitySupported())
    	{
    		return;
    	}
        nuke(SimpleAP.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SimpleAP simpleAP = new SimpleAP();
        simpleAP.setIdNo(1);
        simpleAP.setName("name1");
        pm.makePersistent(simpleAP);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        Object id = pm.newObjectIdInstance(SimpleAP.class, "1");
        pm.currentTransaction().begin();
        SimpleAP s2 = (SimpleAP)pm.getObjectById(id, true);
        Assert.assertEquals("name1", s2.getName());
        Assert.assertEquals(1, s2.getIdNo());
        pm.close();
    }

    public void testnewObjectIdInstanceDSId() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        nukePersons();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.setName("name101");
        p.setVal("val101");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        String idString = pm.getObjectId(p).toString();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Object id = pm.newObjectIdInstance(Person.class, idString);
        Person p2 = (Person)pm.getObjectById(id, true);
        Assert.assertEquals("name101", p2.getName());
        Assert.assertEquals("val101", p2.getVal());
        pm.close();
    }

    /**
     * Test navigation of a reference to a missing object (bug 133).
     */
    public void testNavigateMissingRef() throws Exception {

        // create SimpleRef -> Address
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address a = new Address("nowhereland");
        SimpleRef sr = new SimpleRef("testNavigateMissingRef", a);
        pm.makePersistent(sr);
        Object oidsr = pm.getObjectId(sr);
        Object oida = pm.getObjectId(a);
        pm.currentTransaction().commit();
        pm.close();

        // delete the Address
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        a = (Address)pm.getObjectById(oida, true);
        pm.deletePersistent(a);
        pm.currentTransaction().commit();
        pm.close();

        // navigate to the address and look for the exception
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        sr = (SimpleRef)pm.getObjectById(oidsr, true);
        try {
            System.out.println("sr.getAddress() = " + sr.getAddress());
            throw new TestFailedException("Nav to missing row worked");
        } catch (JDODataStoreException e) {
            System.out.println("GOOD: " + e);
        }
        try { // repeat
            System.out.println("sr.getAddress() = " + sr.getAddress());
            throw new TestFailedException("Nav to missing row worked");
        } catch (JDODataStoreException e) {
            System.out.println("GOOD: " + e);
        }
        pm.currentTransaction().commit();
        pm.close();

        nuke(SimpleRef.class);
    }

}
