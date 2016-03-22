
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

import java.util.*;
import java.io.*;



/**
 *
 */
public class ClassInfo {
	private String className;
	private String objectidClass;
	private String persistenceCapableSuperclass;
	private String topName;

	private ClassInfo topPersistenceCapableSuperclass;

	private Set fieldList = new TreeSet();

	private int identityType;
    private boolean hasKeyGen;
	private boolean isInstanceCallbacks = false;

	public boolean isInstanceCallbacks() {
		return isInstanceCallbacks;
	}

	public void setInstanceCallbacks(boolean instanceCallbacks) {
		isInstanceCallbacks = instanceCallbacks;
	}

	public String getTopName() {
		return topName;
	}

	public void setTopName(String topName) {
		this.topName = topName;
	}

    public boolean isKeyGen() {
        return hasKeyGen;
    }

    public void setKeyGen(boolean hasKeyGen) {
        this.hasKeyGen = hasKeyGen;
    }


    public ClassInfo() {}
	/**
	 * Sets class name.
	 */
	public void setTopPCSuperClass(ClassInfo topPersistenceCapableSuperclass){
		this.topPersistenceCapableSuperclass = topPersistenceCapableSuperclass;
	}
	/**
	 * Gets class name.
	 */
	public ClassInfo getTopPCSuperClass(){
		return topPersistenceCapableSuperclass;
	}
	/**
	 * Sets class name.
	 */
	public void setClassName(String className){
		this.className = className;
	}
	/**
	 * Gets class name.
	 */
	public String getClassName(){
		return className;
	}
	/**
	 * Sets identity type.
	 */
	public void setIdentityType(int identityType){
		this.identityType = identityType;
	}
	/**
	 * Gets identity type.
	 */
	public int getIdentityType(){
		return identityType;
	}
	/**
	 * Sets object id class.
	 */
	public void setObjectidClass(String objectidClass){
		this.objectidClass = objectidClass;
	}
	/**
	 * Gets object id class.
	 */
	public String getObjectidClass(){
		return objectidClass;
	}

	/**
	 * Sets persistence capable super class.
	 */
	public void setPersistenceCapableSuperclass(String persistenceCapableSuperclass){
		this.persistenceCapableSuperclass = persistenceCapableSuperclass;
	}
	/**
	 * Gets persistence capable super class.
	 */
	public String getPersistenceCapableSuperclass(){
		return persistenceCapableSuperclass;
	}

	/**
	 * Sets field list.
	 */
	public void setFieldList(Set fieldList){
		this.fieldList = fieldList;
	}
	/**
	 * Gets field list.
	 */
	public Set getFieldList(){
		return fieldList;
	}


	public String toString(){
	    return  "\n************ Class "+className+"************\n"+
			    "className                      = "+className+"\n"+
			    "identityType                   = "+identityType+"\n"+
				"objectidClass                  = "+objectidClass+"\n"+
				"persistenceCapableSuperclass   = "+persistenceCapableSuperclass+"\n"+
				"topPersistenceCapableSuperclass= "+(topPersistenceCapableSuperclass == null ? null : topPersistenceCapableSuperclass.className)+"\n"+
				"fieldList                      = "+fieldList+"\n"+
				"********************************************\n";
	}
}
