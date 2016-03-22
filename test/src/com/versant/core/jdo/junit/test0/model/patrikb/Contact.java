
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

public class Contact implements Serializable {
   private String street;
   private int streetNumber;
   private String city;
   private int zipCode;
   private String state;

   public Contact(String street, int streetNumber, String city, int zipCode, String state) {
      this.street = street;
      this.streetNumber = streetNumber;
      this.city = city;
      this.zipCode = zipCode;
      this.state = state;
   }

   public String getStreet() {
      return street;
   }

   public void setStreet(String street) {
      this.street = street;
   }

   public int getStreetNumber() {
      return streetNumber;
   }

   public void setStreetNumber(int streetNumber) {
      this.streetNumber = streetNumber;
   }

   public String getCity() {
      return city;
   }

   public void setCity(String city) {
      this.city = city;
   }

   public int getZipCode() {
      return zipCode;
   }

   public void setZipCode(int zipCode) {
      this.zipCode = zipCode;
   }

   public String getState() {
      return state;
   }

   public void setState(String state) {
      this.state = state;
   }
}
