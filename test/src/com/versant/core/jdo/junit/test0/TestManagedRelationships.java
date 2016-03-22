
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
import com.versant.core.common.VersantNullElementException;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test0.model.*;

import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.*;

/**
 * Tests for one-to-many and many-to-many managed relationships.
 */
public class TestManagedRelationships extends VersantTestCase {

    public TestManagedRelationships(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite("ManagedRelationships");
        String[] a = new String[]{
            "testManyToManyHashSet",
            "testManyToManyArrayList",
            "testManyToManyList",
            "testManyToManyVector",
            "testManyToManyLinkedList",
            "testManyToManyListWithInheritance",
            "testManyToManyTreeSet",
            "testMasterDetailAL",
            "testMasterDetailAllAL",
            "testMasterDetailManagedAL",
            "testMasterDetailSet",
            "testMasterDetailAllSet",
            "testMasterDetailManagedSet",
            "testMasterDetailHashSetSpes",
            "testMasterDetailVector",
            "testMasterDetailAllVector",
            "testMasterDetailManagedVector",
            "testMasterDetailLL",
            "testMasterDetailAllLL",
            "testMasterDetailManagedLL",
            "testMasterDetail2",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestManagedRelationships(a[i]));
        }
        return s;
    }

    /**
     * Holds instances used to test many-to-many with HashSet and includes
     * methods to check the state of the model.
     */
    private static class ManyToManyHashSetModel {
        final boolean mysql;
        final GroupHS admin = new GroupHS("admin");
        final GroupHS grunt = new GroupHS("grunt");
        final UserHS carl = new UserHS("carl");
        final UserHS jaco = new UserHS("jaco");

        public ManyToManyHashSetModel(boolean mysql) {
            this.mysql = mysql;
        }

        /**
         * Check the model is consistent now.
         */
        void check(String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            Assert.assertEquals(adminUsers, admin.getUsersString());
            Assert.assertEquals(gruntUsers, grunt.getUsersString());
            Assert.assertEquals(carlGroups, carl.getGroupsString());
            Assert.assertEquals(jacoGroups, jaco.getGroupsString());
        }

        /**
         * Check the model is consistent now and in the next tx.
         */
        void checkNowAndNextTx(PersistenceManager pm,
                String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            checkNowAndNextTx(pm, adminUsers, gruntUsers,
                    carlGroups, jacoGroups, 0);
        }

        /**
         * Check the model is consistent now and in the next tx.
         */
        void checkNowAndNextTx(PersistenceManager pm,
                String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups, int ms) {
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
            try {
                if (mysql && ms > 0) Thread.sleep(ms);
            } catch (InterruptedException e) {
                // ignore
            }
            pm.currentTransaction().begin();
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
        }
    }

    /**
     * Test many-to-many implemented with HashSet's.
     */
    public void testManyToManyHashSet() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	pmf().clearConnectionPool(null);

        PersistenceManager pm = pmf().getPersistenceManager();

        ManyToManyHashSetModel m =
                new ManyToManyHashSetModel(isMySQL3());

        System.out.println("\n*** admin.add(carl) before makePersistent");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        pm.makePersistent(m.admin); // must complete relationship
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.remove(carl)");
        pm.currentTransaction().begin();
        m.admin.remove(m.carl);
        m.checkNowAndNextTx(pm, "", "", "", "");

        System.out.println("\n*** admin.add(carl)");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.add(carl,jaco) grunt.add(jaco)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.add(m.carl);
        m.admin.add(m.jaco);
        m.grunt.add(m.jaco);
        m.checkNowAndNextTx(pm, "carl jaco", "jaco", "admin", "admin grunt");

        System.out.println("\n*** admin.remove(carl) grunt.add(carl)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.remove(m.carl);
        m.grunt.add(m.carl);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt",
                30000);

        System.out.println("\n*** admin.getUsers().clear()");
        pm.currentTransaction().begin();
        m.admin.getUsers().clear();
        m.checkNowAndNextTx(pm, "", "carl jaco", "grunt", "grunt");

        System.out.println("\n*** grunt.getUsers().retainAll(jaco)");
        pm.currentTransaction().begin();
        ArrayList a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().retainAll(a);
        m.checkNowAndNextTx(pm, "", "jaco", "", "grunt", 30000);

        System.out.println("\n*** grunt.getUsers().removeAll(jaco)");
        pm.currentTransaction().begin();
        m.grunt.add(m.carl);
        a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().removeAll(a);
        m.checkNowAndNextTx(pm, "", "carl", "grunt", "");

        System.out.println("\n*** jaco.getGroups().addAll(admin,grunt)");
        pm.currentTransaction().begin();
        a = new ArrayList();
        a.add(m.admin);
        a.add(m.grunt);
        m.jaco.getGroups().addAll(a);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt");

        System.out.println("\n*** deletePersistent(grunt)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.grunt);
        Assert.assertEquals("", m.carl.getGroupsString());
        Assert.assertEquals("admin", m.jaco.getGroupsString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("", m.carl.getGroupsString());
        Assert.assertEquals("admin", m.jaco.getGroupsString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(carl)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.carl);
        Assert.assertEquals("jaco", m.admin.getUsersString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("jaco", m.admin.getUsersString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(admin,jaco)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.admin);
        pm.deletePersistent(m.jaco);
        pm.currentTransaction().commit();

//        System.out.println("\n*** create new model for transient add test");
//        pm.currentTransaction().begin();
//        m = new ManyToManyHashSetModel(getSqlDriver().getName().equals("mysql"));
//        m.admin.add(m.carl);
//        pm.makePersistent(m.admin);
//        m.checkNowAndNextTx(pm, "carl", "", "admin", "");
//
//        System.out.println("\n*** makeTransient carl");
//        pm.currentTransaction().begin();
//        System.out.println("m.admin.getUsersString() = " + m.admin.getUsersString());
//        System.out.println("m.carl.getGroupsString() = " + m.carl.getGroupsString());
//        Object oidCarl = pm.getObjectId(m.carl);
//        Object oidAdmin = pm.getObjectId(m.admin);
//        pm.makeTransient(m.carl);
//        m.carl.add(new GroupHS("newgroup"));
//        pm.currentTransaction().commit();
//
//        System.out.println("\n*** deletePersistent(admin,carl)");
//        pm.currentTransaction().begin();
//        pm.deletePersistent(pm.getObjectById(oidAdmin, true));
//        pm.deletePersistent(pm.getObjectById(oidCarl, true));
//        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Holds instances used to test many-to-many with HashSet and includes
     * methods to check the state of the model.
     */
    private static class ManyToManyTreeSetModel {

        final boolean mysql;
        final GroupTS admin = new GroupTS("admin");
        final GroupTS grunt = new GroupTS("grunt");
        final UserTS carl = new UserTS("carl");
        final UserTS jaco = new UserTS("jaco");

        public ManyToManyTreeSetModel(boolean mysql) {
            this.mysql = mysql;
        }

        /**
         * Check the model is consistent now.
         */
        void check(String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            Assert.assertEquals(adminUsers, admin.getUsersString());
            Assert.assertEquals(gruntUsers, grunt.getUsersString());
            Assert.assertEquals(carlGroups, carl.getGroupsString());
            Assert.assertEquals(jacoGroups, jaco.getGroupsString());
        }

        /**
         * Check the model is consistent now and in the next tx.
         */
        void checkNowAndNextTx(PersistenceManager pm,
                String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            checkNowAndNextTx(pm, adminUsers, gruntUsers,
                    carlGroups, jacoGroups, 0);
        }

        /**
         * Check the model is consistent now and in the next tx.
         */
        void checkNowAndNextTx(PersistenceManager pm,
                String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups, int ms) {
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
            try {
                if (mysql && ms > 0) Thread.sleep(ms);
            } catch (InterruptedException e) {
                // ignore
            }
            pm.currentTransaction().begin();
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
        }
    }

    /**
     * Test many-to-many implemented with TreeSet's.
     */
    public void testManyToManyTreeSet() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	pmf().clearConnectionPool(null);

        PersistenceManager pm = pmf().getPersistenceManager();

        ManyToManyTreeSetModel m
                = new ManyToManyTreeSetModel(isMySQL3());

        System.out.println("\n*** admin.add(carl) before makePersistent");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        pm.makePersistent(m.admin); // must complete relationship
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.remove(carl)");
        pm.currentTransaction().begin();
        m.admin.remove(m.carl);
        m.checkNowAndNextTx(pm, "", "", "", "");

        System.out.println("\n*** admin.add(carl)");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.add(carl,jaco) grunt.add(jaco)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.add(m.carl);
        m.admin.add(m.jaco);
        m.grunt.add(m.jaco);
        m.checkNowAndNextTx(pm, "carl jaco", "jaco", "admin", "admin grunt");

        System.out.println("\n*** admin.remove(carl) grunt.add(carl)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.remove(m.carl);
        m.grunt.add(m.carl);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt",
                30000);

        System.out.println("\n*** admin.getUsers().clear()");
        pm.currentTransaction().begin();
        m.admin.getUsers().clear();
        m.checkNowAndNextTx(pm, "", "carl jaco", "grunt", "grunt");

        System.out.println("\n*** grunt.getUsers().retainAll(jaco)");
        pm.currentTransaction().begin();
        ArrayList a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().retainAll(a);
        m.checkNowAndNextTx(pm, "", "jaco", "", "grunt", 30000);

        System.out.println("\n*** grunt.getUsers().removeAll(jaco)");
        pm.currentTransaction().begin();
        m.grunt.add(m.carl);
        a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().removeAll(a);
        m.checkNowAndNextTx(pm, "", "carl", "grunt", "");

        System.out.println("\n*** jaco.getGroups().addAll(admin,grunt)");
        pm.currentTransaction().begin();
        a = new ArrayList();
        a.add(m.admin);
        a.add(m.grunt);
        m.jaco.getGroups().addAll(a);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt");

        System.out.println("\n*** deletePersistent(grunt)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.grunt);
        Assert.assertEquals("", m.carl.getGroupsString());
        Assert.assertEquals("admin", m.jaco.getGroupsString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("", m.carl.getGroupsString());
        Assert.assertEquals("admin", m.jaco.getGroupsString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(carl)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.carl);
        Assert.assertEquals("jaco", m.admin.getUsersString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("jaco", m.admin.getUsersString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(admin,jaco)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.admin);
        pm.deletePersistent(m.jaco);
        pm.currentTransaction().commit();

//        System.out.println("\n*** create new model for transient add test");
//        pm.currentTransaction().begin();
//        m = new ManyToManyHashSetModel(getSqlDriver().getName().equals("mysql"));
//        m.admin.add(m.carl);
//        pm.makePersistent(m.admin);
//        m.checkNowAndNextTx(pm, "carl", "", "admin", "");
//
//        System.out.println("\n*** makeTransient carl");
//        pm.currentTransaction().begin();
//        System.out.println("m.admin.getUsersString() = " + m.admin.getUsersString());
//        System.out.println("m.carl.getGroupsString() = " + m.carl.getGroupsString());
//        Object oidCarl = pm.getObjectId(m.carl);
//        Object oidAdmin = pm.getObjectId(m.admin);
//        pm.makeTransient(m.carl);
//        m.carl.add(new GroupHS("newgroup"));
//        pm.currentTransaction().commit();
//
//        System.out.println("\n*** deletePersistent(admin,carl)");
//        pm.currentTransaction().begin();
//        pm.deletePersistent(pm.getObjectById(oidAdmin, true));
//        pm.deletePersistent(pm.getObjectById(oidCarl, true));
//        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Holds instances used to test many-to-many with ArrayList
     * and includes methods to check the state of the model.
     */
    private static class ManyToManyArrayListModel {

        final GroupAL admin = new GroupAL("admin");
        final GroupAL grunt = new GroupAL("grunt");
        final UserAL carl = new UserAL("carl");
        final UserAL jaco = new UserAL("jaco");

        /**
         * Check the model is consistent now.
         */
        void check(String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            Assert.assertEquals(adminUsers, admin.getUsersString());
            Assert.assertEquals(gruntUsers, grunt.getUsersString());
            Assert.assertEquals(carlGroups, carl.getGroupsString());
            Assert.assertEquals(jacoGroups, jaco.getGroupsString());
        }

        /**
         * Check the model is consistent now and in the next tx.
         */
        void checkNowAndNextTx(PersistenceManager pm,
                String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
            pm.currentTransaction().begin();
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
        }
    }

    /**
     * Test many-to-many implemented with ArrayList.
     */
    public void testManyToManyArrayList() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();

        ManyToManyArrayListModel m = new ManyToManyArrayListModel();

        System.out.println("\n*** admin.add(carl) before makePersistent");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        pm.makePersistent(m.admin); // must complete relationship
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.remove(carl)");
        pm.currentTransaction().begin();
        m.admin.remove(m.carl);
        m.checkNowAndNextTx(pm, "", "", "", "");

        System.out.println("\n*** admin.add(carl)");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.add(jaco) grunt.add(jaco)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.add(m.jaco);
        m.grunt.add(m.jaco);
        m.checkNowAndNextTx(pm, "carl jaco", "jaco", "admin", "admin grunt");

        System.out.println("\n*** admin.remove(carl) grunt.add(carl)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.remove(m.carl);
        m.grunt.add(m.carl);
        m.checkNowAndNextTx(pm, "jaco", "jaco carl", "grunt", "admin grunt");

        System.out.println("\n*** admin.getUsers().clear()");
        pm.currentTransaction().begin();
        m.admin.getUsers().clear();
        m.checkNowAndNextTx(pm, "", "jaco carl", "grunt", "grunt");

        System.out.println("\n*** grunt.getUsers().retainAll(jaco)");
        pm.currentTransaction().begin();
        ArrayList a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().retainAll(a);
        m.checkNowAndNextTx(pm, "", "jaco", "", "grunt");

        System.out.println("\n*** grunt.getUsers().removeAll(jaco)");
        pm.currentTransaction().begin();
        m.grunt.add(m.carl);
        a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().removeAll(a);
        m.checkNowAndNextTx(pm, "", "carl", "grunt", "");

        System.out.println("\n*** jaco.getGroups().addAll(admin,grunt)");
        pm.currentTransaction().begin();
        a = new ArrayList();
        a.add(m.admin);
        a.add(m.grunt);
        m.jaco.getGroups().addAll(a);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt");

        System.out.println("\n*** grunt.remove(carl) grunt.add(0, carl)");
        pm.currentTransaction().begin();
        m.grunt.remove(m.carl);
        m.check("jaco", "jaco", "", "admin grunt");
        m.grunt.getUsers().add(0, m.carl);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt");

        System.out.println("\n*** admin.getUsers().set(0, carl)");
        pm.currentTransaction().begin();
        m.admin.getUsers().set(0, m.carl);
        m.checkNowAndNextTx(pm, "carl", "carl jaco", "admin grunt", "grunt");

        System.out.println("\n*** deletePersistent(grunt)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.grunt);
        Assert.assertEquals("admin", m.carl.getGroupsString());
        Assert.assertEquals("", m.jaco.getGroupsString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("admin", m.carl.getGroupsString());
        Assert.assertEquals("", m.jaco.getGroupsString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(carl)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.carl);
        Assert.assertEquals("", m.admin.getUsersString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("", m.admin.getUsersString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(admin,jaco)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.admin);
        pm.deletePersistent(m.jaco);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Holds instances used to test many-to-many with List
     * and includes methods to check the state of the model.
     */
    private static class ManyToManyListModel {

        final GroupL admin = new GroupL("admin");
        final GroupL grunt = new GroupL("grunt");
        final UserL carl = new UserL("carl");
        final UserL jaco = new UserL("jaco");

        /**
         * Check the model is consistent now.
         */
        void check(String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            Assert.assertEquals(adminUsers, admin.getUsersString());
            Assert.assertEquals(gruntUsers, grunt.getUsersString());
            Assert.assertEquals(carlGroups, carl.getGroupsString());
            Assert.assertEquals(jacoGroups, jaco.getGroupsString());
        }

        /**
         * Check the model is consistent now and in the next tx.
         */
        void checkNowAndNextTx(PersistenceManager pm,
                String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
            pm.currentTransaction().begin();
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
        }
    }

    /**
     * Test many-to-many implemented with an List.
     */
    public void testManyToManyList() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        ManyToManyListModel m = new ManyToManyListModel();

        System.out.println("\n*** admin.add(carl) before makePersistent");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        pm.makePersistent(m.admin); // must complete relationship
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.remove(carl)");
        pm.currentTransaction().begin();
        m.admin.remove(m.carl);
        m.checkNowAndNextTx(pm, "", "", "", "");

        System.out.println("\n*** admin.add(carl)");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.add(jaco) grunt.add(jaco)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.add(m.jaco);
        m.grunt.add(m.jaco);
        m.checkNowAndNextTx(pm, "carl jaco", "jaco", "admin", "admin grunt");

        System.out.println("\n*** admin.remove(carl) grunt.add(carl)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.remove(m.carl);
        m.grunt.add(m.carl);
        m.checkNowAndNextTx(pm, "jaco", "jaco carl", "grunt", "admin grunt");

        System.out.println("\n*** admin.getUsers().clear()");
        pm.currentTransaction().begin();
        m.admin.getUsers().clear();
        m.checkNowAndNextTx(pm, "", "jaco carl", "grunt", "grunt");

        System.out.println("\n*** grunt.getUsers().retainAll(jaco)");
        pm.currentTransaction().begin();
        ArrayList a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().retainAll(a);
        m.checkNowAndNextTx(pm, "", "jaco", "", "grunt");

        System.out.println("\n*** query for User's with no Group's");
        pm.currentTransaction().begin();
        Query q = pm.newQuery(UserL.class, "groups.isEmpty()");
        Iterator i = ((Collection)q.execute()).iterator();
        Assert.assertTrue(i.hasNext());
        Assert.assertTrue(i.next() == m.carl);
        Assert.assertTrue(!i.hasNext());
        q.closeAll();
        pm.currentTransaction().commit();

        System.out.println("\n*** query for User's containing a Group param");
        pm.currentTransaction().begin();
        q = pm.newQuery(UserL.class, "groups.contains(p)");
        q.declareParameters("GroupL p");
        i = ((Collection)q.execute(m.grunt)).iterator();
        Assert.assertTrue(i.hasNext());
        Assert.assertTrue(i.next() == m.jaco);
        Assert.assertTrue(!i.hasNext());
        q.closeAll();
        pm.currentTransaction().commit();

        System.out.println("\n*** grunt.getUsers().removeAll(jaco)");
        pm.currentTransaction().begin();
        m.grunt.add(m.carl);
        a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().removeAll(a);
        m.checkNowAndNextTx(pm, "", "carl", "grunt", "");

        System.out.println("\n*** jaco.getGroups().addAll(admin,grunt)");
        pm.currentTransaction().begin();
        a = new ArrayList();
        a.add(m.admin);
        a.add(m.grunt);
        m.jaco.getGroups().addAll(a);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt");

        System.out.println("\n*** grunt.remove(carl) grunt.add(0, carl)");
        pm.currentTransaction().begin();
        m.grunt.remove(m.carl);
        m.check("jaco", "jaco", "", "admin grunt");
        m.grunt.getUsers().add(0, m.carl);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt");

        System.out.println("\n*** admin.getUsers().set(0, carl)");
        pm.currentTransaction().begin();
        m.admin.getUsers().set(0, m.carl);
        m.checkNowAndNextTx(pm, "carl", "carl jaco", "admin grunt", "grunt");

//        System.out.println("\n*** swap element 0 and 1 of grunt.getUsers()");
//        pm.currentTransaction().begin();
//        Object t = m.grunt.getUsers().get(0);
//        m.grunt.getUsers().set(0, m.grunt.getUsers().get(1));
//        m.grunt.getUsers().set(1, t);
//        m.checkNowAndNextTx(pm, "carl", "jaco carl", "admin grunt", "grunt");

        System.out.println("\n*** deletePersistent(grunt)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.grunt);
        Assert.assertEquals("admin", m.carl.getGroupsString());
        Assert.assertEquals("", m.jaco.getGroupsString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("admin", m.carl.getGroupsString());
        Assert.assertEquals("", m.jaco.getGroupsString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(carl)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.carl);
        Assert.assertEquals("", m.admin.getUsersString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("", m.admin.getUsersString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(admin,jaco)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.admin);
        pm.deletePersistent(m.jaco);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Holds instances used to test many-to-many with Vector and HashSet
     * and includes methods to check the state of the model.
     */
    private static class ManyToManyVectorModel {

        final GroupV admin = new GroupV("admin");
        final GroupV grunt = new GroupV("grunt");
        final UserV carl = new UserV("carl");
        final UserV jaco = new UserV("jaco");

        /**
         * Check the model is consistent now.
         */
        void check(String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            Assert.assertEquals(adminUsers, admin.getUsersString());
            Assert.assertEquals(gruntUsers, grunt.getUsersString());
            Assert.assertEquals(carlGroups, carl.getGroupsString());
            Assert.assertEquals(jacoGroups, jaco.getGroupsString());
        }

        /**
         * Check the model is consistent now and in the next tx.
         */
        void checkNowAndNextTx(PersistenceManager pm,
                String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
            pm.currentTransaction().begin();
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
        }
    }

    /**
     * Test many-to-many implemented with an ArrayList and a HashSet (on the
     * inverse side).
     */
    public void testManyToManyVector() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();

        ManyToManyVectorModel m = new ManyToManyVectorModel();

        System.out.println("\n*** admin.add(carl) before makePersistent");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        pm.makePersistent(m.admin); // must complete relationship
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.remove(carl)");
        pm.currentTransaction().begin();
        m.admin.remove(m.carl);
        m.checkNowAndNextTx(pm, "", "", "", "");

        System.out.println("\n*** admin.add(carl)");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.add(jaco) grunt.add(jaco)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.add(m.jaco);
        m.grunt.add(m.jaco);
        m.checkNowAndNextTx(pm, "carl jaco", "jaco", "admin", "admin grunt");

        System.out.println("\n*** admin.remove(carl) grunt.add(carl)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.remove(m.carl);
        m.grunt.add(m.carl);
        m.checkNowAndNextTx(pm, "jaco", "jaco carl", "grunt", "admin grunt");

        System.out.println("\n*** admin.getUsers().clear()");
        pm.currentTransaction().begin();
        m.admin.getUsers().clear();
        m.checkNowAndNextTx(pm, "", "jaco carl", "grunt", "grunt");

        System.out.println("\n*** grunt.getUsers().retainAll(jaco)");
        pm.currentTransaction().begin();
        ArrayList a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().retainAll(a);
        m.checkNowAndNextTx(pm, "", "jaco", "", "grunt");

        System.out.println("\n*** grunt.getUsers().removeAll(jaco)");
        pm.currentTransaction().begin();
        m.grunt.add(m.carl);
        a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().removeAll(a);
        m.checkNowAndNextTx(pm, "", "carl", "grunt", "");

        System.out.println("\n*** jaco.getGroups().addAll(admin,grunt)");
        pm.currentTransaction().begin();
        a = new ArrayList();
        a.add(m.admin);
        a.add(m.grunt);
        m.jaco.getGroups().addAll(a);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt");

        System.out.println("\n*** grunt.remove(carl) grunt.add(0, carl)");
        pm.currentTransaction().begin();
        m.grunt.remove(m.carl);
        m.grunt.getUsers().add(0, m.carl);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt");

        System.out.println("\n*** admin.getUsers().set(0, carl)");
        pm.currentTransaction().begin();
        m.admin.getUsers().set(0, m.carl);
        m.checkNowAndNextTx(pm, "carl", "carl jaco", "admin grunt", "grunt");

        System.out.println("\n*** deletePersistent(grunt)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.grunt);
        Assert.assertEquals("admin", m.carl.getGroupsString());
        Assert.assertEquals("", m.jaco.getGroupsString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("admin", m.carl.getGroupsString());
        Assert.assertEquals("", m.jaco.getGroupsString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(carl)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.carl);
        Assert.assertEquals("", m.admin.getUsersString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("", m.admin.getUsersString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(admin,jaco)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.admin);
        pm.deletePersistent(m.jaco);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Holds instances used to test many-to-many with Vector and HashSet
     * and includes methods to check the state of the model.
     */
    private static class ManyToManyLinkedListModel {

        final GroupLL admin = new GroupLL("admin");
        final GroupLL grunt = new GroupLL("grunt");
        final UserLL carl = new UserLL("carl");
        final UserLL jaco = new UserLL("jaco");

        /**
         * Check the model is consistent now.
         */
        void check(String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            Assert.assertEquals(adminUsers, admin.getUsersString());
            Assert.assertEquals(gruntUsers, grunt.getUsersString());
            Assert.assertEquals(carlGroups, carl.getGroupsString());
            Assert.assertEquals(jacoGroups, jaco.getGroupsString());
        }

        /**
         * Check the model is consistent now and in the next tx.
         */
        void checkNowAndNextTx(PersistenceManager pm,
                String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
            pm.currentTransaction().begin();
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
        }
    }

    /**
     * Test many-to-many implemented with an ArrayList and a HashSet (on the
     * inverse side).
     */
    public void testManyToManyLinkedList() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;
		
		PersistenceManager pm = pmf().getPersistenceManager();

        ManyToManyLinkedListModel m = new ManyToManyLinkedListModel();

        System.out.println("\n*** admin.add(carl) before makePersistent");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        pm.makePersistent(m.admin); // must complete relationship
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.remove(carl)");
        pm.currentTransaction().begin();
        m.admin.remove(m.carl);
        m.checkNowAndNextTx(pm, "", "", "", "");

        System.out.println("\n*** admin.add(carl)");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.add(jaco) grunt.add(jaco)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.add(m.jaco);
        m.grunt.add(m.jaco);
        m.checkNowAndNextTx(pm, "carl jaco", "jaco", "admin", "admin grunt");

        System.out.println("\n*** admin.remove(carl) grunt.add(carl)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.remove(m.carl);
        m.grunt.add(m.carl);
        m.checkNowAndNextTx(pm, "jaco", "jaco carl", "grunt", "admin grunt");

        System.out.println("\n*** admin.getUsers().clear()");
        pm.currentTransaction().begin();
        m.admin.getUsers().clear();
        m.checkNowAndNextTx(pm, "", "jaco carl", "grunt", "grunt");

        System.out.println("\n*** grunt.getUsers().retainAll(jaco)");
        pm.currentTransaction().begin();
        ArrayList a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().retainAll(a);
        m.checkNowAndNextTx(pm, "", "jaco", "", "grunt");

        System.out.println("\n*** grunt.getUsers().removeAll(jaco)");
        pm.currentTransaction().begin();
        m.grunt.add(m.carl);
        a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().removeAll(a);
        m.checkNowAndNextTx(pm, "", "carl", "grunt", "");

        System.out.println("\n*** jaco.getGroups().addAll(admin,grunt)");
        pm.currentTransaction().begin();
        a = new ArrayList();
        a.add(m.admin);
        a.add(m.grunt);
        m.jaco.getGroups().addAll(a);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt");

        System.out.println("\n*** grunt.remove(carl) grunt.add(0, carl)");
        pm.currentTransaction().begin();
        m.grunt.remove(m.carl);
        m.grunt.getUsers().add(0, m.carl);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt");

        System.out.println("\n*** admin.getUsers().set(0, carl)");
        pm.currentTransaction().begin();
        m.admin.getUsers().set(0, m.carl);
        m.checkNowAndNextTx(pm, "carl", "carl jaco", "admin grunt", "grunt");

        System.out.println("\n*** deletePersistent(grunt)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.grunt);
        Assert.assertEquals("admin", m.carl.getGroupsString());
        Assert.assertEquals("", m.jaco.getGroupsString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("admin", m.carl.getGroupsString());
        Assert.assertEquals("", m.jaco.getGroupsString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(carl)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.carl);
        Assert.assertEquals("", m.admin.getUsersString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("", m.admin.getUsersString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(admin,jaco)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.admin);
        pm.deletePersistent(m.jaco);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Holds instances used to test many-to-many with List
     * and includes methods to check the state of the model.
     */
    private static class ManyToManyListWithInheritanceModel {

        final GroupLI admin = new GroupLI("admin");
        final GroupLI grunt = new GroupLI("grunt");
        final UserLI carl = new UserLI("carl");
        final UserLI jaco = new UserLI("jaco");

        /**
         * Check the model is consistent now.
         */
        void check(String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            Assert.assertEquals(adminUsers, admin.getUsersString());
            Assert.assertEquals(gruntUsers, grunt.getUsersString());
            Assert.assertEquals(carlGroups, carl.getGroupsString());
            Assert.assertEquals(jacoGroups, jaco.getGroupsString());
        }

        /**
         * Check the model is consistent now and in the next tx.
         */
        void checkNowAndNextTx(PersistenceManager pm,
                String adminUsers, String gruntUsers,
                String carlGroups, String jacoGroups) {
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
            pm.currentTransaction().begin();
            check(adminUsers, gruntUsers, carlGroups, jacoGroups);
            pm.currentTransaction().commit();
        }
    }

    /**
     * Test many-to-many implemented with Lists on subclasses.
     */
    public void testManyToManyListWithInheritance() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();

        ManyToManyListWithInheritanceModel m = new ManyToManyListWithInheritanceModel();

        System.out.println("\n*** admin.add(carl) before makePersistent");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        pm.makePersistent(m.admin); // must complete relationship
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.remove(carl)");
        pm.currentTransaction().begin();
        m.admin.remove(m.carl);
        m.checkNowAndNextTx(pm, "", "", "", "");

        System.out.println("\n*** admin.add(carl)");
        pm.currentTransaction().begin();
        m.admin.add(m.carl);
        m.checkNowAndNextTx(pm, "carl", "", "admin", "");

        System.out.println("\n*** admin.add(jaco) grunt.add(jaco)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.add(m.jaco);
        m.grunt.add(m.jaco);
        m.checkNowAndNextTx(pm, "carl jaco", "jaco", "admin", "admin grunt");

        System.out.println("\n*** admin.remove(carl) grunt.add(carl)");
        pm.currentTransaction().begin();
        pm.makePersistent(m.grunt);
        m.admin.remove(m.carl);
        m.grunt.add(m.carl);
        m.checkNowAndNextTx(pm, "jaco", "jaco carl", "grunt", "admin grunt");

        System.out.println("\n*** admin.getUsers().clear()");
        pm.currentTransaction().begin();
        m.admin.getUsers().clear();
        m.checkNowAndNextTx(pm, "", "jaco carl", "grunt", "grunt");

        System.out.println("\n*** grunt.getUsers().retainAll(jaco)");
        pm.currentTransaction().begin();
        ArrayList a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().retainAll(a);
        m.checkNowAndNextTx(pm, "", "jaco", "", "grunt");

        System.out.println("\n*** grunt.getUsers().removeAll(jaco)");
        pm.currentTransaction().begin();
        m.grunt.add(m.carl);
        a = new ArrayList();
        a.add(m.jaco);
        m.grunt.getUsers().removeAll(a);
        m.checkNowAndNextTx(pm, "", "carl", "grunt", "");

        System.out.println("\n*** jaco.getGroups().addAll(admin,grunt)");
        pm.currentTransaction().begin();
        a = new ArrayList();
        a.add(m.admin);
        a.add(m.grunt);
        m.jaco.getGroups().addAll(a);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt");

        System.out.println("\n*** grunt.remove(carl) grunt.add(0, carl)");
        pm.currentTransaction().begin();
        m.grunt.remove(m.carl);
        m.check("jaco", "jaco", "", "admin grunt");
        m.grunt.getUsers().add(0, m.carl);
        m.checkNowAndNextTx(pm, "jaco", "carl jaco", "grunt", "admin grunt");

        System.out.println("\n*** admin.getUsers().set(0, carl)");
        pm.currentTransaction().begin();
        m.admin.getUsers().set(0, m.carl);
        m.checkNowAndNextTx(pm, "carl", "carl jaco", "admin grunt", "grunt");

        System.out.println("\n*** deletePersistent(grunt)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.grunt);
        Assert.assertEquals("admin", m.carl.getGroupsString());
        Assert.assertEquals("", m.jaco.getGroupsString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("admin", m.carl.getGroupsString());
        Assert.assertEquals("", m.jaco.getGroupsString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(carl)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.carl);
        Assert.assertEquals("", m.admin.getUsersString());
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("", m.admin.getUsersString());
        pm.currentTransaction().commit();

        System.out.println("\n*** deletePersistent(admin,jaco)");
        pm.currentTransaction().begin();
        pm.deletePersistent(m.admin);
        pm.deletePersistent(m.jaco);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test master detail.
     *
     * @throws Exception
     */
    public void testMasterDetailAL() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Master master = new Master();
        System.out.println("########################### master = " + master);
        pm.makePersistent(master);
        Detail detail = new Detail();
        master.add(detail);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "Only one detail was added to the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 1);
        master.clearDetails();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        master = new Master();
        pm.makePersistent(master);
        detail = new Detail();
        master.add(detail);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("Only one detail was added to the master",
                master.getDetailSize() == 1);
        master.clearDetails();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        master = new Master();
        pm.makePersistent(master);
        detail = new Detail();
        master.add(detail);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("Only one detail was added to the master",
                master.getDetailSize() == 1);
        master.clearDetails();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test master detail.
     *
     * @throws Exception
     */
    public void testMasterDetailAllAL() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Master master = new Master();

        Detail detail1 = new Detail(1);
        Detail detail2 = new Detail(2);
        Detail detail3 = new Detail(3);
        Detail detail4 = new Detail(4);
        Detail detail5 = new Detail(5);

        ArrayList details = new ArrayList();
        details.add(detail1);
        details.add(detail2);
        details.add(detail3);
        details.add(detail4);
        details.add(detail5);

        ArrayList list = master.getDetails();
//        master.addAllDetails(details);
        list.addAll(details);

        pm.makePersistent(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("5  details was added to the master.", 5,
                master.getDetailSize());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        details = new ArrayList();
        details.add(detail1);
        details.add(detail2);
        details.add(detail3);

        master.retainAllDetails(details);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Assert.assertTrue(
                "3  details was retained to the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 3);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Master master2 = new Master();
        pm.makePersistent(master2);

        countExecQueryEvents();
        detail2.setMaster(master2);
        Assert.assertEquals(2, countExecQueryEvents());

        Assert.assertTrue(
                "1 detail's master was changed now master has only 2: size = " + master.getDetailSize(),
                master.getDetailSize() == 2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Assert.assertTrue(
                "master2 now has 1 detail: size = " + master2.getDetailSize(),
                master2.getDetailSize() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master.removeAllDetails(details);
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master.getDetails().addAll(0, details);
        System.out.println("\n*** master = " + master);
        Detail det = (Detail)master.getDetails().get(0);
        System.out.println("*** master.details[0].master = " + det.getMaster());
        System.out.println();
        pm.currentTransaction().commit();

        addSomeDetails();

        pm.close();
    }

    private void addSomeDetails() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Master master = new Master();

        Detail detail1 = new Detail(1);
        Detail detail2 = new Detail(2);
        Detail detail3 = new Detail(3);
        Detail detail4 = new Detail(4);
        Detail detail5 = new Detail(666);

        ArrayList details = new ArrayList();
        details.add(detail1);
        details.add(detail2);
        details.add(detail3);
        details.add(detail4);
        details.add(detail5);

        ArrayList list = master.getDetails();
        list.addAll(0, details);

        pm.makePersistent(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Object o = pm.getObjectId(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Master dbMaster = (Master)pm.getObjectById(o, true);
        Assert.assertTrue(
                "There must be 5 Details the master :size = " + dbMaster.getDetailSize(),
                dbMaster.getDetailSize() == 5);
        pm.currentTransaction().commit();


//		Master m = new Master();
//
//
//		pm.currentTransaction().begin();
//
//		m.getDetails().add(detail5);
//
//        pm.currentTransaction().commit();
//
//		pm.currentTransaction().begin();
//		Object id = pm.getObjectId(m);
//		Master mm = (Master)pm.getObjectById(id,true);
//        Assert.assertTrue("There must be 1 Details the master :size = "+mm.getDetailSize(), mm.getDetailSize() == 5);
//		pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * This tests a master detail type relation ship that is not declared in meta-data.
     *
     * @throws Exception
     */
    public void testMasterDetailManagedAL() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin(); // test if the master is not managed
        Master master = new Master();
        Detail detail = new Detail(100);
        pm.makePersistent(detail);
        detail.setMaster(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master.getDetails().size() == 1);
        pm.currentTransaction().commit();

        Master master1 = new Master();  // The master and the detail is not managed
        Detail detail1 = new Detail(888);
        detail1.setMaster(master1);
        master1.getDetails().add(detail1);

        Master master2 = new Master();  // The master and the detail is not managed
        Detail detail2 = new Detail(999);
        master2.getDetails().add(detail2);

        pm.currentTransaction().begin(); // Now we make the datail managed
        pm.makePersistent(detail1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin(); // Now we make the master managed
        pm.makePersistent(master2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master1.getDetails().size() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master2.getDetails().size() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master1.getDetails().add(detail2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master1.getDetails().size() == 2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "the detail that was contained was moved to another list",
                master2.getDetails().size() == 0);
        pm.currentTransaction().commit();

        System.out.println(
                "####################################################################");
        pm.currentTransaction().begin();
        pm.deletePersistent(detail2);
        pm.currentTransaction().commit();
        System.out.println(
                "####################################################################");
        pm.currentTransaction().begin();
        Assert.assertTrue(
                "1 detail was deleted now list has 1 : size = " + master1.getDetails().size(),
                master1.getDetails().size() == 1);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        pm.deletePersistent(master1);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testMasterDetailLL() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MasterLinkedList master = new MasterLinkedList();
        System.out.println("########################### master = " + master);
        pm.makePersistent(master);
        DetailLinkedList detail = new DetailLinkedList();
        master.getDetails().add(detail);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "Only one detail was added to the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 1);
        master.getDetails().clear();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        master = new MasterLinkedList();
        pm.makePersistent(master);
        detail = new DetailLinkedList();
        master.getDetails().add(detail);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("Only one detail was added to the master",
                master.getDetailSize() == 1);
        master.getDetails().clear();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        master = new MasterLinkedList();
        pm.makePersistent(master);
        detail = new DetailLinkedList();
        master.getDetails().add(detail);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("Only one detail was added to the master",
                master.getDetailSize() == 1);
        master.getDetails().clear();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test master detail.
     *
     * @throws Exception
     */
    public void testMasterDetailAllLL() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MasterLinkedList master = new MasterLinkedList();

        DetailLinkedList detail1 = new DetailLinkedList(1);
        DetailLinkedList detail2 = new DetailLinkedList(2);
        DetailLinkedList detail3 = new DetailLinkedList(3);
        DetailLinkedList detail4 = new DetailLinkedList(4);
        DetailLinkedList detail5 = new DetailLinkedList(5);

        ArrayList details = new ArrayList();
        details.add(detail1);
        details.add(detail2);
        details.add(detail3);
        details.add(detail4);
        details.add(detail5);

        LinkedList list = master.getDetails();
//        master.addAllDetails(details);
        list.addAll(details);

        pm.makePersistent(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "5  details was added to the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 5);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        details = new ArrayList();
        details.add(detail1);
        details.add(detail2);
        details.add(detail3);

        master.getDetails().retainAll(details);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Assert.assertTrue(
                "3  details was retained to the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 3);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        MasterLinkedList master2 = new MasterLinkedList();
        pm.makePersistent(master2);

        detail2.setMaster(master2);

        Assert.assertTrue(
                "1 detail's master was changed now master has only 2: size = " + master.getDetailSize(),
                master.getDetailSize() == 2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Assert.assertTrue(
                "master2 now has 1 detail: size = " + master2.getDetailSize(),
                master2.getDetailSize() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master.getDetails().removeAll(details);
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * This tests a master detail type relation ship that is not declared in meta-data.
     *
     * @throws Exception
     */
    public void testMasterDetailManagedLL() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin(); // test if the master is not managed
        MasterLinkedList master = new MasterLinkedList();
        DetailLinkedList detail = new DetailLinkedList(100);
        pm.makePersistent(detail);
        detail.setMaster(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master.getDetails().size() == 1);
        pm.currentTransaction().commit();

        MasterLinkedList master1 = new MasterLinkedList();  // The master and the detail is not managed
        DetailLinkedList detail1 = new DetailLinkedList(101);
        detail1.setMaster(master1);
        master1.getDetails().add(detail1);
        master1.getDetails().add(detail1);

        MasterLinkedList master2 = new MasterLinkedList();  // The master and the detail is not managed
        DetailLinkedList detail2 = new DetailLinkedList(102);
        master2.getDetails().add(detail2);

        pm.currentTransaction().begin(); // Now we make the datail managed
        pm.makePersistent(detail1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin(); // Now we make the master managed
        pm.makePersistent(master2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master1.getDetails().size() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master2.getDetails().size() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master1.getDetails().add(detail2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master1.getDetails().size() == 2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master2.getDetails().size() == 0);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testMasterDetailVector() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MasterVector master = new MasterVector();
        System.out.println("########################### master = " + master);
        pm.makePersistent(master);
        DetailVector detail = new DetailVector();
        master.getDetails().add(detail);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "Only one detail was added to the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 1);
        master.getDetails().clear();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        master = new MasterVector();
        pm.makePersistent(master);
        detail = new DetailVector();
        master.getDetails().add(detail);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("Only one detail was added to the master",
                master.getDetailSize() == 1);
        master.getDetails().clear();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        master = new MasterVector();
        pm.makePersistent(master);
        detail = new DetailVector();
        master.getDetails().add(detail);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("Only one detail was added to the master",
                master.getDetailSize() == 1);
        master.getDetails().clear();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test master detail.
     *
     * @throws Exception
     */
    public void testMasterDetailAllVector() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MasterVector master = new MasterVector();

        DetailVector detail1 = new DetailVector(1);
        DetailVector detail2 = new DetailVector(2);
        DetailVector detail3 = new DetailVector(3);
        DetailVector detail4 = new DetailVector(4);
        DetailVector detail5 = new DetailVector(5);

        ArrayList details = new ArrayList();
        details.add(detail1);
        details.add(detail2);
        details.add(detail3);
        details.add(detail4);
        details.add(detail5);

        Vector list = master.getDetails();
//        master.addAllDetails(details);
        list.addAll(details);

        pm.makePersistent(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "5  details was added to the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 5);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        details = new ArrayList();
        details.add(detail1);
        details.add(detail2);
        details.add(detail3);

        master.getDetails().retainAll(details);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Assert.assertTrue(
                "3  details was retained to the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 3);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        MasterVector master2 = new MasterVector();
        pm.makePersistent(master2);

        detail2.setMaster(master2);

        Assert.assertTrue(
                "1 detail's master was changed now master has only 2: size = " + master.getDetailSize(),
                master.getDetailSize() == 2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Assert.assertTrue(
                "master2 now has 1 detail: size = " + master2.getDetailSize(),
                master2.getDetailSize() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master.getDetails().removeAll(details);
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * This tests a master detail type relation ship that is not declared in meta-data.
     *
     * @throws Exception
     */
    public void testMasterDetailManagedVector() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin(); // test if the master is not managed
        MasterVector master = new MasterVector();
        DetailVector detail = new DetailVector(100);
        pm.makePersistent(detail);
        detail.setMaster(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master.getDetails().size() == 1);
        pm.currentTransaction().commit();

        MasterVector master1 = new MasterVector();  // The master and the detail is not managed
        DetailVector detail1 = new DetailVector(101);
        detail1.setMaster(master1);
        master1.getDetails().add(detail1);
        master1.getDetails().add(detail1);

        MasterVector master2 = new MasterVector();  // The master and the detail is not managed
        DetailVector detail2 = new DetailVector(102);
        master2.getDetails().add(detail2);

        pm.currentTransaction().begin(); // Now we make the datail managed
        pm.makePersistent(detail1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin(); // Now we make the master managed
        pm.makePersistent(master2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master1.getDetails().size() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master2.getDetails().size() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master1.getDetails().add(detail2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master1.getDetails().size() == 2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master2.getDetails().size() == 0);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testMasterDetailVectorSpes() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MasterVector master = new MasterVector();
        pm.makePersistent(master);

        DetailVector detail1 = new DetailVector(10);
        DetailVector detail2 = new DetailVector(20);
        DetailVector detail3 = new DetailVector(30);
        DetailVector detail4 = new DetailVector(40);
        DetailVector detail5 = new DetailVector(50);

        ArrayList details = new ArrayList();
        details.add(detail1);
        details.add(detail2);
        details.add(detail3);
        details.add(detail4);
        details.add(detail5);

        Vector list = master.getDetails();
        list.addAll(details);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "5  details was added to the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 5);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("Is all the elements contained in our collection = ",
                list.containsAll(details));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        ArrayList removeList = new ArrayList();
        removeList.add(detail1);
        removeList.add(detail2);

        list.removeAll(removeList);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "2  details was removed from the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 3);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();

        Assert.assertNull("The master mast be null on the detail1",
                detail1.getMaster());

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        ArrayList retainList = new ArrayList();
        retainList.add(detail4);
        retainList.add(detail5);

        list.retainAll(retainList);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "2  details was retained on the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 2);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        boolean expHappend = false;
        try {
            master.getDetails().add(null);
        } catch (JDOUserException e) {
            expHappend = true;
        } finally {
            if (!expHappend) {
                throw new Exception("a null could be added to the collection");
            }
        }

        pm.currentTransaction().commit();
        master.getDetails().clear();
        pm.currentTransaction().begin();

        pm.currentTransaction().commit();
        Assert.assertTrue(
                "details has been cleared: size = " + master.getDetailSize(),
                master.getDetailSize() == 0);
        pm.currentTransaction().begin();

        pm.currentTransaction().commit();

        DetailVector detail6 = new DetailVector(60);
        DetailVector detail7 = new DetailVector(70);
        DetailVector detail8 = new DetailVector(80);
        DetailVector detail9 = new DetailVector(90);
        DetailVector detail10 = new DetailVector(100);

        ArrayList details1 = new ArrayList();
        details.add(detail6);
        details.add(detail7);
        details.add(detail8);
        details.add(detail9);
        details.add(detail10);
        pm.close();
    }

    /**
     * Test master detail.
     *
     * @throws Exception
     */
    public void testMasterDetailSet() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MasterSet master = new MasterSet();
        System.out.println("########################### master = " + master);
        pm.makePersistent(master);
        DetailSet detail = new DetailSet();
        master.getDetails().add(detail);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "Only one detail was added to the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 1);
        master.getDetails().clear();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        master = new MasterSet();
        pm.makePersistent(master);
        detail = new DetailSet();
        master.getDetails().add(detail);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("Only one detail was added to the master",
                master.getDetailSize() == 1);
        master.getDetails().clear();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        master = new MasterSet();
        pm.makePersistent(master);
        detail = new DetailSet();
        master.getDetails().add(detail);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("Only one detail was added to the master",
                master.getDetailSize() == 1);
        master.getDetails().clear();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test master detail.
     *
     * @throws Exception
     */
    public void testMasterDetailAllSet() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MasterSet master = new MasterSet();

        DetailSet detail1 = new DetailSet(1);
        DetailSet detail2 = new DetailSet(2);
        DetailSet detail3 = new DetailSet(3);
        DetailSet detail4 = new DetailSet(4);
        DetailSet detail5 = new DetailSet(5);

        ArrayList details = new ArrayList();
        details.add(detail1);
        details.add(detail2);
        details.add(detail3);
        details.add(detail4);
        details.add(detail5);

        Collection list = master.getDetails();
        list.addAll(details);

        pm.makePersistent(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "5  details was added to the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 5);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        details = new ArrayList();
        details.add(detail1);
        details.add(detail2);
        details.add(detail3);

        master.getDetails().retainAll(details);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Assert.assertTrue(
                "3  details was retained to the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 3);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        MasterSet master2 = new MasterSet();
        pm.makePersistent(master2);

        detail2.setMaster(master2);

        Assert.assertTrue(
                "1 detail's master was changed now master has only 2: size = " + master.getDetailSize(),
                master.getDetailSize() == 2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        Assert.assertTrue(
                "master2 now has 1 detail: size = " + master2.getDetailSize(),
                master2.getDetailSize() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master.getDetails().removeAll(details);
        Assert.assertTrue("All the details was removed from the master",
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * This tests a master detail type relation ship that is not declared in meta-data.
     *
     * @throws Exception
     */
    public void testMasterDetailManagedSet() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin(); // test if the master is not managed
        MasterSet master = new MasterSet();
        DetailSet detail = new DetailSet(100);
        pm.makePersistent(detail);
        detail.setMaster(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master.getDetails().size() == 1);
        pm.currentTransaction().commit();

        MasterSet master1 = new MasterSet();  // The master and the detail is not managed
        DetailSet detail1 = new DetailSet(101);
        detail1.setMaster(master1);
        master1.getDetails().add(detail1);
        master1.getDetails().add(detail1);

        MasterSet master2 = new MasterSet();  // The master and the detail is not managed
        DetailSet detail2 = new DetailSet(102);
        master2.getDetails().add(detail2);

        pm.currentTransaction().begin(); // Now we make the datail managed
        pm.makePersistent(detail1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin(); // Now we make the master managed
        pm.makePersistent(master2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master1.getDetails().size() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master2.getDetails().size() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master1.getDetails().add(detail2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master1.getDetails().size() == 2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("1 detail was added to the master, " +
                "while the master was not managed",
                master2.getDetails().size() == 0);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master.getDetails().clear();
        master1.getDetails().clear();
        master2.getDetails().clear();
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testMasterDetailHashSetSpes() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        MasterSet master = new MasterSet();
        pm.makePersistent(master);

        DetailSet detail1 = new DetailSet(10);
        DetailSet detail2 = new DetailSet(20);
        DetailSet detail3 = new DetailSet(30);
        DetailSet detail4 = new DetailSet(40);
        DetailSet detail5 = new DetailSet(50);

        ArrayList details = new ArrayList();
        details.add(detail1);
        details.add(detail2);
        details.add(detail3);
        details.add(detail4);
        details.add(detail5);

        HashSet list = master.getDetails();
        list.addAll(details);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "5  details was added to the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 5);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("Is all the elements contained in our collection = ",
                master.getDetails().containsAll(details));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        ArrayList removeList = new ArrayList();
        removeList.add(detail1);
        removeList.add(detail2);

        master.getDetails().removeAll(removeList);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "2  details was removed from the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 3);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();

        Assert.assertNull("The master mast be null on the detail1",
                detail1.getMaster());

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        ArrayList retainList = new ArrayList();
        retainList.add(detail4);
        retainList.add(detail5);

        master.getDetails().retainAll(retainList);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "2  details was retained on the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 2);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        boolean expHappend = false;
        try {
            master.getDetails().add(null);
        } catch (VersantNullElementException e) {
            expHappend = true;
            e.printStackTrace();
        } finally {
            if (!expHappend) {
                throw new Exception("a null could be added to the collection");
            }
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master.getDetails().clear();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        expHappend = false;
        ArrayList nullList = new ArrayList();
        DetailSet det55 = new DetailSet(55);
        nullList.add(det55);
        nullList.add(new DetailSet(56));
        nullList.add(null);
        nullList.add(null);

        try {
            master.getDetails().addAll(nullList);
        } catch (VersantNullElementException e) {
            expHappend = true;
            e.printStackTrace();
        } finally {
            if (!expHappend) {
                throw new Exception("a null could be added to the collection");
            }
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "2  detail was left in the master: size = " + master.getDetailSize(),
                master.getDetailSize() == 2);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master.getDetails().remove(det55);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "1 details is left: size = " + master.getDetailSize(),
                master.getDetailSize() == 1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        master.getDetails().removeAll(nullList);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(
                "details has been cleared: size = " + master.getDetailSize(),
                master.getDetailSize() == 0);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * This tests a master detail type relation ship that is not declared in meta-data.
     *
     * @throws Exception
     */
    public void testMasterDetail2() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Master2 master = new Master2();
        Detail2 detail = new Detail2();
        master.add(detail);
        pm.makePersistent(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("Only one detail was added to the master",
                master.getDetails().size() == 1);
        detail.remove();
        pm.deletePersistent(detail);
        pm.currentTransaction().commit();
        Debug.OUT.println(
                "\n\n\n\n\n######################## after commit ######################\n\n\n\n\n");

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetails().size() == 0);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        master = new Master2();
        detail = new Detail2();
        master.add(detail);
        pm.makePersistent(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("Only one detail was added to the master",
                master.getDetails().size() == 1);
        detail.remove();
        pm.deletePersistent(detail);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetails().size() == 0);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        master = new Master2();
        detail = new Detail2();
        master.add(detail);
        pm.makePersistent(master);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("Only one detail was added to the master",
                master.getDetails().size() == 1);
        detail.remove();
        pm.deletePersistent(detail);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("All the details was removed from the master",
                master.getDetails().size() == 0);
        pm.currentTransaction().commit();
        pm.close();
    }

}
