
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
package com.versant.core.jdo.junit.test0.model.patrikb;

import java.io.Serializable;

public class Session extends NonPersistentTransactional {
   private String transactionalField;

   public String getTransactionalField() {
      return transactionalField;
   }

   public void setTransactionalField(String transactionalField) {
      this.transactionalField = transactionalField;
   }
}
