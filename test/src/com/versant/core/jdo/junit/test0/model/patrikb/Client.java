
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

public class Client implements Serializable {
   private Person person;
   private Bank bank;
   private Set accounts = new HashSet();

   public Client(Person person, Bank bank) {
      this.person = person;
      this.bank = bank;
      bank.getClients().add(this);
   }

   public Person getPerson() {
      return person;
   }

   public Bank getBank() {
      return bank;
   }

   public Set getAccounts() {
      return accounts;
   }

}

