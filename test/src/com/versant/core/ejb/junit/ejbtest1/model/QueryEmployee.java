
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
package com.versant.core.ejb.junit.ejbtest1.model;

import com.versant.core.ejb.junit.ejbtest1.model.EmploymentTime;
import com.versant.core.ejb.junit.ejbtest1.model.QueryAddress;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import static javax.persistence.GeneratorType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

@Entity
@Table(name = "QUERY_EMPLOYEE")
@SecondaryTable(name = "EJB_SALARY")
@JoinColumn(name = "EMP_ID", referencedColumnName = "EMP_ID")
@GeneratedIdTable(name = "QUERY_EMPLOYEE_GENERATOR_TABLE", table = @Table(name = "EJB_EMPLOYEE_SEQ"), pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT")
@NamedQuery(
        name = "findAllEmployeesByFirstName",
        queryString = "SELECT OBJECT(employee) FROM QueryEmployee employee WHERE employee.firstName = :firstname"
)
public class QueryEmployee implements Serializable {
    private Integer id;
    private int version;
    private String firstName;
    private String lastName;
    private QueryAddress queryAddress;
    private Collection<TelNumber> phoneNumbers;
    private Collection<Task> projects;
    private int salary;
    private EmploymentTime time;
    private Collection<QueryEmployee> managedEmployees;
    private QueryEmployee manager;

    public QueryEmployee() {
    }

    @Id(generate = TABLE, generator = "QUERY_EMPLOYEE_GENERATOR")
    @TableGenerator(name = "QUERY_EMPLOYEE_GENERATOR", table = @Table(name = "QUERY_EMPLOYEE_GENERATOR_TABLE"), pkColumnValue = "EMPLOYEE_SEQ")
    @Column(name = "EMP_ID")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION")
    public int getVersion() {
        return version;
    }

    protected void setVersion(int version) {
        this.version = version;
    }

    @Column(name = "F_NAME", length = 80)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String name) {
        this.firstName = name;
    }

    @Column(name = "L_NAME", length = 80)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String name) {
        this.lastName = name;
    }

    @OneToOne(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "ADDR_ID")
    public QueryAddress getQueryAddress() {
        return queryAddress;
    }

    public void setQueryAddress(QueryAddress queryAddress) {
        this.queryAddress = queryAddress;
    }

    @OneToMany(cascade = ALL)
    @JoinColumn(name = "OWNER_ID", referencedColumnName = "EMP_ID")
    public Collection<TelNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(Collection<TelNumber> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    @OneToMany(cascade = PERSIST)
    @JoinColumn(name = "MANAGER_ID", referencedColumnName = "EMP_ID")
    public Collection<QueryEmployee> getManagedEmployees() {
        return managedEmployees;
    }

    public void setManagedEmployees(Collection<QueryEmployee> managedEmployees) {
        this.managedEmployees = managedEmployees;
    }

    @ManyToOne(cascade = PERSIST, fetch = LAZY)
    @JoinColumn(name = "MANAGER_ID", referencedColumnName = "EMP_ID")
    public QueryEmployee getManager() {
        return manager;
    }

    public void setManager(QueryEmployee manager) {
        this.manager = manager;
    }

    @ManyToMany(cascade = PERSIST)
    public Collection<Task> getProjects() {
        return projects;
    }

    public void setProjects(Collection<Task> projects) {
        this.projects = projects;
    }

    @Column(secondaryTable = "EJB_SALARY")
    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    @Embedded
    public EmploymentTime getTime() {
        return time;
    }

    public void setTime(EmploymentTime time) {
        this.time = time;
    }

    public void addManagedEmployee(QueryEmployee emp) {
        getManagedEmployees().add(emp);
        emp.setManager(this);
    }

    public void addPhoneNumber(TelNumber tel) {
        tel.setOwner(this);
        getPhoneNumbers().add(tel);
    }

    public void addProject(Task theTask) {
        getProjects().add(theTask);
    }

    public void removeManagedEmployee(QueryEmployee emp) {
        getManagedEmployees().remove(emp);
    }

    public void removePhoneNumber(TelNumber tel) {
        getPhoneNumbers().remove(tel);
    }

    public void removeProject(Task theTask) {
        getProjects().remove(theTask);
    }

    public String toString() {
        return "QueryEmployee: " + getId();
    }

    public String displayString() {
        StringBuffer sbuff = new StringBuffer();
        sbuff.append("QueryEmployee ").append(getId()).append(": ").append(getLastName()).append(", ").append(getFirstName()).append(getSalary());

        return sbuff.toString();
    }
}


