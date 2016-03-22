
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
//
package com.versant.core.jdo.junit.test0.model.patrikb;

import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

public class Company implements Serializable {
   private String name;
   private Contact contact;
   private Set employees = new HashSet();

   public Company(String name, Contact contact) {
      this.name = name;
      this.contact = contact;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Contact getContact() {
      return contact;
   }

   public void setContact(Contact contact) {
      this.contact = contact;
   }

   public Set getEmployees() {
      return employees;
   }

   public void addEmployee(Employee employee) {
      employees.add(employee);
   }
}
