
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
package com.versant.core.jdo.tools.enhancer.info;

import com.versant.lib.bcel.generic.Type;


import com.versant.core.metadata.MDStatics;

import javax.jdo.spi.PersistenceCapable;
/**
 *
 */
public class FieldInfo implements Comparable{

	private String jdoSetName;
	private String jdoGetName;
	private String returnType;
	private String fieldName;
    private String className;
	private String primativeTypeObject;
	private String signature;
	private Type type;
    private int fieldNo;
	private int persistenceModifier;
	private boolean primaryKey;
	private boolean defaultFetchGroup;
	private boolean isPublic;
	private boolean isPrivate;
	private boolean isProtected;
	private boolean isArray;
	private boolean isPrimative;




    public FieldInfo() {}


    public int getFieldNo() {
        return fieldNo;
    }

    public void setFieldNo(int fieldNo) {
        this.fieldNo = fieldNo;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }



	public void setSignature(String signature){
		this.signature = signature;
	}


	public String getSignature(){
	    return signature;
	}

	public void setType(Type type){
		this.type = type;
	}
	public Type getType(){
	    return type;
	}

	public byte getFlag(){
		if (persistenceModifier == MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL){
		    return PersistenceCapable.CHECK_WRITE;
		} else if (primaryKey){
			return PersistenceCapable.MEDIATE_WRITE;
		} else if (defaultFetchGroup){
			return PersistenceCapable.CHECK_READ + PersistenceCapable.CHECK_WRITE;
		} else {
			return PersistenceCapable.MEDIATE_READ + PersistenceCapable.MEDIATE_WRITE;
		}
	}

	/**
	 * Implements Comparable to order all fields by name
	 *
	 */
	public int compareTo(Object o){
		FieldInfo other = (FieldInfo)o;
		return fieldName.compareTo(other.getFieldName());
	}

	public boolean equals(Object other){
		if (other != null && getClass() == other.getClass()){
			FieldInfo otherFieldInfo = (FieldInfo)other;
			return fieldName.equals(otherFieldInfo.getFieldName());
		} else {
		    return false;
		}
	}

	public int hashCode(){
	    return 13 * fieldName.hashCode();
	}
	/**
	 * Gets the jdoSetName.
	 */
	public String getJdoSetName(){
		return jdoSetName;
	}
	/**
	 * Sets the jdoSetName.
	 */
	public void setJdoSetName(String jdoSetName){
		this.jdoSetName = jdoSetName;
	}
	/**
	 * Gets the jdoGetName.
	 */
	public String getJdoGetName(){
		return jdoGetName;
	}
	/**
	 * Sets the jdoGetName.
	 */
	public void setJdoGetName(String jdoGetName){
		this.jdoGetName = jdoGetName;
	}
	/**
	 * Gets the returnType.
	 */
	public String getReturnType(){
		return returnType;
	}
	/**
	 * Sets the returnType.
	 */
	public void setReturnType(String returnType){
		this.returnType = returnType;
	}
	/**
	 * Gets the fieldName.
	 */
	public String getFieldName(){
		return fieldName;
	}
	/**
	 * Sets the fieldName.
	 */
	public void setFieldName(String fieldName){
		this.fieldName = fieldName;
	}

	/**
	 * Gets the persistence modifier.
	 */
	public int getPersistenceModifier(){
		return persistenceModifier;
	}
	/**
	 * Sets the persistence modifier.
	 */
	public void setPersistenceModifier(int persistenceModifier){
		this.persistenceModifier = persistenceModifier;
	}

	/**
	 * Sets primaryKey.
	 */
	public void primaryKey(boolean primaryKey){
	    this.primaryKey = primaryKey;
	}
	/**
	 * Gets primaryKey.
	 */
	public boolean primaryKey(){
	    return primaryKey;
	}
	/**
	 * Sets default fetch group.
	 */
	public void defaultFetchGroup(boolean defaultFetchGroup){
	    this.defaultFetchGroup = defaultFetchGroup;
	}


	/**
	 * Sets isPublic.
	 */
	public void isPublic(boolean isPublic){
	    this.isPublic = isPublic;
	}
	/**
	 * Gets isPublic.
	 */
	public boolean isPublic(){
	    return isPublic;
	}
	/**
	 * Sets isPrivate.
	 */
	public void isPrivate(boolean isPrivate){
	    this.isPrivate = isPrivate;
	}
	/**
	 * Gets isPrivate.
	 */
	public boolean isPrivate(){
	    return isPrivate;
	}
	/**
	 * Sets isProtected.
	 */
	public void isProtected(boolean isProtected){
	    this.isProtected = isProtected;
	}
	/**
	 * Gets isProtected.
	 */
	public boolean isProtected(){
	    return isProtected;
	}
	/**
	 * Sets isArray.
	 */
	public void isArray(boolean isArray){
	    this.isArray = isArray;
	}
	/**
	 * Gets isArray.
	 */
	public boolean isArray(){
	    return isArray;
	}
	/**
	 * Sets isPrimative.
	 */
	public void isPrimative(boolean isPrimative){
	    this.isPrimative = isPrimative;
	}
	/**
	 * Gets isPrimative.
	 */
	public boolean isPrimative(){
	    return isPrimative;
	}

	/**
	 * Sets primative type object.
	 */
	public void setPrimativeTypeObject(String primativeTypeObject){
	    this.primativeTypeObject = primativeTypeObject;
	}
	/**
	 * Gets primative type object.
	 */
	public String getPrimativeTypeObject(){
	    return primativeTypeObject;
	}

	public String toString(){
	    return  "\n************ Field "+fieldName+"************\n"+
			    "jdoSetName             = "+jdoSetName+"\n"+
			    "jdoGetName             = "+jdoGetName+"\n"+
				"returnType             = "+returnType+"\n"+
				"fieldName              = "+fieldName+"\n"+
				"persistenceModifier    = "+persistenceModifier+"\n"+
				"primaryKey             = "+primaryKey+"\n"+
				"defaultFetchGroup      = "+(primaryKey ? false : defaultFetchGroup)+"\n"+
				"primativeTypeObject    = "+primativeTypeObject+"\n"+
				"isPublic               = "+isPublic+"\n"+
				"isPrivate              = "+isPrivate+"\n"+
				"isProtected            = "+isProtected+"\n"+
				"isArray                = "+isArray+"\n"+
				"isPrimative            = "+isPrimative+"\n"+
				"Type                   = "+type+"\n"+
				"*********************************************\n";
	}

}
