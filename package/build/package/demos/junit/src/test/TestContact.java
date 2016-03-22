
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
package test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestResult;

import javax.jdo.PersistenceManager;

import util.JDOSupport;
import model.Contact;

/**
 * Some simple tests for the Contact class.
 */
public class TestContact extends TestCase {

    /**
     * Create a TestSuite that starts up and shuts down the JDO Genie server.
     * This must set the contextClassLoader for the current thread to the
     * ClassLoader that loaded us or the JUnit GUI will not be able to
     * reload the classes.
     */
    public static Test suite() {
        return new TestSuite(TestContact.class) {
            public void run(TestResult result) {
                // this makes the reload option of the JUnit GUI work
                Thread.currentThread().setContextClassLoader(
                        getClass().getClassLoader());
                JDOSupport.init();
                super.run(result);
                JDOSupport.shutdown();
            }
        };
    }

    /**
     * Test the local PM cache i.e. there will only be once instance for
     * a given JDO identity in the PM at one time.
     */
    public void testKeepSameReference() {
        PersistenceManager pm = JDOSupport.getPM();

        pm.currentTransaction().begin();
        Contact c1 = new Contact("Joe Soap", "joe@soap.com");
        pm.makePersistent(c1);
        Object oid = pm.getObjectId(c1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Contact c2 = (Contact)pm.getObjectById(oid, true);
        assertTrue(c1 == c2);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure changes get rolled back.
     */
    public void testRollback() {
        PersistenceManager pm = JDOSupport.getPM();

        pm.currentTransaction().begin();
        Contact c = new Contact("Joe Soap", "joe@soap.com");
        pm.makePersistent(c);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        c.setEmail("joe@soap.org");
        pm.currentTransaction().rollback();

        // make sure change was rolled back
        pm.currentTransaction().begin();
        assertEquals(c.getEmail(), "joe@soap.com");
        pm.currentTransaction().commit();

        pm.close();
    }

}

 
