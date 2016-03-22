
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

import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test3.model.leaf.*;
import com.versant.core.jdbc.metadata.JdbcClass;

import javax.jdo.PersistenceManager;
import javax.jdo.JDOUserException;
import javax.jdo.Query;
import java.util.List;

/**
 * Tests for flat inheritance with no descriminator column. Instances of the
 * most derived class are returned for this case.
 */
public class TestFlatNoDescriminator extends VersantTestCase {

    /**
     * Test a simple hierarchy of 2 classes mapped flat with no descriminator.
     */
    public void testHierachy2Deep() {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        assertTrue(getCmd(LeafBase.class).instancesNotAllowed);
        if (!isRemote()) {
            assertTrue(((JdbcClass)getCmd(LeafBase.class).storeClass).readAsClass
                    == getCmd(LeafSub.class));
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        // make sure that persisting an instance of the base class triggers
        // an exception
        pm.currentTransaction().begin();
        try {
            pm.makePersistent(new LeafBase("base"));
            fail("Expected JDOUserException");
        } catch (JDOUserException e) {
            System.out.println("Good: " + e);
        }
        pm.currentTransaction().commit();

        // create a leaf instance and holder instance
        pm.currentTransaction().begin();
        LeafSub leaf = new LeafSub("a", "b");
        LeafBaseHolder holder = new LeafBaseHolder(leaf);
        holder.getList().add(leaf);
        pm.makePersistent(holder);
        pm.currentTransaction().commit();
        Object oid = pm.getObjectId(leaf);

        pm.close();
        pm = pmf().getPersistenceManager();

        // check getObjectById
        pm.currentTransaction().begin();
        Object o = pm.getObjectById(oid, false);
        assertTrue(o.getClass() == LeafSub.class);
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check query against base class
        pm.currentTransaction().begin();
        Query q = pm.newQuery(LeafBase.class);
        List ans = (List)q.execute();
        assertEquals(1, ans.size());
        assertTrue(ans.get(0).getClass() == LeafSub.class);
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check query against sub class
        pm.currentTransaction().begin();
        q = pm.newQuery(LeafSub.class);
        ans = (List)q.execute();
        assertEquals(1, ans.size());
        assertTrue(ans.get(0).getClass() == LeafSub.class);
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check query including a downcast through a reference - this also
        // checks that the ref and list fields on holder work
        pm.currentTransaction().begin();
        q = pm.newQuery(LeafBaseHolder.class, "((LeafSub)ref).subField == 'b'");
        ans = (List)q.execute();
        assertEquals(1, ans.size());
        holder = (LeafBaseHolder)ans.get(0);
        assertTrue(holder.getRef().getClass() == LeafSub.class);
        assertTrue(holder.getList().get(0).getClass() == LeafSub.class);
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test a simple hierarchy of 3 classes mapped flat with no descriminator.
     */
    public void testHierachy3Deep() {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        assertTrue(getCmd(LeafBase2.class).instancesNotAllowed);
        assertTrue(getCmd(LeafMid.class).instancesNotAllowed);
        if (!isRemote()) {
            assertTrue(((JdbcClass)getCmd(LeafBase2.class).storeClass).readAsClass == getCmd(LeafSub2.class));
            assertTrue(((JdbcClass)getCmd(LeafMid.class).storeClass).readAsClass == getCmd(LeafSub2.class));
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        // make sure that persisting an instance of the base class triggers
        // an exception
        pm.currentTransaction().begin();
        try {
            pm.makePersistent(new LeafBase2("base"));
            fail("Expected JDOUserException");
        } catch (JDOUserException e) {
            System.out.println("Good: " + e);
        }
        pm.currentTransaction().commit();

        // create a leaf instance and holder instance
        pm.currentTransaction().begin();
        LeafSub2 leaf = new LeafSub2("a", "b", "c");
        LeafBase2Holder holder = new LeafBase2Holder(leaf);
        holder.getList().add(leaf);
        pm.makePersistent(holder);
        pm.currentTransaction().commit();
        Object oid = pm.getObjectId(leaf);

        pm.close();
        pm = pmf().getPersistenceManager();

        // check getObjectById
        pm.currentTransaction().begin();
        Object o = pm.getObjectById(oid, false);
        assertTrue(o.getClass() == LeafSub2.class);
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check query against base class
        pm.currentTransaction().begin();
        Query q = pm.newQuery(LeafBase2.class);
        List ans = (List)q.execute();
        assertEquals(1, ans.size());
        assertTrue(ans.get(0).getClass() == LeafSub2.class);
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check query against middle class
        pm.currentTransaction().begin();
        q = pm.newQuery(LeafMid.class);
        ans = (List)q.execute();
        assertEquals(1, ans.size());
        assertTrue(ans.get(0).getClass() == LeafSub2.class);
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check query against sub class
        pm.currentTransaction().begin();
        q = pm.newQuery(LeafSub2.class);
        ans = (List)q.execute();
        assertEquals(1, ans.size());
        assertTrue(ans.get(0).getClass() == LeafSub2.class);
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check query including a downcast through a reference - this also
        // checks that the ref and list fields on holder work
        pm.currentTransaction().begin();
        q = pm.newQuery(LeafBase2Holder.class, "((LeafSub2)ref).subField == 'c'");
        ans = (List)q.execute();
        assertEquals(1, ans.size());
        holder = (LeafBase2Holder)ans.get(0);
        assertTrue(holder.getRef().getClass() == LeafSub2.class);
        assertTrue(holder.getList().get(0).getClass() == LeafSub2.class);
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
    }
}

