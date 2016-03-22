
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

import junit.framework.Assert;

import javax.jdo.JDOHelper;
import java.io.Serializable;
import java.util.Random;
import java.util.StringTokenizer;

public class Person4 implements Cloneable, Serializable {
    private long personNum;
    private String globalNum;

    private String firstName;
    private String lastName;
    private String emailAddress;

    public Person4() {
    }

    public Person4(long num, String first, String last, String email) {
        globalNum = "global:" + Math.abs(new Random().nextInt());
        personNum = num;
        firstName = first;
        lastName = last;
        emailAddress = email;
    }

    /**
     * @return Returns the globalNum.
     */
    public String getGlobalNum() {
        return globalNum;
    }

    /**
     * @param globalNum The globalNum to set.
     */
    public void setGlobalNum(String globalNum) {
        this.globalNum = globalNum;
    }

    public long getPersonNum() {
        return personNum;
    }

    public void setPersonNum(long num) {
        personNum = num;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String s) {
        firstName = s;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String s) {
        lastName = s;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String s) {
        emailAddress = s;
    }

    public void assertEquals(Person4 p) {
        Assert.assertEquals(lastName, p.lastName);
        Assert.assertEquals(firstName, p.firstName);
        Assert.assertEquals(emailAddress, p.emailAddress);
        Assert.assertEquals(personNum, p.personNum);
    }

    public boolean compareTo(Object obj) {
        Person4 p = (Person4) obj;
        return lastName.equals(p.lastName) && firstName.equals(p.firstName)
                && emailAddress.equals(p.emailAddress) && personNum == p.personNum;
    }

    public int hashCode() {
        Object id = JDOHelper.getObjectId(this);

        return id == null ? super.hashCode() : id.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        Object id = JDOHelper.getObjectId(this);

        return id == null ? super.equals(o) : id.equals(JDOHelper.getObjectId(o));
    }

    public String toString() {
        Object id = JDOHelper.getObjectId(this);

        if (id == null) {
            id = "<not persistent>";
        }

        return "ID: " + id + "\n" +
                "PersonNum: " + getPersonNum() + "\n" +
                "Lastname: " + getLastName() + "\n" +
                "Firstname: " + getFirstName() + "\n" +
                "Email: " + getEmailAddress() + "\n";
    }
}
