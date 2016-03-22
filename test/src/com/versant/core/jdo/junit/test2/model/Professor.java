
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
package com.versant.core.jdo.junit.test2.model;

/**
 * Created by IntelliJ IDEA.
 * User: jaco
 * Date: 17-Nov-2005
 * Time: 13:20:50
 * To change this template use File | Settings | File Templates.
 */
public class Professor extends Person {
    private String professorRole;
    private int salary;

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public Professor(String name, Country country, String professorRole) {
        super(name, country);
        this.professorRole = professorRole;
    }

    public String getProfessorRole() {
        return professorRole;
    }

    public void setProfessorRole(String professorRole) {
        this.professorRole = professorRole;
    }
}
