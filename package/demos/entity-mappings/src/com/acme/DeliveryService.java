
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

public class DeliveryService {
	private String serviceName;
	private int priceCategory;
	private Collection customers;
	public DeliveryService() {}
	public String getServiceName() { return serviceName; }
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public int getPriceCategory() { return priceCategory; }
	public void setPriceCategory(int priceCategory) {
		this.priceCategory = priceCategory;
	}
	public Collection getCustomers() { return customers; }
	public void setCustomers(Collection customers) {
		this.customers = customers;
	}
}
