
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
public class CRCourse {
    protected String name = "";

//            [Versant.ItemType(typeof(Student))]
    protected List students = new ArrayList();
    protected CRProfessor teacher = null;

    public CRCourse() {
    }

    public CRCourse(String n) {
        name = n;
    }

    public void addStudent(CRStudent s) {
        students.add(s);
    }


    public String getName() {
        return name;
    }

    public List getStudents() {
        return students;
    }

    public CRProfessor getTeacher() {
        return teacher;
    }

    /*public int hashCode()
    {
        return (name != null ? name.hashCode() : 0);
    }*/
    public void removeStudent(CRStudent aStudent) {
        students.remove(aStudent);
    }

    public void setName(String newValue) {
        this.name = newValue;
    }

    public void setTeacher(CRProfessor p) {
        teacher = p;
    }
}
