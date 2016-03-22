
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

public class PurchaseOrder {
	private Long id;
	private int version;
	private String itemName;
	private int quantity;
	private Customer customer;
	public PurchaseOrder() {}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	protected int getVersion() { return version; }
	protected void setVersion(int version) {
		this.version = version;
	}
	
	public String getItemName() { return itemName; }
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	
	public int getQuantity() { return quantity; }
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Customer getCustomer() { return customer; }
	public void setCustomer(Customer cust) {
		this.customer = cust;
	}
}
