
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
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.acme.*;
/**
 * A demo for embeddable mapping using annotations
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
        removeCustomers();
        removeEmployees("PartTimeEmployee");
        removeEmployees("FullTimeEmployee");
        removePurchaseOrders();
        removeInvoices();
        removeServiceOptions();
        createCustomers();
        createEmployees();
        selectCustomers();
        updateAddresses();
        updateTerms();
        addPurchaseOrders();
        createInvoices();
        addServiceOptions();
    }

    /**
     * This method shows how entities are persisted
     */
    public void createCustomers() {
        System.out.println("----- Persisting Customers -----");
        
        String[] streetNames = {"Oak St.", "Elm St.", "Maple St."};

        em.getTransaction().begin(); // start transaction
        
        for (String s : streetNames) {
            // create customer
            Customer customer = new Customer();
            customer.setDescription("Retail Customer");
            // create and set address
            Address address = new Address();
            address.setStreet(s);
            customer.setAddress(address);
            System.out.println("Persisting "+ customer);
            em.persist(customer);
        }

        streetNames = new String[]{"Broadway", "Geary", "Vine"};
        for (String s : streetNames) {
            // create customer
            WholesaleCustomer customer = new WholesaleCustomer();
            customer.terms = "Net 30";
            customer.setDescription("Wholesale Customer");
            // create and set address
            Address address = new Address();
            address.setStreet(s);
            customer.setAddress(address);
            System.out.println("Persisting "+ customer);
            em.persist(customer);
        }

        em.getTransaction().commit();// end transaction
    }
    
    public void createEmployees() {
        System.out.println("----- Persisting Employees -----");
        
        String[] streetNames = {"Lemon", "Avocado", "Fig"};

        em.getTransaction().begin(); // start transaction
        Integer empId = 0;
        for (String s : streetNames) {
            // create full-time employee
            FullTimeEmployee employee = new FullTimeEmployee();
            employee.setEmpId(empId++);
            // create and set address
            Address address = new Address();
            address.setStreet(s);
            em.persist(address);
            employee.setAddress(address);
            employee.setSalary(150000);
            System.out.println("Persisting "+ employee);
            em.persist(employee);
        }

        streetNames = new String[]{"Selby St.", "Laurel St.", "Park St."};
        for (String s : streetNames) {
            // create part-time employee
            PartTimeEmployee employee = new PartTimeEmployee();
            employee.setEmpId(empId++);
            Address address = new Address();
            address.setStreet(s);
            em.persist(address);
            employee.setAddress(address);
            employee.setHourlyWage((float)150.00);
            System.out.println("Persisting "+ employee);
            em.persist(employee);
        }

        em.getTransaction().commit();// end transaction
    }
    
    public void removeCustomers() {
        System.out.println("----- Removing all Customers -----");

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

        q = em.createQuery("SELECT c FROM Customer c");
        result = q.getResultList();
        List<Customer> customerList = new ArrayList<Customer>();
 
        System.out.println("Result of query \"SELECT c FROM Customer c\"");
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Customer customer = (Customer) iterator.next();
            System.out.println(customer);
             customerList.add(customer);
        }

        for (Customer c : customerList) {
            em.remove(c);
        }

       em.getTransaction().commit();
   }

    public void removeEmployees(String table) {
        System.out.println("----- Removing Employees -----");

        em.getTransaction().begin();
        Query q;
        List result;
        
        q = em.createQuery("SELECT c FROM " + table + " c");
        result = q.getResultList();
        List<Employee> employeeList = new ArrayList<Employee>();
 
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Employee customer = (Employee) iterator.next();
            System.out.println(customer);
            employeeList.add(customer);
        }

        for (Employee c : employeeList) {
            em.remove(c);
        }

       em.getTransaction().commit();
   }

    public void removePurchaseOrders() {
        System.out.println("----- Removing all purchase orders -----");

        em.getTransaction().begin();
        
        Query q = em.createQuery("SELECT po FROM PurchaseOrder po");
        List result = q.getResultList();
        List<PurchaseOrder> poList = new ArrayList<PurchaseOrder>();
 
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            PurchaseOrder po = (PurchaseOrder) iterator.next();
             poList.add(po);
        }

        for (PurchaseOrder po : poList) {
            em.remove(po);
        }

       em.getTransaction().commit();
   }

    public void removeInvoices() {
        System.out.println("----- Removing all purchase orders -----");

        em.getTransaction().begin();
        
        Query q = em.createQuery("SELECT inv FROM Invoice inv");
        List result = q.getResultList();
        List<Invoice> invoiceList = new ArrayList<Invoice>();
 
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Invoice invoice = (Invoice) iterator.next();
             invoiceList.add(invoice);
        }

        for (Invoice invoice : invoiceList) {
            em.remove(invoice);
        }

       em.getTransaction().commit();
   }

    public void removeServiceOptions() {
        System.out.println("----- Removing all service options -----");

        em.getTransaction().begin();
        
        Query q = em.createQuery("SELECT ds FROM DeliveryService ds");
        List result = q.getResultList();
        List<DeliveryService> dsList = new ArrayList<DeliveryService>();
 
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            DeliveryService ds = (DeliveryService) iterator.next();
             dsList.add(ds);
        }

        for (DeliveryService ds : dsList) {
            em.remove(ds);
        }

       em.getTransaction().commit();
   }

    /**
     * This method shows basic querying
     */
    public void selectCustomers() {
        System.out.println("----- Selecting all Customers -----");

        em.getTransaction().begin();
        Query q = em.createQuery("SELECT c FROM Customer c");
        List result = q.getResultList();

        System.out.println("Result of query \"SELECT c FROM Customer c\"");
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Customer customer = (Customer)iterator.next();
            System.out.println(customer);
            System.out.println("\t" + customer.getAddress());
        }
        em.getTransaction().commit();
    }

    /**
     * This method shows basic querying
     */
    public void updateTerms() {
        System.out.println("----- Selecting wholesale customers -----");

        em.getTransaction().begin();
        Query q = em.createQuery("SELECT c FROM WholesaleCustomer c");
        List result = q.getResultList();

        System.out.println("Result of query \"SELECT c FROM WholesaleCustomer c\"");
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            WholesaleCustomer customer = (WholesaleCustomer)iterator.next();
            customer.setTerms("Net 60");
            System.out.println(customer);
            System.out.println("\tTerms: " + customer.terms);
        }
        em.getTransaction().commit();
    }

    /**
     * This method shows basic querying and setting values
     */
    public void updateAddresses() {
        System.out.println("----- Updating customer addresses -----");

        em.getTransaction().begin();
        Query q = em.createQuery("SELECT a FROM Address a where a.street like '%St.'");
        List result = q.getResultList();

        System.out.println("Result of query \"SELECT a FROM Address a\"");
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Address address = (Address)iterator.next();
            String s = address.getStreet(); 
            address.setStreet(s.replace("St.", "Ave."));
            System.out.println("Changing address " + s +
            		" to " + address.getStreet());
        }
        em.getTransaction().commit();
    }
    
    /**
     * This method shows basic adding one-to-many
     */
    public void addPurchaseOrders() {
        System.out.println("----- Selecting all customers -----");

        em.getTransaction().begin();
        Query q = em.createQuery("SELECT c FROM Customer c");
        List result = q.getResultList();
        List<Customer> customerList = new ArrayList<Customer>();
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Customer customer = (Customer)iterator.next();
            customerList.add(customer);
        }

        System.out.println("----- Adding purchase orders -----");

        String[] items = {"Redwood Bench", 
        		"Cherry Cabinet", "Hickory Table"};
        int nOrders = 1, idx = 0;
        for (Customer customer : customerList) {
        	for (int i = 0; i < nOrders; i++) {
	        	PurchaseOrder po = new PurchaseOrder();
	        	po.setItemName(items[idx++ % items.length]);
	        	po.setQuantity(1);
	        	System.out.println("Adding " + 
	        			po.getQuantity() + " " +
	        			po.getItemName() + 
	        			" to purchase order for " +
	        			customer.toString());
	        	customer.add(po);
        	}
        	nOrders = nOrders % items.length + 1;
        	idx = nOrders - 1;
        }
        em.getTransaction().commit();
    }
    
    public void createInvoices() {
        System.out.println("----- Creating invoices -----");

        em.getTransaction().begin();
        Query q = em.createQuery("SELECT c FROM WholesaleCustomer c");
        List result = q.getResultList();

        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            WholesaleCustomer customer = (WholesaleCustomer)iterator.next();
            Collection orders = customer.getOrders();
        	float amount = 0; 
            for (Object o : orders) {
            	PurchaseOrder po = PurchaseOrder.class.cast(o);
                amount += 100.00 * po.getQuantity();
            }
            Invoice invoice = new Invoice();
            invoice.setAmount(amount);
            em.persist(invoice);
            customer.add(invoice);
            System.out.println(customer);
            System.out.println("\tInvoice Amount: " + 
            		invoice.getAmount());
        }
        em.getTransaction().commit();
    }

    /**
     * This method shows basic adding many-to-many
     */
    public void addServiceOptions() {
        System.out.println("----- Selecting all Customers -----");

        em.getTransaction().begin();
        Query q = em.createQuery("SELECT c FROM Customer c");
        List result = q.getResultList();
        List<Customer> customerList = new ArrayList<Customer>();
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Customer customer = (Customer)iterator.next();
            customerList.add(customer);
        }

        System.out.println("----- Adding service options -----");
		DeliveryService ds = new DeliveryService();
		ds.setServiceName("Free Delivery");
		ds.setCustomers(customerList);
		em.persist(ds);
        em.getTransaction().commit();
    }

}


