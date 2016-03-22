
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

import java.io.Serializable;

public class Employee implements Serializable {
   private Person person;
   private Company company;
   private long salary;

   public Employee(Person person, Company company) {
      this.person = person;
      this.company = company;
   }

   public Company getCompany() {
      return company;
   }

   public long getSalary() {
      return salary;
   }

   public void setSalary(long salary) {
      this.salary = salary;
   }
}
