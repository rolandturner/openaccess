
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

/**
 * @keep-all
 */
public class Employee1 {

    private int age;
    private int salary;
    private String name;
    private Company company;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        if (this.company != company) {
            if (this.company != null) {
                this.company.removeEmployees(this);
            }
            this.company = company;
            if (company != null) {
                company.addEmployees(this);
            }
        }
    }

    public void bonus() {
        salary += 100;
    }

    public String toString() {
        return getName();
    }

}
