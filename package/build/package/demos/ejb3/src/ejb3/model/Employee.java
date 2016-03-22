
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
package ejb3.model;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * A Employee with firstName, lastName, address field and Manager.
 */
@Entity (access = AccessType.PROPERTY)
@Table(name = "EMPLOYEE")
public class Employee implements java.io.Serializable {

    private int empNo;
    private String eName;
    private long sal;
    private BigDecimal cost;
    private Address address;
    private Employee manager;

    @OneToOne(cascade = CascadeType.ALL)
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Column(name = "ORDER_COST", updatable = false, precision = 2, scale = 10)
    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    @Id
    @Column(name = "EMP_NO")
    public int getEmpNo() {
        return empNo;
    }

    public void setEmpNo(int empNo) {
        this.empNo = empNo;
    }

    @Column(name = "E_NAME", unique = true)
    public String getEName() {
        return eName;
    }

    public void setEName(String eName) {
        this.eName = eName;
    }

    public long getSal() {
        return sal;
    }

    public void setSal(long sal) {
        this.sal = sal;
    }

    @OneToOne(cascade = CascadeType.ALL)
    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
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


}


