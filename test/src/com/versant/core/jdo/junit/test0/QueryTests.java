
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
import com.versant.core.jdo.junit.test0.model.noclassid.*;
import com.versant.core.jdo.junit.test0.model.noclassid.BaseClass;
import com.versant.core.jdo.junit.test0.model.kroll.KDetail;
import com.versant.core.jdo.junit.test0.model.kroll.KMaster;
import com.versant.core.jdo.*;
import com.versant.core.logging.LogEvent;
import com.versant.core.jdo.junit.TestFailedException;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.jdbc.JdbcStorageManager;
import com.versant.core.jdo.ServerLogEvent;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.storagemanager.StorageManager;
import com.versant.core.logging.LogEvent;

import javax.jdo.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Lots of JDOQL related tests.
 */
public class QueryTests extends VersantTestCase {

    private final Collection nullCollection = null;

    public QueryTests(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite("QueryTests");
        String[] a = new String[]{
            "testCollectionAccessWithNoClassId",
            "testVariableInResultQuery",
            "testVariableInResultQuery2",
            "testVariableInResultQuery3",
            "testVariableInResultQuery4",
            "testDateFilterQuery",
            "testMapKeyNot",
            "testNtoNQuery",
            "testQgetAll",
            "testQClosing2",
            "testQueryById",
            "testQueryByAppId",
            "testQueryByAppId2",
            "testQueryByAppId3",
            "testRefHollowInstanceFromDirty",
            "testQueryFlush",
            "testQueryGet",
            "testQueryGet1",
            "testForToManyJoins22",
            "testForToManyJoins222",
            "testForToManyJoins0",
            "testForToManyJoins",
            "testForToManyJoins2",
            "testForToManyJoins3",
            "testCollectionParam1",
            "testCollectionParam2",
            "testCollectionParam3",
            "testFlushOnHashSet",
            "testFlushOnTreeSet",
            "testDateFieldWithDateParam",
            "testLiteralBoolean",
            "testNotOnBooleanField",
            "testNotOnBooleanField1",
            "testQSubList",
            "testLiteralBoolean",
            "testNotOnBooleanField",
            "testNotOnBooleanField1",
            "testReQueryOfDeletedInstanceInsameTx",
            "testFGJoinWithFilterJoins",
            "testFGJoinWithFilterJoinsConCurUpdate",
            "testQWithNewPCInstanceParamsAndIgnoreCache",
//            "testQWithWronpParams",
            "testQByFG",
            "testQDirtyList",
            "testQDirtyList1",
            "testQDirtyList2",
            "testQDirtyList3",
            "testSCOCollectionFlush3",

            "testQClosing",
            "testStrictTx",
            "testQMaxResults",
            "testQMaxResults2",
            "testMultipleQClose",
            "testQCache",
            "testQCache2",
            "testQCache3",
            "testQCache4",
            "testQCache5",
            "testQCache6",
            "testQCache7",
            "testQCache8",
            "testQCache9",
            "testQCache10",
            "testQCache11",
            "testQueryCache11",
            "testQueryCache12",
            "testQueryCache13",
            "testQueryCache14",
            "testQueryCache15",
            "testQueryCache16",
            "testQueryColSerialization",
            "testQueryStringParam",
            "testQueryPlan",
            "testDateNullAndDateNull",
            "testPanagiotis",
            "testContainsAnd",
            "testNotContainsAnd",
            "testNotAnd",
            "testExtentForNotIncludeSubclasses",
            "testCloseExtent",
            "testDirtyFlush",
            "testDirtyFlushRetainValues",
            "testRollbackQ",
            "testMap",
            "testMapKey",
            /*DAF*/ "testMapWithRefKey",
            "testMapWithRefValue",
            "testLocalParamAndFieldSameName",
            "testLocalParamAndFieldSameName2",
            "testQCaching",
            "testLocalBigDecEqaulityWrongType",
            "testLocalPCWrongType",
            "testNullPC",
            "testQ",
            "testBla",
//            "testVolumeQ",
//            "testVolumeQ2",
            "testLocalBin",
            "testLocalBinObject",
            "testLocalBinObjectParam",
            "testoo7Q4",
            "testoo7Q5",
            "testCollectionQBasic1",
            "testCollectionQBasic2",
            "testThisKeyWord",

            "testEmptyStringQs",
            "testNullQ",
            "testNullParamQ",
            "testCollectionNullParamQ",
            "testMultiTypeNavigation",
            "testLocalMultiply",
            "testLocalMultiplyBig",
            "testLocalMultiplyBigDec",
            "testLocalToLowerCase",
            "testLocalIsEmpty",
            "testLocalIsEmptyAnd",
            /*DAF*/"testLocalMapContainsKey",
            "testLocalMapContainsKey2",
            "testNotEqual",
            "testUnaryTilde",
            "testUnaryTildeObject",
            "testUnaryNot",
            "testUnaryNeg",
            "testUnaryNegOnObject",
            "testUnaryNegOnObjectParam",
            "testLocalBigEqaulity",
            "testLocalBigDecEqaulity",
            "testPrimitiveAddition",
            "testPrimitiveSubtract",
            "testPrimitiveAndObjectAddition",
            "testPrimitiveAndObjectSubtraction",
            "testPrimitiveAndBigObjectAddition",
            "testStringConcat",
            "testLocalTrueQuery",

            "testLocalOrIntLongObject",
            "testLocalStartsWith",
            "testLocalStartsWithOr",
            "testLocalEndsWith",

            "testCollectionQBasic",
            /* test fails - mem queries need to be fixed  "testCollectionQ", */
            "testCollectionQ1",
            "testCollectionQ11",
            "testCollectionQ2",
            "testCollectionQ2Or",
            "testCollectionQWithOR",
            "testMultipleAnds",
            "testIntIntOp",
            "testLocalIntIntegerGE",
            "testLocalIntInteger",
            "testLocalIntegerIntGE",

            "testLocalIntLongObject",
            "testLocalFloatDoubleObject",
            "testLocalDoubleFloatObject",
            "testMultipleAnds2",
            "testDoubleAndDouble",
            "testDoubleAndInt",
            "testAnd",
            "testLocalPCQNav",
            "testLocalPCQNav2",
            "testLocalPCQNav3",
            "testLocalPCQ",
            "testLocalPCQ1",
            "testLocalStringParamQRev",
            "testLocalStringParamQ",
            "testLocalLiteralToString",
            "testLocalStringToLiteral",
            "testLocalIntInt",
            "testLocalByteInt",
            "testLocalShortInt",
            "testLocalLongInt",
            "testLocalDoubleInt",
            "testLocalFloatInt",
            "testLocalIntFloat",
            "testLocalLongFloat",
            "testLocalCharInt",

            "testLocalQDate",
            "testIgnoreCache",
            "testBasicRandomAccess",
            "testBasicRandomAccess3",
            "testBasicRandomAccess2",
            "testModifyQueryCollection",
            "testMultipleLocalGets",
            "testMultipleLocalIters",
            "testQueryWithPCNonManagedInstance",
            "testMultipleQs",
            "testConcurrentQs",
            "testGetListIter",
            "testGet",
            "testExecuteQueryWithClosedPM",
            "testSerialisedQueryPMNull",
            "testRepeatQWithDeserialisedQuery",
            "testSerialize",
            "testParamQuery",
            "testNonTxQueries",
            "testParamQuery2",
            "testLocalQueryOrdering",
            "testLocalQueryOrderingWithNavigation",
            "testLocalQueryOrderingWithNullFilter",
            "testQueryOrdering1",
            "testQueryOrdering2",
            "testExtent1",
            "testQueryWithSuppliedFG",
            "testExtentForSupClass",
            "testExtentForSupClass2",
            "testExtentForSupClass3",
            "testExtentForSupClass4",
            "testInheritanceQStartsWith",
            "testInheritanceQEndsWith",
            "testInheritanceQEndsWithOrdered",
            "testQueryExecuteWithMap",
            "testQueryExecuteWithMapErrorParamNames",
            "testQueryExecuteWithMapErrorParamAmount",
            "testCollectionQuery",
            "testCollectionQueryFromExtentQuery",
            "testCollectionQueryFromExtentQuerySorted",
            "testGetMethodOnQueryCollection",
            "testQueryIgnoreCacheFalse1",
            "testQueryIgnoreCacheFalse2",
            "testQueryIgnoreCacheFalse3",
            "testLocalQInheritance",
            "testLocalQInheritance2",
            "testMultipleContainsQs",
            "testLocalParamContainsQsAndCompare",
//////            "testLocalContainsWithUnboundVar",
            "testLocalContainsWithUnboundVar2",
            "testLocalContainsWithInt",
            "testQWithAppIdInstanceAsParam",
            "testRAccessQSubList",
            "testBackSlachInFilter",
//            "testSubOrdering",
            "testBooleanNavigation",
            "testOJ1",
            "testOJ2",
            "testUnboundContainsVar",
            "testUnboundContainsVar2",
            "testCacheableJDOQL",
            "testSizeOnEmptyQuery",
            "testCrossJoinOnFkCol",
            "testQueryGetOnEmptyQuery",
            "testQueryThatIncludedsDeletedInstance",
            "testGetNFromCacheQueryResult",
            "testMultipleQueries",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new QueryTests(a[i]));
        }
        return s;
    }

    public void testCollectionAccessWithNoClassId() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        ClassWithReferenceCol cwr = new ClassWithReferenceCol();
        String val = "" + System.currentTimeMillis();
        cwr.setVal(val);

        cwr.getCol().add(new SubClass1(cwr, "SubClass1"));
        cwr.getCol().add(new com.versant.core.jdo.junit.test0.model.noclassid.BaseClass(cwr, "BaseClass"));
        cwr.getCol().add(new com.versant.core.jdo.junit.test0.model.noclassid.SubClass2(cwr, "SubClass2"));
        pm.makePersistent(cwr);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(ClassWithReferenceCol.class, "val == param");
        q.setBounded(true);
        q.declareParameters("String param");
        Collection result = (Collection) q.execute(val);
        System.out.println(" = " +result.size());
        cwr = (ClassWithReferenceCol) result.iterator().next();
        for (Iterator iterator = cwr.getCol().iterator(); iterator.hasNext();) {
            com.versant.core.jdo.junit.test0.model.noclassid.BaseClass baseClass = (com.versant.core.jdo.junit.test0.model.noclassid.BaseClass) iterator.next();
            if (!baseClass.getClass().getName().endsWith(baseClass.getValue())) {
                fail();
            }
        }
        assertEquals(3, cwr.getCol().size());
        pm.close();
    }

    public void testVariableInResultQuery() {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p1 = new Person("owner");
        Person p2 = new Person("1");
        p2.setVal("2003");
        Person p3 = new Person("2");
        p3.setVal("2003");
        Person p4 = new Person("3");
        p4.setVal("2004");

        p1.getPersonsSet().add(p2);
        p1.getPersonsSet().add(p3);
        p1.getPersonsSet().add(p4);
        pm.makePersistent(p1);

        Person p6 = new Person("owner2");
        Person p7 = new Person("4");
        p7.setVal("2000");
        p6.getPersonsSet().add(p7);
        pm.makePersistent(p6);
        pm.currentTransaction().commit();


        pm.currentTransaction().begin();
        String filter = "personsSet.contains(v1) && v1.val == \"2003\"";
//        String filter = "personsSet.contains(v1) && (v1.val == \"2003\" || v1.val == \"2000\")";
//        String filter = "personsSet.contains(v1) && (v1.val == \"2003\" || v1.val == \"2000\") && personsSet.contains(v2) && v2.val == \"2003\"";
        String resultString = "name, val, v1.name";
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class);
        q.setFilter(filter);
        q.setResult(resultString);
        q.declareVariables("Person v1; Person v2");

        Collection result = (Collection) q.execute();
        assertEquals(2, result.size());
        System.out.println("result = " + result);
        pm.close();
    }

    public void testVariableInResultQuery2() {
        if (true) {
            broken();
            return;
        }

        if (!isJdbc()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p1 = new Person("owner");
        Person p2 = new Person("1");
        p2.setVal("2003");
        Person p3 = new Person("2");
        p3.setVal("2003");
        Person p4 = new Person("3");
        p4.setVal("2004");

        p1.getPersonsSet().add(p2);
        p1.getPersonsSet().add(p3);
        p1.getPersonsSet().add(p4);
        pm.makePersistent(p1);

        Person p6 = new Person("owner2");
        Person p7 = new Person("4");
        p7.setVal("2003");
        p6.getPersonsSet().add(p7);
        pm.makePersistent(p6);
        pm.currentTransaction().commit();


        pm.currentTransaction().begin();
        String filter = "personsSet.contains(v1) && v1.val == \"2003\"";
//        String filter = "personsSet.contains(v1) && (v1.val == \"2003\" || v1.val == \"2000\")";
//        String filter = "personsSet.contains(v1) && (v1.val == \"2003\" || v1.val == \"2000\") && personsSet.contains(v2) && v2.val == \"2003\"";
        String resultString = "name, val, v1.val";
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class);
        q.setFilter(filter);
        q.setResult(resultString);
        q.declareVariables("Person v1; Person v2");

        Collection result = (Collection) q.execute();
        assertEquals(2, result.size());
        System.out.println("result = " + result);
        pm.close();
    }

    public void testVariableInResultQuery3() {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p1 = new Person("owner");
        Person p2 = new Person("1");
        p2.setVal("2003");
        p2.setIntField(55);
        Person p3 = new Person("2");
        p3.setVal("2003");
        p3.setIntField(55);
        Person p4 = new Person("3");
        p4.setVal("2004");

        p1.getPersonsSet().add(p2);
        p1.getPersonsSet().add(p3);
        p1.getPersonsSet().add(p4);
        pm.makePersistent(p1);

        Person p6 = new Person("owner2");
        Person p7 = new Person("4");
        p7.setVal("2003");
        p7.setIntField(55);
        p6.getPersonsSet().add(p7);
        pm.makePersistent(p6);
        pm.currentTransaction().commit();


        pm.currentTransaction().begin();
        String filter = "personsSet.contains(v1) && v1.val == \"2003\"";
//        String filter = "personsSet.contains(v1) && (v1.val == \"2003\" || v1.val == \"2000\")";
//        String filter = "personsSet.contains(v1) && (v1.val == \"2003\" || v1.val == \"2000\") && personsSet.contains(v2) && v2.val == \"2003\"";
        String resultString = "name, val, v1, v1.intField, v1.val, v1.name";
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class);
        q.setFilter(filter);
        q.setResult(resultString);
        q.declareVariables("Person v1; Person v2");

        List result = (List) q.execute();
        assertEquals(3, result.size());
        for (int i = 0; i < 3; i++) {
            System.out.println("\n\n\n\n\n\nBEFORE");
            String val = ((Person)((Object[])result.get(i))[2]).getVal();
            assertEquals("2003", val);
            int intField = ((Number)((Object[])result.get(i))[3]).intValue();
            assertEquals(55, intField);
        }

        System.out.println("result = " + result);
        pm.close();
    }

    public void testVariableInResultQuery4() {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p1 = new Person("owner");
        Person p2 = new Person("1");
        p2.setVal("2003");
        p2.setIntField(55);
        Person p3 = new Person("2");
        p3.setVal("2003");
        p3.setIntField(55);
        Person p4 = new Person("3");
        p4.setVal("2004");

        p1.getPersonsSet().add(p2);
        p1.getPersonsSet().add(p3);
        p1.getPersonsSet().add(p4);
        pm.makePersistent(p1);

        Person p6 = new Person("owner2");
        Person p7 = new Person("4");
        p7.setVal("2003");
        p7.setIntField(55);
        p6.getPersonsSet().add(p7);
        pm.makePersistent(p6);
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        String filter = "personsSet.contains(v1) && v1.val == \"2003\"";
//        String filter = "personsSet.contains(v1) && (v1.val == \"2003\" || v1.val == \"2000\")";
//        String filter = "personsSet.contains(v1) && (v1.val == \"2003\" || v1.val == \"2000\") && personsSet.contains(v2) && v2.val == \"2003\"";
        String resultString = "this, v1";
        VersantQuery q = (VersantQuery) pm.newQuery(Person.class);
        q.setFilter(filter);
        q.setResult(resultString);
        q.declareVariables("Person v1; Person v2");

        Collection results = (Collection) q.execute();
        for (Iterator iterator = results.iterator(); iterator.hasNext();) {
            Object[] o = (Object[]) iterator.next();
            for (int i = 0; i < o.length; i++) {
                Object o1 = o[i];
                System.out.println("o1 = " + o1);
            }
        }
        System.out.println("result = " + results.size());
        pm.close();
    }

    public void testMultipleQueries() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        pm.makePersistent(new Address("oink123"));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        int n = 10;
        for (int i = 0; i < n; i++) {
            Query q = pm.newQuery(Address.class, "street == p");
            q.declareParameters("String p");
            Collection ans = (Collection)q.execute("oink123");
            ans.size();
            q.closeAll();
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testQueryGetOnEmptyQuery() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        Query q = pm.newQuery(Address.class, "street == param");
        q.declareParameters("String param");
        List result = (List) q.execute(val);
        try {
            result.get(0);
            fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        pm.close();

    }

    public void testQueryThatIncludedsDeletedInstance() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address();
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        VersantQuery q = (VersantQuery) pm.newQuery(Address.class);
        List result = new ArrayList((List) q.execute());
        pm.close();
    }

    public void testQueryThatIncludedsDeletedInstance2() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address();
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        VersantQuery q = (VersantQuery) pm.newQuery(Address.class);
        List result = (List) q.execute();
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Address address = (Address) iterator.next();
            System.out.println("address = " + address);
        }
        pm.close();
    }

    public void testGetNFromCacheQueryResult() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for(int i = 0; i < 10; i++) {
            NonMutableJavaTypes nm = new NonMutableJavaTypes();
            nm.setIntValue(i);
            pm.makePersistent(nm);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setOrdering("intValue ascending");
        List result = (List) q.execute();
        //this should cache the results.
        ArrayList al = new ArrayList(result);
        for (int i = 0; i < al.size(); i++) {
            NonMutableJavaTypes nonMutableJavaTypes = (NonMutableJavaTypes) al.get(i);
            assertEquals(i, nonMutableJavaTypes.getIntValue());
        }

        q = pm.newQuery(NonMutableJavaTypes.class);
        q.setOrdering("intValue ascending");
        result = (List) q.execute();
        //this should cache the results.
        assertEquals(5, ((NonMutableJavaTypes)result.get(5)).getIntValue());

        pm.close();
    }


    public void testCrossJoinOnFkCol() {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        long lVal = System.currentTimeMillis();
        KMaster m = new KMaster(1, 1);
        m.setlVal(lVal);
        m.setVal("m1");
        for (int i = 0; i < 2; i++) {
            m.getDetails().add(new KDetail(m));
        }
        pm.makePersistent(m);

        m = new KMaster(1, 2);
        m.setlVal(lVal);
        m.setVal("m2");
        pm.makePersistent(m);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(KMaster.class, "lVal == param");
        q.declareParameters("long param");
        q.setOrdering("val ascending");
        List result = (List) q.execute(new Long(lVal));
        ArrayList al = new ArrayList(result);
        for (int i = 0; i < al.size(); i++) {
            KMaster kMaster = (KMaster) al.get(i);
            if (i == 0) {
                assertEquals("m1", kMaster.getVal());
                assertEquals(2, kMaster.getDetails().size());
            } else {
                assertEquals("m2", kMaster.getVal());
                assertEquals(0, kMaster.getDetails().size());
            }
        }
        pm.close();
    }

    public void testSizeOnEmptyQuery() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        VersantQuery q = (VersantQuery) pm.newQuery(Address.class);
        q.setCountStarOnSize(true);
        q.declareParameters("String param");
        q.setFilter("street == param");
        List result = (List) q.execute(val);
        if (result.iterator().hasNext()) {
            System.out.println("has more");
        } else {
            System.out.println("No more");
        }
        q.closeAll();


        result = (List) q.execute(val);
        result.size();
        q.closeAll();

        pm.close();
    }

    /**
     * Make sure that the caching override on VersantQuery works for a JDOQL
     * query.
     */
    public void testCacheableJDOQL() {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Address a = new Address("somewhere");
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        // Note that in these tests a new VersantQuery must be created instead
        // of just re-executing the existing query as the results are
        // effectively cached in the local PM as the query is resolved.

        // make sure JDOQL query is cached by default
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Address.class,
                "street == 'somewhere'");
        findExecQuerySQL();
        new ArrayList((Collection)q.execute());
        q.closeAll();
        assertNotNull(findExecQuerySQL());
        q = (VersantQuery)pm.newQuery(q);
        new ArrayList((Collection)q.execute());
        q.closeAll();
        assertNull(findExecQuerySQL());
        pm.currentTransaction().commit();

        // make sure JDOQL query result is not cached when override is used
        pm.currentTransaction().begin();
        q = (VersantQuery)pm.newQuery(q);
        q.setCacheable(false);
        findExecQuerySQL();
        new ArrayList((Collection)q.execute());
        q.closeAll();
        assertNotNull(findExecQuerySQL());
        new ArrayList((Collection)q.execute());
        q.closeAll();
        assertNotNull(findExecQuerySQL());
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testDateFilterQuery() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DATE, 1);
        Date d = cal.getTime();
        System.out.println("d = " + d);

        DateClass p1 = new DateClass();
        p1.setDate(cal.getTime());
        pm.makePersistent(p1);

        DateClass p2 = new DateClass();
        cal.set(Calendar.DATE, 2);
        p2.setDate(cal.getTime());
        pm.makePersistent(p2);

        DateClass p3 = new DateClass();
        cal.set(Calendar.DATE, 2);
        p3.setDate(cal.getTime());
        pm.makePersistent(p3);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(DateClass.class);
        q.declareParameters("java.util.Date param");
        q.setFilter("param >= date");
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n BEFORE EXECUTE");
        Collection col = (Collection) q.execute(d);
        Assert.assertEquals(1, col.size());
        pm.close();
    }

    public void testOJ1() {
    	if (!isUnboundVariableSupported())
    		return;

    	deleteAllPersons();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String var = "" + System.currentTimeMillis();
        Collection col = new ArrayList();

        for (int i = 0; i < 3; i++) {
            Person p = new Person();
            col.add(p);
            Address ad = new Address(var);
            col.add(ad);
            p.setAddress(ad);
        }

        for (int i = 0; i < 3; i++) {
            Person p = new Person();
            col.add(p);
            p.setPerson(p);
        }

        pm.makePersistentAll(col);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.declareVariables("Person comp");
        q.setFilter(
                "this.address.street == \"" + var + "\"  || comp.person == this");
        List result = (List)q.execute();
        long timeStart = System.currentTimeMillis();
        Assert.assertEquals(6, result.size());
        System.out.println(
                "Time = " + (System.currentTimeMillis() - timeStart));

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOJ2() {
    	if (!isUnboundVariableSupported())
    		return;

    	deleteAllPersons();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String var = "" + System.currentTimeMillis();
        Collection col = new ArrayList();

        for (int i = 0; i < 3; i++) {
            Person p = new Person();
            col.add(p);
            Address ad = new Address(var);
            col.add(ad);
            p.setAddress(ad);
        }

        for (int i = 0; i < 3; i++) {
            Person p = new Person();
            col.add(p);
            p.setPerson(p);
        }

        pm.makePersistentAll(col);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.declareVariables("Person comp");
        q.setFilter("this.address.street == \"" + var + "\"  || comp == this");
        List result = (List)q.execute();
        long timeStart = System.currentTimeMillis();
        Assert.assertEquals(6, result.size());
        System.out.println(
                "Time = " + (System.currentTimeMillis() - timeStart));

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testUnboundContainsVar() {
    	if (!isUnboundVariableSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class, "eVar.superList.contains(this)");
        q.declareVariables("EmpSuper eVar");
        List result = (List)q.execute();
        result.size();
        pm.close();
    }

    public void testUnboundContainsVar2() {
    	if (!isUnboundVariableSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        Collection col = new ArrayList();

        pm.currentTransaction().begin();

        ColNuke colNuke = new ColNuke();
        col.add(colNuke);

        Address add1 = new Address();
        col.add(add1);
        colNuke.getList().add(add1);

        Address add2 = new Address();
        col.add(add2);
        colNuke.getList().add(add2);

        Address add3 = new Address();
        col.add(add3);
        colNuke.getList().add(add3);

        pm.makePersistent(colNuke);

        Person p1 = new Person();
        col.add(p1);
        p1.setAddress(add1);

        Person p2 = new Person();
        col.add(p2);
        p2.setAddress(add2);

        Person p3 = new Person();
        col.add(p3);
        p3.setAddress(new Address());

        Person p4 = new Person();
        col.add(p4);
        p4.setAddress(new Address());

        pm.makePersistentAll(col);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class, "eVar.list.contains(this.address)");
        q.declareVariables("ColNuke eVar");
        List result = (List)q.execute();
        Assert.assertEquals(2, result.size());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testBooleanNavigation() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();

        for (int i = 0; i < 3; i++) {
            Person p = new Person(val);
            Address a = new Address();
            a.setBoolVal(true);
            p.setAddress(a);
            pm.makePersistent(p);
        }

        for (int i = 0; i < 3; i++) {
            Person p = new Person(val);
            Address a = new Address();
            a.setBoolVal(false);
            p.setAddress(a);
            pm.makePersistent(p);
        }

        for (int i = 0; i < 3; i++) {
            Person p = new Person(val + "-1");
            Address a = new Address();
            a.setBoolVal(true);
            p.setAddress(a);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);

        q.setFilter("name == nameParam && address.boolVal");
        q.declareParameters("String nameParam");
        List result = (List)q.execute(val);
        int size = result.size();
        Assert.assertEquals(3, size);
        pm.close();
    }

    public void testNtoNQuery() {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        UserAL user1 = new UserAL("user1");
        UserAL user2 = new UserAL("user2");

        GroupAL g1 = new GroupAL("group1");
        GroupAL g2 = new GroupAL("group2");
        GroupAL g3 = new GroupAL("group3");
        GroupAL g4 = new GroupAL("group4");
        GroupAL g5 = new GroupAL("group5");

        user1.add(g1);
        user1.add(g2);
        user1.add(g3);

        user2.add(g2);
        user2.add(g3);
        user2.add(g4);
        user2.add(g5);

        pm.makePersistent(user1);
        pm.makePersistent(user2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        g1.getUsers();
        System.out.println("\n\n\n\n");

        Query q = pm.newQuery(GroupAL.class);
        q.declareParameters("UserAL user, String param");
        q.setOrdering("name ascending");
        q.setFilter("users.contains(user) && name.startsWith(param)");
        List result = (List)q.execute(user1, "group");
        for (int i = 0; i < result.size(); i++) {
            GroupAL g = (GroupAL)result.get(i);
            System.out.println("g.getName() = " + g.getName());
            System.out.println("expected = group" + i + 1);
            Assert.assertEquals("group" + (i + 1), g.getName());
        }
        Assert.assertEquals(3, result.size());
        pm.close();
    }

    public void testQgetAll() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String sValue = "val" + System.currentTimeMillis();

        for (int j = 0; j < 2; j++) {
            ClassWithTreeSet cwts = new ClassWithTreeSet();
            cwts.setsValue(sValue);
            for (int i = 0; i < 3; i++) {
                cwts.gettSet().add(new PCComparable(i));
            }
            pm.makePersistent(cwts);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ClassWithTreeSet.class);
        q.declareParameters("String param");
        q.setFilter("sValue == param");
        ((VersantQuery)q).setFetchGroup("treeSet");
        List result = (List)q.execute(sValue);
        for (int i = 0; i < result.size(); i++) {
            ClassWithTreeSet classWithTreeSet = (ClassWithTreeSet)result.get(i);
            Assert.assertEquals(3, classWithTreeSet.gettSet().size());
        }

        System.out.println("\n\n\n\n\n\n");

        q = pm.newQuery(ClassWithTreeSet.class);
        q.declareParameters("String param");
        q.setFilter("sValue == param");
        ((VersantQuery)q).setFetchGroup("treeSet");
        result = (List)q.execute(sValue);
        for (int i = 0; i < result.size(); i++) {
            ClassWithTreeSet classWithTreeSet = (ClassWithTreeSet)result.get(i);
            Assert.assertEquals(3, classWithTreeSet.gettSet().size());
        }
        pm.close();
    }

    public void testQClosing2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address a1 = new Address();
        a1.setStreet("bla");
        pm.makePersistent(a1);
        a1 = new Address();
        a1.setStreet("bla");
        pm.makePersistent(a1);
        a1 = new Address();
        a1.setStreet("bla");
        pm.makePersistent(a1);

        for (int i = 0; i < 20; i++) {
            a1 = new Address("aaa");
            pm.makePersistent(a1);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        Query q = pm.newQuery(Address.class);
        ((VersantQuery)q).setFetchSize(5);
        q.setFilter("street == sp");
        q.declareParameters("String sp");
        Iterator iter = ((List)q.execute("aaa")).iterator();

        while (iter.hasNext()) {
            iter.next();
            if (!doQuery(pm)) {
                throw new RuntimeException();
            }
        }


        //clean-up
        q = pm.newQuery(Address.class);
        Collection results = (Collection)q.execute();
        pm.deletePersistentAll(results);

        pm.close();
    }

    private boolean doQuery(PersistenceManager pm) {
        Query q = null;
        Address stockLevel = null;
        try {
            q = pm.newQuery(Address.class);
            q.declareParameters("String c");
            q.setFilter("street == c");
            ((VersantQuery)q).setFetchGroup(null);
            ((VersantQuery)q).setMaxRows(1);
            ((VersantQuery)q).setFetchSize(1);

            List lst = (List)q.execute("bla");
            //TODO JACO if this is uncommented it all runs through
            lst.size();
            Iterator itr = lst.iterator();
            if (itr.hasNext()) {
                stockLevel = (Address)itr.next();
            }
            return (stockLevel != null);

        } finally {
            q.closeAll();
        }
    }

    public void testQueryById() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        Address a = new Address();
        p.setAddress(a);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setFilter("address == a");
        q.declareParameters("Address a");
        List results = (List)q.execute(pm.getObjectId(a));
        Assert.assertTrue(p == results.get(0));

        pm.deletePersistent(p);
        pm.deletePersistent(a);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testQueryByAppId() {
        if (!isApplicationIdentitySupported())
        	return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String id1 = "id1" + System.currentTimeMillis();
        String id2 = "id2" + System.currentTimeMillis();
        AppIdString app1 = new AppIdString(id1, "app1");
        AppIdString app2 = new AppIdString(id2, "app2");
        app1.setFriend(app2);
        pm.makePersistent(app1);
        pm.makePersistent(app2);
        pm.currentTransaction().commit();
        System.out.println(
                "---- pm.getObjectId(app1) = " + pm.getObjectId(app1));

        pm.currentTransaction().begin();
        Query q = pm.newQuery(AppIdString.class);
        q.setFilter("friend == f");
        q.declareParameters("AppIdString f");
        List results = (List)q.execute(pm.getObjectId(app2));
        Assert.assertTrue(app1 == results.get(0));

        pm.deletePersistent(app1);
        pm.deletePersistent(app2);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testQueryByAppId2() {
        if (!isApplicationIdentitySupported())
        	return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String id1 = "id1" + System.currentTimeMillis();
        String id2 = "id2" + System.currentTimeMillis();
        AppIdString app1 = new AppIdString(id1, "app1");
        AppIdString app2 = new AppIdString(id2, "app2");
        app1.setFriend(app2);
        pm.makePersistent(app1);
        pm.makePersistent(app2);
        pm.currentTransaction().commit();
        System.out.println(
                "---- pm.getObjectId(app1) = " + pm.getObjectId(app1));

        pm.currentTransaction().begin();
        Query q = pm.newQuery(AppIdString.class);
        ((VersantQuery)q).setFetchGroup("friend");
        q.setFilter("friend == f");
        q.declareParameters("AppIdString f");
        List results = (List)q.execute(pm.getObjectId(app2));
        Assert.assertTrue(app1 == results.get(0));
        Assert.assertTrue(app2 == app1.getFriend());

        pm.deletePersistent(app1);
        pm.deletePersistent(app2);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testQueryByAppId3() {
        if (!isApplicationIdentitySupported())
        	return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String id1 = "id1";
        String id2 = "" + 1;
        String id3 = "id2";
        String id4 = "" + 2;
        CompositeAppIdString app1 = new CompositeAppIdString((short)1, id1,
                "app1");
        CompositeAppIdString app2 = new CompositeAppIdString((short)1, id3,
                "app2");
        app1.setFriend(app2);
        pm.makePersistent(app1);
        pm.makePersistent(app2);
        pm.currentTransaction().commit();
        System.out.println(
                "---- pm.getObjectId(app1) = " + pm.getObjectId(app1));

        pm.currentTransaction().begin();
        Query q = pm.newQuery(CompositeAppIdString.class);
        ((VersantQuery)q).setFetchGroup("friend");
        q.setFilter("friend == f");
        q.declareParameters("AppIdString f");
        List results = (List)q.execute(pm.getObjectId(app2));
        Assert.assertTrue(app1 == results.get(0));
        Assert.assertTrue(app2 == app1.getFriend());

        pm.deletePersistent(app1);
        pm.deletePersistent(app2);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testBackSlachInFilter() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address address = new Address("A String with \\ for example");
        address.setCity("Cape Town");
        pm.makePersistent(address);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query qry = pm.newQuery(Address.class);
        qry.setFilter("street == param");
        qry.declareParameters("String param");
        Iterator it = ((Collection)qry.execute("A String with \\ for example")).iterator();

        if (it.hasNext()) {
            System.out.println("--- FOUND IT ---");
            pm.currentTransaction().commit();
            pm.close();
        } else {
            System.out.println("--- DID NOT FIND IT ---");
            pm.currentTransaction().commit();
            pm.close();
            Utils.fail("Expecting a to find a Address");
        }

    }

    /**
     * Tests that there is no db access for a hollow instance referenced from a
     * dirty instance at commit time.
     */
    public void testRefHollowInstanceFromDirty() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String street = "Street" + System.currentTimeMillis();
        Address address = new Address(street);
        pm.makePersistent(address);
        Object id = pm.getObjectId(address);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        countExecQueryEvents();
        address = (Address)pm.getObjectById(id, false);

        LogEvent[] events = getEvents();
        Assert.assertEquals(0, countExecQueryEvents(events));
        Assert.assertEquals(0,
                countEventsOfType(events, ServerLogEvent.GET_STATE));

        Utils.isHollow(address);
        p.setAddress(address);
        pm.makePersistent(p);
        Utils.isHollow(address);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.setName("bla");
        Utils.isHollow(address);

        pm.deletePersistent(p);
        pm.deletePersistent(address);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testQueryFlush() {
        deleteAllPersons();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        pm.setIgnoreCache(false);

        Assert.assertFalse(open(pm, "00000001"));
        Assert.assertTrue(open(pm, "00000001"));

        Assert.assertFalse(open(pm, "00000002"));
        Assert.assertTrue(open(pm, "00000002"));
        pm.close();
    }

    private boolean open(PersistenceManager pm, String val) {
        Query qry = pm.newQuery(Address.class);
        qry.declareParameters("String v0");
        qry.setFilter("city == v0");
        Iterator it = ((Collection)qry.execute(val)).iterator();

        if (it.hasNext()) {
            System.out.println("--- FOUND IT ---");
            return true;
        } else {
            System.out.println("--- CREATE NEW ONE");
            Address cliente = new Address();
            pm.makePersistent(cliente);
            cliente.setCity(val);
            return false;
        }
    }

    public void testQueryGet() {
//        if (!isQueryOrderingSupported()) return;
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(
                (List)pm.newQuery(NonMutableJavaTypes.class).execute());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        for (int i = 0; i < 200; i++) {
            NonMutableJavaTypes nm = new NonMutableJavaTypes();
            nm.setIntValue(i);
            pm.makePersistent(nm);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setOrdering("intValue ascending");
        List result = (List)q.execute();

        System.out.println("\n\n\n\n ---- start gets");
        Assert.assertEquals(10,
                ((NonMutableJavaTypes)result.get(10)).getIntValue());
        Assert.assertEquals(11,
                ((NonMutableJavaTypes)result.get(11)).getIntValue());
        Assert.assertEquals(12,
                ((NonMutableJavaTypes)result.get(12)).getIntValue());
        Assert.assertEquals(13,
                ((NonMutableJavaTypes)result.get(13)).getIntValue());

        Assert.assertEquals(55,
                ((NonMutableJavaTypes)result.get(55)).getIntValue());
        Assert.assertEquals(56,
                ((NonMutableJavaTypes)result.get(56)).getIntValue());
        Assert.assertEquals(57,
                ((NonMutableJavaTypes)result.get(57)).getIntValue());
        Assert.assertEquals(58,
                ((NonMutableJavaTypes)result.get(58)).getIntValue());

        Assert.assertEquals(100,
                ((NonMutableJavaTypes)result.get(100)).getIntValue());
        Assert.assertEquals(101,
                ((NonMutableJavaTypes)result.get(101)).getIntValue());
        Assert.assertEquals(102,
                ((NonMutableJavaTypes)result.get(102)).getIntValue());
        Assert.assertEquals(103,
                ((NonMutableJavaTypes)result.get(103)).getIntValue());
        System.out.println("---- end gets \n\n\n\n\n");

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(
                (List)pm.newQuery(NonMutableJavaTypes.class).execute());
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testQueryGet1() {
//        if (!isQueryOrderingSupported()) return;
        String db = getSubStoreInfo().getDataStoreType();
        if (db.equals("firebird")) {
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(
                (List)pm.newQuery(NonMutableJavaTypes.class).execute());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            NonMutableJavaTypes nm = new NonMutableJavaTypes();
            nm.setIntValue(i);
            pm.makePersistent(nm);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        ((VersantQuery)q).setFetchSize(1);
        q.setOrdering("intValue ascending");
        List result = (List)q.execute();

        System.out.println("\n\n\n\n ---- start gets");
        for (int i = 0; i < result.size(); i++) {
            Assert.assertEquals(i,
                    ((NonMutableJavaTypes)result.get(i)).getIntValue());
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = pm.newQuery(NonMutableJavaTypes.class);
        ((VersantQuery)q).setFetchSize(1);
        ((VersantQuery)q).setRandomAccess(true);
        q.setOrdering("intValue ascending");
        result = (List)q.execute();

        System.out.println("\n\n\n\n ---- start gets");
        for (int i = 0; i < result.size(); i++) {
            Assert.assertEquals(i,
                    ((NonMutableJavaTypes)result.get(i)).getIntValue());
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(
                (List)pm.newQuery(NonMutableJavaTypes.class).execute());
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testForToManyJoins0() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();

        for (int i = 0; i < 3; i++) {
            SimpleRefB sb = new SimpleRefB();
            sb.setValB1(val);
            SimpleRefA sa = new SimpleRefA();
            sa.setSimpleRefB(sb);
            pm.makePersistent(sa);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SimpleRefA.class);
        q.setFilter("(simpleRefB.valB1 == sp " +
                "|| simpleRefB.valB2 == sp" +
                "|| simpleRefB.valB3 == sp) && simpleRefB.valB1 == sp");
        q.declareParameters("String sp");
        List result = new ArrayList((List)q.execute(val));
        Assert.assertEquals(3, result.size());
        pm.deletePersistentAll(result);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testForToManyJoins() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();

        SimpleRefC sc = new SimpleRefC();
        sc.setValC1(val);
        SimpleRefB sb = new SimpleRefB();
        sb.setValB1(val);
        sb.setSimpleRefC(sc);
        SimpleRefA sa = new SimpleRefA();
        sa.setSimpleRefB(sb);
        pm.makePersistent(sa);

        sc = new SimpleRefC();
        sc.setValC2(val);
        sb = new SimpleRefB();
        sb.setSimpleRefC(sc);
        sb.setValB1(val);
        sa = new SimpleRefA();
        sa.setSimpleRefB(sb);
        pm.makePersistent(sa);

        sc = new SimpleRefC();
        sc.setValC3(val);
        sb = new SimpleRefB();
        sb.setSimpleRefC(sc);
        sb.setValB1(val);
        sa = new SimpleRefA();
        sa.setSimpleRefB(sb);
        pm.makePersistent(sa);

        sc = new SimpleRefC();
        sc.setValC3(val);
        sb = new SimpleRefB();
        sb.setSimpleRefC(sc);
        sb.setValB1(val + "-1");
        sa = new SimpleRefA();
        sa.setSimpleRefB(sb);
        pm.makePersistent(sa);

        sc = new SimpleRefC();
        sc.setValC3(val + "-1");
        sb = new SimpleRefB();
        sb.setSimpleRefC(sc);
        sb.setValB1(val);
        sa = new SimpleRefA();
        sa.setSimpleRefB(sb);
        pm.makePersistent(sa);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Query q = pm.newQuery(SimpleRefA.class);
        q.setFilter(
                "(simpleRefB.simpleRefC.valC1 == sp " +
                "|| simpleRefB.simpleRefC.valC2 == sp" +
                "|| simpleRefB.simpleRefC.valC3 == sp) && simpleRefB.valB1 == sp");
        q.declareParameters("String sp");
        List result = new ArrayList((List)q.execute(val));
        Assert.assertEquals(3, result.size());
        pm.close();
    }

    public void testForToManyJoins22() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val1 = "" + System.currentTimeMillis() + "-1";
        String val2 = "" + System.currentTimeMillis() + "-2";

        SimpleRefA hit1;
        SimpleRefA hit2;

        SimpleRefC sc = new SimpleRefC();
        sc.setValC1(val1);
        SimpleRefB sb = new SimpleRefB();
        sb.setSimpleRefC(sc);
        SimpleRefA sa = hit1 = new SimpleRefA();
        sa.setSimpleRefB(sb);
        pm.makePersistent(sa);

        sc = new SimpleRefC();
        sc.setValC1(val2);
        sb = new SimpleRefB();
        sb.setSimpleRefC(sc);
        sa = hit2 = new SimpleRefA();
        sa.setSimpleRefB(sb);
        pm.makePersistent(sa);

        sc = new SimpleRefC();
        sc.setValC1("-1");
        sb = new SimpleRefB();
        sb.setSimpleRefC(sc);
        sa = new SimpleRefA();
        sa.setSimpleRefB(sb);
        pm.makePersistent(sa);

        sc = new SimpleRefC();
        sc.setValC1("-2");
        sb = new SimpleRefB();
        sb.setSimpleRefC(sc);
        sa = new SimpleRefA();
        sa.setSimpleRefB(sb);
        pm.makePersistent(sa);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SimpleRefA.class);
        q.setFilter("this.simpleRefB.simpleRefC.valC1 == sp1 " +
                "|| this.simpleRefB.simpleRefC.valC1 == sp2");
        q.declareParameters("String sp1, String sp2");
        List result = new ArrayList((List)q.execute(val1, val2));
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains(hit1));
        Assert.assertTrue(result.contains(hit2));
        pm.close();
    }

    public void testForToManyJoins222() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val1 = "" + System.currentTimeMillis() + "-1";
        String val2 = "" + System.currentTimeMillis() + "-2";

        SimpleRefD sd = new SimpleRefD();
        sd.setValD1(val1);
        SimpleRefC sc = new SimpleRefC(sd);
        SimpleRefB sb = new SimpleRefB(sc);
        SimpleRefA sa = new SimpleRefA(sb);
        pm.makePersistent(sa);

        sd = new SimpleRefD();
        sd.setValD1(val2);
        sc = new SimpleRefC(sd);
        sb = new SimpleRefB(sc);
        sa = new SimpleRefA(sb);
        pm.makePersistent(sa);

        sd = new SimpleRefD();
        sd.setValD1("-1");
        sc = new SimpleRefC(sd);
        sb = new SimpleRefB(sc);
        sa = new SimpleRefA(sb);
        pm.makePersistent(sa);

        sd = new SimpleRefD();
        sd.setValD1("-2");
        sc = new SimpleRefC(sd);
        sb = new SimpleRefB(sc);
        sa = new SimpleRefA(sb);
        pm.makePersistent(sa);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SimpleRefA.class);
        q.setFilter("this.simpleRefB.simpleRefC.refD.valD1 == sp1 " +
                "|| this.simpleRefB.simpleRefC.refD.valD1 == sp2");
        q.declareParameters("String sp1, String sp2");
        List result = new ArrayList((List)q.execute(val1, val2));
        Assert.assertEquals(2, result.size());
        pm.close();
    }

    public void testForToManyJoins2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(SimpleRefA.class);
        q.setFilter("simpleRefB.valB1 == sp " +
                "|| simpleRefB.valB2 == sp" +
                "|| simpleRefB.valB3 == sp");
        q.declareParameters("String sp");
        List result = new ArrayList((List)q.execute("bla"));
        result.size();
        pm.close();
    }

    public void testForToManyJoins3() {
        PersistenceManager pm = pmf().getPersistenceManager();
        String s = "" + System.currentTimeMillis();
        pm.currentTransaction().begin();
        SimpleRefA sa = new SimpleRefA();
        SimpleRefB sb = new SimpleRefB();
        sb.setValB1(s);
        sb.setValB2(s);
        sb.setValB3(s);

        SimpleRefC sc = new SimpleRefC();
        sc.setValC1(s);
        sa.setSimpleRefB(sb);
        sa.setSimpleRefC(sc);
        pm.makePersistent(sa);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SimpleRefA.class);
        q.setFilter("simpleRefB.valB1 == sp " +
                "|| simpleRefB.valB2 == sp" +
                "|| simpleRefB.valB3 == sp " +
                "|| simpleRefC.valC1 == sp");
        q.declareParameters("String sp");
        List result = new ArrayList((List)q.execute("bla"));
        result.size();
        pm.close();
    }

    public void testCollectionParam1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("street");
        Address a = new Address();
        a.setCity("Durban");
        p.setAddress(a);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setFilter("name == s && c.contains(address.city) && name == s");
        q.declareImports("import java.util.Collection;");
        q.declareParameters("Collection c, String s");
        List l = new ArrayList();
        l.add("Cape Town");
        l.add("Durban");
        List result = (List)q.execute(l, "street");
        result.size();
        System.out.println("result = " + result);

        result = (List)q.execute(l, null);
        result.size();

        result = (List)q.execute(l, "ss");
        result.size();

        l.add("Bla1");
        result = (List)q.execute(l, "street");
        result.size();
        System.out.println("result = " + result);

        l.remove(2);
        l.remove(0);
        result = (List)q.execute(l, "street");
        result.size();
        System.out.println("result = " + result);

        pm.close();
    }

    public void testCollectionParam2() {
    	if (!isApplicationIdentitySupported())
    		return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ComplexAppId complexAppId = new ComplexAppId();
        complexAppId.setbID(true);
        complexAppId.setcID(true);
        complexAppId.setlID(new Locale("US", ""));
        complexAppId.setsID("" + System.currentTimeMillis());
        pm.makePersistent(complexAppId);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(ComplexAppIdHolder.class);
        q.declareParameters("Collection c");
        q.setFilter("c.contains(complexAppId)");
        Collection col = new ArrayList();
        col.add(complexAppId);
        try {
            List result = (List)q.execute(col);
            Utils.fail("Expecting a JDOUserException");
        } catch (JDOUserException e) {
            //expected
        }
        pm.close();
    }

    public void testCollectionParam3() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("street");
        p.setIntField(2);
        Address a = new Address();
        a.setCity("Durban");
        p.setAddress(a);
        pm.makePersistent(p);
        Address a2 = new Address();
        a2.setStreet("s2");
        pm.makePersistent(a2);

        Person p2 = new Person("street");
        p2.setIntField(2);
        pm.makePersistent(p2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.declareImports("import java.util.Collection;");
        q.setFilter("name == s && c.contains(address) && name == s");
        q.declareParameters("Collection c, String s");
        List l = new ArrayList();
        l.add(a);
        l.add(a2);
        List result = (List)q.execute(l, "street");
        result.size();
        Assert.assertEquals(1, result.size());
        System.out.println("result = " + result);

        result = (List)q.execute(l, null);
        result.size();
        result.size();
        Assert.assertEquals(0, result.size());

        result = (List)q.execute(l, "ss");
        result.size();
        Assert.assertEquals(0, result.size());

        result = (List)q.execute(l, "street");
        result.size();
        Assert.assertEquals(1, result.size());

        l.remove(1);
        result = (List)q.execute(l, "street");
        result.size();
        Assert.assertEquals(1, result.size());
        System.out.println("result = " + result);

        pm.close();
    }

    public void testFlushOnHashSet() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();

        Person p1 = new Person("p1");
        Person p2 = new Person("p2");
        pm.makePersistent(p1);
        pm.makePersistent(p2);

        Person root = new Person("root");
        pm.makePersistent(root);
        root.getPersonsSet().add(p1);

//        Extent ex = pm.getExtent(Person.class, false);
//        ex.iterator();
        Query q = pm.newQuery(Person.class);
        List result = (List)q.execute();
        result.isEmpty();

        root.getPersonsSet().add(p2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(2, root.getPersonsSet().size());
        Assert.assertTrue(root.getPersonsSet().contains(p1));
        Assert.assertTrue(root.getPersonsSet().contains(p2));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(p1);
        pm.deletePersistent(p2);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testFlushOnTreeSet() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();

        PCComparable p1 = new PCComparable(1);
        PCComparable p2 = new PCComparable(2);
        pm.makePersistent(p1);
        pm.makePersistent(p2);

        ClassWithTreeSet root = new ClassWithTreeSet();
        pm.makePersistent(root);
        root.gettSet().add(p1);

        Query q = pm.newQuery(ClassWithTreeSet.class);
        List result = (List)q.execute();
        result.isEmpty();

        System.out.println("root.gettSet() = " + root.gettSet());

        root.gettSet().add(p2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(2, root.gettSet().size());
        Assert.assertTrue(root.gettSet().contains(p1));
        Assert.assertTrue(root.gettSet().contains(p2));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(p1);
        pm.deletePersistent(p2);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDateFieldWithDateParam() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(DateClass.class);
        q.setFilter("this.date < date");
        q.declareParameters("java.util.Date date");
        q.setOrdering("date descending");
        List result = (List)q.execute(new Date());
        System.out.println("result.size() = " + result.size());
        pm.close();
    }

    public void testQSubList() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        char[] chars = new char[]{'a'};
        for (int i = 0; i < 15; i++) {
            Person p = new Person(String.valueOf(chars));
            chars[0]++;
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setOrdering("name ascending");
        List result = (List)q.execute();
        Assert.assertEquals(15, result.size());
        Assert.assertEquals("a", ((Person)result.get(0)).getName());
        Assert.assertEquals("j", ((Person)result.get(9)).getName());
        Assert.assertEquals("o", ((Person)result.get(14)).getName());
        List result2 = result.subList(0, 10);
        Assert.assertEquals(10, result2.size());
        Assert.assertEquals("a", ((Person)result2.get(0)).getName());
        Assert.assertEquals("b", ((Person)result2.get(1)).getName());
        Assert.assertEquals("j", ((Person)result2.get(9)).getName());
        result2 = result.subList(2, 10);
        Assert.assertEquals(8, result2.size());
        Assert.assertEquals("c", ((Person)result2.get(0)).getName());
        Assert.assertEquals("d", ((Person)result2.get(1)).getName());
        Assert.assertEquals("j", ((Person)result2.get(7)).getName());
        result2 = result.subList(2, 2);
        Assert.assertEquals(0, result2.size());
        try {
            Assert.assertEquals("c", ((Person)result2.get(0)).getName());
            Utils.fail("Expected ArrayIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //ignore
        }
        pm.close();


    }

    public void testLiteralBoolean() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query dq = pm.newQuery(NonMutableJavaTypes.class);
        pm.deletePersistentAll((List)dq.execute());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        NonMutableJavaTypes nm = new NonMutableJavaTypes();
        nm.setBooleanValue(true);
        pm.makePersistent(nm);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setFilter("booleanValue == true");
        List result = (List)q.execute();
        System.out.println("result.size() = " + result.size());
        Assert.assertEquals(1, result.size());

        pm.deletePersistent(nm);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testNotOnBooleanField() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query dq = pm.newQuery(NonMutableJavaTypes.class);
        pm.deletePersistentAll((List)dq.execute());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        NonMutableJavaTypes nm = new NonMutableJavaTypes();
        nm.setBooleanValue(true);
        pm.makePersistent(nm);
        nm = new NonMutableJavaTypes();
        nm.setBooleanValue(false);
        pm.makePersistent(nm);
        nm = new NonMutableJavaTypes();
        nm.setBooleanValue(false);
        pm.makePersistent(nm);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setFilter("booleanValue == false && !booleanValue");
        List result = (List)q.execute();
        System.out.println("result.size() = " + result.size());
        Assert.assertEquals(2, result.size());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = pm.newQuery(NonMutableJavaTypes.class);
        pm.deletePersistentAll((Collection)q.execute());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testNotOnBooleanField1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes nm = new NonMutableJavaTypes();
        nm.setBooleanValue(true);
        pm.makePersistent(nm);
        nm = new NonMutableJavaTypes();
        nm.setBooleanValue(true);
        pm.makePersistent(nm);
        nm = new NonMutableJavaTypes();
        nm.setBooleanValue(false);
        pm.makePersistent(nm);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setFilter("booleanValue == true && booleanValue");
        List result = (List)q.execute();
        System.out.println("result.size() = " + result.size());
        Assert.assertEquals(2, result.size());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = pm.newQuery(NonMutableJavaTypes.class);
        pm.deletePersistentAll((Collection)q.execute());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testReQueryOfDeletedInstanceInsameTx() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        long time = System.currentTimeMillis();
        Person p = new Person("" + time);
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);

        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        pm.deletePersistent(p);

        Query q = pm.newQuery(Person.class);
        q.setFilter("name == param");
        q.declareParameters("String param");

        List l = (List)q.execute("" + time);
        Assert.assertEquals(0, l.size());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testFGJoinWithFilterJoins() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Employee emp = new Employee();
        ModelFGTest p = new ModelFGTest();
        p.setEmployee(emp);
        p.setAddress(new Address("street2"));
        Person person = new Person();
        person.setAddress(new Address(null));
        p.setPerson(person);
        p.setA("A");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(ModelFGTest.class);
        q.setFilter("person.address.street == null");
        q.setFetchGroup("testFG");
        System.out.println(
                "((Collection)q.execute()).size() = " + ((Collection)q.execute()).size());
        pm.currentTransaction().commit();

        //clean-up
        pm.currentTransaction().begin();
        pm.deletePersistent(emp);
        pm.deletePersistent(p.getAddress());
        pm.deletePersistent(p);
        pm.deletePersistent(person.getAddress());
        pm.deletePersistent(person);
        pm.currentTransaction().commit();
        pm.close();

    }

    public void testFGJoinWithFilterJoinsConCurUpdate() throws Exception {
        if (!isDatastoreTxLockingNoneSupported()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        ((VersantPersistenceManager)pm).setDatastoreTxLocking(
                VersantPersistenceManager.LOCKING_NONE);
        //create an address
        pm.currentTransaction().begin();
        ModelFGTest p = new ModelFGTest();
        p.setAddress(new Address(null));
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(ModelFGTest.class);
        q.setFilter("address.street == null");
        q.setFetchGroup("testFG");
        System.out.println("\n%%% querying data");
        List l = (List)q.execute();
        assertEquals(1, l.size());
        p = (ModelFGTest)l.get(0);
        System.out.println("%%% pm.getObjectId(p) = " + pm.getObjectId(p));

        changePerson(q);

        System.out.println("\n%%% changing data in other tx");
        p.setA("a3");
        try {
            pm.currentTransaction().commit();
            Utils.fail(
                    "The commit should through an concurrent update exception");
        } catch (Exception e) {
            if (e instanceof JDOOptimisticVerificationException) {
                // good
            } else {
                e.printStackTrace(System.out);
                fail("Expected JDOOptimisticVerificationException: " + e);
            }
        }

        pm.close();
    }

    private void changePerson(VersantQuery query) {
        System.out.println("\n%%% changing data");
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(query);
        ModelFGTest p = (ModelFGTest)((List)q.execute()).get(0);
        System.out.println("%%% pm.getObjectId(p) = " + pm.getObjectId(p));
        p.setA("a2");
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testQWithNewPCInstanceParamsAndIgnoreCache() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String name = "" + System.currentTimeMillis();
        Person p = new Person(name);
        Person friend = new Person("friend");
        p.setPerson(friend);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.setIgnoreCache(true);
        Query q = pm.newQuery(Person.class);
        Address add = new Address("street");
        p.setAddress(add);
        pm.makePersistent(add);
        q.declareParameters("Person per, Address addr");
        q.setFilter("person == per && address == addr");
        Collection col = (Collection)q.execute(friend, add);
        Assert.assertEquals(0, col.size());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        col = (Collection)q.execute(friend, add);
        Assert.assertEquals(1, col.size());


        //clean-up
        pm.deletePersistent(p.getPerson());
        pm.deletePersistent(p);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testQWithWronpParams() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class);
        q.declareParameters("String n");
        q.setFilter("street == n");
        Object[] a = new Object[]{"street"};
        try {
            List l = (List)q.execute(a);
            Utils.fail("Expected exception");
        } catch (JDOUserException e) {
            //expected
        }
        pm.close();
    }

    public void testQByFG() throws Exception {
//        nuke(PrestoreModel.class);
//        nuke(ModelFGTest.class);
//        nuke(Address.class);
//        nuke(ModelFGTest.class);
//        nuke(Person.class);

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Person p = new Person("name1");
        p.setAddress(new Address("street"));
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().setNontransactionalRead(true);
        VersantQuery q = (VersantQuery)pm.newQuery(Person.class);
        q.setFilter("name == p");
        q.declareParameters("String p");
        q.setFetchGroup("testfg1");
        List l = (List)q.execute("name1");
        Assert.assertEquals(1, l.size());
        p = (Person)l.get(0);
        Assert.assertEquals("street", p.getAddress().getStreet());
        Assert.assertEquals("name1", p.getName());

        pm.currentTransaction().begin();
        pm.deletePersistent(p.getAddress());
        pm.deletePersistent(p);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * These tests is to test the flushing of a sco collection when a query is executed.
     * The issue was that the second flush tried to insert the already inserted data again.
     */
    public void testQDirtyList() throws Exception {
        nuke(ListModel.class);
        nuke(PCCollectionEntry.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();

        ListModel lModel = new ListModel();
        pm.makePersistent(lModel);

        PCCollectionEntry p1 = new PCCollectionEntry(331);
        PCCollectionEntry p2 = new PCCollectionEntry(332);
        lModel.getOrderedList().add(p1);
        lModel.getOrderedList().add(p2);

        Query q = pm.newQuery(ListModel.class);
        ((Collection)q.execute()).size();
        q.closeAll();

        PCCollectionEntry p3 = new PCCollectionEntry(333);
        lModel.getOrderedList().add(p3);

        q = pm.newQuery(ListModel.class);
        Assert.assertEquals(1, ((Collection)q.execute()).size());
        Assert.assertEquals(3, lModel.getOrderedList().size());
        q.closeAll();
        pm.close();

    }

    public void testQDirtyList1() throws Exception {
        nuke(PCCollectionEntry.class);
        nuke(ListModel.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();

        ListModel lModel = new ListModel();
        pm.makePersistent(lModel);

        PCCollectionEntry p1 = new PCCollectionEntry(331);
        PCCollectionEntry p2 = new PCCollectionEntry(332);
        lModel.getaList().add(p1);
        lModel.getaList().add(p2);

        Query q = pm.newQuery(ListModel.class);
        ((Collection)q.execute()).size();
        q.closeAll();

        PCCollectionEntry p3 = new PCCollectionEntry(333);
        lModel.getaList().add(p3);

        q = pm.newQuery(ListModel.class);
        Assert.assertEquals(1, ((Collection)q.execute()).size());
        Assert.assertEquals(3, lModel.getaList().size());
        q.closeAll();
        pm.close();
    }

    public void testQDirtyList3() throws Exception {
        nuke(PCCollectionEntry.class);
        nuke(ListModel.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();

        ListModel lModel = new ListModel();
        pm.makePersistent(lModel);

        String p1 = "s1";
        String p2 = "s2";
        lModel.getVector().add(p1);
        lModel.getVector().add(p2);

        Query q = pm.newQuery(ListModel.class);
        ((Collection)q.execute()).size();
        q.closeAll();

        String p3 = "s3";
        lModel.getVector().add(p3);

        q = pm.newQuery(ListModel.class);
        Assert.assertEquals(1, ((Collection)q.execute()).size());
        Assert.assertEquals(3, lModel.getVector().size());
        q.closeAll();
        pm.close();
    }

    public void testQDirtyList2() throws Exception {
        nuke(PCCollectionEntry.class);
        nuke(ListModel.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();

        ListModel lModel = new ListModel();
        pm.makePersistent(lModel);

        PCCollectionEntry p1 = new PCCollectionEntry(331);
        PCCollectionEntry p2 = new PCCollectionEntry(332);
        lModel.getlList2().add(p1);
        lModel.getlList2().add(p2);

        Query q = pm.newQuery(ListModel.class);
        ((Collection)q.execute()).size();
        q.closeAll();

        PCCollectionEntry p3 = new PCCollectionEntry(333);
        lModel.getlList2().add(p3);

        q = pm.newQuery(ListModel.class);
        Assert.assertEquals(1, ((Collection)q.execute()).size());
        Assert.assertEquals(3, lModel.getlList2().size());
        q.closeAll();
        pm.close();
    }

    public void testSCOCollectionFlush3() throws Exception {
        nuke(PCCollectionEntry.class);
        nuke(MapModel.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();

        MapModel mModel = new MapModel();
        pm.makePersistent(mModel);

        mModel.getBasicMap().put("K1", "V1");
        mModel.getBasicMap().put("K2", "V2");

        Query q = pm.newQuery(MapModel.class);
        ((Collection)q.execute()).size();
        Assert.assertEquals(1, ((Collection)q.execute()).size());
        Assert.assertEquals(2, mModel.getBasicMap().size());
        q.closeAll();

        mModel.getBasicMap().put("K3", "V3");

        q = pm.newQuery(MapModel.class);
        Assert.assertEquals(1, ((Collection)q.execute()).size());
        Assert.assertEquals(3, mModel.getBasicMap().size());
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        mModel.getBasicMap().put("K4", "V4");

        q = pm.newQuery(MapModel.class);
        Assert.assertEquals(1, ((Collection)q.execute()).size());
        Assert.assertEquals(4, mModel.getBasicMap().size());
        q.closeAll();

        pm.close();
    }

//=============================== end of testQDirtyListXXX =============================================================

    public void testQClosing() throws Exception {
        if (isRemote()) {
            unsupported();
            return;
        }
        
        pmf().evictAll();
        PersistenceManager pm = pmf().getPersistenceManager();

        assertFalse(hasDatastoreConnection(pm));
        assertEquals(0, getOpenQueryResultCount(pm));

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Address a = new Address();
            a.setStreet("bla" + i);
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Address.class);
        q.setFetchSize(5);
        List l = (List)q.execute();
        l.get(0);
        assertTrue(hasDatastoreConnection(pm));
        assertEquals(1, getOpenQueryResultCount(pm));
        q.closeAll();

        assertEquals(isConnectionPinnedInOptTx() ? true : false,
                hasDatastoreConnection(pm));
        assertEquals(0, getOpenQueryResultCount(pm));
        pm.close();
    }

    public void testStrictTx() throws Exception {
        nuke(PrestoreModel.class);
        nuke(Person.class);
        nuke(Address.class);
        PersistenceManager pm1 = pmf().getPersistenceManager();
        pm1.currentTransaction().setOptimistic(true);
        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().setOptimistic(true);

        pm1.currentTransaction().begin();
        for (int i = 0; i < 5; i++) {
            Address a = new Address("street" + i);
            pm1.makePersistent(a);
        }
        pm1.currentTransaction().commit();

        pm1.currentTransaction().begin();
        pm2.currentTransaction().begin();

        Address a1 = testStrictGet(pm1);
        Object id1 = pm1.getObjectId(a1);

        Address a2 = testStrictGet(pm2);
        Object id2 = pm2.getObjectId(a2);

        Assert.assertEquals(id1.toString(), id2.toString());

        a1.setStreet("changed");
        pm1.currentTransaction().commit();
        pm1.currentTransaction().begin();

        Assert.assertEquals("changed", a1.getStreet());

        pm2.currentTransaction().commit();
        pm2.currentTransaction().begin();
        Assert.assertEquals("changed", a2.getStreet());

        pm1.close();
        pm2.close();

    }

    private Address testStrictGet(PersistenceManager pm1) {
        Assert.assertTrue(pm1.currentTransaction().getOptimistic());
        Query q1 = pm1.newQuery(Address.class);
        q1.setOrdering("street ascending");
        List r1 = (List)q1.execute();
        Assert.assertEquals(5, r1.size());
        Assert.assertEquals("street0", ((Address)r1.get(0)).getStreet());
        return (Address)r1.get(0);
    }

    public void testQMaxResults() throws Exception {
        if (getDbName().equals("sybase")) {
            unsupported();
            return;
        }
        pmf().evictAll();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 50; i++) {
            NonMutableJavaTypes n = new NonMutableJavaTypes();
            n.setIntValue(i);
            pm.makePersistent(n);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(NonMutableJavaTypes.class);
        q.setMaxRows(3);
        List l = (List)q.execute();
        Assert.assertEquals(3, l.size());

        assertEquals(isConnectionPinnedInOptTx() ? true : false, hasDatastoreConnection(pm));

        q.closeAll();
        pm.close();
    }

    public void testQMaxResults2() throws Exception {
        nuke(NonMutableJavaTypes.class);
        pmf().evictAll();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 50; i++) {
            NonMutableJavaTypes n = new NonMutableJavaTypes();
            n.setIntValue(i);
            pm.makePersistent(n);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(NonMutableJavaTypes.class);
        q.setOrdering("intValue ascending");
        q.setMaxRows(3);
        q.setFetchSize(5);
        List l = (List)q.execute();
        Assert.assertEquals(2, ((NonMutableJavaTypes)l.get(2)).getIntValue());
        Assert.assertEquals(0, ((NonMutableJavaTypes)l.get(0)).getIntValue());
        Assert.assertEquals(1, ((NonMutableJavaTypes)l.get(1)).getIntValue());

        assertEquals(isConnectionPinnedInOptTx() ? true : false, hasDatastoreConnection(pm));
        try {
            Assert.assertEquals(3,
                    ((NonMutableJavaTypes)l.get(3)).getIntValue());
        } catch (IndexOutOfBoundsException e) {
            //expected
        }

        q.closeAll();
        pm.close();
    }

    public void testMultipleQClose() throws Exception {
        nuke(NonMutableJavaTypes.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 50; i++) {
            NonMutableJavaTypes n = new NonMutableJavaTypes();
            n.setIntValue(i);
            pm.makePersistent(n);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        Object r1 = q.execute();
        Object r2 = q.execute();
        Object r3 = q.execute();
        Object r4 = q.execute();
        Object r5 = q.execute();
        Object r6 = q.execute();
        Object r7 = q.execute();
        Object r8 = q.execute();

        Collection c1 = (Collection)r1;
        Collection c2 = (Collection)r2;
        Collection c3 = (Collection)r3;
        Collection c4 = (Collection)r4;
        Collection c5 = (Collection)r5;
        Collection c6 = (Collection)r6;
        Collection c7 = (Collection)r7;
        Collection c8 = (Collection)r8;

        Iterator iter1 = c1.iterator();
        Iterator iter2 = c2.iterator();
        Iterator iter3 = c3.iterator();
        Iterator iter4 = c4.iterator();
        Iterator iter5 = c5.iterator();
        Iterator iter6 = c6.iterator();
        Iterator iter7 = c7.iterator();
        Iterator iter8 = c8.iterator();

        Assert.assertTrue(iter4.hasNext());
        Assert.assertTrue(iter1.hasNext());
        q.close(r4);
        Assert.assertTrue(!iter4.hasNext());
        Assert.assertTrue(iter1.hasNext());

        q.closeAll();
        Assert.assertTrue(!iter1.hasNext());
        Assert.assertTrue(!iter2.hasNext());
        Assert.assertTrue(!iter3.hasNext());
        Assert.assertTrue(!iter4.hasNext());
        Assert.assertTrue(!iter5.hasNext());
        Assert.assertTrue(!iter6.hasNext());
        Assert.assertTrue(!iter7.hasNext());
        Assert.assertTrue(!iter8.hasNext());
        pm.close();
    }

    public void testQCache() throws Exception {
        if (!Utils.cacheEnabled()) return;
        nuke(PrestoreModel.class);
        nuke(Address.class);
        pmf().evictAll();
        PersistenceManager pm = pmf().getPersistenceManager();

        assertFalse(hasDatastoreConnection(pm));
        assertEquals(0, getOpenQueryResultCount(pm));

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Address a = new Address();
            a.setStreet("bla" + i);
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertFalse(hasDatastoreConnection(pm));
        assertEquals(0, getOpenQueryResultCount(pm));
        Query q = pm.newQuery(Address.class);
        List l = (List)q.execute();
        l.size();
        assertEquals(isConnectionPinnedInOptTx() ? true : false, hasDatastoreConnection(pm));
        assertEquals(0, getOpenQueryResultCount(pm));
        Assert.assertEquals(10, l.size());
        q.closeAll();
        Utils.checkQCacheSize(1, pm);
        assertEquals(isConnectionPinnedInOptTx() ? true : false, hasDatastoreConnection(pm));
        assertEquals(0, getOpenQueryResultCount(pm));

        q = pm.newQuery(Address.class);
        l = (List)q.execute();
        l.size();
        assertEquals(isConnectionPinnedInOptTx() ? true : false, hasDatastoreConnection(pm));
        assertEquals(0, getOpenQueryResultCount(pm));
        q.closeAll();
        Utils.checkQCacheSize(1, pm);
        pm.close();
    }

    public void testQCache2() throws Exception {
        if (!Utils.cacheEnabled()) return;
        nuke(Address.class);
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Address a = new Address();
            a.setStreet("bla" + i);
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class);
        List l = (List)q.execute();
        l.size();
        Assert.assertEquals(10, l.size());
        q.closeAll();
        Utils.checkQCacheSize(1, pm);
        pm.currentTransaction().commit();

        makeNewAddress();

        pm.currentTransaction().begin();
        Utils.checkQCacheSize(0, pm);
        System.out.println("\n*** about to query for 10 + 1");
        q = pm.newQuery(Address.class);
        l = (List)q.execute();
        Assert.assertEquals(11, l.size());
        q.closeAll();
        Utils.checkQCacheSize(1, pm);
        pm.close();
    }

    public void testQCache3() throws Exception {
        if (!Utils.cacheEnabled()) return;
        nuke(Address.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pmf().evictAll();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("person" + i);
            Address a = new Address();
            a.setStreet("bla" + i);
            p.setAddress(a);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();
        Utils.checkQCacheSize(0, pm);

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class, "address.street.startsWith(p)");
        q.declareParameters("String p");
        List l = (List)q.execute("bla");
        l.size();
        Assert.assertEquals(10, l.size());
        q.closeAll();
        Utils.checkQCacheSize(1, pm);

        q = pm.newQuery(Person.class, "address.street.startsWith(p)");
        q.declareParameters("String p");
        l = (List)q.execute("bla");
        Assert.assertEquals(10, l.size());
        q.closeAll();
        Utils.checkQCacheSize(1, pm);
        pm.currentTransaction().commit();

        //edit one of the address of the persons
        //this must remove the cache q results
        editAddress();

        pm.currentTransaction().begin();
        Utils.checkQCacheSize(0, pm);
        q = pm.newQuery(Person.class, "address.street.startsWith(p)");
        q.declareParameters("String p");
        l = (List)q.execute("bla");
        Assert.assertEquals(9, l.size());
        q.closeAll();
        pm.close();
    }

    public void testQCache4() throws Exception {
        if (!Utils.cacheEnabled()) return;

        deleteAllPersons();
        pmf().evictAll();

        VersantPersistenceManagerImp pm = ((PMProxy)pmf().getPersistenceManager()).getRealPM();
        Utils.checkQCacheSize(0, pm);

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("person" + i);
            Address a = new Address();
            a.setStreet("bla" + i);
            p.setAddress(a);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.setIgnoreCache(false);
        Query q = pm.newQuery(Person.class, "address.street.startsWith(p)");
        q.declareParameters("String p");
        ForwardQueryResult qCol1 = (ForwardQueryResult)q.execute("bla");
        qCol1.size();
        Assert.assertEquals(10, qCol1.size());
        q.closeAll();
        Utils.checkQCacheSize(1, pm);

        Query q2 = pm.newQuery(Person.class, "address.street.startsWith(p)");
        q2.declareParameters("String p");
        ForwardQueryResult qCol2 = (ForwardQueryResult)q2.execute("bla");

        Assert.assertTrue(qCol1.isCompiledQueryEqual(qCol2));

        q = pm.newQuery(Person.class, "address.street.startsWith(p)");
        q.declareParameters("String p");
        List l = (List)q.execute("bla");
        Assert.assertEquals(10, l.size());
        q.closeAll();
        Utils.checkQCacheSize(1, pm);

        p.getAddress().setStreet("Edited");

        System.out.println("\n\n\n\n>>>>>>> before last q <<<<<<<<<<<<<");
        q = pm.newQuery(Person.class, "address.street.startsWith(p)");
        q.declareParameters("String p");
        l = (List)q.execute("bla");
        Assert.assertEquals(9, l.size());
        q.closeAll();
        pm.currentTransaction().commit();
        Utils.checkQCacheSize(0, pm);
        pm.close();
    }

    private void editAddress() {
        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().begin();

        Query q = pm2.newQuery(Address.class);
        List l = (List)q.execute();
        Assert.assertEquals(10, l.size());
        Address a = (Address)l.get(0);
        a.setStreet("Edited");
        q.closeAll();
        pm2.currentTransaction().commit();
        pm2.close();
    }

    private void makeNewAddress() {
        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().begin();
        Address a = new Address();
        pm2.makePersistent(a);
        pm2.currentTransaction().commit();
        pm2.close();
    }

    public void testQCache5() throws Exception {
        if (!Utils.cacheEnabled()) return;

        nuke(Person.class);
        nuke(Address.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("person" + i);
            Address a = new Address();
            a.setStreet("bla" + i);
            p.setAddress(a);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.setIgnoreCache(false);
        Query q = pm.newQuery(Person.class, "address.street.startsWith(p)");
        q.declareParameters("String p");
        List l = (List)q.execute("bla");
        l.size();
        Assert.assertEquals(10, l.size());
        q.closeAll();

        q = pm.newQuery(Person.class, "address.street.startsWith(p)");
        q.declareParameters("String p");
        l = (List)q.execute("bla");
        Assert.assertEquals(10, l.size());
        q.closeAll();

        p.getAddress().setStreet("Edited");

        q = pm.newQuery(Person.class, "address.street.startsWith(p)");
        q.declareParameters("String p");
        l = (List)q.execute("bla");
        Assert.assertEquals(9, l.size());
        q.closeAll();
        pm.close();
    }

    /**
     * This is not test that if the class is specifield for no caching that
     * it's query results does no end up in cache.
     *
     * @throws Exception
     */
    public void testQCache6() throws Exception {
        if (!Utils.cacheEnabled()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pmf().evictAll();
        pm.currentTransaction().begin();
        ClassA ca = new ClassA();
        pm.makePersistent(ca);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Utils.checkQCacheSize(0, pm);
        Query q = pm.newQuery(ClassA.class);
        ((List)q.execute()).size();
        Utils.checkQCacheSize(0, pm);
        q.closeAll();

        q = pm.newQuery(ClassAB.class);
        ((List)q.execute()).size();
        Utils.checkQCacheSize(0, pm);
        q.closeAll();

        q = pm.newQuery(ClassAC.class);
        ((List)q.execute()).size();
        Utils.checkQCacheSize(0, pm);
        q.closeAll();

        pm.close();
    }

    public void testQCache7() throws Exception {
        if (!Utils.cacheEnabled()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pmf().evictAll();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        Query q = pm.newQuery(Person.class);
        q.setOrdering("address.street ascending");
        ((List)q.execute()).size();
        Utils.checkQCacheSize(1, pm);
        q.closeAll();

        q = pm.newQuery(Person.class);
        q.setOrdering("address.street ascending");
        ((List)q.execute()).size();
        Utils.checkQCacheSize(1, pm);
        q.closeAll();

        makeNewAddress();
        Utils.checkQCacheSize(0, pm);

        pm.close();
    }

    public void testQCache8() throws Exception {
        if (!Utils.cacheEnabled()) return;

        nuke(Person.class);
        nuke(Address.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Address a = new Address();
            a.setStreet("bla" + i);
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class);
        ((VersantQuery)q).setFetchSize(5);
        q.setOrdering("street ascending");
        List l = (List)q.execute();
        for (int i = 0; i < 5; i++) {
            Address address = (Address)l.get(i);
            Assert.assertEquals("bla" + i, address.getStreet());
        }

        assertTrue(hasDatastoreConnection(pm));
        assertEquals(1, getOpenQueryResultCount(pm));

        Query q2 = pm.newQuery(Address.class);
        ((VersantQuery)q2).setFetchSize(5);
        q2.setOrdering("street ascending");
        List l2 = (List)q2.execute();
        for (int i = 0; i < l2.size(); i++) {
            Address address = (Address)l2.get(i);
            Assert.assertEquals("bla" + i, address.getStreet());
        }

        Utils.checkQCacheSize(1, pm);
        assertTrue(hasDatastoreConnection(pm));
        assertEquals(1, getOpenQueryResultCount(pm));

        for (int i = 5; i < 10; i++) {
            Address address = (Address)l.get(i);
            Assert.assertEquals("bla" + i, address.getStreet());
        }
        Assert.assertEquals(10, l2.size());

        assertEquals(isConnectionPinnedInOptTx() ? true : false, hasDatastoreConnection(pm));
        assertEquals(0, getOpenQueryResultCount(pm));

        q.closeAll();
        q2.closeAll();

        pm.close();
    }

    public void testQCache9() throws Exception {
        if (!Utils.cacheEnabled()) return;

        nuke(Person.class);
        nuke(Address.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pmf().evictAll();
        Utils.checkQCacheSize(0, pm);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Address a = new Address();
            a.setStreet("bla" + i);
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class);
        q.setFilter("street.startsWith(p)");
        q.declareParameters("String p");
        q.setOrdering("street ascending");
        List l = (List)q.execute("bla");
        for (int i = 0; i < 5; i++) {
            Address address = (Address)l.get(i);
            Assert.assertEquals("bla" + i, address.getStreet());
        }
        makeNewAddress();

        for (int i = 5; i < 10; i++) {
            Address address = (Address)l.get(i);
            Assert.assertEquals("bla" + i, address.getStreet());
        }
        try {
            l.get(10);
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        Utils.checkQCacheSize(0, pm);
        q.closeAll();
        pm.close();
    }

    public void testQCache10() throws Exception {
        if (!Utils.cacheEnabled()) return;

        nuke(Person.class);
        nuke(Address.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pmf().evictAll();
        Utils.checkQCacheSize(0, pm);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Address a = new Address();
            a.setStreet("bla" + i);
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class);
        q.setFilter("street.startsWith(p)");
        q.declareParameters("String p");
        q.setOrdering("street ascending");
        List l = (List)q.execute("bla");
        for (int i = 0; i < 5; i++) {
            Address address = (Address)l.get(i);
            Assert.assertEquals("bla" + i, address.getStreet());
        }
        makeNewAddress();

        Query q2 = pm.newQuery(Address.class);
        q2.setFilter("street.startsWith(p)");
        q2.declareParameters("String p");
        q2.setOrdering("street ascending");
        List l2 = (List)q2.execute("bla");
        for (int i = 0; i < 5; i++) {
            Address address = (Address)l2.get(i);
            Assert.assertEquals("bla" + i, address.getStreet());
        }

        for (int i = 5; i < 10; i++) {
            Address address = (Address)l.get(i);
            Assert.assertEquals("bla" + i, address.getStreet());
        }
        Utils.checkQCacheSize(1, pm);
        try {
            l.get(10);
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        Utils.checkQCacheSize(1, pm);
        q.closeAll();

        pm.close();
    }

    public void testQCache11() throws Exception {
        if (!Utils.cacheEnabled()) return;
        nuke(Person.class);
        nuke(Address.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pmf().evictAll();
        Utils.checkQCacheSize(0, pm);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Address a = new Address();
            a.setStreet("bla" + i);
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class);
        ((VersantQuery)q).setFetchSize(5);
        q.setFilter("street.startsWith(p)");
        q.declareParameters("String p");
        q.setOrdering("street ascending");
        List l = (List)q.execute("bla");
        for (int i = 0; i < 5; i++) {
            Address address = (Address)l.get(i);
            Assert.assertEquals("bla" + i, address.getStreet());
        }

        Query q2 = pm.newQuery(Address.class);
        ((VersantQuery)q2).setFetchSize(5);
        q2.setFilter("street.startsWith(p)");
        q2.declareParameters("String p");
        q2.setOrdering("street ascending");
        List l2 = (List)q2.execute("bla");
        for (int i = 0; i < 5; i++) {
            Address address = (Address)l2.get(i);
            Assert.assertEquals("bla" + i, address.getStreet());
        }

        for (int i = 5; i < 10; i++) {
            Address address = (Address)l.get(i);
            Assert.assertEquals("bla" + i, address.getStreet());
        }

        for (int i = 5; i < 10; i++) {
            Address address = (Address)l2.get(i);
            Assert.assertEquals("bla" + i, address.getStreet());
        }

        Utils.checkQCacheSize(1, pm);
        try {
            l.get(10);
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        Utils.checkQCacheSize(1, pm);
        q.closeAll();

        pm.close();
    }

    public void testQueryCache11() throws Exception {
        if (!Utils.cacheEnabled()) return;
        nuke(PrestoreModel.class);
        nuke(Person.class);
        nuke(Address.class);
        pmf().evictAll();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        for (int i = 0; i < 30; i++) {
            Address a = new Address();
            a.setStreet("bla" + i);
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class);
        List l = (List)q.execute();
        l.size();
        Assert.assertEquals(30, l.size());
        q.closeAll();
        Utils.checkQCacheSize(1, pm);

        q = pm.newQuery(Address.class);
        l = (List)q.execute();
        l.size();
        Assert.assertEquals(30, l.size());
        q.closeAll();
        Utils.checkQCacheSize(1, pm);
        pm.close();
    }

    public void testQueryCache12() throws Exception {
        if (!Utils.cacheEnabled()) return;
        nuke(PrestoreModel.class);
        nuke(Person.class);
        nuke(Address.class);
        pmf().evictAll();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        for (int i = 0; i < 30; i++) {
            Address a = new Address();
            a.setStreet("bla" + i);
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Utils.checkQCacheSize(0, pm);
        Query q = pm.newQuery(Address.class);
        List l = (List)q.execute();
        int count = 0;
        Iterator iter = l.iterator();
        while (iter.hasNext()) {
            Object o = (Object)iter.next();
            count++;
        }
        Assert.assertEquals(30, count);
        q.closeAll();
        Utils.checkQCacheSize(1, pm);

        q = pm.newQuery(Address.class);
        l = (List)q.execute();
        l.size();
        Assert.assertEquals(30, l.size());
        q.closeAll();
        Utils.checkQCacheSize(1, pm);
        pm.close();
    }

    public void testQueryCache13() throws Exception {
        if (!Utils.cacheEnabled()) return;
        nuke(PrestoreModel.class);
        nuke(Person.class);
        nuke(Address.class);
        pmf().evictAll();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        for (int i = 0; i < 30; i++) {
            Address a = new Address();
            a.setStreet("bla" + i);
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Utils.checkQCacheSize(0, pm);
        Query q = pm.newQuery(Address.class);
        List l = (List)q.execute();
        int count = 0;
        for (int i = 0; i < 30; i++) {
            Object o = (Object)l.get(i);
            count++;
        }
        Assert.assertEquals(30, count);
        q.closeAll();
        Utils.checkQCacheSize(1, pm);

        q = pm.newQuery(Address.class);
        l = (List)q.execute();
        l.size();
        Assert.assertEquals(30, l.size());
        q.closeAll();
        Utils.checkQCacheSize(1, pm);
        pm.close();
    }

    public void testQueryCache14() throws Exception {
        if (!Utils.cacheEnabled()) return;
        nuke(PrestoreModel.class);
        nuke(Person.class);
        nuke(Address.class);
        pmf().evictAll();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        for (int i = 0; i < 30; i++) {
            Address a = new Address();
            a.setStreet("bla" + i);
            pm.makePersistent(a);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Utils.checkQCacheSize(0, pm);
        VersantQuery q = (VersantQuery)pm.newQuery(Address.class);
        q.setFetchSize(10);
        q.setOrdering("street ascending");
        List l = (List)q.execute();
        for (int i = 0; i < 2; i++) {
            Assert.assertEquals("bla" + i, ((Address)l.get(i)).getStreet());
        }
        Utils.checkQCacheSize(1, pm);

        System.out.println(">>>>>>>>>>>>>> After q");

        VersantQuery q1 = (VersantQuery)pm.newQuery(Address.class);
        q1.setOrdering("street ascending");
        q1.setFetchSize(10);
        Assert.assertEquals("bla" + 0,
                ((Address)((List)q1.execute()).get(0)).getStreet());
        Utils.checkQCacheSize(1, pm);

        System.out.println(">>>>>>>>>>>>>> After q1");

        VersantQuery q2 = (VersantQuery)pm.newQuery(Address.class);
        q2.setOrdering("street ascending");
        q2.setFetchSize(10);
        Assert.assertEquals("bla" + 0,
                ((Address)((List)q2.execute()).get(0)).getStreet());
        Utils.checkQCacheSize(1, pm);

        System.out.println(">>>>>>>>>>>>>> After q2");

        //removed with SM refactor ((PMProxy)pm).getRealPM().jdoConnection.testRemoveStale();
        Utils.checkQCacheSize(1, pm);

        q.closeAll();
        q1.closeAll();
        q2.closeAll();

        pm.close();
    }

    public void testQueryCache15() throws Exception {
        if (!Utils.cacheEnabled()) return;
        nuke(Person.class);
        nuke(Address.class);
        pmf().evictAll();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Address a = new Address("street4346");
        pm.makePersistent(a);
        Object id = pm.getObjectId(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        a = (Address)pm.getObjectById(id, true);
        pm.currentTransaction().commit();

        Utils.checkQCacheSize(0, pm);
        doQ(pm, a);
        Utils.checkQCacheSize(1, pm);
        doQ(pm, a);
        pm.close();
    }

    public void testQueryCache16() throws Exception {
        if (!Utils.cacheEnabled()) return;
        nuke(Person.class);
        pmf().evictAll();
        PersistenceManager pm = pmf().getPersistenceManager();
        Utils.checkQCacheSize(0, pm);

        pm.currentTransaction().setOptimistic(true);
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        long start = System.currentTimeMillis();
        Person p = new Person();
        p.setName("new" + 0);
        pm.makePersistent(p);

        for (int i = 0; i < 30; i++) {
            Query q = pm.newQuery(Person.class);
            q.setFilter("name.startsWith(p)");
            q.declareParameters("String p");
            List l = (List)q.execute("new");
            if (!((Person)l.get(0)).getName().equals("new0")) {
                throw new RuntimeException("Test3 Failed");
            }
            Utils.checkQCacheSize(1, pm);
            q.closeAll();
            Utils.checkQCacheSize(1, pm);
        }

        pm.currentTransaction().rollback();
        pm.close();
    }

    private void doQ(PersistenceManager pm, Address a) {
        pm.currentTransaction().setNontransactionalRead(true);
        Query q = pm.newQuery(Address.class);
        q.setFilter("street == p");
        q.declareParameters("String p");
        Collection l = (Collection)q.execute("street4346");
        Iterator iterator = l.iterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(a == iterator.next());
        q.closeAll();
        Assert.assertTrue(!iterator.hasNext());
    }

    public void testQueryColSerialization() throws Exception {
        nuke(PrestoreModel.class);
        nuke(Person.class);
        nuke(Address.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("name" + i);
            Address a = new Address();
            a.setStreet("bla" + i);
            p.setAddress(a);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        List l = (List)q.execute();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(l);
        ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bout.toByteArray()));
        List l2 = (List)in.readObject();
        System.out.println("l2.getClass() = " + l2.getClass());
        Assert.assertEquals(l, l2);
        pm.close();
    }

    /**
     * Test simple exact match query with a String parameter.
     */
    public void testQueryStringParam() throws Exception {
        if (!isSQLSupported())
        	return;

        if (isRemote()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        String aStreet = "testQueryStringParam";
        Address a = new Address(aStreet);
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class, "street == s");
        q.declareParameters("String s");
        Iterator i = ((Collection)q.execute(a.getStreet())).iterator();
        Assert.assertTrue(i.hasNext());
        Address b = (Address)i.next();
        Assert.assertTrue(!i.hasNext());
        Assert.assertEquals(aStreet, b.getStreet());
        q.closeAll();
        pm.currentTransaction().commit();

        java.sql.Connection con = pmf().getJdbcConnection(null);
        PreparedStatement ps = con.prepareStatement(
                "update address set street=? where street=?");
        ps.setString(1, "boo");
        ps.setString(2, aStreet);
        int uc = ps.executeUpdate();
        ps.close();
        con.commit();
        con.close();
        System.out.println("*** uc = " + uc);

        pm.currentTransaction().begin();
        Assert.assertEquals(a.getStreet(), "boo");
        pm.deletePersistent(a);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testSubOrdering() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person();
            Address a = new Address();
            a.setStreet("bla" + i);
            p.setAddress(a);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setOrdering("address ascending");
        List results = (List)q.execute();
        pm.close();
    }

    /**
     * Test query plan support. This just checks that the SQL comes back.
     */
    public void testQueryPlan() throws Exception {
        if (!isSQLSupported())
        	return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(TwoDate.class,
                "date1 == p1 && date2 == p2");
        q.declareParameters("java.util.Date p1, java.util.Date p2");
        VersantQueryPlan qp = q.getPlan(new Object[2]);
        System.out.println(
                "\nqp.getDatastoreQuery() = \n'" + qp.getDatastoreQuery() + "'");
        System.out.println(
                "\nqp.getDatastorePlan() = \n'" + qp.getDatastorePlan() + "'");
        Assert.assertTrue(qp.getDatastoreQuery() != null);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test filter: 'date1 == p1 && date2 == p2' with p1 and p2 null.
     */
    public void testDateNullAndDateNull() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        TwoDate good = new TwoDate(1, null, null);
        TwoDate bad = new TwoDate(1, new Date(), new Date());
        pm.makePersistent(good);
        pm.makePersistent(bad);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(TwoDate.class, "date1 == p1 && date2 == p2");
        q.declareParameters("java.util.Date p1, java.util.Date p2");
        Collection ans = (Collection)q.execute(null, null);
        Assert.assertEquals(1, ans.size());
        Assert.assertEquals(good, ans.iterator().next());
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(good);
        pm.deletePersistent(bad);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testRAccessQSubList() throws Exception {
        if (!getSubStoreInfo().isScrollableResultSetSupported() &&
            !isVds()) return;

        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("name" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Person.class);
        q.setOrdering("name ascending");
        q.setRandomAccess(true);

        System.out.println("\n\n\n\n\n --- BEFORE QUERY");
        List results = (List)q.execute();
        List supList = results.subList(5, 20);
        Assert.assertEquals(5, supList.size());
        for (int i = 0; i < supList.size(); i++) {
            Person person = (Person)supList.get(i);
            System.out.println("person.getName() = " + person.getName());
            Assert.assertEquals("name" + (i + 5), person.getName());
        }
        pm.close();
    }

    public void testQWithAppIdInstanceAsParam() {
    	if (!isApplicationIdentitySupported())
    		return;
        if (!getSubStoreInfo().isScrollableResultSetSupported() &&
            !isVds()) return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Manual man = new Manual(null);
        pm.makePersistent(man);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Module.class);
        q.setRandomAccess(true);
        q.declareParameters("Manual p");
        q.setFilter("man == p");
        List results = (List)q.execute(man);
        results.size();
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test case for Panagiotis Louridas (panos.louridas@investment-bank.gr).
     * This bombs on other implementations on MySQL.
     */
    public void testPanagiotis() throws Exception {
        nuke(PanR.class);
        nuke(PanA.class);

    	if (!isApplicationIdentitySupported())
    	{
    		unsupported();
    		return;
    	}

    	PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();

        int nabc = 12; // four of each class
        PanA[] abc = new PanA[nabc];
        for (int i = 0; i < nabc; i++) {
            PanA o;
            switch (i % 3) {
                case 0:
                    o = new PanA("A" + i);
                    break;
                case 1:
                    o = new PanB("B" + i);
                    break;
                default:
                    o = new PanC("C" + i);
            }
            pm.makePersistent(abc[i] = o);
        }

        int nr = 16;
        for (int i = 0; i < nr; i++) {
            PanR pr = new PanR();
            int n = i % nabc;
            if (n > 0) pr.setRefA1(abc[n]);
            n = (i * 3 + 1) % nabc;
            if (n > 0) pr.setRefA2(abc[n]);
            pr.setAnotherField(i % 3);
            pr.setYetAnotherField((i + 1) % 3);
            pm.makePersistent(pr);
        }

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(PanR.class);
        q.setOrdering("yetAnotherField ascending");
        ArrayList all = new ArrayList((Collection)q.execute());
        pm.retrieveAll(all);
        q.closeAll();
        System.out.println("\n*** QueryTests.testPanagiotis === PanR list");
        for (int i = 0; i < all.size(); i++) {
            System.out.println("[" + i + "] = " + all.get(i));
        }
        System.out.println("---\n");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        q = pm.newQuery(PanR.class,
                "(refA1.id == myId || refA2.id == myId)\n" +
                "&& anotherField == aValue\n" +
                "&& yetAnotherField == anotherValue");
        q.declareParameters("String myId, int aValue, int anotherValue");
        Collection ans = (Collection)q.execute("B4", new Integer(1),
                new Integer(2));
        checkPanagiotisResult(ans);
        q.closeAll();

        q = pm.newQuery(PanR.class,
                "(refA1== myId || refA2 == myId)\n" +
                "&& anotherField == aValue\n" +
                "&& yetAnotherField == anotherValue");
        q.declareParameters("Object myId, int aValue, int anotherValue");
        ans = (Collection)q.execute(new PanA.ID("B4"), new Integer(1),
                new Integer(2));
        checkPanagiotisResult(ans);
        q.closeAll();

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistentAll(all);
        pm.deletePersistentAll(abc);
        pm.currentTransaction().commit();

        pm.close();
    }

    private void checkPanagiotisResult(Collection ans) {
        int c = 0;
        for (Iterator i = ans.iterator(); i.hasNext(); c++) {
            PanR r = (PanR)i.next();
            System.out.println("[" + c + "] = " + r);
            Assert.assertTrue(r.getRefA1().getId().equals("B4") || r.getRefA2().getId().equals(
                    "B4"));
            Assert.assertEquals(1, r.getAnotherField());
            Assert.assertEquals(2, r.getYetAnotherField());
        }
        Assert.assertEquals(3, c);
    }

    /**
     * Test a query involving an and with an 'exists (select ..' subquery
     * and another ordinary term. The subquery includes a where clause. This
     * tests the normal rewriting path in AndExp for mysql.
     */
    public void testContainsAnd() throws Exception {
        nuke(ColNuke.class);

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ArrayList a = new ArrayList();
        int n = 4;
        for (int i = 0; i < n; i++) {
            ColNuke cn = new ColNuke();
            cn.setAge(i);
            for (int j = 0; j < i; j++) {
                cn.getList().add(new Address("street" + j));
            }
            a.add(cn);
        }
        pm.makePersistentAll(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(ColNuke.class,
                "age == n && list.contains(v) && v.street == s");
        q.declareParameters("int n, String s");
        q.declareVariables("Address v");
        Collection ans = (Collection)q.execute(new Integer(1), "street0");
        Assert.assertEquals(1, ans.size());
        ColNuke cn = (ColNuke)ans.iterator().next();
        Assert.assertEquals(1, cn.getAge());
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        for (Iterator i = a.iterator(); i.hasNext();) {
            pm.deletePersistentAll(((ColNuke)i.next()).getList());
        }
        pm.deletePersistentAll(a);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test a query involving an and with a 'not exists (select ..' subquery
     * and another ordinary term. The subquery includes a where clause. This
     * tests the 'not' rewriting path in AndExp for mysql.
     */
    public void testNotContainsAnd() throws Exception {
        nuke(ColNuke.class);

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ArrayList a = new ArrayList();
        int n = 4;
        for (int i = 0; i < n; i++) {
            ColNuke cn = new ColNuke();
            cn.setAge(i);
            for (int j = 0; j < i; j++) {
                cn.getList().add(new Address("street" + j));
            }
            a.add(cn);
        }
        pm.makePersistentAll(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(ColNuke.class,
                "age == n && !(list.contains(v) && v.street == s)");
        q.declareParameters("int n, String s");
        q.declareVariables("Address v");
        Collection ans = (Collection)q.execute(new Integer(1), "piggy");
        Assert.assertEquals(1, ans.size());
        ColNuke cn = (ColNuke)ans.iterator().next();
        Assert.assertEquals(1, cn.getAge());
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        for (Iterator i = a.iterator(); i.hasNext();) {
            pm.deletePersistentAll(((ColNuke)i.next()).getList());
        }
        pm.deletePersistentAll(a);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * This test checks that the precence of 'not' is handled correctly.
     * All databases except mysql will treat 'not a == b' as 'not (a == b)'.
     * Mysql requires the brackets.
     */
    public void testNotAnd() throws Exception {
        if ("mysql".equals(getDbName())) {
            unsupported();
            return;
        }
        
        nuke(Enterprise.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        String street = "testNotAnd street";
        String name = "testNotAnd";

        //match
        Enterprise ent = new Enterprise(name, new Address(street));
        pm.makePersistent(ent);

        //match
        ent = new Enterprise(name, null);
        pm.makePersistent(ent);

        //match
        ent = new Enterprise(name, new Address(null));
        pm.makePersistent(ent);

        //no-match
        ent = new Enterprise(name, new Address("piggy"));
        pm.makePersistent(ent);

        //no-match
        pm.makePersistent(new Enterprise("n3", new Address(street)));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Enterprise.class,
                "name == n && !(address.street == s)");
        q.declareParameters("String n,String s");
        Collection ans = (Collection)q.execute(name, "piggy");
        Assert.assertEquals(3, ans.size());
//        ent = (Enterprise)ans.iterator().next();
//        Assert.assertEquals(ent.getAddress().getStreet(), street);
//        Assert.assertEquals(ent.getName(), name);
        q.closeAll();
        pm.deletePersistent(ent.getAddress());
        pm.deletePersistent(ent);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * This test checks that the precence of 'not' is handled correctly.
     * All databases except mysql will treat 'not a == b' as 'not (a == b)'.
     * Mysql requires the brackets.
     */
    public void testNotAnd2() throws Exception {
        nuke(Enterprise.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        String street = "testNotAnd street";
        String name = "testNotAnd";

        //match
        Enterprise ent = new Enterprise(name, new Address(street));
        pm.makePersistent(ent);

        //match
        ent = new Enterprise(name, null);
        pm.makePersistent(ent);

        //match
        ent = new Enterprise(name, new Address(null));
        pm.makePersistent(ent);

        //no-match
        ent = new Enterprise(name, new Address("piggy"));
        pm.makePersistent(ent);

        //no-match
        pm.makePersistent(new Enterprise("n3", new Address(street)));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Enterprise.class,
                "name == n && address.street != s");
        q.declareParameters("String n,String s");
        Collection ans = (Collection)q.execute(name, "piggy");
        Assert.assertEquals(1, ans.size());
        ent = (Enterprise)ans.iterator().next();
        Assert.assertEquals(ent.getAddress().getStreet(), street);
        Assert.assertEquals(ent.getName(), name);
        q.closeAll();
        pm.deletePersistent(ent.getAddress());
        pm.deletePersistent(ent);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testExtentForNotIncludeSubclasses() throws Exception {
        System.out.println(
                "\n*** QueryTests.testExtentForNotIncludeSubclasses - sleeping");
          if (isMySQL3()) {
            Thread.sleep(30000); // TODO figure out why this horrible hack is required!!!!
          }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Extent e = pm.getExtent(EmpSuper.class, true);
        Iterator iter = e.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            System.out.println(
                    "\n*** QueryTests.testExtentForNotIncludeSubclasses pm.deletePersistent(o)");
            pm.deletePersistent(o);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            EmpSuper empSuper = new EmpSuper("empSuper" + i);
            col.add(empSuper);
            Employee emp = new Employee("Employee" + i, "" + i);
            col.add(emp);
        }
        pm.makePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Extent extent = pm.getExtent(EmpSuper.class, false);
        iter = extent.iterator();
        int counter = 0;
        while (iter.hasNext()) {
            counter++;
            EmpSuper empSuper = (EmpSuper)iter.next();
        }
        Assert.assertEquals(10, counter);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        extent = pm.getExtent(EmpSuper.class, true);
        iter = extent.iterator();
        counter = 0;
        while (iter.hasNext()) {
            counter++;
            EmpSuper empSuper = (EmpSuper)iter.next();
        }
        Assert.assertEquals(20, counter);
        pm.close();
    }

    public void testCloseExtent() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Extent e = pm.getExtent(Person.class, false);
        Iterator i = e.iterator();
        e.close(i);
        pm.close();
    }

    public void testRollbackQ() throws Exception {
        nuke(MapModel.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 20; i++) {
            MapModel mapModel = new MapModel();
            mapModel.getBasicMap().put("key1", "val1");
            col.add(mapModel);
        }
        pm.makePersistentAll(col);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(MapModel.class);
        q.setIgnoreCache(false);
        q.setFilter("basicMap.contains(p)");
        q.declareParameters("String p");
        List results = (List)q.execute("val1");
        Iterator iter = results.iterator();
        iter.next();
        pm.currentTransaction().rollback();

        pm.currentTransaction().setNontransactionalRead(true);
        while (iter.hasNext()) {
            System.out.println("next = " + iter.next());
        }
        pm.close();
        checkAllQsClosed(pm);
    }

    public void testDirtyFlush() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person p = new Person();
        p.setName("name1");
        col.add(p);
        pm.makePersistentAll(col);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, true);
        p.setName("name2");
        ((VersantPersistenceManager)pm).flush();

//        Query q = pm.newQuery(Person.class);
//        q.setIgnoreCache(false);
//        List results = (List) q.execute();
//        results.iterator();
        p.setName("name3");
        pm.currentTransaction().commit();
        pm.close();
        checkAllQsClosed(pm);
    }

    public void testDirtyFlushRetainValues() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person p = new Person();
        p.setName("name1");
        col.add(p);
        pm.makePersistentAll(col);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, true);
        p.setName("name2");

        Query q = pm.newQuery(Person.class);
        q.setIgnoreCache(false);
        List results = (List)q.execute();
        results.iterator();
        p.setName("name3");

        pm.currentTransaction().commit();
        pm.close();
        checkAllQsClosed(pm);
    }

    public void testMap() throws Exception {
        nuke(MapModel.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        MapModel mapModel = new MapModel();
        mapModel.getBasicMap().put("key1", "val1");
        col.add(mapModel);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(MapModel.class);
        q.setIgnoreCache(false);
        q.setFilter("basicMap.contains(p)");
        q.declareParameters("String p");
        List results = (List)q.execute("val1");
        Assert.assertEquals(1, results.size());
        pm.close();
        checkAllQsClosed(pm);
    }

    public void testMapKey() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        MapModel mapModel = new MapModel();
        mapModel.getBasicMap().put("key1", "val1");
        col.add(mapModel);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(MapModel.class, col);
        q.setFilter("basicMap.containsKey(p)");
        q.declareParameters("String p");
        List results = (List)q.execute("key1");
        Assert.assertEquals(1, results.size());
        pm.close();
        checkAllQsClosed(pm);
    }

    public void testMapKeyNot() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        MapModel mapModel = new MapModel();
        mapModel.getBasicMap().put("key1", "val1");
        mapModel.getBasicMap().put("key2", "val2");
        col.add(mapModel);

        mapModel = new MapModel();
        mapModel.getBasicMap().put("key1", "val4");
        mapModel.getBasicMap().put("key3", "val5");
        col.add(mapModel);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(MapModel.class);
        q.setFilter("!basicMap.containsKey(p)");
        q.declareParameters("String p");
        List results = (List)q.execute("key2");
        Assert.assertEquals(1, results.size());
        mapModel = (MapModel)results.get(0);
        Assert.assertEquals("val5", mapModel.getBasicMap().get("key3"));

        q = pm.newQuery(MapModel.class, col);
        q.setFilter("!basicMap.containsKey(p)");
        q.declareParameters("String p");
        results = (List)q.execute("key2");
        Assert.assertEquals(1, results.size());
        mapModel = (MapModel)results.get(0);
        Assert.assertEquals("val5", mapModel.getBasicMap().get("key3"));

        pm.close();
        checkAllQsClosed(pm);
    }

    public void testMapWithRefKey() throws Exception {
        int errCount = 0;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        MapModel mapModel = new MapModel();
        Person p = new Person("keyP1");
        pm.makePersistent(p);
        mapModel.getRefKeyMap().put(p, "val1");
        col.add(mapModel);
        pm.makePersistentAll(col);
        pm.currentTransaction().commit();
        System.out.println(
                ">>>>>>>>>>> QueryTests.testMapWithRefKey " + ++errCount + " <<<<<<<<<<<<< ");

        pm.currentTransaction().begin();
        Query q = pm.newQuery(MapModel.class);
//        q.setIgnoreCache(false);
        q.setFilter("refKeyMap.containsKey(person)");
        q.declareParameters("Person person");
        List results = (List)q.execute(p);
        Assert.assertEquals(1, results.size());
        pm.close();
        checkAllQsClosed(pm);
    }

    public void testMapWithRefValue() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        MapModel mapModel = new MapModel();
        Person p = new Person("keyP1");
        pm.makePersistent(p);
        Person p2 = new Person("valP1");
        pm.makePersistent(p2);
        mapModel.getRefRefMap().put(p, p2);
        col.add(mapModel);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(MapModel.class, col);
        q.setFilter("refRefMap.contains(person)");
        q.declareParameters("Person person");
        List results = (List)q.execute(p2);
        Assert.assertEquals(1, results.size());
        pm.close();
        checkAllQsClosed(pm);
    }

    public void testQCaching() throws Exception {
        if (!Utils.cacheEnabled()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person p = new Person("p");

        Address address = new Address();
        address.setStreet("street");
        p.setAddress(address);
        col.add(p);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("Person n, Address pr");
        q.setFilter(
                "(person == n && address == pr) || person.personsList.contains(n)");
        List result = (List)q.executeWithArray(new Object[]{null, address});
        Assert.assertEquals(1, result.size());
        q.close(result);
        q.closeAll();
        checkAllQsClosed(pm);

        q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("Person n, Address pr");
        q.setFilter(
                "(person == n && address == pr) || person.personsList.contains(n)");
        result = (List)q.executeWithArray(new Object[]{null, address});
        Assert.assertEquals(1, result.size());
        q.close(result);
        q.closeAll();
        checkAllQsClosed(pm);
        pm.close();
        checkAllQsClosed(pm);
    }

    public void testNullPC() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person p = new Person("p");

        Address address = new Address();
        address.setStreet("street");
        p.setAddress(address);
        col.add(p);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("Person n, Address pr");
        q.setFilter(
                "(person == n && address == pr) || person.personsList.contains(n)");
        List result = (List)q.executeWithArray(new Object[]{null, address});
        Assert.assertEquals(1, result.size());
        q.close(result);
        pm.close();
    }

    public void testQ() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(10);
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.declareParameters("int param");
        q.setFilter("intValue <= param");
        List results = (List)q.execute(new Integer(10));
        Assert.assertEquals(1, results.size());
        q.close(results);
        pm.close();
    }

    public void testoo7Q5() throws Exception {
    	if (!isApplicationIdentitySupported())
 			return;
        nuke(BaseAssembly.class);
        nuke(Module.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        int count = 0;

        Collection col = new ArrayList();

        Module mod = new Module(pm, 2, 0);
        BaseAssembly baseAssembly = new BaseAssembly(pm, mod, null);
        pm.makePersistent(baseAssembly);

        col.add(baseAssembly);

        pm.setIgnoreCache(false);

        Extent clnBase = pm.getExtent(BaseAssembly.class, false);
        String vars = "CompositePart cp";
        String filter = "(componentsPriv.contains(cp) & cp.buildDate > buildDate)";
        Query q = pm.newQuery(clnBase, filter);
        q.declareVariables(vars);
        q.declareImports(
                "import com.versant.core.jdo.junit.test0.model.CompositePart");
        Collection oldBaseAssms = (Collection)q.execute();
        // iterate over BaseAssemblys found and return count.
        for (Iterator i = oldBaseAssms.iterator(); i.hasNext();) {
            BaseAssembly ba = (BaseAssembly)i.next();
//            processBaseAssembly(ba);
            pm.evict(ba);
        }

        pm.close();
    }

    public void testoo7Q4() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Document doc = new Document();
        doc.setTitle("bla");
        Collection col = new ArrayList();

        Module mod = new Module(pm, 2, 0);
        BaseAssembly baseAssembly = new BaseAssembly(pm, mod, null);
        pm.makePersistent(baseAssembly);
        col.add(baseAssembly);
        pm.setIgnoreCache(false);
        baseAssembly.setBuildDate(new Date(TIME1));

        String param = "String title, String param";
        String vars = "CompositePart cp";
        String filter = "(componentsPriv.contains(cp) " +
                "& (cp.documentation.title == title & cp.documentation.text == param))";
        Extent baExtent = pm.getExtent(BaseAssembly.class, false);
        Query q = pm.newQuery(baExtent, filter);
        //q.setClass(BaseAssembly.class);   //  Needed by some implementations (4/02).^M
        q.declareParameters(param);
        q.declareVariables(vars);
        q.declareImports("import com.versant.core.jdo.junit.test0.model.CompositePart; " +
                "import java.util.Collection");
        List baseAssms = (List)q.execute("bla", "bla");
        System.out.println("Size = " + baseAssms.size());
        pm.close();
    }

    public void testThisKeyWord() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(3);
        Collection col = new ArrayList();
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.declareParameters("Long param");
        q.setFilter("this.intValue == param");
        List results = (List)q.execute(new Long(3));
        Assert.assertEquals(1, results.size());
        q.closeAll();
        pm.close();
    }

    public void testNullQ() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setBooleanValue(true);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setBooleanValue(true);
        n.setBooleanWValue(null);
        col.add(n);

        pm.makePersistentAll(col);

        List results = null;
        Query q = null;

        q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.setFilter("booleanWValue == null");
        results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        q.closeAll();

        q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.setFilter("null == booleanWValue");
        results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        q.closeAll();

        pm.close();
    }

    public void testNullParamQ() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setBooleanValue(true);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setBooleanValue(true);
        n.setBooleanWValue(null);
        col.add(n);

        pm.makePersistentAll(col);

        List results = null;
        Query q = null;

        q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.declareParameters("Boolean param");
        q.setFilter("booleanWValue == param");
        results = (List)q.execute(null);
        Assert.assertEquals(1, results.size());
        q.closeAll();

        pm.close();
    }

    public void testCollectionNullParamQ() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person n = new Person();
        n.getStringList().add("bla");
        col.add(n);

        n = new Person();
        n.getStringList().add("bla");
        col.add(n);

        n = new Person();
        n.getStringList().add(null);
        col.add(n);

        try {
            pm.makePersistentAll(col);
            throw new TestFailedException(
                    "expected JDOException for null element");
        } catch (JDOException e) {
            // good
        }

        // this test can no longer run as we do not allow nulls in collections
        if (true) {
            pm.close();
            return;
        }

        List results = null;
        Query q = null;

        q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("String param");
        q.setFilter("this.stringList.contains(param)");
        results = (List)q.execute(null);
        Assert.assertEquals(1, results.size());
        q.closeAll();

        pm.close();
    }

    public void testLocalParamAndFieldSameName() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person n = new Person("a");
        col.add(n);

        n = new Person("b");
        col.add(n);

        n = new Person("ab");
        col.add(n);

        pm.makePersistentAll(col);

        List results = null;
        Query q = null;

        q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("String name");
        q.setFilter("this.name.startsWith(name)");
        results = (List)q.execute("a");
        Assert.assertEquals(2, results.size());
        q.closeAll();

        pm.close();
    }

    public void testLocalParamAndFieldSameName2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person main = null;
        Person n = main = new Person("a");
        col.add(n);

        n = new Person("b");
        col.add(n);

        n = new Person("a");
        main.setPerson(n);
        col.add(n);

        n = new Person("ab");
        main.setPerson(n);
        col.add(n);

        pm.makePersistentAll(col);

        List results = null;
        Query q = null;

        q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("String name");
        q.setFilter("this.person.name.startsWith(name)");
        results = (List)q.execute("a");
        Assert.assertEquals(1, results.size());
        q.closeAll();

        pm.close();
    }

    public void testEmptyStringQs() throws Exception {
        nuke(NonMutableJavaTypes.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setBooleanValue(true);
        n.setDoubleValue(3);
        n.setFloatValue(3);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setDoubleValue(3);
        n.setFloatValue(3);
        n.setBooleanValue(false);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setDoubleValue(3);
        n.setFloatValue(3);
        n.setBooleanValue(false);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setDoubleValue(3);
        n.setFloatValue(3);
        n.setBooleanValue(false);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        pm.makePersistentAll(col);

        //local
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.declareParameters("");
        q.setFilter("");
        q.declareImports("");
        q.setOrdering("");
        List results = (List)q.execute();
        Assert.assertEquals(4, results.size());
        q.closeAll();

        //against the db
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        q.declareParameters("");
        q.setFilter("");
        q.declareImports("");
        q.setOrdering("");
        results = (List)q.execute();
        Assert.assertEquals(4, results.size());
        q.closeAll();

        pm.close();
    }

    public void testLocalBin() throws Exception {
        nuke(NonMutableJavaTypes.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setBooleanValue(true);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setBooleanValue(false);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setBooleanValue(false);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setBooleanValue(false);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        pm.makePersistentAll(col);

        //local
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
//        q.setFilter("stringValue == \"bla\"" );
        q.setFilter("booleanValue == true");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        q.closeAll();

//        //against the db
//        pm.currentTransaction().commit();
//
//        pm.currentTransaction().begin();
//        col = null;
//        q.setCandidates(col);
//        results = (List) q.execute();
//        Assert.assertEquals(1, results.size());
//        q.closeAll();

        pm.close();
    }

    public void testLocalBinObject() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setBooleanValue(true);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setBooleanValue(false);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setBooleanValue(false);
        n.setBooleanWValue(new Boolean(true));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setBooleanValue(false);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
//        q.setFilter("stringValue == \"bla\"" );
        q.setFilter("booleanWValue == true");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        q.closeAll();

//        pm.currentTransaction().commit();
//        pm.currentTransaction().begin();
//        col = null;
//        q.setCandidates(col);
//        results = (List) q.execute();
//        Assert.assertEquals(1, results.size());
//        q.closeAll();

        pm.close();
    }

    public void testLocalBinObjectParam() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
//        n.setCharValue('a');
        n.setBooleanValue(true);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        n = new NonMutableJavaTypes();
//        n.setCharValue('a');
        n.setBooleanValue(false);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        n = new NonMutableJavaTypes();
//        n.setCharValue('a');
        n.setBooleanValue(false);
        n.setBooleanWValue(new Boolean(true));
        col.add(n);

        n = new NonMutableJavaTypes();
//        n.setCharValue('a');
        n.setBooleanValue(false);
        n.setBooleanWValue(new Boolean(false));
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.declareParameters("Boolean bool");
        q.setFilter("booleanWValue == bool");
        List results = (List)q.execute(new Boolean(true));
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(
                ((NonMutableJavaTypes)results.get(0)).getCharValue(),
                Character.MIN_VALUE);
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute(new Boolean(true));
        Assert.assertEquals(1, results.size());
        q.closeAll();

        pm.close();
    }

    public void testLocalMultiply() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(3);
        n.setLongValue(6);
        n.setIntWValue(new Integer(1));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setIntValue(2);
        n.setLongValue(6);
        n.setIntWValue(new Integer(1));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setIntValue(4);
        n.setLongValue(6);
        n.setIntWValue(new Integer(1));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setIntValue(4);
        n.setLongValue(-1);
        n.setIntWValue(new Integer(1));
        col.add(n);

        pm.makePersistentAll(col);
        final int expectedResults = 2;

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
//        q.setFilter("stringValue == \"bla\"" );
        q.setFilter(
                "this.intValue * intValue + this.longValue  + intWValue == 16");
//        q.setFilter("intValue * intValue + longValue  + intWValue == 16" );
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testLocalMultiplyBig() {
    	if (!isQueryOnBigNumberSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person n = new Person();
        n.setIntField(6);
        n.setIntegerField(new Integer(1));
        n.setBigIntegerField(new BigInteger(String.valueOf(4)));
        col.add(n);

        n = new Person();
        n.setIntField(2);
        n.setIntegerField(new Integer(1));
        n.setBigIntegerField(new BigInteger(String.valueOf(4)));
        col.add(n);

        n = new Person();
        n.setIntField(5);
        n.setIntegerField(new Integer(1));
        n.setBigIntegerField(new BigInteger(String.valueOf(15)));
        col.add(n);

        n = new Person();
        n.setIntField(6);
        n.setIntegerField(new Integer(1));
        n.setBigIntegerField(new BigInteger(String.valueOf(3)));
        col.add(n);

        pm.makePersistentAll(col);

        final int expectedResults = 2;

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter(
                "intField * intField + integerField + bigIntegerField == 41");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
//        col = null;
        q.setCandidates(nullCollection);
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    public void testLocalMultiplyBigDec() throws Exception {
    	if (!isQueryOnBigNumberSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person n = new Person();
        n.setIntField(6);
        n.setIntegerField(new Integer(1));
        n.setBigDecimalField(new BigDecimal(String.valueOf(4)));
        col.add(n);

        n = new Person();
        n.setIntField(2);
        n.setIntegerField(new Integer(1));
        n.setBigDecimalField(new BigDecimal(String.valueOf(4)));
        col.add(n);

        n = new Person();
        n.setIntField(5);
        n.setIntegerField(new Integer(1));
        n.setBigDecimalField(new BigDecimal(String.valueOf(15)));
        col.add(n);

        n = new Person();
        n.setIntField(6);
        n.setIntegerField(new Integer(1));
        n.setBigDecimalField(new BigDecimal(String.valueOf(3)));
        col.add(n);

        pm.makePersistentAll(col);

        final int expectedResults = 2;

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter(
                "intField * intField + integerField + bigDecimalField == 41");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testLocalToLowerCase() {
        String db = getSubStoreInfo().getDataStoreType();
        // Informix SE does not support lower(..).
        if (db.equals("informixse")) return;
        if (db.equals("firebird")) return;

        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        Person n = new Person();
        n.setName("blaA");
        col.add(n);

        n = new Person();
        n.setName("blaB");
        col.add(n);

        n = new Person();
        n.setName("blaC");
        col.add(n);

        n = new Person();
        n.setName("blaD");
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("String param");
        q.setFilter("name.toLowerCase().startsWith(param)");
        List results = (List)q.execute("blaa");
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute("blaa");
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testLocalMapContainsKey() {
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        MapModel mapModel = new MapModel();
        Person keyPerson = new Person("key");
        mapModel.getRefKeyMap().put(keyPerson, "value");
        col.add(mapModel);
//        col.add(keyPerson);
        pm.makePersistent(mapModel);
        pm.makePersistent(keyPerson);

        Query q = pm.newQuery(MapModel.class);
        q.setCandidates(col);
        q.declareParameters("Person param");
        q.setFilter("refKeyMap.containsKey(param)");
        List results = (List)q.execute(keyPerson);
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute(keyPerson);
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testLocalMapContainsKey2() {
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        MapModel mapModel = new MapModel();
        mapModel.getBasicMap().put("key", "value");
        col.add(mapModel);

        mapModel = new MapModel();
        mapModel.getBasicMap().put("key1", "value");
        col.add(mapModel);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(MapModel.class);
        q.setCandidates(col);
        q.declareParameters("String param");
        q.setFilter("basicMap.containsKey(param)");
        List results = (List)q.execute("key");
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute("key");
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testLocalIsEmpty() throws Exception {
        deleteAllPersons();

        final int expectedResults = 3;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        List tCol1 = new ArrayList();
        tCol1.add("Bla");
        List tCol2 = new ArrayList();

        Person n = new Person();
        n.setStringList(tCol1);
        col.add(n);

        n = new Person();
        n.setStringList(tCol2);
        col.add(n);

        n = new Person();
        col.add(n);

        n = new Person();
        n.setIntField(1);
        n.setStringList(tCol2);
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter("stringList.isEmpty()");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.close();
    }

    private void deleteAllPersons() {
        try {
            nuke(ListModel.class);
            nuke(PCCollectionEntry.class);

            nuke(PCCollectionEntry.class);
            nuke(MapModel.class);
            nuke(PrestoreModel.class);
            nuke(ModelFGTest.class);

            nuke(Person.class);
            nuke(Address.class);
        } catch (SQLException e) {
            Utils.fail(e.getMessage());
        }
        pmf().evictAll();
//        PersistenceManager pm = pmf().getPersistenceManager();
//        pm.currentTransaction().begin();
//        Collection toDel = (Collection)pm.newQuery(Person.class).execute();
//        pm.deletePersistentAll(toDel);
//        pm.currentTransaction().commit();
    }

    public void testLocalIsEmptyAnd() throws Exception {
        if (isVds()) {
            broken();
            return;
        }

        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        List tCol1 = new ArrayList();
        tCol1.add("Bla");
        List tCol2 = new ArrayList();

        Person n = new Person();
        n.setStringList(tCol1);
        col.add(n);

        n = new Person();
        n.setName("person");
        n.setStringList(tCol2);
        col.add(n);

        n = new Person();
        col.add(n);

        n = new Person();
        n.setIntField(1);
        n.setStringList(tCol2);
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter("stringList.isEmpty() && name == \"person\"");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        System.out.println("\n*** QueryTests.testLocalIsEmptyAnd last");
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();
        pm.close();
    }

    public void testUnaryTilde() throws Exception {
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        Person n = new Person();
        n.setIntField(3);
        col.add(n);

        n = new Person();
        n.setIntField(-3);
        col.add(n);

        n = new Person();
        n.setIntField(4);
        col.add(n);

        n = new Person();
        n.setIntField(1);
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter("~intField == ~3");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        q.setCandidates(nullCollection);
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();
        pm.deletePersistentAll(col);

        pm.close();
    }

    public void testUnaryTildeObject() throws Exception {
        nuke(Person.class);
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        Person n = new Person();
        n.setIntField(3);
        col.add(n);

        n = new Person();
        n.setIntField(-3);
        col.add(n);

        n = new Person();
        n.setIntField(4);
        col.add(n);

        n = new Person();
        n.setIntField(1);
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("Integer intParam");
        q.setFilter("~intField == intParam");
        List results = (List)q.execute(new Integer(~3));
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = results = (List)q.execute(new Integer(~3));
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testUnaryNeg() throws Exception {
        nuke(Person.class);
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person n = new Person();
        n.setIntField(3);
        col.add(n);

        n = new Person();
        n.setIntField(-3);
        col.add(n);

        n = new Person();
        n.setIntField(4);
        col.add(n);

        n = new Person();
        n.setIntField(1);
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter("-intField == -3");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testUnaryNegOnObject() throws Exception {
        nuke(Person.class);
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person n = new Person();
        n.setIntegerField(new Integer(3));
        col.add(n);

        n = new Person();
        n.setIntegerField(new Integer(-3));
        col.add(n);

        n = new Person();
        n.setIntegerField(new Integer(4));
        col.add(n);

        n = new Person();
        n.setIntegerField(new Integer(1));
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter("-integerField == -3");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testUnaryNegOnObjectParam() throws Exception {
        nuke(Person.class);
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person n = new Person();
        n.setIntegerField(new Integer(3));
        col.add(n);

        n = new Person();
        n.setIntegerField(new Integer(-3));
        col.add(n);

        n = new Person();
        n.setIntegerField(new Integer(4));
        col.add(n);

        n = new Person();
        n.setIntegerField(new Integer(1));
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("Integer intParam");
        q.setFilter("-integerField == intParam");
        List results = (List)q.execute(new Integer(-3));
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute(new Integer(-3));
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testNotEqual() throws Exception {
        nuke(new Class[]{Person.class, MapModel.class});

        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person n = new Person();
        n.setName("bla");
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("String param");
        q.setFilter("name != param");
        List results = (List)q.execute("bl");
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute("bl");
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testUnaryNot() throws Exception {
        nuke(Person.class);
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        Person n = new Person();
        n.setName("bla");
        n.getStringList().add("bla");
        col.add(n);

        n = new Person();
        n.setName("blaa");
        n.getStringList().add("blaa");
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("String param");
        q.setFilter("!stringList.contains(param)");
        List results = (List)q.execute("bla");
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);

        System.out.println("\n\n\n\n\n\n\n\n");
        results = (List)q.execute("bla");
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testStringConcat() throws Exception {
        nuke(NonMutableJavaTypes.class);
        final int expectedResults = 1;

        // This test breaks on Informix SE as the concat includes the spaces
        // from the CHAR field that stringValue is mapped to.
        if (getSubStoreInfo().getDataStoreType().equals("informixse")) return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setStringValue("bla");
        Collection col = new ArrayList();
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
//        q.setFilter("stringValue == \"bla\"" );
        q.setFilter("stringValue + stringValue + stringValue == \"blablabla\"");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testLocalBigEqaulity() throws Exception {
        nuke(Person.class);
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person n = new Person();
        n.setBigIntegerField(new BigInteger(String.valueOf(10)));
        col.add(n);

        n = new Person();
        n.setBigIntegerField(new BigInteger(String.valueOf(9)));
        col.add(n);

        n = new Person();
        n.setBigIntegerField(new BigInteger(String.valueOf(11)));
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter("bigIntegerField == 10");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        q.declareParameters("java.math.BigInteger param");
        q.setFilter("bigIntegerField == param");
        results = (List)q.execute(new BigInteger(String.valueOf(10)));
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
//        col = null;
        q.setCandidates(nullCollection);
        results = (List)q.execute(new BigInteger(String.valueOf(10)));
        Assert.assertEquals(expectedResults, results.size());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        q.closeAll();

        pm.close();
    }

    public void testLocalBigDecEqaulity() throws Exception {
        nuke(Person.class);
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person n = new Person();
        n.setBigDecimalField(new BigDecimal(String.valueOf(10)));
        col.add(n);

        n = new Person();
        n.setBigDecimalField(new BigDecimal(String.valueOf(9)));
        col.add(n);

        n = new Person();
        n.setBigDecimalField(new BigDecimal(String.valueOf(11)));
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter("bigDecimalField == 10");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        q.declareParameters("java.math.BigDecimal param");
        q.setFilter("bigDecimalField == param");
        results = (List)q.execute(new BigDecimal(String.valueOf(10)));
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
//        col = null;
        q.setCandidates(nullCollection);
        results = (List)q.execute(new BigDecimal(String.valueOf(10)));
        Assert.assertEquals(expectedResults, results.size());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        q.closeAll();

        pm.close();
    }

    public void testLocalBigDecEqaulityWrongType() throws Exception {
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person n = new Person();
        n.setBigDecimalField(new BigDecimal(String.valueOf(10)));
        col.add(n);

        n = new Person();
        n.setBigDecimalField(new BigDecimal(String.valueOf(9)));
        col.add(n);

        n = new Person();
        n.setBigDecimalField(new BigDecimal(String.valueOf(11)));
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter("bigDecimalField == 10");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testLocalPCWrongType() throws Exception {
        final int expectedResults = 0;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person n = new Person();
        Person child = new Person("child");
        n.setPerson(child);
        n.setBigDecimalField(new BigDecimal(String.valueOf(10)));
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("Person param");
        q.setFilter("person == param");
        List results = (List)q.execute("bla");
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testPrimitiveAddition() throws Exception {
        nuke(NonMutableJavaTypes.class);
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(3);
        n.setLongValue(6);
        Collection col = new ArrayList();
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
//        q.setFilter("stringValue == \"bla\"" );
        q.setFilter("intValue + intValue + longValue == 12");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testPrimitiveSubtract() throws Exception {
        nuke(NonMutableJavaTypes.class);
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(3);
        n.setLongValue(6);
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setIntValue(4);
        n.setLongValue(8);
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setIntValue(10);
        n.setLongValue(1);
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.setFilter("intValue - intValue + longValue == 6");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testPrimitiveAndObjectAddition() throws Exception {
        nuke(NonMutableJavaTypes.class);
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(3);
        n.setLongValue(6);
        n.setIntWValue(new Integer(3));
        Collection col = new ArrayList();
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
//        q.setFilter("stringValue == \"bla\"" );
        q.setFilter("intValue + intWValue + longValue == 12");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testPrimitiveAndObjectSubtraction() throws Exception {
        nuke(NonMutableJavaTypes.class);
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(3);
        n.setLongValue(6);
        n.setIntWValue(new Integer(3));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setIntValue(3);
        n.setLongValue(6);
        n.setIntWValue(new Integer(6));
        col.add(n);

        n = new NonMutableJavaTypes();
        n.setIntValue(3);
        n.setLongValue(0);
        n.setIntWValue(new Integer(3));
        col.add(n);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
//        q.setFilter("stringValue == \"bla\"" );
        q.setFilter("intValue - intWValue + longValue == 3");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testPrimitiveAndBigObjectAddition() throws Exception {
    	if (!isQueryOnBigNumberSupported())
    		return;

    	nuke(Person.class);
        final int expectedResults = 2;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person p = new Person();
        p.setBigIntegerField(new BigInteger(String.valueOf(6)));
        p.setIntField(6);
        col.add(p);

        p = new Person();
        p.setBigIntegerField(new BigInteger(String.valueOf(8)));
        p.setIntField(5);
        col.add(p);

        p = new Person();
        p.setBigIntegerField(new BigInteger(String.valueOf(7)));
        p.setIntField(5);
        col.add(p);

        p = new Person();
        p.setBigIntegerField(new BigInteger(String.valueOf(8)));
        p.setIntField(4);
        col.add(p);

        p = new Person();
        p.setBigIntegerField(new BigInteger(String.valueOf(9)));
        p.setIntField(5);
        col.add(p);

        p = new Person();
        p.setBigIntegerField(new BigInteger(String.valueOf(8)));
        p.setIntField(6);
        col.add(p);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
//        q.setFilter("stringValue == \"bla\"" );
        q.setFilter("intField + intField + bigIntegerField == 18");
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testLocalTrueQuery() throws Exception {
        nuke(NonMutableJavaTypes.class);
        final int expectedResults = 1;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(3);
        Collection col = new ArrayList();
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        List results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        col = null;
        q.setCandidates(col);
        results = (List)q.execute();
        Assert.assertEquals(expectedResults, results.size());
        q.closeAll();

        pm.close();
    }

    public void testMultiTypeNavigation() throws Exception {
        nuke(new Class[]{Employee1.class, Company.class});
        PersistenceManager pm = pmf().getPersistenceManager();
        Collection col = new ArrayList();
        pm.currentTransaction().begin();

        Employee1 emp1 = new Employee1();
        emp1.setCompany(new Company());
        emp1.getCompany().setVal("company");
        col.add(emp1);

        emp1 = new Employee1();
        emp1.setCompany(new Company());
        emp1.getCompany().setVal("dompany");
        col.add(emp1);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Employee1.class);
        q.setCandidates(col);
        q.setFilter("company.val.startsWith(\"c\")");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        q.closeAll();

        pm.close();

    }

    public void testLocalOrIntLongObject() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(3);
        Collection col = new ArrayList();
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
//        q.setFilter("intValue == 3 || intValue == 3");
        q.setFilter("intValue == 4 || intValue == 3");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        q.closeAll();
        pm.close();
    }

    public void testLocalStartsWith() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person p = new Person("p1");
        p.setIntField(3);
        col.add(p);

        p = new Person("p2");
        p.setIntField(5);
        col.add(p);

        p = new Person("3");
        p.setIntField(3);
        col.add(p);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter("name.startsWith(\"p\") && intField == 3");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        pm.close();
    }

    public void testLocalStartsWithOr() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person p = new Person("p1");
        p.setIntField(3);
        col.add(p);

        p = new Person("p2");
        p.setIntField(5);
        col.add(p);

        p = new Person("3");
        p.setIntField(3);
        col.add(p);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter("this.name.startsWith(\"p\") || intField == 3");
        List results = (List)q.execute();
        Assert.assertEquals(3, results.size());
        pm.close();
    }

    public void testLocalEndsWith() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person p = new Person("p1z");
        p.setIntField(3);
        col.add(p);

        p = new Person("p2z");
        p.setIntField(5);
        col.add(p);

        p = new Person("p3");
        p.setIntField(3);
        col.add(p);

        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter("name.endsWith(\"z\") && intField == 3");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        pm.close();
    }

    public void testCollectionQBasic() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person parent = new Person("parent");
        parent.setIntField(4);
        col.add(parent);

        Person child = new Person("child");
        child.setIntField(2);
        parent.getPersonsSet().add(child);
        child.getPersonsSet().add(parent);
        col.add(child);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
//        q.declareVariables("Person var; Person var2");
        q.declareVariables("Person var");
        q.setFilter("personsSet.contains(var) && (var.intField == 2)");
//        (componentsPriv.contains(cp) & cp.buildDate > buildDate)
//        q.setFilter("personsSet.contains(var) && (personsList.contains(var2) && (var.intField == 3 && var2.intField == 4))");
//        q.setFilter("(personsSet.contains(var) && var.intField > 3) && (personsSet.contains(var) && var.intField < 5)");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        pm.close();
    }

    public void testCollectionQBasic1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person parent = new Person("parent");
        Date d = new Date(TIME1);
        parent.setIntField(4);
        parent.setBirthDate(d);
        col.add(parent);

        Person child = new Person("child");
        child.setIntField(2);
        parent.getPersonsSet().add(child);
        child.getPersonsSet().add(parent);
        child.setBirthDate(d);
        col.add(child);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
//        q.declareVariables("Person var; Person var2");
        q.declareVariables("Person var");
        q.setFilter("personsSet.contains(var) && (var.birthDate == birthDate)");
//        (componentsPriv.contains(cp) & cp.buildDate > buildDate)
//        q.setFilter("personsSet.contains(var) && (personsList.contains(var2) && (var.intField == 3 && var2.intField == 4))");
//        q.setFilter("(personsSet.contains(var) && var.intField > 3) && (personsSet.contains(var) && var.intField < 5)");
        List results = (List)q.execute();
        Assert.assertEquals(2, results.size());
        pm.close();
    }

    public void testCollectionQBasic2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        Person p = new Person("other");
        Date d = new Date(TIME1);
        p.setIntField(4);
        p.setBirthDate(d);
        col.add(p);

        p = new Person("dude");
        p.setIntField(4);
        p.setBirthDate(d);
        col.add(p);

        Person parent = new Person("parent");
        parent.setIntField(4);
        parent.setBirthDate(d);
        col.add(parent);

        Person child = new Person("child");
        child.setIntField(2);
        child.setPerson(p);
        parent.setPerson(child);
        parent.getPersonsSet().add(child);
        child.getPersonsSet().add(parent);
        child.getPersonsSet().add(p);
        child.setBirthDate(d);
        col.add(child);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
//        q.declareVariables("Person var; Person var2");
        q.declareVariables("Person var");
        q.declareParameters("Date p");
        q.setFilter(
                "person.personsSet.contains(var) && (var.birthDate == p && var.name == \"dude\")");
//        q.setFilter("person.personsSet.contains(var) && (var.birthDate == p)");
//        (componentsPriv.contains(cp) & cp.buildDate > buildDate)
//        q.setFilter("personsSet.contains(var) && (personsList.contains(var2) && (var.intField == 3 && var2.intField == 4))");
//        q.setFilter("(personsSet.contains(var) && var.intField > 3) && (personsSet.contains(var) && var.intField < 5)");
        List results = (List)q.execute(d);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("parent", ((Person)results.get(0)).getName());
        pm.close();
    }

    public void testCollectionQ() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person parent = new Person("parent");
        parent.setIntField(4);
        col.add(parent);

        Person child = new Person("child");
        child.setIntField(2);
        parent.getPersonsSet().add(child);
        child.getPersonsSet().add(parent);
        col.add(child);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        System.out.println("\n\n***");
        for (Iterator i = col.iterator(); i.hasNext();) {
            Person p = (Person)i.next();
            for (Iterator j = p.getPersonsSet().iterator(); j.hasNext();) {
                Person r = (Person)j.next();
                System.out.println("r.getIntField() = " + r.getIntField());
                System.out.println("r.getName() = " + r.getName());
            }
        }
        System.out.println("***\n\n");

//        q.declareVariables("Person var; Person var2");
        q.declareVariables("Person var");
        q.setFilter(
                "personsSet.contains(var) && (var.intField < 5 && var.name == \"child\")");
//        q.setFilter("personsSet.contains(var) && (personsList.contains(var2) && (var.intField == 3 && var2.intField == 4))");
//        q.setFilter("(personsSet.contains(var) && var.intField > 3) && (personsSet.contains(var) && var.intField < 5)");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        pm.close();
    }

    public void testCollectionQWithOR() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person parent = new Person("parent");
        parent.setIntField(4);
        col.add(parent);

        Person child = new Person("child");
        child.setIntField(2);
        parent.getPersonsSet().add(child);
        child.getPersonsSet().add(parent);
        col.add(child);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
//        q.declareVariables("Person var; Person var2");
        q.declareVariables("Person var");
        q.setFilter(
                "personsSet.contains(var) && (var.intField < 5 || var.name == \"child\")");
//        q.setFilter("personsSet.contains(var) && (personsList.contains(var2) && (var.intField == 3 && var2.intField == 4))");
//        q.setFilter("(personsSet.contains(var) && var.intField > 3) && (personsSet.contains(var) && var.intField < 5)");
        List results = (List)q.execute();
        Assert.assertEquals(2, results.size());
        pm.close();
    }

    public void testCollectionQ2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person parent = new Person("parent");
        parent.setIntField(4);
        col.add(parent);

        Person child = new Person("child");
        child.setIntField(3);
        parent.getPersonsSet().add(child);
        col.add(child);

        Person child2 = new Person("child2");
        child2.setIntField(4);
        parent.getPersonsList().add(child2);
        col.add(child2);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareVariables("Person var; Person var2");
//        q.declareVariables("Person var");
//        q.setFilter("personsSet.contains(var) && (var.intField < 5 && var.name == \"child\")");
        q.setFilter(
                "personsSet.contains(var) && (var.name == \"child\" && personsList.contains(var2) && (var.intField == 3 && var2.intField == 4))");
//        q.setFilter("personsSet.contains(var) && (personsList.contains(var2) && (var.intField == 3 && var2.intField == 4))");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        pm.close();
    }

    public void testCollectionQ2Or() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person parent = new Person("parent");
        parent.setIntField(4);
        col.add(parent);

        Person child = new Person("child");
        child.setIntField(4);
        parent.getPersonsSet().add(child);
        col.add(child);

        Person child2 = new Person("child2");
        child2.setIntField(4);
        parent.getPersonsList().add(child2);
        col.add(child2);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareVariables("Person var; Person var2");
//        q.declareVariables("Person var");
//        q.setFilter("personsSet.contains(var) && (var.intField < 5 && var.name == \"child\")");
        q.setFilter(
                "personsSet.contains(var) && (var.name == \"child\" && personsList.contains(var2) && (var.intField == 4 || var2.intField == 4))");
//        q.setFilter("personsSet.contains(var) && (personsList.contains(var2) && (var.intField == 3 && var2.intField == 4))");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        pm.close();
    }

    /**
     * This is an example of an badly writen query.
     * see testCollectionQ11 for the correct way.
     */
    public void testCollectionQ1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(true);
        pm.currentTransaction().begin();
        String nameString = "" + System.currentTimeMillis();
        Collection col = new ArrayList();
        Person parent = new Person(nameString);
        parent.setIntField(4);
        col.add(parent);

        Person child = new Person("child");
        child.setIntField(3);
        parent.getPersonsSet().add(child);
        col.add(child);

        Person child2 = new Person("child2");
        child2.setIntField(4);
        parent.getPersonsList().add(child2);
        col.add(child2);
        pm.makePersistentAll(col);

        Person p2 = new Person(nameString);
        Person c2 = new Person("child21");
        c2.setIntField(3);
        p2.getPersonsSet().add(c2);

        Person c3 = new Person("child22");
        c3.setIntField(5);
        p2.getPersonsSet().add(c3);
        pm.makePersistent(p2);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("String nameParam");
        q.declareVariables("Person var; Person var2");
        q.setFilter("name == nameParam && personsSet.contains(var) " +
                "&& (personsList.contains(var2) " +
                "&& (var.intField == 3 && var2.intField == 4))");
        List results = (List)q.execute(nameString);
        Assert.assertEquals(1, results.size());
        pm.close();
    }

    public void testCollectionQ11() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(true);
        String nameString = "" + System.currentTimeMillis();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person parent = new Person(nameString);
        parent.setIntField(4);
        col.add(parent);

        Person child = new Person("child");
        child.setIntField(3);
        parent.getPersonsSet().add(child);
        col.add(child);

        Person child2 = new Person("child2");
        child2.setIntField(4);
        parent.getPersonsList().add(child2);
        col.add(child2);
        pm.makePersistentAll(col);

        Person p2 = new Person(nameString);
        Person c2 = new Person("child21");
        c2.setIntField(3);
        p2.getPersonsSet().add(c2);

        Person c3 = new Person("child22");
        c3.setIntField(5);
        p2.getPersonsSet().add(c3);
        pm.makePersistent(p2);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("String nameParam");
        q.declareVariables("Person var; Person var2");
        q.setFilter(
                "name == nameParam && personsSet.contains(var) " +
                "&& (var.intField == 3 || (personsList.contains(var2) && (var2.intField == 4)))");
        List results = (List)q.execute(nameString);
        Assert.assertEquals(1, results.size());

        pm.setIgnoreCache(false);
        pm.close();
    }

    public void testVolumeQ2() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        System.gc();
        System.gc();
        System.gc();
        long start = Runtime.getRuntime().freeMemory();
        int amount = 10000;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < amount; i++) {
            Query q = pm.newQuery(Person.class);
            q.setFilter("intField == " + i);
//            q.setFilter("intField == 1");
            q.setOrdering("intField ascending");
            ((List)q.execute()).size();
        }
        long endTime = System.currentTimeMillis();
        System.gc();
        System.gc();
        System.gc();
        long end = Runtime.getRuntime().freeMemory();
        long diff = start - end;
        System.out.println(">>>>>>>>>>>>>>>> bytes/query = " + diff / amount);
        System.out.println("Total time = " + (endTime - startTime));
        System.out.println("Avg time = " + (endTime - startTime) / amount);
        pm.close();
    }

    public void testVolumeQ() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        for (int i = 0; i < 2000; i++) {
            NonMutableJavaTypes n = new NonMutableJavaTypes();
            n.setLongValue(3);
            n.setIntValue(3);
            n.setDoubleValue(4);
            col.add(n);
        }
        for (int i = 0; i < 2000; i++) {
            NonMutableJavaTypes n = new NonMutableJavaTypes();
            n.setLongValue(3);
            n.setIntValue(3);
            n.setDoubleValue(5);
            col.add(n);
        }
        pm.makePersistentAll(col);

        long start = System.currentTimeMillis();
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.declareParameters("Long l, Integer i, Double d");
        q.setFilter("longValue == l && intValue == i && doubleValue == d");
//        q.setFilter("longValue == 3 && intValue == 3 && doubleValue == 4");
        executeQ(q);
        executeQ(q);
        executeQ(q);

        long tot = 0;
        int n = 20;
        for (int i = 0; i < n; i++) {
            tot += executeQ(q);
        }
        System.out.println("avg = " + tot / n);
        pm.close();
    }

    private long executeQ(Query q) {
        long start = System.currentTimeMillis();
        List result = (List)q.execute(new Long(3), new Integer(3),
                new Double(4));
//        List result = (List) q.execute();
        Assert.assertEquals(2000, result.size());
        long diff = System.currentTimeMillis() - start;
        System.out.println("Time for q = " + diff);
        return diff;
    }

    public void testIntIntOp() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(3);
        col.add(n);
        n = new NonMutableJavaTypes();
        n.setIntValue(4);
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);

        List results = null;
        q.declareParameters("Integer param");

        q.setFilter("intValue >= param");
        results = (List)q.execute(new Integer(3));
        Assert.assertEquals(2, results.size());
        q.closeAll();

        q.setFilter("intValue >= param");
        results = (List)q.execute(new Integer(4));
        Assert.assertEquals(1, results.size());
        q.closeAll();

        q.setFilter("intValue > param");
        results = (List)q.execute(new Integer(3));
        Assert.assertEquals(1, results.size());
        q.closeAll();

        q.setFilter("intValue < param");
        results = (List)q.execute(new Integer(3));
        Assert.assertEquals(0, results.size());
        q.closeAll();

        q.setFilter("intValue < param");
        results = (List)q.execute(new Integer(4));
        Assert.assertEquals(1, results.size());
        q.closeAll();

        q.setFilter("intValue <= param");
        results = (List)q.execute(new Integer(4));
        Assert.assertEquals(2, results.size());
        q.closeAll();

        q.setFilter("intValue <= param");
        results = (List)q.execute(new Integer(3));
        Assert.assertEquals(1, results.size());
        q.closeAll();

        q.setFilter("intValue == param");
        results = (List)q.execute(new Integer(3));
        Assert.assertEquals(1, results.size());
        q.closeAll();

        q.setFilter("intValue == param");
        results = (List)q.execute(new Integer(2));
        Assert.assertEquals(0, results.size());
        q.closeAll();

        pm.close();
    }

    public void testLocalIntIntegerGE() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(3);
        col.add(n);
        n = new NonMutableJavaTypes();
        n.setIntValue(4);
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.declareParameters("Integer param");
        q.setFilter("intValue >= param");
        List results = (List)q.execute(new Integer(3));
        Assert.assertEquals(2, results.size());
        q.closeAll();
        pm.close();
    }

    public void testLocalIntegerIntGE() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        Person n = new Person();
        n.setIntegerField(new Integer(3));
        col.add(n);
        n = new Person();
        n.setIntegerField(new Integer(4));
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.setFilter("integerField >= 3");
        List results = (List)q.execute();
        Assert.assertEquals(2, results.size());
        q.closeAll();
        pm.close();
    }

    public void testLocalIntInteger() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(3);
        Collection col = new ArrayList();
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.declareParameters("Integer param");
        q.setFilter("intValue == param");
        List results = (List)q.execute(new Integer(3));
        Assert.assertEquals(1, results.size());
        q.closeAll();
        pm.close();
    }

    public void testLocalIntLongObject() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setIntValue(3);
        Collection col = new ArrayList();
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.declareParameters("Long param");
        q.setFilter("intValue == param");
        List results = (List)q.execute(new Long(3));
        Assert.assertEquals(1, results.size());
        q.closeAll();
        pm.close();
    }

    public void testLocalFloatDoubleObject() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setFloatValue(3);
        Collection col = new ArrayList();
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.declareParameters("Double param");
        q.setFilter("floatValue == param");
        List results = (List)q.execute(new Double(3));
        Assert.assertEquals(1, results.size());
        q.closeAll();
        pm.close();
    }

    public void testLocalDoubleFloatObject() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setDoubleValue(3);
        Collection col = new ArrayList();
        col.add(n);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.declareParameters("Float param");
        q.setFilter("doubleValue == param");
        List results = (List)q.execute(new Float(3));
        Assert.assertEquals(1, results.size());
        q.closeAll();
        pm.close();
    }

    public void testMultipleAnds() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setDoubleValue(3);
        n.setIntValue(4);
        n.setShortValue((short)5);
        NonMutableJavaTypes n1 = new NonMutableJavaTypes();
        n1.setDoubleValue(4);
        n1.setIntValue(4);
        n1.setShortValue((short)5);
        NonMutableJavaTypes n2 = new NonMutableJavaTypes();
        n2.setDoubleValue(4);
        n2.setIntValue(4);
        n2.setShortValue((short)5);
        Collection col = new ArrayList();
        col.add(n);
        col.add(n1);
        col.add(n2);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.setFilter("doubleValue == 3 && intValue == 4 && shortValue == 5");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        q.closeAll();
        pm.close();
    }

    public void testMultipleAnds2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setDoubleValue(3);
        n.setIntValue(4);
        n.setShortValue((short)5);
        n.setStringValue("bla");
        NonMutableJavaTypes n1 = new NonMutableJavaTypes();
        n1.setDoubleValue(4);
        n1.setIntValue(4);
        n1.setShortValue((short)5);
        NonMutableJavaTypes n2 = new NonMutableJavaTypes();
        n2.setDoubleValue(4);
        n2.setIntValue(4);
        n2.setShortValue((short)5);
        Collection col = new ArrayList();
        col.add(n);
        col.add(n1);
        col.add(n2);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.declareParameters("String param");
        q.setFilter(
                "doubleValue == 3 && intValue == 4 && shortValue == 5 && stringValue == param");
        List results = (List)q.execute("bla");
        Assert.assertEquals(1, results.size());
        q.closeAll();
        pm.close();
    }

    public void testDoubleAndDouble() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setDoubleValue(3);
        pm.makePersistent(n);
        Collection col = new ArrayList();
        col.add(n);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
//        q.setFilter("doubleValue == 3");
//        q.setFilter("doubleValue == 3 && doubleValue == 3");
        q.setFilter("doubleValue == 3 && doubleValue == 3 && doubleValue == 3");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        q.closeAll();
        pm.close();
    }

    public void testDoubleAndInt() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();
        n.setDoubleValue(3);
        n.setIntValue(4);
        pm.makePersistent(n);
        Collection col = new ArrayList();
        col.add(n);

        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(col);
        q.setFilter("doubleValue == 3 && intValue == 4");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        q.closeAll();
        pm.close();
    }

    public void testAnd() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p1 = new Person("p1");
        Person p2 = new Person("p2");
        p1.setPerson(p2);
        pm.makePersistent(p1);
        pm.makePersistent(p2);
        Collection col = new ArrayList();
        col.add(p1);
        col.add(p2);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("Person param1, String param2");
        q.setFilter("person == param1 && name == param2");
        List results = (List)q.execute(p2, "p1");
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(p1, results.get(0));
        q.closeAll();
        pm.close();
    }

    public void testLocalPCQ() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p1 = new Person("p1");
        Person p2 = new Person("p2");
        p1.setPerson(p2);
        pm.makePersistent(p1);
        pm.makePersistent(p2);
        Collection col = new ArrayList();
        col.add(p1);
        col.add(p2);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("Person param");
        q.setFilter("person == param");
        List results = (List)q.execute(p2);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(p1, results.get(0));
        q.closeAll();
        pm.close();
    }

    public void testLocalPCQ1() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p1 = new Person("p1");
        Person p2 = new Person("p2");
        Person p3 = new Person("p3");
        p1.setPerson(p2);
        p2.setPerson(p3);
        Collection col = new ArrayList();
        col.add(p1);
        col.add(p2);
        col.add(p3);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("Person param");
        q.setFilter("person.person == param");
        List results = (List)q.execute(p3);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(p1, results.get(0));
        q.closeAll();
        pm.close();
    }

    public void testLocalPCQNav() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p1 = new Person("p1");
        Person p2 = new Person("p2");
        p1.setPerson(p2);
        pm.makePersistent(p1);
        pm.makePersistent(p2);
        Collection col = new ArrayList();
        col.add(p1);
        col.add(p2);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("String param");
        q.setFilter("person.name == param");
        List results = (List)q.execute("p2");
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(p1, results.get(0));
        q.closeAll();
        pm.close();
    }

    public void testLocalPCQNav2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p1 = new Person("p1");
        Person p2 = new Person("p2");
        Person p3 = new Person("p3");
        p1.setPerson(p2);
        p2.setPerson(p3);
        Collection col = new ArrayList();
        col.add(p1);
        col.add(p2);
        col.add(p3);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("String param");
        q.setFilter("person.person.name == param");
        List results = (List)q.execute("p3");
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(p1, results.get(0));
        q.closeAll();
        pm.close();
    }

    public void testLocalPCQNav3() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p1 = new Person("p1");
        Person p2 = new Person("p2");
        Person p3 = new Person("p3");
        Person p4 = new Person("p4");
        p1.setPerson(p2);
        p2.setPerson(p3);
        p3.setPerson(p4);
        Collection col = new ArrayList();
        col.add(p1);
        col.add(p2);
        col.add(p3);
        col.add(p4);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("String param");
        q.setFilter("person.person.person.name == param");
        List results = (List)q.execute("p4");
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(p1, results.get(0));
        q.closeAll();
        pm.close();
    }

    public void testLocalQDate() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p1 = new Person("p1");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, 2);
        Date d1 = cal.getTime();
        p1.setBirthDate(d1);
        cal.set(Calendar.DATE, 4);

        Date d2 = cal.getTime();
        Person p2 = new Person("p2");
        p2.setBirthDate(d2);

        cal.set(Calendar.DATE, 6);
        Date d3 = cal.getTime();
        Person p3 = new Person("p3");
        p3.setBirthDate(d3);

        cal.set(Calendar.DATE, 3);
        Date d4 = cal.getTime();
        Person p4 = new Person("p4");
        p4.setBirthDate(d4);
        p4.setIntField(4);

        pm.currentTransaction().begin();
        Collection col = new ArrayList();
        col.add(p1);
        col.add(p2);
        col.add(p3);
        col.add(p4);
        pm.makePersistentAll(col);
        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("Date d1, Date d2");
//        q.setFilter("birthDate >= d1");
        q.setFilter("birthDate >= d1 && birthDate <= d2 && intField == 0");
        cal.set(Calendar.DATE, 1);
        Date dp1 = cal.getTime();
        cal.set(Calendar.DATE, 4);
        Date dp2 = cal.getTime();
        List list = (List)q.execute(dp1, dp2);
        Assert.assertEquals(2, list.size());
        q.closeAll();
        pm.close();
    }

    public void testBla() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        Person parent = new Person("parent");
        Person child = new Person("child");
        Person child2 = new Person("bla");
        parent.setPerson(child);
        child.setPerson(child2);
        pm.makePersistent(parent);
        pm.makePersistent(child);
        pm.makePersistent(child2);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(Person.class);
        q.setCandidates(candidates);
        q.setFilter("person.person.name == \"bla\"");
        List result = (List)q.execute();
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testLocalStringToLiteral() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        Person parent = new Person("bla");
        pm.makePersistent(parent);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(Person.class);
        q.setCandidates(candidates);
        q.setFilter("name == \"bla\"");
        List result = (List)q.execute();
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testLocalStringParamQ() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        Person parent = new Person("bla");
        pm.makePersistent(parent);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(Person.class);
        q.setCandidates(candidates);
        q.declareParameters("String param");
        q.setFilter("name == param");
        List result = (List)q.execute("bla");
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testLocalStringParamQRev() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        Person parent = new Person("bla");
        pm.makePersistent(parent);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(Person.class);
        q.setCandidates(candidates);
        q.declareParameters("String param");
        q.setFilter("param == name");
        List result = (List)q.execute("bla");
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testLocalLiteralToString() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        Person parent = new Person("bla");
        pm.makePersistent(parent);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(Person.class);
        q.setCandidates(candidates);
        q.setFilter("\"bla\" == name");
        List result = (List)q.execute();
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testLocalIntInt() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        Person parent = new Person("parent");
        parent.setIntField(4);
        pm.makePersistent(parent);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(Person.class);
        q.setCandidates(candidates);
        q.setFilter("intField == 4");
        List result = (List)q.execute();
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testLocalByteInt() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        NonMutableJavaTypes parent = new NonMutableJavaTypes();
        parent.setByteValue((byte)3);
        pm.makePersistent(parent);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(candidates);
        q.setFilter("byteValue == 3");
        List result = (List)q.execute();
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testLocalShortInt() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        NonMutableJavaTypes parent = new NonMutableJavaTypes();
        parent.setShortValue((short)3);
        pm.makePersistent(parent);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(candidates);
        q.setFilter("shortValue == 3");
        List result = (List)q.execute();
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testLocalLongInt() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        NonMutableJavaTypes parent = new NonMutableJavaTypes();
        parent.setLongValue(3);
        pm.makePersistent(parent);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(candidates);
        q.setFilter("longValue == 3");
        List result = (List)q.execute();
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testLocalDoubleInt() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        NonMutableJavaTypes parent = new NonMutableJavaTypes();
        parent.setDoubleValue(3);
        pm.makePersistent(parent);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(candidates);
        q.setFilter("doubleValue == 3");
        List result = (List)q.execute();
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testLocalFloatInt() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        NonMutableJavaTypes parent = new NonMutableJavaTypes();
        parent.setFloatValue(3);
        pm.makePersistent(parent);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(candidates);
        q.setFilter("floatValue == 3");
        List result = (List)q.execute();
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testLocalIntFloat() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        NonMutableJavaTypes parent = new NonMutableJavaTypes();
        parent.setIntValue(3);
        pm.makePersistent(parent);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(candidates);
        q.setFilter("intValue == 3f");
        List result = (List)q.execute();
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testLocalLongFloat() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        NonMutableJavaTypes parent = new NonMutableJavaTypes();
        parent.setLongValue(3);
        pm.makePersistent(parent);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(candidates);
        q.setFilter("longValue == 3f");
        List result = (List)q.execute();
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testLocalCharInt() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        NonMutableJavaTypes parent = new NonMutableJavaTypes();
        parent.setCharValue((char)3);
        pm.makePersistent(parent);
        Collection candidates = new ArrayList();
        candidates.add(parent);
        Query q = pm.newQuery(NonMutableJavaTypes.class);
        q.setCandidates(candidates);
        q.setFilter("charValue == 3");
        List result = (List)q.execute();
        Assert.assertEquals(1, result.size());
        result.size();
        pm.close();
    }

    public void testBla2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        Person p = new Person("p");
        p.setIntField(10);
        pm.makePersistent(p);
        Collection candidates = new ArrayList();
        candidates.add(p);
        Query q = pm.newQuery(Person.class);
        q.setCandidates(candidates);
        q.setFilter("intField == 10");
        List result = (List)q.execute();
        result.size();
        pm.close();

    }

    public void testIgnoreCache() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p1 = new Person("p1");
        pm.makePersistent(p1);
        Person p2 = new Person("p2");
        pm.currentTransaction().commit();

        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        pm.makePersistent(p2);
        Query q = pm.newQuery(Person.class);
        List result = (List)q.execute();
        Assert.assertEquals(2, result.size());
        pm.close();
    }

    public void testBasicRandomAccess() throws Exception {
        if (!getSubStoreInfo().isScrollableResultSetSupported() &&
            !isVds()) return;

        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery();
        q.setClass(Person.class);
        q.declareParameters("String value, String jdoGenieOptions");
        q.setFilter("name.startsWith(value)");
        List result = (List)q.execute("p", "randomAccess=true");
        result.get(7);
        result.get(1);
        result.get(9);
        Assert.assertEquals(10, result.size());

        q.close(result);
        q.closeAll();
        checkAllQsClosed(pm);
        pm.close();
    }

    private void checkAllQsClosed(PersistenceManager pm) {
//        if (((JdoGeniePersistenceManagerImp)pm).jdoConnection.hasOpenQueries()) {
//            Assert.fail("All q's must be closed at this stage");
//        }
    }

    public void testBasicRandomAccess3() throws Exception {
        if (!getSubStoreInfo().isScrollableResultSetSupported() &&
            !isVds()) return;

        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery();
        q.setClass(Person.class);
        q.declareParameters("String value, String jdoGenieOptions");
        q.setFilter("name.startsWith(value)");
        List result = (List)q.execute("p", "randomAccess=true");
        Assert.assertEquals(10, result.size());
        pm.close();
    }

    public void testBasicRandomAccess2() throws Exception {
        if (!getSubStoreInfo().isScrollableResultSetSupported() &&
            !isVds()) return;

        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery();
        q.setClass(Person.class);
        q.setOrdering("name ascending");
        q.declareParameters("String value, String jdoGenieOptions");
        q.setFilter("name.startsWith(value)");
        List result = (List)q.execute("p", "randomAccess=true");
        Assert.assertEquals("p5", ((Person)result.get(5)).getName());
        Assert.assertEquals("p0", ((Person)result.get(0)).getName());
        Assert.assertEquals("p9", ((Person)result.get(9)).getName());
        try {
            Assert.assertEquals("p10", ((Person)result.get(10)).getName());
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
        Assert.assertEquals(10, result.size());
        pm.close();
    }

    public void testQueryWithPCNonManagedInstance() throws Exception {
        nuke(Person.class);
        Person p = new Person("p");
        p.setPerson(new Person("Friend"));
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Person p2 = new Person("nonManaged");
        Query q = pm.newQuery(Person.class);
        q.declareParameters("Person param");
        q.setFilter("person == param");
        try {
            List list = (List)q.execute(p2);
        } catch (JDOUserException e) {
            //expected
        }
        q.closeAll();
        pm.close();
    }

    public void testMultipleQs() throws Exception {
        nuke(Person.class);
//        if (!isQueryOrderingSupported()) return;
        
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setOrdering("name ascending");
        checkQ(q, 8);
        checkQ(q, 0);
        checkQ(q, 1);
        checkQ(q, 9);
        try {
            checkQ(q, 10);
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        q.closeAll();
        pm.close();
    }

    public void testMultipleLocalGets() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setOrdering("name ascending");
        List list = (List)q.execute();
        list.size();
        Assert.assertEquals("p8", ((Person)list.get(8)).getName());
        Assert.assertEquals("p0", ((Person)list.get(0)).getName());
        Assert.assertEquals("p1", ((Person)list.get(1)).getName());
        Assert.assertEquals("p9", ((Person)list.get(9)).getName());
        try {
            Assert.assertEquals("p10", ((Person)list.get(10)).getName());
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        q.closeAll();
        pm.close();
    }

    public void testMultipleLocalIters() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setOrdering("name ascending");
        List list = (List)q.execute();
        list.size();

        checkList(list, 8);
        checkList(list, 0);
        checkList(list, 1);
        checkList(list, 9);
        try {
            checkList(list, 10);
        } catch (NoSuchElementException e) {
            //expected
        }
        q.closeAll();
        pm.close();
    }

    private void checkList(List list, int index) {
        Iterator iter = list.iterator();
        int count = index;
        for (int i = 0; i < count; i++) {
            iter.next();
        }
        Assert.assertEquals("p" + index, ((Person)iter.next()).getName());
    }

    public void testConcurrentQs() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setOrdering("name ascending");
        List list = (List)q.execute();
        List list2 = (List)q.execute();
        List list3 = (List)q.execute();

        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("p" + i, ((Person)list.get(i)).getName());
            Assert.assertEquals("p" + i, ((Person)list2.get(i)).getName());
            Assert.assertEquals("p" + i, ((Person)list3.get(i)).getName());
        }
        q.closeAll();
        pm.close();
    }

    private void checkQ(Query q, int i) {
        System.out.println("\n*** QueryTests.checkQ " + i);
        List list = (List)q.execute();
        Person p1 = (Person)list.get(i);
        Assert.assertEquals("p" + i, p1.getName());
    }

    public void testGetListIter() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        List list = (List)q.execute();
        ListIterator iter = list.listIterator();
        int count = 0;
        while (iter.hasNext()) {
            Person o = (Person)iter.next();
            count++;
            Assert.assertEquals(count, iter.nextIndex());
        }
        Assert.assertEquals(10, count);
        Assert.assertEquals(10, list.size());
        q.closeAll();
        pm.close();
    }

    public void testGet() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setOrdering("name ascending");
        List list = (List)q.execute();

        Assert.assertEquals(10, list.size());
//        if (isVds()) return;

        Assert.assertEquals("p4", ((Person)list.get(4)).getName());
        Assert.assertEquals("p6", ((Person)list.get(6)).getName());
        Assert.assertEquals("p9", ((Person)list.get(9)).getName());
        try {
            Assert.assertEquals("p10", ((Person)list.get(10)).getName());
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    /**
     * If a query is deserialised it pm must be null.
     */
    public void testSerialisedQueryPMNull() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Query q = pm.newQuery(Person.class);
        Query q2 = serialiseQuery(q);

        Assert.assertNull(q2.getPersistenceManager());
        q.closeAll();
        pm.close();
    }

    private Query serialiseQuery(Query q) throws IOException,
            ClassNotFoundException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ObjectOutputStream oOut = new ObjectOutputStream(bOut);
        oOut.writeObject(q);

        ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
        ObjectInputStream oIn = new ObjectInputStream(bIn);
        Query q2 = (Query)oIn.readObject();
        return q2;
    }

    public void testRepeatQWithDeserialisedQuery() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("p");
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        List col = (List)q.execute();
        Assert.assertEquals(1, col.size());
        Assert.assertEquals(p, col.get(0));
        Assert.assertTrue(p == col.get(0));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q2 = serialiseQuery(q);
        q2 = pm.newQuery(q2);
        List list2 = (List)q2.execute();
        Assert.assertEquals(col, list2);
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    /**
     * The result from an execute query must be unmodifiable.
     *
     * @throws Exception
     */
    public void testModifyQueryCollection() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("p");
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        List col = (List)q.execute();
        try {
            col.add("bla");
            Utils.fail("Must not be able to modify a query collection");
        } catch (UnsupportedOperationException e) {
            //expected
        } catch (Exception e) {
            throw e;
        }
        try {
            col.addAll(new ArrayList());
            Utils.fail("Must not be able to modify a query collection");
        } catch (UnsupportedOperationException e) {
            //expected
        } catch (Exception e) {
            throw e;
        }
        try {
            col.clear();
            Utils.fail("Must not be able to modify a query collection");
        } catch (UnsupportedOperationException e) {
            //expected
        } catch (Exception e) {
            throw e;
        }
        try {
            col.remove("bla");
            Utils.fail("Must not be able to modify a query collection");
        } catch (UnsupportedOperationException e) {
            //expected
        } catch (Exception e) {
            throw e;
        }
        try {
            col.removeAll(new ArrayList());
            Utils.fail("Must not be able to modify a query collection");
        } catch (UnsupportedOperationException e) {
            //expected
        } catch (Exception e) {
            throw e;
        }
        try {
            col.retainAll(new ArrayList());
            Utils.fail("Must not be able to modify a query collection");
        } catch (UnsupportedOperationException e) {
            //expected
        } catch (Exception e) {
            throw e;
        }
        try {
            col.set(0, null);
            Utils.fail("Must not be able to modify a query collection");
        } catch (UnsupportedOperationException e) {
            //expected
        } catch (Exception e) {
            throw e;
        }
        q.closeAll();
        pm.close();
    }

    public void testExecuteQueryWithClosedPM() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("p");
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        pm.close();
        try {
            q.execute();
            Utils.fail("Non allowed to execute an query with a closed pm");
        } catch (JDOUserException e) {
            //expected
        }
    }

    /**
     * This is to test if result is serializable.
     *
     * @throws Exception
     */
    public void testSerialize() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        List list = new ArrayList((Collection)q.execute());
        pm.retrieveAll(list);
        ObjectOutputStream out = new ObjectOutputStream(
                new ByteArrayOutputStream());
        out.writeObject(list);
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    public void testParamQuery() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        nuke(Person.class);
        pm.currentTransaction().begin();
        Query q = pm.newQuery();
        q.setClass(Person.class);
        q.declareParameters("String value");
        q.setFilter("name == value");
        q.setIgnoreCache(true);
        Collection result = (Collection)q.execute("name1");
        Iterator iterator = result.iterator();
        while (iterator.hasNext()) {
            System.out.println(((Person)iterator.next()).getName());
            throw new TestFailedException(
                    "The should be nothing in the results");
        }
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();

    }

    public void testParamQuery2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);

        nuke(Person.class);
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("nameff");
            pm.makePersistent(p);
        }

        Query q = pm.newQuery(Person.class);
        q.declareParameters("String value");
        q.setFilter("name == value");
        q.setIgnoreCache(false);
        Collection result = (Collection)q.execute("nameff");
        Iterator iterator = result.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            count++;
            System.out.println(((Person)iterator.next()).getName());
        }
        if (count != 10) {
            throw new TestFailedException("expected 10 and found " + count);
        }
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();

    }

    public void testNonTxQueries() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("name1");
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setNontransactionalRead(true);
        Query q = pm.newQuery();
        q.setCandidates(new DummyExtent(Person.class, false));
        q.setClass(Person.class);
        q.declareParameters("String var");
        q.setFilter("name == var");

        Collection col = (Collection)q.execute("name1");

        int count = 0;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            count++;
            Person o = (Person)iterator.next();
            Utils.assertEquals("name1", o.getName());
        }
        if (count != 10) {
            throw new TestFailedException(
                    "The expected amount was not returned in the query. expected = " + 10 + " actual = " + count);
        }

        q = pm.newQuery();
        q.setCandidates(new DummyExtent(Person.class, false));
        q.setClass(Person.class);
        q.declareParameters("String var");
        q.setFilter("name == var");

        col = (Collection)q.execute("name1");

        count = 0;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            count++;
            Person o = (Person)iterator.next();
            Utils.assertEquals("name1", o.getName());
        }
        if (count != 10) {
            throw new TestFailedException(
                    "The expected amount was not returned in the query. expected = " + 10 + " actual = " + count);
        }
        q.closeAll();
        pm.close();
    }

    public void testLocalQueryOrdering() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);

        nuke(Person.class);
        pm.currentTransaction().begin();
        Person p = null;
        Date bday = new Date(TIME1);
        int[] nos = new int[]{3, 4, 1, 6, 5, 7, 8, 0, 9, 2};
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            p = new Person("nameff" + nos[i]);
            p.setBirthDate(bday);
            pm.makePersistent(p);
            col.add(p);
        }

        Query q = pm.newQuery();
        q.setCandidates(col);
        q.setClass(Person.class);
        q.declareParameters("Date bday");
        q.setFilter("birthDate == bday");
        q.setIgnoreCache(false);
        q.setOrdering("name descending");
        Collection result = (Collection)q.execute(bday);
        Iterator iterator = result.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Person person = (Person)iterator.next();
            Assert.assertEquals("nameff" + (9 - count++), person.getName());
            if (Debug.DEBUG) {
                Debug.OUT.println("name = " + person.getName());
            }
        }
        if (count != 10) {
            throw new TestFailedException("expected 10 and found " + count);
        }

        q = pm.newQuery();
        q.setCandidates(col);
        q.setClass(Person.class);
        q.declareParameters("Date bday");
        q.setFilter("birthDate == bday");
        q.setIgnoreCache(false);
        q.setOrdering("name ascending");
        result = (Collection)q.execute(bday);
        iterator = result.iterator();
        count = 0;
        while (iterator.hasNext()) {
            Person person = (Person)iterator.next();
            Assert.assertEquals("nameff" + count++, person.getName());
            if (Debug.DEBUG) {
                Debug.OUT.println("name = " + person.getName());
            }
        }
        if (count != 10) {
            throw new TestFailedException("expected 10 and found " + count);
        }
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    public void testLocalQueryOrderingWithNavigation() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);

        nuke(Person.class);
        pm.currentTransaction().begin();
        Person p = null;
        Date bday = new Date(TIME1);
        int[] nos = new int[]{3, 4, 1, 6, 5, 7, 8, 0, 9, 2};
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person parent = new Person();
            p = new Person("nameff" + nos[i]);
            parent.setPerson(p);
            parent.setBirthDate(bday);
            col.add(p);
            col.add(parent);
        }
        pm.makePersistentAll(col);

        Query q = pm.newQuery();
        q.setCandidates(col);
        q.setClass(Person.class);
        q.declareParameters("Date bday");
        q.setFilter("birthDate == bday");
        q.setIgnoreCache(false);
        q.setOrdering("person.name descending");
        Collection result = (Collection)q.execute(bday);
        Iterator iterator = result.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Person person = (Person)iterator.next();
            Assert.assertEquals("nameff" + (9 - count++),
                    person.getPerson().getName());
        }
        if (count != 10) {
            throw new TestFailedException("expected 10 and found " + count);
        }

        q = pm.newQuery();
        q.setCandidates(col);
        q.setClass(Person.class);
        q.declareParameters("Date bday");
        q.setFilter("birthDate == bday");
        q.setIgnoreCache(false);
        q.setOrdering("person.name ascending");
        result = (Collection)q.execute(bday);
        iterator = result.iterator();
        count = 0;
        while (iterator.hasNext()) {
            Person person = (Person)iterator.next();
            Assert.assertEquals("nameff" + count++,
                    person.getPerson().getName());
        }
        if (count != 10) {
            throw new TestFailedException("expected 10 and found " + count);
        }
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    public void testLocalQueryOrderingWithNullFilter() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);

        nuke(Person.class);
        pm.currentTransaction().begin();
        Person p = null;
        Date bday = new Date(TIME1);
        int[] nos = new int[]{3, 4, 1, 6, 5, 7, 8, 0, 9, 2};
        Collection col = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person parent = new Person();
            p = new Person("nameff" + nos[i]);
            parent.setPerson(p);
            parent.setBirthDate(bday);
            col.add(parent);
        }
        pm.makePersistentAll(col);

        Query q = pm.newQuery();
        q.setCandidates(col);
        q.setClass(Person.class);
        q.setOrdering("person.name descending");
        Collection result = (Collection)q.execute();
        Iterator iterator = result.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Person person = (Person)iterator.next();
            Assert.assertEquals("nameff" + (9 - count++),
                    person.getPerson().getName());
        }
        if (count != 10) {
            throw new TestFailedException("expected 10 and found " + count);
        }

        q = pm.newQuery();
        q.setCandidates(col);
        q.setClass(Person.class);
        q.setIgnoreCache(false);
        q.setOrdering("person.name ascending");
        result = (Collection)q.execute();
        iterator = result.iterator();
        count = 0;
        while (iterator.hasNext()) {
            Person person = (Person)iterator.next();
            Assert.assertEquals("nameff" + count++,
                    person.getPerson().getName());
        }
        if (count != 10) {
            throw new TestFailedException("expected 10 and found " + count);
        }
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    public void testQueryOrdering1() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        Person p = null;

        nuke(Person.class);

        pm.currentTransaction().begin();
        Date bday = new Date(TIME1);
        for (int i = 0; i < 5; i++) {
            p = new Person("nameff" + i);
            p.setBirthDate(bday);
            p.setVal("s");
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        bday = new Date(TIME2);
        for (int i = 5; i < 10; i++) {
            p = new Person("nameff" + i);
            p.setBirthDate(bday);
            p.setVal("s");
            pm.makePersistent(p);
        }

        Query q = pm.newQuery(Person.class, "val == ss");
        q.setCandidates(pm.getExtent(Person.class, false));
        q.declareParameters("String ss");
        q.setIgnoreCache(false);
        q.setOrdering("name descending");
        Collection result = (Collection)q.execute("s");
        Iterator iterator = result.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Person person = (Person)iterator.next();
            if (Debug.DEBUG) {
                Debug.OUT.println("name = " + person.getName());
            }
            Assert.assertEquals("nameff" + (9 - count++), person.getName());
            if (Debug.DEBUG) {
                Debug.OUT.println(
                        "@@@@@@@@@@@@@@@@ passed @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            }
        }
        if (count != 10) {
            throw new TestFailedException("expected 10 and found " + count);
        }
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "#############################################################################################################");
        }

        q = pm.newQuery(pm.getExtent(Person.class, false), "val == ss");
        q.declareParameters("String ss");
        q.setIgnoreCache(false);
        q.setOrdering("name ascending");
        result = (Collection)q.execute("s");
        iterator = result.iterator();
        count = 0;
        while (iterator.hasNext()) {
            Person person = (Person)iterator.next();
            if (Debug.DEBUG) {
                Debug.OUT.println("name = " + person.getName());
            }
            Assert.assertEquals("nameff" + count++, person.getName());
            if (Debug.DEBUG) {
                Debug.OUT.println(
                        "@@@@@@@@@@@@@@@@ passed @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            }
        }
        if (count != 10) {
            throw new TestFailedException("expected 10 and found " + count);
        }
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    public void testQueryOrdering2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        Person p = null;

        nuke(Person.class);

        pm.currentTransaction().begin();
        Date bday = new Date(TIME1);
        for (int i = 5; i < 10; i++) {
            p = new Person("nameff" + i);
            p.setBirthDate(bday);
            p.setVal("s");
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        bday = new Date(TIME2);
        for (int i = 0; i < 5; i++) {
            p = new Person("nameff" + i);
            p.setBirthDate(bday);
            p.setVal("s");
            pm.makePersistent(p);
        }

        Query q = pm.newQuery();
        q.setCandidates(pm.getExtent(Person.class, false));
        q.declareParameters("String ss");
        q.setFilter("val == ss");
        q.setIgnoreCache(false);
        q.setOrdering("name descending");
        Collection result = (Collection)q.execute("s");
        Iterator iterator = result.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Person person = (Person)iterator.next();
            System.out.println("name = " + person.getName());
            Assert.assertEquals("nameff" + (9 - count++), person.getName());
            System.out.println(
                        "@@@@@@@@@@@@@@@@ passed @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        }
        if (count != 10) {
            throw new TestFailedException("expected 10 and found " + count);
        }
        System.out.println(
                    "#############################################################################################################");

        q = pm.newQuery();
        q.setCandidates(pm.getExtent(Person.class, false));
        q.declareParameters("String ss");
        q.setFilter("val == ss");
        q.setIgnoreCache(false);
        q.setOrdering("name ascending");
        result = (Collection)q.execute("s");
        iterator = result.iterator();
        count = 0;
        while (iterator.hasNext()) {
            Person person = (Person)iterator.next();
            if (Debug.DEBUG) {
                Debug.OUT.println("name = " + person.getName());
            }
            Assert.assertEquals("nameff" + count++, person.getName());
            if (Debug.DEBUG) {
                Debug.OUT.println(
                        "@@@@@@@@@@@@@@@@ passed @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            }
        }
        if (count != 10) {
            throw new TestFailedException("expected 10 and found " + count);
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void teststartsWith() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        pm.currentTransaction().commit();
    }

    public void testExtent1() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        nuke(Person.class);
        Person p = null;

        pm.currentTransaction().begin();
        Date bday = new Date(TIME1);
        for (int i = 0; i < 10; i++) {
            p = new Person("nameff" + i);
            p.setBirthDate(bday);
            p.setVal("s");
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Extent ex = pm.getExtent(Person.class, false);
        Iterator iter = ex.iterator();
        int count = 0;
        while (iter.hasNext()) {
            count++;
            Person o = (Person)iter.next();
            System.out.println("%%% person = " + o);
        }
        assertEquals(10, count);
        pm.close();
    }

    public void testQueryWithSuppliedFG() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

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

        pm.currentTransaction().begin();
        Query q = pm.newQuery(pm.getExtent(Level1.class, false));
        ((VersantQueryImp)q).setFetchGroup("fg1");
        Collection col = (Collection)q.execute();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Level1 o = (Level1)iterator.next();
            Debug.OUT.println(
                    "########### name = " + o.getLevel2().getLevel3().getName());
        }
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    public void testExtentForSupClass() throws Exception {

        nuke(ClassA.class);
        nuke(ClassAB.class);
        nuke(ClassBB.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        ClassA classA = null;
        for (int i = 0; i < 10; i++) {
            classA = new ClassA();
            classA.setStringA("stringA" + i);
            pm.makePersistent(classA);
        }

        for (int i = 0; i < 10; i++) {
            classA = new ClassAB();
            ((ClassAB)classA).setStringAB("stringAB" + i);
            pm.makePersistent(classA);
        }

        for (int i = 0; i < 10; i++) {
            classA = new ClassBB();
            ((ClassBB)classA).setStringBB("stringBB" + i);
            pm.makePersistent(classA);
        }

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Extent ex = pm.getExtent(ClassA.class, true);
        Iterator iter = ex.iterator();
        int count = 0;
        while (iter.hasNext()) {
            ClassA a = (ClassA)iter.next();
            count++;
        }

        Assert.assertEquals(30, count);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testExtentForSupClass2() throws Exception {
        nuke(ClassA.class);
        nuke(ClassAB.class);
        nuke(ClassBB.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        ClassA classA = null;
        for (int i = 0; i < 10; i++) {
            classA = new ClassA();
            classA.setStringA("stringA" + i);
            pm.makePersistent(classA);
        }

        for (int i = 0; i < 10; i++) {
            classA = new ClassAB();
            ((ClassAB)classA).setStringAB("stringAB" + i);
            pm.makePersistent(classA);
        }

        for (int i = 0; i < 10; i++) {
            classA = new ClassBB();
            ((ClassBB)classA).setStringBB("stringBB" + i);
            pm.makePersistent(classA);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Extent ex = pm.getExtent(ClassAB.class, true);
        Iterator iter = ex.iterator();
        int count = 0;
        while (iter.hasNext()) {
            ClassA a = (ClassA)iter.next();
            count++;
        }

        Assert.assertEquals(10, count);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testExtentForSupClass3() throws Exception {

        nuke(ClassA.class);
        nuke(ClassAB.class);
        nuke(ClassBB.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        ClassA classA = null;
//        for (int i = 0; i < 10; i++) {
//            classA = new ClassA();
//            classA.setStringA("stringA" + i);
//            pm.makePersistent(classA);
//        }
//
//        for (int i = 0; i < 10; i++) {
//            classA = new ClassAB();
//            ((ClassAB)classA).setStringAB("stringAB" + i);
//            pm.makePersistent(classA);
//        }

        for (int i = 0; i < 10; i++) {
            classA = new ClassBB();
            ((ClassBB)classA).setStringBB("stringBB" + i);
            pm.makePersistent(classA);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Extent ex = pm.getExtent(ClassA.class, true);
        Iterator iter = ex.iterator();
        int count = 0;
        while (iter.hasNext()) {
            ClassBB a = (ClassBB)iter.next();
            count++;
        }
        Assert.assertEquals(10, count);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testExtentForSupClass4() throws Exception {
        nuke(ClassA.class);
        nuke(ClassAB.class);
        nuke(ClassBB.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        ClassA classA = null;
        for (int i = 0; i < 10; i++) {
            classA = new ClassA();
            classA.setStringA("stringA" + i);
            pm.makePersistent(classA);
        }

        for (int i = 0; i < 10; i++) {
            classA = new ClassAB();
            ((ClassAB)classA).setStringAB("stringAB" + i);
            pm.makePersistent(classA);
        }

        for (int i = 0; i < 10; i++) {
            classA = new ClassBB();
            ((ClassBB)classA).setStringBB("stringBB" + i);
            pm.makePersistent(classA);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Extent ex = pm.getExtent(ClassA.class, true);
        Iterator iter = ex.iterator();
        int count = 0;
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof ClassBB) {
                ClassBB classBB = (ClassBB)o;
                Assert.assertTrue(classBB.getStringBB().startsWith("stringBB"));
                count++;
            }
        }
        Assert.assertEquals(10, count);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testInheritanceQStartsWith() throws Exception {
        nuke(ClassA.class);
        nuke(ClassAB.class);
        nuke(ClassBB.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        ClassA classA = null;
        for (int i = 0; i < 10; i++) {
            classA = new ClassBB();
            ((ClassBB)classA).setStringBB("stringBB" + i);
            pm.makePersistent(classA);
        }

        Query q = pm.newQuery(pm.getExtent(ClassBB.class, false));
        q.declareParameters("String value");
        q.setFilter("stringBB.startsWith(value)");
        q.setIgnoreCache(false);
        Collection col = (Collection)q.execute("stringBB");
        int c = 0;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            ClassBB cBB = (ClassBB)iterator.next();
            Assert.assertTrue("Must start with 'stringBB'",
                    cBB.getStringBB().startsWith("stringBB"));
            c++;
        }
        Assert.assertEquals(10, c);
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    public void testInheritanceQEndsWith() throws Exception {
        nuke(ClassA.class);
        nuke(ClassAB.class);
        nuke(ClassBB.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        ClassA classA = null;
        for (int i = 0; i < 10; i++) {
            classA = new ClassBB();
            ((ClassBB)classA).setStringBB("" + i + "stringBB");
            pm.makePersistent(classA);
        }

        Query q = pm.newQuery(pm.getExtent(ClassBB.class, false));
        q.declareParameters("String value");
        q.setFilter("stringBB.endsWith(value)");
        q.setIgnoreCache(false);
        Collection col = (Collection)q.execute("stringBB");
        int c = 0;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            ClassBB cBB = (ClassBB)iterator.next();
            Assert.assertTrue("Must ends with 'stringBB'",
                    cBB.getStringBB().endsWith("stringBB"));
            c++;
        }
        Assert.assertEquals(10, c);
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    public void testInheritanceQEndsWithOrdered() throws Exception {
        nuke(ClassA.class);
        nuke(ClassAB.class);
        nuke(ClassBB.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        ClassA classA = null;
        for (int i = 0; i < 10; i++) {
            classA = new ClassBB();
            ((ClassBB)classA).setStringBB("" + i + "stringBB");
            pm.makePersistent(classA);
        }

        Query q = pm.newQuery(pm.getExtent(ClassBB.class, false));
        q.declareParameters("String value");
        q.setFilter("stringBB.endsWith(value)");
        q.setIgnoreCache(false);
        q.setOrdering("stringBB descending");
        Collection col = (Collection)q.execute("stringBB");
        int c = 0;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            ClassBB cBB = (ClassBB)iterator.next();
            Assert.assertTrue("Must ends with 'stringBB'",
                    cBB.getStringBB().endsWith("stringBB"));
            Assert.assertEquals("" + (9 - c) + "stringBB", cBB.getStringBB());
//            Debug.out.println("stringBB = " + cBB.getStringBB());
            c++;
        }
        Assert.assertEquals(10, c);
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    public void testQueryExecuteWithMap() throws Exception {
        nuke(Person.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        p.setVal("val");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Map m = new HashMap();
        m.put("namep", "name");
        m.put("valp", "val");
        Query q = pm.newQuery(Person.class);
        q.declareParameters("String namep, String valp");
        q.setFilter("name == namep && val == valp");
        Collection col = (Collection)q.executeWithMap(m);
        int c = 0;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Person qp = (Person)iterator.next();
            Assert.assertEquals("name", qp.getName());
            c++;
        }
        Assert.assertEquals(1, c);
        q.closeAll();
        pm.close();
    }

    public void testQueryExecuteWithMapErrorParamNames() throws Exception {
        nuke(Person.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        p.setVal("val");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Map m = new HashMap();
        m.put("namer", "name");
        m.put("valp", "val");
        Query q = pm.newQuery(Person.class);
        q.declareParameters("String namep, String valp");
        q.setFilter("name == namep && val == valp");
        Collection col = null;
        try {
            col = (Collection)q.executeWithMap(m);
            Assert.fail(
                    "This should throw an exception because of inconsistent param naming");
        } catch (JDOException e) {
            if (Debug.DEBUG) {
                Debug.OUT.println(e.getMessage());
            }
        }
        q.closeAll();
        pm.close();
    }

    public void testQueryExecuteWithMapErrorParamAmount() throws Exception {
        nuke(Person.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        p.setVal("val");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Map m = new HashMap();
        m.put("namep", "name");
        m.put("valp", "val");
        m.put("dir", "bla");
        Query q = pm.newQuery(Person.class);
        q.declareParameters("String namep, String valp");
        q.setFilter("name == namep && val == valp");
        Collection col = null;
        try {
            col = (Collection)q.executeWithMap(m);
            Assert.fail(
                    "This should throw an exception because of inconsistent param sizes");
        } catch (JDOException e) {
            if (Debug.DEBUG) {
                Debug.OUT.println(e.getMessage());
            }
        }
        pm.close();
    }

    public void testCollectionQuery() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        List list = new ArrayList();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p4" + i);
            pm.makePersistent(p);
            list.add(p);
        }

        Collection col = (Collection)pm.newQuery(Person.class, list,
                "name == \"p44\"").execute();
        int c = 0;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Person o = (Person)iterator.next();
            Assert.assertEquals("p44", o.getName());
            c++;
        }
        Assert.assertEquals(1, c);
        pm.close();
    }

    public void testCollectionQueryFromExtentQuery() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p44" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(pm.getExtent(Person.class, false),
                "name.startsWith(n)");
        q.declareParameters("String n");
        Collection col1 = (Collection)q.execute("p44");
        List list1 = new ArrayList(col1);
        int c = 0;
//        for (Iterator iterator = col1.iterator(); iterator.hasNext();) {
//            Person person = (Person)iterator.next();
//            Debug.out.println("name = " + person.getName());
//            c++;
//        }
        Assert.assertEquals(10, list1.size());

        Collection col = (Collection)pm.newQuery(Person.class, col1,
                "name == \"p444\"").execute();
        c = 0;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Person o = (Person)iterator.next();
            Assert.assertEquals("p444", o.getName());
            c++;
        }
        Assert.assertEquals(1, c);
        q.closeAll();
        pm.close();
    }

    public void testCollectionQueryFromExtentQuerySorted() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("p44" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(pm.getExtent(Person.class, false),
                "name.startsWith(n)");
        q.declareParameters("String n");
        Collection col1 = (Collection)q.execute("p44");
        List list1 = new ArrayList(col1);
        int c = 0;
//        for (Iterator iterator = col1.iterator(); iterator.hasNext();) {
//            Person person = (Person)iterator.next();
//            Debug.out.println("name = " + person.getName());
//            c++;
//        }
        Assert.assertEquals(10, list1.size());

        q = pm.newQuery(Person.class, col1, "name.startsWith(n)");
        q.declareParameters("String n");
        q.setOrdering("name ascending");
        Collection col = (Collection)q.execute("p44");
        c = 0;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Person o = (Person)iterator.next();
            Assert.assertEquals(("p44" + c++), o.getName());
        }
        Assert.assertEquals(10, c);
        q.closeAll();
        pm.close();
    }

    public void testGetMethodOnQueryCollection() throws Exception {
        nuke(Person.class);
//        if (!isQueryOrderingSupported()) return;
        
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        for (int i = 0; i < 10; i++) {
            p = new Person("name" + i);
            pm.makePersistent(p);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setOrdering("name ascending");
        List list = (List)q.execute();
        Assert.assertEquals("name" + 0, ((Person)list.get(0)).getName());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = pm.newQuery(Person.class);
        q.setOrdering("name ascending");
        list = (List)q.execute();
        Assert.assertEquals("name" + 9, ((Person)list.get(9)).getName());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = pm.newQuery(Person.class);
        q.setOrdering("name ascending");
        list = (List)q.execute();
        Assert.assertEquals("name" + 5, ((Person)list.get(5)).getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test a modified local instance that still matches q crit is in results
     *
     * @throws Exception
     */
    public void testQueryIgnoreCacheFalse1() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("p1");
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        p.setVal("val3");
        Query q = pm.newQuery(Person.class);
        q.declareParameters("String n");
        q.setFilter("name == n");
        List list = (List)q.execute("p1");
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("val3", p.getVal());
        Assert.assertTrue(p == list.get(0));
        q.closeAll();
        pm.close();
    }

    /**
     * Test a modified local instance that does not matches q crit is in results
     *
     * @throws Exception
     */
    public void testQueryIgnoreCacheFalse2() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("p1");
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        p.setVal("val3");
        p.setName("p2");
        Query q = pm.newQuery(Person.class);
        q.declareParameters("String n");
        q.setFilter("name == n");
        List list = (List)q.execute("p1");
        Assert.assertEquals(0, list.size());
        q.closeAll();
        pm.close();
    }

    /**
     * Test a modified local instance that is marked for deletion
     *
     * @throws Exception
     */
    public void testQueryIgnoreCacheFalse3() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("p1");
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        pm.deletePersistent(p);
        Query q = pm.newQuery(Person.class);
        q.declareParameters("String n");
        q.setFilter("name == n");
        List list = (List)q.execute("p1");
        Assert.assertEquals(0, list.size());
        q.closeAll();
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testLocalQInheritance() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        EmpSuper empSuper = new EmpSuper();
        empSuper.setName("emp1");

        Employee emp = new Employee();
        emp.setName("emp2");

        TempEmployee tEmp = new TempEmployee();
        tEmp.setName("emp3");

        pm.currentTransaction().begin();
        pm.makePersistent(empSuper);
        pm.makePersistent(emp);
        pm.makePersistent(tEmp);

        List list = new ArrayList();
        list.add(empSuper);
        list.add(emp);
        list.add(tEmp);

        Query q = pm.newQuery(EmpSuper.class);
        q.declareParameters("String n");
        q.setFilter("name.startsWith(n)");
        q.setOrdering("name ascending");
        q.setCandidates(list);
        List list2 = (List)q.execute("emp");
        list2.size();
        Assert.assertEquals("emp1", ((EmpSuper)list2.get(0)).getName());
        Assert.assertTrue(list2.get(0).getClass().getName().equals(
                EmpSuper.class.getName()));

        Assert.assertEquals("emp2", ((EmpSuper)list2.get(1)).getName());
        Assert.assertTrue(list2.get(1).getClass().getName().equals(
                Employee.class.getName()));

        Assert.assertEquals("emp3", ((EmpSuper)list2.get(2)).getName());
        Assert.assertTrue(list2.get(2) instanceof TempEmployee);

        Assert.assertEquals(3, list2.size());
        q.closeAll();
        pm.close();
    }

    public void testLocalQInheritance2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("tLQI2");
        EmpSuper empSuper = new EmpSuper();
        empSuper.setName("emp1");
        empSuper.getSuperList().add(p);

        Employee emp = new Employee();
        emp.setName("emp2");
        emp.getSuperList().add(p);

        TempEmployee tEmp = new TempEmployee();
        tEmp.setName("emp3");
        tEmp.getSuperList().add(p);

        pm.currentTransaction().begin();
        pm.makePersistent(empSuper);
        pm.makePersistent(emp);
        pm.makePersistent(tEmp);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();

        List list = new ArrayList();
        list.add(empSuper);
        list.add(emp);
        list.add(tEmp);

        Query q = pm.newQuery(EmpSuper.class);
        q.declareParameters("String n, Person p");
        q.setFilter("name.startsWith(n) && superList.contains(p)");
        q.setOrdering("name ascending");
        q.setCandidates(list);
        List list2 = (List)q.execute("emp", p);
        list2.size();
        Assert.assertEquals("emp1", ((EmpSuper)list2.get(0)).getName());
        Assert.assertTrue(list2.get(0).getClass().getName().equals(
                EmpSuper.class.getName()));

        Assert.assertEquals("emp2", ((EmpSuper)list2.get(1)).getName());
        Assert.assertTrue(list2.get(1).getClass().getName().equals(
                Employee.class.getName()));

        Assert.assertEquals("emp3", ((EmpSuper)list2.get(2)).getName());
        Assert.assertTrue(list2.get(2).getClass().getName().equals(
                TempEmployee.class.getName()));

        Assert.assertEquals(3, list2.size());
        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    public void testLocalParamContainsQsAndCompare() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        List sList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            sList.add("s" + i);
        }

        Collection col = new ArrayList();
        Person p = new Person();
        p.setName("pSList");
        p.getStringList().addAll(sList);
        p.setVal("bla1");
        col.add(p);

        p = new Person();
        p.setName("pSList");
        p.getStringList().addAll(sList);
        p.setVal("bla2");
        col.add(p);

        p = new Person();
        p.setName("pSList");
        p.setVal("bla2");
        col.add(p);
        pm.makePersistentAll(col);

        Query q = pm.newQuery(Person.class);
        q.setCandidates(col);
        q.declareParameters("String s");
//        q.setFilter("stringList.contains(s)");
        q.setFilter("stringList.contains(s) && val == \"bla2\"");
        List result = (List)q.execute("s4");
        Assert.assertEquals(1, result.size());
        q.closeAll();
        pm.close();
    }

    public void testMultipleContainsQs() throws Exception {
        nuke(new Class[]{Person.class, EmpSuper.class});

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        List sList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            sList.add("s" + i);
        }

        Person p = new Person();
        p.setName("pSList");
        p.getStringList().addAll(sList);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.declareParameters("String s");
        q.setFilter("stringList.contains(s)");
        List result = (List)q.execute("s4");
        Assert.assertEquals(1, result.size());
        q.closeAll();

        p.setVal("bla1");
        q = pm.newQuery(Person.class);
        q.declareParameters("String s");
        q.setFilter("stringList.contains(s) && val == \"bla2\"");
        result = (List)q.execute("s4");
        Assert.assertEquals(0, result.size());
        q.closeAll();

        pm.currentTransaction().commit();
        q.closeAll();
        pm.close();
    }

    public void testLocalContainsWithUnboundVar() throws Exception {
        nuke(Person.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        List sList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            sList.add("s" + i);
        }
        p.getStringList().addAll(sList);
        pm.makePersistent(p);

        Query q = pm.newQuery(Person.class);
        q.declareVariables("String s");
        q.setFilter("stringList.contains(s)");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        q.closeAll();
        pm.close();
    }

    public void testLocalContainsWithUnboundVar2() throws Exception {
        nuke(Person.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();

        Person p = new Person();
        p.setName("p1");
        List sList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            sList.add(new Person("child" + i));
        }
        p.getPersonsList().addAll(sList);
        pm.makePersistent(p);

        Query q = pm.newQuery(Person.class);
        q.declareVariables("Person s");
        q.setFilter("personsList.contains(s)");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        q.closeAll();
        pm.close();
    }

    public void testLocalContainsWithInt() throws Exception {
        nuke(Person.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();

        Person p = new Person();
        p.setName("p1");
        List sList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Person child = new Person("child" + i);
            child.setIntField(i);
            sList.add(child);
        }
        p.getPersonsList().addAll(sList);
        pm.makePersistent(p);

        Query q = pm.newQuery(Person.class);
        q.declareVariables("Person s");
        q.setFilter("personsList.contains(s) && s.intField >= 3");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        q.closeAll();

        q = pm.newQuery(Person.class);
        q.declareVariables("Person s");
        q.setFilter("personsList.contains(s) && s.intField >= 11");
        results = (List)q.execute();
        Assert.assertEquals(0, results.size());
        q.closeAll();
        pm.close();
    }
}
