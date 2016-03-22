
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
@Entity(access=AccessType.FIELD) @Table(name="PT_EMP")
@AttributeOverride(name="address", column=@Column(name="ADDR_ID"))
public class PartTimeEmployee extends Employee {
//	 Inherited empId field mapped to PT_EMP.EMPID
//	 Inherited version field mapped to PT_EMP.VERSION
//	 address field mapping overridden to PT_EMP.ADDR_ID fk
	@Column(name="WAGE")
	protected Float hourlyWage;
	public PartTimeEmployee() {}
	public Float getHourlyWage() { return hourlyWage; }
	public void setHourlyWage(Float wage) { hourlyWage = wage; }
}
