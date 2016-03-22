
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

import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.GeneratorType;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;


/**
 * @author Robert Greene
 * @version 1.0
 */

@Entity( access = AccessType.FIELD )
public class Company {

	@Id(generate=GeneratorType.IDENTITY)	
	private int companyId;
	
	@Column( name = "NAME" )
	String 	name;

	@ManyToMany(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST,CascadeType.MERGE})
	Collection<Department> departments;

	//constructors
	
	@SuppressWarnings("unchecked")
	public Company( ) {
	  this.departments = new ArrayList();
	  this.name = "";
	}
	
	public Company( String name ){
	  this();
	  this.name = name;
	}
	
	//setters
	
	public void setName( String name ){
	  this.name = name;
	}
	
	@SuppressWarnings("unchecked")
	public void addDepartment( Department department ){
	  this.departments.add( department );
	}
	
	//getters
	
	public Department getDepartmentNamed( String deptName ) {
		for (Department dept : departments) {
			if( dept.getName().equals(deptName) ) {
				return dept;
			}
		}
		return null;
	}
	
	public String getName(){
	  return this.name;
	}
	
	public Collection<Department> getDepartments(){
	  return this.departments;
	}
	
	public void setDepartments(Collection<Department> departments){
		  this.departments = departments;
	}
	
	public boolean hasDepartments(){
		return ( departments.size() > 0 );
	}
	
	//supporting methods
	public void showCompany(){
	   System.out.println( "Company: " + this.getName() );
	   if( departments.size() < 1 ){
		  System.out.println( "There are no departments." );
		  return;
	   }
	   this.showDepartments();
	}
	
	public void showDepartments(){
		for (Department dept : departments) {
	        dept.showDepartment();
		}
	}
	
	public int getCompanyId() {
		return companyId;
	}
	
	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

}
