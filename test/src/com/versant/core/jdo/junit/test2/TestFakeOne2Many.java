
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

import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test2.model.fake.*;
import com.versant.core.jdo.junit.test2.model.drake2k.Request;
import com.versant.core.jdo.junit.test2.model.drake2k.Note;
import com.versant.core.jdo.VersantPersistenceManager;

import javax.jdo.*;
import javax.jdo.spi.PersistenceCapable;

import junit.framework.Assert;

import java.util.*;

/**
 * Tests for one-to-many and many-to-many unmanaged relationships.
 */
public class TestFakeOne2Many extends VersantTestCase {

    /**
     * Test fetching of a tree with an unmanaged one-to-many for children.
     */
    public void testTreeOneToManyFetch() throws Exception {
    	if (!isSQLSupported()) // SQL
    		return;

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
        pm.close();
        int numNodes = all.size();

        Query q;
        Collection col;
        HashSet found;
        countExecQueryEvents();

        int minSelects = depth + 1;

        System.out.println("Walk the whole tree starting from the root node found with\n" +
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

        System.out.println("Walk the whole tree starting from the root node found with\n" +
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
        if (!isWeakRefs(pm)) {
            Assert.assertTrue(countExecQueryEvents() <= minSelects);
        }
        Object rootOID = pm.getObjectId(root);
        pm.close();

        pmf().evictAll();

        System.out.println("Walk the whole tree starting from the root node found with\n" +
                "getObjectById. This will run (number of nodes) + 1 SELECTs.");
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true); // TODO datastore???
        pm.setCheckModelConsistencyOnCommit(false);
        pm.currentTransaction().begin();
        root = (TreeNode)pm.getObjectById(rootOID, true);
        walkTree(root, found = new HashSet());
        pm.currentTransaction().commit();
        Assert.assertEquals(numNodes, found.size());
        if (!isWeakRefs(pm)) {
            Assert.assertEquals(numNodes + 1,
                    countExecQueryEvents());
        }
        pm.close();

        pmf().evictAll();

        System.out.println("Walk the whole tree starting from the root node found with\n" +
                "getObjectById and fetchGroup=all. This will run\n" +
                "(depth of tree) + 1 SELECTs.");
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true); // TODO datastore???
        pm.setCheckModelConsistencyOnCommit(false);
        pm.currentTransaction().begin();
        root = (TreeNode)pm.getObjectById(rootOID, false);
        ((VersantPersistenceManager)pm).loadFetchGroup(
                (PersistenceCapable)root, "all");
        walkTree(root, found = new HashSet());
        pm.currentTransaction().commit();
        Assert.assertEquals(numNodes, found.size());
        if (!isWeakRefs(pm)) {
            Assert.assertTrue(countExecQueryEvents() <= minSelects);
        }

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
        for (Iterator i = node.getChildren().iterator(); i.hasNext();) {
            walkTree((TreeNode)i.next(), found);
        }
    }

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
        OrderFakeDId o1 = new OrderFakeDId("o5");
        OrderLineFakeDId line1 = new OrderLineFakeDId(50);
        o1.getLines().add(line1);
        pm.makePersistent(o1);
        pm.makePersistent(line1);
        pm.deletePersistent(line1);
        try {
            // line1 has been deleted so this should fail
            pm.currentTransaction().commit();
            fail("expected JDOFatalUserException");
        } catch (JDOFatalUserException e) {
            // good
            System.out.println("Got: " + e);
        }

        pm.close();
    }

    public void testNonTxWrite() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().begin();
        OrderFakeDId o1 = new OrderFakeDId("o1");
        OrderLineFakeDId line1 = new OrderLineFakeDId(10);
        o1.add(line1);
        pm.makePersistent(o1);
        pm.currentTransaction().commit();

        o1.setLines(new ArrayList());
        pm.refresh(line1);
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

        OrderFakeDId o1 = new OrderFakeDId("o1");
        OrderLineFakeDId line1 = new OrderLineFakeDId(10);
        o1.add(line1);
        pm.makePersistent(o1);

        OrderFakeDId o2 = new OrderFakeDId("o2");
        pm.makePersistent(o2);
        OrderLineFakeDId lineFakeDId = new OrderLineFakeDId(20);
        pm.makePersistent(lineFakeDId);
        o2.add(lineFakeDId);

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();

        // Make sure line1 is not linked to o1 after commit as the back
        // reference was not set. Make sure that lineFakeDId is linked to o2
        // as its back reference was set
        Assert.assertTrue(o1.getLines().contains(line1));
        Assert.assertTrue(o2.getLines().contains(lineFakeDId));

        pm.currentTransaction().commit();

        // remove lineFakeDId from o2
        pm.currentTransaction().begin();
        o2.remove(lineFakeDId);
        pm.currentTransaction().commit();

        // make sure change was done properly
        pm.currentTransaction().begin();
        Assert.assertFalse(o2.getLines().contains(lineFakeDId));
        pm.currentTransaction().commit();

        // now add it back
        pm.currentTransaction().begin();
        o2.add(lineFakeDId);
        Assert.assertTrue(o2.getLines().contains(lineFakeDId));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(o2.getLines().contains(lineFakeDId));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        o2.setLines(new ArrayList());
        Assert.assertFalse(o2.getLines().contains(lineFakeDId));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertFalse(o2.getLines().contains(lineFakeDId));
        pm.currentTransaction().commit();

        // now add it back
        pm.currentTransaction().begin();
        o2.add(lineFakeDId);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(o2.getLines().contains(lineFakeDId));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        o1.add(lineFakeDId);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertFalse(o2.getLines().contains(lineFakeDId));
        Assert.assertTrue(o1.getLines().contains(lineFakeDId));
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistentAll(new Object[]{o1, o2, line1, lineFakeDId});
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure that fake one-to-many works properly with App Id.
     */
    public void testOneToManyAppId() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();
        ((VersantPersistenceManager)pm).setCheckModelConsistencyOnCommit(false);

        // make sure relationship is not completed either way when instances
        // are made persistent
        pm.currentTransaction().begin();

        OrderFakeAId o1 = new OrderFakeAId("test1", 1, "o1");
        OrderLineFakeAId line1 = new OrderLineFakeAId(1, 10);
        o1.add(line1);
        pm.makePersistent(o1);

        OrderFakeAId o2 = new OrderFakeAId("test2", 2, "o2");
        pm.makePersistent(o2);
        OrderLineFakeAId lineFakeAId = new OrderLineFakeAId(2, 20);
        pm.makePersistent(lineFakeAId);
        o2.add(lineFakeAId);

        pm.currentTransaction().commit();
        pm.currentTransaction().begin();

        // Make sure line1 is not linked to o1 after commit as the back
        // reference was not set. Make sure that lineFakeAId is linked to o2
        // as its back reference was set
        Assert.assertTrue(o1.getLines().contains(line1));
        Assert.assertTrue(o2.getLines().contains(lineFakeAId));

        pm.currentTransaction().commit();

        // remove lineFakeAId from o2
        pm.currentTransaction().begin();
        o2.remove(lineFakeAId);
        pm.currentTransaction().commit();

        // make sure change was done properly
        pm.currentTransaction().begin();
        Assert.assertFalse(o2.getLines().contains(lineFakeAId));
        pm.currentTransaction().commit();

        // now add it back
        pm.currentTransaction().begin();
        o2.add(lineFakeAId);
        Assert.assertTrue(o2.getLines().contains(lineFakeAId));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(o2.getLines().contains(lineFakeAId));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        o2.setLines(new ArrayList());
        Assert.assertFalse(o2.getLines().contains(lineFakeAId));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertFalse(o2.getLines().contains(lineFakeAId));
        pm.currentTransaction().commit();

        // now add it back
        pm.currentTransaction().begin();
        o2.add(lineFakeAId);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(o2.getLines().contains(lineFakeAId));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        o1.add(lineFakeAId);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertFalse(o2.getLines().contains(lineFakeAId));
        Assert.assertTrue(o1.getLines().contains(lineFakeAId));
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistentAll(new Object[]{o1, o2, line1, lineFakeAId});
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testDelOrph() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;

    	DelOrphParent parent = new DelOrphParent();
        for (int x = 0; x < 10; x++) {
            DelOrphChild child = new DelOrphChild("child " + x);
            parent.addChildren(child);
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        pm.makePersistent(parent);
        pm.currentTransaction().commit();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query query = pm.newQuery(DelOrphParent.class);
        Collection parents = (Collection)query.execute();
        for (Iterator it = parents.iterator(); it.hasNext();) {
            Object o = it.next();
            DelOrphParent p = (DelOrphParent)o;
            p.getChildren().clear();
        }
        pm.currentTransaction().commit();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        query = pm.newQuery(DelOrphChild.class);
        Collection res = (Collection)query.execute();
        Assert.assertEquals("delete-orphans on DelOrphChild did not work!", 0,
                res.size());
        pm.currentTransaction().commit();
    }

    public void testDelOrph2() throws Exception {
    	if (!isManagedRelationshipSupported())
    		return;
    	
        Request parent = new Request();
        for (int x = 0; x < 10; x++) {
            Note child = new Note();
            parent.getNotes().add(child);
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        pm.makePersistent(parent);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query query = pm.newQuery(Request.class);
        Collection parents = (Collection)query.execute();
        for (Iterator it = parents.iterator(); it.hasNext();) {
            Object o = it.next();
            Request p = (Request)o;
            p.getNotes().clear();
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        query = pm.newQuery(Note.class);
        Collection res = (Collection)query.execute();
        Assert.assertEquals("delete-orphans on DelOrphChild did not work!", 0,
                res.size());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDelOrph3() throws Exception {

    	if (!isManagedRelationshipSupported())
    		return;

        Request parent = new Request();
        Note child = new Note();
        parent.getNotes().add(child);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        pm.makePersistent(parent);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        parent.removeNote(child);
        pm.currentTransaction().commit();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query query = pm.newQuery(Note.class);
        Collection res = (Collection)query.execute();
        Assert.assertEquals("delete-orphans on DelOrphChild did not work!", 0,
                res.size());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDependant() throws Exception {
        DelDepParent parent = new DelDepParent();
        for (int x = 0; x < 10; x++) {
            DelDepChild child = new DelDepChild("child " + x);
            parent.addChildren(child);
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        pm.makePersistent(parent);
        pm.currentTransaction().commit();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query query = pm.newQuery(DelDepParent.class);
        Collection parents = (Collection)query.execute();
        pm.deletePersistentAll(parents);
        pm.currentTransaction().commit();
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        query = pm.newQuery(DelDepChild.class);
        Collection res = (Collection)query.execute();
        Assert.assertEquals("delete-Depans on DelDepChild did not work!", 0,
                res.size());
        pm.currentTransaction().commit();
    }
}
