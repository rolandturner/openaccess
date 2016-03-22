
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

public class Person implements Serializable {
   private String firstName;
   private String surname;
   private Contact contact;

   public Person(String firstName, String surname, Contact contact) {
      this.firstName = firstName;
      this.surname = surname;
      this.contact = contact;
   }

   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   public String getSurname() {
      return surname;
   }

   public void setSurname(String surname) {
      this.surname = surname;
   }

   public Contact getContact() {
      return contact;
   }

   public void setContact(Contact contact) {
      this.contact = contact;
   }
}
