
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
package com.versant.core.ejb.junit.ejbtest0;

import com.versant.core.ejb.junit.VersantEjbTestCase;
import com.versant.core.ejb.junit.ejbtest0.model.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import java.util.List;
import java.util.Iterator;

public class TestCallbackAndListener extends VersantEjbTestCase {

    private static EventCollector eventCollector = Animal.EVENT_COLLECTOR;
    private static int i = 0;
    /**
     * Test insert with no keygen.
     */
    public void testEventLifecycle() {
        clearEvents();  // clear all previous events
        EntityManager em = emf().getEntityManager();
        Cat cat = new Cat();
        cat.setLivesLeft(9);
        cat.setDangerous(false);
        cat.setName("Gate");

        em.getTransaction().begin();
        em.persist(cat);
        // PRE_PERSIST is on EntityLifecycleListener
        assertEvent(EventCollector.PRE_PERSIST_EVENT, Cat.class, EntityLifecycleListener.class);
        em.getTransaction().commit();

        assertEvent(EventCollector.POST_PERSIST_EVENT, Cat.class, Cat.class);



        em.getTransaction().begin();
        Query q = em.createQuery("SELECT o FROM Cat o");
        List result = q.getResultList();
        Cat catFromDB = null;
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            catFromDB = (Cat) iterator.next(); // now the cat gets loaded
            catFromDB.setLivesLeft(8); // now we update the cat

        }
        assertEvent(EventCollector.POST_LOAD_EVENT, Cat.class, Cat.class);


        em.getTransaction().commit();
        // PRE_UPDATE and POST_UPDATE is on animal
        assertEvent(EventCollector.PRE_UPDATE_EVENT, Cat.class, Animal.class);
        assertEvent(EventCollector.POST_UPDATE_EVENT, Cat.class, Animal.class);

        em.getTransaction().begin();
        Cat mergeCat = em.merge(catFromDB);
        em.remove(mergeCat);
        assertEvent(EventCollector.POST_LOAD_EVENT, Cat.class, Cat.class);
        assertEvent(EventCollector.PRE_REMOVE_EVENT, Cat.class, Cat.class);
        em.getTransaction().commit();
        assertEvent(EventCollector.POST_REMOVE_EVENT, Cat.class, Cat.class);
    }


    /**
     * Test insert with no keygen.
     */
    public void testEventCollection() {
        clearEvents();  // clear all previous events
        EntityManager em = emf().getEntityManager();
        BlackSheep bSheep = new BlackSheep();
        bSheep.setDangerous(false);
        bSheep.setWoolType("Black");
        bSheep.addWoolbags(new WoolBag("Black"));
        bSheep.addWoolbags(new WoolBag("LightBlack"));
        bSheep.addWoolbags(new WoolBag("DarkBlack"));

        em.getTransaction().begin();
        em.persist(bSheep);
        // PRE_PERSIST and POST_PERSIST is on Sheep
        assertEvent(EventCollector.PRE_PERSIST_EVENT, BlackSheep.class, Sheep.class);
        assertEvent(EventCollector.PRE_PERSIST_EVENT, WoolBag.class, WoolBag.class);
        assertEvent(EventCollector.PRE_PERSIST_EVENT, WoolBag.class, WoolBag.class);
        assertEvent(EventCollector.PRE_PERSIST_EVENT, WoolBag.class, WoolBag.class);

        em.getTransaction().commit();

        assertEvent(EventCollector.POST_PERSIST_EVENT, BlackSheep.class, Sheep.class);

        assertEvent(EventCollector.POST_PERSIST_EVENT, WoolBag.class, WoolBag.class);
        assertEvent(EventCollector.POST_PERSIST_EVENT, WoolBag.class, WoolBag.class);
        assertEvent(EventCollector.POST_PERSIST_EVENT, WoolBag.class, WoolBag.class);


        em.getTransaction().begin();
        Query q = em.createQuery("SELECT o FROM BlackSheep o");
        List result = q.getResultList();
        BlackSheep bSheepFromDB = null;
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            bSheepFromDB = (BlackSheep) iterator.next(); // now the cat gets BlackSheep
            bSheepFromDB.setWoolType("Brown"); // now we update the BlackSheep
            for (Iterator iter = bSheepFromDB.getWoolbags().iterator(); iter.hasNext();) {
                WoolBag woolBag = (WoolBag) iter.next();
                woolBag.setType("NEW_Black");
            }

        }
        assertEvent(EventCollector.POST_LOAD_EVENT, BlackSheep.class, BlackSheep.class);
        assertEvent(EventCollector.POST_LOAD_EVENT, WoolBag.class, WoolBag.class);
        assertEvent(EventCollector.POST_LOAD_EVENT, WoolBag.class, WoolBag.class);
        assertEvent(EventCollector.POST_LOAD_EVENT, WoolBag.class, WoolBag.class);


        em.getTransaction().commit();
        // PRE_UPDATE and POST_UPDATE is on animal
        assertEvent(EventCollector.PRE_UPDATE_EVENT, BlackSheep.class, Animal.class);
        assertEvent(EventCollector.PRE_UPDATE_EVENT, WoolBag.class, WoolBag.class);
        assertEvent(EventCollector.PRE_UPDATE_EVENT, WoolBag.class, WoolBag.class);
        assertEvent(EventCollector.PRE_UPDATE_EVENT, WoolBag.class, WoolBag.class);
        assertEvent(EventCollector.POST_UPDATE_EVENT, BlackSheep.class, Animal.class);
        assertEvent(EventCollector.POST_UPDATE_EVENT, WoolBag.class, WoolBag.class);
        assertEvent(EventCollector.POST_UPDATE_EVENT, WoolBag.class, WoolBag.class);
        assertEvent(EventCollector.POST_UPDATE_EVENT, WoolBag.class, WoolBag.class);

        em.getTransaction().begin();
        BlackSheep mergebSheep = em.merge(bSheepFromDB);
        assertEvent(EventCollector.POST_LOAD_EVENT, BlackSheep.class, BlackSheep.class);
        em.remove(mergebSheep);
        // PRE_REMOVE and POST_REMOVE is on EntityLifecycleListener
        assertEvent(EventCollector.PRE_REMOVE_EVENT, BlackSheep.class, EntityLifecycleListener.class);
        em.getTransaction().commit();
        assertEvent(EventCollector.POST_REMOVE_EVENT, BlackSheep.class, EntityLifecycleListener.class);
    }

    private void clearEvents() {
        eventCollector.getRegisteredEvents().clear();
        i = 0;
    }

    public void assertEvent(int expectedEventType,
                            Class instanceClass,
                            Class listernerClass) {
        EventCollector.EventTypeAndClass[] events =
                eventCollector.getRegisteredEventsAsArray();
        EventCollector.EventTypeAndClass event = events[i++];
        try {
            Assert.assertEquals(expectedEventType, event.getEventType());
        } catch (AssertionFailedError e) {
            throw new AssertionFailedError("expected: <" +
                    eventCollector.getEventName(expectedEventType) + "> but was:<" +
                    eventCollector.getEventName(event.getEventType()) + ">");
        }

        try {
            Assert.assertEquals(instanceClass, event.getSrcClass());
        } catch (AssertionFailedError e) {
            throw new AssertionFailedError("expected: instance class <" + instanceClass +
                    "> but got:<" + event.getSrcClass() + ">");
        }

        try {
            Assert.assertEquals(listernerClass, event.getFromClass());
        } catch (AssertionFailedError e) {
            throw new AssertionFailedError("expected: listerner class <" + listernerClass +
                    "> but got:<" + event.getFromClass() + ">");
        }
    }

}

