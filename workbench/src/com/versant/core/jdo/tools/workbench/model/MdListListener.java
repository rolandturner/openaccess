
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
package com.versant.core.jdo.tools.workbench.model;

import java.util.List;

/**
 * Receive insert/update/delete notification from an ObservableList.
 *
 * @see MdObservableList
 */
public interface MdListListener extends MdEventListener {

    /**
     * An object is being inserted into the list.
     *
     * @param index -1 if the object is being added to the end of the list
     * @throws IllegalArgumentException to veto the insert
     */
    public void objectInserting(List list, int index, Object o)
            throws IllegalArgumentException;

    /**
     * An object has been inserted into the list.
     *
     * @param index -1 if the object has been added to the end of the list
     */
    public void objectInserted(List list, int index, Object o);

    /**
     * An object is being deleted from the list. At least one of the index
     * and object parameters will be present.
     *
     * @param index Index of object or -1 if not available
     * @param o     Object being deleted or null if not available
     * @throws IllegalArgumentException to veto the delete
     */
    public void objectDeleting(List list, int index, Object o)
            throws IllegalArgumentException;

    /**
     * An object has been deleted from the list. At least one of the index
     * and object parameters will be present.
     *
     * @param index Index of object or -1 if not available
     * @param o     Object being deleted
     */
    public void objectDeleted(List list, int index, Object o);

    /**
     * A bulk operation (addAll etc) has changed some or all of the
     * elements in the list.
     */
    public void listUpdated(List list);

    /**
     * An object in the list has changed.
     *
     * @param index -1 if the index is not available
     * @param event Optional event object (may be null)
     */
    public void objectUpdated(List list, int index, Object o, Object event);

}

