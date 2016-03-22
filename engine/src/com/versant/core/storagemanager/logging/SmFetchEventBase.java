
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
package com.versant.core.storagemanager.logging;

/**
 * Base class for events logged for operations that return a state packet
 * for a get state type call.
 */
public class SmFetchEventBase extends SmStatesReturnedEvent {

    protected String fieldName;
    private String fetchGroup;

    public SmFetchEventBase(int storageManagerId, int type, String fieldName,
            String fetchGroup) {
        super(storageManagerId, type);
        this.fieldName = fieldName;
        this.fetchGroup = fetchGroup;
    }

    /**
     * If the data is being fetched in response to a missing field then this
     * is the name of the field. It will be fully qualified if it is from
     * a superclass of the requested class or belongs to a different class
     * entirely (i.e. a reference field was navigated causing the fetch).
     */
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Get the fetch group requested. If there are lots of input OIDS then
     * this must be common to all of the OIDs.
     */
    public String getFetchGroup() {
        return fetchGroup;
    }

    public void setFetchGroup(String fetchGroup) {
        this.fetchGroup = fetchGroup;
    }

}
