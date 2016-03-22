
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
package com.versant.core.jdo.junit.testHorizontal.model.horizontal;

import java.io.Serializable;

public class Customer extends Person
{
	private int amountSpent;
	private int numberOfPurchases;

	public static class ID extends Person.ID implements Serializable
	{
		public ID() { }
		public ID(String s)
		{
			super(s);
		}

		public boolean equals(Object o)
		{
			if (!(o instanceof Customer.ID)) return false;
			return super.equals(o);
		}

		public String toString()
		{
			return (new Long(personID)).toString();
		}
	}

	public int getAmountSpent() {
		return amountSpent;
	}

	public int getNumberOfPurchases() {
		return numberOfPurchases;
	}

	public void setAmountSpent(int i) {
		amountSpent = i;
	}

	public void setNumberOfPurchases(int i) {
		numberOfPurchases = i;
	}

	public String toString()
	{
		return "customer: id="+personID+" name="+name;
	}
}
