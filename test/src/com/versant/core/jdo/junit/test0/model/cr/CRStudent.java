
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
public class CRStudent extends CREmployee {
    protected int grade = 0;
//            [Versant.ItemType(typeof(Course))]
    protected List takes = new ArrayList();
    protected CRCourse speciality = null;

    public CRStudent() {
    }

    public CRStudent(String name,
            java.util.Date birthday,
            String passportNo,
            CRAddress address,
            int seniority,
            int salery,
            int department,
            int grade)
//    : base(name, birthday, passportNo, address, seniority, salery, department)
    {
        this.grade = grade;
    }

    public void addTakes(CRCourse c) {
        takes.add(c);
        speciality = c;
    }


    public String getActivity() {
        return ("studying for life");
    }

    public int getGrade() {
        return grade;
    }

    public CRCourse getSpeciality() {
        return speciality;
    }

    public List getTakes() {
        return takes;
    }

    public void removeTakes(CRCourse aCourse) {
        takes.remove(aCourse);
    }

    public void setGrade(int aGrade) {
        grade = aGrade;
    }

    public void setSpeciality(CRCourse aCourse) {
        speciality = aCourse;
    }

}
