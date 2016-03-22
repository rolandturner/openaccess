
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

import com.versant.core.ejb.junit.ejbtest0.model.*;
import com.versant.core.ejb.junit.VersantEjbTestCase;

import javax.persistence.Query;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for primary key generation related stuff.
 */
public class TestKeyGen extends VersantEjbTestCase {
    
    public TestKeyGen(String name) {
        super(name);
    }

    /**
     * Test insert with no keygen.
     */
    public void testPersist() {
        EntityManager em = emf().getEntityManager();

        TableGen tg = new TableGen();
        tg.setName("tg1");
        em.getTransaction().begin();
        em.persist(tg);
        em.getTransaction().commit();
    }

}
