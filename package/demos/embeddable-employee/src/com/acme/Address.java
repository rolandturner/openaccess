
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

public class Address {
	private Long id;
	private int version;
	private String street;
	public Address() {}
	public Long getId() { return id; }
	protected void setId(Long id) { this.id = id; }
	public int getVersion() { return version; }
	protected void setVersion(int version) {
		this.version = version;
	}
	public String getStreet() { return street; }
	public void setStreet(String street) {
		this.street = street;
	}
}

