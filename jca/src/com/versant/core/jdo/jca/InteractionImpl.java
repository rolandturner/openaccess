
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
package com.versant.core.jdo.jca;

import javax.resource.cci.*;

class InteractionImpl implements Interaction {

  InteractionImpl() {
  }

  public void clearWarnings() {
    throw new RuntimeException ("not-supported");
  }

  public void close() {
    throw new RuntimeException ("not-supported");
  }

  public Record execute(InteractionSpec ispec, Record input) {
    throw new RuntimeException ("not-supported");
  }

  public boolean execute(InteractionSpec ispec, Record input, Record output) {
    return false;
  }

  public Connection getConnection() {
    throw new RuntimeException ("not-supported");
  }

  public ResourceWarning getWarnings() {
    return new ResourceWarning("Versant");
  }
}
