
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

@EmbeddableSuperclass(access=AccessType.FIELD)
public class Employee {
	@Id	protected Integer empId;
	@Version protected Integer version;
	@ManyToOne(cascade = CascadeType.ALL) 
	protected Address address;
	public Integer getEmpId() { return empId; }
	public void setEmpId(Integer id) { empId = id; }
	public Address getAddress() { return address; }
	public void setAddress(Address addr) { address = addr; }	
}
