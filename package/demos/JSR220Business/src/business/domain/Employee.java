
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

import javax.persistence.*;
import javax.persistence.AccessType;


@Entity( access = AccessType.FIELD )
public class Employee extends Person{
	
	@Column( name = "EMPLOYEE_ID" )
	private int generatedId;
	
	@Basic
	String 	title;
	
	@OneToOne
	Department department;
	
	@Basic
	float	salary;


	//constructors
	
	public Employee(){
	}
	
	public Employee( String name ){
	    super(name);
	    this.title = "";
	    this.salary = 0;
	}

	//setters
	
	public void setTitle( String title ){
		this.title = title;
	}

	//getters
	
	public String getTitle( ){
		return this.title;
	}

	//supporting
	
	public void showEmployee(){
        System.out.println( "Employee: ");
        System.out.println( "\tName: " + this.getName() );
	    System.out.println( "\tTitle: " + this.getTitle() );
	    if( this.getPhone() != null ) 
	    	this.numbr.showPhone();
	}

	/**
	 * @return Department department
	 */
	public Department getDepartment() {
		return department;
	}

	/**
	 * @param department
	 */
	public void setDepartment(Department department) {
		this.department = department;
	}


	/**
	 * @return float salary
	 */
	public float getSalary() {
		return salary;
	}

	/**
	 * @param f float
	 */
	public void setSalary(float f) {
		salary = f;
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

}
