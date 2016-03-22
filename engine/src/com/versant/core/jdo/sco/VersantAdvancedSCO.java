
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
 * A advanced SCO that can be re-used.
 */
public interface VersantAdvancedSCO extends VersantSimpleSCO {

    /**
     * Returns the owner object of the SCO instance
     *
     * @return owner object
     */
    Object getOwner();

    /**
     * Reset the sco for the next tx. This is only nec for
     * imp's that carry data relevant to a tx.
     */
    void reset();



}
