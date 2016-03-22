
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
package com.versant.testcenter.service;

import com.versant.testcenter.model.*;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Application's service facade. Uses {@link Context} to obtain
 * {@link PersistenceManager} for current thread.
 * <p/>
 * This code is not web-application specific, but can be used in
 * any environment.
 */
public final class TestCenterService {

    private TestCenterService() {
    }

    /**
     * Fetch {@link SystemUser} given object id. This will not
     * require database if object is already in level 2 cache.
     *
     * @param userOID object id
     * @return {@link SystemUser}
     */
    public static SystemUser findUserByOID(String userOID) {
        return (SystemUser)findObjectByOID(SystemUser.class, userOID);
    }

    /**
     * Fetch {@link SystemUser} given login name.
     *
     * @param login login name
     * @return {@link SystemUser}
     */
    public static SystemUser findUserByLogin(String login) {
        Context ctx = Context.getContext();
        PersistenceManager pm = ctx.getPersistenceManager();
        Query q = pm.newQuery(SystemUser.class);
        try {
            q.declareParameters("String l");
            q.setFilter("login == l");
            SystemUser usr = null;
            Iterator itr = ((Collection)q.execute(login.toLowerCase())).iterator();
            if (itr.hasNext()) usr = (SystemUser)itr.next();
            return usr;
        } finally {
            q.closeAll();
        }
    }

    /**
     * Validate the login and password and change the current user if ok.
     *
     * @return True if ok, false if the user does not exist or bad password
     */
    public static boolean login(String login, String password) {
        SystemUser u = findUserByLogin(login);
        if (u == null || !u.getPassword().equals(password)) return false;
        Context.getContext().setCurrentUser(u);
        return true;
    }

    /**
     * Creates new empty {@link Student}
     *
     * @return new {@link Student}
     */
    public static Student createStudent() {
        Context ctx = Context.getContext();
        PersistenceManager pm = ctx.getPersistenceManager();
        Student student = new Student();
        pm.makePersistent(student);
        return student;
    }

    /**
     * Creates default administrator with login name "admin"
     *
     * @return {@link Administrator}
     */
    public static Administrator createDefaultAdministrator() {
        String login = "admin";
        Administrator admin = null;
        if (findUserByLogin((login)) == null) {
            Context ctx = Context.getContext();
            PersistenceManager pm = ctx.getPersistenceManager();
            admin = new Administrator();
            admin.setSurname("Administrator");
            admin.setFirstName("System");
            admin.setLogin("admin");
            admin.setPassword("admin");
            pm.makePersistent(admin);
        }
        return admin;
    }

    /**
     * Fetch {@link Exam} given object id.
     *
     * @param examOID object id
     * @return {@link Exam}
     */
    public static Exam findExamByOID(String examOID) {
        return (Exam)findObjectByOID(Exam.class, examOID);
    }

    /**
     * Find all exams whose name starts with given name. This
     * search is case insensitive.
     *
     * @param name
     * @return {@link Collection} of {@link Exam} instances
     */
    public static Collection findExamsByName(String name) {
        Context ctx = Context.getContext();
        PersistenceManager pm = ctx.getPersistenceManager();
        Query q = pm.newQuery(Exam.class);
        try {
            q.declareParameters("String nm");
            q.setFilter("name.startsWith(nm)");
            q.setOrdering("name ascending");
            return new ArrayList((Collection)q.execute(name));
        } finally {
            q.closeAll();
        }
    }

    /**
     * Creates new empty {@link Exam}
     *
     * @return new {@link Exam}
     */
    public static Exam createExam() {
        Exam exam = new Exam();
        Context ctx = Context.getContext();
        PersistenceManager pm = ctx.getPersistenceManager();
        pm.makePersistent(exam);
        return exam;
    }

    /**
     * Fetch all available {@link ExamCategory}'s
     *
     * @return {@link Collection} of {@link ExamCategory}'s
     */
    public static Collection findAllExamCategories() {
        Context ctx = Context.getContext();
        PersistenceManager pm = ctx.getPersistenceManager();
        Query q = pm.newQuery(ExamCategory.class);
        try {
            return new ArrayList((Collection)q.execute());
        } finally {
            q.closeAll();
        }
    }

    /**
     * Fetch {@link ExamCategory} given object id.
     *
     * @param examCategoryOID object id
     * @return {@link Exam}
     */
    public static ExamCategory findExamCategoryByOID(String examCategoryOID) {
        return (ExamCategory)findObjectByOID(ExamCategory.class,
                examCategoryOID);
    }

    /**
     * Create default exam categories
     */
    public static void createExamCategories() {
        if (findAllExamCategories().size() == 0) {
            Context ctx = Context.getContext();
            PersistenceManager pm = ctx.getPersistenceManager();
            ExamCategory exCat = new ExamCategory();
            exCat.setName("Java");
            exCat.setDescription("Java developement");
            pm.makePersistent(exCat);
            exCat = new ExamCategory();
            exCat.setName("Unix");
            exCat.setDescription("Unix administration");
            pm.makePersistent(exCat);
        }
    }

    /**
     * Obtains user associated with current thread using {@link Context}
     *
     * @return currentl user, or null
     */
    public static SystemUser getCurrentUser() {
        return Context.getContext().getCurrentUser();
    }

    /**
     * Fetches object of given class by Object id
     *
     * @param cls of objects requred
     * @param oid object id
     * @return object for given oid
     */
    public static Object findObjectByOID(Class cls, String oid) {
        if (oid == null || oid.length() == 0) return null;
        Context ctx = Context.getContext();
        PersistenceManager pm = ctx.getPersistenceManager();
        Object oidObj = JDOUtil.parseOID(pm, cls, oid);
        return pm.getObjectById(oidObj, false);
    }

    /**
     * Use current {@link PersistenceManager} to start transaction
     */
    public static void beginTxn() {
        Context ctx = Context.getContext();
        PersistenceManager pm = ctx.getPersistenceManager();
        pm.currentTransaction().begin();
    }

    /**
     * Use current {@link PersistenceManager} to commit transaction
     */
    public static void commitTxn() {
        Context ctx = Context.getContext();
        PersistenceManager pm = ctx.getPersistenceManager();
        pm.currentTransaction().commit();
    }

    /**
     * Use current {@link PersistenceManager} to rollback transaction
     */
    public static void rollbackTxn() {
        Context ctx = Context.getContext();
        PersistenceManager pm = ctx.getPersistenceManager();
        pm.currentTransaction().rollback();
    }
}

