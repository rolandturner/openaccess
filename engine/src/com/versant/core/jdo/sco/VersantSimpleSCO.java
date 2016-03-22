
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
 * A Simple SCO. Mutable Second Class Objects are required to mark  the field
 * on the owner PC as dirty when they change.
 * <p/>
 * Try to implement ?VersantAdvancedSCO? rather than this. SCO's implementing
 * ?VersantAdvancedSCO? increase performance because they can be re-used and
 * saves lots of new instance creation.
 * <p/>
 * SCO's that are collections should implement ?VersantSCOCollection? for
 * dramatic performance increases on commit and flush.
 * <p/>
 * SCO's that are maps should implement ?VersantSCOMap? for dramatic performance
 * increases on commit and flush.
 *
 * @see VersantAdvancedSCO
 * @see VersantSCOCollection
 * @see VersantSCOMap
 */
public interface VersantSimpleSCO extends Cloneable {

    /**
     * Nullifies references to the owner Object, Field and StateManager
     */
    void makeTransient();

    /**
     * Make a clone. (Creates and returns a copy of this object.)
     * <p/>
     * Mutable Second Class Objects are required to provide a public
     * clone method in order to allow for copying PersistenceCapable
     * objects. In contrast to Object.clone(), this method must not throw a
     * CloneNotSupportedException.
     */
    Object clone();

}
