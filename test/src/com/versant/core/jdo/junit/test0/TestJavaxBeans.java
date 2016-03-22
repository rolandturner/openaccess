
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

import com.versant.core.jdo.junit.test0.model.Person;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.VersantPersistenceManagerFactory;

import javax.jdo.PersistenceManager;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.beans.XMLDecoder;
import java.beans.ExceptionListener;
import java.beans.XMLEncoder;

import junit.framework.Assert;

/**
 * JDK 1.4 specific javax.beans tests.
 */
public class TestJavaxBeans extends VersantTestCase {

    public void testBeansPers1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        p.getPersonsSet().add(new Person("ps1"));
        p.getOrderedRefList().add(new Person("or1"));

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(bout);
        ((VersantPersistenceManagerFactory)pmf()).registerSCOPersistenceDelegates(
                encoder);
        encoder.setExceptionListener(new ExceptionListener() {
            public void exceptionThrown(Exception exception) {
                exception.printStackTrace();
            }
        });
        encoder.writeObject(p);
        encoder.close();

        XMLDecoder decoder = new XMLDecoder(
                new ByteArrayInputStream(bout.toByteArray()));
        Person p2 = (Person)decoder.readObject();
        Assert.assertEquals("name", p2.getName());
        Assert.assertEquals(1, p2.getOrderedRefList().size());
        Assert.assertEquals("or1",
                ((Person)p2.getOrderedRefList().get(0)).getName());

        Assert.assertEquals(1, p2.getPersonsSet().size());
        Assert.assertEquals("ps1",
                ((Person)p2.getPersonsSet().iterator().next()).getName());

        pm.makePersistent(p);

        bout = new ByteArrayOutputStream();
        encoder = new XMLEncoder(bout);
        ((VersantPersistenceManagerFactory)pmf()).registerSCOPersistenceDelegates(
                encoder);
        encoder.setExceptionListener(new ExceptionListener() {
            public void exceptionThrown(Exception exception) {
                exception.printStackTrace();
            }
        });
        encoder.writeObject(p);
        encoder.close();

        decoder = new XMLDecoder(new ByteArrayInputStream(bout.toByteArray()));
        p2 = (Person)decoder.readObject();
        Assert.assertEquals("name", p2.getName());
        Assert.assertEquals(1, p2.getOrderedRefList().size());
        Assert.assertEquals("or1",
                ((Person)p2.getOrderedRefList().get(0)).getName());

        Assert.assertEquals(1, p2.getPersonsSet().size());
        Assert.assertEquals("ps1",
                ((Person)p2.getPersonsSet().iterator().next()).getName());

        pm.close();
    }

}

