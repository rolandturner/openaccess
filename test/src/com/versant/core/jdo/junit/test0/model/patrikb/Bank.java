
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

public class Bank extends Company implements Serializable {
   private String bankCode;
   private Set clients = new HashSet();
   private Set accounts = new HashSet();

   public Bank(String name, String bankCode, Contact contact) {
      super(name, contact);
      this.bankCode = bankCode;
   }

   public String getBankCode() {
      return bankCode;
   }

   public Set getClients() {
      return clients;
   }

   public Set getAccounts() {
      return accounts;
   }
}
