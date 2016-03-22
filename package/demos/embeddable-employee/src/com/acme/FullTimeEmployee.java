
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
package com.acme;
import javax.persistence.*;
@Entity
@Table(name="FT_EMP")
public class FullTimeEmployee extends Employee {
//	 Inherited empId field mapped to FTEMPLOYEE.EMPID
//	 Inherited version field mapped to FTEMPLOYEE.VERSION
//	 Inherited address field mapped to FTEMPLOYEE.ADDR fk
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="startDate", column=@Column(name="EMP_START")),
		@AttributeOverride(name="endDate", column=@Column(name="EMP_END"))
	})	
	private EmploymentPeriod employmentPeriod;
	public EmploymentPeriod getEmploymentPeriod() {
		return employmentPeriod;
	}
	public void setEmploymentPeriod(EmploymentPeriod employmentPeriod) {
		this.employmentPeriod = employmentPeriod;
	}
	private Integer salary;
	public FullTimeEmployee() {}
//	 Defaults to FTEMPLOYEE.SALARY
	public Integer getSalary() { return salary; }
	public void setSalary(Integer salary) { 
		this.salary = salary; 
	}
}
