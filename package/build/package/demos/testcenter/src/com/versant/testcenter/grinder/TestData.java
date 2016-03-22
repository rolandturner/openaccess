
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
package com.versant.testcenter.grinder;

import com.versant.testcenter.service.Context;
import com.versant.testcenter.model.Exam;
import com.versant.testcenter.model.Student;

import javax.jdo.PersistenceManager;

/**
 * Insert test data for grinder testing of the application.
 *
 */
public class TestData {

    public static void main(String[] args) throws Exception {
        Context ctx = Context.getContext();
        Context.initialize(Context.loadJDOProperties());
        doExams(ctx);
        doStudents(ctx);
        ctx.shutdown();
        System.exit(0);
    }

    private static void doStudents(Context ctx) {
        PersistenceManager pm = ctx.getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 20; i++) {
            Student s = new Student();
            s.setFirstName("user" + i);
            s.setLogin("user" + i);
            s.setPassword("user" + i);
            s.setSurname("user" + i);
            pm.makePersistent(s);
        }
        pm.currentTransaction().commit();
    }

    private static void doExams(Context ctx) {
        PersistenceManager pm = ctx.getPersistenceManager();
        for (char j = 'a'; j < ('z' + 1); j++) {
            pm.currentTransaction().begin();
            for (int i = 0; i < 20; i++) {
                Exam e = new Exam();
                Character c = new Character(j);
                String s = c.toString() + i;
                e.setName(s);
                pm.makePersistent(e);
            }
            pm.currentTransaction().commit();
        }
    }
}


