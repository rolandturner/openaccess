
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

import java.math.BigDecimal;
import java.util.Date;
import java.io.Serializable;

public class Transaction implements Serializable {

   static public class PK implements Serializable {
      public String transactionId;

      public PK() {
      }

      public PK(String transactionId) {
         this.transactionId = transactionId;
      }

      public String toString() {
         return transactionId;
      }
   }

   private String transactionId;
   private Date date;
   private Account payerAccount;
   private Account recipientAccount;
   private BigDecimal amount;

   public Transaction(Date date, Account payerAccount, Account recipientAccount, BigDecimal amount) {
      this.date = date;
      this.payerAccount = payerAccount;
      this.recipientAccount = recipientAccount;
      this.amount = amount;
   }

   public String  getTransactionId() {
      return transactionId;
   }

   public Date getDate() {
      return date;
   }

   public Account getPayerAccount() {
      return payerAccount;
   }

   public Account getRecipientAccount() {
      return recipientAccount;
   }

   public BigDecimal getAmount() {
      return amount;
   }
   
   public String toString() {
      return "Transaction{" +
            "transactionId='" + transactionId + "'" +
            ", date=" + date +
            ", payerAccount=" + payerAccount +
            ", recipientAccount=" + recipientAccount +
            ", amount=" + amount +
            "}";
   }

   
}
