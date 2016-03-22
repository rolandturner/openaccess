
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
package com.versant.core.jdo.sco;

/**
 * SCO for java.util.Date
 */
public interface SCODate extends VersantAdvancedSCO {

    /**
     * Sets initial date value without notifying the owner object
     */
    void setTimeInternal(long time);


}
