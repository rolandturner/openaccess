
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
 * SCO's that are managed collections must implement this.
 */
public interface VersantManagedSCOCollection extends VersantSCOCollection {

    /**
     * Not in a managed relationship.
     */
    public static final int MANAGED_NONE = 0;
    /**
     * In a managed one-to-many or master/detail relationship.
     */
    public static final int MANAGED_ONE_TO_MANY = 1;
    /**
     * In a managed many-to-many relationship.
     */
    public static final int MANAGED_MANY_TO_MANY = 2;


    /**
     * Called to complete one side of a many-to-many on add.
     */
    public void manyToManyAdd(Object o);

    /**
     * Called to complete one side of a many-to-many on remove.
     */
    public void manyToManyRemove(Object o);



}
