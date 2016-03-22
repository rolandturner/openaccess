
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

import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test0.model.AllInstanceLifecycleListener;
import com.versant.core.jdo.junit.test0.model.Person4;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;


public class TestLifecycleEvents extends VersantTestCase {
    public TestLifecycleEvents(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
                "testLifecycleListenerForSimpleObjects",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestLifecycleEvents(a[i]));
        }
        return s;
    }

    /**
     * Test of basic lifecycle listener behaviour, listeneing to the changes in the lifecycle
     * of a simple object (with no relationships). The object is persisted, then updated, then detached
     * then updated (whilst detached), then attached, and finally deleted. This exercises all listener
     * event types.
     */
    public void testLifecycleListenerForSimpleObjects() {
        if (true) {
            broken();
            return;
        }
        AllInstanceLifecycleListener listener = new AllInstanceLifecycleListener();
//        pmf().addInstanceLifecycleListener(listener, new Class[]{Person4.class});
        PersistenceManager pm = pmf().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        int i = 0;
        try {
            pm.addInstanceLifecycleListener(listener, new Class[]{Person4.class});
            Object person_id;

            tx.begin();

            // Persist an object and check the events
            Person4 person = new Person4(12345, "Robert", "Smith", "robert.green@versant.com");
            pm.makePersistent(person);
            AllInstanceLifecycleListener.EventTypeAndClass[] events = listener.getRegisteredEventsAsArray();
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_CREATE, events[i++].getEventType());

            // Commit the changes and check the events
            tx.commit();

            events = listener.getRegisteredEventsAsArray();
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_PRE_STORE, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_PRE_CLEAR, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_CLEAR, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_STORE, events[i++].getEventType());


            // Save the object id
            person_id = pm.getObjectId(person);

            // Clean the cache and retrieve the object from datastore
            tx.begin();
            pm.evictAll();

            // Update a field on the object and check the events
            person = (Person4) pm.getObjectById(person_id);
            person.setEmailAddress("robert.green@bea.com");
            events = listener.getRegisteredEventsAsArray();
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_LOAD, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_PRE_DIRTY, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_DIRTY, events[i++].getEventType());

            // Commit the changes and check the events
            tx.commit();
            events = listener.getRegisteredEventsAsArray();
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_PRE_STORE, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_PRE_CLEAR, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_CLEAR, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_STORE, events[i++].getEventType());


            assertTrue("Total number of lifecycle events received was incorrect : should have been " + i + " but was " + events.length,
                    events.length == i);

            tx.begin();
            pm.evictAll();

            // Retrieve the object and detach it
            person = (Person4) pm.getObjectById(person_id);
            Person4 detachedPerson = (Person4) pm.detachCopy(person);
            events = listener.getRegisteredEventsAsArray();
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_LOAD, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_PRE_DETACH, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_DETACH, events[i++].getEventType());

            // Commit the changes and check the events

            tx.commit();
            events = listener.getRegisteredEventsAsArray();
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_PRE_CLEAR, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_CLEAR, events[i++].getEventType());

            // Update the detached object
            detachedPerson.setLastName("Green");

            tx.begin();

            // Attach the detached object
            pm.attachCopy(detachedPerson, true);
            events = listener.getRegisteredEventsAsArray();
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_PRE_ATTACH, events[i++].getEventType());
//            assertEquals(listener, AllInstanceLifecycleListener.EVENT_PRE_DIRTY, events[i++].getEventType());
//            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_DIRTY, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_ATTACH, events[i++].getEventType());

            // Commit the changes and check the events
            tx.commit();
            events = listener.getRegisteredEventsAsArray();
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_PRE_STORE, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_PRE_CLEAR, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_CLEAR, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_STORE, events[i++].getEventType());

            assertTrue("Total number of lifecycle events received was incorrect : should have been " + i + " but was " + events.length,
                    events.length == i);

            tx.begin();

            // Delete the object and check the events
            person = (Person4) pm.getObjectById(person_id);
            pm.deletePersistent(person);
            events = listener.getRegisteredEventsAsArray();
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_LOAD, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_PRE_DELETE, events[i++].getEventType());
            assertEquals(listener, AllInstanceLifecycleListener.EVENT_POST_DELETE, events[i++].getEventType());

            // Commit the changes and check the events
            tx.commit();
            events = listener.getRegisteredEventsAsArray();

            assertTrue("Total number of lifecycle events received was incorrect : should have been " + i + " but was " + events.length,
                    events.length == i);
            // now we remove the listener, we must not get any events
            pm.removeInstanceLifecycleListener(listener);

            tx.begin();

            // Persist an object and check the events
            person = new Person4(54321, "Robert", "Smith", "robert.green@versant.com");
            pm.makePersistent(person);
            events = listener.getRegisteredEventsAsArray();
            // Commit the changes and check the events
            tx.commit();
            // we must not recieve any more events
            assertTrue("Total number of lifecycle events received was incorrect : should have been " + i + " but was " + events.length,
                    events.length == i);

        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown while running lifecycle listener simple object test : " + e.getMessage());
        }
        finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    public  void assertEquals(AllInstanceLifecycleListener l, int i, int i1) {
        try {
            Assert.assertEquals(i,i1);
        } catch (AssertionFailedError e) {
            throw new AssertionFailedError("expected: <"+ l.getEventName(i)+"> but was:<" + l.getEventName(i1) + ">");
        }
    }

//    /**
//     * Test of basic lifecycle listener behaviour, listeneing to the changes in the lifecycle
//     * of an object with a collection of other objects.
//     */
//    public void testLifecycleListenerForCollections() {
//        AllInstanceLifecycleListener listener = new AllInstanceLifecycleListener();
//        PersistenceManager pm = pmf().getPersistenceManager();
//        Transaction tx = pm.currentTransaction();
//        int i = 0;
//        try {
//            pm.addInstanceLifecycleListener(listener, new Class[]{Manager.class, Department.class});
//            Object managerId;
//            Object dept2Id;
//
//            tx.begin();
//
//            // Persist related objects and check the events
//            // Manager has a 1-N (FK) with Department
//            Manager manager = new Manager(12346, "George", "Bush", "george.bush@thewhitehouse.com", 2000000, "ABC-DEF");
//            Department dept1 = new Department("Invasions");
//            Department dept2 = new Department("Propaganda");
//            Department dept3 = new Department("Lies");
//            manager.addDepartment(dept1);
//            manager.addDepartment(dept2);
//            manager.addDepartment(dept3);
//            dept1.setManager(manager);
//            dept2.setManager(manager);
//            dept3.setManager(manager);
//
//            pm.makePersistent(manager);
//            AllInstanceLifecycleListener.EventTypeAndClass[] events = listener.getRegisteredEventsAsArray();
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_CREATE, events[i++].getEventType()); // Manager
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_STORE, events[i++].getEventType()); // Manager
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_CREATE, events[i++].getEventType()); // Department 1
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_STORE, events[i++].getEventType()); // Department 1
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_STORE, events[i++].getEventType()); // Department 1
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_CREATE, events[i++].getEventType()); // Department 2
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_STORE, events[i++].getEventType()); // Department 2
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_STORE, events[i++].getEventType()); // Department 2
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_CREATE, events[i++].getEventType()); // Department 3
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_STORE, events[i++].getEventType()); // Department 3
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_STORE, events[i++].getEventType()); // Department 3
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_STORE, events[i++].getEventType()); // Manager
//
//            // Commit the changes and check the events
//            tx.commit();
//            events = listener.getRegisteredEventsAsArray();
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_CLEAR, events[i++].getEventType()); // Manager
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_CLEAR, events[i++].getEventType()); // Manager
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_CLEAR, events[i++].getEventType()); // Department 1
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_CLEAR, events[i++].getEventType()); // Department 1
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_CLEAR, events[i++].getEventType()); // Department 2
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_CLEAR, events[i++].getEventType()); // Department 2
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_CLEAR, events[i++].getEventType()); // Department 3
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_CLEAR, events[i++].getEventType()); // Department 3
//
//            assertTrue("Total number of lifecycle events received was incorrect : should have been " + i + " but was " + events.length,
//                    events.length == i);
//
//            // Save the object ids
//            managerId = pm.getObjectId(manager);
//            dept2Id = pm.getObjectId(dept2);
//
//            tx.begin();
//
//            dept2 = (Department) pm.getObjectById(dept2Id);
//            manager = (Manager) pm.getObjectById(managerId);
//
//            // Remove manager of dept2 and check the events
//            dept2.setManager(null);
//            manager.removeDepartment(dept2);
//            events = listener.getRegisteredEventsAsArray();
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_LOAD, events[i++].getEventType()); // Department 2
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_LOAD, events[i++].getEventType()); // Manager
//
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_DIRTY, events[i++].getEventType()); // Department 2
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_DIRTY, events[i++].getEventType()); // Department 2
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_DIRTY, events[i++].getEventType()); // Manager
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_DIRTY, events[i++].getEventType()); // Manager
//
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_STORE, events[i++].getEventType()); // Department 2
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_STORE, events[i++].getEventType()); // Department 2
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_STORE, events[i++].getEventType()); // Manager
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_STORE, events[i++].getEventType()); // Manager
//
//            // Commit the changes and check the events
//            tx.commit();
//            events = listener.getRegisteredEventsAsArray();
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_CLEAR, events[i++].getEventType()); // Department 2
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_CLEAR, events[i++].getEventType()); // Department 2
//            assertEquals(AllInstanceLifecycleListener.EVENT_PRE_CLEAR, events[i++].getEventType()); // Manager
//            assertEquals(AllInstanceLifecycleListener.EVENT_POST_CLEAR, events[i++].getEventType()); // Manager
//
//            assertTrue("Total number of lifecycle events received was incorrect : should have been " + i + " but was " + events.length,
//                    events.length == i);
//
//            // TODO Add attach/detach of the Manager and its Departments.
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            fail("Exception thrown while running lifecycle listener collection test : " + e.getMessage());
//        }
//        finally {
//            if (tx.isActive()) {
//                tx.rollback();
//            }
//            pm.close();
//        }
//    }


}

