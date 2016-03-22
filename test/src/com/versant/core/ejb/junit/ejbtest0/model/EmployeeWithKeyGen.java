
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
package com.versant.core.ejb.junit.ejbtest0.model;

import javax.persistence.*;

@Entity (access = AccessType.FIELD)
@Table(name = "EMP_WKG")
public class EmployeeWithKeyGen {
    @Id(generate = GeneratorType.TABLE, generator = "HIGHLOW")
    @Column(name="EMP_NO")
    private int empNo;
    private String eName;
    private double sal;


    public int getEmpNo() {
        return empNo;
    }

    public void setEmpNo(int empNo) {
        this.empNo = empNo;
    }

    public String getEname() {
        return eName;
    }

    public void setEname(String eName) {
        this.eName = eName;
    }


    public double getSal() {
        return sal;
    }

    public void setSal(double sal) {
        this.sal = sal;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Class:")
                .append(this.getClass().getName())
                .append(" :: ")
                .append(" empNo:")
                .append(getEmpNo())
                .append(" ename:")
                .append(getEname())
                .append(" sal:")
                .append(getSal());
        return buf.toString();
    }
}
