
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
import javax.persistence.InheritanceType;
import javax.persistence.GeneratorType;
import javax.persistence.DiscriminatorType;

/**
 * @author Robert Greene
 * @version 1.0
 */

@Entity( access = AccessType.FIELD )
@Inheritance( strategy = InheritanceType.JOINED, discriminatorType = DiscriminatorType.INTEGER )
public class Person {

	@Id(generate=GeneratorType.IDENTITY)	
	@Column( name = "PERSON_ID" )
	private int generatedId;
	
	@Basic
	String 	name;
	
	@OneToOne( cascade = {CascadeType.ALL} )
	/**
	 * says the following @Column annotation cannot be used on an association attribute.
	 * I think this is not spec compliant because if I leave the attribute as "number"
	 * then it is a keyword that Oracle will not support as a column name.  
	 * I had to change the name of my attribute.
	 */
	//@Column( name = "NUMBR" )  
	Phone 	numbr;


	//constructors
	
	public Person(){
	  this.name = "";
	  this.numbr = null;
	}
	
	public Person( String name ){
	  this();
	  this.name = name;
	}
	
	//setters
	
	public void setName( String name ){
	  this.name = name;
	}
	
	public void setPhone( Phone number ){
	  this.numbr = number;
	}
	
	//getters
	
	public String getName( ){
	  return this.name;
	}
	
	public Phone getPhone( ){
	  return this.numbr;
	}
	
	//supporting
	
	public void showPerson(){
	  System.out.println( "\tPerson: " + this.getName() );
	  if( this.numbr != null ) this.numbr.showPhone();
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
	 * @return Returns the number.
	 */
	public Phone getNumber() {
		return numbr;
	}
	
	/**
	 * @param number The number to set.
	 */
	public void setNumber(Phone number) {
		this.numbr = number;
	}

}
