
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
package com.versant.core.jdo;

import javax.jdo.spi.PersistenceCapable;

/**
 * A class that can be detached by a JDO Genie implementation
 * must implement this interface.
 */
public interface VersantDetachable extends PersistenceCapable {

    /**
     * Tests whether this object is dirty.
     * <p/>
     * Instances that have been modified or newly made persistent
     * return <code>true</code>.
     *
     * @return <code>true</code> if this instance has been modified.
     * @see PersistenceCapable#jdoMakeDirty(String fieldName)
     */
    public boolean versantIsDirty();

    /**
     * Tests whether this field is dirty.
     *
     * @param fieldNo the field no in the metadata
     */
    public boolean versantIsDirty(int fieldNo);

    /**
     * Mark the associated field dirty.
     *
     * @param fieldName the name of the field
     */
    public void versantMakeDirty(String fieldName);

    /**
     * Mark the associated field dirty.
     *
     * @param fieldNo the field no in the metadata
     */
    public void versantMakeDirty(int fieldNo);

    /**
     * Return <code>true</code> if the field is cached in this instance.
     *
     * @param fieldNo the field no in the metadata
     * @return whether the field is cached in the calling instance
     */
    public boolean versantIsLoaded(int fieldNo);

    /**
     * Marks the the field as cached in this instance.
     *
     * @param fieldNo the field no in the metadata
     */
    public void versantSetLoaded(int fieldNo);

    /**
     * Sets the object representing the JDO identity of this instance.
     */
    public void versantSetOID(Object oid);

    /**
     * Return the object representing the JDO identity of this instance.
     *
     * @return the object representing the JDO identity of this instance
     */
    public Object versantGetOID();

    /**
     * The value of the version field on the pc.
     */
    public void versantSetVersion(Object version);

    /**
     * The value of the version field on the pc.
     * This will return null if there are no version fields.
     */
    public Object versantGetVersion();

    /**
     * The VersantDetachedStateManager for this class.
     * If the StateManager is not of type VersantDetachedStateManager then null is returned.
     * The VersantDetachedStateManager is used to keep track of deleted objects.
     *
     * @return the StateManager if it is a VersantDetachedStateManager
     */
    public VersantDetachedStateManager versantGetDetachedStateManager();

}
