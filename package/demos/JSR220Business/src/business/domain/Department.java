
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
package business.domain;

import java.util.*;

import javax.persistence.Column;
import javax.persistence.*;
import javax.persistence.GeneratorType;
import javax.persistence.Id;
import javax.persistence.AccessType;
import javax.persistence.FetchType;


@Entity( access = AccessType.FIELD )
public class Department{
			
	@Id(generate=GeneratorType.IDENTITY)	
	@Column( name = "DEPARTMENT_ID" )
	private int generatedId;
	
	@Column( name = "NAME" )
	String 	name;

	@OneToMany( mappedBy = "department" )
	Collection<Employee> 	employees;
	
	@OneToOne(fetch = FetchType.LAZY)
	//@Column( name = "BPHONE_ID" )
	Phone bPhone;

	//constructors
	
	@SuppressWarnings("unchecked")
	public Department( ) {
		this.employees = new ArrayList();
		this.name = "";
		this.bPhone = new Phone( "555-1212" );
	}
	
	@SuppressWarnings("unchecked")
	public Department( String name ){
		this.employees = new ArrayList();
		this.name = name;
	}
	
	//setters
	
	public void setName( String name ){
		this.name = name;
	}
	
	public void addEmployee( Employee employee ){
	
		this.employees.add( employee );
		employee.setDepartment(this);
	}

	//getters
	
	public Employee getEmployeeNamed( String empName ){
		for (Employee emp : employees) {
			if (emp.getName().equals(empName)) {
				return emp;
			}
		}
		return null;
	}
	
	public String getName(){
		return this.name;
	}
	
	public Collection<Employee> getEmployees(){
		return this.employees;
	}
	
	public boolean hasEmployees(){
	       return ( employees.size() > 0 );
	}
	
	//supporting methods
	
	public void removeEmployee( Employee emp ){
		this.employees.remove(emp);
	}
	
	public void showDepartment() {
		System.out.println( "Department: " + this.getName() );
		if( employees.size() < 1 ){
			System.out.println( "There are no employees." );
			return;
		}
		this.showEmployees();
	}
	
	public void showEmployees() {
		for (Employee emp : employees) {
			emp.showEmployee();
		}
	}
	
	/**
	 * @return Returns the generatedId.
	 */
	public int getGeneratedId() {
		return generatedId;
	}
	
	/**
	 * @param generatedId The generatedId to set.
	 */
	public void setGeneratedId(int generatedId) {
		this.generatedId = generatedId;
	}
	
	/**
	 * @param employees The employees to set.
	 */
	public void setEmployees(Collection<Employee> employees) {
		this.employees = employees;
	}
	
	/**
	 * @return Returns the bPhone.
	 */
	public Phone getBPhone() {
		return bPhone;
	}
	
	/**
	 * @param phone The bPhone to set.
	 */
	public void setBPhone(Phone phone) {
		bPhone = phone;
	}

}

