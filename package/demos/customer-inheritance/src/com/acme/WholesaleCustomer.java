
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

public class WholesaleCustomer extends Customer {
	public String terms;
	public String getTerms() { return terms; }
	public void setTerms(String terms) { this.terms = terms; }
	private List<Invoice> invoices = new ArrayList<Invoice>();
	public List<Invoice> getInvoices() { return invoices; }
	public void add(Invoice invoice) {
		invoices.add(invoice);
	}
}
