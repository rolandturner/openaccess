
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
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@Entity (access = AccessType.PROPERTY)
@Table(name = "EMP")
public class Employee implements java.io.Serializable {

    private int empNo;
    private String eName;
    private double sal;
    private BigDecimal cost;
    private List<Address> address = new ArrayList();
    private Address currentAddress;

    @OneToMany(
            targetEntity=com.versant.core.ejb.junit.ejbtest0.model.Address.class,
            cascade=CascadeType.ALL
    )
    public List getAddress() {
        return address;
    }

    public void setAddress(List address) {
        this.address = address;
    }

    @Column(name="ORDER_COST", updatable=false, precision=2, scale=10)
    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    @Id
    @Column(name="EMP_NO")
    public int getEmpNo() {
        return empNo;
    }

    public void setEmpNo(int empNo) {
        this.empNo = empNo;
    }

    @Column(name="E_NAME", unique = true)
    public String getEName() {
        return eName;
    }

    public void setEName(String eName) {
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
                .append(getEName())
                .append(" sal:")
                .append(getSal());
        return buf.toString();
    }

    public void setCurrentAddress(Address address) {
        this.currentAddress = address;
    }

    @OneToOne(cascade = CascadeType.ALL)
    public Address getCurrentAddress() {
        return currentAddress;
    }
}
