
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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @keep-all
 */
public class Company {

    private String name;
    private ArrayList employees = new ArrayList();
    private Manager manager;
    private String val;

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

    public ArrayList getEmployees() {
        return employees;
    }

    public void addEmployees(Employee1 employee) {
        if (!this.employees.contains(employee)) {
            this.employees.add(employee);
            employee.setCompany(this);
        }
    }

    public void removeEmployees(Employee1 employee) {
        if (this.employees.remove(employee)) {
            employee.setCompany(null);
        }
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        if (this.manager != manager) {
            this.manager = manager;
            if (manager != null) {
                manager.setIsManagerOf(this);
            }
        }
    }

    public long getNumberOfEmployees() {
        return employees.size();
    }

    public Iterator getAllEmployees() {
        return employees.iterator();
    }

    public String toString() {
        return getName();
    }

    public void populate() {
        Employee1 emp;
        for (int i = 0; i < 100; i++) {
            emp = new Employee1();
            addEmployees(emp);
            emp.setName("Employee " + i);
            emp.setAge(20 + i % 30);
            emp.setSalary(100000 + i * 100);
            emp.setCompany(this);
        }
        Manager man = new Manager();
        man.setName(getName() + "'s manager");
        man.setAge(52);
        man.setSalary(10000000);
        man.setCompany(this);
        man.setStocks(25000);
        this.setManager(man);
    }

    public boolean isPopulated() {
        return ((employees.size() > 0) && (manager != null));
    }

}
