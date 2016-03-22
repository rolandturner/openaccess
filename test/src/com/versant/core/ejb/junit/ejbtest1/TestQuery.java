
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
package com.versant.core.ejb.junit.ejbtest1;

import com.versant.core.ejb.junit.VersantEjbTestCase;
import com.versant.core.ejb.junit.ejbtest1.model.*;

import javax.persistence.Query;
import javax.persistence.EntityManager;
import java.util.*;

public class TestQuery extends VersantEjbTestCase {

    public void createEmployees() {
        EntityManager em = emf().getEntityManager();

        em.getTransaction().begin();

        QueryEmployee queryEmployee = new QueryEmployee();
        queryEmployee.setFirstName("Carl");
        queryEmployee.setLastName("Cronje");
        em.persist(queryEmployee);

        QueryEmployee queryEmployee1 = new QueryEmployee();
        queryEmployee1.setFirstName("Jaco");
        queryEmployee1.setLastName("Uys");
        em.persist(queryEmployee1);

        QueryEmployee queryEmployee2 = new QueryEmployee();
        queryEmployee2.setFirstName("Dirk");
        queryEmployee2.setLastName("le Roux");
        em.persist(queryEmployee2);

        QueryEmployee queryEmployee3 = new QueryEmployee();
        queryEmployee3.setFirstName("David");
        queryEmployee3.setLastName("Tinker");
        em.persist(queryEmployee2);
        // create tasks

        Task task = new Task();
        task.setName("JDO");
        task.setDescription("JDO Project");
        em.persist(task);

        SmallTask sTask = new SmallTask();
        sTask.setName("JSR220");
        sTask.setDescription("Eclipse plugin");
        em.persist(sTask);

        BigTask bTask = new BigTask();
        bTask.setName("EJB3");
        bTask.setDescription("EJB3 Project");
        bTask.setBudget(1000000.0);
        em.persist(bTask);

        // Give employees tasks

        task.addTeamMember(queryEmployee1);
        queryEmployee1.addProject(task);

        sTask.addTeamMember(queryEmployee2);
        queryEmployee2.addProject(sTask);

        bTask.addTeamMember(queryEmployee);
        queryEmployee.addProject(bTask);

        bTask.addTeamMember(queryEmployee3);
        queryEmployee3.addProject(bTask);

        queryEmployee3.addManagedEmployee(queryEmployee);
        queryEmployee3.addManagedEmployee(queryEmployee1);
        queryEmployee3.addManagedEmployee(queryEmployee2);

        TelNumber telNumber = new TelNumber("home", "021", "7626394");
        em.persist(telNumber);
        queryEmployee.addPhoneNumber(telNumber);
        TelNumber telNumber1 = new TelNumber("home", "021", "7626394");
        em.persist(telNumber1);
        queryEmployee1.addPhoneNumber(telNumber1);
        TelNumber telNumber2 = new TelNumber("home", "021", "7626394");
        em.persist(telNumber2);
        queryEmployee2.addPhoneNumber(telNumber2);
        TelNumber telNumber3 = new TelNumber("home", "021", "7626394");
        em.persist(telNumber3);
        queryEmployee3.addPhoneNumber(telNumber3);


        QueryAddress address = new QueryAddress("2 Wargrave","Cape Town","Westren Cape", "South Africa", "7001");
        em.persist(address);
        queryEmployee.setQueryAddress(address);
        queryEmployee.setTime(new EmploymentTime(new java.sql.Date(1000000000000l), new java.sql.Date(new Date().getTime())));

        QueryAddress address1 = new QueryAddress("10 Bla", "Cape Town", "Westren Cape", "South Africa", "7001");
        em.persist(address1);
        queryEmployee1.setQueryAddress(address1);
        queryEmployee.setTime(new EmploymentTime(new java.sql.Date(1000000000000l), new java.sql.Date(new Date().getTime()+1000000)));

        QueryAddress address2 = new QueryAddress("20 Yo", "Cape Town", "Westren Cape", "South Africa", "7001");
        em.persist(address2);
        queryEmployee2.setQueryAddress(address2);
        queryEmployee.setTime(new EmploymentTime(new java.sql.Date(1000000000000l),null));

        QueryAddress address3 = new QueryAddress("30 Oink", "Cape Town", "Westren Cape", "South Africa", "7001");
        em.persist(address3);
        queryEmployee3.setQueryAddress(address3);
        queryEmployee.setTime(new EmploymentTime(new java.sql.Date(1000000000000l), null));

        em.getTransaction().commit();

    }



    public void testNamedQuery() {
        createEmployees();
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        Collection employees = em.createNamedQuery("findAllEmployeesByFirstName")
                .setParameter("firstname", "Carl").getResultList();
        Iterator employeeIterator = employees.iterator();
        // there should only be one
        while (employeeIterator.hasNext()) {
            QueryEmployee queryEmployee = (QueryEmployee) employeeIterator.next();
            assertEquals("We have not fetched the right employee", "Cronje", queryEmployee.getLastName());
        }
        em.getTransaction().commit();

    }

    public void testOrAndIsNull() {
        createEmployees();
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        // We should only return 3
        Query query = em.createQuery(
                "SELECT OBJECT(employee) " +
                "FROM QueryEmployee employee " +
                "WHERE employee.time.endDate IS NULL " +
                "OR employee.time.endDate > :currentDate");
        query.setParameter("currentDate", new java.sql.Date(System.currentTimeMillis()));
        assertEquals("We must return 3 employees",3, query.getResultList());
        em.getTransaction().commit();
    }

    public void testSmallerThan() {
        createEmployees();
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        Query query = em.createQuery(
                "SELECT OBJECT(employee) " +
                "FROM QueryEmployee employee " +
                "WHERE employee.time.endDate < :currentDate");
        query.setParameter("currentDate", new java.sql.Date(System.currentTimeMillis()));
        assertEquals("We must return 1 employees", 1, query.getResultList());
        em.getTransaction().commit();

    }

    public void testWithParam() {
        createEmployees();
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        Collection employees = em.createQuery(
                "SELECT OBJECT(employee) " +
                "FROM QueryEmployee employee " +
                "WHERE employee.lastName = :lastname")
                .setParameter("lastname", "Cronje").getResultList();
        assertEquals("We must return 1 employees", 1, employees.size());
        em.getTransaction().commit();
    }

    public void test2Ref() {
        createEmployees();
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        Collection employees = em.createQuery(
                "SELECT OBJECT(employee) " +
                "FROM QueryEmployee employee " +
                "WHERE employee.queryAddress.city = :city")
                .setParameter("city", "Cape Town").getResultList();
        assertEquals("We must return 4 employees", 4, employees.size());
        em.getTransaction().commit();
    }


    public void findAllEmployees() {
        createEmployees();
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        Collection employees = em.createQuery(
                "SELECT OBJECT(employee) " +
                "FROM QueryEmployee employee")
                .setHint("refresh", new Boolean(true))
                .getResultList();
        em.getTransaction().commit();
    }


    public void testNamedQueryWithParam() {
        createEmployees();
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        Task task = (Task) em.createNamedQuery("findTaskByName")
                .setParameter("name", "JDO").getSingleResult();
        assertEquals("Should just be on task","JDO Project", task.getDescription());
        em.getTransaction().commit();
    }

    public void testLike(Vector params) {
        createEmployees();
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        Collection projects = em.createQuery(
                "SELECT OBJECT(task) " +
                "FROM Task task " +
                "WHERE task.name LIKE :projectName")
                .setParameter("projectName", "E").getResultList();
        assertEquals("Should just be 2 task", 2, projects.size());
        em.getTransaction().commit();
    }



    public void testUpdate() {
        createEmployees();
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        StringBuffer buffer = new StringBuffer();
        buffer.append(
                "UPDATE QueryAddress address " +
                "SET address.city = '");
        buffer.append("New York");
        buffer.append("' WHERE address.city = '");
        buffer.append("Cape Town");
        buffer.append("'");
        String ejbqlString = buffer.toString();
        int updateCount = em.createQuery(ejbqlString).executeUpdate();
        assertEquals("Should have updated 4 employees", 4, updateCount);
        em.getTransaction().commit();
    }


}



