
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
package com.versant.core.jdo.junit.test0.model;

import javax.jdo.InstanceCallbacks;
import javax.jdo.JDOHelper;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * @keep-all A person class that holds a pc ref to address
 */
public class Person implements Serializable, InstanceCallbacks {

    private String name = null;
    private String val;
    private Address address = null;
    private Date birthDate = null;
    private List personsList = new ArrayList();
    private Set personsSet = new HashSet();
    private Set stringSet = new HashSet();
    private List stringList = new ArrayList();
    private List orderedStringList = new ArrayList();
    private List orderedRefList = new ArrayList();
    private Date autoDate;
    private Collection refCol = new ArrayList();
    private SimpleAP_KeyGen simpleAP_keyGen;
    private Person person;
    private int intField;
    private String nonDFGString;
    private BigInteger bigIntegerField;
    private BigDecimal bigDecimalField;
    private Integer integerField;
    private int _underscoreField; // name generator should remove the underscore
    private Address txAddress = null;
    private Employee employee;
    private Employee zmployee;

    /**
     * A flag to indacate if postLoad has been called.
     */
    public transient int jdoPostLoadCalledCounter = 0;
    public transient int jdoPreClearCalledCounter = 0;
    public transient int jdoPreStoreCalledCounter = 0;
    public transient int jdoPreDeleteCalledCounter = 0;

    public Person() {
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getZmployee() {
        return zmployee;
    }

    public void setZmployee(Employee zmployee) {
        this.zmployee = zmployee;
    }

    public Address getTxAddress() {
        return txAddress;
    }

    public void setTxAddress(Address txAddress) {
        this.txAddress = txAddress;
    }

    public Person(String name) {
        this.name = name;
    }

    public Person(String name, Address address, Date birthDate) {
        this.name = name;
        this.address = address;
        this.birthDate = birthDate;
    }

    public Integer getIntegerField() {
        return integerField;
    }

    public void setIntegerField(Integer integerField) {
        this.integerField = integerField;
    }

    public BigInteger getBigIntegerField() {
        return bigIntegerField;
    }

    public void setBigIntegerField(BigInteger bigIntegerField) {
        this.bigIntegerField = bigIntegerField;
    }

    public BigDecimal getBigDecimalField() {
        return bigDecimalField;
    }

    public void setBigDecimalField(BigDecimal bigDecimalField) {
        this.bigDecimalField = bigDecimalField;
    }

    public String getNonDFGString() {
        return nonDFGString;
    }

    public void setNonDFGString(String nonDFGString) {
        this.nonDFGString = nonDFGString;
    }

    public int getIntField() {
        return intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public SimpleAP_KeyGen getSimpleAP_keyGen() {
        return simpleAP_keyGen;
    }

    public void setSimpleAP_keyGen(SimpleAP_KeyGen simpleAP_keyGen) {
        this.simpleAP_keyGen = simpleAP_keyGen;
    }

    public Collection getRefCol() {
        return refCol;
    }

    public void setRefCol(Collection refCol) {
        this.refCol = refCol;
    }

    public Date getAutoDate() {
        return autoDate;
    }

    public void setAutoDate(Date autoDate) {
        this.autoDate = autoDate;
    }

    public List getOrderedRefList() {
        return orderedRefList;
    }

    public void setOrderedRefList(List orderedRefList) {
        this.orderedRefList = orderedRefList;
    }

    public List getOrderedStringList() {
        return orderedStringList;
    }

    public void setOrderedStringList(List orderedStringList) {
        this.orderedStringList = orderedStringList;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public List getPersonsList() {
        return personsList;
    }

    public void setPersonsList(List personsList) {
        this.personsList = personsList;
    }

    public Set getPersonsSet() {
        return personsSet;
    }

    public void setPersonsSet(Set personsSet) {
        this.personsSet = personsSet;
    }

    public void addPersonToList(Person p) {
        personsList.add(p);
    }

    public void addPersonToSet(Person p) {
        personsSet.add(p);
    }

    public Set getStringSet() {
        return stringSet;
    }

    public void setStringSet(Set stringSet) {
        this.stringSet = stringSet;
    }

    public List getStringList() {
        return stringList;
    }

    public void setStringList(List stringList) {
        this.stringList = stringList;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Person) {
            Person other = (Person)obj;
            if (name != null) {
                if (!name.equals(other.name)) return false;
            } else {
                if (other.name != null) return false;
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        return name == null ? 0 : name.length();
    }

    public String toString() {
        return "Person name = " + name;
    }

    public void jdoPostLoad() {
        this.jdoPostLoadCalledCounter++;
    }

    public void jdoPreStore() {
        this.jdoPreStoreCalledCounter++;
    }

    public void jdoPreClear() {
        this.jdoPreClearCalledCounter++;
    }

    public void jdoPreDelete() {
        this.jdoPreDeleteCalledCounter++;
    }
}

