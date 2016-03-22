
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
package ejb3;

import ejb3.model.Employee;
import ejb3.model.Address;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A very simple demo for EJB3 persistance
 */
public class Main {

    public static void main(String[] args) {
    	EntityManagerFactory emf = null;
    	EntityManager em = null;
        try {
        	emf = Persistence.createEntityManagerFactory();
        	em = emf.createEntityManager();
            Main main = new Main(em);
            main.go();
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.exit(1);
        }
        finally {
        	if (em != null) {
                em.close();
        	}
        	if (emf != null) {
                emf.close();
        	}
        }
    }
    
    EntityManager em;
    
    private Main(EntityManager em) {
    	this.em = em;
    }

    private void go() {
        removeAllEmployees();
        persistSomeEmployees();
        selectAllEmployees();
        selectEmployeeAndSetSalary();
        findEmployeeByIdAndCheckSalary();
        setManagerOnEmployee();
        removeEmployee();
        em.close();
    }

    /**
     * This method shows how entity's are persisted
     */
    public void persistSomeEmployees() {
        System.out.println("----- Persisting all Employees -----");
 
        // create employee
        Employee emp1 = new Employee();
        emp1.setEmpNo(11); // primary key
        emp1.setEName("carl");
        // create and set address
        Address address1 = new Address();
        address1.setCity("Cape Town");
        address1.setStreet("Wargrave");
        address1.setCode(7000);
        emp1.setAddress(address1);

        Employee emp2 = new Employee();
        emp2.setEmpNo(22); // primary key
        emp2.setEName("jaco");
        // create and set address
        Address address2 = new Address();
        address2.setCity("Somerset West");
        address2.setStreet("Watsonia");
        address2.setCode(5000);
        emp2.setAddress(address2);

        Employee emp3 = new Employee();
        emp3.setEmpNo(33); // primary key
        emp3.setEName("dave");
        // create and set address
        Address address3 = new Address();
        address3.setCity("Kenilworh");
        address3.setStreet("Dunkin");
        address3.setCode(6000);
        emp3.setAddress(address3);

        Employee emp4 = new Employee();
        emp4.setEmpNo(44); // primary key
        emp4.setEName("dirk");
        // create and set address
        Address address4 = new Address();
        address4.setCity("Constansia");
        address4.setStreet("Main");
        address4.setCode(8000);
        emp4.setAddress(address4);

        // EJB3 persistance code
        em.getTransaction().begin(); // start transaction
        em.persist(emp1);
        System.out.println("Persisting "+ emp1);
        em.persist(emp2);
        System.out.println("Persisting " + emp2);
        em.persist(emp3);
        System.out.println("Persisting " + emp3);
        em.persist(emp4);
        System.out.println("Persisting " + emp4);
        em.getTransaction().commit();// end transaction
    }
    public void removeAllEmployees() {
        System.out.println("----- Removing all Employees -----");

        em.getTransaction().begin();
        
        Query q = em.createQuery("SELECT a FROM Address a");
        List result = q.getResultList();
        
        List<Address> addressList = new ArrayList<Address>();
 
        System.out.println("Result of query \"SELECT a FROM Address a\"");
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Address address = (Address) iterator.next();
            System.out.println(address);
            addressList.add(address);
        }
        for (Address a : addressList) {
            em.remove(a);
        }
       q = em.createQuery("SELECT e FROM Employee e");
        result = q.getResultList();
        
        List<Employee> employeeList = new ArrayList<Employee>();
 
        System.out.println("Result of query \"SELECT e FROM Employee e\"");
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Employee employee = (Employee) iterator.next();
            System.out.println(employee);
             employeeList.add(employee);
        }

        for (Employee e : employeeList) {
            em.remove(e);
        }

       em.getTransaction().commit();
 
   }

    /**
     * This method shows basic querying
     */
    public void selectAllEmployees() {
        System.out.println("----- Selecting all Employees -----");

        em.getTransaction().begin();
        Query q = em.createQuery("SELECT e FROM Employee e");
        List result = q.getResultList();

        System.out.println("Result of query \"SELECT e FROM Employee e\"");
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Employee employee = (Employee) iterator.next();
            System.out.println(employee);
            System.out.println("      "+employee.getAddress());
        }
        em.getTransaction().commit();
    }

    /**
     * This method shows basic querying and setting values
     */
    public void selectEmployeeAndSetSalary() {
        System.out.println("----- Selecting a Employee and set salary-----");

        em.getTransaction().begin();
        Query q = em.createQuery("SELECT e FROM Employee e WHERE e.eName = ?1");
        q.setParameter(0, "dirk");
        List result = q.getResultList();

        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Employee employee = (Employee) iterator.next();

            System.out.println("Setting salary to 99999 on " + employee);
            employee.setSal(99999); // set salary
        }
        em.getTransaction().commit();
    }

    /**
     * This method shows how EJB3 finds object.
     */
    public void findEmployeeByIdAndCheckSalary () {
        System.out.println("----- Find a Employee and Check Salary -----");
 
        em.getTransaction().begin();
        Employee w = em.find(Employee.class, 44);
        System.out.println("Salary should be 99999 and is "+ w.getSal());
        em.getTransaction().commit();
    }

    /**
     * This method shows how you merge changes that was done outside a transaction
     */
    public void setManagerOnEmployee() {
        System.out.println("----- Set manager on Employee outside a transaction -----");

        em.getTransaction().begin();
        Employee manager = em.find(Employee.class, 44);
        Employee employee = em.find(Employee.class, 11);
        em.getTransaction().commit();

        // setting stuff outside a transaction
        employee.setManager(manager);

        em.getTransaction().begin();
        em.merge(employee);
        em.getTransaction().commit();

        em.getTransaction().begin();
        employee = em.find(Employee.class, 11);
        System.out.println("Manager set to "+employee.getManager());
        em.getTransaction().commit();
    }

    /**
     * This method shows how objects are removed from the database
     */
    public void removeEmployee() {
        System.out.println("----- remove Address on a Employee -----");
 
        em.getTransaction().begin();
        Employee jaco = em.find(Employee.class, 22);
        int code = jaco.getAddress().getCode();
        em.remove(jaco);
        //em.remove(jaco.getAddress());
        em.getTransaction().commit();

        em.getTransaction().begin();
        Query q = em.createQuery(
                "SELECT a " +
                "FROM Address a " +
                "WHERE a.code = ?1");
        q.setParameter(0, code);
        List result = q.getResultList();

        // there must be no results
        System.out.println("result.size() must be 0 and is "+ result.size());
        em.getTransaction().commit();
    }

}


