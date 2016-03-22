
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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.versant.core.jdo.VersantQuery;
import com.versant.core.jdo.*;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test3.model.*;
import com.versant.core.jdo.junit.test3.model.smart.*;
import com.versant.core.jdo.junit.test3.model.attachdetach.bug1113.B;
import com.versant.core.jdo.junit.test3.model.kai.SubA;
import com.versant.core.jdo.junit.test3.model.complex.*;
import com.versant.core.jdo.junit.test3.model.nav.NavRootSubA;
import com.versant.core.jdo.junit.test3.model.nav.NavRootSubARef1;
import com.versant.core.jdo.junit.test3.model.nav.NavRootSubARef2;
import com.versant.core.common.config.ConfigParser;
import com.versant.core.jdbc.JdbcConfig;
import com.versant.core.metadata.MetaDataEnums;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.metadata.JdbcMetaDataEnums;
import com.versant.core.jdbc.JdbcConfigParser;
import com.versant.core.jdbc.JdbcConfig;
import com.versant.core.jdbc.VersantClientJDBCConnection;

import javax.jdo.*;
import java.util.*;
import java.io.File;
import java.sql.Statement;

/**
 * More general tests.
 */
public class TestGeneral3 extends VersantTestCase {

    public TestGeneral3(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testCheckIdleConTestingDisabled",
            "testSimpleHierarchy",
            "testSimpleVerticalHierarchy2",
            "testSimpleVerticalHierarchy3",
            "testComplex1",
            "testComplexQueryq",
            "testComplexQueryq2",
            "testComplexQueryq3",
            "testComplexQueryq4",
            "testComplexQueryq5",
            "testComplexQueryNav",
            "testComplexQueryNav2",
            "testRefFieldQuery",
            "testNullRefFieldQuery",
            "testFKCollection",
            "testSimpleVerticalHierarchyAutoInc",
            "testSimpleVerticalHierarchyWithLOBs",
            "testSimpleVerticalHierarchyAppId",
            "testVarQuery1",
            "testNavQuery2",
            "testNavQuery3",
            "testNavContainsParam",
            "testCollQuery1",
            "testCollQuery2",
            "testForToManyJoins22",
            "testNonPrimitiveArrays",
            "testNonPrimitiveArraysNullElement",
            "testIntegerFieldNullValue",
            "testInverseForeignKeyArrays",
            "testStringDescriminator",
            "testStringDescriminatorName",
            "testStringDescriminatorFullName",
            "testNullWrapperAndPrim",
            "testKai1",
            "testBug1228",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestGeneral3(a[i]));
        }
        return s;
    }

    public void testParColFetch() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SmartA sa = new SmartA();
        sa.setVal("sa");
        SmartB sb = new SmartB();
        sb.setVal("sb");
        sa.setRefB(sb);

        SmartD sd = new SmartD();
        sd.setVal("sd");
        sd.getStringList().add("sl1");
        sd.getStringList().add("sl2");
        sd.getStringList().add("sl3");
        pm.makePersistent(sa);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(sa);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        sa = (SmartA) pm.getObjectById(id, true);
        sa.getRefB();
        pm.close();
    }

    public void testBug1228() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;
        com.versant.core.jdo.junit.test3.model.attachdetach.bug1113.A a = new com.versant.core.jdo.junit.test3.model.attachdetach.bug1113.A();
        B bb = new B();
        B bb1 = new B();
        B bb2 = new B();
        a.addElement(bb);
        a.addElement(bb1);
        a.addElement(bb2);

        VersantPersistenceManager pm = (VersantPersistenceManager) pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        pm.makePersistent(a);
        pm.currentTransaction().commit();
        pm.close();

        pm = (VersantPersistenceManager) pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setRestoreValues(false);
        pm.setIgnoreCache(false);

        pm.currentTransaction().begin();
        VersantQuery query = (VersantQuery) pm.newQuery(
                com.versant.core.jdo.junit.test3.model.attachdetach.bug1113.A.class);
        query.setFetchGroup("PDSTest");
        query.setRandomAccess(true);

        Collection coll = (Collection) query.execute();
        assertEquals(1, coll.size());

        pm.currentTransaction().commit();
    }

    public void testKai1() {
    	if (!isApplicationIdentitySupported())
    		return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SubA sa = new SubA();
        pm.makePersistent(sa);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery) pm.newQuery(SubA.class);
        q.setFetchGroup("fg");
        Collection col = (Collection) q.execute();

        List retrievedList = (List) ((VersantPersistenceManager)pm).versantDetachCopy(col, "fg");
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Make sure that testing of idle connections is off. This is not really
     * a test but just a check to make sure that the test harness is disabling
     * this. If it is true tests that count queries and so on fail randomly.
     */
    public void testCheckIdleConTestingDisabled() throws Exception {
        if (!isJdbc() || isRemote() || isDataSource()) {
            unsupported();
            return;
        }
        assertFalse(getJdbcConnectionPool().isTestWhenIdle());
    }

    private char primChar;
    private boolean primBoole;

    /**
     * Make sure that when we have a null wrapper, that we return a null, and
     * for primatives that is is never null, even when the database is messed up.
     */
    public void testNullWrapperAndPrim() throws Exception {
        if (!isJdbc() || isRemote()) {
            unsupported();
            return;
        }

        PersistenceManager pm = null;
        VersantClientJDBCConnection con;
        Statement s;
        try {
            pm = pmf().getPersistenceManager();
            pm.currentTransaction().begin();
            String sql = "insert into null_wrapper (" +
                    "null_wrapper_id, " +
                    "prim_bool, " +
                    "prim_char, " +
                    "wrap_bool, " +
                    "wrap_char, " +
                    "jdo_version) " +
                    "values (1, null, null, null, null, 1)";
            con = (VersantClientJDBCConnection)
                            ((VersantPersistenceManager)pm).getJdbcConnection(null);
            s = con.createStatement();
            s.executeUpdate(sql);
            s.close();
            con.close();
            pm.currentTransaction().commit();

            pm.currentTransaction().begin();
            Query q = pm.newQuery(NullWrapper.class);
            ArrayList a = new ArrayList((Collection) q.execute());
            q.closeAll();

            assertEquals(1, a.size());
            assertTrue(((NullWrapper)a.get(0)).getWrapBool() == null);
            assertTrue(((NullWrapper)a.get(0)).getWrapChar() == null);

            assertTrue(((NullWrapper)a.get(0)).getPrimChar() == primChar);
            assertTrue(((NullWrapper)a.get(0)).isPrimBool() == primBoole);

            pm.currentTransaction().commit();
        } finally {
            if (!pm.currentTransaction().isActive()){
                pm.currentTransaction().begin();
            }
            try {
                con = (VersantClientJDBCConnection)
                        ((VersantPersistenceManager) pm).getJdbcConnection(null);
                s = con.createStatement();
                int c = s.executeUpdate("delete from null_wrapper");
                s.close();
                con.close();
                Assert.assertEquals(1, c);
            } finally {
                pm.currentTransaction().commit();
                pm.close();
            }
        }
    }

    /**
     * Test hierarchy using {name} descriminator column values.
     */
    public void testStringDescriminatorName() throws Exception {
        if (!isJdbc() || isRemote()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        BaseStrCidName base = new BaseStrCidName("abase");
        SubStrCidName sub = new SubStrCidName("asub", 10);
        pm.makePersistent(base);
        pm.makePersistent(sub);
        pm.currentTransaction().commit();

        // make sure both come back ok
        pm.currentTransaction().begin();
        Query q = pm.newQuery(BaseStrCidName.class);
        q.setOrdering("name ascending");
        ArrayList a = new ArrayList((Collection)q.execute());
        q.closeAll();
        assertEquals(2, a.size());
        assertTrue(base == a.get(0));
        assertTrue(sub == a.get(1));
        pm.currentTransaction().commit();

        // make sure no subclasses on extent works
        pm.currentTransaction().begin();
        findExecQuerySQL();
        q = pm.newQuery(pm.getExtent(BaseStrCidName.class, false));
        a = new ArrayList((Collection)q.execute());
        String sql = findExecQuerySQL();
        assertTrue(sql.indexOf("'BaseStrCidName'") > 0); // make sure correct id used
        q.closeAll();
        assertEquals(1, a.size());
        assertTrue(base == a.get(0));
        pm.currentTransaction().commit();

        // make sure a class cast works to filter out base class instances
        // This is broken due to a bug with casting of 'this'.
        /*
        pm.currentTransaction().begin();
        q = pm.newQuery(BaseStrCidName.class, "((SubStrCidName)this).name.startsWith('a')");
        a = new ArrayList((Collection)q.execute());
        q.closeAll();
        assertEquals(1, a.size());
        assertTrue(sub == a.get(0));
        pm.currentTransaction().commit();
        */

        pm.close();
    }

    /**
     * Test hierarchy using {fullname} descriminator column values.
     */
    public void testStringDescriminatorFullName() throws Exception {
        if (!isJdbc() || isRemote()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        BaseStrCidFullName base = new BaseStrCidFullName("abase");
        SubStrCidFullName sub = new SubStrCidFullName("asub", 10);
        pm.makePersistent(base);
        pm.makePersistent(sub);
        pm.currentTransaction().commit();

        // make sure both come back ok
        pm.currentTransaction().begin();
        Query q = pm.newQuery(BaseStrCidFullName.class);
        q.setOrdering("name ascending");
        ArrayList a = new ArrayList((Collection)q.execute());
        q.closeAll();
        assertEquals(2, a.size());
        assertTrue(base == a.get(0));
        assertTrue(sub == a.get(1));
        pm.currentTransaction().commit();

        // make sure no subclasses on extent works
        pm.currentTransaction().begin();
        findExecQuerySQL();
        q = pm.newQuery(pm.getExtent(BaseStrCidFullName.class, false));
        a = new ArrayList((Collection)q.execute());
        String sql = findExecQuerySQL();
        assertTrue(sql.indexOf("'" + BaseStrCidFullName.class.getName() +
                "'") > 0); // make sure correct id used
        q.closeAll();
        assertEquals(1, a.size());
        assertTrue(base == a.get(0));
        pm.currentTransaction().commit();

        // make sure a class cast works to filter out base class instances
        // This is broken due to a bug with casting of 'this'.
        /*
        pm.currentTransaction().begin();
        q = pm.newQuery(BaseStrCid.class, "((SubStrCid)this).name.startsWith('a')");
        a = new ArrayList((Collection)q.execute());
        q.closeAll();
        assertEquals(1, a.size());
        assertTrue(sub == a.get(0));
        pm.currentTransaction().commit();
        */

        pm.close();
    }

    /**
     * Test hierarchy using string descriminator column values.
     */
    public void testStringDescriminator() throws Exception {
        if (!isJdbc() || isRemote()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        BaseStrCid base = new BaseStrCid("abase");
        SubStrCid sub = new SubStrCid("asub", 10);
        pm.makePersistent(base);
        pm.makePersistent(sub);
        pm.currentTransaction().commit();

        // make sure both come back ok
        pm.currentTransaction().begin();
        Query q = pm.newQuery(BaseStrCid.class);
        q.setOrdering("name ascending");
        ArrayList a = new ArrayList((Collection)q.execute());
        q.closeAll();
        assertEquals(2, a.size());
        assertTrue(base == a.get(0));
        assertTrue(sub == a.get(1));
        pm.currentTransaction().commit();

        // make sure no subclasses on extent works
        pm.currentTransaction().begin();
        findExecQuerySQL();
        q = pm.newQuery(pm.getExtent(BaseStrCid.class, false));
        a = new ArrayList((Collection)q.execute());
        String sql = findExecQuerySQL();
        assertTrue(sql.indexOf("'BASE'") > 0); // make sure correct id used
        q.closeAll();
        assertEquals(1, a.size());
        assertTrue(base == a.get(0));
        pm.currentTransaction().commit();

        // make sure a class cast works to filter out base class instances
        // This is broken due to a bug with casting of 'this'.
        /*
        pm.currentTransaction().begin();
        q = pm.newQuery(BaseStrCid.class, "((SubStrCid)this).name.startsWith('a')");
        a = new ArrayList((Collection)q.execute());
        q.closeAll();
        assertEquals(1, a.size());
        assertTrue(sub == a.get(0));
        pm.currentTransaction().commit();
        */

        pm.close();
    }

    /**
     * Test CRUD for PC[] mapped using an inverse foreign key.
     */
    public void testInverseForeignKeyArrays() throws Exception {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        InvFkArrayContainer con = new InvFkArrayContainer();
        InvFkArrayElement ea = new InvFkArrayElement(con, "a");
        InvFkArrayElement eb = new InvFkArrayElement(con, "b");
        InvFkArrayElement ec = new InvFkArrayElement(con, "c");
        con.setElements(new InvFkArrayElement[]{ea, eb, ec});
        pm.makePersistent(con);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("[a, b, c]", con.getElementsStr());
        con.getElements()[2] = null;
        ec.setParent(null);
        InvFkArrayElement ed = new InvFkArrayElement(con, "d");
        con.getElements()[2] = ed;
        JDOHelper.makeDirty(con, "elements");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("[a, b, d]", con.getElementsStr());
        con.getElements()[0].setParent(null);
        con.getElements()[1].setParent(null);
        con.getElements()[2].setParent(null);
        InvFkArrayElement ef = new InvFkArrayElement(con, "f");
        InvFkArrayElement eg = new InvFkArrayElement(con, "g");
        con.setElements(new InvFkArrayElement[]{ef, eg});
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("[f, g]", con.getElementsStr());
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test CRUD for Integer fields and null values.
     */
    public void testIntegerFieldNullValue() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        WrapperContainer con = new WrapperContainer();
        pm.makePersistent(con);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertNull(con.getIntw());
        con.setIntw(new Integer(20));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals(new Integer(20), con.getIntw());
        con.setIntw(null);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertNull(con.getIntw());
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testNonEmbeddedPrimitives() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ArrayContainer con = new ArrayContainer();
        con.setPints(new int[] {1});
        pm.makePersistent(con);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(1, con.getPints().length);
        pm.close();
    }

    /**
     * Test CRUD for arrays[] of non-primitives containing null elements.
     * When nulls are supported for Integer[] etc then this test needs to
     * be updated.
     */
    public void testNonPrimitiveArraysNullElement() throws Exception {
    	if (!isExtraJavaTypeSupported()) { // java.util.File
    		return;
   		}
    	
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ArrayContainer con = new ArrayContainer();
        con.setStrings(new String[]{null, "b", null});
//        con.setInts(new Integer[]{null, new Integer(2), null});
        con.setLocales(new Locale[]{null, new Locale("fr", "", ""), null});
        con.setFiles(new File[]{null, new File("22"), null});
        pm.makePersistent(con);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("[null, b, null]", con.getStringsStr());
//        assertEquals("[null, 2, null]", con.getIntsStr());
        assertEquals("[null, fr, null]", con.getLocalesStr());
        assertEquals("[null, 22, null]", con.getFilesStr());
        con.getStrings()[1] = null;
        con.getStrings()[2] = "d";
//        con.getInts()[1] = null;
//        con.getInts()[2] = new Integer(4);
        con.getLocales()[1] = null;
        con.getLocales()[2] = new Locale("es", "", "");
        con.getFiles()[1] = null;
        con.getFiles()[2] = new File("44");
        JDOHelper.makeDirty(con, "strings");
//        JDOHelper.makeDirty(con, "ints");
        JDOHelper.makeDirty(con, "locales");
        JDOHelper.makeDirty(con, "files");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("[null, null, d]", con.getStringsStr());
//        assertEquals("[null, null, 4]", con.getIntsStr());
        assertEquals("[null, null, es]", con.getLocalesStr());
        assertEquals("[null, null, 44]", con.getFilesStr());
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test CRUD for arrays[] of non-primitives.
     */
    public void testNonPrimitiveArrays() throws Exception {
    	if (!isExtraJavaTypeSupported()) {
    		return;
   		}

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ArrayContainer con = new ArrayContainer();
        con.setStrings(new String[]{"a", "b", "c"});
        con.setInts(new Integer[]{new Integer(1), new Integer(2), new Integer(3)});
        con.setLocales(new Locale[]{new Locale("en", ""), new Locale("fr", ""), new Locale("de", "")});
        con.setFiles(new File[]{new File("11"), new File("22"), new File("33")});
        pm.makePersistent(con);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("[a, b, c]", con.getStringsStr());
        assertEquals("[1, 2, 3]", con.getIntsStr());
        assertEquals("[en, fr, de]", con.getLocalesStr());
        assertEquals("[11, 22, 33]", con.getFilesStr());
        con.getStrings()[2] = "d";
        con.getInts()[2] = new Integer(4);
        con.getLocales()[2] = new Locale("es", "");
        con.getFiles()[2] = new File("44");
        JDOHelper.makeDirty(con, "strings");
        JDOHelper.makeDirty(con, "ints");
        JDOHelper.makeDirty(con, "locales");
        JDOHelper.makeDirty(con, "files");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("[a, b, d]", con.getStringsStr());
        assertEquals("[1, 2, 4]", con.getIntsStr());
        assertEquals("[en, fr, es]", con.getLocalesStr());
        assertEquals("[11, 22, 44]", con.getFilesStr());
        con.setStrings(new String[]{"f", "g"});
        con.setInts(new Integer[]{new Integer(6), new Integer(7)});
        con.setLocales(new Locale[]{new Locale("aa", ""), new Locale("bb", "")});
        con.setFiles(new File[]{new File("66"), new File("77")});
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals("[f, g]", con.getStringsStr());
        assertEquals("[6, 7]", con.getIntsStr());
        assertEquals("[aa, bb]", con.getLocalesStr());
        assertEquals("[66, 77]", con.getFilesStr());
        pm.deletePersistent(con);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testForToManyJoins22() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val1 = "" + System.currentTimeMillis() + "-1";
        String val2 = "" + System.currentTimeMillis() + "-2";

        NavRootSubARef2 nra2 = new NavRootSubARef2();
        nra2.setValBase(val1);
        nra2.setNavRootRef2("navRootRef2-" + val1);
        NavRootSubARef1 nra1 = new NavRootSubARef1("navRootSubARef1-" + val1,
                nra2);
        NavRootSubA nra = new NavRootSubA("navRootSubAVal-" + val1, nra1);
        pm.makePersistent(nra);

        nra2 = new NavRootSubARef2();
        nra2.setValBase(val2);
        nra2.setNavRootRef2("navRootRef2-" + val1);
        nra1 = new NavRootSubARef1("navRootSubARef1-" + val1, nra2);
        nra = new NavRootSubA("navRootSubAVal-" + val1, nra1);
        pm.makePersistent(nra);

        nra2 = new NavRootSubARef2();
        nra2.setValBase(val2 + "-not");
        nra2.setNavRootRef2("navRootRef2-" + val1);
        nra1 = new NavRootSubARef1("navRootSubARef1-" + val1, nra2);
        nra = new NavRootSubA("navRootSubAVal-" + val1, nra1);
        pm.makePersistent(nra);

        nra2 = new NavRootSubARef2();
        nra2.setVal(val2);
        nra2.setValBase("not");
        nra2.setNavRootRef2("navRootRef2-" + val1);
        nra1 = new NavRootSubARef1("navRootSubARef1-" + val1, nra2);
        nra = new NavRootSubA("navRootSubAVal-" + val1, nra1);
        pm.makePersistent(nra);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(NavRootSubA.class);
        q.setFilter("this.navRootSubARef1.navRootSubARef2.valBase == sp1 " +
                "|| this.navRootSubARef1.navRootSubARef2.valBase == sp2");
        q.declareParameters("String sp1, String sp2");
        List result = new ArrayList((List)q.execute(val1, val2));
        Assert.assertEquals(2, result.size());
        pm.close();
    }

    public void testCollQuery1() {
        String db = getDbName();
        if (db.equals("db2") || db.equals("informixse") || db.equals("informix")) {
            unsupported();
            return;
        }

        Collection col = new ArrayList();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();

        for (int j = 0; j < 3; j++) {
            AA_A aa_a = new AA_A();
            aa_a.setVal("aa_a-" + j + "-" + val);
            col.add(aa_a);

            for (int i = 0; i < 3; i++) {
                RefA refA = new RefA();
                refA.setVal(aa_a.getVal() + "-refa1-" + i);
                aa_a.getListRefA().add(refA);
                col.add(refA);
            }

            for (int i = 0; i < 3; i++) {
                RefB refB = new RefB();
                refB.setVal(aa_a.getVal() + "-refb1-" + i);
                aa_a.getRefBList().add(refB);
                col.add(refB);
            }

        }

        for (int j = 0; j < 2; j++) {
            AA_A aa_a = new AA_A();
            aa_a.setVal("aa_a-" + j + "-" + val);
            col.add(aa_a);

            for (int i = 0; i < 1000; i++) {
                RefA refA = new RefA();
                refA.setVal(aa_a.getVal() + "-refa-" + i);
                aa_a.getListRefA().add(refA);
                col.add(refA);
            }

            for (int i = 0; i < 1; i++) {
                RefB refB = new RefB();
                refB.setVal("not-" + aa_a.getVal() + "-refb-" + i);
                aa_a.getRefBList().add(refB);
                col.add(refB);
            }

        }

        for (int j = 0; j < 2; j++) {
            AA_A aa_a = new AA_A();
            aa_a.setVal("aa_a-" + j + "-" + val);
            col.add(aa_a);

            for (int i = 0; i < 1000; i++) {
                RefA refA = new RefA();
                refA.setVal("not-" + aa_a.getVal() + "-refa-" + i);
                aa_a.getListRefA().add(refA);
                col.add(refA);
            }

            for (int i = 0; i < 2; i++) {
                RefB refB = new RefB();
                refB.setVal(aa_a.getVal() + "-refb-" + i);
                aa_a.getRefBList().add(refB);
                col.add(refB);
            }

        }

        pm.makePersistentAll(col);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(AA_A.class);
        q.declareVariables("RefA refAVar; RefB refBVar");
        q.setFilter(
                "listRefA.contains(refAVar) && (refAVar.val.startsWith(\"aa_a-\")) " +
                "&& refBList.contains(refBVar) && (refBVar.val.startsWith(\"aa_a-\"))");

        List results = (List)q.execute();
        long timeStart = System.currentTimeMillis();
        Assert.assertEquals(3, results.size());
        System.out.println(
                "\n\n\n\n\n ***** timeStart = " + (System.currentTimeMillis() - timeStart));

        timeStart = System.currentTimeMillis();
        results = (List)q.execute();
        Assert.assertEquals(3, results.size());
        System.out.println(
                " ***** timeStart = " + (System.currentTimeMillis() - timeStart));

        timeStart = System.currentTimeMillis();
        results = (List)q.execute();
        Assert.assertEquals(3, results.size());
        System.out.println(
                " ***** timeStart = " + (System.currentTimeMillis() - timeStart));

        timeStart = System.currentTimeMillis();
        results = (List)q.execute();
        Assert.assertEquals(3, results.size());
        System.out.println(
                " ***** timeStart = " + (System.currentTimeMillis() - timeStart));

        for (int i = 0; i < results.size(); i++) {
            AA_A aa_a = (AA_A)results.get(i);
            Assert.assertEquals(3, aa_a.getListRefA().size());
            for (int j = 0; j < aa_a.getListRefA().size(); j++) {
                RefA refA = (RefA)aa_a.getListRefA().get(j);
                System.out.println("refA.getVal() = " + refA.getVal());
                Assert.assertTrue(
                        refA.getVal().startsWith(aa_a.getVal() + "-refa1-"));
            }
            Assert.assertEquals(3, aa_a.getRefBList().size());
        }
        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testCollQuery2() {
        Collection col = new ArrayList();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();

        for (int j = 0; j < 3; j++) {
            AA_A aa_a = new AA_A();
            aa_a.setVal("aa_a-" + j + "-" + val);
            col.add(aa_a);

            for (int i = 0; i < 3; i++) {
                RefA refA = new RefA();
                refA.setVal(aa_a.getVal() + "-refa-" + i);
                aa_a.getListRefA().add(refA);
                col.add(refA);
            }

            for (int i = 0; i < 3; i++) {
                RefB refB = new RefB();
                refB.setVal(aa_a.getVal() + "-refb-" + i);
                aa_a.getRefBList().add(refB);
                col.add(refB);
            }

        }

        for (int j = 0; j < 2; j++) {
            AA_A aa_a = new AA_A();
            aa_a.setVal("aa_a-" + j + "-" + val);
            col.add(aa_a);

            for (int i = 0; i < 1; i++) {
                RefA refA = new RefA();
                refA.setVal(aa_a.getVal() + "-refa-" + i);
                aa_a.getListRefA().add(refA);
                col.add(refA);
            }

            for (int i = 0; i < 1; i++) {
                RefB refB = new RefB();
                refB.setVal("not-" + aa_a.getVal() + "-refb-" + i);
                aa_a.getRefBList().add(refB);
                col.add(refB);
            }

        }

        for (int j = 0; j < 2; j++) {
            AA_A aa_a = new AA_A();
            aa_a.setVal("aa_a-" + j + "-" + val);
            col.add(aa_a);

            for (int i = 0; i < 1; i++) {
                RefA refA = new RefA();
                refA.setVal("not-" + aa_a.getVal() + "-refa-" + i);
                aa_a.getListRefA().add(refA);
                col.add(refA);
            }

            for (int i = 0; i < 2; i++) {
                RefB refB = new RefB();
                refB.setVal(aa_a.getVal() + "-refb-" + i);
                aa_a.getRefBList().add(refB);
                col.add(refB);
            }

        }

        pm.makePersistentAll(col);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(AA_A.class);
        q.declareVariables("RefA refAVar; RefB refBVar");
        q.setFilter(
                "listRefA.contains(refAVar) && (refAVar.val.startsWith(\"aa_a-\")) " +
                "|| refBList.contains(refBVar) && (refBVar.val.startsWith(\"aa_a-\"))");
        List results = (List)q.execute();
        Assert.assertEquals(7, results.size());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testVarQuery1() {
    	if (!isUnboundVariableSupported())
    	{
    		unsupported();
    		return;
    	}

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();

        A_A a_a1 = null;
        for (int i = 0; i < 5; i++) {
            a_a1 = new A_A();
            a_a1.setVal(val);
            pm.makePersistent(a_a1);
        }

        for (int i = 0; i < 5; i++) {
            a_a1 = new A_A();
            a_a1.setVal(val + "-s");
            pm.makePersistent(a_a1);
        }

        RefB refB = new RefB();
        refB.setVal(val);
        pm.makePersistent(refB);

        refB = new RefB();
        refB.setVal(val + "-not");
        pm.makePersistent(refB);

        RefA refA = new RefA();
        refA.setVal(val);
        pm.makePersistent(refA);

        refA = new RefA();
        refA.setVal(val + "-not");
        pm.makePersistent(refA);

        RefBase refBase = new RefA();
        refBase.setVal(val);
        pm.makePersistent(refBase);

        refBase = new RefA();
        refBase.setVal(val + "-not");
        pm.makePersistent(refBase);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(A_A.class);
        q.declareVariables("RefA refAVar");
        q.declareParameters("String varParam");
        q.setFilter("val == refAVar.val && val == varParam");
        List results = (List)q.execute(val);
        Assert.assertEquals(5, results.size());
        pm.close();
    }

    public void testNavQuery2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();

        A_A a_a1 = null;
        RefA refA = null;
        for (int i = 0; i < 5; i++) {
            a_a1 = new A_A();
            a_a1.setVal(val);

            refA = new RefA();
            refA.setVal(val);
            a_a1.setRefA(refA);
            pm.makePersistent(a_a1);
        }

        RefB refB = new RefB();
        refB.setVal(val);
        pm.makePersistent(refB);

        refB = new RefB();
        refB.setVal(val + "-not");
        pm.makePersistent(refB);

        refA = new RefA();
        refA.setVal(val);
        pm.makePersistent(refA);

        refA = new RefA();
        refA.setVal(val + "-not");
        pm.makePersistent(refA);

        RefBase refBase = new RefA();
        refBase.setVal(val);
        pm.makePersistent(refBase);

        refBase = new RefA();
        refBase.setVal(val + "-not");
        pm.makePersistent(refBase);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(A_A.class);
        q.declareParameters("String varParam");
        q.setFilter("refA.val == varParam");
        List results = (List)q.execute(val);
        Assert.assertEquals(5, results.size());
        pm.close();
    }

    public void testNavContainsParam() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();

        A_A a_a1 = null;
        RefA refA = null;
        for (int i = 0; i < 5; i++) {
            a_a1 = new A_A();
            a_a1.setVal(val);

            refA = new RefA();
            refA.setVal(val);
            a_a1.setRefA(refA);
            pm.makePersistent(a_a1);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        String[] params = new String[]{val, "bla"};
        Collection col = Arrays.asList(params);

        Query q = pm.newQuery(A_A.class);
        q.declareImports("import java.util.Collection;");
        q.declareParameters("Collection varParams");
        q.setFilter("varParams.contains(refA.val)");
        List results = (List)q.execute(col);
        Assert.assertEquals(5, results.size());
        pm.close();
    }

    public void testNavQuery3() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();

        A_A a_a1 = null;
        RefA refA = null;
        for (int i = 0; i < 5; i++) {
            a_a1 = new A_A();
            a_a1.setVal(val);

            refA = new RefA();

            RefA refA1 = new RefA();
            refA1.setVal(val);

            refA.setRefARef(refA1);

            a_a1.setRefA(refA);
            pm.makePersistent(a_a1);
        }

        RefB refB = new RefB();
        refB.setVal(val);
        pm.makePersistent(refB);

        refB = new RefB();
        refB.setVal(val + "-not");
        pm.makePersistent(refB);

        refA = new RefA();
        refA.setVal(val);
        pm.makePersistent(refA);

        refA = new RefA();
        refA.setVal(val + "-not");
        pm.makePersistent(refA);

        RefBase refBase = new RefA();
        refBase.setVal(val);
        pm.makePersistent(refBase);

        refBase = new RefA();
        refBase.setVal(val + "-not");
        pm.makePersistent(refBase);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(A_A.class);
        q.declareParameters("String varParam");
        q.setFilter("refA.refARef.val == varParam");
        List results = (List)q.execute(val);
        Assert.assertEquals(5, results.size());
        pm.close();
    }

    /**
     * Test insert/select/update/delete of a simple hierarchy mapped using
     * default inheritance.
     */
    public void testSimpleHierarchy() throws Exception {
        if (isJdbc() && !isRemote()) {

            // make sure the class meta data matches the properties for the
            // test i.e. the default project settings actually work

            String inheritance = getProperties().getProperty("versant.ext.jdbc-inheritance");
            assertNotNull(inheritance);
            JdbcMetaDataEnums mde = new JdbcMetaDataEnums();
            int expectedInheritance = ((Integer)mde.INHERITANCE_ENUM.get(inheritance)).intValue();
            assertTrue(expectedInheritance != 0);

            String jdbcClassId = getProperties().getProperty("versant.ext.jdbc-class-id");
            assertNotNull(jdbcClassId);
            JdbcConfigParser cp = new JdbcConfigParser();
            int expectedJdbcClassId = ((Integer)cp.JDBC_CLASS_ID_ENUM.get(jdbcClassId)).intValue();
            assertTrue(expectedJdbcClassId != 0);

            JdbcClass jdbcClass = getJdbcClass(SubClass.class);
            if (getJdbcSmf().getJdbcConfig().defaultClassId == JdbcConfig.DEFAULT_CLASS_ID_NO) {
                assertEquals(JdbcClass.INHERITANCE_VERTICAL, jdbcClass.inheritance);
            } else {
                assertEquals(expectedInheritance, jdbcClass.inheritance);
            }
            assertEquals(expectedJdbcClassId, getJdbcSmf().getJdbcConfig().defaultClassId);
            switch (expectedJdbcClassId) {
                case JdbcConfig.DEFAULT_CLASS_ID_NO:
                    assertNull(jdbcClass.classIdCol);
                    assertNull(jdbcClass.jdbcClassId);
                    break;
                case JdbcConfig.DEFAULT_CLASS_ID_HASH:
                    assertNotNull(jdbcClass.classIdCol);
                    assertTrue(jdbcClass.jdbcClassId instanceof Integer);
                    break;
                case JdbcConfig.DEFAULT_CLASS_ID_NAME:
                    assertNotNull(jdbcClass.classIdCol);
                    assertEquals("SubClass", jdbcClass.jdbcClassId);
                    break;
                case JdbcConfig.DEFAULT_CLASS_ID_FULLNAME:
                    assertNotNull(jdbcClass.classIdCol);
                    assertEquals(SubClass.class.getName(), jdbcClass.jdbcClassId);
                    break;
            }

            // also make sure that a class not in a hierarchy has no classIdCol
            assertNull(getJdbcClass(ArrayContainer.class).classIdCol);
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        // persist instance of BaseClass and SubClass
        pm.currentTransaction().begin();
        BaseClass bc = new BaseClass("bc");
        SubClass sc = new SubClass("sc", 10);
        BaseClass bcx = new BaseClass("bcx");
        SubClass scx = new SubClass("scx", 10);
        pm.makePersistent(bc);
        pm.makePersistent(sc);
        pm.makePersistent(bcx);
        pm.makePersistent(scx);
        pm.currentTransaction().commit();
        Object oidbc = pm.getObjectId(bc);
        Object oidsc = pm.getObjectId(sc);
        Object oidbcx = pm.getObjectId(bcx);
        Object oidscx = pm.getObjectId(scx);

        // get a new PM to clear local cache
        pm.close();

        pm = pmf().getPersistenceManager();

        // check the instances come back ok
        pm.currentTransaction().begin();
        bc = (BaseClass)pm.getObjectById(oidbc, true);
        System.out.println("\n\n\n\n ----- ");
        sc = (SubClass)pm.getObjectById(oidsc, true);
        Assert.assertEquals("bc", bc.getName());
        Assert.assertEquals("sc", sc.getName());
        Assert.assertEquals(10, sc.getAge());
        pm.currentTransaction().commit();

        // update each
        pm.currentTransaction().begin();
        bc.setName("bc2");
        sc.setName("sc2");
        sc.setAge(20);
        pm.currentTransaction().commit();

        // check that updates worked
        pm.currentTransaction().begin();
        Assert.assertEquals("bc2", bc.getName());
        Assert.assertEquals("sc2", sc.getName());
        Assert.assertEquals(20, sc.getAge());
        pm.currentTransaction().commit();

        // update only fields in the subclass
        pm.currentTransaction().begin();
        sc.setAge(30);
        pm.currentTransaction().commit();

        // check that update worked
        pm.currentTransaction().begin();
        Assert.assertEquals(30, sc.getAge());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(bc);
        pm.deletePersistent(sc);
        bcx = (BaseClass)pm.getObjectById(oidbcx, true);
        scx = (SubClass)pm.getObjectById(oidscx, true);
        pm.deletePersistent(bcx);
        pm.deletePersistent(scx);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test insert/select/update/delete of a simple 4 class hierarchy mapped
     * using vertical inheritance.
     */
    public void testSimpleVerticalHierarchy2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // persist instance of BaseClass and SubClass
        pm.currentTransaction().begin();
        BaseClass2 bc = new BaseClass2("bc");
        SubClass2 sc = new SubClass2("sc", 10);
        SubClass2A sca = new SubClass2A("sca", 20, "scaf2");
        SubClass2B scb = new SubClass2B("scb", 30, "scbf2");
        BaseClass2 bcx = new BaseClass2("bcx");
        SubClass2 scx = new SubClass2("scx", 10);
        pm.makePersistent(bc);
        pm.makePersistent(sc);
        pm.makePersistent(sca);
        pm.makePersistent(scb);
        pm.makePersistent(bcx);
        pm.makePersistent(scx);
        pm.currentTransaction().commit();
        Object oidbc = pm.getObjectId(bc);
        Object oidsc = pm.getObjectId(sc);
        Object oidsca = pm.getObjectId(sca);
        Object oidscb = pm.getObjectId(scb);
        Object oidbcx = pm.getObjectId(bcx);
        Object oidscx = pm.getObjectId(scx);

        // get a new PM to clear local cache
        pm.close();

//        if (true) return; // HACK

        pm = pmf().getPersistenceManager();

        // check the instances come back ok
        pm.currentTransaction().begin();
        bc = (BaseClass2)pm.getObjectById(oidbc, true);
        sc = (SubClass2)pm.getObjectById(oidsc, true);
        sca = (SubClass2A)pm.getObjectById(oidsca, true);
        scb = (SubClass2B)pm.getObjectById(oidscb, true);
        Assert.assertEquals("bc", bc.getName());

        Assert.assertEquals("sc", sc.getName());
        Assert.assertEquals(10, sc.getAge());

        Assert.assertEquals("sca", sca.getName());
        Assert.assertEquals(20, sca.getAge());
        Assert.assertEquals("scaf2", sca.getField2a());

        Assert.assertEquals("scb", scb.getName());
        Assert.assertEquals(30, scb.getAge());
        Assert.assertEquals("scbf2", scb.getField2b());

        pm.currentTransaction().commit();

//        if (true) {
//            pm.close();
//            return; // HACK
//        }

        // update each
        pm.currentTransaction().begin();
        bc.setName("bc2");
        sc.setName("sc2");
        sc.setAge(20);
        pm.currentTransaction().commit();

        // check that updates worked
        pm.currentTransaction().begin();
        Assert.assertEquals("bc2", bc.getName());
        Assert.assertEquals("sc2", sc.getName());
        Assert.assertEquals(20, sc.getAge());
        pm.currentTransaction().commit();

        // update only fields in the subclass
        pm.currentTransaction().begin();
        scb.setField2b("scbf2-2");
        pm.currentTransaction().commit();

        // check that update worked
        pm.currentTransaction().begin();
        Assert.assertEquals("scbf2-2", scb.getField2b());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(bc);
        pm.deletePersistent(sc);
        pm.deletePersistent(sca);
        pm.deletePersistent(scb);
        bcx = (BaseClass2)pm.getObjectById(oidbcx, true);
        scx = (SubClass2)pm.getObjectById(oidscx, true);
        pm.deletePersistent(bcx);
        pm.deletePersistent(scx);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test insert/select/update/delete of a simple 4 class hierarchy mapped
     * using vertical inheritance.
     */
    public void testSimpleVerticalHierarchy3() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // persist instance of BaseClass and SubClass
        pm.currentTransaction().begin();
        BaseClass2 bc = new BaseClass2("bc");
        SubClass2 sc = new SubClass2("sc", 10);
        SubClass2A sca = new SubClass2A("sca", 20, "scaf2");
        SubClass2B scb = new SubClass2B("scb", 30, "scbf2");
        BaseClass2 bcx = new BaseClass2("bcx");
        SubClass2 scx = new SubClass2("scx", 10);
        pm.makePersistent(bc);
        pm.makePersistent(sc);
        pm.makePersistent(sca);
        pm.makePersistent(scb);
        pm.makePersistent(bcx);
        pm.makePersistent(scx);
        pm.currentTransaction().commit();
        Object oidbc = pm.getObjectId(bc);
        Object oidsc = pm.getObjectId(sc);
        Object oidsca = pm.getObjectId(sca);
        Object oidscb = pm.getObjectId(scb);
        Object oidbcx = pm.getObjectId(bcx);
        Object oidscx = pm.getObjectId(scx);

        // get a new PM to clear local cache
        pm.close();

        pm = pmf().getPersistenceManager();

        // check the instances come back ok
        pm.currentTransaction().begin();
//        ((VersantPersistenceManager)pm).getObjectByIDString(oidscx.toString(), true, false);

        String ids = createNewIdFrom(SubClass2.class, oidsca);
        System.out.println("\n\n\n\n -- ids = " + ids);
        ((VersantPersistenceManager)pm).getObjectByIDString(ids, true, false);
        pm.close();
    }

    private String createNewIdFrom(Class cls, Object oidsca) {
        String ids = oidsca.toString();
        ids = ids.substring(ids.indexOf("-") + 1);
        int classId = pmf().getClassID(cls);
        ids = "" + classId + "-" + ids;
        return ids;
    }

    public void testComplex1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        AB_B ab_b = new AB_B();
        ab_b.setAb_b("ab_b");
        ab_b.setA_b("a_b");
        String val = "" + System.currentTimeMillis();
        ab_b.setVal(val);
        pm.makePersistent(ab_b);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(ab_b);
        pm.close();

        pm = pmf().getPersistenceManager();
        String newId = createNewIdFrom(A.class, id);
        pm.currentTransaction().begin();
        ab_b = (AB_B)((VersantPersistenceManager)pm).getObjectByIDString(
                newId, true, false);
        Assert.assertEquals("ab_b", ab_b.getAb_b());
        Assert.assertEquals("a_b", ab_b.getA_b());
        Assert.assertEquals(val, ab_b.getVal());
        pm.close();
    }

    public void testComplexQueryq() {
        String db = getSubStoreInfo().getDataStoreType();
        if (db.equals("firebird")) {
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        AB_B ab_b = new AB_B();
        ab_b.setAb_b("bla");
        ab_b.setA_b("bla");
        ab_b.setVal("bla");
        pm.makePersistent(ab_b);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(ab_b);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(AB_B.class);
        q.setFilter("val == \"bla\" && ab_b == \"bla\"");
        List results = (List)q.execute();
        Assert.assertEquals(1, results.size());
        pm.close();
    }

    public void testComplexQueryq2() {
        String db = getSubStoreInfo().getDataStoreType();
        if (db.equals("firebird")) {
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        A_B ab_b = new A_B();
        ab_b.setA_b("bla");
        ab_b.setVal(val);
        pm.makePersistent(ab_b);

        ab_b = new AB_B();
        ab_b.setVal(val);
        ab_b.setA_b("bla");
        pm.makePersistent(ab_b);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(ab_b);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(A_B.class);
        q.setFilter("val == valP && a_b == \"bla\"");
        q.declareParameters("String valP");
        List results = (List)q.execute(val);
        Assert.assertEquals(2, results.size());
        pm.close();
    }

    public void testComplexQueryq3() {
        String db = getSubStoreInfo().getDataStoreType();
        if (db.equals("firebird")) {
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        A_B ab_b = new A_B();
        ab_b.setA_b("bla");
        ab_b.setVal(val);
        pm.makePersistent(ab_b);

        ab_b = new AB_B();
        ab_b.setVal(val);
        ab_b.setA_b("bla");
        pm.makePersistent(ab_b);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(ab_b);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(pm.getExtent(A_B.class, false));
        q.setFilter("val == valP && a_b == \"bla\"");
        q.declareParameters("String valP");
        List results = (List)q.execute(val);
        Assert.assertEquals(1, results.size());
        pm.close();
    }

    public void testComplexQueryq4() {
        String db = getSubStoreInfo().getDataStoreType();
        if (db.equals("firebird")) {
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        A_B ab_b = new A_B();
        ab_b.setA_b("bla");
        ab_b.setVal(val);
        ab_b.getStringListBase().add("s1");
        pm.makePersistent(ab_b);

        ab_b = new AB_B();
        ab_b.setVal(val);
        ab_b.setA_b("bla");
        pm.makePersistent(ab_b);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(ab_b);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(pm.getExtent(A_B.class, false));
        q.setFilter(
                "stringListBase.contains(\"s1\") && val == valP && a_b == \"bla\"");
        q.declareParameters("String valP");
        List results = (List)q.execute(val);
        Assert.assertEquals(1, results.size());
        pm.close();
    }

    public void testComplexQueryq5() {
        String db = getSubStoreInfo().getDataStoreType();
        if (db.equals("firebird")) {
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        A_B ab_b = new A_B();
        ab_b.setA_b("bla");
        ab_b.setVal(val);
        ab_b.getStringListAB().add("s1");
        pm.makePersistent(ab_b);

        ab_b = new AB_B();
        ab_b.setVal(val);
        ab_b.setA_b("bla");
        pm.makePersistent(ab_b);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(ab_b);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(pm.getExtent(A_B.class, false));
        q.setFilter(
                "stringListAB.contains(\"s1\") && val == valP && a_b == \"bla\"");
        q.declareParameters("String valP");
        List results = (List)q.execute(val);
        Assert.assertEquals(1, results.size());

        ab_b = (A_B)results.get(0);
        Assert.assertEquals("s1", ab_b.getStringListAB().get(0));
        pm.close();
    }

    public void testComplexQueryNav() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        A_A a_a1 = new A_A();
        a_a1.setA_a("bla1");

        RefB refB = new RefB();
        refB.setVal("refB-" + val);
        a_a1.setDfgBaseRef(refB);

        RefBase refBase = new RefA();
        refBase.setVal("val1-" + val);
        ((RefA)refBase).setRefA("refA-" + val);
        a_a1.setBaseRef(refBase);
        pm.makePersistent(a_a1);

        A_A a_a = new A_A();
        a_a.setVal("val2-" + val);
        a_a.setA_a("bla2");
        refBase = new RefB();
        refBase.setVal("val-" + val);
        a_a.setBaseRef(refBase);
        pm.makePersistent(a_a);

        a_a = new A_A();
        a_a.setVal("val3-");
        a_a.setA_a("bla3");
        refBase = new RefB();
        refBase.setVal("val--" + val);
        a_a.setBaseRef(refBase);
        pm.makePersistent(a_a);

        pm.currentTransaction().commit();
        Object id = pm.getObjectId(a_a1);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        a_a = (A_A)pm.getObjectById(id, true);
        Assert.assertEquals("val1-" + val, a_a.getBaseRef().getVal());
        Assert.assertEquals("refA-" + val, ((RefA)a_a.getBaseRef()).getRefA());
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(A_A.class);
        q.setFilter("baseRef.val == valP");
        q.declareParameters("String valP");
        List results = (List)q.execute("val-" + val);
        Assert.assertEquals(1, results.size());
        a_a = (A_A)results.get(0);
        Assert.assertEquals("val2-" + val, a_a.getVal());

        pm.close();
    }

    public void testComplexQueryNav2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        A_A a_a1 = new A_A();

        RefB refB = new RefB();
        refB.setVal("refB-" + val);
        a_a1.setDfgBaseRef(refB);

        RefBase refBase = new RefA();
        refBase.setVal("val1-" + val);
        ((RefA)refBase).setRefA("refA-" + val);
        a_a1.setBaseRef(refBase);
        pm.makePersistent(a_a1);

        A_A a_a = new A_A();
        a_a.setVal("val2-" + val);
        refBase = new RefB();
        refBase.setVal("val-" + val);
        a_a.setBaseRef(refBase);

        RefB refBEntry = new RefB();
        refBEntry.setVal("refBEntry1-" + val);
        a_a.getRefBList().add(refBEntry);
        refBEntry = new RefB();
        refBEntry.setVal("refBEntry2-" + val);
        a_a.getRefBList().add(refBEntry);

        pm.makePersistent(a_a);

        a_a = new A_A();
        a_a.setVal("val3-");
        refBase = new RefB();
        refBase.setVal("val--" + val);
        a_a.setBaseRef(refBase);
        pm.makePersistent(a_a);

        pm.currentTransaction().commit();
        Object id = pm.getObjectId(a_a1);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        a_a = (A_A)pm.getObjectById(id, true);
        Assert.assertEquals("val1-" + val, a_a.getBaseRef().getVal());
        Assert.assertEquals("refA-" + val, ((RefA)a_a.getBaseRef()).getRefA());
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(A_A.class);
        ((VersantQuery)q).setFetchGroup("refbList");
        ((VersantQuery)q).setBounded(true);
        q.setFilter("baseRef.val == valP");
        q.declareParameters("String valP");
        System.out.println("\n\n\n\n\n\n before query");
        List results = (List)q.execute("val-" + val);
        Assert.assertEquals(1, results.size());
        countExecQueryEvents();
        a_a = (A_A)results.get(0);
        Assert.assertEquals(0, countExecQueryEvents());
        Assert.assertEquals("val2-" + val, a_a.getVal());
        if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
        Assert.assertEquals(2, a_a.getRefBList().size());
        ((RefB)a_a.getRefBList().get(0)).getVal();
        if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
        pm.close();
    }

    public void testComplexQueryNav3() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String val = "" + System.currentTimeMillis();
        A_A a_a1 = new A_A();

        RefB refB = new RefB();
        refB.setVal("refB-" + val);
        a_a1.setDfgBaseRef(refB);

        RefBase refBase = new RefA();
        refBase.setVal("val1-" + val);
        ((RefA)refBase).setRefA("refA-" + val);
        a_a1.setBaseRef(refBase);
        pm.makePersistent(a_a1);

        A_A a_a = new A_A();
        a_a.setVal("val2-" + val);
        refBase = new RefB();
        refBase.setVal("val-" + val);
        a_a.setBaseRef(refBase);

        RefB refBEntry = new RefB();
        refBEntry.setVal("refBEntry1-" + val);
        a_a.getRefBList().add(refBEntry);
        refBEntry = new RefB();
        refBEntry.setVal("refBEntry2-" + val);
        a_a.getRefBList().add(refBEntry);

        pm.makePersistent(a_a);

        a_a = new A_A();
        a_a.setVal("val3-");
        refBase = new RefB();
        refBase.setVal("val--" + val);
        a_a.setBaseRef(refBase);
        pm.makePersistent(a_a);

        pm.currentTransaction().commit();
        Object id = pm.getObjectId(a_a1);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        a_a = (A_A)pm.getObjectById(id, true);
        Assert.assertEquals("val1-" + val, a_a.getBaseRef().getVal());
        Assert.assertEquals("refA-" + val, ((RefA)a_a.getBaseRef()).getRefA());
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(A_A.class);
        ((VersantQuery)q).setFetchGroup("refbList");
        ((VersantQuery)q).setBounded(true);
        q.setFilter("baseRef.val == valP");
        q.declareParameters("String valP");
        List results = (List)q.execute("val-" + val);
        Assert.assertEquals(1, results.size());
        countExecQueryEvents();
        a_a = (A_A)results.get(0);
        Assert.assertEquals(0, countExecQueryEvents());
        Assert.assertEquals("val2-" + val, a_a.getVal());
        Assert.assertEquals(0, countExecQueryEvents());
        Assert.assertEquals(2, a_a.getRefBList().size());
        ((RefB)a_a.getRefBList().get(0)).getVal();
        Assert.assertEquals(0, countExecQueryEvents());
        pm.close();
    }

    public void testRefFieldQuery() {
        PersistenceManager pm = pmf().getPersistenceManager();
        String s = "" + System.currentTimeMillis();
        pm.currentTransaction().begin();

        A_A a_a = new A_A();
        RefA refA = new RefA();
        refA.setVal("refA-" + s);
        RefB refB = new RefB();
        refB.setVal("refB-" + s);
        a_a.setRefA(refA);
        a_a.setRefB(refB);
        pm.makePersistent(a_a);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(A_A.class);
        q.setFilter("refB == bParam");
        q.declareParameters("RefB bParam");
        List result = (List)q.execute(refB);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("refA-" + s,
                ((A_A)result.get(0)).getRefA().getVal());
        pm.close();
    }

    public void testNullRefFieldQuery() {
        PersistenceManager pm = pmf().getPersistenceManager();
        String s = "" + System.currentTimeMillis();
        pm.currentTransaction().begin();

        A_A a_a = new A_A();
        RefA refA = new RefA();
        refA.setVal("refA-" + s);
        RefB refB = new RefB();
        refB.setVal("refB-" + s);
        a_a.setRefA(refA);
        a_a.setRefB(null);
        pm.makePersistent(a_a);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(A_A.class);
        q.setFilter("refB == null && refA == aParam");
        q.declareParameters("RefB aParam");
        List result = (List)q.execute(refA);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("refA-" + s,
                ((A_A)result.get(0)).getRefA().getVal());
        pm.close();
    }

    public void testFKCollection() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String s = "" + System.currentTimeMillis();

        A_B ab = new A_B();
        ab.setVal("a1" + s);
        pm.makePersistent(ab);
        ab.getFkCol().add(new FKColEntryBase(ab, "fkBase" + ab.getVal()));
        ab.getFkCol().add(
                new FKColEntryA(ab, "fkA" + ab.getVal(), "valA" + ab.getVal()));
        ab.getFkCol().add(
                new FKColEntryB(ab, "fkB" + ab.getVal(), "valB" + ab.getVal()));
        Object id1 = pm.getObjectId(ab);

        ab = new A_B();
        ab.setVal("a2" + s);
        pm.makePersistent(ab);
        ab.getFkCol().add(new FKColEntryBase(ab, "fkBase" + ab.getVal()));
        ab.getFkCol().add(
                new FKColEntryA(ab, "fkA" + ab.getVal(), "valA" + ab.getVal()));
        ab.getFkCol().add(
                new FKColEntryB(ab, "fkB" + ab.getVal(), "valB" + ab.getVal()));

        Object id2 = pm.getObjectId(ab);

        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        ab = (A_B)pm.getObjectById(id1, true);
        Assert.assertEquals("a1" + s, ab.getVal());
        Assert.assertEquals(3, ab.getFkCol().size());
        List l = ab.getFkCol();
        boolean foundFKBase = false;
        boolean foundFKA = false;
        boolean foundFKB = false;
        for (int i = 0; i < l.size(); i++) {
            FKColEntryBase fkColEntryBase = (FKColEntryBase)l.get(i);
            Assert.assertTrue(fkColEntryBase.getValBase().endsWith("a1" + s));
            if (fkColEntryBase.getClass().equals(FKColEntryBase.class)) {
                foundFKBase = true;
            } else if (fkColEntryBase.getClass().equals(FKColEntryA.class)) {
                foundFKA = true;
            } else if (fkColEntryBase.getClass().equals(FKColEntryB.class)) {
                foundFKB = true;
            } else {
                throw new JDOFatalInternalException();
            }
        }

        if (!(foundFKBase && foundFKA && foundFKB)) {
            throw new JDOFatalInternalException();
        }

        foundFKBase = false;
        foundFKA = false;
        foundFKB = false;

        ab = (A_B)pm.getObjectById(id2, true);
        Assert.assertEquals("a2" + s, ab.getVal());
        Assert.assertEquals(3, ab.getFkCol().size());
        l = ab.getFkCol();
        for (int i = 0; i < l.size(); i++) {
            FKColEntryBase fkColEntryBase = (FKColEntryBase)l.get(i);
            Assert.assertTrue(fkColEntryBase.getValBase().endsWith("a2" + s));
            if (fkColEntryBase.getClass().equals(FKColEntryBase.class)) {
                foundFKBase = true;
            } else if (fkColEntryBase.getClass().equals(FKColEntryA.class)) {
                foundFKA = true;
            } else if (fkColEntryBase.getClass().equals(FKColEntryB.class)) {
                foundFKB = true;
            } else {
                throw new JDOFatalInternalException();
            }
        }

        if (!(foundFKBase && foundFKA && foundFKB)) {
            throw new JDOFatalInternalException();
        }
        pm.close();
    }

    /**
     * Test insert/select/update/delete of a simple hierarchy mapped using
     * vertical inheritance using an autoincrement column.
     */
    public void testSimpleVerticalHierarchyAutoInc() throws Exception {
        if (!doAutoIncTests()) return;

        PersistenceManager pm = pmf().getPersistenceManager();

        // persist instance of BaseClass and SubClass
        pm.currentTransaction().begin();
        BaseClassAutoInc bc = new BaseClassAutoInc("bc");
        SubClassAutoInc sc = new SubClassAutoInc("sc", 10);
        BaseClassAutoInc bcx = new BaseClassAutoInc("bcx");
        SubClassAutoInc scx = new SubClassAutoInc("scx", 10);
        pm.makePersistent(bc);
        pm.makePersistent(sc);
        pm.makePersistent(bcx);
        pm.makePersistent(scx);
        pm.currentTransaction().commit();
        Object oidbc = pm.getObjectId(bc);
        Object oidsc = pm.getObjectId(sc);
        Object oidbcx = pm.getObjectId(bcx);
        Object oidscx = pm.getObjectId(scx);

        // get a new PM to clear local cache
        pm.close();

        pm = pmf().getPersistenceManager();

        // check the instances come back ok
        pm.currentTransaction().begin();
        bc = (BaseClassAutoInc)pm.getObjectById(oidbc, true);
        sc = (SubClassAutoInc)pm.getObjectById(oidsc, true);
        Assert.assertEquals("bc", bc.getName());
        Assert.assertEquals("sc", sc.getName());
        Assert.assertEquals(10, sc.getAge());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(bc);
        pm.deletePersistent(sc);
        bcx = (BaseClassAutoInc)pm.getObjectById(oidbcx, true);
        scx = (SubClassAutoInc)pm.getObjectById(oidscx, true);
        pm.deletePersistent(bcx);
        pm.deletePersistent(scx);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test insert/select/update/delete of a simple hierarchy mapped using
     * vertical inheritance with both classes having LOB fields.
     */
    public void testSimpleVerticalHierarchyWithLOBs() throws Exception {
        if (getDbName().equals("informixse")) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        // persist instance of BaseClass and SubClass
        pm.currentTransaction().begin();
        BaseClassLOB bc = new BaseClassLOB("bc", "bclob");
        SubClassLOB sc = new SubClassLOB("sc", "sc-bclob", 10, "sclob");
        pm.makePersistent(bc);
        pm.makePersistent(sc);
        pm.currentTransaction().commit();
        Object oidbc = pm.getObjectId(bc);
        Object oidsc = pm.getObjectId(sc);

        // get a new PM to clear local cache
        pm.close();

//        if (true) return; // HACK

        pm = pmf().getPersistenceManager();

        // check the instances come back ok
        pm.currentTransaction().begin();
        bc = (BaseClassLOB)pm.getObjectById(oidbc, true);
        sc = (SubClassLOB)pm.getObjectById(oidsc, true);
        Assert.assertEquals("bc", bc.getName());
        Assert.assertEquals("bclob", bc.getBaseLob());
        Assert.assertEquals("sc", sc.getName());
        Assert.assertEquals("sc-bclob", sc.getBaseLob());
        Assert.assertEquals(10, sc.getAge());
        Assert.assertEquals("sclob", sc.getSubLob());
        pm.currentTransaction().commit();

        // update each
        pm.currentTransaction().begin();
        bc.setName("bc2");
        bc.setBaseLob("bclob2");
        sc.setName("sc2");
        sc.setBaseLob("sc-bclob2");
        sc.setAge(20);
        sc.setSubLob("sclob2");
        pm.currentTransaction().commit();

        // check that updates worked
        pm.currentTransaction().begin();
        Assert.assertEquals("bc2", bc.getName());
        Assert.assertEquals("bclob2", bc.getBaseLob());
        Assert.assertEquals("sc2", sc.getName());
        Assert.assertEquals("sc-bclob2", sc.getBaseLob());
        Assert.assertEquals(20, sc.getAge());
        Assert.assertEquals("sc-bclob2", sc.getBaseLob());
        pm.currentTransaction().commit();

        // update only fields in the subclass
        pm.currentTransaction().begin();
        sc.setAge(30);
        pm.currentTransaction().commit();

        // check that update worked
        pm.currentTransaction().begin();
        Assert.assertEquals(30, sc.getAge());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(bc);
        pm.deletePersistent(sc);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test insert/select/update/delete of a simple hierarchy mapped using
     * vertical inheritance using application identity with composite pk.
     */
    public void testSimpleVerticalHierarchyAppId() throws Exception {
        if (!isApplicationIdentitySupported()) return;

        PersistenceManager pm = pmf().getPersistenceManager();

        // persist instance of BaseClass and SubClass
        pm.currentTransaction().begin();
        BaseClassAppId bc = new BaseClassAppId(1, 2, "bc");
        SubClassAppId sc = new SubClassAppId(3, 4, "sc", 10);
        BaseClassAppId bcx = new BaseClassAppId(5, 6, "bcx");
        SubClassAppId scx = new SubClassAppId(7, 8, "scx", 10);
        pm.makePersistent(bc);
        pm.makePersistent(sc);
        pm.makePersistent(bcx);
        pm.makePersistent(scx);
        pm.currentTransaction().commit();
        Object oidbc = pm.getObjectId(bc);
        Object oidsc = pm.getObjectId(sc);
        Object oidbcx = pm.getObjectId(bcx);
        Object oidscx = pm.getObjectId(scx);

        // get a new PM to clear local cache
        pm.close();

        pm = pmf().getPersistenceManager();

        // check the instances come back ok
        pm.currentTransaction().begin();
        bc = (BaseClassAppId)pm.getObjectById(oidbc, true);
        sc = (SubClassAppId)pm.getObjectById(oidsc, true);
        Assert.assertEquals("bc", bc.getName());
        Assert.assertEquals("sc", sc.getName());
        Assert.assertEquals(10, sc.getAge());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(bc);
        pm.deletePersistent(sc);
        bcx = (BaseClassAppId)pm.getObjectById(oidbcx, true);
        scx = (SubClassAppId)pm.getObjectById(oidscx, true);
        pm.deletePersistent(bcx);
        pm.deletePersistent(scx);
        pm.currentTransaction().commit();

        pm.close();
    }

}
