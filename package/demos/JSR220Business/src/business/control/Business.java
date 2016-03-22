
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
package business.control;

import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import business.domain.*;

/**
 * @Pure Java Business Application 
 */

public class Business {
    Company company = null;
    ArrayList  unassignedEmployees = new ArrayList();
    CommIO input = new CommIO();
    String response = "true";
    int c = 0;

   static EntityManager em = null;
   static EntityManagerFactory factory = null;


    public static void main(String[] args) {
        Business biz = new Business();

        
        try{
        	 factory = Persistence.createEntityManagerFactory("mycontext");
             em = factory.createEntityManager(PersistenceContextType.EXTENDED);
        	
        }catch( PersistenceException p ){
        	p.printStackTrace();
        	System.exit(-1);
        } 
        

        biz.start();
		
        biz.init();
        
        try {
            biz.printMenu();
            while (biz.response.equals("true")) {
                biz.printRequest();
                int s = biz.c;
                switch (s) {
                    case 0:
                        biz.printMenu();
                        break;
                    case 1:
                        biz.createDepartment();
                        biz.commit();
                        break;
                    case 2:
                        biz.createEmployee();
                                    
                        break;
                    case 3:
                        biz.assignEmployee();                     
                        biz.commit();
                        break;
                    case 4:
                        biz.showDepartments();
                        biz.commit();
                        break;
                    case 5:
                        biz.showEmployeeFromDepartment();
                        break;
                    case 6:
                        biz.showEmployeesForDepartment();
                        break;
                    case 7:
                        biz.showEmployees();
                        break;
                    case 8:
						biz.updateEmployeePhone();
						biz.commit();
                        break;
                    case 9:
                        biz.findDepartmentOfEmployee();
                        break;
                    case 10:
                          Employee emp = biz.removeEmployee();
					
                          em.remove(emp);
                          biz.commit();
                          break;
					case 11:
						biz.reassignEmployee();
						biz.commit();
						break;
                    case 12:
                        biz.response = "false";
                        biz.stop();
                        break;
                    default:
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void start(){
    	em.getTransaction().begin();
    }  

	public void commit(){
		em.getTransaction().commit();
		em.getTransaction().begin();
	} 

	public void stop(){
		em.getTransaction().commit();
		em.close();
	}         

    private void init(){

     Query q = em.createQuery("SELECT comp FROM Company comp");
	 Iterator iter = ((List)q.getResultList()).iterator();
	 
     if (!iter.hasNext()) {
		 String response = input.getUserInput("What company are you opening the business for?");
         company = new Company( response );
         em.persist(company);
     }  else {
         company = (Company) iter.next();
         System.out.println("Company name: " + company.getName());    
     }

     commit();              

    }   

    public Employee removeEmployee(){

      Employee emp = findEmployeeByModel();
      if(emp != null) 
	     emp.getDepartment().removeEmployee( emp );
      
      return emp;
      
    //  Phone phone = emp.getPhone();
	//  em.Remove(phone);  THIS IS DONE VIA META INFO

    }

    public Phone updateEmployeePhone() {

	  Phone p = null;
	  /**
	   * The following can be replaced by findEmployeeByQuery to show
	   * a more realistic business use case which would normall start with
	   * a query instead of blindly traversing the entire model
	   */
      Employee emp = findEmployeeByModel();  
      if( emp != null ){
          p = emp.getPhone();
          System.out.println("The employee's current phone is: " + p.getNumber());
          String newNumber = input.getUserInput(
              "What do you want to change it to? ");
          p.setNumber(newNumber);
      }
      return p;
    }

    
    @SuppressWarnings("unused")
	private Employee findEmployeeByQuery(){
      Employee emp = null;
      String name = input.getUserInput("What is the employee's name?");
      String title = input.getUserInput("What is the employee's title?" );

	  Query q = em.createQuery("SELECT emp FROM Employee emp WHERE emp.name = :nme AND emp.title = :ttl ");
	  q.setParameter("nme" , name );
	  q.setParameter("ttl" , title );
	  Iterator found = ((List)q.getResultList()).iterator();

      if( found.hasNext() ){
         emp = (Employee)found.next();
      }else{
        System.out.println( "No employee with that name and title" );
      }

      return emp;

    }     
    
    
	private Employee findEmployeeByModel(){
	  Employee emp = null;
	  String name = input.getUserInput("What is the employee's name?");
	  String title = input.getUserInput("What is the employee's title?" );

	  Iterator overDept = company.getDepartments().iterator();
	  while( overDept.hasNext() ){
		Department current = (Department)overDept.next();
		Iterator overEmps = current.getEmployees().iterator();
		while( overEmps.hasNext() ){
			emp = (Employee)overEmps.next();
			if( emp.getName().matches(name) && 
					emp.getTitle().matches(title)) return emp;
		}
	  }
	  if( emp == null ) 
		System.out.println( "There is no employee with the name: " + name + " and title: "  + title );
	  return null;

	}
	
    public void findDepartmentOfEmployee() {

		Employee emp = findEmployeeByModel();
		if( emp != null )
			System.out.println( "Employee's department is: " + emp.getDepartment().getName() );

	}
	
    public void assignEmployee(Employee empl) {
        Department dpt = this.selectDepartment();
        if (dpt != null) {
            dpt.addEmployee(empl);
            em.persist(empl);
        }
    }

    public Employee assignEmployee() {
        Department dpt = this.selectDepartment();
        Employee empl = null;
        
        if (dpt != null) {
            empl = findUnassignedEmployee();
            if (empl != null){
              dpt.addEmployee(empl);
              em.persist(empl);
              System.out.println("I just made an assignment to department");
            }else{
              System.out.println("There is no unassigned employee by that name.");
            }
        }
        return empl;
    }

	public void reassignEmployee() {
		Employee emp = findEmployeeByModel();
		if (emp != null) {
			Department dept = selectDepartment();
			if (dept != null){
				emp.getDepartment().removeEmployee(emp);
				dept.addEmployee(emp);
			  	System.out.println("I just made a reassignment to another department");
			}else{
			  System.out.println("There is no Department by that name.");
			}
		}
	}
	
    public Employee findUnassignedEmployee(){

      Employee emp = null;
      Iterator emps = unassignedEmployees.iterator();
      String name = getEmployeeName();
      while( emps.hasNext() ){
        emp = (Employee)emps.next();
        if( emp.getName().equals(name) ) return emp;
      }
      return null;
    }

    public Department createDepartment() {
        Department dpt = null;
        String resp = input.getUserInput("What is the department name?");
        dpt = new Department(resp);
        company.getDepartments().add(dpt);
        em.persist(dpt);
        return dpt;
    }

    @SuppressWarnings("unchecked")
	public void createEmployee() {
        Employee empl = null;

        String resp = input.getUserInput("What is the employee's name?");
        empl = new Employee(resp);
        empl.setTitle(input.getUserInput("What is the employee's title?"));
        empl.setPhone(this.createPhone());
        System.out.println("Please note - you must assign the employee to a department");
        unassignedEmployees.add(empl);
    }

    public Phone createPhone() {
        Phone phone = null;
        System.out.println("What is the Employee's phone number?");
        String resp = input.getUserInput();
        phone = new Phone(resp);
        return phone;
    }

    public Employee selectEmployee(Department dept) {
        Collection<Employee> emps = dept.getEmployees();
        if (emps.size() < 1) {
            System.out.println("There are no employees.");
            return null;
        }
        Employee empl = null;
        String resp = getEmployeeName();
        Iterator employees = emps.iterator();
        while(employees.hasNext()) {
            empl = (Employee)employees.next();
            if (empl.getName().equals(resp)) return empl;
        }
        System.out.println("There is no Employee with that name in the " + dept.getName() + " department.");
        return null;
    }

    public String getEmployeeName(){
      System.out.println("What is the employees Name?");
      String resp = input.getUserInput();
      return resp;
    }
    
    public Employee selectEmployee() {

      Iterator ofDepartments = null;
      Department dept = null;
      Employee emp = null;

      String resp = getEmployeeName();

        ofDepartments = company.getDepartments().iterator();
        while( ofDepartments.hasNext() ){
          dept = (Department)ofDepartments.next();
          emp = dept.getEmployeeNamed( resp );
          if( emp != null ) return emp;
        }

        System.out.println("There is no Employee with that name.");
        return null;
    }

    public Department selectDepartment() {
        if (company.getDepartments().size() < 1) {
            System.out.println("There are no departments.");
            return null;
        }
        Department dpt = null;
        System.out.println("What is the department Name?");
        String resp = input.getUserInput();
        Collection<Department> depts = company.getDepartments();
        Iterator over = depts.iterator();
        while( over.hasNext()){
            dpt = (Department)over.next();
            if (dpt.getName().equals(resp)) return dpt;        	
        }
        System.out.println("There is no Department with that name.");
        return null;
    }

    public void showDepartments() {
        Collection<Department> depts = company.getDepartments();
        Department dpt = null;

        if (depts.size() < 1) {
            System.out.println("There are no departments.");
            return;
        }
        Iterator e = depts.iterator();
        while (e.hasNext()) {
            dpt = (Department)e.next();
            dpt.showDepartment();
        }
    }

    public void showEmployeeFromDepartment() {
        Department dept = selectDepartment();
        if( dept == null){
          return;
        }
        Employee emp = selectEmployee(dept);
        if( emp == null ){
          return;
        }
        emp.showEmployee();
    }

    public void showEmployeesForDepartment() {
        Department dept = selectDepartment();
        if( dept == null ) 
        	return;
        showEmployees(dept);
    }

    public void showEmployees(Department dept) {
        Collection<Employee> emps = dept.getEmployees();
        if (emps.size() < 1) {
            System.out.println("There are no employees.");
            return;
        }
        Iterator e = emps.iterator();
        while (e.hasNext()) {
            Employee empl = (Employee)e.next();
            empl.showEmployee();
        }
    }

    public void showEmployees() {

      boolean hasEmployees = false;
      Iterator e = null;
      Department dept = null;

        e = company.getDepartments().iterator();
        while (e.hasNext()) {
            dept = (Department)e.next();
            if( !hasEmployees ) hasEmployees = dept.hasEmployees();
            dept.showEmployees();
        }
        if (!hasEmployees) {
            System.out.println("There are no employees.");
            return;
        }

    }
    
    public void printMenu() {
        //Note - if you add to this menu you must change the range in <printRequest() >
        System.out.println("0 - Print Menu");
        System.out.println("1 - Create Department");
        System.out.println("2 - Create Employee");
        System.out.println("3 - Assign Employee to department");
        System.out.println("4 - Show Departments");
        System.out.println("5 - Show Employee from Department");
        System.out.println("6 - Show Employees for Department");
        System.out.println("7 - Show Employees");
        System.out.println("8 - Update Employees Phone");
        System.out.println("9 - Find Employees Department");
        System.out.println("10 - Remove Employee");
        System.out.println("11 - Reassign Employee" );
        System.out.println("12 - Quit");
        System.out.println();
    }

    public void printRequest() {
        int range = 12;
        System.out.println("Make a selection");
        String in = input.getUserInput();
        try {
            c = Integer.valueOf(in).intValue();
        } catch (NumberFormatException n) {
            System.out.println("Invalid selection - pick a number!");
            printRequest();
        }
        if (c > range) {
            System.out.println("Invalid selection - pick a number between 0 and " + range);
            printRequest();
        }
    }

}
