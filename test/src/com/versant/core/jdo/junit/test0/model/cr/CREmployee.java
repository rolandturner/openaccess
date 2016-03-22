
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
package com.versant.core.jdo.junit.test0.model.cr;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class CREmployee extends CRPerson {
    protected int seniority = 0;
    protected int salary = 0;

//            [Versant.ItemType(typeof(Person))]
    protected List subordinates = new ArrayList();
    protected int deptno = 0;

    public CREmployee() {
    }

    public CREmployee(String name,
            java.util.Date birthday,
            String passportNo,
            CRAddress address,
            int seniority,
            int salery,
            int department) {
        super(name, birthday, passportNo, address);
        this.seniority = seniority;
        this.salary = salery;
        this.deptno = department;
    }

    public void addSubordinate(CRPerson p) {
        subordinates.add(p);
    }

    public List getAllSubordinates() {
        return subordinates;
    }

    public int getDeptno() {
        return deptno;
    }

    public int getSalary() {
        return salary;
    }

    public int getSeniority() {
        return seniority;
    }

    public void removeSubordinate(CRPerson aPerson) {
        subordinates.remove(aPerson);
    }

    public void setDeptno(int newValue) {
        this.deptno = newValue;
    }

    public void setSalary(int newValue) {
        this.salary = newValue;
    }

    public void setSeniority(int newValue) {
        this.seniority = newValue;
    }

}
