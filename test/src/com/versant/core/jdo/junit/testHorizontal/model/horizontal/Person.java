
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

public class Person
{
	protected long personID;
	protected int age;
	protected String name;

	public static class ID implements Serializable
	{
		public long personID;

		public ID() { }

		public ID(String s)
		{
			personID = (new Long(s)).longValue();
		}

		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof Person.ID)) return false;

			final Person.ID id = (Person.ID) o;

			if (this.personID != id.personID) return false;
			return true;
		}

		public int hashCode()
		{
			return (new Long(personID)).hashCode();
		}

		public String toString()
		{
			return (new Long(personID)).toString();
		}
	}

	public int getAge() {
		return age;
	}

	public String getName() {
		return name;
	}

	public long getPersonID() {
		return personID;
	}

	public void setAge(int i) {
		age = i;
	}

	public void setName(String string) {
		name = string;
	}

	public void setPersonID(long l) {
		personID = l;
	}
}
