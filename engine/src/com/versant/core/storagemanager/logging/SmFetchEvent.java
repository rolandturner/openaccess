
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
 * Event logged when data is fetched for an instance.
 */
public class SmFetchEvent extends SmFetchEventBase {

    private String className;
    private String primaryKey;

    public SmFetchEvent(int storageManagerId, int type, String className,
            String primaryKey, String fetchGroup, String fieldName) {
        super(storageManagerId, type, fieldName, fetchGroup);
        this.className = className;
        this.primaryKey = primaryKey;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * Get a long description for this event (e.g. the query text).
     */
    public String getDescription() {
        return className + " " + primaryKey + " " + getFetchGroup() + " " +
                (fieldName == null ? "" : fieldName + " ") +
                returnedSize + " state(s)";
    }

}
