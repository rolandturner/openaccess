
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
package com.versant.core.ejb;

/**
 * Interface for Versant EJB Query.
 */
public interface VersantQuery {
    public static final String FETCH_SIZE = "fetchSize";

    /**
     * Indicate that random access to query results is required or not. If this
     * is true then the collection returned by execute can be cast to a
     * List and the get(index) method can be used to get any entry in the list.
     * JDO Genie must use a scrollable JDBC ResultSet to provide this
     * functionality. This may use more database resources (cursors etc.)
     * than a normal forward only ResultSet. This option is useful for paged
     * results i.e. you only want a few results from position n onwards.
     */
    public void setRandomAccess(boolean on);

    public boolean isRandomAccess();

    /**
     * Limit the number of instances to be returned. If this property has
     * been set and {@link #setFetchSize} is not set then the batchSize
     * is set to maxRows.
     *
     * @see #setFetchSize
     * @see #getMaxRows
     */
    public void setMaxRows(int amount);

    /**
     * The maximum number of instances to return.
     *
     * @see #setMaxRows
     */
    public int getMaxRows();

    /**
     * Set the number of instances fetched per server round trip. This
     * property controls JDO Genie's own batching and is also passed
     * through to JDBC drivers that support this. If this property is
     * not set and maxRows is set then the default is maxRows.
     *
     * @see #getFetchSize
     */
    public void setFetchSize(int value);

    /**
     * The number of instances fetched from server per round trip.
     *
     * @see #setFetchSize
     */
    public int getFetchSize();
}
