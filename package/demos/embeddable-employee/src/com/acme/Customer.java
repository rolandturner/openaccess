
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

import java.util.*;

public class Customer {
	Long id;
	protected int version;
	Address address;
	String description;
	Collection orders = new Vector();
	Set<DeliveryService> serviceOptions = 
							new HashSet<DeliveryService>();
	public Customer() {}
	public Long getId() { return id; }
	public Address getAddress() { return address; }
	public void setAddress(Address addr) {
		this.address = addr;
	}
	public String getDescription() { return description; }
	public void setDescription(String desc) {
		this.description = desc;
	}
	public Collection getOrders() { return orders; }
	public void add(PurchaseOrder po) {
		orders.add(po);
		po.setCustomer(this);
	}

	public Set<DeliveryService> getServiceOptions() {
		return serviceOptions;
	}
	public String toString() {
		return getDescription() + ' ' + id;
	}
}
