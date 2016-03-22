
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

import com.versant.core.jdo.junit.test2.model.unmanaged.*;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.VersantPersistenceManager;

import javax.jdo.*;
import javax.jdo.spi.PersistenceCapable;

import junit.framework.Assert;

import java.util.*;

/**
 * Tests for one-to-many and many-to-many unmanaged relationships.
 */
public class TestUnmanagedRelationships2 extends VersantTestCase {

    /**
     * Test that the level 2 cache is correctly evicted when only the
     * back reference is set on a one-to-many.
     */
    public void testOneToManyL2Evict() throws Exception {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.setCheckModelConsistencyOnCommit(false);

        pm.currentTransaction().begin();
        OrderAL o1 = new OrderAL("o1");
        OrderAL o2 = new OrderAL("o2");
        pm.makePersistent(o1);
        pm.makePersistent(o2);
        pm.currentTransaction().commit();
        Object oid1 = pm.getObjectId(o1);
        Object oid2 = pm.getObjectId(o2);

        // create a new line, set the order after makePersistent and make sure
        // order is evicted
        pm.currentTransaction().begin();
        o1.getName();
        o2.getName();
        assertTrue(pmf().isInCache(oid1));
        assertTrue(pmf().isInCache(oid2));
        OrderLineAL line = new OrderLineAL(10);
        pm.makePersistent(line);
        line.setOrder(o1);
        pm.currentTransaction().commit();
        assertTrue(!pmf().isInCache(oid1));
        assertTrue(pmf().isInCache(oid2));

        // create a new line, set the order before makePersistent and make sure
        // order is evicted
        pm.currentTransaction().begin();
        o1.getName();
        o2.getName();
        assertTrue(pmf().isInCache(oid1));
        assertTrue(pmf().isInCache(oid2));
        OrderLineAL line2 = new OrderLineAL(20);
        line2.setOrder(o2);
        pm.makePersistent(line2);
        pm.currentTransaction().commit();
        assertTrue(pmf().isInCache(oid1));
        assertTrue(!pmf().isInCache(oid2));

        // move line from o1 to o2 and make sure both are evicted
        pm.currentTransaction().begin();
        o1.getName();
        o2.getName();
        assertTrue(pmf().isInCache(oid1));
        assertTrue(pmf().isInCache(oid2));
        line.setOrder(o2);
        pm.currentTransaction().commit();
        assertTrue(!pmf().isInCache(oid1));
        assertTrue(!pmf().isInCache(oid2));

        // Make sure a manually flushed change evicts classes and not just
        // oids. This verifies that the memory hungry OID data structures
        // have been converted into cheap class structures.
        pm.currentTransaction().begin();
        o1.getName();
        o2.getName();
        assertTrue(pmf().isInCache(oid1));
        assertTrue(pmf().isInCache(oid2));
        OrderLineAL line3 = new OrderLineAL(30);
        line3.setOrder(o1);
        pm.makePersistent(line3);
        pm.flush();
        assertTrue(!pmf().isInCache(oid1));
        assertTrue(!pmf().isInCache(oid2));
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test fetching of a tree with an unmanaged one-to-many for children.
     */
    public void testTreeOneToManyFetch() throws Exception {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        // create a graph
        int fanout = 10;
        int depth = 3;
        pm.currentTransaction().begin();
        HashSet all = new HashSet();
        TreeNode root = createTreeNode(1, fanout, depth, all);
        root.setName("root");
        pm.makePersistent(root);
        pm.currentTransaction().commit();
        Object rootOID = pm.getObjectId(root);
        pm.close();
        int numNodes = all.size();

        Query q;
        Collection col;
        HashSet found;
        countExecQueryEvents();

        int minSelects = depth + 1;

        System.out.println(
            "Walk the whole tree starting from the root node found with\n"+
            "a JDOQL query. This will run (number of nodes) + 1 SELECTs.");
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true); // TODO datastore???
        pm.setCheckModelConsistencyOnCommit(false);
        pm.currentTransaction().begin();
        q = pm.newQuery(TreeNode.class, "name == 'root'");
        col = (Collection)q.execute();
        root = (TreeNode)col.iterator().next();
        q.closeAll();
        walkTree(root, found = new HashSet());
        pm.currentTransaction().commit();
        Assert.assertEquals(numNodes, found.size());
        Assert.assertEquals(numNodes + 1, countExecQueryEvents());
        pm.close();

        pmf().evictAll();

        System.out.println(
            "Walk the whole tree starting from the root node found with\n" +
            "a JDOQL query using bounded=true and fetchGroup=all that\n" +
            "recursively includes the name and outEdges of every node.\n" +
            "This will run (depth of tree) + 1 SELECTs.");
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true); // TODO datastore???
        pm.setCheckModelConsistencyOnCommit(false);
        pm.currentTransaction().begin();
        q = pm.newQuery(TreeNode.class, "name == 'root'");
        q.declareParameters("String jdoGenieOptions");
        col = (Collection)q.execute("fetchGroup=all;bounded=true");
        root = (TreeNode)col.iterator().next();
        q.closeAll();
        walkTree(root, found = new HashSet());
        pm.currentTransaction().commit();
        Assert.assertEquals(numNodes, found.size());
        if (!isWeakRefs(pm)) Assert.assertTrue(countExecQueryEvents() <= minSelects);
        pm.close();

        pmf().evictAll();

        System.out.println(
            "Walk the whole tree starting from the root node found with\n" +
            "getObjectById. This will run (number of nodes) + 1 SELECTs.");
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true); // TODO datastore???
        pm.setCheckModelConsistencyOnCommit(false);
        pm.currentTransaction().begin();
        root = (TreeNode)pm.getObjectById(rootOID, true);
        walkTree(root, found = new HashSet());
        pm.currentTransaction().commit();
        Assert.assertEquals(numNodes, found.size());
        if (!isWeakRefs(pm)) Assert.assertEquals(numNodes + 1, countExecQueryEvents());
        pm.close();

        pmf().evictAll();

        System.out.println(
            "Walk the whole tree starting from the root node found with\n" +
            "getObjectById and fetchGroup=all. This will run\n" +
            "(depth of tree) + 1 SELECTs.");
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true); // TODO datastore???
        pm.setCheckModelConsistencyOnCommit(false);
        pm.currentTransaction().begin();
        root = (TreeNode)pm.getObjectById(rootOID, false);
        ((VersantPersistenceManager)pm).loadFetchGroup((PersistenceCapable)root, "all");
        walkTree(root, found = new HashSet());
        pm.currentTransaction().commit();
        Assert.assertEquals(numNodes, found.size());
        if (!isWeakRefs(pm)) Assert.assertTrue(countExecQueryEvents() <= minSelects);

        // delete the tree
        pm.currentTransaction().begin();
        pm.deletePersistentAll(found);
        pm.currentTransaction().commit();
        pm.close();
    }

    private TreeNode createTreeNode(int depth, int fanout,
            int maxDepth, Set all) {
        TreeNode ans = new TreeNode();
        all.add(ans);
        if (depth < maxDepth) {
            for (int i = 0; i < fanout; i++) {
                TreeNode child = createTreeNode(depth + 1, fanout,
                        maxDepth, all);
                child.setName("N" + all.size());
                ans.addChild(child);
            }
        }
        return ans;
    }

    private void walkTree(TreeNode node, Set found) {
        if (found.contains(node)) return;
        found.add(node);
        for (Iterator i = node.getChildren().iterator(); i.hasNext(); ) {
            walkTree((TreeNode)i.next(), found);
        }
    }

    /**
     * Test fetching of a graph with an unmanaged many-to-many for edges.
     * Serveral
     */
    public void testGraphManyToManyFetch() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

        assertFalse(getFieldMetaData(GraphNode.class, "inEdges").managed);
        assertFalse(getFieldMetaData(GraphNode.class, "outEdges").managed);

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        // create a graph
        int fanout = 3;
        int depth = 3;
        pm.currentTransaction().begin();
        HashSet all = new HashSet();
        GraphNode root = createGraphNode(1, fanout, depth, all);
        root.setName("root");
        pm.makePersistent(root);
        pm.currentTransaction().commit();
        pm.close();
        int numNodes = all.size();

        Query q;
        Collection col;
        HashSet found;
        countExecQueryEvents();

        int minSelects = depth + 2;

        System.out.println(
            "Walk the whole graph starting from the root node found with\n"+
            "a JDOQL query. This will run (number of nodes) + 1 SELECTs.");
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true); // TODO datastore???
        pm.setCheckModelConsistencyOnCommit(false);
        pm.currentTransaction().begin();
        q = pm.newQuery(GraphNode.class, "name == 'root'");
        col = (Collection)q.execute();
        root = (GraphNode)col.iterator().next();
        q.closeAll();
        walkGraph(root, found = new HashSet());
        pm.currentTransaction().commit();
        Assert.assertEquals(numNodes, found.size());
        Assert.assertEquals(numNodes + 1, countExecQueryEvents());
        pm.close();

        pmf().evictAll();

        System.out.println(
            "Walk the whole graph starting from the root node found with\n" +
            "a JDOQL query using bounded=true and fetchGroup=all that\n" +
            "recursively includes the name and outEdges of every node.\n" +
            "This will run (depth of graph) + 2 SELECTs.");
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true); // TODO datastore???
        pm.setCheckModelConsistencyOnCommit(false);
        pm.currentTransaction().begin();
        q = pm.newQuery(GraphNode.class, "name == 'root'");
        q.declareParameters("String jdoGenieOptions");
        col = (Collection)q.execute("fetchGroup=all;bounded=true");
        root = (GraphNode)col.iterator().next();
        q.closeAll();
        walkGraph(root, found = new HashSet());
        pm.currentTransaction().commit();
        Assert.assertEquals(numNodes, found.size());
        int execEvents = countExecQueryEvents();
        Assert.assertTrue("found " + execEvents + ": expected <= " + minSelects, execEvents <= minSelects);
        Object rootOID = pm.getObjectId(root);
        pm.close();

        pmf().evictAll();

        System.out.println(
            "Walk the whole graph starting from the root node found with\n" +
            "getObjectById. This will run (number of nodes) + 1 SELECTs.");
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true); // TODO datastore???
        pm.setCheckModelConsistencyOnCommit(false);
        pm.currentTransaction().begin();
        root = (GraphNode)pm.getObjectById(rootOID, true);
        walkGraph(root, found = new HashSet());
        pm.currentTransaction().commit();
        Assert.assertEquals(numNodes, found.size());
        Assert.assertEquals(numNodes + 1, countExecQueryEvents());
        pm.close();

        pmf().evictAll();

        System.out.println(
            "Walk the whole graph starting from the root node found with\n" +
            "getObjectById and fetchGroup=all. This will run\n" +
            "(depth of graph) + 2 SELECTs.");
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true); // TODO datastore???
        pm.setCheckModelConsistencyOnCommit(false);
        pm.currentTransaction().begin();
        root = (GraphNode)pm.getObjectById(rootOID, false);
        ((VersantPersistenceManager)pm).loadFetchGroup((PersistenceCapable)root, "all");
        walkGraph(root, found = new HashSet());
        pm.currentTransaction().commit();
        Assert.assertEquals(numNodes, found.size());
        execEvents = countExecQueryEvents();
        Assert.assertTrue("found " + execEvents + ": expected <= " + minSelects, execEvents <= minSelects);
//        Assert.assertTrue(countExecQueryEvents() <= minSelects);

        // delete the graph
        pm.currentTransaction().begin();
        pm.deletePersistentAll(found);
        pm.currentTransaction().commit();
        pm.close();
    }

    private GraphNode createGraphNode(int depth, int fanout,
            int maxDepth, Set all) {
        GraphNode ans = new GraphNode();
        ans.setName("depth" + depth);
        all.add(ans);
        if (depth < maxDepth) {
            for (int i = 0; i < fanout; i++) {
                GraphNode child = createGraphNode(depth + 1, fanout,
                        maxDepth, all);
                child.setName("N" + all.size()+ child.getName());
                ans.addOutEdge(child);
                // only every second child has an edge back to its parent
                if (depth + 1 < maxDepth) if ((i % 2) == 0) child.addOutEdge(ans);
            }
        }
        return ans;
    }

    private void walkGraph(GraphNode node, Set found) {
        if (found.contains(node)) return;
        found.add(node);
        for (Iterator i = node.getOutEdges().iterator(); i.hasNext(); ) {
            walkGraph((GraphNode)i.next(), found);
        }
    }

    /**
     * Make sure the consistency checking for many-to-many relationships
     * works properly.
     */
    public void testManyToManyConsistencyCheck() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        UserAL u1 = new UserAL("u1");
        GroupAL g1 = new GroupAL("g1");
        u1.getGroups().add(g1);
        pm.makePersistent(u1);
        pm.makePersistent(g1);
        try {
            pm.currentTransaction().commit();
        } catch (JDOFatalUserException e) {
            // good
            System.out.println("Got: " + e);
        }

        pm.close();
    }

    /**
     * Make sure that consistency checking for one-to-many relationships
     * works properly.
     */
    public void testOneToManyConsistencyCheck() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        // check: detail (many) in collection on master (one) but inverse
        // reference field is not set
        pm.currentTransaction().begin();
        Order2 o1 = new Order2("o1");
        OrderLine2 line1 = new OrderLine2(10);
        o1.getLines().add(line1);
        pm.makePersistent(o1);
        pm.makePersistent(line1);
        try {
            // line1.order == null so this should fail
            pm.currentTransaction().commit();
            throw new RuntimeException("expected JDOFatalUserException");
        } catch (JDOFatalUserException e) {
            // good
            System.out.println("Got: " + e);
        }

        // check: detail (many) in collection on master but has been deleted
        pm.currentTransaction().begin();
        o1 = new Order2("o5");
        line1 = new OrderLine2(50);
        o1.getLines().add(line1);
        pm.makePersistent(o1);
        pm.makePersistent(line1);
        pm.deletePersistent(line1);
        try {
            // line1 has been deleted so this should fail
            pm.currentTransaction().commit();
            throw new RuntimeException("expected JDOFatalUserException");
        } catch (JDOFatalUserException e) {
            // good
            System.out.println("Got: " + e);
        }

        // check: inverse reference field set but detail (many) not in
        // collection on master (one)
        pm.currentTransaction().begin();
        o1 = new Order2("o2");
        line1 = new OrderLine2(20);
        line1.setOrder(o1);
        pm.makePersistent(o1);
        pm.makePersistent(line1);
        try {
            // line1 is not in o1.lines so this should fail
            pm.currentTransaction().commit();
            throw new RuntimeException("expected JDOFatalUserException");
        } catch (JDOFatalUserException e) {
            // good
            System.out.println("Got: " + e);
        }

        // check: detail (many) in collection on master (one) but inverse
        // reference field is set to incorrect master
        pm.currentTransaction().begin();
        o1 = new Order2("o1");
        Order2 o2 = new Order2("o2");
        line1 = new OrderLine2(30);
        o1.getLines().add(line1);
        line1.setOrder(o2);
        pm.makePersistent(o1);
        pm.makePersistent(o2);
        pm.makePersistent(line1);
        try {
            // line1.order == null so this should fail
            pm.currentTransaction().commit();
            throw new RuntimeException("expected JDOFatalUserException");
        } catch (JDOFatalUserException e) {
            // good
            System.out.println("Got: " + e);
        }

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
        PersistenceManager pm = pmf().getPersistenceManager();
        ((VersantPersistenceManager)pm).setCheckModelConsistencyOnCommit(false);

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
        m.admin.clearUsers();
        m.checkNowAndNextTx(pm, "", "jaco carl", "grunt", "grunt");

        System.out.println("\n*** deletePersistent(admin,jaco)");
        pm.currentTransaction().begin();
        m.grunt.clearUsers();
        pm.deletePersistent(m.admin);
        pm.deletePersistent(m.jaco);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Holds instances used to test many-to-many with ArrayList
     * and includes methods to check the state of the model.
     */
    private static class ManyToManyArrayListModelManaged {

        final GroupALManaged admin = new GroupALManaged("admin");
        final GroupALManaged grunt = new GroupALManaged("grunt");
        final UserALManaged carl = new UserALManaged("carl");
        final UserALManaged jaco = new UserALManaged("jaco");

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
     * Test many-to-many implemented with ArrayList. The classes involved
     * have the managed=true explicitly set in the meta data.
     */
	public void testManyToManyArrayListManaged() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;
		
        PersistenceManager pm = pmf().getPersistenceManager();

        ManyToManyArrayListModelManaged m = new ManyToManyArrayListModelManaged();

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
     * Make sure that an unmanaged one-to-many works properly.
     */
    public void testOneToManyArrayList() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();
        ((VersantPersistenceManager)pm).setCheckModelConsistencyOnCommit(false);

        // make sure relationship is not completed either way when instances
        // are made persistent
        pm.currentTransaction().begin();

        OrderAL o1 = new OrderAL("o1");
        OrderLineAL line1 = new OrderLineAL(10);
        o1.add(line1);
        pm.makePersistent(o1);

        // make sure relationship not completed
        Assert.assertTrue(null == line1.getOrder());

        OrderAL o2 = new OrderAL("o2");
        pm.makePersistent(o2);
        OrderLineAL line2 = new OrderLineAL(20);
        pm.makePersistent(line2);
        line2.setOrder(o2);

        // make sure relationship not completed
        Assert.assertTrue(!o2.getLines().contains(line2));

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();

        // Make sure line1 is not linked to o1 after commit as the back
        // reference was not set. Make sure that line2 is linked to o2
        // as its back reference was set
        Assert.assertTrue(null == line1.getOrder());
        Assert.assertTrue(!o1.getLines().contains(line1));
        Assert.assertTrue(o2 == line2.getOrder());
        Assert.assertTrue(o2.getLines().contains(line2));

        // now complete the relationship between line1 and o1 properly
        o1.add(line1);
        line1.setOrder(o1);

        pm.currentTransaction().commit();

        // check line1 belongs to o1
        pm.currentTransaction().begin();
        Assert.assertTrue(o1 == line1.getOrder());
        Assert.assertTrue(o1.getLines().contains(line1));
        pm.currentTransaction().commit();

        // remove line2 from o2 without completing the relationship and make
        // it does not work
        pm.currentTransaction().begin();
        o2.remove(line2);
        Assert.assertTrue(o2 == line2.getOrder());
        pm.currentTransaction().commit();

        // make sure o2 has reverted in next tx
        pm.currentTransaction().begin();
        Assert.assertTrue(o2.getLines().contains(line2));
        Assert.assertTrue(o2 == line2.getOrder());
        pm.currentTransaction().commit();

        // remove line2 from o2 properly (i.e. clear back reference)
        pm.currentTransaction().begin();
        o2.remove(line2);
        line2.setOrder(null);
        pm.currentTransaction().commit();

        // make sure change was done properly
        pm.currentTransaction().begin();
        Assert.assertTrue(null == line2.getOrder());
        Assert.assertTrue(!o2.getLines().contains(line2));
        pm.currentTransaction().commit();

        // now add it back by just setting the back reference - this will
        // work after commit as the database update is done properly
        pm.currentTransaction().begin();
        line2.setOrder(o2);
        Assert.assertTrue(!o2.getLines().contains(line2));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(o2 == line2.getOrder());
        Assert.assertTrue(o2.getLines().contains(line2));
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistentAll(new Object[]{o1, o2, line1, line2});
        pm.currentTransaction().commit();

        pm.close();
    }

}
