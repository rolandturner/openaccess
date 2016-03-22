
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
import javax.persistence.GeneratorType;

@Entity( access = AccessType.FIELD )
public class Phone{

	@Id(generate=GeneratorType.IDENTITY)	
	@Column( name = "PHONE_ID" )
	private int generatedId;
	
	@Basic
	@Column( name = "NUMBR" )
	String 	number;

//constructors
public Phone(){
	this.number = "";
}
public Phone( String number ){
	this();
	this.number = number;
}

//setters

public void setNumber( String number ){
	this.number = number;
}

//getters

public String getNumber( ){
	return this.number;
}
// supporting
public void showPhone(){
	System.out.println( "\tPhone: " + this.number );
}

public String toString(){
	return this.number;
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
